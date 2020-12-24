package cc.cc1234.domain.factory;

import cc.cc1234.client.curator.CuratorZookeeperConnectionFactory;
import cc.cc1234.domain.data.entity.SSHTunnel;
import cc.cc1234.domain.data.entity.Zookeeper;
import cc.cc1234.spi.config.model.ServerConfig;
import cc.cc1234.spi.connection.ZookeeperConnectionFactory;
import cc.cc1234.spi.listener.ServerListener;
import cc.cc1234.spi.listener.ZookeeperNodeListener;

import java.util.List;

public class ZookeeperFactory {

    public Zookeeper create(ServerConfig serverConfig,
                            List<ZookeeperNodeListener> nodeListeners,
                            List<ServerListener> serverListeners) {
        SSHTunnel tunnel = null;
        if (serverConfig.getSshTunnelEnabled()) {
            tunnel = serverConfig.getSshTunnelConfig()
                    .map(tunnelConfig -> SSHTunnel.builder()
                            .localhost(tunnelConfig.getLocalhost())
                            .localPort(tunnelConfig.getLocalPort())
                            .sshUsername(tunnelConfig.getSshUsername())
                            .sshPassword(tunnelConfig.getPassword())
                            .sshHost(tunnelConfig.getSshHost())
                            .sshPort(tunnelConfig.getSshPort())
                            .remoteHost(tunnelConfig.getRemoteHost())
                            .remotePort(tunnelConfig.getRemotePort())
                            .build())
                    .orElse(null);
        }
        ZookeeperConnectionFactory factory = new CuratorZookeeperConnectionFactory();
        return new Zookeeper(serverConfig.getHost(), () -> factory.create(serverConfig), tunnel, nodeListeners, serverListeners);
    }
}
