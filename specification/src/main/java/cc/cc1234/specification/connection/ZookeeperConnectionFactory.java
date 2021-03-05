package cc.cc1234.specification.connection;

public interface ZookeeperConnectionFactory<T> {

    ZookeeperConnection<T> create(ZookeeperParams params);

}
