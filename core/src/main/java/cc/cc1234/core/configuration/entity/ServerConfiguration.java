package cc.cc1234.core.configuration.entity;

import cc.cc1234.core.configuration.value.SSHTunnelConfiguration;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ServerConfiguration {

    /**
     * host:port
     */
    private String url;

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
    private ServerConnectionAdvanceConfiguration connectionAdvanceConfiguration
            = new ServerConnectionAdvanceConfiguration();

    public void update(ServerConfiguration update) {
        if (update.getSshTunnelEnabled() && update.getSshTunnel() == null) {
            throw new IllegalStateException();
        }
        this.aclList = update.getAclList();
        this.sshTunnelEnabled = update.getSshTunnelEnabled();
        this.sshTunnel = update.getSshTunnel();
        this.alias = update.getAlias();
        this.enableConnectionAdvanceConfiguration = update.getEnableConnectionAdvanceConfiguration();
        // advance config
        ServerConnectionAdvanceConfiguration advanceConfig = update.getConnectionAdvanceConfiguration();
        this.connectionAdvanceConfiguration.setConnectionTimeout(advanceConfig.getConnectionTimeout());
        this.connectionAdvanceConfiguration.setSessionTimeout(advanceConfig.getSessionTimeout());
        this.connectionAdvanceConfiguration.setMaxRetries(advanceConfig.getMaxRetries());
        this.connectionAdvanceConfiguration.setRetryIntervalTime(advanceConfig.getRetryIntervalTime());
    }

    public void incrementConnectTimes() {
        this.connectTimes += 1;
    }

}
