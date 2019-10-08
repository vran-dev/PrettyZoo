package cc.cc1234.main.controller;

import cc.cc1234.main.cache.TreeItemCache;
import cc.cc1234.main.cell.ZkNodeTreeCell;
import cc.cc1234.main.cell.ZkServerListCell;
import cc.cc1234.main.context.ActiveServerContext;
import cc.cc1234.main.listener.JfxListenerManager;
import cc.cc1234.main.model.ZkNode;
import cc.cc1234.main.util.FXMLs;
import cc.cc1234.main.util.Transitions;
import cc.cc1234.main.vo.PrettyZooConfigVO;
import cc.cc1234.main.vo.ZkNodeOperationVO;
import cc.cc1234.main.vo.ZkNodeVO;
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

    private PrettyZooConfigVO prettyZooConfigVO = new PrettyZooConfigVO();

    private ZkNodeOperationVO zkNodeOperationVO = new ZkNodeOperationVO();

    private ZkNodeVO zkNodeVO = new ZkNodeVO();

    private AddServerViewController addServerViewController;

    private AddNodeViewController addNodeViewController;

    @FXML
    private void onAddServerAction(ActionEvent event) {
        serverViewMenuItems.setVisible(false);
        addServerViewController.show();
    }

    @FXML
    private void onRemoveServerAction() {
        serverViewMenuItems.setVisible(false);
        final ZkServerConfigVO removeItem = serverListView.getSelectionModel().getSelectedItem();
        if (removeItem == null) {
            VToast.toastFailure("no server selected");
            return;
        }
        prettyZooConfigVO.remove(removeItem.getHost());
        zkNodeTreeView.setRoot(null);
    }

    @FXML
    private void updateDataAction(ActionEvent actionEvent) {
        if (!ActiveServerContext.exists()) {
            VToast.toastFailure("Error: connect zookeeper first");
            return;
        }
        if (!zkNodeOperationVO.nodeExists()) {
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

    @FXML
    private void initialize() {
        zkNodePropertyBind();
        zkOperationPropertyBind();
        registerTreeViewListener();
        initServerListView();
        recursiveModeCheckBox.selectedProperty().addListener(JfxListenerManager.getRecursiveModeChangeListener(prettyZooLabel, serverViewMenuItems));
        addServerViewController = FXMLs.getController("fxml/AddServerView.fxml");
        addNodeViewController = FXMLs.getController("fxml/AddNodeView.fxml");
    }

    private void zkOperationPropertyBind() {
        final StringBinding binding = Bindings.createStringBinding(() -> {
            final TreeItem<ZkNode> selectedItem = zkNodeTreeView.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                return "";
            }
            return selectedItem.getValue().getPath();
        }, zkNodeTreeView.getSelectionModel().selectedItemProperty());
        zkNodeOperationVO.absolutePathProperty().bind(binding);
        zkNodeOperationVO.dataProperty().bind(dataTextArea.textProperty());
    }


    private void zkNodePropertyBind() {
        this.pathLabel.textProperty().bind(zkNodeVO.pathProperty());
        this.cZxidLabel.textProperty().bind(zkNodeVO.czxidProperty().asString());
        this.mZxidLabel.textProperty().bind(zkNodeVO.mzxidProperty().asString());
        this.ctimeLabel.textProperty().bind(zkNodeVO.ctimeProperty().asString());
        this.mtimeLabel.textProperty().bind(zkNodeVO.mtimeProperty().asString());
        this.dataVersionLabel.textProperty().bind(zkNodeVO.versionProperty().asString());
        this.cversionLabel.textProperty().bind(zkNodeVO.cversionProperty().asString());
        this.aclVersionLabel.textProperty().bind(zkNodeVO.aversionProperty().asString());
        this.ephemeralOwnerLabel.textProperty().bind(zkNodeVO.ephemeralOwnerProperty().asString());
        this.dataLengthLabel.textProperty().bind(zkNodeVO.dataLengthProperty().asString());
        this.numChildrenLabel.textProperty().bind(zkNodeVO.numChildrenProperty().asString());
        this.pZxidLabel.textProperty().bind(zkNodeVO.pzxidProperty().asString());
        this.dataTextArea.textProperty().bindBidirectional(zkNodeVO.dataProperty());
    }

    private void registerTreeViewListener() {
        zkNodeTreeView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    if (newValue != null) {
                        zkNodeVO.change(newValue.getValue());
                    }
                });
    }

    private void initServerListView() {
        serverListView.itemsProperty().set(prettyZooConfigVO.getServers());
        serverListView.setCellFactory(cellCallback -> new ZkServerListCell(this::switchServer));
        serverListMenu.setOnMouseClicked(event -> serverViewMenuItems.setVisible(!serverViewMenuItems.isVisible()));
    }

    private void switchServer(ZkServerConfigVO server) {
        try {
            log.debug("begin to switch server to {}", server.getHost());
            server.connectIfNecessary();
        } catch (InterruptedException e) {
            log.debug("switch server {} failed: {}", server.getHost(), e.getMessage());
            VToast.toastFailure("Failed: " + e.getMessage());
            return;
        }
        zkNodeTreeView.setCellFactory(view -> new ZkNodeTreeCell());
        final TreeItem<ZkNode> root = getOrCreateTreeRoot(server);
        zkNodeTreeView.setRoot(root);
        zkNodeVO.change(root.getValue());
        ActiveServerContext.set(server.getHost());
        server.syncNodeIfNecessary();

        log.debug("switch server {} success", server.getHost());
    }

    private TreeItem<ZkNode> getOrCreateTreeRoot(ZkServerConfigVO server) {
        String host = server.getHost();
        final String root = "/";
        final TreeItemCache treeItemCache = TreeItemCache.getInstance();
        if (!treeItemCache.hasNode(host, root)) {
            final ZkNode zkNode = new ZkNode(root, root);
            zkNode.setStat(new Stat());
            treeItemCache.add(host, root, new TreeItem<>(zkNode));
        }
        return treeItemCache.get(host, root);
    }
}
