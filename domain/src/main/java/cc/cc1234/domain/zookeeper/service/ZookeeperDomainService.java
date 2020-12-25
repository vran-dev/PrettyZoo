package cc.cc1234.domain.zookeeper.service;

import cc.cc1234.domain.configuration.entity.ServerConfiguration;
import cc.cc1234.domain.zookeeper.entity.Zookeeper;
import cc.cc1234.domain.zookeeper.factory.ZookeeperFactory;
import cc.cc1234.spi.listener.ServerListener;
import cc.cc1234.spi.listener.ZookeeperNodeListener;
import org.apache.zookeeper.CreateMode;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ZookeeperDomainService {

    private static final Map<String, Zookeeper> zookeeperMap = new ConcurrentHashMap<>();

    public void connect(ServerConfiguration serverConfig,
                        List<ZookeeperNodeListener> nodeListeners,
                        List<ServerListener> serverListeners) {
        if (!zookeeperMap.containsKey(serverConfig.getHost())) {
            Zookeeper zookeeper = new ZookeeperFactory().create(serverConfig, nodeListeners, serverListeners);
            zookeeperMap.put(serverConfig.getHost(), zookeeper);
        }
    }

    public void disconnect(String host) {
        if (zookeeperMap.containsKey(host)) {
            zookeeperMap.get(host).disconnect();
            zookeeperMap.remove(host);
        }
    }

    public void disconnectAll() {
        zookeeperMap.values().forEach(Zookeeper::disconnect);
        zookeeperMap.clear();
    }

    public void sync(String host) {
        assertZookeeperExists(host);
        zookeeperMap.get(host).sync();
    }

    public void set(String host, String path, String data) throws Exception {
        assertZookeeperExists(host);
        zookeeperMap.get(host).set(path, data);
    }

    public void delete(String host, String path) throws Exception {
        assertZookeeperExists(host);
        zookeeperMap.get(host).delete(path);
    }

    public void create(String host, String path, String data, CreateMode mode) throws Exception {
        assertZookeeperExists(host);
        zookeeperMap.get(host).create(path, data, mode);
    }

    private void assertZookeeperExists(String host) {
        if (!zookeeperMap.containsKey(host)) {
            throw new IllegalStateException("connect zookeeper first " + host);
        }
    }
}
