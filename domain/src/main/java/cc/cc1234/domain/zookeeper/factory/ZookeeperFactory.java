package cc.cc1234.domain.zookeeper.factory;

import cc.cc1234.client.curator.CuratorZookeeperConnectionFactory;
import cc.cc1234.domain.configuration.entity.ServerConfiguration;
import cc.cc1234.domain.configuration.value.SSHTunnelConfiguration;
import cc.cc1234.domain.zookeeper.entity.SSHTunnel;
import cc.cc1234.domain.zookeeper.entity.Terminal;
import cc.cc1234.domain.zookeeper.entity.Zookeeper;
import cc.cc1234.spi.connection.ZookeeperConnection;
import cc.cc1234.spi.connection.ZookeeperConnectionFactory;
import cc.cc1234.spi.connection.ZookeeperParams;
import cc.cc1234.spi.listener.ServerListener;
import cc.cc1234.spi.listener.ZookeeperNodeListener;
import cc.cc1234.spi.util.StringWriter;
import cc.cc1234.zookeeper.ZooKeeperMain;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.ZooKeeper;

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

    public Terminal createTerminal(String host, StringWriter writer) throws Exception {
        writer.write("connecting to " + host + "...\n");
        ZookeeperConnectionFactory factory = new CuratorZookeeperConnectionFactory();
        var params = new ZookeeperParams(host, List.of());
        final ZookeeperConnection connection = factory.create(params);
        final ZooKeeper zk = ((CuratorFramework) connection.getClient()).getZookeeperClient().getZooKeeper();
        writer.write("connect success \n");
        final ZooKeeperMain zkMain = new ZooKeeperMain(zk, writer);
        return new Terminal(host, connection, zkMain);
    }
}
