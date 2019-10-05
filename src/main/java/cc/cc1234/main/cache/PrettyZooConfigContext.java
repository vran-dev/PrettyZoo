package cc.cc1234.main.cache;

import cc.cc1234.main.model.PrettyZooConfig;

import java.util.Optional;

public class PrettyZooConfigContext {

    private static volatile Optional<PrettyZooConfig> config = Optional.empty();

    public static void set(PrettyZooConfig prettyZooConfig) {
        config = Optional.ofNullable(prettyZooConfig);
    }

    public static Optional<PrettyZooConfig> getOption() {
        return config;
    }

    public static PrettyZooConfig get() {
        return config.orElseThrow(() -> new IllegalStateException("Must initialize config before get"));
    }

}
