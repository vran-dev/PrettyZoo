package cc.cc1234.spi.config;


import cc.cc1234.spi.config.model.RootConfig;

import java.io.InputStream;
import java.io.OutputStream;

public interface PrettyZooConfigRepository {

    String CONFIG_PATH = System.getProperty("user.home") + "/.prettyZoo/server-input.history";

    RootConfig get();

    void save(RootConfig config);

    default void importConfig(InputStream stream) {

    }

    default void exportConfig(RootConfig config, OutputStream targetStream) {
    }
}
