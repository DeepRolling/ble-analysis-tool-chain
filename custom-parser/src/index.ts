import fs from 'fs'

type EachBleFrame = {
    "_index": string,
    "_type": string,
    "_score": null,
    "_source": {
        "layers": {
            "frame": {
                "frame.time_epoch": string//"1662442410.991191000",//unit is second
            },
            "nordic_ble":any
            "btle": {
                "btle.advertising_header"?: string,//if this field exist, this packet is an advertisement packet
                //master address, if this field exist with "btle.advertising_header", this packet is CONNECT_IND
                //NOTICE : this packet should only display once within json file due to capturing strategy
                "btle.initiator_address"?: string,
                //these fields exist or un-exist together
                "btle.master_bd_addr"?: string,
                "btle.slave_bd_addr"?: string,
                "btle.data_header"?: {
                    //0x01 : EMPTY_PDU
                    //0x02 : data pdu, indicate this packet is L2CAP packet, include SMP ( Security Manager protocol ) and ATT ( Attribute protocol )
                    //0x03 : control pdu, control link layer connection
                    "btle.data_header.llid": "0x01" | "0x02" | "0x03",
                },
                //only exist when "btle.data_header"."btle.data_header.llid" === "0x03"
                //0x08 : LL_FEATURE_REQ
                //0x09 : LL_FEATURE_RSP
                //0x0c : LL_VERSION_IND
                //0x00 : LL_CONNECTION_UPDATE_IND
                //0x02 : LL_TERMINATE_IND
                "btle.control_opcode"?: "0x08" | "0x09" | "0x0c" | "0x00" | "0x02",
            }
            //these fields exist or un-exist together
            //only exist when "btle.data_header"."btle.data_header.llid" === "0x02"
            "btl2cap"?: any,
            "btatt"?: {
                //0x10 : sent read by group type request
                //0x12 : sent write request
                //0x13 : receive write request
                //0x52 : sent write command
                //0x1b : receive handle value notification
                "btatt.opcode": "0x10" | "0x12" | "0x13" | "0x52" | "0x1b",
            }
        }
    }
}


// key in this profile is type of specific time point, value is corresponding unix timestamp(millisecond)
type AnalysisProfile = {
    'sniffer_start_connect': number
    'sniffer_connect_finish': number
    'sniffer_start_service_discovery': number
    'sniffer_service_discovery_finish': number
    'sniffer_start_info_exchange': number
    'sniffer_info_exchange_finish': number
}

const EXIT_ERROR_CODE_INVALID_PACKET = 1
const EXIT_ERROR_CODE_LACK_SPECIFIC_FRAME = 2

const rawData: string = fs.readFileSync('.\\..\\temp\\ble_data.json', 'utf-8');

//remove the stupid bom
const frames: EachBleFrame[] = JSON.parse(rawData.replace(/^\uFEFF/, ''));
// console.log(JSON.stringify(frames))

//validate json file, if json file is invalid, exist with error code
const connectIndicatorFrames = frames.filter((eachFrame) => {
    return eachFrame._source.layers.btle["btle.initiator_address"] !== undefined && eachFrame._source.layers.btle["btle.advertising_header"] === "f6:41:14:f3:8c:aa"
})
if (connectIndicatorFrames.length !== 1) {
    process.exit(EXIT_ERROR_CODE_INVALID_PACKET)
}
//create map used to record specific packets
const TIME_RECORD_KEY_CONNECT_IND = 'CONNECT_IND'
const TIME_RECORD_KEY_FIRST_CONNECTION_UPDATE_IND = 'FIRST_CONNECTION_UPDATE_IND'
const TIME_RECORD_KEY_FIRST_SENT_READ_BY_GROUP_TYPE = 'FIRST_SENT_READ_BY_GROUP_TYPE'
const TIME_RECORD_KEY_FIRST_SENT_WRITE_REQUEST = 'FIRST_SENT_WRITE_REQUEST'
const TIME_RECORD_KEY_RECEIVE_HANDLE_VALUE_NOTIFICATION = 'RECEIVE_HANDLE_VALUE_NOTIFICATION'
const timeRecordMap = new Map<string, number>();

