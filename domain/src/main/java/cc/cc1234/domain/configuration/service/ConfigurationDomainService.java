package cc.cc1234.domain.configuration.service;

import cc.cc1234.config.JsonPrettyZooConfigRepository;
import cc.cc1234.domain.configuration.entity.Configuration;
import cc.cc1234.domain.configuration.entity.ServerConfiguration;
import cc.cc1234.domain.configuration.factory.ConfigurationFactory;
import cc.cc1234.spi.config.PrettyZooConfigRepository;
import cc.cc1234.spi.listener.ConfigurationChangeListener;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

public class ConfigurationDomainService {

    private static final Cache<Configuration> configurationCache = new Cache<>();

    private PrettyZooConfigRepository prettyZooConfigRepository = new JsonPrettyZooConfigRepository();

    public Configuration load(List<ConfigurationChangeListener> listeners) {
        final Configuration configuration = new ConfigurationFactory().create(listeners);
        configurationCache.setVal(configuration);
        prettyZooConfigRepository.save(configuration.toPersistModel());
        return configuration;
    }

    public void save(ServerConfiguration serverConfig) {
        final Configuration configuration = get().orElseThrow();
        final Optional<ServerConfiguration> serverConfigurationOpt = get(serverConfig.getHost());
        if (serverConfigurationOpt.isPresent()) {
            configuration.update(serverConfig);
        } else {
            configuration.add(serverConfig);
        }
        prettyZooConfigRepository.save(configuration.toPersistModel());
    }

    public Optional<Configuration> get() {
        return Optional.ofNullable(configurationCache.getVal());
    }

    public Optional<ServerConfiguration> get(String host) {
        return get().orElseThrow()
                .getServerConfigurations()
                .stream()
                .filter(s -> s.getHost().equals(host))
                .findFirst();
    }

    public void deleteServerConfiguration(String server) {
        Objects.requireNonNull(configurationCache.getVal());
        configurationCache.getVal().delete(server);
        prettyZooConfigRepository.save(configurationCache.getVal().toPersistModel());
    }

    public Boolean containServerConfig(String server) {
        Objects.requireNonNull(configurationCache.getVal());
        return configurationCache.getVal().exists(server);
    }

    public void importConfig(File configFile) {
        try (var stream = Files.newInputStream(configFile.toPath(), StandardOpenOption.READ)) {
            prettyZooConfigRepository.importConfig(stream);
            final Configuration originConfiguration = configurationCache.getVal();
            load(originConfiguration.getConfigurationChangeListeners());
        } catch (IOException e) {
            throw new IllegalStateException("import config failed", e);
        }
    }

    public void exportConfig(File dir) {
        try (var stream = Files.newOutputStream(dir.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
            prettyZooConfigRepository.exportConfig(stream);
        } catch (IOException e) {
            throw new IllegalStateException("export config failed", e);
        }
    }

    private static class Cache<T> {

        private T val;

        public T getVal() {
            return val;
        }

        public void setVal(T val) {
            this.val = val;
        }

    }
}
