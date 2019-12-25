package cc.cc1234.service;

import cc.cc1234.cache.PrettyZooConfigCache;
import cc.cc1234.model.PrettyZooConfig;
import cc.cc1234.model.ZkServerConfig;
import cc.cc1234.util.JsonUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class PrettyZooConfigService {

    private static final String PRETTYZOO_CONFIG = System.getProperty("user.home") + "/.prettyZoo/server-input.history";


    private List<Consumer<PrettyZooConfig>> onChangeListener = new ArrayList<>();

    public PrettyZooConfig load() {
        return PrettyZooConfigCache.getOption()
                .orElseGet(() -> {
                    final PrettyZooConfig config = doLoad();
                    // caching
                    PrettyZooConfigCache.set(config);
                    return config;
                });
    }

    public PrettyZooConfig loadForce() {
        final PrettyZooConfig config = doLoad();
        PrettyZooConfigCache.set(config);
        return config;
    }

    private PrettyZooConfig doLoad() {
        final PrettyZooConfig config = JsonUtils.from(PRETTYZOO_CONFIG, PrettyZooConfig.class);
        final List<ZkServerConfig> sortedServers = config.getServers()
                .stream()
                .sorted(Comparator.comparingInt(ZkServerConfig::getConnectTimes))
                .collect(Collectors.toList());
        // sort by connect times desc
        Collections.reverse(sortedServers);
        config.setServers(sortedServers);
        return config;
    }

    public boolean save(PrettyZooConfig config) {
        try {
            final String json = JsonUtils.to(config);
            Files.write(Paths.get(PRETTYZOO_CONFIG), json.getBytes());
            PrettyZooConfigCache.set(config);
            return true;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public PrettyZooConfig add(ZkServerConfig zkServerConfig) {
        final PrettyZooConfig config = load();
        config.getServers().add(zkServerConfig);
        save(config);
        onChange(config);
        return config;
    }

    public void updateConnectTimes(String host, int connectTimes) {
        final PrettyZooConfig config = load();
        config.getServers()
                .stream()
                .filter(s -> s.getHost().equals(host))
                .findFirst()
                .ifPresent(exists -> {
                    exists.setConnectTimes(connectTimes);
                });
    }

    public boolean contains(String host) {
        final PrettyZooConfig config = load();
        return config.getServers().stream().anyMatch(s -> s.getHost().equals(host));
    }

    protected void onChange(PrettyZooConfig config) {
        onChangeListener.forEach(consumer -> consumer.accept(config));
    }

    public boolean registerListener(Consumer<PrettyZooConfig> listener) {
        return onChangeListener.add(listener);
    }

    public boolean removeListener(Consumer<PrettyZooConfig> listener) {
        return onChangeListener.remove(listener);
    }
}
