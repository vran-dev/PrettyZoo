package cc.cc1234.main.service;

import cc.cc1234.main.listener.TreeNodeListener;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.BackgroundCallback;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.RetryOneTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class ZkServerService {

    private static final Map<String, ZkServerService> ZK_SERVICES = new ConcurrentHashMap<>();

    private static final Logger log = LoggerFactory.getLogger(ZkServerService.class);

    private final String server;

    private volatile CuratorFramework client;

    private volatile TreeCache treeCache;

    private volatile boolean connected = false;

    private TreeNodeListener treeNodeListener;

    private ZkServerService(String server) {
        this.server = server;
        treeNodeListener = new TreeNodeListener(server);
    }

    public static ZkServerService getInstance(String server) {
        return ZK_SERVICES.computeIfAbsent(server, ZkServerService::new);
    }

    public static void close() {
        ZK_SERVICES.forEach((k, v) -> v.closeALl());
    }

    public CuratorFramework connectIfNecessary() throws InterruptedException {
        if (connected) {
            return client;
        }
        if (client == null) {
            synchronized (this) {
                if (client == null) {
                    final RetryOneTime retryPolicy = new RetryOneTime(3000);
                    client = CuratorFrameworkFactory.newClient(this.server, retryPolicy);
                    client.start();
                    // TODO @vran use connection listener
                    final boolean res = client.blockUntilConnected(5, TimeUnit.SECONDS);
                    if (!res) {
                        throw new InterruptedException("connect timeout");
                    }
                    connected = true;
                }
            }
        }
        return client;
    }

    public void syncNodeIfNecessary() {
        if (treeCache == null) {
            synchronized (this) {
                if (treeCache == null) {
                    treeCache = new TreeCache(client, "/");
                    treeCache.getListenable().addListener(treeNodeListener);
                    try {
                        treeCache.start();
                    } catch (Exception e) {
                        log.error("sync zookeeper node error", e);
                    }
                }
            }
        }
    }

    public void delete(String path) throws Exception {
        client.delete().forPath(path);
    }

    public void delete(String path, BackgroundCallback callback) throws Exception {
        client.delete()
                .inBackground(callback)
                .forPath(path);
    }

    public void setData(String path, String data) throws Exception {
        client.setData().forPath(path, data.getBytes());
    }

    public void setData(String path, String data, BackgroundCallback callback) throws Exception {
        client.setData()
                .inBackground(callback)
                .forPath(path, data.getBytes());
    }

    public void closeALl() {
        if (treeCache != null) {
            treeCache.close();
        }

        if (client != null) {
            client.close();
        }
    }

    public CuratorFramework getClient() {
        return client;
    }
}
