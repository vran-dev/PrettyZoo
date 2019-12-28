package cc.cc1234.spi.connection;

import cc.cc1234.spi.listener.ZookeeperNodeListener;
import org.apache.zookeeper.CreateMode;

import java.util.List;

public interface ZookeeperConnection<T> {

    void create(String path, String data, boolean recursive, CreateMode mode) throws Exception;

    void delete(String path, boolean recursive) throws Exception;

    void setData(String path, String data) throws Exception;

    void close();

    T getClient();

    void sync(List<ZookeeperNodeListener> listeners);
}
