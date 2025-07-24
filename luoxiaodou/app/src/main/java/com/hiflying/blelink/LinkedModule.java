package com.hiflying.blelink;

import java.io.Serializable;

public class LinkedModule implements Serializable {

    private static final long serialVersionUID = 833195854008521358L;

    private String mac;
    private String ip;
    private String id;

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public LinkedModule(String mac, String ip, String id) {
        this.mac = mac;
        this.ip = ip;
        this.id = id;
    }

    public LinkedModule() {
    }

    @Override
    public String toString() {
        return "SmartLinkedModule{" +
                "mac='" + mac + '\'' +
                ", ip='" + ip + '\'' +
                ", id='" + id + '\'' +
                '}';
    }
}
