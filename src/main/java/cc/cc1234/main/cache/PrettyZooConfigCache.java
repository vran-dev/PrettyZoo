package cc.cc1234.main.cache;

import cc.cc1234.main.model.PrettyZooConfig;

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
