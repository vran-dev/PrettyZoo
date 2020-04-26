package cc.cc1234.app.vo;

import cc.cc1234.app.cache.TreeItemCache;
import cc.cc1234.app.context.ActiveServerContext;
import cc.cc1234.facade.PrettyZooFacade;
import cc.cc1234.spi.config.model.RootConfig;
import cc.cc1234.spi.config.model.ServerConfig;
import cc.cc1234.spi.listener.PrettyZooConfigChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class PrettyZooConfigVO {

    private ObservableList<ZkServerConfigVO> servers = FXCollections.observableArrayList();

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
                final List<ZkServerConfigVO> removeServers = servers.stream()
                        .filter(z -> z.getHost().equals(removeServer.getHost()))
                        .collect(Collectors.toList());
                servers.removeAll(removeServers);
            }
        });
    }

    private List<ZkServerConfigVO> toVO(List<ServerConfig> servers) {
        return servers.stream().map(ZkServerConfigVO::new).collect(Collectors.toList());
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

    private RootConfig toModel() {
        final RootConfig config = new RootConfig();
        final List<ServerConfig> zkServerConfigs = getServers()
                .stream()
                .map(zk -> {
                    final ServerConfig serverConfig = new ServerConfig();
                    serverConfig.setConnectTimes(zk.getConnectTimes());
                    serverConfig.setAclList(new ArrayList<>(zk.getAclList()));
                    serverConfig.setHost(zk.getHost());
                    return serverConfig;
                })
                .collect(Collectors.toList());
        config.setServers(zkServerConfigs);
        return config;
    }

    public ObservableList<ZkServerConfigVO> getServers() {
        return servers;
    }

    public void importConfig(File configFile) {
        if (configFile == null) {
            return;
        }
        if (!configFile.isFile()) {
            throw new IllegalStateException("config must be a file");
        }
        prettyZooFacade.importConfig(configFile);
        servers.clear();
        servers.addAll(toVO(prettyZooFacade.loadConfig().getServers()));
    }
}
