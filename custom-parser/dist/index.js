"use strict";
var __importDefault = (this && this.__importDefault) || function (mod) {
    return (mod && mod.__esModule) ? mod : { "default": mod };
};
Object.defineProperty(exports, "__esModule", { value: true });
const fs_1 = __importDefault(require("fs"));
const EXIT_ERROR_CODE_INVALID_PACKET = 1;
const EXIT_ERROR_CODE_LACK_SPECIFIC_FRAME = 2;
const rawData = fs_1.default.readFileSync('.\\..\\temp\\ble_data.json', 'utf-8');
//remove the stupid bom
const frames = JSON.parse(rawData.replace(/^\uFEFF/, ''));
// console.log(JSON.stringify(frames))
//validate json file, if json file is invalid, exist with error code
const connectIndicatorFrames = frames.filter((eachFrame) => {
    return eachFrame._source.layers.btle["btle.initiator_address"] !== undefined;
});
if (connectIndicatorFrames.length !== 1) {
    process.exit(EXIT_ERROR_CODE_INVALID_PACKET);
}
//create map used to record specific packets
const TIME_RECORD_KEY_CONNECT_IND = 'CONNECT_IND';
const TIME_RECORD_KEY_FIRST_CONNECTION_UPDATE_IND = 'FIRST_CONNECTION_UPDATE_IND';
const TIME_RECORD_KEY_FIRST_SENT_READ_BY_GROUP_TYPE = 'FIRST_SENT_READ_BY_GROUP_TYPE';
const TIME_RECORD_KEY_FIRST_SENT_WRITE_REQUEST = 'FIRST_SENT_WRITE_REQUEST';
const TIME_RECORD_KEY_RECEIVE_HANDLE_VALUE_NOTIFICATION = 'RECEIVE_HANDLE_VALUE_NOTIFICATION';
const timeRecordMap = new Map();
//sometimes there maybe have multiple frames have same type, only record the first one for analysis
const throwInTimeRecordMapIfNotExist = (key, originalTime) => {
    if (timeRecordMap.has(key)) {
        return;
    }
    //transfer the second time to millisecond time
    const strings = originalTime.split('.');
    let baseSecondTime = strings[0];
    //append three digit to second time to transfer it to millisecond format
    baseSecondTime = baseSecondTime + strings[1].slice(0, 3);
    timeRecordMap.set(key, Number(baseSecondTime));
};
//extract specific frames from packet and throw in map
for (let frame of frames) {
    const layers = frame._source.layers;
    const timeEpoch = layers.frame["frame.time_epoch"];
    if (layers.btle["btle.initiator_address"] !== undefined) {
        throwInTimeRecordMapIfNotExist(TIME_RECORD_KEY_CONNECT_IND, timeEpoch);
        continue;
    }
    if (layers.btle["btle.slave_bd_addr"] !== "f6:41:14:f3:8c:aa") {
        //omit the advertisement frame or other frame not relate with specific device
        continue;
    }
    const dataHeader = layers.btle["btle.data_header"];
    if (dataHeader !== undefined && dataHeader["btle.data_header.llid"] === "0x03") {
        //this frame is control frame
        if (layers.btle["btle.control_opcode"] === '0x00') {
            throwInTimeRecordMapIfNotExist(TIME_RECORD_KEY_FIRST_CONNECTION_UPDATE_IND, timeEpoch);
            continue;
        }
    }
    if (dataHeader !== undefined && dataHeader["btle.data_header.llid"] === "0x02") {
        //this frame is control frame
        const attLayer = layers["btatt"];
        if (attLayer === undefined) { //for robustness, this field must be existed when llid is 0x02
            continue;
        }
        switch (attLayer["btatt.opcode"]) {
            case "0x10":
                throwInTimeRecordMapIfNotExist(TIME_RECORD_KEY_FIRST_SENT_READ_BY_GROUP_TYPE, timeEpoch);
                break;
            case "0x12":
                throwInTimeRecordMapIfNotExist(TIME_RECORD_KEY_FIRST_SENT_WRITE_REQUEST, timeEpoch);
                break;
            case "0x1b":
                throwInTimeRecordMapIfNotExist(TIME_RECORD_KEY_RECEIVE_HANDLE_VALUE_NOTIFICATION, timeEpoch);
                break;
            default:
                break;
        }
    }
}
//determine count of filtered frame, if the count less than 5, represent there have some problem when parse packet
if (timeRecordMap.size !== 5) {
    console.error(timeRecordMap);
    process.exit(EXIT_ERROR_CODE_LACK_SPECIFIC_FRAME);
}
const parseResult = {
    'sniffer_start_connect': timeRecordMap.get(TIME_RECORD_KEY_CONNECT_IND),
    'sniffer_connect_finish': timeRecordMap.get(TIME_RECORD_KEY_FIRST_CONNECTION_UPDATE_IND),
    'sniffer_start_service_discovery': timeRecordMap.get(TIME_RECORD_KEY_FIRST_SENT_READ_BY_GROUP_TYPE),
    'sniffer_service_discovery_finish': timeRecordMap.get(TIME_RECORD_KEY_FIRST_SENT_WRITE_REQUEST),
    'sniffer_start_info_exchange': timeRecordMap.get(TIME_RECORD_KEY_FIRST_SENT_WRITE_REQUEST),
    'sniffer_info_exchange_finish': timeRecordMap.get(TIME_RECORD_KEY_RECEIVE_HANDLE_VALUE_NOTIFICATION),
};
const targetJsonFilePath = '.\\..\\temp\\parser_result.json';
fs_1.default.writeFileSync(targetJsonFilePath, JSON.stringify(parseResult), 'utf-8');
//program exit normally
