package com.example.timerecordcollector.bean;

import com.example.timerecordcollector.autoRunner.ParserResult;

public class BleTimeRecord {

    public BleTimeRecord() {
    }

    private ApplicationSideData applicationSideData;
    private ParserResult wiresharkParseResult;

    public void gainApplicationSideData( ApplicationSideData applicationSideData){
        this.applicationSideData = applicationSideData;
    }

    public void gainWiresharkSideData(ParserResult wiresharkParseResult){
        this.wiresharkParseResult = wiresharkParseResult;
    }


    public BleCommunicationSummary summaryData(){
        BleCommunicationSummary bleCommunicationSummary = new BleCommunicationSummary();
        //application side data calculate
        bleCommunicationSummary.setAppSideFindDevice(applicationSideData.getAppFindDevice() - applicationSideData.getAppStartScan());
        bleCommunicationSummary.setAppSideEstablishConnection(applicationSideData.getAppConnectSlaveSuccess() - applicationSideData.getAppStartConnectSlave());
        bleCommunicationSummary.setAppSideServiceDiscovery(applicationSideData.getAppDiscoveryServiceSuccess() - applicationSideData.getAppStartDiscoveryService());
        bleCommunicationSummary.setAppSideInfoExchange(applicationSideData.getAppInfoExchangeFinish() - applicationSideData.getAppStartInfoExchange());
        //wireshark side data calculate
        bleCommunicationSummary.setSnifferSideEstablishConnection(wiresharkParseResult.getSniffer_connect_finish() - wiresharkParseResult.getSniffer_start_connect());
        bleCommunicationSummary.setSnifferSideServiceDiscovery(wiresharkParseResult.getSniffer_service_discovery_finish() - wiresharkParseResult.getSniffer_start_service_discovery());
        bleCommunicationSummary.setSnifferSideInfoExchange(wiresharkParseResult.getSniffer_info_exchange_finish() - wiresharkParseResult.getSniffer_start_info_exchange());
        return bleCommunicationSummary;
    }


}
