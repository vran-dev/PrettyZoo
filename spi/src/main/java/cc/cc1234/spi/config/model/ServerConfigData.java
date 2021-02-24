package cc.cc1234.spi.config.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ServerConfigData {

    private String host;

    private String alias;

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

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ServerConfigData that = (ServerConfigData) o;
        return connectTimes == that.connectTimes && Objects.equals(host, that.host) && Objects.equals(alias, that.alias) && Objects.equals(aclList, that.aclList) && Objects.equals(sshTunnelEnabled, that.sshTunnelEnabled) && Objects.equals(sshTunnelConfig, that.sshTunnelConfig);
    }

    @Override
    public int hashCode() {
        return Objects.hash(host, alias, connectTimes, aclList, sshTunnelEnabled, sshTunnelConfig);
    }
}
