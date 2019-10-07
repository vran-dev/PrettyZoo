package cc.cc1234.main.vo;

import cc.cc1234.main.cache.PrettyZooConfigContext;
import cc.cc1234.main.model.PrettyZooConfig;
import cc.cc1234.main.model.ZkServerConfig;
import cc.cc1234.main.util.JsonUtils;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class PrettyZooConfigVO {

    private static final String PRETTYZOO_CONFIG = System.getProperty("user.home") + "/.prettyZoo/server-input.history";


    private ObservableList<ZkServerConfigVO> servers = FXCollections.observableArrayList();

    public PrettyZooConfigVO() {
        init();
        PrettyZooConfigContext.set(this);
    }

    private void init() {
        final PrettyZooConfig config = JsonUtils.from(PRETTYZOO_CONFIG, PrettyZooConfig.class);
        final List<ZkServerConfigVO> configs = config.getServers()
                .stream()
                .map(ZkServerConfigVO::new)
                .sorted(Comparator.comparingInt(ZkServerConfigVO::getConnectTimes))
                .collect(Collectors.toList());
        // sort by connect times desc
        Collections.reverse(configs);
        servers.addAll(configs);
    }


    public boolean save(ZkServerConfigVO serverConfig) {
        final Optional<ZkServerConfigVO> configOpt = servers.stream()
                .filter(z -> z.getHost().equals(serverConfig.getHost()))
                .findFirst();
        return configOpt.map(c -> false)
                .orElseGet(() -> {
                    servers.add(serverConfig);
                    flush();
                    return true;
                });
    }

    public boolean contains(String host) {
        return servers.stream().anyMatch(z -> z.getHost().equals(host));
    }

    public void remove(String host) {
        final List<ZkServerConfigVO> removeServers = servers.stream()
                .filter(z -> z.getHost().equals(host))
                .collect(Collectors.toList());
        servers.removeAll(removeServers);
        flush();
    }

    public void flush() {
        final PrettyZooConfig config = toModel();
        final String json = JsonUtils.to(config);
        try {
            Files.write(Paths.get(PRETTYZOO_CONFIG), json.getBytes());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
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
