package cc.cc1234.core.configuration.factory;

import cc.cc1234.config.JsonPrettyZooConfigRepository;
import cc.cc1234.core.configuration.entity.Configuration;
import cc.cc1234.core.configuration.entity.ConnectionConfiguration;
import cc.cc1234.core.configuration.entity.ServerConfiguration;
import cc.cc1234.core.configuration.value.SSHTunnelConfiguration;
import cc.cc1234.specification.config.PrettyZooConfigRepository;
import cc.cc1234.specification.config.model.ConfigData;
import cc.cc1234.specification.config.model.ConnectionConfigData;
import cc.cc1234.specification.config.model.ServerConfigData;
import cc.cc1234.specification.listener.ConfigurationChangeListener;

import java.util.List;
import java.util.stream.Collectors;

public class ConfigurationFactory {

    private PrettyZooConfigRepository prettyZooConfigRepository = new JsonPrettyZooConfigRepository();

    public Configuration create(List<ConfigurationChangeListener> listeners) {
        ConfigData configData = prettyZooConfigRepository.get();
        final List<ServerConfiguration> serverConfigurations = configData.getServers()
                .stream()
                .map(serverConfig -> {
                    var hostAndPort = serverConfig.getHost().split(":");
                    // compatible: before v1.9.2 host is [xxx:port]
                    var host = serverConfig.getPort().map(p -> serverConfig.getHost())
                            .orElse(hostAndPort[0]);
                    var port = serverConfig.getPort()
                            .orElseGet(() -> Integer.parseInt(hostAndPort[1]));
                    var url = host + ":" + port;
                    SSHTunnelConfiguration tunnelConfiguration = sshTunnelConfiguration(serverConfig);
                    ConnectionConfiguration connectionConfiguration = connectionConfiguration(serverConfig);
                    return ServerConfiguration.builder()
                            .alias(serverConfig.getAlias())
                            .url(url)
                            .host(host)
                            .port(port)
                            .aclList(serverConfig.getAclList())
                            .connectTimes(serverConfig.getConnectTimes())
                            .sshTunnelEnabled(serverConfig.getSshTunnelEnabled())
                            .sshTunnel(tunnelConfiguration)
                            .enableConnectionAdvanceConfiguration(
                                    serverConfig.getEnableConnectionAdvanceConfiguration())
                            .connectionConfiguration(connectionConfiguration)
                            .build();
                })
                .collect(Collectors.toList());
        var fontConfiguration = getOrDefaultFontConfiguration(configData.getFontConfig());
        var locale = configData.getLocalConfig().getLang().getLocale();
        var localeConfiguration = new Configuration.LocaleConfiguration(locale);
        var uiConfig = configData.getUiConfig();
        var uiConfiguration = new Configuration.UiConfiguration(uiConfig.getMainSplitPaneDividerPosition(),
                uiConfig.getNodeViewSplitPaneDividerPosition());
        return Configuration.builder()
                .fontConfiguration(fontConfiguration)
                .localeConfiguration(localeConfiguration)
                .configurationChangeListeners(listeners)
                .serverConfigurations(serverConfigurations)
                .uiConfiguration(uiConfiguration)
                .theme(configData.getTheme())
                .build();
    }

    private static SSHTunnelConfiguration sshTunnelConfiguration(ServerConfigData serverConfig) {
        return serverConfig.getSshTunnelConfig()
                .map(tunnelConfig -> SSHTunnelConfiguration.builder()
                        .localhost(tunnelConfig.getLocalhost())
                        .localPort(tunnelConfig.getLocalPort())
                        .sshUsername(tunnelConfig.getSshUsername())
                        .sshPassword(tunnelConfig.getPassword())
                        .sshKeyFilePath(tunnelConfig.getSshKeyFilePath())
                        .sshHost(tunnelConfig.getSshHost())
                        .sshPort(tunnelConfig.getSshPort())
                        .remoteHost(tunnelConfig.getRemoteHost())
                        .remotePort(tunnelConfig.getRemotePort())
                        .build())
                .orElse(null);
    }

    private ConnectionConfiguration connectionConfiguration(ServerConfigData serverConfig) {
        var connectionConfiguration = new ConnectionConfiguration();
        ConnectionConfigData originAdvance = serverConfig.getConnectionConfig();
        connectionConfiguration.setRetryIntervalTime(originAdvance.getRetryIntervalTime());
        connectionConfiguration.setMaxRetries(originAdvance.getMaxRetries());
        connectionConfiguration.setSessionTimeout(originAdvance.getSessionTimeout());
        connectionConfiguration.setConnectionTimeout(originAdvance.getConnectionTimeout());
        return connectionConfiguration;
    }

    private Configuration.FontConfiguration getOrDefaultFontConfiguration(ConfigData.FontConfigData data) {
        if (data == null) {
            return new Configuration.FontConfiguration(14);
        } else {
            return new Configuration.FontConfiguration(data.getFontSize());
        }
    }
}
