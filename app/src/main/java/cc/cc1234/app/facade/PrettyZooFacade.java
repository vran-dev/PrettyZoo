package cc.cc1234.app.facade;

import cc.cc1234.app.cache.TreeItemCache;
import cc.cc1234.app.context.ActiveServerContext;
import cc.cc1234.app.context.LogTailerThreadContext;
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
import cc.cc1234.specification.config.model.ConfigData;
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
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Paths;
import java.util.*;
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

    public void deleteNode(String server, List<String> pathList) {
        try {
            zookeeperDomainService.delete(server, pathList);
        } catch (Exception e) {
            log.error("delete node failed", e);
            throw new IllegalStateException(e);
        }
    }

    public CompletableFuture<Void> connect(String url,
                                           List<ZookeeperNodeListener> nodeListeners,
                                           List<ServerListener> serverListeners) {
        return CompletableFuture.runAsync(() -> {
            var serverConfig = configurationDomainService.get(url).orElseThrow();
            zookeeperDomainService.connect(serverConfig, nodeListeners, serverListeners);
            configurationDomainService.incrementConnectTimes(url);
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
        LogTailerThreadContext.stop();
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
                    var highlights = Fills.fill(path, input, Text::new,
                            s -> {
                                final Text highlight = new Text(s);
                                highlight.setFill(Color.RED);
                                return highlight;
                            });
                    var textFlow = new TextFlow(highlights.toArray(new Text[0]));
                    return new ZkNodeSearchResult(path, textFlow, item);
                })
                .collect(Collectors.toList());
        return res;
    }

    public boolean nodeExists(String nodeAbsolutePath) {
        return treeItemCache.hasNode(ActiveServerContext.get(), nodeAbsolutePath);
    }

    public Stat updateData(String host,
                           String nodePath,
                           String data,
                           Consumer<Throwable> errorCallback) {
        return Try.of(() -> zookeeperDomainService.set(host, nodePath, data))
                .onFailure(errorCallback::accept)
                .get();
    }

    public boolean hasServerConfiguration(String host) {
        return configurationDomainService.containServerConfig(host);
    }

    public void saveServerConfiguration(ServerConfigurationVO serverConfigurationVO) {
        var tunnelConfigurationBuilder = SSHTunnelConfiguration.builder();
        if (serverConfigurationVO.getRemoteServer().trim().length() > 0) {
            tunnelConfigurationBuilder.remoteHost(serverConfigurationVO.getRemoteServer())
                    .remotePort(serverConfigurationVO.getRemoteServerPort());
        }

        if (serverConfigurationVO.getSshServer().trim().length() > 0) {
            tunnelConfigurationBuilder.sshHost(serverConfigurationVO.getSshServer())
                    .sshPort(serverConfigurationVO.getSshServerPort());
        }
        tunnelConfigurationBuilder.localhost(serverConfigurationVO.getZkHost())
                .localPort(serverConfigurationVO.getZkPort());
        tunnelConfigurationBuilder.sshUsername(serverConfigurationVO.getSshUsername())
                .sshPassword(serverConfigurationVO.getSshPassword());

        var serverConfiguration = ServerConfiguration.builder()
                .alias(serverConfigurationVO.getZkAlias())
                .url(serverConfigurationVO.getZkUrl())
                .host(serverConfigurationVO.getZkHost())
                .port(serverConfigurationVO.getZkPort())
                .aclList(new ArrayList<>(serverConfigurationVO.getAclList()))
                .sshTunnelEnabled(serverConfigurationVO.isSshEnabled())
                .sshTunnel(tunnelConfigurationBuilder.build())
                .build();
        configurationDomainService.save(serverConfiguration);
    }

    public Double getMainSplitPaneDividerPosition() {
        return configurationDomainService.get()
                .orElseThrow()
                .getUiConfiguration()
                .getMainSplitPaneDividerPosition();
    }

    public Double getNodeViewSplitPaneDividerPosition() {
        return configurationDomainService.get()
                .orElseThrow()
                .getUiConfiguration()
                .getNodeViewSplitPaneDividerPosition();
    }

    public void changeMainSplitPaneDividerPosition(Double value) {
        configurationDomainService.saveMainSplitPaneDividerPosition(value);
    }

    public void changeNodeViewSplitPaneDividerPosition(Double value) {
        configurationDomainService.saveNodeViewSplitPaneDividerPosition(value);
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

    public Locale getLocale() {
        return configurationDomainService.getLocale();
    }

    public void updateLocale(ConfigData.Lang lang) {
        configurationDomainService.save(new Configuration.LocaleConfiguration(lang.getLocale()));
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

    public void resetConfiguration() {
        changeFontSize(14);
        changeMainSplitPaneDividerPosition(0.25);
        changeNodeViewSplitPaneDividerPosition(0.3);
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

    public void startLogTailer(Consumer<String> lineConsumer, Consumer<Exception> exceptionConsumer) {
        var userHome = System.getProperty("user.home");
        var path = Paths.get(userHome + "/.prettyZoo/log/prettyZoo.log");
        var tailerThread = new Thread(() -> {
            new Tailer(path.toFile(), new TailerListener() {
                @Override
                public void init(Tailer tailer) {
                    log.info("init log dashboard");
                }

                @Override
                public void fileNotFound() {
                    log.info("can't find log file in " + path);
                }

                @Override
                public void fileRotated() {

                }

                @Override
                public void handle(String line) {
                    if (line != null) {
                        lineConsumer.accept(line);
                    }
                }

                @Override
                public void handle(Exception ex) {
                    exceptionConsumer.accept(ex);
                }
            }, 1000, true).run();
        });
        LogTailerThreadContext.set(tailerThread);
        tailerThread.start();
    }
}
