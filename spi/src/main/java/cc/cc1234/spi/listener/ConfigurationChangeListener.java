package cc.cc1234.spi.listener;


import cc.cc1234.spi.config.model.ServerConfig;

import java.util.List;

public interface ConfigurationChangeListener {

    default void onServerAdd(ServerConfig serverConfig) {

    }

    default void onServerRemove(ServerConfig serverConfig) {

    }

    default void onServerChange(ServerConfig oldValue, ServerConfig newValue) {

    }

    default void onReload(List<ServerConfig> configs) {

    }

}
