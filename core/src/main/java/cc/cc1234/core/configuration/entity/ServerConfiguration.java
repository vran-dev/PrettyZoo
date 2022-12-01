package cc.cc1234.core.configuration.entity;

import cc.cc1234.core.configuration.value.SSHTunnelConfiguration;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ServerConfiguration {

    private String id;

    private String host;

    private Integer port;

    @Builder.Default
    private String alias = "";

    @Builder.Default
    private List<String> aclList = List.of();

    private int connectTimes;

    @Builder.Default
    private Boolean sshTunnelEnabled = false;

    @Builder.Default
    private Boolean enableConnectionAdvanceConfiguration = false;

    private SSHTunnelConfiguration sshTunnel;

    @Builder.Default
    private ConnectionConfiguration connectionConfiguration = new ConnectionConfiguration();

    public void update(ServerConfiguration update) {
        if (update.getSshTunnelEnabled() && update.getSshTunnel() == null) {
            throw new IllegalStateException();
        }
        this.host = update.getHost();
        this.port = update.getPort();
        this.aclList = update.getAclList();
        this.sshTunnelEnabled = update.getSshTunnelEnabled();
        this.sshTunnel = update.getSshTunnel();
        this.alias = update.getAlias();
        this.enableConnectionAdvanceConfiguration = update.getEnableConnectionAdvanceConfiguration();
        // advance config
        ConnectionConfiguration connectionConfig = update.getConnectionConfiguration();
        this.connectionConfiguration.setConnectionTimeout(connectionConfig.getConnectionTimeout());
        this.connectionConfiguration.setSessionTimeout(connectionConfig.getSessionTimeout());
        this.connectionConfiguration.setMaxRetries(connectionConfig.getMaxRetries());
        this.connectionConfiguration.setRetryIntervalTime(connectionConfig.getRetryIntervalTime());
    }

    public void incrementConnectTimes() {
        this.connectTimes += 1;
    }

    public String getLabel() {
        if (alias != null && !alias.isBlank()) {
            return alias;
        }
        if (sshTunnelEnabled) {
            return getSshTunnel().getRemoteHost() + ":" + getSshTunnel().getRemotePort();
        }
        return host + ":" + port;
    }

    public String getConnectionTo() {
        // TODO if ssh enabled, use localhost && random port
        return host + ":" + port;
    }
}
