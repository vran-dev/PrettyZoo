package cc.cc1234.app.listener;

import cc.cc1234.app.cache.TreeItemCache;
import cc.cc1234.app.context.ActiveServerContext;
import cc.cc1234.app.context.LocaleContext;
import cc.cc1234.app.vo.ConfigurationVO;
import cc.cc1234.app.vo.ConfigurationVOTransfer;
import cc.cc1234.app.vo.ServerConfigurationVO;
import cc.cc1234.app.vo.ServerConnectionAdvanceConfigurationVO;
import cc.cc1234.specification.config.model.ServerConfigData;
import cc.cc1234.specification.config.model.ServerConnectionAdvanceConfigData;
import cc.cc1234.specification.listener.ConfigurationChangeListener;
import javafx.collections.FXCollections;

import java.util.List;
import java.util.Locale;
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
        if (removeServer.getUrl().equals(ActiveServerContext.get())) {
            ActiveServerContext.invalidate();
        }
        TreeItemCache.getInstance().remove(removeServer.getUrl());
        configurationVO.getServers().removeIf(vo -> vo.getZkUrl().equals(removeServer.getUrl()));
    }

    @Override
    public void onServerChange(ServerConfigData newValue) {
        final ServerConfigurationVO vo = ConfigurationVOTransfer.to(newValue);
        configurationVO.getServers()
                .stream()
                .filter(s -> Objects.equals(s.getZkUrl(), newValue.getUrl()))
                .findFirst()
                .map(old -> {
                    old.setAclList(FXCollections.observableList(newValue.getAclList()));
                    old.setZkAlias(newValue.getAlias());
                    old.setEnableConnectionAdvanceConfiguration(newValue.getEnableConnectionAdvanceConfiguration());
                    newValue.getSshTunnelConfig()
                            .map(sshTunnelConfig -> {
                                old.setSshEnabled(newValue.getSshTunnelEnabled());
                                old.setSshServer(sshTunnelConfig.getSshHost());
                                old.setSshServerPort(sshTunnelConfig.getSshPort());
                                old.setRemoteServer(sshTunnelConfig.getRemoteHost());
                                old.setRemoteServerPort(sshTunnelConfig.getRemotePort());
                                old.setSshUsername(sshTunnelConfig.getSshUsername());
                                old.setSshPassword(sshTunnelConfig.getPassword());
                                return true;
                            })
                            .orElseGet(() -> {
                                old.setSshEnabled(newValue.getSshTunnelEnabled());
                                old.setSshServer("");
                                old.setSshServerPort(null);
                                old.setSshUsername("");
                                old.setSshPassword("");
                                old.setRemoteServer("");
                                old.setRemoteServerPort(null);
                                return true;
                            });

                    ServerConnectionAdvanceConfigData newAdvanceConfig = newValue.getConnectionAdvanceConfig();
                    ServerConnectionAdvanceConfigurationVO updated = new ServerConnectionAdvanceConfigurationVO();
                    updated.setConnectionTimeout(newAdvanceConfig.getConnectionTimeout());
                    updated.setSessionTimeout(newAdvanceConfig.getSessionTimeout());
                    updated.setMaxRetries(newAdvanceConfig.getMaxRetries());
                    updated.setRetryIntervalTime(newAdvanceConfig.getRetryIntervalTime());
                    old.setConnectionAdvanceConfiguration(updated);
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

    @Override
    public void onLocaleChange(Locale locale) {
        LocaleContext.set(locale);
    }
}
