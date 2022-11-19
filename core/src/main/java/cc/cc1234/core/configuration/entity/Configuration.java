package cc.cc1234.core.configuration.entity;

import cc.cc1234.core.configuration.value.SSHTunnelConfiguration;
import cc.cc1234.specification.config.model.ConfigData;
import cc.cc1234.specification.config.model.ConnectionConfigData;
import cc.cc1234.specification.config.model.SSHTunnelConfigData;
import cc.cc1234.specification.config.model.ServerConfigData;
import cc.cc1234.specification.listener.ConfigurationChangeListener;
import lombok.*;

import java.util.*;
import java.util.stream.Collectors;

@Builder
@Getter
public class Configuration {

    @Singular
    private List<ServerConfiguration> serverConfigurations;

    @Singular
    private List<ConfigurationChangeListener> configurationChangeListeners;

    private FontConfiguration fontConfiguration;

    private LocaleConfiguration localeConfiguration;

    private UiConfiguration uiConfiguration;

    private String theme;

    public Configuration(List<ServerConfiguration> serverConfigs,
                         List<ConfigurationChangeListener> listeners,
                         FontConfiguration fontConfiguration,
                         LocaleConfiguration localeConfiguration,
                         UiConfiguration uiConfiguration,
                         String theme) {
        Objects.requireNonNull(listeners);
        Objects.requireNonNull(serverConfigs);
        Objects.requireNonNull(uiConfiguration);
        this.serverConfigurations = serverConfigs;
        this.configurationChangeListeners = listeners;
        this.fontConfiguration = fontConfiguration;
        this.localeConfiguration = localeConfiguration;
        this.uiConfiguration = uiConfiguration;
        this.theme = theme;

        final List<ServerConfigData> servers = this.serverConfigurations.stream()
                .map(this::toServerConfig)
                .collect(Collectors.toList());
        configurationChangeListeners.forEach(listener -> listener.onReload(servers));
    }

    public void add(ServerConfiguration serverConfiguration) {
        serverConfigurationPrecondition(serverConfiguration);
        serverConfigurations.stream()
                .filter(s -> s.getUrl().equals(serverConfiguration.getUrl()))
                .findFirst()
                .ifPresent(s -> {
                    throw new IllegalStateException(serverConfiguration.getUrl() + " exists");
                });
        var copiedServers = new ArrayList<>(this.serverConfigurations);
        copiedServers.add(serverConfiguration);
        this.serverConfigurations = copiedServers;
        configurationChangeListeners.forEach(listener -> listener.onServerAdd(toServerConfig(serverConfiguration)));
    }

    public void update(ServerConfiguration serverConfiguration) {
        serverConfigurationPrecondition(serverConfiguration);
        ServerConfiguration server = serverConfigurations.stream()
                .filter(s -> s.getUrl().equals(serverConfiguration.getUrl()))
                .findFirst()
                .orElseThrow();
        server.update(serverConfiguration);
        configurationChangeListeners.forEach(listener -> listener.onServerChange(toServerConfig(serverConfiguration)));
    }

    public void updateFont(FontConfiguration fontConfiguration) {
        this.fontConfiguration = fontConfiguration;
    }

    public void updateLocale(LocaleConfiguration localeConfiguration) {
        Objects.requireNonNull(localeConfiguration.getLocale());
        this.localeConfiguration = localeConfiguration;
        configurationChangeListeners.forEach(listener ->
                listener.onLocaleChange(this.localeConfiguration.getLocale()));
    }

    public Optional<ServerConfiguration> get(String url) {
        return serverConfigurations.stream().filter(s -> s.getUrl().equals(url)).findFirst();
    }

    public Boolean exists(String host) {
        return get(host).isPresent();
    }

    public void delete(String url) {
        final ServerConfiguration existsServer = get(url).orElseThrow();
        List<ServerConfiguration> configurations = serverConfigurations.stream()
                .filter(s -> !s.getUrl().equals(url))
                .collect(Collectors.toList());
        this.serverConfigurations = configurations;
        configurationChangeListeners.forEach(listener -> listener.onServerRemove(toServerConfig(existsServer)));
    }

    public void incrementConnectTimes(String server) {
        serverConfigurations.stream()
                .filter(config -> config.getUrl().equals(server))
                .forEach(ServerConfiguration::incrementConnectTimes);
    }

    public void changeTheme(String theme) {
        this.theme = theme;
    }

