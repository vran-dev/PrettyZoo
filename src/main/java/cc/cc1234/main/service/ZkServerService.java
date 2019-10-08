package cc.cc1234.main.service;

import cc.cc1234.main.cache.ActiveServerContext;
import cc.cc1234.main.cache.TreeViewCache;
import cc.cc1234.main.listener.TreeNodeListener;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.DeleteBuilder;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.RetryOneTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ZkServerService {

    private static final Map<String, ZkServerService> ZK_SERVICES = new ConcurrentHashMap<>();

    private static final Logger log = LoggerFactory.getLogger(ZkServerService.class);

    private final String host;

    private volatile CuratorFramework client;

    private volatile TreeCache treeCache;

    private volatile boolean connected = false;

    private TreeNodeListener treeNodeListener;

    private ZkServerService(String host) {
        this.host = host;
        treeNodeListener = new TreeNodeListener(host);
    }

    public static ZkServerService getOrCreate(String server) {
        return ZK_SERVICES.computeIfAbsent(server, ZkServerService::new);
    }

    public static ZkServerService getActive() {
        return ZK_SERVICES.get(ActiveServerContext.get());
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
                    client = CuratorFrameworkFactory.newClient(this.host, retryPolicy);
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

    public void delete(String path, boolean recursive, Consumer<Exception> errorCallback) {
        final DeleteBuilder deleteBuilder = client.delete();
        try {
            if (recursive) {
                deleteBuilder.deletingChildrenIfNeeded().forPath(path);
            } else {
                deleteBuilder.forPath(path);
            }
        } catch (Exception e) {
            log.error("delete node failed", e);
            errorCallback.accept(e);
        }
    }


    public void setData(String path, String data, Consumer<Exception> errorCallback) {
        try {
            client.setData().forPath(path, data.getBytes());
        } catch (Exception e) {
            log.error("set data failed", e);
            errorCallback.accept(e);
        }
    }

    public void closeALl() {
        ZK_SERVICES.remove(host);
        TreeViewCache.getInstance().clear(host);

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
