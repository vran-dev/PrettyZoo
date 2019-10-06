package cc.cc1234.main.controller;

import cc.cc1234.main.cache.ActiveServerContext;
import cc.cc1234.main.cache.PrettyZooConfigContext;
import cc.cc1234.main.cache.RecursiveModeContext;
import cc.cc1234.main.cache.TreeViewCache;
import cc.cc1234.main.listener.JfxListenerManager;
import cc.cc1234.main.model.PrettyZooConfig;
import cc.cc1234.main.model.ZkNode;
import cc.cc1234.main.model.ZkServer;
import cc.cc1234.main.service.ZkServerService;
import cc.cc1234.main.util.Conditions;
import cc.cc1234.main.util.FXMLs;
import cc.cc1234.main.util.Transitions;
import cc.cc1234.main.view.ZkNodeTreeCell;
import cc.cc1234.main.view.ZkServerListCell;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Window;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeNodeViewController {

    private static final Logger log = LoggerFactory.getLogger(TreeNodeViewController.class);

    @FXML
    private TreeView<ZkNode> zkNodeTreeView;

    @FXML
    private ListView<ZkServer> serverListView;

    @FXML
    private Button serverListMenu;

    @FXML
    private AnchorPane serverViewMenuItems;

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
    private Label prettyZooLabel;

    @FXML
    private CheckBox recursiveModeCheckBox;

    public static final String ROOT_PATH = "/";

    private TreeViewCache treeViewCache = TreeViewCache.getInstance();

    private PrettyZooConfig prettyZooConfig = new PrettyZooConfig();

    private AddServerViewController addServerViewController;

    private AddNodeViewController addNodeViewController;

    @FXML
    private void initialize() {
        PrettyZooConfigContext.set(prettyZooConfig);
        initZkNodeTreeView();
        initServerListView();
        recursiveModeCheckBox.selectedProperty().addListener(JfxListenerManager.getRecursiveModeChangeListener(prettyZooLabel, serverViewMenuItems));
        addServerViewController = FXMLs.getController("fxml/AddServerView.fxml");
        addNodeViewController = FXMLs.getController("fxml/AddNodeView.fxml");
    }

    @FXML
    private void onAddServerAction(ActionEvent event) {
        serverViewMenuItems.setVisible(false);
        Window parent = ((Node) event.getSource()).getScene().getWindow();
        addServerViewController.show(parent);
    }

    @FXML
    private void onRemoveServerAction(ActionEvent event) {
        serverViewMenuItems.setVisible(false);
        Window parent = ((Node) event.getSource()).getParent().getScene().getWindow();
        final ZkServer removeItem = serverListView.getSelectionModel().getSelectedItem();
        Conditions.on(() -> removeItem == null)
                .thenDo(() -> VToast.toastFailure(parent, "no server selected"))
                .elseDo(() -> {
                    prettyZooConfig.remove(removeItem.getServer());
                    zkNodeTreeView.setRoot(null);
                    ActiveServerContext.invalidate();
                    ZkServerService.getOrCreate(removeItem.getServer()).closeALl();
                });
    }

    @FXML
    private void updateDataAction(ActionEvent actionEvent) {
        Window parent = ((Node) actionEvent.getSource()).getParent().getScene().getWindow();
        if (!ActiveServerContext.exists()) {
            VToast.toastFailure(parent, "Error: connect zookeeper first");
            return;
        }
        final String path = this.pathLabel.getText();
        if (treeViewCache.get(ActiveServerContext.get(), path) == null) {
            VToast.toastFailure(parent, "Node not exists");
            return;
        }
        Button button = (Button) actionEvent.getSource();
        Transitions.rotate(button).play();
        ZkServerService.getActive().setData(path, this.dataTextArea.getText(),
                (client, event) -> Platform.runLater(() -> VToast.toastSuccess(parent)),
                e -> VToast.toastFailure(parent, e.getMessage()));

    }

    @FXML
    private void onNodeDeleteAction(ActionEvent actionEvent) {
        Window parent = ((Node) actionEvent.getSource()).getParent().getScene().getWindow();
        Button button = (Button) actionEvent.getSource();
        Transitions.rotate(button, 360).play();
        final TreeItem<ZkNode> selectedItem = zkNodeTreeView.getSelectionModel().getSelectedItem();
        Conditions.on(() -> selectedItem == null)
                .thenDo(() -> VToast.toastFailure(parent, "select item first"))
                .elseDo(() -> {
                    final String path = selectedItem.getValue().getPath();
                    ZkServerService.getActive()
                            .delete(path, RecursiveModeContext.get(), e -> VToast.toastFailure(parent, e.getMessage()));
                    zkNodeTreeView.getSelectionModel().clearSelection();
                });
    }

    @FXML
    private void onNodeAddAction(ActionEvent actionEvent) {
        Window parent = ((Node) actionEvent.getSource()).getScene().getWindow();
        Button button = (Button) actionEvent.getSource();
        Transitions.rotate(button, 360).play();
        final TreeItem<ZkNode> selectedItem = zkNodeTreeView.getSelectionModel().getSelectedItem();
        Conditions.on(() -> selectedItem == null)
                .thenDo(() -> VToast.toastFailure(parent, "select item first"))
                .elseDo(() -> {
                    final CuratorFramework client = ZkServerService.getActive().getClient();
                    addNodeViewController.show(selectedItem.getValue().getPath(), client);
                });
    }

    private void initZkNodeTreeView() {
        zkNodeTreeView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    // skip no selected item
                    if (newValue != null) {
                        if (oldValue != null) {
                            this.dataTextArea.textProperty().unbindBidirectional(oldValue.getValue().dataProperty());
                        }
                        // properties bind
                        final ZkNode node = newValue.getValue();
                        bindZkNodeProperties(node);
                    }
                });
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

    private void initServerListView() {
        serverListView.itemsProperty().set(prettyZooConfig.getServers());
        serverListView.setCellFactory(cellCallback -> new ZkServerListCell(this::switchServer));
        serverListMenu.setOnMouseClicked(event -> serverViewMenuItems.setVisible(!serverViewMenuItems.isVisible()));
    }

    private void switchServer(ZkServer server) {
        final ZkServerService service = ZkServerService.getOrCreate(server.getServer());
        Window parent = zkNodeTreeView.getParent().getScene().getWindow();
        try {
            service.connectIfNecessary();
        } catch (InterruptedException e) {
            VToast.toastFailure(parent, "Failed: " + e.getMessage());
            return;
        }

        zkNodeTreeView.setCellFactory(view -> new ZkNodeTreeCell());
        initVirtualRootIfNecessary(server.getServer());
        switchTreeRoot(server.getServer());
        ActiveServerContext.set(server.getServer());
        service.syncNodeIfNecessary();
        server.setConnect(true);
    }

    private void initVirtualRootIfNecessary(String server) {
        if (!treeViewCache.hasServer(server)) {
            String path = TreeNodeViewController.ROOT_PATH;
            final ZkNode zkNode = new ZkNode(path, path);
            zkNode.setStat(new Stat());
            treeViewCache.add(server, path, new TreeItem<>(zkNode));
        }
    }

    private void switchTreeRoot(String server) {
        final TreeItem<ZkNode> root = treeViewCache.get(server, ROOT_PATH);
        zkNodeTreeView.setRoot(root);
    }

}
