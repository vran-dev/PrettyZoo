package cc.cc1234.main.controller;

import cc.cc1234.main.cache.TreeViewCache;
import cc.cc1234.main.history.History;
import cc.cc1234.main.model.ZkNode;
import cc.cc1234.main.model.ZkServer;
import cc.cc1234.main.service.ZkServerService;
import cc.cc1234.main.view.DefaultTreeCell;
import cc.cc1234.main.view.ServerListViewManager;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicReference;

public class TreeNodeViewController {

    private static final Logger log = LoggerFactory.getLogger(TreeNodeViewController.class);

    @FXML
    private TreeView<ZkNode> zkNodeTreeView;

    @FXML
    private ListView<ZkServer> serversListView;

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

    public static final String ROOT_PATH = "/";

    private TreeViewCache<ZkNode> treeViewCache = TreeViewCache.getInstance();

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
    private void updateDataAction() {
        if (activeServer.get() == null) {
            VToast.toastFailure(primaryStage, "Error: connect zookeeper first");
            return;
        }
        final String path = this.pathLabel.getText();
        if (treeViewCache.get(activeServer.get(), path) == null) {
            VToast.toastFailure(primaryStage, "Node not exists");
            return;
        }
        try {
            ZkServerService.getInstance(activeServer.get())
                    .setData(path,
                            this.dataTextArea.getText(),
                            (client, event) -> Platform.runLater(() -> VToast.toastSuccess(primaryStage)));
        } catch (Exception e) {
            VToast.toastFailure(primaryStage, "update data failed");
            log.error("update data error", e);
        }
    }

    @FXML
    private void initialize() {
        initBindListener();
        initServerTableView();
        treeViewCache.setTreeView(zkNodeTreeView);
    }

    private void initBindListener() {
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
                        bindZkNodeProperties(node);
                    }
                }));

    }

    private void bindZkNodeProperties(ZkNode node) {
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

    private void initServerTableView() {
        history = History.createIfAbsent(History.SERVER_HISTORY);
        ServerListViewManager.init(serversListView, this::switchServer, history);
    }

    private void switchServer(ZkServer server) {
        final ZkServerService service = ZkServerService.getInstance(server.getServer());
        try {
            final CuratorFramework client = service.connectIfNecessary();
            zkNodeTreeView.setCellFactory(view -> new DefaultTreeCell(primaryStage, client));
        } catch (InterruptedException e) {
            VToast.toastFailure(primaryStage, "Failed: " + e.getMessage());
            return;
        }

        refreshServerHistory(server.getServer());
        initVirtualRootIfNecessary(server.getServer());
        switchTreeRoot(server.getServer());
        service.syncNodeIfNecessary();
        activeServer.set(server.getServer());
        server.setConnect(true);
    }

    private void initVirtualRootIfNecessary(String server) {
        if (!treeViewCache.hasServer(server)) {
            String path = TreeNodeViewController.ROOT_PATH;
            final ZkNode zkNode = new ZkNode(path, path);
            TreeItem<ZkNode> virtualRoot = new TreeItem<>(zkNode);
            zkNode.setData("");
            zkNode.setStat(new Stat());
            treeViewCache.add(server, path, virtualRoot);
        }
    }

    private void refreshServerHistory(String server) {
        Platform.runLater(() -> {
            final String value = history.get(server, "0");
            history.save(server, String.valueOf(Integer.parseInt(value) + 1));
            history.store();
        });
    }

    private void switchTreeRoot(String server) {
        final TreeItem<ZkNode> root = treeViewCache.get(server, ROOT_PATH);
        zkNodeTreeView.setRoot(root);
        // fix binding error
//        bindZkNodeProperties(root.getValue());
    }

}
