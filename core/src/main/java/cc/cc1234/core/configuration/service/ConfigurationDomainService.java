package cc.cc1234.core.configuration.service;

import cc.cc1234.config.JsonPrettyZooConfigRepository;
import cc.cc1234.core.configuration.entity.Configuration;
import cc.cc1234.core.configuration.entity.ServerConfiguration;
import cc.cc1234.core.configuration.factory.ConfigurationFactory;
import cc.cc1234.specification.config.PrettyZooConfigRepository;
import cc.cc1234.specification.listener.ConfigurationChangeListener;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Optional;

@Slf4j
public class ConfigurationDomainService {

    private static final Cache<Configuration> configurationCache = new Cache<>();

    private PrettyZooConfigRepository prettyZooConfigRepository = new JsonPrettyZooConfigRepository();

    public Configuration load(List<ConfigurationChangeListener> listeners) {
        final Configuration configuration = new ConfigurationFactory().create(listeners);
        configurationCache.setVal(configuration);
        prettyZooConfigRepository.save(configuration.toPersistModel());
        return configuration;
    }

    public Configuration load() {
        return new ConfigurationFactory().create(List.of());
    }

    public void save(ServerConfiguration serverConfig) {
        final Configuration configuration = get().orElseThrow();
        final Optional<ServerConfiguration> serverConfigurationOpt = get(serverConfig.getUrl());
        if (serverConfigurationOpt.isPresent()) {
            configuration.update(serverConfig);
        } else {
            configuration.add(serverConfig);
        }
        prettyZooConfigRepository.save(configuration.toPersistModel());
    }

    public void save(Configuration.FontConfiguration fontConfiguration) {
        fontConfiguration.checkIsValid();
        var configuration = get().orElseThrow();
        configuration.updateFont(fontConfiguration);
        prettyZooConfigRepository.save(configuration.toPersistModel());
    }

    public void save(Configuration.LocaleConfiguration localeConfiguration) {
        Objects.requireNonNull(localeConfiguration);
        var configuration = get().orElseThrow();
        configuration.updateLocale(localeConfiguration);
        prettyZooConfigRepository.save(configuration.toPersistModel());
    }

    public void saveMainSplitPaneDividerPosition(Double value) {
        var configuration = get().orElseThrow();
        configuration.getUiConfiguration().setMainSplitPaneDividerPosition(value);
        prettyZooConfigRepository.save(configuration.toPersistModel());
    }

    public void saveNodeViewSplitPaneDividerPosition(Double value) {
        var configuration = get().orElseThrow();
        configuration.getUiConfiguration().setNodeViewSplitPaneDividerPosition(value);
        prettyZooConfigRepository.save(configuration.toPersistModel());
    }

    public void saveTheme(String theme) {
        var configuration = get().orElseThrow();
        configuration.changeTheme(theme);
        prettyZooConfigRepository.save(configuration.toPersistModel());
    }

    public Optional<Configuration> get() {
        return Optional.ofNullable(configurationCache.getVal());
    }

    public Optional<ServerConfiguration> get(String url) {
        return get().orElseThrow()
                .getServerConfigurations()
                .stream()
                .filter(s -> s.getUrl().equals(url))
                .findFirst();
    }

    public Locale getLocale() {
        final Configuration configuration = new ConfigurationFactory().create(List.of());
        return configuration.getLocaleConfiguration().getLocale();
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
        } catch (Exception e) {
            log.error("import config failed", e);
            throw new IllegalStateException("import config failed", e);
        }
    }

    public void exportConfig(File dir) {
        try (var stream = Files.newOutputStream(dir.toPath(), StandardOpenOption.WRITE, StandardOpenOption.CREATE)) {
            prettyZooConfigRepository.exportConfig(stream);
        } catch (IOException e) {
            log.error("export config failed", e);
            throw new IllegalStateException("export config failed", e);
        }
    }

    public void incrementConnectTimes(String server) {
        get().ifPresent(config -> {
            config.incrementConnectTimes(server);
            prettyZooConfigRepository.save(config.toPersistModel());
        });
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
