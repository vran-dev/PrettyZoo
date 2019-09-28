package cc.cc1234.main.controller;

import cc.cc1234.main.manager.CuratorlistenerManager;
import cc.cc1234.main.model.ZkNode;
import cc.cc1234.main.util.PathUtils;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.zookeeper.data.Stat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

public class NodeTreeViewController {

    @FXML
    private TreeView<ZkNode> zkNodeTreeView;

    @FXML
    private Label numChildrenLabel;

    @FXML
    private Label pathLabel;

    @FXML
    private Label ctimeLabel;

    @FXML
    private Label mtimeLabel;

    @FXML
    private Label pZxidLabel;

    @FXML
    private Label ephemeralOwnerLabel;

    @FXML
    private Label dataVersionLabel;

    @FXML
    private Label cZxidLabel;

    @FXML
    private Label mZxidLabel;

    @FXML
    private Label cversionLabel;

    @FXML
    private Label aclVersionLabel;

    @FXML
    private Label dataLengthLabel;

    @FXML
    private TextArea dataTextArea;

    private CuratorlistenerManager curatorlistenerManager;

    private Map<String, TreeItem<ZkNode>> treeItemMap = new ConcurrentHashMap<>();

    private CuratorFramework curatorFramework;

    private long start;

    @FXML
    private void initialize() {
        // init tree item select event listener
        initTreeItemListener();
        // create virtual root item
        initVirtualRoot("/");
    }

    public void viewInit(CuratorFramework client) {
        this.curatorFramework = client;
        // curator treeCache listener
        start = System.currentTimeMillis();
        initZookeeperListener(client);
    }

    private void initTreeItemListener() {
        zkNodeTreeView.getSelectionModel()
                .selectedItemProperty()
                .addListener(((observable, oldValue, newValue) -> {
                    // skip no selected item
                    if (newValue != null) {
                        final ZkNode node = newValue.getValue();
                        this.pathLabel.setText(node.getPath());
                        showStat(node.getStat());
                        showData(node.getData());
                    }
                }));
    }

    private void initVirtualRoot(String root) {
        final ZkNode zkNode = new ZkNode(root, root);
        TreeItem<ZkNode> virtualRoot = new TreeItem<>(zkNode);
        treeItemMap.put(root, virtualRoot);
        zkNodeTreeView.setRoot(virtualRoot);
    }


    private AtomicInteger atomicInteger = new AtomicInteger(0);

    private void initZookeeperListener(CuratorFramework client) {
        curatorlistenerManager = new CuratorlistenerManager(client);
        curatorlistenerManager.start(new TreeCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, TreeCacheEvent event) throws Exception {
                if (event.getType() == TreeCacheEvent.Type.NODE_ADDED) {
                    onNodeAdded(client, event);
                }

                if (event.getType() == TreeCacheEvent.Type.NODE_REMOVED) {
                    onNodeRemoved(event);
                }

                if (event.getType() == TreeCacheEvent.Type.NODE_UPDATED) {
                    onNodeUpdated(event);
                }

                if (event.getType() == TreeCacheEvent.Type.INITIALIZED) {
                    System.err.println("over: " + atomicInteger.get() + ":" + treeItemMap.size());
                    System.err.println("cost time: " + (System.currentTimeMillis() - start) + " mill");
                }
            }
        });
    }

    private void onNodeUpdated(TreeCacheEvent event) {
        final String path = event.getData().getPath();
        final TreeItem<ZkNode> item = treeItemMap.get(path);
        final ZkNode node = item.getValue();
        node.setStat(event.getData().getStat());
        node.setData(new String(event.getData().getData()));
    }

    private void onNodeRemoved(TreeCacheEvent event) {
        final String path = event.getData().getPath();
        final String parent = PathUtils.getParent(path);
        final TreeItem<ZkNode> parentItem = treeItemMap.get(parent);
        final TreeItem<ZkNode> removeItem = treeItemMap.remove(path);
        parentItem.getChildren().remove(removeItem);
    }

    private void onNodeAdded(CuratorFramework client, TreeCacheEvent event) {
        final ChildData eventData = event.getData();
        final String path = eventData.getPath();
        final String name = PathUtils.getLastPath(path);

        atomicInteger.incrementAndGet();
        final ZkNode node = new ZkNode(name, path);
        node.setStat(eventData.getStat());
        node.setData(new String(eventData.getData()));
        final TreeItem<ZkNode> treeItem = new TreeItem<>(node);
        treeItemMap.put(path, treeItem);
        if (path.equals("/")) {
            zkNodeTreeView.setRoot(treeItem);
        } else {
            final String parent = PathUtils.getParent(path);
            final TreeItem<ZkNode> parentItem = treeItemMap.get(parent);
            parentItem.getChildren().add(treeItem);
        }
    }

    private void showStat(Stat stat) {
        this.ctimeLabel.setText(String.valueOf(stat.getCtime()));
        this.mtimeLabel.setText(String.valueOf(stat.getMtime()));

        this.numChildrenLabel.setText(String.valueOf(stat.getNumChildren()));
        this.aclVersionLabel.setText(String.valueOf(stat.getAversion()));
        this.cversionLabel.setText(String.valueOf(stat.getCversion()));
        this.cZxidLabel.setText(String.valueOf(stat.getCzxid()));
        this.mZxidLabel.setText(String.valueOf(stat.getMzxid()));
        this.pZxidLabel.setText(String.valueOf(stat.getPzxid()));
        this.dataLengthLabel.setText(String.valueOf(stat.getDataLength()));
        this.dataVersionLabel.setText(String.valueOf(stat.getVersion()));
        this.ephemeralOwnerLabel.setText(String.valueOf(stat.getEphemeralOwner()));
    }

    private void showData(String data) {
        this.dataTextArea.setText(data);
    }

    @FXML
    private void updateDataAction() {
        try {
            curatorFramework.setData().forPath(this.pathLabel.getText(), this.dataTextArea.getText().getBytes());
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }
}
