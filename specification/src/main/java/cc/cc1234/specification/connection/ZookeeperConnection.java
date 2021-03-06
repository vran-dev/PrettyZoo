package cc.cc1234.specification.connection;

import cc.cc1234.specification.listener.ZookeeperNodeListener;
import cc.cc1234.specification.node.NodeMode;

import java.util.List;

public interface ZookeeperConnection<T> {

    void create(String path, String data, boolean recursive, NodeMode mode) throws Exception;

    void delete(String path, boolean recursive) throws Exception;

    void setData(String path, String data) throws Exception;

    void close();

    T getClient();

    void sync(List<ZookeeperNodeListener> listeners);
}
