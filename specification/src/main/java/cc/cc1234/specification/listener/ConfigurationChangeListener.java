package cc.cc1234.specification.listener;

import cc.cc1234.specification.config.model.ServerConfigData;

import java.util.List;

public interface ConfigurationChangeListener {

    default void onServerAdd(ServerConfigData serverConfig) {

    }

    default void onServerRemove(ServerConfigData serverConfig) {

    }

    default void onServerChange(ServerConfigData newValue) {

    }

    default void onReload(List<ServerConfigData> configs) {

    }

}
