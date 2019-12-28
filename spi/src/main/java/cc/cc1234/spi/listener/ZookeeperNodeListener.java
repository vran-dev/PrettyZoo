package cc.cc1234.spi.listener;


public interface ZookeeperNodeListener {

    default void onNodeUpdate(NodeEvent event) {

    }

    default void onNodeDelete(NodeEvent event) {

    }

    default void onNodeAdd(NodeEvent event) {

    }

    default void syncCompleted(String server) {

    }

}
