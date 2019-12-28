package cc.cc1234.cache;

import cc.cc1234.spi.config.model.RootConfig;

import java.util.Optional;

public class PrettyZooConfigCache {

    private static volatile Optional<RootConfig> config = Optional.empty();

    public static void set(RootConfig prettyZooConfigVO) {
        config = Optional.ofNullable(prettyZooConfigVO);
    }

    public static Optional<RootConfig> getOption() {
        return config;
    }

    public static RootConfig get() {
        return config.orElseThrow(() -> new IllegalStateException("Must initialize config before get"));
    }

}
