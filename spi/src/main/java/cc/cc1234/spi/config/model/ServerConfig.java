package cc.cc1234.spi.config.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ServerConfig {

    private String host;

    private int connectTimes = 0;

    private Boolean connected = false;

    private List<String> aclList = new ArrayList<>();

    private Optional<SSHTunnelConfig> sshTunnelConfig = Optional.empty();

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

    public Optional<SSHTunnelConfig> getSshTunnelConfig() {
        return sshTunnelConfig;
    }

    public void setSshTunnelConfig(Optional<SSHTunnelConfig> sshTunnelConfig) {
        this.sshTunnelConfig = sshTunnelConfig;
    }
}
