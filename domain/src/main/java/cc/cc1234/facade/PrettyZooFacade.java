package cc.cc1234.facade;

import cc.cc1234.manager.ListenerManager;
import cc.cc1234.manager.SSHTunnelManager;
import cc.cc1234.manager.ZookeeperConnectionManager;
import cc.cc1234.service.PrettyZooConfigService;
import cc.cc1234.spi.config.model.RootConfig;
import cc.cc1234.spi.config.model.ServerConfig;
import cc.cc1234.spi.connection.ZookeeperConnection;
import cc.cc1234.spi.listener.PrettyZooConfigChangeListener;
import cc.cc1234.spi.listener.ZookeeperNodeListener;
import cc.cc1234.spi.node.NodeMode;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

public class PrettyZooFacade {

    private PrettyZooConfigService configService = new PrettyZooConfigService();

    private ZookeeperConnectionManager connectionManager = ZookeeperConnectionManager.instance();

    private ListenerManager listenerManager = ListenerManager.instance();

    private SSHTunnelManager sshTunnelManager = SSHTunnelManager.instance();

    public void addNode(String server, String path, String data, boolean recursive, NodeMode mode) throws Exception {
        connectionManager.getConnection(server).create(path, data, recursive, mode.createMode());
    }

    public void setData(String server, String path, String data) throws Exception {
        connectionManager.getConnection(server).setData(path, data);
    }

    public void deleteNode(String server, String path, boolean recursive) throws Exception {
        connectionManager.getConnection(server).delete(path, recursive);
    }

    public void connect(ServerConfig config) throws Exception {
        // if tunnel config exists, must be create ssh tunnel before connect server
        CountDownLatch latch = new CountDownLatch(1);
        config.getSshTunnelConfig()
                .map(sshTunnelConfig -> {
                    latch.countDown();
                    sshTunnelManager.forwarding(sshTunnelConfig);
                    return true;
                })
                .orElseGet(() -> {
                    latch.countDown();
                    return false;
                });
        latch.await(3000, TimeUnit.SECONDS);
        connectionManager.connect(config);
        configService.add(config);
    }

    public void increaseConnectTimes(String host) {
        configService.increaseConnectTimes(host);
    }

    private void close(String server) {
        connectionManager.getConnectionOpt(server).ifPresent(ZookeeperConnection::close);
    }

    public void close() {
        connectionManager.closeAll();
        listenerManager.clear();
        sshTunnelManager.closeAll();
    }

    public void syncIfNecessary(String host) {
        connectionManager.getConnection(host).sync(listenerManager.getZookeeperNodeListeners());
    }

    public boolean hasServerConfig(String host) {
        return configService.contains(host);
    }

    public void addConfig(ServerConfig serverConfig) {
        configService.add(serverConfig);
    }

    public void removeConfig(String server) {
        configService.remove(server);
    }

    public RootConfig loadConfig() {
        return configService.load();
    }

    public void registerConfigChangeListener(PrettyZooConfigChangeListener listener) {
        listenerManager.add(listener);
    }

    public void registerNodeListener(ZookeeperNodeListener listener) {
        listenerManager.add(listener);
    }
}
