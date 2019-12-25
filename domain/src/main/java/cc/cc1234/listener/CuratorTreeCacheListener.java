package cc.cc1234.listener;

import cc.cc1234.cache.TreeItemCache;
import cc.cc1234.controller.TreeNodeViewController;
import cc.cc1234.model.ZkNode;
import cc.cc1234.util.PathUtils;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class CuratorTreeCacheListener implements TreeCacheListener {

    private static final Logger log = LoggerFactory.getLogger(CuratorTreeCacheListener.class);

    /**
     * loaded node num
     */
    private AtomicInteger loadedNodeNum = new AtomicInteger(0);

    /**
     * total node num
     */
    private AtomicInteger totalNodeNum = new AtomicInteger(0);

    private volatile boolean completed = false;

    private final String server;

    private TreeItemCache treeItemCache = TreeItemCache.getInstance();

    private TreeNodeListeners listeners;

    public CuratorTreeCacheListener(String server, TreeNodeListeners listeners) {
        this.server = server;
        this.listeners = listeners;
    }

    @Override
    public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
        if (event.getType() == TreeCacheEvent.Type.NODE_ADDED) {
            Platform.runLater(() -> onNodeAdded(event));
        }

        if (event.getType() == TreeCacheEvent.Type.NODE_REMOVED) {
            Platform.runLater(() -> onNodeRemoved(event));
        }

        if (event.getType() == TreeCacheEvent.Type.NODE_UPDATED) {
            Platform.runLater(() -> onNodeUpdated(event));
        }

        if (event.getType() == TreeCacheEvent.Type.INITIALIZED) {
            completed = true;
            log.debug("{} tree node sync finished", server);
        }
    }

    public void onNodeUpdated(TreeCacheEvent event) {
        final String path = event.getData().getPath();
        final TreeItem<ZkNode> item = treeItemCache.get(server, path);
        final ZkNode node = item.getValue();
        node.setStat(event.getData().getStat());
        node.setData(new String(event.getData().getData()));
        listeners.onNodeUpdate(node);
    }

    public void onNodeRemoved(TreeCacheEvent event) {
        final String path = event.getData().getPath();
        final String parent = PathUtils.getParent(path);
        final TreeItem<ZkNode> parentItem = treeItemCache.get(server, parent);
        final TreeItem<ZkNode> removeItem = treeItemCache.get(server, path);
        parentItem.getChildren().remove(removeItem);
        treeItemCache.remove(server, path);
        decreaseNumOfChildFiled(path);
    }

    public void onNodeAdded(TreeCacheEvent event) {
        final ChildData eventData = event.getData();
        final String path = eventData.getPath();
        final String name = PathUtils.getLastPath(path);

        loadedNodeNum.incrementAndGet();
        final ZkNode node = new ZkNode(name, path);
        node.setStat(eventData.getStat());
        node.setData(new String(eventData.getData()));

        totalNodeNum.addAndGet(eventData.getStat().getNumChildren());
        if (path.equals(TreeNodeViewController.ROOT_PATH)) {
            final TreeItem<ZkNode> root = treeItemCache.get(server, TreeNodeViewController.ROOT_PATH);
            root.getValue().setStat(eventData.getStat());
            root.getValue().setData(new String(eventData.getData()));
            root.setExpanded(true);
        } else {
            final TreeItem<ZkNode> treeItem = new TreeItem<>(node);
            treeItemCache.add(server, path, treeItem);
            final String parent = PathUtils.getParent(path);
            final TreeItem<ZkNode> parentItem = treeItemCache.get(server, parent);
            parentItem.getChildren().add(treeItem);
            increaseNumOfChildField(path);
        }
    }

    private void increaseNumOfChildField(String node) {
        if (skip(node)) {
            return;
        }

        final TreeItem<ZkNode> parentItem = treeItemCache.get(server, PathUtils.getParent(node));
        final int numChildren = parentItem.getValue().getNumChildren();
        parentItem.getValue().setNumChildren(numChildren + 1);
    }

    private void decreaseNumOfChildFiled(String node) {
        if (skip(node)) {
            return;
        }
        final TreeItem<ZkNode> parentItem = treeItemCache.get(server, PathUtils.getParent(node));
        final int numChildren = parentItem.getValue().getNumChildren();
        if (numChildren == 0) {
            return;
        }
        parentItem.getValue().setNumChildren(numChildren - 1);
    }

    private boolean skip(String node) {
        return loadedNodeNum.get() < totalNodeNum.get() || !completed || Objects.equals(node, TreeNodeViewController.ROOT_PATH);
    }


}
