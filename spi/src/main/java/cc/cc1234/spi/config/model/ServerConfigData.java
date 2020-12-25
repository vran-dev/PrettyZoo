package cc.cc1234.spi.config.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ServerConfigData {

    private String host;

    private int connectTimes = 0;

    private List<String> aclList = new ArrayList<>();

    private Boolean sshTunnelEnabled = false;

    private Optional<SSHTunnelConfigData> sshTunnelConfig = Optional.empty();

    public Boolean getSshTunnelEnabled() {
        return sshTunnelEnabled;
    }

    public void setSshTunnelEnabled(Boolean sshTunnelEnabled) {
        this.sshTunnelEnabled = sshTunnelEnabled;
    }

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

    public List<String> getAclList() {
        return aclList;
    }

    public void setAclList(List<String> aclList) {
        this.aclList = aclList;
    }

    public Optional<SSHTunnelConfigData> getSshTunnelConfig() {
        return sshTunnelConfig;
    }

    public void setSshTunnelConfig(Optional<SSHTunnelConfigData> sshTunnelConfig) {
        this.sshTunnelConfig = sshTunnelConfig;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerConfigData that = (ServerConfigData) o;
        return connectTimes == that.connectTimes &&
                host.equals(that.host) &&
                aclList.equals(that.aclList) &&
                sshTunnelConfig.equals(that.sshTunnelConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, connectTimes, aclList, sshTunnelConfig);
    }
}
