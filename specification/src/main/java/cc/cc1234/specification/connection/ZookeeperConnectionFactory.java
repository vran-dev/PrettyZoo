package cc.cc1234.specification.connection;

import cc.cc1234.specification.listener.ServerListener;

import java.util.List;

public interface ZookeeperConnectionFactory<T> {

    ZookeeperConnection<T> create(ZookeeperParams params);

    default ZookeeperConnection<T> createAsync(ZookeeperParams params, List<ServerListener> listener) {
        return null;
    }
}
