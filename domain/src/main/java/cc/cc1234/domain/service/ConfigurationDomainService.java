package cc.cc1234.domain.service;

import cc.cc1234.config.JsonPrettyZooConfigRepository;
import cc.cc1234.config.PrettyZooConfigRepositoryCacheWrapper;
import cc.cc1234.spi.config.PrettyZooConfigRepository;
import cc.cc1234.spi.config.model.RootConfig;
import cc.cc1234.spi.config.model.ServerConfig;
import cc.cc1234.spi.listener.ConfigurationChangeListener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;
import java.util.stream.Collectors;

public class ConfigurationDomainService {

    private PrettyZooConfigRepository prettyZooConfigRepository = new PrettyZooConfigRepositoryCacheWrapper(new JsonPrettyZooConfigRepository());

    private static final List<ConfigurationChangeListener> CONFIGURATION_CHANGE_LISTENERS = new ArrayList<>();

    public void addListener(ConfigurationChangeListener listener) {
        CONFIGURATION_CHANGE_LISTENERS.add(listener);
    }

    public RootConfig load() {
        return prettyZooConfigRepository.get();
    }

    public void save(RootConfig config) {
        prettyZooConfigRepository.save(config);
    }

    public void save(ServerConfig serverConfig) {
        final RootConfig rootConfig = load();
        final Optional<ServerConfig> existsServerConfig = rootConfig.getServers()
                .stream()
                .filter(server -> server.getHost().equals(serverConfig.getHost()))
                .findFirst();

        // update
        var servers = rootConfig.getServers()
                .stream()
                .filter(s -> !Objects.equals(s.getHost(), serverConfig.getHost()))
                .collect(Collectors.toList());
        servers.add(serverConfig);
        rootConfig.setServers(servers);
        save(rootConfig);

        // multicast change event
        existsServerConfig
                .map(oldValue -> {
                    CONFIGURATION_CHANGE_LISTENERS.forEach(l -> l.onServerChange(oldValue, serverConfig));
                    return true;
                })
                .orElseGet(() -> {
                    CONFIGURATION_CHANGE_LISTENERS.forEach(l -> l.onServerAdd(serverConfig));
                    return true;
                });
    }

    public void add(ServerConfig zkServerConfig) {
        final RootConfig config = load();
        final Set<String> servers = config.getServers().stream().map(ServerConfig::getHost).collect(Collectors.toSet());
        if (servers.contains(zkServerConfig.getHost())) {
            return;
        }
        config.getServers().add(zkServerConfig);
        save(config);
        CONFIGURATION_CHANGE_LISTENERS.forEach(l -> l.onServerAdd(zkServerConfig));
    }

    public void remove(String server) {
        final RootConfig config = load();
        final List<ServerConfig> removeConfig = config.getServers()
                .stream()
                .filter(serverConfig -> serverConfig.getHost().equals(server))
                .collect(Collectors.toList());
        config.getServers().removeAll(removeConfig);
        save(config);
        removeConfig.forEach(removedConfig -> CONFIGURATION_CHANGE_LISTENERS.forEach(l -> l.onServerRemove(removedConfig)));
    }

    public boolean contains(String host) {
        final RootConfig config = load();
        return config.getServers().stream().anyMatch(s -> s.getHost().equals(host));
    }

    public Optional<ServerConfig> get(String host) {
        return load().getServers().stream().filter(s -> s.getHost().equals(host)).findFirst();
    }

    public void export(File file) {
        RootConfig config = load();
        try (var stream = Files.newOutputStream(file.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
            prettyZooConfigRepository.exportConfig(config, stream);
        } catch (IOException e) {
            throw new IllegalStateException("export config failed", e);
        }

    }

    public void importConfig(File configFile) {
        try (var stream = Files.newInputStream(configFile.toPath(), StandardOpenOption.READ)) {
            prettyZooConfigRepository.importConfig(stream);
            final RootConfig rootConfig = load();
            CONFIGURATION_CHANGE_LISTENERS.forEach(l -> l.onReload(rootConfig.getServers()));
        } catch (IOException e) {
            throw new IllegalStateException("import config failed", e);
        }
    }
}
