package cc.cc1234.main.controller;

import cc.cc1234.main.cache.TreeItemCache;
import cc.cc1234.main.cache.ZkClientCache;
import cc.cc1234.main.curator.NodeEventHandler;
import cc.cc1234.main.history.History;
import cc.cc1234.main.manager.CuratorlistenerManager;
import cc.cc1234.main.model.ZkNode;
import cc.cc1234.main.model.ZkServer;
import cc.cc1234.main.view.DefaultTreeCell;
import cc.cc1234.main.view.ServerTableView;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.data.Stat;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;

public class NodeTreeViewController {

    @FXML
    private TreeView<ZkNode> zkNodeTreeView;

    @FXML
    private TableView<ZkServer> serversTableView;

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

    public static final String ROOT_PATH = "/";

    private TreeItemCache serverTreeCache = TreeItemCache.getInstance();

    private ZkClientCache serverClientCache = ZkClientCache.getInstance();

    /**
     * [server:manager]
     */
    private Map<String, CuratorlistenerManager> curatorlistenerManagerCache = new ConcurrentHashMap<>();

    /**
     * current selected zk server
     */
    private AtomicReference<String> activeServer = new AtomicReference<>();

    private Stage primaryStage;

    private History history;


    public void setPrimaryStage(Stage primary) {
        this.primaryStage = primary;
    }

    @FXML
    private void initialize() {
        initServerTableView();
        initZkNodeTreeView();
    }

    private void initZkNodeTreeView() {
        zkNodeTreeView.getSelectionModel()
                .selectedItemProperty()
                .addListener(((observable, oldValue, newValue) -> {
                    // skip no selected item
                    if (newValue != null) {
                        if (oldValue != null) {
                            this.dataTextArea.textProperty().unbindBidirectional(oldValue.getValue().dataProperty());
                        }
                        // properties bind
                        final ZkNode node = newValue.getValue();
                        this.pathLabel.textProperty().bind(node.pathProperty());
                        this.cZxidLabel.textProperty().bind(node.czxidProperty().asString());
                        this.mZxidLabel.textProperty().bind(node.mzxidProperty().asString());
                        this.ctimeLabel.textProperty().bind(node.ctimeProperty().asString());
                        this.mtimeLabel.textProperty().bind(node.mtimeProperty().asString());
                        this.dataVersionLabel.textProperty().bind(node.versionProperty().asString());
                        this.cversionLabel.textProperty().bind(node.cversionProperty().asString());
                        this.aclVersionLabel.textProperty().bind(node.aversionProperty().asString());
                        this.ephemeralOwnerLabel.textProperty().bind(node.ephemeralOwnerProperty().asString());
                        this.dataLengthLabel.textProperty().bind(node.dataLengthProperty().asString());
                        this.numChildrenLabel.textProperty().bind(node.numChildrenProperty().asString());
                        this.pZxidLabel.textProperty().bind(node.pzxidProperty().asString());
                        this.dataTextArea.textProperty().bindBidirectional(node.dataProperty());
                    }
                }));

    }

    private void initServerTableView() {
        history = History.createIfAbsent(History.SERVER_HISTORY);
        ServerTableView.init(serversTableView,
                (observable, oldValue, newValue) -> {
                    refreshClient(newValue);
                    // TODO  use async to avoiding UI blocking
                    switchTreeTableRoot(newValue);
                },
                history);

    }

    private void refreshClient(ZkServer newValue) {
        if (!serverClientCache.contains(newValue.getServer())) {
            final CuratorFramework client = doConnect(newValue);
            serverClientCache.put(newValue.getServer(), client);
            newValue.setConnect(true);
        }
        zkNodeTreeView.setCellFactory(view -> new DefaultTreeCell(primaryStage,
                serverClientCache.get(newValue.getServer())));
    }

    private void switchTreeTableRoot(ZkServer newValue) {
        if (!serverTreeCache.hasServer(newValue.getServer())) {
            initVirtualRoot(newValue.getServer(), ROOT_PATH);
            final CuratorFramework client = serverClientCache.get(newValue.getServer());
            syncTreeNode(newValue.getServer(), client);
        }

        activeServer.set(newValue.getServer());
        zkNodeTreeView.setRoot(serverTreeCache.getItemByPath(newValue.getServer(), ROOT_PATH));
    }

    private void initVirtualRoot(String server, String root) {
        final ZkNode zkNode = new ZkNode(root, root);
        TreeItem<ZkNode> virtualRoot = new TreeItem<>(zkNode);
        zkNode.setData("");
        zkNode.setStat(new Stat());
        serverTreeCache.cacheItemByPath(server, root, virtualRoot);
        zkNodeTreeView.setRoot(virtualRoot);
    }

    private CuratorFramework doConnect(ZkServer server) {
        final RetryOneTime retryPolicy = new RetryOneTime(3000);
        final CuratorFramework client = CuratorFrameworkFactory.newClient(server.getServer(), retryPolicy);
        try {
            client.start();
            // TODO @vran use connection listener
            client.blockUntilConnected();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }

        Executors.newSingleThreadExecutor().execute(() -> {
            final String value = history.get(server.getServer(), "0");
            history.save(server.getServer(), String.valueOf(Integer.parseInt(value) + 1));
            history.store();
        });
        return client;
    }

    void syncTreeNode(String server, CuratorFramework client) {
        startSyncTreeNodeListener(server, client);
    }

    private void startSyncTreeNodeListener(String server, CuratorFramework client) {
        CuratorlistenerManager manager = curatorlistenerManagerCache.getOrDefault(server,
                new CuratorlistenerManager(client));
        manager.start(new TreeCacheListener() {

            private NodeEventHandler eventHandler = new NodeEventHandler(server);

            @Override
            public void childEvent(CuratorFramework client, TreeCacheEvent event) {
                if (event.getType() == TreeCacheEvent.Type.NODE_ADDED) {
                    Platform.runLater(() -> eventHandler.onNodeAdded(zkNodeTreeView, event));
                }

                if (event.getType() == TreeCacheEvent.Type.NODE_REMOVED) {
                    Platform.runLater(() -> eventHandler.onNodeRemoved(zkNodeTreeView, event));
                }

                if (event.getType() == TreeCacheEvent.Type.NODE_UPDATED) {
                    Platform.runLater(() -> eventHandler.onNodeUpdated(event));
                }

                if (event.getType() == TreeCacheEvent.Type.INITIALIZED) {
                    Platform.runLater(() -> {
                        nodeSyncProgressBar.setVisible(false);
                    });
                }

                // ignore other event now
            }
        });
    }


    @FXML
    private void updateDataAction() {
        if (activeServer.get() == null) {
            VToast.toastFailure(primaryStage, "Error: connect zookeeper first");
            return;
        }
        try {
            serverClientCache.get(activeServer.get())
                    .setData()
                    .inBackground((client, event) -> Platform.runLater(() -> VToast.toastSuccess(primaryStage)))
                    .forPath(this.pathLabel.getText(), this.dataTextArea.getText().getBytes());
        } catch (Exception e) {
            VToast.toastFailure(primaryStage);
            throw new IllegalStateException(e);
        }
    }

}