//sometimes there maybe have multiple frames have same type, only record the first one for analysis
const throwInTimeRecordMapIfNotExist = (key: string, originalTime: string) => {
    if (timeRecordMap.has(key)) {
        return
    }
    //transfer the second time to millisecond time
    const strings = originalTime.split('.');
    let baseSecondTime = strings[0];
    //append three digit to second time to transfer it to millisecond format
    baseSecondTime = baseSecondTime + strings[1].slice(0, 3)
    timeRecordMap.set(key, Number(baseSecondTime))
}
//extract specific frames from packet and throw in map
for (let frame of frames) {
    const layers = frame._source.layers;
    const timeEpoch = layers.frame["frame.time_epoch"];
    if (layers.btle["btle.initiator_address"] !== undefined) {
        throwInTimeRecordMapIfNotExist(TIME_RECORD_KEY_CONNECT_IND, timeEpoch)
        continue
    }
    if (layers.btle["btle.slave_bd_addr"] !== "f6:41:14:f3:8c:aa") {
        //omit the advertisement frame or other frame not relate with specific device
        continue
    }
    const dataHeader = layers.btle["btle.data_header"];
    if (dataHeader !== undefined && dataHeader["btle.data_header.llid"] === "0x03") {
        //this frame is control frame
        if (layers.btle["btle.control_opcode"] === '0x00') {
            throwInTimeRecordMapIfNotExist(TIME_RECORD_KEY_FIRST_CONNECTION_UPDATE_IND, timeEpoch)
            continue
        }
    }
    if (dataHeader !== undefined && dataHeader["btle.data_header.llid"] === "0x02") {
        //this frame is control frame
        const attLayer = layers["btatt"];
        if (attLayer === undefined) {//for robustness, this field must be existed when llid is 0x02
            continue
        }
        switch (attLayer["btatt.opcode"]) {
            case "0x10":
                throwInTimeRecordMapIfNotExist(TIME_RECORD_KEY_FIRST_SENT_READ_BY_GROUP_TYPE, timeEpoch)
                break;
            case "0x12":
                throwInTimeRecordMapIfNotExist(TIME_RECORD_KEY_FIRST_SENT_WRITE_REQUEST, timeEpoch)
                break;
            case "0x1b":
                throwInTimeRecordMapIfNotExist(TIME_RECORD_KEY_RECEIVE_HANDLE_VALUE_NOTIFICATION, timeEpoch)
                break;
            default :
                break;
        }
    }
}
//determine count of filtered frame, if the count less than 5, represent there have some problem when parse packet
if (timeRecordMap.size !== 5) {
    console.error(timeRecordMap)
    process.exit(EXIT_ERROR_CODE_LACK_SPECIFIC_FRAME)
}
const parseResult:AnalysisProfile = {
    'sniffer_start_connect': timeRecordMap.get(TIME_RECORD_KEY_CONNECT_IND)!!,
    'sniffer_connect_finish': timeRecordMap.get(TIME_RECORD_KEY_FIRST_CONNECTION_UPDATE_IND)!!,
    'sniffer_start_service_discovery': timeRecordMap.get(TIME_RECORD_KEY_FIRST_SENT_READ_BY_GROUP_TYPE)!!,
    'sniffer_service_discovery_finish': timeRecordMap.get(TIME_RECORD_KEY_FIRST_SENT_WRITE_REQUEST)!!,
    'sniffer_start_info_exchange': timeRecordMap.get(TIME_RECORD_KEY_FIRST_SENT_WRITE_REQUEST)!!,
    'sniffer_info_exchange_finish': timeRecordMap.get(TIME_RECORD_KEY_RECEIVE_HANDLE_VALUE_NOTIFICATION)!!,
}
const targetJsonFilePath = '.\\..\\temp\\parser_result.json'
fs.writeFileSync(targetJsonFilePath,JSON.stringify(parseResult),'utf-8');
//program exit normally
