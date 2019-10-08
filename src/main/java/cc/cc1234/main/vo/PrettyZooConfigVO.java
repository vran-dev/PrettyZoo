package cc.cc1234.main.vo;

import cc.cc1234.main.cache.ActiveServerContext;
import cc.cc1234.main.context.ApplicationContext;
import cc.cc1234.main.model.PrettyZooConfig;
import cc.cc1234.main.model.ZkServerConfig;
import cc.cc1234.main.service.PrettyZooConfigService;
import cc.cc1234.main.service.ZkServerService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class PrettyZooConfigVO {

    private ObservableList<ZkServerConfigVO> servers = FXCollections.observableArrayList();

    private PrettyZooConfigService prettyZooConfigService = ApplicationContext.get().getBean(PrettyZooConfigService.class);

    public PrettyZooConfigVO() {
        init();
    }

    private void init() {
        servers.addAll(toVO(prettyZooConfigService.load().getServers()));
        prettyZooConfigService.registerListener(newConfig -> {
            this.servers.clear();
            this.servers.addAll(toVO(newConfig.getServers()));
        });
    }

    private List<ZkServerConfigVO> toVO(List<ZkServerConfig> servers) {
        return servers.stream().map(ZkServerConfigVO::new).collect(Collectors.toList());
    }

    public void remove(String host) {
        final List<ZkServerConfigVO> removeServers = servers.stream()
                .filter(z -> z.getHost().equals(host))
                .collect(Collectors.toList());
        servers.removeAll(removeServers);
        prettyZooConfigService.save(toModel());
        ActiveServerContext.invalidate();
        ZkServerService.getOrCreate(host).closeALl();
    }

    private PrettyZooConfig toModel() {
        final PrettyZooConfig config = new PrettyZooConfig();
        final List<ZkServerConfig> zkServerConfigs = getServers()
                .stream()
                .map(zk -> {
                    final ZkServerConfig serverConfig = new ZkServerConfig();
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

}
