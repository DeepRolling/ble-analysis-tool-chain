package com.example.timerecordcollector.bean;

public class ApplicationSideData {

    public ApplicationSideData() {
    }

    public ApplicationSideData(long appStartScan, long appFindDevice, long appStartConnectSlave, long appConnectSlaveSuccess, long appStartDiscoveryService, long appDiscoveryServiceSuccess, long appStartInfoExchange, long appInfoExchangeFinish) {
        this.appStartScan = appStartScan;
        this.appFindDevice = appFindDevice;
        this.appStartConnectSlave = appStartConnectSlave;
        this.appConnectSlaveSuccess = appConnectSlaveSuccess;
        this.appStartDiscoveryService = appStartDiscoveryService;
        this.appDiscoveryServiceSuccess = appDiscoveryServiceSuccess;
        this.appStartInfoExchange = appStartInfoExchange;
        this.appInfoExchangeFinish = appInfoExchangeFinish;
    }

    private long appStartScan;//record by app
    private long appFindDevice;//record by app

    private long appStartConnectSlave;//record by app
    private long appConnectSlaveSuccess;//record by app



    private long appStartDiscoveryService;//record by app
    private long appDiscoveryServiceSuccess;//record by app



    private long appStartInfoExchange;//record by app
    private long appInfoExchangeFinish;//record by app

    public long getAppStartScan() {
        return appStartScan;
    }

    public void setAppStartScan(long appStartScan) {
        this.appStartScan = appStartScan;
    }

    public long getAppFindDevice() {
        return appFindDevice;
    }

    public void setAppFindDevice(long appFindDevice) {
        this.appFindDevice = appFindDevice;
    }

    public long getAppStartConnectSlave() {
        return appStartConnectSlave;
    }

    public void setAppStartConnectSlave(long appStartConnectSlave) {
        this.appStartConnectSlave = appStartConnectSlave;
    }

    public long getAppConnectSlaveSuccess() {
        return appConnectSlaveSuccess;
    }

    public void setAppConnectSlaveSuccess(long appConnectSlaveSuccess) {
        this.appConnectSlaveSuccess = appConnectSlaveSuccess;
    }

    public long getAppStartDiscoveryService() {
        return appStartDiscoveryService;
    }

    public void setAppStartDiscoveryService(long appStartDiscoveryService) {
        this.appStartDiscoveryService = appStartDiscoveryService;
    }

    public long getAppDiscoveryServiceSuccess() {
        return appDiscoveryServiceSuccess;
    }

    public void setAppDiscoveryServiceSuccess(long appDiscoveryServiceSuccess) {
        this.appDiscoveryServiceSuccess = appDiscoveryServiceSuccess;
    }

    public long getAppStartInfoExchange() {
        return appStartInfoExchange;
    }

    public void setAppStartInfoExchange(long appStartInfoExchange) {
        this.appStartInfoExchange = appStartInfoExchange;
    }

    public long getAppInfoExchangeFinish() {
        return appInfoExchangeFinish;
    }

    public void setAppInfoExchangeFinish(long appInfoExchangeFinish) {
        this.appInfoExchangeFinish = appInfoExchangeFinish;
    }

    @Override
    public String toString() {
        return "ApplicationSideData{" +
                "appStartScan=" + appStartScan +
                ", appFindDevice=" + appFindDevice +
                ", appStartConnectSlave=" + appStartConnectSlave +
                ", appConnectSlaveSuccess=" + appConnectSlaveSuccess +
                ", appStartDiscoveryService=" + appStartDiscoveryService +
                ", appDiscoveryServiceSuccess=" + appDiscoveryServiceSuccess +
                ", appStartInfoExchange=" + appStartInfoExchange +
                ", appInfoExchangeFinish=" + appInfoExchangeFinish +
                '}';
    }
}
