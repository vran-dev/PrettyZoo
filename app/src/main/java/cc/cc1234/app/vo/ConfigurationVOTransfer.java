package cc.cc1234.app.vo;

import cc.cc1234.core.configuration.entity.Configuration;
import cc.cc1234.core.configuration.entity.ServerConfiguration;
import cc.cc1234.specification.config.model.ConfigData;
import cc.cc1234.specification.config.model.ServerConfigData;

import java.util.List;
import java.util.stream.Collectors;

public class ConfigurationVOTransfer {

    public static ConfigurationVO to(Configuration configuration) {
        final List<ServerConfigurationVO> serverConfigs = configuration.getServerConfigurations()
                .stream()
                .map(ConfigurationVOTransfer::to)
                .collect(Collectors.toList());
        final ConfigurationVO vo = new ConfigurationVO();
        vo.getServers().addAll(serverConfigs);
        return vo;
    }

    public static ServerConfigurationVO to(ServerConfiguration serverConfiguration) {
        final ServerConfigurationVO vo = new ServerConfigurationVO();
        vo.setZkUrl(serverConfiguration.getUrl());
        vo.setZkHost(serverConfiguration.getHost());
        vo.setZkPort(serverConfiguration.getPort());
        vo.getAclList().addAll(serverConfiguration.getAclList());
        if (serverConfiguration.getSshTunnel() != null) {
            var sshTunnelConfig = serverConfiguration.getSshTunnel();
            if (sshTunnelConfig.getRemoteHost() == null) {
                vo.setRemoteServer("");
            } else {
                vo.setRemoteServer(sshTunnelConfig.getRemoteHost());
                vo.setRemoteServerPort(sshTunnelConfig.getRemotePort());
            }
            if (sshTunnelConfig.getSshHost() == null) {
                vo.setSshServer("");
            } else {
                vo.setSshServer(sshTunnelConfig.getSshHost() + ":" + sshTunnelConfig.getSshPort());
            }
            vo.setSshUsername(sshTunnelConfig.getSshUsername());
            vo.setSshPassword(sshTunnelConfig.getSshPassword());
        }
        vo.setSshEnabled(serverConfiguration.getSshTunnelEnabled());
        return vo;
    }

    public static ConfigurationVO to(ConfigData config) {
        final List<ServerConfigurationVO> serverConfigs = config.getServers()
                .stream()
                .map(ConfigurationVOTransfer::to)
                .collect(Collectors.toList());

        final ConfigurationVO vo = new ConfigurationVO();
        vo.getServers().addAll(serverConfigs);
        return vo;
    }

    public static ServerConfigurationVO to(ServerConfigData serverConfig) {
        final ServerConfigurationVO vo = new ServerConfigurationVO();
        vo.setZkUrl(serverConfig.getUrl());
        vo.setZkHost(serverConfig.getHost());
        vo.setZkPort(serverConfig.getPort().get());
        vo.getAclList().addAll(serverConfig.getAclList());
        vo.setZkAlias(serverConfig.getAlias());
        serverConfig.getSshTunnelConfig().ifPresent(sshTunnelConfig -> {
            if (sshTunnelConfig.getRemoteHost() == null || sshTunnelConfig.getRemotePort() == null) {
                vo.setRemoteServer("");
            } else {
                vo.setRemoteServer(sshTunnelConfig.getRemoteHost());
                vo.setRemoteServerPort(sshTunnelConfig.getRemotePort());
            }
            vo.setSshUsername(sshTunnelConfig.getSshUsername());
            vo.setSshPassword(sshTunnelConfig.getPassword());
            if (sshTunnelConfig.getRemoteHost() == null || sshTunnelConfig.getRemotePort() == null) {
                vo.setRemoteServer("");
            } else {
                vo.setSshServer(sshTunnelConfig.getSshHost() + ":" + sshTunnelConfig.getSshPort());
            }
        });
        vo.setSshEnabled(serverConfig.getSshTunnelEnabled());
        return vo;
    }

}
