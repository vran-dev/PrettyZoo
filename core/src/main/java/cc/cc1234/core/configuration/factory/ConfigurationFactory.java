package cc.cc1234.core.configuration.factory;

import cc.cc1234.config.JsonPrettyZooConfigRepository;
import cc.cc1234.core.configuration.entity.Configuration;
import cc.cc1234.core.configuration.entity.ServerConfiguration;
import cc.cc1234.core.configuration.value.SSHTunnelConfiguration;
import cc.cc1234.specification.config.PrettyZooConfigRepository;
import cc.cc1234.specification.config.model.ConfigData;
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
                    SSHTunnelConfiguration tunnelConfiguration = serverConfig.getSshTunnelConfig()
                            .map(tunnelConfig -> SSHTunnelConfiguration.builder()
                                    .localhost(tunnelConfig.getLocalhost())
                                    .localPort(tunnelConfig.getLocalPort())
                                    .sshUsername(tunnelConfig.getSshUsername())
                                    .sshPassword(tunnelConfig.getPassword())
                                    .sshHost(tunnelConfig.getSshHost())
                                    .sshPort(tunnelConfig.getSshPort())
                                    .remoteHost(tunnelConfig.getRemoteHost())
                                    .remotePort(tunnelConfig.getRemotePort())
                                    .build())
                            .orElse(null);
                    return ServerConfiguration.builder()
                            .alias(serverConfig.getAlias())
                            .host(serverConfig.getHost())
                            .aclList(serverConfig.getAclList())
                            .connectTimes(serverConfig.getConnectTimes())
                            .sshTunnelEnabled(serverConfig.getSshTunnelEnabled())
                            .sshTunnel(tunnelConfiguration)
                            .build();
                })
                .collect(Collectors.toList());
        var fontConfiguration = getOrDefaultFontConfiguration(configData.getFontConfig());
        return Configuration.builder()
                .fontConfiguration(fontConfiguration)
                .configurationChangeListeners(listeners)
                .serverConfigurations(serverConfigurations)
                .build();
    }

    private Configuration.FontConfiguration getOrDefaultFontConfiguration(ConfigData.FontConfigData data) {
        if (data == null) {
            return new Configuration.FontConfiguration(14);
        } else {
            return new Configuration.FontConfiguration(data.getFontSize());
        }
    }
}
