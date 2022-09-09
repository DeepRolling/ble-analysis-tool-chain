package com.example.timerecordcollector.bean;

public class BleCommunicationSummary {

    public BleCommunicationSummary() {
    }

    public BleCommunicationSummary(long appSideFindDevice, long appSideEstablishConnection, long snifferSideEstablishConnection, long appSideServiceDiscovery, long snifferSideServiceDiscovery, long appSideInfoExchange, long snifferSideInfoExchange) {
        this.appSideFindDevice = appSideFindDevice;
        this.appSideEstablishConnection = appSideEstablishConnection;
        this.snifferSideEstablishConnection = snifferSideEstablishConnection;
        this.appSideServiceDiscovery = appSideServiceDiscovery;
        this.snifferSideServiceDiscovery = snifferSideServiceDiscovery;
        this.appSideInfoExchange = appSideInfoExchange;
        this.snifferSideInfoExchange = snifferSideInfoExchange;
    }

    long appSideFindDevice;
    long appSideEstablishConnection;
    long snifferSideEstablishConnection;
    long appSideServiceDiscovery;
    long snifferSideServiceDiscovery;
    long appSideInfoExchange;
    long snifferSideInfoExchange;


    public long getAppSideFindDevice() {
        return appSideFindDevice;
    }

    public void setAppSideFindDevice(long appSideFindDevice) {
        this.appSideFindDevice = appSideFindDevice;
    }

    public long getAppSideEstablishConnection() {
        return appSideEstablishConnection;
    }

    public void setAppSideEstablishConnection(long appSideEstablishConnection) {
        this.appSideEstablishConnection = appSideEstablishConnection;
    }

    public long getSnifferSideEstablishConnection() {
        return snifferSideEstablishConnection;
    }

    public void setSnifferSideEstablishConnection(long snifferSideEstablishConnection) {
        this.snifferSideEstablishConnection = snifferSideEstablishConnection;
    }

    public long getAppSideServiceDiscovery() {
        return appSideServiceDiscovery;
    }

    public void setAppSideServiceDiscovery(long appSideServiceDiscovery) {
        this.appSideServiceDiscovery = appSideServiceDiscovery;
    }

    public long getSnifferSideServiceDiscovery() {
        return snifferSideServiceDiscovery;
    }

    public void setSnifferSideServiceDiscovery(long snifferSideServiceDiscovery) {
        this.snifferSideServiceDiscovery = snifferSideServiceDiscovery;
    }

    public long getAppSideInfoExchange() {
        return appSideInfoExchange;
    }

    public void setAppSideInfoExchange(long appSideInfoExchange) {
        this.appSideInfoExchange = appSideInfoExchange;
    }

    public long getSnifferSideInfoExchange() {
        return snifferSideInfoExchange;
    }

    public void setSnifferSideInfoExchange(long snifferSideInfoExchange) {
        this.snifferSideInfoExchange = snifferSideInfoExchange;
    }

    @Override
    public String toString() {
        return "BleCommunicationSummary{" +
                "appSideFindDevice=" + appSideFindDevice +
                ", appSideEstablishConnection=" + appSideEstablishConnection +
                ", snifferSideEstablishConnection=" + snifferSideEstablishConnection +
                ", appSideServiceDiscovery=" + appSideServiceDiscovery +
                ", snifferSideServiceDiscovery=" + snifferSideServiceDiscovery +
                ", appSideInfoExchange=" + appSideInfoExchange +
                ", snifferSideInfoExchange=" + snifferSideInfoExchange +
                '}';
    }
}
