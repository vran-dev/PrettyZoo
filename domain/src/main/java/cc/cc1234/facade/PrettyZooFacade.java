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

import java.io.File;

public class PrettyZooFacade {

    private PrettyZooConfigService configService = new PrettyZooConfigService();

    private ZookeeperConnectionManager connectionManager = ZookeeperConnectionManager.instance();

    private ListenerManager listenerManager = ListenerManager.instance();

    private SSHTunnelManager sshTunnelManager = SSHTunnelManager.instance();


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

    public void exportConfig(File file) {
        configService.export(file);
    }

    public void importConfig(File configFile) {
        configService.importConfig(configFile);
    }
}
