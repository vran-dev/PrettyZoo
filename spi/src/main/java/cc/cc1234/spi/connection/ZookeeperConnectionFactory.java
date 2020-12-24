package cc.cc1234.spi.connection;

import cc.cc1234.spi.config.model.ServerConfig;

public interface ZookeeperConnectionFactory<T> {

    ZookeeperConnection<T> create(ServerConfig config);

}
