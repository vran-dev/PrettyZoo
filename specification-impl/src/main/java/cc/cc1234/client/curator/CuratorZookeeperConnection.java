package cc.cc1234.client.curator;

import cc.cc1234.specification.connection.ZookeeperConnection;
import cc.cc1234.specification.listener.ZookeeperNodeListener;
import cc.cc1234.specification.node.NodeMode;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CreateBuilder;
import org.apache.curator.framework.api.DeleteBuilder;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class CuratorZookeeperConnection implements ZookeeperConnection<CuratorFramework> {

    private static final Logger log = LoggerFactory.getLogger(CuratorZookeeperConnection.class);

    private final String id;

    private final CuratorFramework curatorFramework;

    private final TreeCache treeCache;

    private AtomicBoolean isSync = new AtomicBoolean(false);

    public CuratorZookeeperConnection(String id, CuratorFramework curatorFramework) {
        this.id = id;
        this.curatorFramework = curatorFramework;
        treeCache = new TreeCache(curatorFramework, "/");
    }

    @Override
    public void create(String path, String data, boolean recursive, NodeMode mode) throws Exception {
        final CreateBuilder createBuilder = getClient().create();
        final CreateMode createMode = CreateMode.valueOf(mode.name());
        if (recursive) {
            createBuilder.creatingParentsIfNeeded()
                    .withMode(createMode)
                    .forPath(path, data.getBytes());
        } else {
            createBuilder.withMode(createMode)
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
    public void deleteAsync(List<String> pathList) throws Exception {
        for (String s : pathList) {
            getClient()
                    .delete()
                    .deletingChildrenIfNeeded()
                    .inBackground()
                    .forPath(s);
        }
    }

    @Override
    public Stat setData(String path, String data) throws Exception {
        return getClient().setData().forPath(path, data.getBytes());
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
            treeCache.getListenable().addListener(new CuratorTreeCacheListener(id, listeners));
            try {
                treeCache.start();
                isSync.set(true);
            } catch (Exception e) {
                log.error("sync zookeeper node error", e);
                treeCache.close();
            }
        } else {
            log.info("ignore sync operation, because of {} [{}] has been sync", server, getId());
        }
    }

    @Override
    public String getId() {
        return this.id;
    }
}
