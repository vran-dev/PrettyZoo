package cc.cc1234.app.facade;

import cc.cc1234.app.cache.TreeItemCache;
import cc.cc1234.app.context.ActiveServerContext;
import cc.cc1234.app.context.LocaleContext;
import cc.cc1234.app.context.LogTailerThreadContext;
import cc.cc1234.app.context.PrimaryStageContext;
import cc.cc1234.app.fp.Try;
import cc.cc1234.app.util.Asserts;
import cc.cc1234.app.util.Fills;
import cc.cc1234.app.util.ResourceBundleUtils;
import cc.cc1234.app.view.toast.VToast;
import cc.cc1234.app.vo.ConfigurationVOTransfer;
import cc.cc1234.app.vo.ConnectionConfigurationVO;
import cc.cc1234.app.vo.ServerConfigurationVO;
import cc.cc1234.app.vo.ZkNodeSearchResult;
import cc.cc1234.core.configuration.entity.Configuration;
import cc.cc1234.core.configuration.entity.ConnectionConfiguration;
import cc.cc1234.core.configuration.entity.ServerConfiguration;
import cc.cc1234.core.configuration.service.ConfigurationDomainService;
import cc.cc1234.core.configuration.value.SSHTunnelConfiguration;
import cc.cc1234.core.zookeeper.service.ZookeeperDomainService;
import cc.cc1234.specification.config.PrettyZooConfigRepository;
import cc.cc1234.specification.config.model.ConfigData;
import cc.cc1234.specification.listener.ConfigurationChangeListener;
import cc.cc1234.specification.listener.ServerListener;
import cc.cc1234.specification.listener.ZookeeperNodeListener;
import cc.cc1234.specification.node.NodeMode;
import cc.cc1234.specification.util.StringWriter;
import com.google.common.base.Strings;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apache.commons.io.input.ReversedLinesFileReader;
import org.apache.commons.io.input.Tailer;
import org.apache.commons.io.input.TailerListener;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
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

    public void createNode(String serverId, String path, String data, NodeMode mode) throws Exception {
        zookeeperDomainService.create(serverId, path, data, mode);
    }

    public void deleteNode(String serverId, List<String> pathList) {
        try {
            zookeeperDomainService.delete(serverId, pathList);
        } catch (Exception e) {
            log.error("delete node failed", e);
            throw new IllegalStateException(e);
        }
    }

    public CompletableFuture<Void> connect(String id,
                                           List<ZookeeperNodeListener> nodeListeners,
                                           List<ServerListener> serverListeners) {
        return CompletableFuture.runAsync(() -> {
            var serverConfig = configurationDomainService.getById(id).orElseThrow();
            zookeeperDomainService.connect(serverConfig, nodeListeners, serverListeners);
            configurationDomainService.incrementConnectTimes(id);
        });
    }

    public void disconnect(String id) {
        Platform.runLater(() -> {
            ServerConfiguration serverConfiguration = configurationDomainService.getById(id).orElseThrow();
            zookeeperDomainService.disconnect(serverConfiguration.getId());
            treeItemCache.remove(id);
        });
    }

    public void closeAll() {
        zookeeperDomainService.disconnectAll();
        zookeeperDomainService.closeAllTerminal();
        LogTailerThreadContext.stop();
    }

    public void syncIfNecessary(String serverId) {
        zookeeperDomainService.sync(serverId);
    }

    public List<ZkNodeSearchResult> onSearch(String input) {
        final String serverId = ActiveServerContext.get();
        if (Strings.isNullOrEmpty(input)) {
            return Collections.emptyList();
        }
        final List<ZkNodeSearchResult> res = treeItemCache.search(serverId, input)
                .stream()
                .map(item -> {
                    String path = item.getValue().getPath();
                    var highlights = Fills.fill(path, input,
                            s -> {
                                Text text = new Text(s);
                                text.getStyleClass().add("black-text");
                                return text;
                            },
                            s -> {
                                final Text highlight = new Text(s);
                                highlight.getStyleClass().add("red-text");
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

    public Stat updateData(String serverId,
                           String nodePath,
                           String data,
                           Consumer<Throwable> errorCallback) {
        return Try.of(() -> zookeeperDomainService.set(serverId, nodePath, data))
                .onFailure(errorCallback::accept)
                .get();
    }

    public boolean hasServerConfiguration(String id) {
        if (id == null) {
            return false;
        }
        return configurationDomainService.containServerConfig(id);
    }

    public void saveServerConfiguration(ServerConfigurationVO serverConfigurationVO) {
        SSHTunnelConfiguration tunnelConfig = null;
        if (serverConfigurationVO.isSshEnabled()) {
            tunnelConfig = SSHTunnelConfiguration.builder()
                    .remoteHost(serverConfigurationVO.getRemoteServer())
                    .remotePort(serverConfigurationVO.getRemoteServerPort())
                    .sshHost(serverConfigurationVO.getSshServer())
                    .sshPort(serverConfigurationVO.getSshServerPort())
                    .localhost(serverConfigurationVO.getZkHost())
                    .localPort(serverConfigurationVO.getZkPort())
                    .sshUsername(serverConfigurationVO.getSshUsername())
                    .sshPassword(serverConfigurationVO.getSshPassword())
                    .sshKeyFilePath(serverConfigurationVO.getSshKeyFilePath())
                    .build();
        }

        ConnectionConfiguration advanceConfig = new ConnectionConfiguration();
        if (serverConfigurationVO.isEnableConnectionAdvanceConfiguration()) {
            ConnectionConfigurationVO inputAdvanceConfig =
                    serverConfigurationVO.getConnectionConfiguration();
            advanceConfig.setConnectionTimeout(inputAdvanceConfig.getConnectionTimeout());
            advanceConfig.setSessionTimeout(inputAdvanceConfig.getSessionTimeout());
            advanceConfig.setMaxRetries(inputAdvanceConfig.getMaxRetries());
            advanceConfig.setRetryIntervalTime(inputAdvanceConfig.getRetryIntervalTime());
        }

        boolean idIsBlank = serverConfigurationVO.getId().isBlank();
        var serverConfiguration = ServerConfiguration.builder()
                .id(idIsBlank ? UUID.randomUUID().toString() : serverConfigurationVO.getId())
                .alias(serverConfigurationVO.getZkAlias())
                .host(serverConfigurationVO.getZkHost())
                .port(serverConfigurationVO.getZkPort())
                .aclList(new ArrayList<>(List.of(Objects.toString(serverConfigurationVO.getAcl(), "").split("\n"))))
                .sshTunnelEnabled(serverConfigurationVO.isSshEnabled())
                .sshTunnel(tunnelConfig)
                .enableConnectionAdvanceConfiguration(serverConfigurationVO.isEnableConnectionAdvanceConfiguration())
                .connectionConfiguration(advanceConfig)
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

    public void deleteServerConfigurationById(String id) {
        configurationDomainService.deleteServerConfiguration(id);
    }

    public Locale getLocale() {
        return configurationDomainService.getLocale();
    }

    public void updateLocale(ConfigData.Lang lang) {
        configurationDomainService.save(new Configuration.LocaleConfiguration(lang.getLocale()));
    }

    public List<ServerConfigurationVO> loadServerConfigurations(ConfigurationChangeListener changeListener) {
        final Configuration configuration = configurationDomainService.load(List.of(changeListener));
        if (configuration.getLocaleConfiguration() != null) {
            LocaleContext.set(configuration.getLocaleConfiguration().getLocale());
        }
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

    public ServerConfiguration getServerConfigurationById(String id) {
        final Configuration configuration = configurationDomainService.get().orElseThrow();
        return configuration.getById(id).orElseThrow();
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

    public String getThemeFromConfig() {
        return configurationDomainService.load().getTheme();
    }

    public void changeTheme() {
        Scene scene = PrimaryStageContext.get().getScene();
        ObservableList<String> stylesheets = scene.getStylesheets();
        String currentTheme = configurationDomainService.get()
                .orElseThrow()
                .getTheme();
        String dark = "/assets/css/dark/style.css";
        String light = "/assets/css/default/style.css";
        if (Objects.equals(currentTheme, PrettyZooConfigRepository.THEME_DARK)) {
            stylesheets.remove(dark);
            if (!stylesheets.contains(light)) {
                stylesheets.add(light);
                configurationDomainService.saveTheme(PrettyZooConfigRepository.THEME_DEFAULT);
            }
        } else {
            stylesheets.remove(light);
            if (!stylesheets.contains(dark)) {
                stylesheets.add(dark);
                configurationDomainService.saveTheme(PrettyZooConfigRepository.THEME_DARK);
            }
        }
    }

    public void resetConfiguration() {
        closeAll();
        changeFontSize(14);
        changeMainSplitPaneDividerPosition(0.25);
        changeNodeViewSplitPaneDividerPosition(0.3);
    }

    public void startTerminal(String serverId, StringWriter stream) {
        ServerConfiguration server = configurationDomainService.getById(serverId)
                .orElseThrow();
        String urlToConnect;
        if (server.getSshTunnelEnabled()) {
            urlToConnect = "localhost:" + server.getPort();
        } else {
            urlToConnect = server.getHost() + ":" + server.getPort();
        }
        zookeeperDomainService.initTerminal(serverId, urlToConnect, stream);
    }

    public void executeCommand(String id, String command) {
        try {
            zookeeperDomainService.execute(id, command);
        } catch (Exception e) {
            log.error("execute command failed at  " + id + ":" + command, e);
            VToast.error("命令执行失败，请重试");
        }
    }

    public String executeFourLetterCommand(String serverId, String fourLetter) {
        ServerConfiguration server = configurationDomainService.getById(serverId)
                .orElseThrow();
        String hostToConnect;
        if (server.getSshTunnelEnabled()) {
            hostToConnect = "localhost:" + server.getPort();
        } else {
            hostToConnect = server.getHost() + ":" + server.getPort();
        }
        return zookeeperDomainService.execute4LetterCommand(server.getId(), hostToConnect, fourLetter);
    }

    public void startLogTailer(Consumer<String> lineConsumer, Consumer<Exception> exceptionConsumer) {
        var userHome = System.getProperty("user.home");
        var path = Paths.get(userHome + "/.prettyZoo/log/prettyZoo.log");
        try (ReversedLinesFileReader reader = new ReversedLinesFileReader(path, Charset.defaultCharset())) {
            List<String> lines = reader.readLines(50);
            Collections.reverse(lines);
            lines.forEach(lineConsumer);
        } catch (Exception e) {
            log.error("file read error, msg:{}", e.getMessage(), e);
        }

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

    public void initZookeeperSystemProperties() {
        var properties = loadZookeeperSystemProperties();
        for (var entry : properties.entrySet()) {
            System.setProperty(entry.getKey().toString(), entry.getValue().toString());
        }
    }

    public Properties loadZookeeperSystemProperties() {
        String sysPropPath = PrettyZooConfigRepository.SYS_PROP_PATH;
        if (Files.exists(Paths.get(sysPropPath))) {
            try {
                var properties = new Properties();
                properties.load(new FileInputStream(sysPropPath));
                log.info("load system properties success ->\n {}", properties);
                return properties;
            } catch (IOException e) {
                // ignore error and log it
                log.error("load system properties failed", e);
                return new Properties();
            }
        } else {
            log.info("ignore load system properties, file not exists -> {}", sysPropPath);
            return new Properties();
        }
    }

    public void saveZookeeperSystemProperties(String content) {
        String sysPropPath = PrettyZooConfigRepository.SYS_PROP_PATH;
        Properties properties = new Properties();
        try (StringReader reader = new StringReader(content)) {
            properties.load(reader);
        } catch (IOException e) {
            log.error("save properties failed: " + content, e);
            Platform.runLater(() -> VToast.info(ResourceBundleUtils.getContent("notification.save.failed")));
        }

        try (OutputStream out = Files.newOutputStream(Paths.get(sysPropPath))) {
            properties.store(out, null);
        } catch (IOException e) {
            log.error("save properties file failed", e);
            Platform.runLater(() -> VToast.info(ResourceBundleUtils.getContent("notification.save.failed")));
        }
    }

}
