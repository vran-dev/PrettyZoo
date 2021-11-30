package cc.cc1234.specification.listener;

public interface ZookeeperNodeListener {

    default void onNodeUpdate(NodeEvent event) {

    }

    default void onNodeDelete(NodeEvent event) {

    }

    default void onNodeAdd(NodeEvent event) {

    }

    default void syncCompleted(String server) {

    }

    default void disConnect(String server) {

    }

    default void reconnected(String server) {

    }
}
