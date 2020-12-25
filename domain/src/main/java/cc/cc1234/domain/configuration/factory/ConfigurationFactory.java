package cc.cc1234.domain.configuration.factory;

import cc.cc1234.config.JsonPrettyZooConfigRepository;
import cc.cc1234.domain.configuration.entity.Configuration;
import cc.cc1234.domain.configuration.entity.ServerConfiguration;
import cc.cc1234.domain.configuration.value.SSHTunnelConfiguration;
import cc.cc1234.spi.config.PrettyZooConfigRepository;
import cc.cc1234.spi.config.model.ConfigData;
import cc.cc1234.spi.listener.ConfigurationChangeListener;

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
                            .host(serverConfig.getHost())
                            .aclList(serverConfig.getAclList())
                            .sshTunnelEnabled(serverConfig.getSshTunnelEnabled())
                            .sshTunnel(tunnelConfiguration)
                            .build();
                })
                .collect(Collectors.toList());
        return Configuration.builder()
                .configurationChangeListeners(listeners)
                .serverConfigurations(serverConfigurations)
                .build();
    }
}
