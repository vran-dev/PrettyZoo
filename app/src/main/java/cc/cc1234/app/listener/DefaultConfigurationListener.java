package cc.cc1234.app.listener;

import cc.cc1234.app.cache.TreeItemCache;
import cc.cc1234.app.context.ActiveServerContext;
import cc.cc1234.app.vo.ConfigurationVO;
import cc.cc1234.app.vo.ConfigurationVOTransfer;
import cc.cc1234.app.vo.ServerConfigurationVO;
import cc.cc1234.spi.config.model.ServerConfigData;
import cc.cc1234.spi.listener.ConfigurationChangeListener;
import javafx.collections.FXCollections;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class DefaultConfigurationListener implements ConfigurationChangeListener {

    private ConfigurationVO configurationVO;

    public DefaultConfigurationListener(ConfigurationVO configurationVO) {
        this.configurationVO = configurationVO;
    }

    @Override
    public void onServerAdd(ServerConfigData serverConfig) {
        configurationVO.getServers().add(ConfigurationVOTransfer.to(serverConfig));
    }

    @Override
    public void onServerRemove(ServerConfigData removeServer) {
        if (removeServer.getHost().equals(ActiveServerContext.get())) {
            ActiveServerContext.invalidate();
        }
        TreeItemCache.getInstance().remove(removeServer.getHost());
        final List<ServerConfigurationVO> removeServers = configurationVO.getServers()
                .stream()
                .filter(z -> z.getZkServer().equals(removeServer.getHost()))
                .collect(Collectors.toList());
        configurationVO.getServers().removeAll(removeServers);
    }

    @Override
    public void onServerChange(ServerConfigData newValue) {
        final ServerConfigurationVO vo = ConfigurationVOTransfer.to(newValue);
        configurationVO.getServers()
                .stream()
                .filter(s -> Objects.equals(s.getZkServer(), newValue.getHost()))
                .findFirst()
                .map(old -> {
                    old.setAclList(FXCollections.observableList(newValue.getAclList()));
                    old.setZkAlias(newValue.getAlias());
                    newValue.getSshTunnelConfig()
                            .map(sshTunnelConfig -> {
                                old.setSshEnabled(newValue.getSshTunnelEnabled());
                                if (sshTunnelConfig.getSshHost() == null) {
                                    old.setSshServer("");
                                } else {
                                    old.setSshServer(String.format("%s:%d", sshTunnelConfig.getSshHost(), sshTunnelConfig.getSshPort()));
                                }
                                if (sshTunnelConfig.getRemoteHost() == null) {
                                    old.setRemoteServer("");
                                } else {
                                    old.setRemoteServer(String.format("%s:%d", sshTunnelConfig.getRemoteHost(), sshTunnelConfig.getRemotePort()));
                                }
                                old.setSshUsername(sshTunnelConfig.getSshUsername());
                                old.setSshPassword(sshTunnelConfig.getPassword());
                                return true;
                            })
                            .orElseGet(() -> {
                                old.setSshEnabled(newValue.getSshTunnelEnabled());
                                old.setSshServer("");
                                old.setSshUsername("");
                                old.setSshPassword("");
                                old.setRemoteServer("");
                                return true;
                            });

                    return true;
                })
                .orElseGet(() -> configurationVO.getServers().add(vo));
    }

    @Override
    public void onReload(List<ServerConfigData> configs) {
        final List<ServerConfigurationVO> configurations = configs.stream()
                .map(ConfigurationVOTransfer::to)
                .collect(Collectors.toList());
        configurationVO.getServers().clear();
        configurationVO.getServers().addAll(configurations);
    }
}
