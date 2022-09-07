package com.example.timerecordcollector.bean;

public class BleTimeRecord {

    public BleTimeRecord() {
    }



    private long appStartScan;//record by app
    private long appFindDevice;//record by app


    private long appStartConnectSlave;//record by app
    private long appConnectSlaveSuccess;//record by app

    private long snifferConnectIndicator;//record by sniffed packet
    private long snifferConnectionUpdate;//record by sniffed packet

    private long appStartDiscoveryService;//record by app
    private long appDiscoveryServiceSuccess;//record by app

    private long snifferStartDiscoveryService;//record by sniffed packet
    private long snifferDiscoveryServiceSuccess;//record by sniffed packet


    private long appStartInfoExchange;//record by app
    private long appInfoExchangeFinish;//record by app

    private long snifferStartInfoExchange;//record by sniffed packet
    private long snifferInfoExchangeFinish;//record by sniffed packet
}
