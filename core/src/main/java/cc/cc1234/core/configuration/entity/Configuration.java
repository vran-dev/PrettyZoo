package cc.cc1234.core.configuration.entity;

import cc.cc1234.core.configuration.value.SSHTunnelConfiguration;
import cc.cc1234.specification.config.model.ConfigData;
import cc.cc1234.specification.config.model.SSHTunnelConfigData;
import cc.cc1234.specification.config.model.ServerConfigData;
import cc.cc1234.specification.listener.ConfigurationChangeListener;
import lombok.*;

import java.util.*;
import java.util.stream.Collectors;

@Builder
@Getter
public class Configuration {

    private List<ServerConfiguration> serverConfigurations;

    private List<ConfigurationChangeListener> configurationChangeListeners;

    private FontConfiguration fontConfiguration;

    private LocaleConfiguration localeConfiguration;

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

    public Configuration(List<ServerConfiguration> serverConfigs,
                         List<ConfigurationChangeListener> listeners,
                         FontConfiguration fontConfiguration,
                         LocaleConfiguration localeConfiguration) {
        Objects.requireNonNull(listeners);
        Objects.requireNonNull(serverConfigs);
        this.serverConfigurations = serverConfigs;
        this.configurationChangeListeners = listeners;
        this.fontConfiguration = fontConfiguration;
        this.localeConfiguration = localeConfiguration;

        final List<ServerConfigData> servers = this.serverConfigurations.stream()
                .map(this::toServerConfig)
                .collect(Collectors.toList());
        configurationChangeListeners.forEach(listener -> listener.onReload(servers));
    }

    public void add(ServerConfiguration serverConfiguration) {
        serverConfigurationPrecondition(serverConfiguration);
        serverConfigurations.stream()
                .filter(s -> s.getHost().equals(serverConfiguration.getHost()))
                .findFirst()
                .ifPresent(s -> {
                    throw new IllegalStateException(serverConfiguration.getHost() + " exists");
                });
        serverConfigurations.add(serverConfiguration);
        configurationChangeListeners.forEach(listener -> listener.onServerAdd(toServerConfig(serverConfiguration)));
    }

    public void update(ServerConfiguration serverConfiguration) {
        serverConfigurationPrecondition(serverConfiguration);
        ServerConfiguration server = serverConfigurations.stream()
                .filter(s -> s.getHost().equals(serverConfiguration.getHost()))
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
    }

    public Optional<ServerConfiguration> get(String host) {
        return serverConfigurations.stream().filter(s -> s.getHost().equals(host)).findFirst();
    }

    public Boolean exists(String host) {
        return get(host).isPresent();
    }

    public void delete(String host) {
        final ServerConfiguration existsServer = get(host).orElseThrow();
        List<ServerConfiguration> configurations = serverConfigurations.stream()
                .filter(s -> !s.getHost().equals(host))
                .collect(Collectors.toList());
        this.serverConfigurations = configurations;
        configurationChangeListeners.forEach(listener -> listener.onServerRemove(toServerConfig(existsServer)));
    }

    public void incrementConnectTimes(String server) {
        serverConfigurations.stream()
                .filter(config -> config.getHost().equals(server))
                .forEach(ServerConfiguration::incrementConnectTimes);
    }

    private void serverConfigurationPrecondition(ServerConfiguration serverConfig) {
        Objects.requireNonNull(serverConfig);
        Objects.requireNonNull(serverConfig.getHost());
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
        var langConfig = new ConfigData.LocalConfigData(ConfigData.Lang.valueOf((this.localeConfiguration.getLocale())));
        configData.setServers(servers);
        configData.setFontConfig(fontConfig);
        configData.setLocalConfig(langConfig);
        return configData;
    }

    private ServerConfigData toServerConfig(ServerConfiguration serverConfiguration) {
        SSHTunnelConfigData sshTunnelData = null;
        if (serverConfiguration.getSshTunnel() != null) {
            SSHTunnelConfiguration tunnelConfiguration = serverConfiguration.getSshTunnel();
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
        serverData.setConnectTimes(serverConfiguration.getConnectTimes());
        serverData.setAclList(new ArrayList<>(serverConfiguration.getAclList()));
        serverData.setHost(serverConfiguration.getHost());
        serverData.setSshTunnelEnabled(serverConfiguration.getSshTunnelEnabled());
        serverData.setSshTunnelConfig(Optional.ofNullable(sshTunnelData));
        serverData.setAlias(serverConfiguration.getAlias());
        return serverData;
    }
}
