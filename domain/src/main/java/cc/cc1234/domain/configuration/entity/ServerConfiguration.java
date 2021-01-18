package cc.cc1234.domain.configuration.entity;

import cc.cc1234.domain.configuration.value.SSHTunnelConfiguration;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class ServerConfiguration {

    // entity id
    private String host;

    private List<String> aclList;

    private int connectTimes;

    private Boolean sshTunnelEnabled;

    private SSHTunnelConfiguration sshTunnel;

    public void update(ServerConfiguration serverConfiguration) {
        if (serverConfiguration.getSshTunnelEnabled() && serverConfiguration.getSshTunnel() == null) {
            throw new IllegalStateException();
        }
        this.aclList = serverConfiguration.getAclList();
        this.sshTunnelEnabled = serverConfiguration.getSshTunnelEnabled();
        this.sshTunnel = serverConfiguration.getSshTunnel();
        this.connectTimes = serverConfiguration.getConnectTimes();
    }

    public void incrementConnectTimes() {
        this.connectTimes += 1;
    }
}
