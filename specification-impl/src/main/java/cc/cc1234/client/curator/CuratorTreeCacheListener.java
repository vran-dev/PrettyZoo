package cc.cc1234.client.curator;

import cc.cc1234.specification.listener.NodeEvent;
import cc.cc1234.specification.listener.ZookeeperNodeListener;
import cc.cc1234.specification.node.ZkNode;
import cc.cc1234.specification.util.PathUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.List;

public class CuratorTreeCacheListener implements TreeCacheListener {

    private static final Logger log = LoggerFactory.getLogger(CuratorTreeCacheListener.class);

    private String id;

    private List<ZookeeperNodeListener> listeners;

    public CuratorTreeCacheListener(String id,
                                    List<ZookeeperNodeListener> listeners) {
        this.id = id;
        this.listeners = listeners;
    }

    @Override
    public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
        final String server = client.getZookeeperClient().getCurrentConnectionString();
        if (event.getType() == TreeCacheEvent.Type.INITIALIZED) {
            log.debug("{} tree node sync finished", server);
            listeners.forEach(listeners -> listeners.syncCompleted(id));
            return;
        }
        if (event.getType() == TreeCacheEvent.Type.CONNECTION_SUSPENDED
                || event.getType() == TreeCacheEvent.Type.CONNECTION_LOST) {
            listeners.forEach(listeners -> listeners.disConnect(id));
            return;
        }

        if (event.getType() == TreeCacheEvent.Type.CONNECTION_RECONNECTED) {
            listeners.forEach(listeners -> listeners.reconnected(id));
            return;
        }

        final String path = event.getData().getPath();
        final ZkNode node = new ZkNode();
        node.setPath(path);
        node.setStat(event.getData().getStat());
        if (event.getData().getData() == null) {
            // TODO check null
            node.setData("");
            node.setDataBytes(new byte[0]);
        } else {
            node.setData(new String(event.getData().getData(), StandardCharsets.UTF_8));
            node.setDataBytes(event.getData().getData());
        }

        node.setName(PathUtils.getLastPath(path));

        if (event.getType() == TreeCacheEvent.Type.NODE_ADDED) {
            listeners.forEach(listener -> listener.onNodeAdd(new NodeEvent(node, server, id)));
        }

        if (event.getType() == TreeCacheEvent.Type.NODE_REMOVED) {
            listeners.forEach(listener -> listener.onNodeDelete(new NodeEvent(node, server, id)));

        }

        if (event.getType() == TreeCacheEvent.Type.NODE_UPDATED) {
            listeners.forEach(listener -> listener.onNodeUpdate(new NodeEvent(node, server, id)));
        }
    }
}
