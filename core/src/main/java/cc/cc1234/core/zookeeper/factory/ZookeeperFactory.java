package cc.cc1234.core.zookeeper.factory;

import cc.cc1234.client.curator.CuratorZookeeperConnectionFactory;
import cc.cc1234.core.configuration.entity.ServerConfiguration;
import cc.cc1234.core.configuration.value.SSHTunnelConfiguration;
import cc.cc1234.core.zookeeper.entity.SSHTunnel;
import cc.cc1234.core.zookeeper.entity.Terminal;
import cc.cc1234.core.zookeeper.entity.Zookeeper;
import cc.cc1234.specification.connection.ZookeeperParams;
import cc.cc1234.specification.listener.ServerListener;
import cc.cc1234.specification.listener.ZookeeperNodeListener;
import cc.cc1234.specification.util.StringWriter;
import cc.cc1234.zookeeper.ZooKeeperMain;

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
                    .sshKeyFilePath(tunnelConfig.getSshKeyFilePath())
                    .sshHost(tunnelConfig.getSshHost())
                    .sshPort(tunnelConfig.getSshPort())
                    .remoteHost(tunnelConfig.getRemoteHost())
                    .remotePort(tunnelConfig.getRemotePort())
                    .build();
        }
        var factory = new CuratorZookeeperConnectionFactory();
        var params = ZookeeperParams.builder()
                .id(serverConfig.getId())
                .url(serverConfig.getConnectionTo())
                .aclList(serverConfig.getAclList())
                .maxRetries(serverConfig.getConnectionConfiguration().getMaxRetries())
                .connectionTimeout(serverConfig.getConnectionConfiguration().getConnectionTimeout())
                .retryIntervalTime(serverConfig.getConnectionConfiguration().getRetryIntervalTime())
                .sessionTimeout(serverConfig.getConnectionConfiguration().getSessionTimeout())
                .build();
        return new Zookeeper(serverConfig.getId(),
                serverConfig.getConnectionTo(),
                () -> factory.createAsync(params, serverListeners),
                tunnel,
                nodeListeners,
                serverListeners);
    }

    public Terminal createTerminal(String id, String url, StringWriter writer) throws Exception {
        writer.write("connecting to " + url + "...\n");
        var factory = new CuratorZookeeperConnectionFactory();
        var params = ZookeeperParams.builder()
                .id(id)
                .url(url)
                .aclList(List.of())
                .build();
        var connection = factory.create(params);
        var zk = connection.getClient().getZookeeperClient().getZooKeeper();
        writer.write("connect success \n");
        var zkMain = new ZooKeeperMain(zk, writer);
        return new Terminal(id, url, connection, zkMain);
    }
}
