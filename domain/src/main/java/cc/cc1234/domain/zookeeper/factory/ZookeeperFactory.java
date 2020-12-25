package cc.cc1234.domain.zookeeper.factory;

import cc.cc1234.client.curator.CuratorZookeeperConnectionFactory;
import cc.cc1234.domain.configuration.entity.ServerConfiguration;
import cc.cc1234.domain.configuration.value.SSHTunnelConfiguration;
import cc.cc1234.domain.zookeeper.entity.SSHTunnel;
import cc.cc1234.domain.zookeeper.entity.Zookeeper;
import cc.cc1234.spi.connection.ZookeeperConnectionFactory;
import cc.cc1234.spi.connection.ZookeeperParams;
import cc.cc1234.spi.listener.ServerListener;
import cc.cc1234.spi.listener.ZookeeperNodeListener;

import java.util.List;

public class ZookeeperFactory {

    public Zookeeper create(ServerConfiguration serverConfig,
                            List<ZookeeperNodeListener> nodeListeners,
                            List<ServerListener> serverListeners) {
        SSHTunnel tunnel = null;
        if (serverConfig.getSshTunnelEnabled() && serverConfig.getSshTunnel() != null) {
            final SSHTunnelConfiguration tunnelConfig = serverConfig.getSshTunnel();
            tunnel = SSHTunnel.builder()
                    .localhost(tunnelConfig.getLocalhost())
                    .localPort(tunnelConfig.getLocalPort())
                    .sshUsername(tunnelConfig.getSshUsername())
                    .sshPassword(tunnelConfig.getSshPassword())
                    .sshHost(tunnelConfig.getSshHost())
                    .sshPort(tunnelConfig.getSshPort())
                    .remoteHost(tunnelConfig.getRemoteHost())
                    .remotePort(tunnelConfig.getRemotePort())
                    .build();
        }
        ZookeeperConnectionFactory factory = new CuratorZookeeperConnectionFactory();
        var params = new ZookeeperParams(serverConfig.getHost(), serverConfig.getAclList());
        return new Zookeeper(serverConfig.getHost(), () -> factory.create(params), tunnel, nodeListeners, serverListeners);
    }
}
