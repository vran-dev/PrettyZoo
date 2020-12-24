package cc.cc1234.app.facade;

import cc.cc1234.app.cache.TreeItemCache;
import cc.cc1234.app.util.Asserts;
import cc.cc1234.app.context.ActiveServerContext;
import cc.cc1234.app.fp.Try;
import cc.cc1234.app.util.Fills;
import cc.cc1234.app.view.toast.VToast;
import cc.cc1234.app.vo.ConfigurationVOTransfer;
import cc.cc1234.app.vo.ServerConfigurationVO;
import cc.cc1234.app.vo.ZkNodeSearchResult;
import cc.cc1234.domain.service.ConfigurationDomainService;
import cc.cc1234.domain.service.ZookeeperDomainService;
import cc.cc1234.spi.config.model.RootConfig;
import cc.cc1234.spi.config.model.SSHTunnelConfig;
import cc.cc1234.spi.config.model.ServerConfig;
import cc.cc1234.spi.listener.ConfigurationChangeListener;
import cc.cc1234.spi.listener.ServerListener;
import cc.cc1234.spi.listener.ZookeeperNodeListener;
import cc.cc1234.spi.node.NodeMode;
import com.google.common.base.Strings;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PrettyZooFacade {

    private static final Logger log = LoggerFactory.getLogger(PrettyZooFacade.class);

    private TreeItemCache treeItemCache = TreeItemCache.getInstance();

    private ConfigurationDomainService configService = new ConfigurationDomainService();

    private ZookeeperDomainService zookeeperDomainService = new ZookeeperDomainService();

    public void createNode(String server, String path, String data, NodeMode mode) throws Exception {
        zookeeperDomainService.create(server, path, data, mode.createMode());
    }

    public void deleteNode(String server, String path) {
        try {
            zookeeperDomainService.delete(server, path);
        } catch (Exception e) {
            log.error("delete node failed", e);
            throw new IllegalStateException(e);
        }
    }

    public void connect(String host,
                        List<ZookeeperNodeListener> nodeListeners,
                        List<ServerListener> serverListeners) {
        final ServerConfig serverConfig = configService.get(host).orElseThrow();
        zookeeperDomainService.connect(serverConfig, nodeListeners, serverListeners);
    }

    public void disconnect(String host) {
        Platform.runLater(() -> {
            zookeeperDomainService.disconnect(host);
            treeItemCache.remove(host);
        });
    }

    public void closeAll() {
        zookeeperDomainService.disconnectAll();
    }

    public void syncIfNecessary(String host) {
        zookeeperDomainService.sync(host);
    }

    public boolean hasServerConfig(String host) {
        return configService.contains(host);
    }

    public void saveConfig(ServerConfigurationVO serverConfigurationVO) {
        var serverConfig = new ServerConfig();
        serverConfig.setHost(serverConfigurationVO.getZkServer());
        serverConfig.setAclList(new ArrayList<>(serverConfigurationVO.getAclList()));
        serverConfig.setSshTunnelEnabled(serverConfigurationVO.isSshEnabled());

        var sshTunnelConfig = new SSHTunnelConfig();
        if (serverConfigurationVO.getZkServer().trim().length() > 0) {
            var localHostAndPort = serverConfigurationVO.getZkServer().split(":");
            sshTunnelConfig.setLocalhost(localHostAndPort[0]);
            sshTunnelConfig.setLocalPort(Integer.parseInt(localHostAndPort[1]));
        }

        if (serverConfigurationVO.getRemoteServer().trim().length() > 0) {
            var remoteHostAndPort = serverConfigurationVO.getRemoteServer().split(":");
            sshTunnelConfig.setRemoteHost(remoteHostAndPort[0]);
            sshTunnelConfig.setRemotePort(Integer.parseInt(remoteHostAndPort[1]));
        }

        if (serverConfigurationVO.getSshServer().trim().length() > 0) {
            var sshServerHostAndPort = serverConfigurationVO.getSshServer().split(":");
            sshTunnelConfig.setSshHost(sshServerHostAndPort[0]);
            sshTunnelConfig.setSshPort(Integer.parseInt(sshServerHostAndPort[1]));
        }
        sshTunnelConfig.setSshUsername(serverConfigurationVO.getSshUsername());
        sshTunnelConfig.setPassword(serverConfigurationVO.getSshPassword());
        serverConfig.setSshTunnelConfig(Optional.of(sshTunnelConfig));
        serverConfig.setSshTunnelEnabled(serverConfigurationVO.isSshEnabled());
        configService.save(serverConfig);
    }

    public void removeConfig(String server) {
        configService.remove(server);
    }

    public List<ServerConfigurationVO> loadConfigs(ConfigurationChangeListener changeListener) {
        if (changeListener != null) {
            configService.addListener(changeListener);
        }
        final RootConfig rootConfig = configService.load();
        return rootConfig.getServers().stream().map(ConfigurationVOTransfer::to).collect(Collectors.toList());
    }

    public void exportConfig(File file) {
        Objects.requireNonNull(file);
        configService.export(file);
    }

    public void importConfig(File configFile) {
        Try.of(() -> {
            Asserts.notNull(configFile, "文件不存在");
            Asserts.assertTrue(configFile.isFile(), "请选择文件");
        })
                .onFailure(e -> VToast.error(e.getMessage()))
                .onSuccess(e -> configService.importConfig(configFile));
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

    public void updateData(String host,
                           String nodePath,
                           String data,
                           Consumer<Throwable> errorCallback) {
        Try.of(() -> zookeeperDomainService.set(host, nodePath, data)).onFailure(errorCallback::accept);
    }

}
