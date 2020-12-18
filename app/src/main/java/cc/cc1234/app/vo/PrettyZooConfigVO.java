package cc.cc1234.app.vo;

import cc.cc1234.app.cache.TreeItemCache;
import cc.cc1234.app.context.ActiveServerContext;
import cc.cc1234.app.facade.PrettyZooFacade;
import cc.cc1234.app.util.VToast;
import cc.cc1234.spi.config.model.ServerConfig;
import cc.cc1234.spi.listener.PrettyZooConfigChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class PrettyZooConfigVO {

    private ObservableList<ServerConfigVO> servers = FXCollections.observableArrayList();

    private PrettyZooFacade prettyZooFacade = new PrettyZooFacade();

    public PrettyZooConfigVO() {
        init();
    }

    private void init() {
        servers.addAll(toVO(prettyZooFacade.loadConfig().getServers()));

        prettyZooFacade.registerConfigChangeListener(new PrettyZooConfigChangeListener() {
            @Override
            public void onServerAdd(ServerConfig serverConfig) {
                servers.addAll(toVO(Collections.singletonList(serverConfig)));
            }

            @Override
            public void onServerRemove(ServerConfig removeServer) {
                if (removeServer.getHost().equals(ActiveServerContext.get())) {
                    ActiveServerContext.invalidate();
                }
                TreeItemCache.getInstance().remove(removeServer.getHost());
                final List<ServerConfigVO> removeServers = servers.stream()
                        .filter(z -> z.getZkServer().equals(removeServer.getHost()))
                        .collect(Collectors.toList());
                servers.removeAll(removeServers);
            }

            @Override
            public void onServerChange(ServerConfig oldValue, ServerConfig newValue) {
                final ServerConfigVO vo = toVO(List.of(newValue)).iterator().next();
                servers.stream()
                        .filter(s -> Objects.equals(s.getZkServer(), oldValue.getHost()))
                        .findFirst()
                        .map(old -> {
                            old.setAclList(FXCollections.observableList(newValue.getAclList()));
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
                        .orElseGet(() -> servers.add(vo));
            }
        });
    }

    private List<ServerConfigVO> toVO(List<ServerConfig> servers) {
        return servers.stream()
                .map(serverConfig -> {
                    final ServerConfigVO vo = new ServerConfigVO();
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
                })
                .collect(Collectors.toList());
    }

    public void remove(String host) {
        prettyZooFacade.removeConfig(host);
    }

    public void export(File file) {
        if (file == null) {
            return;
        }
        prettyZooFacade.exportConfig(file);
    }

    public ObservableList<ServerConfigVO> getServers() {
        return servers;
    }

    public void importConfig(File configFile) {
        if (configFile == null) {
            return;
        }
        if (!configFile.isFile()) {
            VToast.error("config must be a file");
            return;
        }
        prettyZooFacade.importConfig(configFile);
        servers.clear();
        servers.addAll(toVO(prettyZooFacade.loadConfig().getServers()));
    }
}
