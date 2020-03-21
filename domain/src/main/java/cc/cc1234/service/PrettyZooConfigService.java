package cc.cc1234.service;

import cc.cc1234.config.JsonPrettyZooConfigRepository;
import cc.cc1234.config.PrettyZooConfigRepositoryCacheWrapper;
import cc.cc1234.manager.ListenerManager;
import cc.cc1234.spi.config.PrettyZooConfigRepository;
import cc.cc1234.spi.config.model.RootConfig;
import cc.cc1234.spi.config.model.ServerConfig;

import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class PrettyZooConfigService {

    private PrettyZooConfigRepository prettyZooConfigRepository =
            new PrettyZooConfigRepositoryCacheWrapper(new JsonPrettyZooConfigRepository());

    public RootConfig load() {
        return prettyZooConfigRepository.get();
    }

    public void save(RootConfig config) {
        prettyZooConfigRepository.save(config);
    }

    public void add(ServerConfig zkServerConfig) {
        final RootConfig config = load();
        final Set<String> servers = config.getServers().stream().map(ServerConfig::getHost).collect(Collectors.toSet());
        if (servers.contains(zkServerConfig.getHost())) {
            return;
        }
        config.getServers().add(zkServerConfig);
        save(config);
        ListenerManager.instance()
                .getPrettyZooConfigChangeListeners()
                .forEach(listener -> listener.onServerAdd(zkServerConfig));
    }

    public void remove(String server) {
        final RootConfig config = load();
        final List<ServerConfig> removeConfig = config.getServers()
                .stream()
                .filter(serverConfig -> serverConfig.getHost().equals(server))
                .collect(Collectors.toList());
        config.getServers().removeAll(removeConfig);
        save(config);

        removeConfig.forEach(removedConfig ->
                ListenerManager.instance()
                        .getPrettyZooConfigChangeListeners()
                        .forEach(listener -> listener.onServerRemove(removedConfig)));
    }

    public void increaseConnectTimes(String host) {
        final RootConfig config = load();
        config.getServers()
                .stream()
                .filter(s -> s.getHost().equals(host))
                .findFirst()
                .ifPresent(exists -> {
                    exists.setConnectTimes(exists.getConnectTimes() + 1);
                });
    }

    public boolean contains(String host) {
        final RootConfig config = load();
        return config.getServers().stream().anyMatch(s -> s.getHost().equals(host));
    }

    public Optional<ServerConfig> get(String host) {
        return load().getServers().stream().filter(s -> s.getHost().equals(host)).findFirst();
    }
}
