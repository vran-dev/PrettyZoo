package cc.cc1234.spi.listener;


import cc.cc1234.spi.config.model.ServerConfig;

public interface PrettyZooConfigChangeListener {

    default void onServerAdd(ServerConfig serverConfig) {

    }

    default void onServerRemove(ServerConfig serverConfig) {

    }

    default void onServerChange(ServerConfig oldValue, ServerConfig newValue) {

    }

}
