package cc.cc1234.client.curator;

import cc.cc1234.spi.connection.ZookeeperConnection;
import cc.cc1234.spi.listener.ZookeeperNodeListener;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CreateBuilder;
import org.apache.curator.framework.api.DeleteBuilder;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.zookeeper.CreateMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CuratorZookeeperConnection implements ZookeeperConnection<CuratorFramework> {

    private static final Logger log = LoggerFactory.getLogger(CuratorZookeeperConnection.class);

    private final CuratorFramework curatorFramework;

    private final TreeCache treeCache;

    private AtomicBoolean isSync = new AtomicBoolean(false);

    public CuratorZookeeperConnection(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
        treeCache = new TreeCache(curatorFramework, "/");
    }

    @Override
    public void create(String path, String data, boolean recursive, CreateMode mode) throws Exception {
        final CreateBuilder createBuilder = getClient().create();
        if (recursive) {
            createBuilder.creatingParentsIfNeeded()
                    .withMode(mode)
                    .forPath(path, data.getBytes());
        } else {
            createBuilder.withMode(mode)
                    .forPath(path, data.getBytes());
        }
    }

    @Override
    public void delete(String path, boolean recursive) throws Exception {
        final DeleteBuilder deleteBuilder = getClient().delete();
        if (recursive) {
            deleteBuilder.deletingChildrenIfNeeded().forPath(path);
        } else {
            deleteBuilder.forPath(path);
        }
    }

    @Override
    public void setData(String path, String data) throws Exception {
        getClient().setData().forPath(path, data.getBytes());
    }

    @Override
    public void close() {
        treeCache.close();
        curatorFramework.close();
        isSync.set(false);
    }

    @Override
    public CuratorFramework getClient() {
        return this.curatorFramework;
    }

    @Override
    public void sync(List<ZookeeperNodeListener> listeners) {
        final String server = curatorFramework.getZookeeperClient().getCurrentConnectionString();
        if (!isSync.get()) {
            log.debug("begin to sync tree node from {}", server);
            treeCache.getListenable().addListener(new CuratorTreeCacheListener(listeners));
            try {
                treeCache.start();
                isSync.set(true);
            } catch (Exception e) {
                log.error("sync zookeeper node error", e);
                treeCache.close();
            }
        } else {
            log.info("ignore sync operation, because of {} has been sync", server);
        }
    }
}
