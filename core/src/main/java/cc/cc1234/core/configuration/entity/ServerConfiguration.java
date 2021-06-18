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

    private SSHTunnelConfiguration sshTunnel;

    public void update(ServerConfiguration serverConfiguration) {
        if (serverConfiguration.getSshTunnelEnabled() && serverConfiguration.getSshTunnel() == null) {
            throw new IllegalStateException();
        }
        this.aclList = serverConfiguration.getAclList();
        this.sshTunnelEnabled = serverConfiguration.getSshTunnelEnabled();
        this.sshTunnel = serverConfiguration.getSshTunnel();
        this.alias = serverConfiguration.getAlias();
    }

    public void incrementConnectTimes() {
        this.connectTimes += 1;
    }

}
