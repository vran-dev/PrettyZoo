package cc.cc1234.main.curator;

import cc.cc1234.main.cache.TreeItemCache;
import cc.cc1234.main.controller.NodeTreeViewController;
import cc.cc1234.main.model.ZkNode;
import cc.cc1234.main.util.PathUtils;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;

import java.util.concurrent.atomic.AtomicInteger;

public class NodeEventHandler {

    private TreeItemCache serverTreeCache = TreeItemCache.getInstance();

    /**
     * loaded node num
     */
    private AtomicInteger loadedNodeNum = new AtomicInteger(0);

    /**
     * total node num
     */
    private AtomicInteger totalNodeNum = new AtomicInteger(0);

    private final String activeServer;

    public NodeEventHandler(String activeServer) {
        this.activeServer = activeServer;
    }

    public void onNodeUpdated(TreeCacheEvent event) {
        final String path = event.getData().getPath();
        final TreeItem<ZkNode> item = serverTreeCache.getItemByPath(activeServer, path);
        final ZkNode node = item.getValue();
        node.setStat(event.getData().getStat());
        node.setData(new String(event.getData().getData()));
    }

    public void onNodeRemoved(TreeView<ZkNode> zkNodeTreeView, TreeCacheEvent event) {
        final String path = event.getData().getPath();
        final String parent = PathUtils.getParent(path);
        final TreeItem<ZkNode> parentItem = serverTreeCache.getItemByPath(activeServer, parent);
        final TreeItem<ZkNode> removeItem = serverTreeCache.getItemByPath(activeServer, path);

        // note: must be clear selection before remove
        final TreeItem<ZkNode> selectedItem = zkNodeTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem == removeItem) {
            zkNodeTreeView.getSelectionModel().clearSelection();
        }
        parentItem.getChildren().remove(removeItem);
    }

    public void onNodeAdded(TreeView<ZkNode> zkNodeTreeView, TreeCacheEvent event) {
        final ChildData eventData = event.getData();
        final String path = eventData.getPath();
        final String name = PathUtils.getLastPath(path);

        loadedNodeNum.incrementAndGet();
        final ZkNode node = new ZkNode(name, path);
        node.setStat(eventData.getStat());
        node.setData(new String(eventData.getData()));
        final TreeItem<ZkNode> treeItem = new TreeItem<>(node);
        serverTreeCache.cacheItemByPath(activeServer, path, treeItem);

        totalNodeNum.addAndGet(eventData.getStat().getNumChildren());
        if (path.equals(NodeTreeViewController.ROOT_PATH)) {
            treeItem.setExpanded(true);
            zkNodeTreeView.setRoot(treeItem);
        } else {
            final String parent = PathUtils.getParent(path);
            final TreeItem<ZkNode> parentItem = serverTreeCache.getItemByPath(activeServer, parent);
            parentItem.getChildren().add(treeItem);
        }
    }



}
