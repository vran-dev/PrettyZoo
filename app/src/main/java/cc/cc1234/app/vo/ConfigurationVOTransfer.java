package cc.cc1234.app.vo;

import cc.cc1234.spi.config.model.RootConfig;
import cc.cc1234.spi.config.model.ServerConfig;

import java.util.List;
import java.util.stream.Collectors;

public class ConfigurationVOTransfer {

    public static ConfigurationVO to(RootConfig config) {
        final List<ServerConfigurationVO> serverConfigs = config.getServers()
                .stream()
                .map(ConfigurationVOTransfer::to)
                .collect(Collectors.toList());

        final ConfigurationVO vo = new ConfigurationVO();
        vo.getServers().addAll(serverConfigs);
        return vo;
    }

    public static ServerConfigurationVO to(ServerConfig serverConfig) {
        final ServerConfigurationVO vo = new ServerConfigurationVO();
        vo.setZkServer(serverConfig.getHost());
        vo.getAclList().addAll(serverConfig.getAclList());
        vo.setConnected(false);
        serverConfig.getSshTunnelConfig().ifPresent(sshTunnelConfig -> {
            if (sshTunnelConfig.getRemoteHost() == null || sshTunnelConfig.getRemotePort() == null) {
                vo.setRemoteServer("");
            } else {
                vo.setRemoteServer(sshTunnelConfig.getRemoteHost() + ":" + sshTunnelConfig.getRemotePort());
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
