package cc.cc1234.spi.config;


import cc.cc1234.spi.config.model.RootConfig;

public interface PrettyZooConfigRepository {

    String CONFIG_PATH = System.getProperty("user.home") + "/.prettyZoo/server-input.history";

    RootConfig get();

    void save(RootConfig config);
}
