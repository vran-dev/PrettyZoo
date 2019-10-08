package cc.cc1234.main.controller;

import cc.cc1234.main.cache.ActiveServerContext;
import cc.cc1234.main.cache.PrimaryStageContext;
import cc.cc1234.main.cache.TreeViewCache;
import cc.cc1234.main.cell.ZkNodeTreeCell;
import cc.cc1234.main.cell.ZkServerListCell;
import cc.cc1234.main.context.ApplicationContext;
import cc.cc1234.main.listener.JfxListenerManager;
import cc.cc1234.main.model.ZkNode;
import cc.cc1234.main.model.ZkServerConfig;
import cc.cc1234.main.service.ZkNodeService;
import cc.cc1234.main.util.Conditions;
import cc.cc1234.main.util.FXMLs;
import cc.cc1234.main.util.Transitions;
import cc.cc1234.main.vo.PrettyZooConfigVO;
import cc.cc1234.main.vo.ZkNodeOperationVO;
import cc.cc1234.main.vo.ZkServerConfigVO;
import com.google.common.base.Strings;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TreeNodeViewController {

    private static final Logger log = LoggerFactory.getLogger(TreeNodeViewController.class);

    @FXML
    private TreeView<ZkNode> zkNodeTreeView;

    @FXML
    private ListView<ZkServerConfigVO> serverListView;

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

    private PrettyZooConfigVO serverListVO = new PrettyZooConfigVO();

    private ZkNodeOperationVO zkNodeOperationVO = new ZkNodeOperationVO();

    private AddServerViewController addServerViewController;

    private AddNodeViewController addNodeViewController;

    @FXML
    private void initialize() {
        initZkNodeTreeView();
        initServerListView();
        recursiveModeCheckBox.selectedProperty().addListener(JfxListenerManager.getRecursiveModeChangeListener(prettyZooLabel, serverViewMenuItems));
        addServerViewController = FXMLs.getController("fxml/AddServerView.fxml");
        addNodeViewController = FXMLs.getController("fxml/AddNodeView.fxml");
    }

    @FXML
    private void onAddServerAction(ActionEvent event) {
        serverViewMenuItems.setVisible(false);
        addServerViewController.show(PrimaryStageContext.get());
    }

    @FXML
    private void onRemoveServerAction() {
        serverViewMenuItems.setVisible(false);
        final ZkServerConfigVO removeItem = serverListView.getSelectionModel().getSelectedItem();
        Conditions.on(() -> removeItem == null)
                .thenDo(() -> VToast.toastFailure("no server selected"))
                .elseDo(() -> {
                    serverListVO.remove(removeItem.getHost());
                    zkNodeTreeView.setRoot(null);
                });
    }

    @FXML
    private void updateDataAction(ActionEvent actionEvent) {
        if (!ActiveServerContext.exists()) {
            VToast.toastFailure("Error: connect zookeeper first");
            return;
        }

        if (treeViewCache.get(ActiveServerContext.get(), zkNodeOperationVO.getAbsolutePath()) == null) {
            VToast.toastFailure("Node not exists");
            return;
        }
        Transitions.rotate((Button) actionEvent.getSource()).play();
        zkNodeOperationVO.updateData(e -> VToast.toastFailure(e.getMessage()));
        VToast.toastSuccess("update success");
    }

    @FXML
    private void onNodeDeleteAction(ActionEvent actionEvent) {
        Button button = (Button) actionEvent.getSource();
        Transitions.scaleAndRotate(button);
        if (Strings.isNullOrEmpty(zkNodeOperationVO.getAbsolutePath())) {
            VToast.toastFailure("select node first");
            return;
        }
        zkNodeOperationVO.onDelete(e -> VToast.toastSuccess(e.getMessage()));
        zkNodeTreeView.getSelectionModel().clearSelection();
    }

    @FXML
    private void onNodeAddAction(ActionEvent actionEvent) {
        Button button = (Button) actionEvent.getSource();
        Transitions.scaleAndRotate(button);
        if (Strings.isNullOrEmpty(zkNodeOperationVO.getAbsolutePath())) {
            VToast.toastFailure("select node first");
            return;
        }
        addNodeViewController.show(zkNodeOperationVO.getAbsolutePath());
    }

    private void initZkNodeTreeView() {
        final StringBinding binding = Bindings.createStringBinding(() -> {
            final TreeItem<ZkNode> selectedItem = zkNodeTreeView.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                return "";
            }
            return selectedItem.getValue().getPath();
        }, zkNodeTreeView.getSelectionModel().selectedItemProperty());
        zkNodeOperationVO.absolutePathProperty().bind(binding);
        zkNodeOperationVO.dataProperty().bind(dataTextArea.textProperty());
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
        serverListView.itemsProperty().set(serverListVO.getServers());
        serverListView.setCellFactory(cellCallback -> new ZkServerListCell(this::switchServer));
        serverListMenu.setOnMouseClicked(event -> serverViewMenuItems.setVisible(!serverViewMenuItems.isVisible()));
    }

    private void switchServer(ZkServerConfigVO server) {
        final String host = server.getHost();
        final ZkNodeService service = ApplicationContext.get().getBean(ZkNodeService.class);
        try {
            final ZkServerConfig config = new ZkServerConfig();
            config.setHost(host);
            service.connectIfNecessary(config);
        } catch (InterruptedException e) {
            VToast.toastFailure("Failed: " + e.getMessage());
            return;
        }

        zkNodeTreeView.setCellFactory(view -> new ZkNodeTreeCell());
        initVirtualRootIfNecessary(host);
        switchTreeRoot(host);
        ActiveServerContext.set(host);
        service.syncIfNecessary(host);
        server.connectSuccess();
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
