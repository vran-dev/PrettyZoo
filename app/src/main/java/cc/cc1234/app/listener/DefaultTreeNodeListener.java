package cc.cc1234.app.listener;

import cc.cc1234.app.cache.TreeItemCache;
import cc.cc1234.specification.listener.NodeEvent;
import cc.cc1234.specification.listener.ZookeeperNodeListener;
import cc.cc1234.specification.node.ZkNode;
import cc.cc1234.specification.util.PathUtils;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class DefaultTreeNodeListener implements ZookeeperNodeListener {

    private static final Logger log = LoggerFactory.getLogger(DefaultTreeNodeListener.class);
    public Set<String> completed = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private TreeItemCache treeItemCache = TreeItemCache.getInstance();

    public DefaultTreeNodeListener() {
    }

    @Override
    public void disConnect(String server) {
        log.warn(server + " is disconnected");
    }

    @Override
    public void onNodeUpdate(NodeEvent event) {
        Platform.runLater(() -> {
            final String path = event.getNode().getPath();
            final TreeItem<ZkNode> item = treeItemCache.get(event.getServer(), path);
            final ZkNode itemValue = item.getValue();
            itemValue.copyField(event.getNode());
        });
    }

    @Override
    public void onNodeDelete(NodeEvent event) {
        final String path = event.getNode().getPath();
        final String parent = PathUtils.getParent(path);
        final TreeItem<ZkNode> parentItem = treeItemCache.get(event.getServer(), parent);
        final TreeItem<ZkNode> removeItem = treeItemCache.get(event.getServer(), path);
        if (parentItem != null && removeItem != null) {
            Platform.runLater(() -> {
                parentItem.getChildren().remove(removeItem);
                treeItemCache.remove(event.getServer(), path);
            });
        }
    }

    @Override
    public void onNodeAdd(NodeEvent event) {
        final ZkNode origin = event.getNode();
        final String path = origin.getPath();
        final String name = PathUtils.getLastPath(path);

        final ZkNode node = new ZkNode(name, path);
        node.copyField(origin);

        final String rootPath = "/";
        Platform.runLater(() -> {
            if (path.equals(rootPath)) {
                final TreeItem<ZkNode> root = treeItemCache.get(event.getServer(), rootPath);
                root.getValue().copyField(origin);
                root.setExpanded(true);
            } else {
                // fixme numOfChildren of the node should increase manual
                final TreeItem<ZkNode> treeItem = new TreeItem<>(node);
                treeItemCache.add(event.getServer(), path, treeItem);
                final String parent = PathUtils.getParent(path);
                final TreeItem<ZkNode> parentItem = treeItemCache.get(event.getServer(), parent);
                parentItem.getChildren().add(treeItem);
            }
        });
    }

    @Override
    public void syncCompleted(String server) {
        completed.add(server);
        log.info("{} sync completed, loaded = ?, total = ?", server);
    }

    private boolean skip(String node, NodeEvent event) {
        return !completed.contains(event.getServer()) || Objects.equals(node, "/");
    }
}
