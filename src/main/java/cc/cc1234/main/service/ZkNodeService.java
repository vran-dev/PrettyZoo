package cc.cc1234.main.service;

import cc.cc1234.main.cache.ActiveServerContext;
import cc.cc1234.main.cache.CuratorCache;
import cc.cc1234.main.listener.TreeNodeListener;
import cc.cc1234.main.model.ZkServerConfig;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.CreateBuilder;
import org.apache.curator.framework.api.DeleteBuilder;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class ZkNodeService {

    private static final Logger log = LoggerFactory.getLogger(ZkNodeService.class);

    public Optional<CuratorFramework> connectIfNecessary(ZkServerConfig config) throws InterruptedException {
        final Optional<CuratorFramework> result = CuratorCache.getClientOption(config.getHost());
        if (result.isPresent()) {
            log.debug("{} is connected, return cache client", config.getHost());
            return result;
        } else {
            final RetryOneTime retryPolicy = new RetryOneTime(3000);
            final CuratorFramework client = CuratorFrameworkFactory.builder()
                    .connectString(config.getHost())
                    .retryPolicy(retryPolicy)
                    .build();
            client.start();
            // TODO use async
            if (!client.blockUntilConnected(5, TimeUnit.SECONDS)) {
                return Optional.empty();
            }
            CuratorCache.put(config.getHost(), client);
            return Optional.of(client);
        }
    }

    public boolean syncIfNecessary(String host) {
        final CuratorFramework client = CuratorCache.getClient(host);
        return CuratorCache.getTreeCache(host)
                .map(treeCache -> true)
                .orElseGet(() -> {
                    log.debug("begin to sync tree node from {}", host);
                    TreeCache treeCache = new TreeCache(client, "/");
                    treeCache.getListenable()
                            .addListener(new TreeNodeListener(host));
                    try {
                        treeCache.start();
                        CuratorCache.put(host, treeCache);
                        return true;
                    } catch (Exception e) {
                        log.error("sync zookeeper node error", e);
                        treeCache.close();
                        return false;
                    }
                });
    }


    public void add(String path,
                    String data,
                    CreateMode mode,
                    boolean recursive) throws Exception {
        final String host = ActiveServerContext.get();
        final CuratorFramework client = CuratorCache.getClient(host);
        final CreateBuilder createBuilder = client.create();
        if (recursive) {
            createBuilder.creatingParentsIfNeeded()
                    .withMode(mode)
                    .forPath(path, data.getBytes());
        } else {
            createBuilder.withMode(mode)
                    .forPath(path, data.getBytes());
        }
    }


    public void setData(String path, String data, Consumer<Exception> errorCallback) {
        try {
            final String host = ActiveServerContext.get();
            CuratorCache.getClient(host).setData().forPath(path, data.getBytes());
        } catch (Exception e) {
            log.error("set data failed", e);
            errorCallback.accept(e);
        }
    }

    public void delete(String path, boolean recursive, Consumer<Exception> errorCallback) {
        final String host = ActiveServerContext.get();
        final DeleteBuilder deleteBuilder = CuratorCache.getClient(host).delete();
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

}
