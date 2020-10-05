package cc.cc1234.app.facade;

import cc.cc1234.app.cache.TreeItemCache;
import cc.cc1234.app.context.ActiveServerContext;
import cc.cc1234.app.util.Fills;
import cc.cc1234.app.vo.ZkNodeSearchResult;
import cc.cc1234.app.vo.ServerConfigVO;
import cc.cc1234.manager.ListenerManager;
import cc.cc1234.manager.SSHTunnelManager;
import cc.cc1234.manager.ZookeeperConnectionManager;
import cc.cc1234.service.PrettyZooConfigService;
import cc.cc1234.spi.config.model.RootConfig;
import cc.cc1234.spi.config.model.SSHTunnelConfig;
import cc.cc1234.spi.config.model.ServerConfig;
import cc.cc1234.spi.connection.ZookeeperConnection;
import cc.cc1234.spi.listener.PrettyZooConfigChangeListener;
import cc.cc1234.spi.listener.ZookeeperNodeListener;
import cc.cc1234.spi.node.NodeMode;
import com.google.common.base.Strings;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PrettyZooFacade {

    private static final Logger log = LoggerFactory.getLogger(PrettyZooFacade.class);

    private TreeItemCache treeItemCache = TreeItemCache.getInstance();

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

    public void deleteNode(String server, String path, boolean recursive) {
        try {
            connectionManager.getConnection(server).delete(path, recursive);
        } catch (Exception e) {
            log.error("delete node failed", e);
        }
    }

    public void connect(String host) throws Exception {
        Optional<ServerConfig> config = configService.get(host);
        connect(config.orElseThrow(() -> new IllegalStateException("server not exists")));
    }

    public ZookeeperConnection connect(ServerConfig config) throws Exception {
        if (connectionManager.getConnection(config.getHost()) != null) {
            return connectionManager.getConnection(config.getHost());
        }
        // if tunnel config exists, must be create ssh tunnel before connect server
        return CompletableFuture
                .runAsync(() -> {
                    if (config.getSshTunnelEnabled()) {
                        config.getSshTunnelConfig()
                                .map(sshTunnelConfig -> {
                                    sshTunnelManager.forwarding(sshTunnelConfig);
                                    return true;
                                })
                                .orElse(false);
                    }
                })
                .thenApply(res -> {
                    try {
                        var connection = connectionManager.connect(config);
                        configService.add(config);
                        return connection;
                    } catch (Exception e) {
                        throw new RuntimeException(e);
                    }
                })
                .get();
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

    public void saveConfig(ServerConfigVO serverConfigVO) {
        var serverConfig = new ServerConfig();
        serverConfig.setHost(serverConfigVO.getZkServer());
        serverConfig.setAclList(new ArrayList<>(serverConfigVO.getAclList()));
        serverConfig.setSshTunnelEnabled(serverConfigVO.isSshEnabled());

        var sshTunnelConfig = new SSHTunnelConfig();
        if (serverConfigVO.getZkServer().trim().length() > 0) {
            var localHostAndPort = serverConfigVO.getZkServer().split(":");
            sshTunnelConfig.setLocalhost(localHostAndPort[0]);
            sshTunnelConfig.setLocalPort(Integer.parseInt(localHostAndPort[1]));
        }

        if (serverConfigVO.getRemoteServer().trim().length() > 0) {
            var remoteHostAndPort = serverConfigVO.getRemoteServer().split(":");
            sshTunnelConfig.setRemoteHost(remoteHostAndPort[0]);
            sshTunnelConfig.setRemotePort(Integer.parseInt(remoteHostAndPort[1]));
        }

        if (serverConfigVO.getSshServer().trim().length() > 0) {
            var sshServerHostAndPort = serverConfigVO.getSshServer().split(":");
            sshTunnelConfig.setSshHost(sshServerHostAndPort[0]);
            sshTunnelConfig.setSshPort(Integer.parseInt(sshServerHostAndPort[1]));
        }
        sshTunnelConfig.setSshUsername(serverConfigVO.getSshUsername());
        sshTunnelConfig.setPassword(serverConfigVO.getSshPassword());
        serverConfig.setSshTunnelConfig(Optional.of(sshTunnelConfig));
        serverConfig.setSshTunnelEnabled(serverConfigVO.isSshEnabled());
        configService.save(serverConfig);
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

    public List<ZkNodeSearchResult> onSearch(String input) {
        final String host = ActiveServerContext.get();
        if (Strings.isNullOrEmpty(input)) {
            return Collections.emptyList();
        }
        final List<ZkNodeSearchResult> res = treeItemCache.search(host, input)
                .stream()
                .map(item -> {
                    String path = item.getValue().getPath();
                    final List<Text> highlights = Fills.fill(path, input, Text::new,
                            s -> {
                                final Text highlight = new Text(s);
                                highlight.setFill(Color.RED);
                                return highlight;
                            });
                    final TextFlow textFlow = new TextFlow(highlights.toArray(new Text[0]));
                    return new ZkNodeSearchResult(path, textFlow, item);
                })
                .collect(Collectors.toList());
        return res;
    }

    public boolean nodeExists(String nodeAbsolutePath) {
        return treeItemCache.hasNode(ActiveServerContext.get(), nodeAbsolutePath);
    }

    public void updateData(String nodePath, String data, Consumer<Exception> errorCallback) {
        try {
            connectionManager.getConnection(ActiveServerContext.get()).setData(nodePath, data);
        } catch (Exception e) {
            errorCallback.accept(e);
        }
    }
}