    private void serverConfigurationPrecondition(ServerConfiguration serverConfig) {
        Objects.requireNonNull(serverConfig);
        Objects.requireNonNull(serverConfig.getUrl());
        Objects.requireNonNull(serverConfig.getHost());
        Objects.requireNonNull(serverConfig.getPort());
        Objects.requireNonNull(serverConfig.getSshTunnelEnabled());
        if (serverConfig.getSshTunnelEnabled() && serverConfig.getSshTunnel() == null) {
            throw new IllegalStateException("add SSHTunnel before save");
        }
        final String alias = serverConfig.getAlias();
        if (alias != null && !alias.isEmpty() && alias.isBlank()) {
            throw new IllegalStateException("Alias must not be all blank");
        }
    }

    /**
     * FIXME 实体不应该和数据模型强耦合
     */
    public ConfigData toPersistModel() {
        var configData = new ConfigData();
        var servers = getServerConfigurations().stream()
                .map(this::toServerConfig)
                .collect(Collectors.toList());
        var fontConfig = new ConfigData.FontConfigData(this.getFontConfiguration().getSize());
        var langConfig = new ConfigData.LocalConfigData(ConfigData.Lang.valueOf((localeConfiguration.getLocale())));
        var uiConfig = new ConfigData.UiConfig(
                uiConfiguration.getMainSplitPaneDividerPosition(),
                uiConfiguration.getNodeViewSplitPaneDividerPosition()
        );
        configData.setTheme(this.theme);
        configData.setServers(servers);
        configData.setFontConfig(fontConfig);
        configData.setLocalConfig(langConfig);
        configData.setUiConfig(uiConfig);
        return configData;
    }

    private ServerConfigData toServerConfig(ServerConfiguration config) {
        SSHTunnelConfigData sshTunnelData = null;
        if (config.getSshTunnel() != null) {
            SSHTunnelConfiguration tunnelConfiguration = config.getSshTunnel();
            sshTunnelData = new SSHTunnelConfigData();
            sshTunnelData.setLocalhost(tunnelConfiguration.getLocalhost());
            sshTunnelData.setLocalPort(tunnelConfiguration.getLocalPort());
            sshTunnelData.setSshHost(tunnelConfiguration.getSshHost());
            sshTunnelData.setSshPort(tunnelConfiguration.getSshPort());
            sshTunnelData.setSshUsername(tunnelConfiguration.getSshUsername());
            sshTunnelData.setPassword(tunnelConfiguration.getSshPassword());
            sshTunnelData.setRemoteHost(tunnelConfiguration.getRemoteHost());
            sshTunnelData.setRemotePort(tunnelConfiguration.getRemotePort());
        }

        final ServerConfigData serverData = new ServerConfigData();
        serverData.setConnectTimes(config.getConnectTimes());
        serverData.setAclList(new ArrayList<>(config.getAclList()));
        serverData.setUrl(config.getUrl());
        serverData.setHost(config.getHost());
        serverData.setPort(Optional.of(config.getPort()));
        serverData.setSshTunnelEnabled(config.getSshTunnelEnabled());
        serverData.setSshTunnelConfig(Optional.ofNullable(sshTunnelData));
        serverData.setAlias(config.getAlias());
        serverData.setEnableConnectionAdvanceConfiguration(config.getEnableConnectionAdvanceConfiguration());

        ConnectionConfigData advanceConfig = serverData.getConnectionConfig();
        ConnectionConfiguration inputAdvanceConfig = config.getConnectionConfiguration();
        advanceConfig.setConnectionTimeout(inputAdvanceConfig.getConnectionTimeout());
        advanceConfig.setSessionTimeout(inputAdvanceConfig.getSessionTimeout());
        advanceConfig.setMaxRetries(inputAdvanceConfig.getMaxRetries());
        advanceConfig.setRetryIntervalTime(inputAdvanceConfig.getRetryIntervalTime());
        return serverData;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FontConfiguration {

        private Integer size;

        public void checkIsValid() {
            if (size == null) {
                throw new IllegalArgumentException("font size is invalid");
            }
            if (size < 8 || size > 50) {
                throw new IllegalArgumentException("font size is invalid");
            }
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LocaleConfiguration {

        private Locale locale;

    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class UiConfiguration {

        private double mainSplitPaneDividerPosition;

        private double nodeViewSplitPaneDividerPosition;

    }
}
