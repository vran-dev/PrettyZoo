package cc.cc1234.spi.connection;

public interface ZookeeperConnectionFactory<T> {

    ZookeeperConnection<T> create(ZookeeperParams params);

}
