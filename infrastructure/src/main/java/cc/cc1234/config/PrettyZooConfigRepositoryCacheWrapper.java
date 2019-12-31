package cc.cc1234.config;

import cc.cc1234.spi.config.PrettyZooConfigRepository;
import cc.cc1234.spi.config.model.RootConfig;

import java.util.Optional;

public class PrettyZooConfigRepositoryCacheWrapper implements PrettyZooConfigRepository {

    private volatile Optional<RootConfig> cache = Optional.empty();

    private PrettyZooConfigRepository configRepository;

    public PrettyZooConfigRepositoryCacheWrapper(PrettyZooConfigRepository configRepository) {
        if (configRepository instanceof PrettyZooConfigRepositoryCacheWrapper) {
            throw new IllegalStateException("can not wrapper self");
        }
        this.configRepository = configRepository;
    }


    @Override
    public RootConfig get() {
        return cache.orElseGet(() -> {
            final RootConfig config = configRepository.get();
            cache = Optional.of(config);
            return config;
        });
    }

    @Override
    public void save(RootConfig config) {
        configRepository.save(config);
        // flush cache
        cache = Optional.of(config);
    }
}
