package cc.cc1234.spi.config.model;

import java.util.ArrayList;
import java.util.List;

public class ServerConfig {

    private String host;

    private int connectTimes = 0;

    private Boolean connected = false;

    private List<String> aclList = new ArrayList<>();


    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public int getConnectTimes() {
        return connectTimes;
    }

    public void setConnectTimes(int connectTimes) {
        this.connectTimes = connectTimes;
    }

    public Boolean getConnected() {
        return connected;
    }

    public void setConnected(Boolean connected) {
        this.connected = connected;
    }

    public List<String> getAclList() {
        return aclList;
    }

    public void setAclList(List<String> aclList) {
        this.aclList = aclList;
    }
}
