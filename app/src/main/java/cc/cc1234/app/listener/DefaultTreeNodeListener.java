package cc.cc1234.app.listener;

import cc.cc1234.app.cache.TreeItemCache;
import cc.cc1234.spi.listener.NodeEvent;
import cc.cc1234.spi.listener.ZookeeperNodeListener;
import cc.cc1234.spi.node.ZkNode;
import cc.cc1234.spi.util.PathUtils;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class DefaultTreeNodeListener implements ZookeeperNodeListener {

    private static final Logger log = LoggerFactory.getLogger(DefaultTreeNodeListener.class);

    private TreeItemCache treeItemCache = TreeItemCache.getInstance();

    public Set<String> completed = Collections.newSetFromMap(new ConcurrentHashMap<>());

    // TODO
    private Map<String, AtomicInteger> loadedNodeNum = new ConcurrentHashMap<>();

    // TODO
    private Map<String, AtomicInteger> totalNodeNum = new ConcurrentHashMap<>();


    public DefaultTreeNodeListener() {
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
        Platform.runLater(() -> {
            parentItem.getChildren().remove(removeItem);
            treeItemCache.remove(event.getServer(), path);
//        totalNodeNum.get(event.getServer()).decrementAndGet();
//        loadedNodeNum.get(event.getServer()).decrementAndGet();
            decreaseNumOfChildFiled(path, event);
        });
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
                increaseNumOfChildField(path, event);
            }
        });
    }


    private void increaseNumOfChildField(String node, NodeEvent event) {
        if (skip(node, event)) {
            return;
        }

        final TreeItem<ZkNode> parentItem = treeItemCache.get(event.getServer(), PathUtils.getParent(node));
        final int numChildren = parentItem.getValue().getNumChildren();
        parentItem.getValue().setNumChildren(numChildren + 1);
    }

    private void decreaseNumOfChildFiled(String node, NodeEvent event) {
        if (skip(node, event)) {
            return;
        }
        final TreeItem<ZkNode> parentItem = treeItemCache.get(event.getServer(), PathUtils.getParent(node));
        final int numChildren = parentItem.getValue().getNumChildren();
        if (numChildren == 0) {
            return;
        }
        parentItem.getValue().setNumChildren(numChildren - 1);
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
