package cc.cc1234.app.facade;

import cc.cc1234.app.cache.TreeItemCache;
import cc.cc1234.app.context.ActiveServerContext;
import cc.cc1234.app.fp.Try;
import cc.cc1234.app.util.Asserts;
import cc.cc1234.app.util.Fills;
import cc.cc1234.app.view.toast.VToast;
import cc.cc1234.app.vo.ConfigurationVOTransfer;
import cc.cc1234.app.vo.ServerConfigurationVO;
import cc.cc1234.app.vo.ZkNodeSearchResult;
import cc.cc1234.core.configuration.entity.Configuration;
import cc.cc1234.core.configuration.entity.ServerConfiguration;
import cc.cc1234.core.configuration.service.ConfigurationDomainService;
import cc.cc1234.core.configuration.value.SSHTunnelConfiguration;
import cc.cc1234.core.zookeeper.service.ZookeeperDomainService;
import cc.cc1234.specification.listener.ConfigurationChangeListener;
import cc.cc1234.specification.listener.ServerListener;
import cc.cc1234.specification.listener.ZookeeperNodeListener;
import cc.cc1234.specification.node.NodeMode;
import cc.cc1234.specification.util.StringWriter;
import com.google.common.base.Strings;
import javafx.application.Platform;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PrettyZooFacade {

    private static final Logger log = LoggerFactory.getLogger(PrettyZooFacade.class);

    private TreeItemCache treeItemCache = TreeItemCache.getInstance();

    private ZookeeperDomainService zookeeperDomainService = new ZookeeperDomainService();

    private ConfigurationDomainService configurationDomainService = new ConfigurationDomainService();

    public void createNode(String server, String path, String data, NodeMode mode) throws Exception {
        zookeeperDomainService.create(server, path, data, mode);
    }

    public void deleteNode(String server, String path) {
        try {
            zookeeperDomainService.delete(server, path);
        } catch (Exception e) {
            log.error("delete node failed", e);
            throw new IllegalStateException(e);
        }
    }

    public CompletableFuture<Void> connect(String host,
                                     List<ZookeeperNodeListener> nodeListeners,
                                     List<ServerListener> serverListeners) {
        return CompletableFuture.runAsync(() -> {
            var serverConfig = configurationDomainService.get(host).orElseThrow();
            zookeeperDomainService.connect(serverConfig, nodeListeners, serverListeners);
            configurationDomainService.incrementConnectTimes(host);
        });
    }

    public void disconnect(String host) {
        Platform.runLater(() -> {
            zookeeperDomainService.disconnect(host);
            treeItemCache.remove(host);
        });
    }

    public void closeAll() {
        zookeeperDomainService.disconnectAll();
        zookeeperDomainService.closeAllTerminal();
    }

    public void syncIfNecessary(String host) {
        zookeeperDomainService.sync(host);
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

    public boolean hasServerConfiguration(String host) {
        return configurationDomainService.containServerConfig(host);
    }

    public void saveServerConfiguration(ServerConfigurationVO serverConfigurationVO) {
        var tunnelConfigurationBuilder = SSHTunnelConfiguration.builder();
        if (serverConfigurationVO.getZkServer().trim().length() > 0) {
            var localHostAndPort = serverConfigurationVO.getZkServer().split(":");
            tunnelConfigurationBuilder.localhost(localHostAndPort[0]).localPort(Integer.parseInt(localHostAndPort[1]));
        }

        if (serverConfigurationVO.getRemoteServer().trim().length() > 0) {
            var remoteHostAndPort = serverConfigurationVO.getRemoteServer().split(":");
            tunnelConfigurationBuilder.remoteHost(remoteHostAndPort[0]).remotePort(Integer.parseInt(remoteHostAndPort[1]));
        }

        if (serverConfigurationVO.getSshServer().trim().length() > 0) {
            var sshServerHostAndPort = serverConfigurationVO.getSshServer().split(":");
            tunnelConfigurationBuilder.sshHost(sshServerHostAndPort[0]).sshPort(Integer.parseInt(sshServerHostAndPort[1]));
        }
        tunnelConfigurationBuilder.sshUsername(serverConfigurationVO.getSshUsername())
                .sshPassword(serverConfigurationVO.getSshPassword());

        final ServerConfiguration serverConfiguration = ServerConfiguration.builder()
                .alias(serverConfigurationVO.getZkAlias())
                .host(serverConfigurationVO.getZkServer())
                .aclList(new ArrayList<>(serverConfigurationVO.getAclList()))
                .sshTunnelEnabled(serverConfigurationVO.isSshEnabled())
                .sshTunnel(tunnelConfigurationBuilder.build())
                .build();
        configurationDomainService.save(serverConfiguration);
    }

    public Integer getFontSize() {
        return configurationDomainService.get().orElseThrow().getFontConfiguration().getSize();
    }

    public void changeFontSize(Integer newSize) {
        configurationDomainService.save(new Configuration.FontConfiguration(newSize));
    }

    public void deleteServerConfiguration(String server) {
        configurationDomainService.deleteServerConfiguration(server);
    }

    public List<ServerConfigurationVO> loadServerConfigurations(ConfigurationChangeListener changeListener) {
        final Configuration configuration = configurationDomainService.load(List.of(changeListener));
        return configuration.getServerConfigurations()
                .stream()
                .map(ConfigurationVOTransfer::to)
                .collect(Collectors.toList());
    }

    public List<ServerConfigurationVO> getServerConfigurations() {
        final Configuration configuration = configurationDomainService.get().orElseThrow();
        return configuration.getServerConfigurations()
                .stream()
                .map(ConfigurationVOTransfer::to)
                .collect(Collectors.toList());
    }

    public void exportConfig(File file) {
        Objects.requireNonNull(file);
        configurationDomainService.exportConfig(file);
    }

    public void importConfig(File configFile) {
        Try.of(() -> {
            Asserts.notNull(configFile, "文件不存在");
            Asserts.assertTrue(configFile.isFile(), "请选择文件");
        })
                .onFailure(e -> VToast.error(e.getMessage()))
                .onSuccess(e -> configurationDomainService.importConfig(configFile));
    }

    public void startTerminal(String server, StringWriter stream) {
        zookeeperDomainService.initTerminal(server, stream);
    }

    public void executeCommand(String server, String command) {
        try {
            zookeeperDomainService.execute(server, command);
        } catch (Exception e) {
            log.error("execute command failed at  " + server + ":" + command, e);
            VToast.error("命令执行失败，请重试");
        }
    }

    public String executeFourLetterCommand(String server, String fourLetter) {
        return zookeeperDomainService.execute4LetterCommand(server, fourLetter);
    }
}
