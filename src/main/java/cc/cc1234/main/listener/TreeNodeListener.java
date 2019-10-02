package cc.cc1234.main.listener;

import cc.cc1234.main.cache.TreeViewCache;
import cc.cc1234.main.controller.NodeTreeViewController;
import cc.cc1234.main.model.ZkNode;
import cc.cc1234.main.util.PathUtils;
import javafx.application.Platform;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

public class TreeNodeListener implements TreeCacheListener {

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

    private TreeViewCache<ZkNode> treeViewCache = TreeViewCache.getInstance();

    public TreeNodeListener(String server) {
        this.server = server;
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
//            Platform.runLater(() -> {
//                nodeSyncProgressBar.setVisible(false);
//            });
        }
    }

    public void onNodeUpdated(TreeCacheEvent event) {
        final String path = event.getData().getPath();
        final TreeItem<ZkNode> item = treeViewCache.get(server, path);
        final ZkNode node = item.getValue();
        node.setStat(event.getData().getStat());
        node.setData(new String(event.getData().getData()));
    }

    public void onNodeRemoved(TreeCacheEvent event) {
        final String path = event.getData().getPath();
        final String parent = PathUtils.getParent(path);
        final TreeItem<ZkNode> parentItem = treeViewCache.get(server, parent);
        final TreeItem<ZkNode> removeItem = treeViewCache.get(server, path);

        // note: must be clear selection before remove
        final TreeView<ZkNode> zkNodeTreeView = treeViewCache.getTreeView();
        final TreeItem<ZkNode> selectedItem = zkNodeTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem == removeItem) {
            zkNodeTreeView.getSelectionModel().clearSelection();
        }
        parentItem.getChildren().remove(removeItem);
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
        if (path.equals(NodeTreeViewController.ROOT_PATH)) {
            final TreeView<ZkNode> treeView = treeViewCache.getTreeView();
            final ZkNode exists = treeView.getRoot().getValue();
            exists.setStat(eventData.getStat());
            exists.setData(new String(eventData.getData()));
            treeView.getRoot().setExpanded(true);
        } else {
            final TreeItem<ZkNode> treeItem = new TreeItem<>(node);
            treeViewCache.add(server, path, treeItem);
            final String parent = PathUtils.getParent(path);
            final TreeItem<ZkNode> parentItem = treeViewCache.get(server, parent);
            parentItem.getChildren().add(treeItem);
            increaseNumOfChildField(path);
        }
    }

    private void increaseNumOfChildField(String node) {
        if (!completed || Objects.equals(node, NodeTreeViewController.ROOT_PATH)) {
            return;
        }

        final TreeItem<ZkNode> parentItem = treeViewCache.get(server, PathUtils.getParent(node));
        final int numChildren = parentItem.getValue().getNumChildren();
        parentItem.getValue().setNumChildren(numChildren + 1);
    }

    private void decreaseNumOfChildFiled(String node) {
        if (!completed || Objects.equals(node, NodeTreeViewController.ROOT_PATH)) {
            return;
        }
        final TreeItem<ZkNode> parentItem = treeViewCache.get(server, PathUtils.getParent(node));
        final int numChildren = parentItem.getValue().getNumChildren();
        if (numChildren == 0) {
            return;
        }
        parentItem.getValue().setNumChildren(numChildren - 1);
    }


}
