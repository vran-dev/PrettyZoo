package cc.cc1234.main.cache;

import cc.cc1234.main.vo.PrettyZooConfigVO;

import java.util.Optional;

public class PrettyZooConfigContext {

    private static volatile Optional<PrettyZooConfigVO> config = Optional.empty();

    public static void set(PrettyZooConfigVO prettyZooConfigVO) {
        config = Optional.ofNullable(prettyZooConfigVO);
    }

    public static Optional<PrettyZooConfigVO> getOption() {
        return config;
    }

    public static PrettyZooConfigVO get() {
        return config.orElseThrow(() -> new IllegalStateException("Must initialize config before get"));
    }

}
