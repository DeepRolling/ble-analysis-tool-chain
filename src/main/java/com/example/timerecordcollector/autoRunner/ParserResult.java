package com.example.timerecordcollector.autoRunner;


public class ParserResult {

    public ParserResult() {
    }

    public ParserResult(long sniffer_start_connect, long sniffer_connect_finish, long sniffer_start_service_discovery, long sniffer_service_discovery_finish, long sniffer_start_info_exchange, long sniffer_info_exchange_finish) {
        this.sniffer_start_connect = sniffer_start_connect;
        this.sniffer_connect_finish = sniffer_connect_finish;
        this.sniffer_start_service_discovery = sniffer_start_service_discovery;
        this.sniffer_service_discovery_finish = sniffer_service_discovery_finish;
        this.sniffer_start_info_exchange = sniffer_start_info_exchange;
        this.sniffer_info_exchange_finish = sniffer_info_exchange_finish;
    }

    private long sniffer_start_connect;
    private long sniffer_connect_finish;
    private long sniffer_start_service_discovery;
    private long sniffer_service_discovery_finish;
    private long sniffer_start_info_exchange;
    private long sniffer_info_exchange_finish;
    public void setSniffer_start_connect(long sniffer_start_connect) {
        this.sniffer_start_connect = sniffer_start_connect;
    }
    public long getSniffer_start_connect() {
        return sniffer_start_connect;
    }

    public void setSniffer_connect_finish(long sniffer_connect_finish) {
        this.sniffer_connect_finish = sniffer_connect_finish;
    }
    public long getSniffer_connect_finish() {
        return sniffer_connect_finish;
    }

    public void setSniffer_start_service_discovery(long sniffer_start_service_discovery) {
        this.sniffer_start_service_discovery = sniffer_start_service_discovery;
    }
    public long getSniffer_start_service_discovery() {
        return sniffer_start_service_discovery;
    }

    public void setSniffer_service_discovery_finish(long sniffer_service_discovery_finish) {
        this.sniffer_service_discovery_finish = sniffer_service_discovery_finish;
    }
    public long getSniffer_service_discovery_finish() {
        return sniffer_service_discovery_finish;
    }

    public void setSniffer_start_info_exchange(long sniffer_start_info_exchange) {
        this.sniffer_start_info_exchange = sniffer_start_info_exchange;
    }
    public long getSniffer_start_info_exchange() {
        return sniffer_start_info_exchange;
    }

    public void setSniffer_info_exchange_finish(long sniffer_info_exchange_finish) {
        this.sniffer_info_exchange_finish = sniffer_info_exchange_finish;
    }
    public long getSniffer_info_exchange_finish() {
        return sniffer_info_exchange_finish;
    }

    @Override
    public String toString() {
        return "ParserResult{" +
                "sniffer_start_connect=" + sniffer_start_connect +
                ", sniffer_connect_finish=" + sniffer_connect_finish +
                ", sniffer_start_service_discovery=" + sniffer_start_service_discovery +
                ", sniffer_service_discovery_finish=" + sniffer_service_discovery_finish +
                ", sniffer_start_info_exchange=" + sniffer_start_info_exchange +
                ", sniffer_info_exchange_finish=" + sniffer_info_exchange_finish +
                '}';
    }
}