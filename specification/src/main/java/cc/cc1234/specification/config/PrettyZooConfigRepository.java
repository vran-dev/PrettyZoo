package cc.cc1234.specification.config;

import cc.cc1234.specification.config.model.ConfigData;

import java.io.InputStream;
import java.io.OutputStream;

public interface PrettyZooConfigRepository {

    /**
     * will be remove in future
     */
    @Deprecated
    String OLD_CONFIG_PATH = System.getProperty("user.home") + "/.prettyZoo/server-input.history";

    String CONFIG_PATH = System.getProperty("user.home") + "/.prettyZoo/prettyZoo.cfg";

    ConfigData get();

    void save(ConfigData config);

    default void importConfig(InputStream stream) {

    }

    default void exportConfig(OutputStream targetStream) {
    }
}
