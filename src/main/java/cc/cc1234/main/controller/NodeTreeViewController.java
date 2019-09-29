package cc.cc1234.main.controller;

import cc.cc1234.main.manager.CuratorlistenerManager;
import cc.cc1234.main.model.ZkNode;
import cc.cc1234.main.util.PathUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;
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

    @FXML
    private ProgressBar nodeSyncProgressBar;

    private CuratorlistenerManager curatorlistenerManager;

    private Map<String, TreeItem<ZkNode>> treeItemMap = new ConcurrentHashMap<>();

    private CuratorFramework curatorFramework;

    private Stage primaryStage;

    private long start;

    /**
     * loaded node num
     */
    private AtomicInteger loadedNodeNum = new AtomicInteger(0);

    /**
     * total node num
     */
    private AtomicInteger totalNodeNum = new AtomicInteger(0);

    public static void showNodeTreeView(CuratorFramework client, Stage primary) throws IOException {
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ConnectViewController.class.getResource("NodeTreeView.fxml"));
        final AnchorPane anchorPane = loader.load();
        primary.getScene().setRoot(anchorPane);
        primary.sizeToScene();
        NodeTreeViewController controller = loader.getController();
        controller.setPrimaryStage(primary);
        controller.initTreeNode(client);
    }

    private void setPrimaryStage(Stage primary) {
        this.primaryStage = primary;
    }

    @FXML
    private void initialize() {
        // init tree item select event listener
        initTreeItemListener();
        // create virtual root item
        initVirtualRoot("/");
        zkNodeTreeView.setCellFactory(view -> new TreeCellImpl());
    }

    void initTreeNode(CuratorFramework client) {
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
        zkNode.setData("");
        zkNode.setStat(new Stat());
        treeItemMap.put(root, virtualRoot);
        zkNodeTreeView.setRoot(virtualRoot);
    }

    private void initZookeeperListener(CuratorFramework client) {
        curatorlistenerManager = new CuratorlistenerManager(client);
        curatorlistenerManager.start(new TreeCacheListener() {
            @Override
            public void childEvent(CuratorFramework client, TreeCacheEvent event) {
                if (event.getType() == TreeCacheEvent.Type.NODE_ADDED) {
                    Platform.runLater(() -> onNodeAdded(client, event));
                }

                if (event.getType() == TreeCacheEvent.Type.NODE_REMOVED) {
                    Platform.runLater(() -> onNodeRemoved(event));
                }

                if (event.getType() == TreeCacheEvent.Type.NODE_UPDATED) {
                    Platform.runLater(() -> onNodeUpdated(event));
                }

                if (event.getType() == TreeCacheEvent.Type.INITIALIZED) {
                    Platform.runLater(() -> {
                        nodeSyncProgressBar.setVisible(false);
                        System.err.println("cached node numbers: " + loadedNodeNum.get() + ":" + treeItemMap.size());
                        System.err.println("cost time: " + (System.currentTimeMillis() - start) + " mill");
                    });
                }

                // ignore other event now
            }
        });
    }

    private void onNodeUpdated(TreeCacheEvent event) {
        final String path = event.getData().getPath();
        final TreeItem<ZkNode> item = treeItemMap.get(path);
        final ZkNode node = item.getValue();
        node.setStat(event.getData().getStat());
        node.setData(new String(event.getData().getData()));
        if (zkNodeTreeView.getSelectionModel().getSelectedItem() == item) {
            showStat(node.getStat());
        }
    }

    private void onNodeRemoved(TreeCacheEvent event) {
        final String path = event.getData().getPath();
        final String parent = PathUtils.getParent(path);
        final TreeItem<ZkNode> parentItem = treeItemMap.get(parent);
        final TreeItem<ZkNode> removeItem = treeItemMap.remove(path);

        // note: must be clear selection before remove
        final TreeItem<ZkNode> selectedItem = zkNodeTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem == removeItem) {
            zkNodeTreeView.getSelectionModel().clearSelection();
        }
        parentItem.getChildren().remove(removeItem);
    }

    private void onNodeAdded(CuratorFramework client, TreeCacheEvent event) {
        final ChildData eventData = event.getData();
        final String path = eventData.getPath();
        final String name = PathUtils.getLastPath(path);

        loadedNodeNum.incrementAndGet();
        final ZkNode node = new ZkNode(name, path);
        node.setStat(eventData.getStat());
        node.setData(new String(eventData.getData()));
        final TreeItem<ZkNode> treeItem = new TreeItem<>(node);
        treeItemMap.put(path, treeItem);

        totalNodeNum.addAndGet(eventData.getStat().getNumChildren());
        if (path.equals("/")) {
            treeItem.setExpanded(true);
            zkNodeTreeView.setRoot(treeItem);
        } else {
            final String parent = PathUtils.getParent(path);
            final TreeItem<ZkNode> parentItem = treeItemMap.get(parent);
            parentItem.getChildren().add(treeItem);
        }

        // calc progress
        final double p = loadedNodeNum.get() / (totalNodeNum.get() + 0.0);
        nodeSyncProgressBar.setProgress(p);
    }

    private void showStat(Stat stat) {
        if (stat == null) {
            setDefaultValue();
        } else {
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
    }

    private void setDefaultValue() {
        this.ctimeLabel.setText("");
        this.mtimeLabel.setText("");
        this.numChildrenLabel.setText("");
        this.aclVersionLabel.setText("");
        this.cversionLabel.setText("");
        this.cZxidLabel.setText("");
        this.mZxidLabel.setText("");
        this.pZxidLabel.setText("");
        this.dataLengthLabel.setText("");
        this.dataVersionLabel.setText("");
        this.ephemeralOwnerLabel.setText("");
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

    final class TreeCellImpl extends TreeCell<ZkNode> {

        private ContextMenu operationMenus;

        private final MenuItem deleteMenu;

        private final MenuItem addMenu;


        TreeCellImpl() {
            deleteMenu = new MenuItem("Delete");
            deleteMenu.setOnAction(event -> {
                try {
                    curatorFramework.delete().forPath(getTreeItem().getValue().getPath());
                } catch (Exception e) {
                    throw new IllegalStateException(e);
                }
            });

            // TODO @vran add action
            addMenu = new MenuItem("Add");
            addMenu.setOnAction(event -> {
                try {
                    AddNodeViewController.initController(getTreeItem().getValue().getPath(), curatorFramework);
                } catch (IOException e) {
                    throw new IllegalStateException(e);
                }
            });
            operationMenus = new ContextMenu();
        }

        @Override
        protected void updateItem(ZkNode item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setText(null);
                setGraphic(null);
            } else {
                setText(item.getName());
                final TreeItem<ZkNode> treeItem = getTreeItem();
                setGraphic(treeItem.getGraphic());
                setContextMenu(operationMenus);

                if (item.getStat() != null && item.getStat().getEphemeralOwner() == 0) {
                    addIfAbsent(addMenu);
                } else {
                    operationMenus.getItems().remove(addMenu);
                }

                // add delete menu for leaf node
                if (treeItem.isLeaf() && treeItem.getParent() != null) {
                    addIfAbsent(deleteMenu);
                } else {
                    operationMenus.getItems().remove(deleteMenu);
                }

            }
        }

        private void addIfAbsent(MenuItem menuItem) {
            if (!operationMenus.getItems().contains(menuItem)) {
                operationMenus.getItems().add(menuItem);
            }
        }
    }
}
