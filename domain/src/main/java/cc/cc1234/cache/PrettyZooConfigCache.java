package cc.cc1234.cache;

import cc.cc1234.model.PrettyZooConfig;

import java.util.Optional;

public class PrettyZooConfigCache {

    private static volatile Optional<PrettyZooConfig> config = Optional.empty();

    public static void set(PrettyZooConfig prettyZooConfigVO) {
        config = Optional.ofNullable(prettyZooConfigVO);
    }

    public static Optional<PrettyZooConfig> getOption() {
        return config;
    }

    public static PrettyZooConfig get() {
        return config.orElseThrow(() -> new IllegalStateException("Must initialize config before get"));
    }

}
