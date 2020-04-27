package cc.cc1234.app.controller;

import cc.cc1234.app.cache.TreeItemCache;
import cc.cc1234.app.cell.ZkNodeTreeCell;
import cc.cc1234.app.cell.ZkServerListCell;
import cc.cc1234.app.context.ActiveServerContext;
import cc.cc1234.app.context.PrimaryStageContext;
import cc.cc1234.app.listener.DefaultTreeNodeListener;
import cc.cc1234.app.listener.JfxListenerManager;
import cc.cc1234.app.util.FXMLs;
import cc.cc1234.app.vo.*;
import cc.cc1234.facade.PrettyZooFacade;
import cc.cc1234.spi.listener.NodeEvent;
import cc.cc1234.spi.listener.ZookeeperNodeListener;
import cc.cc1234.spi.node.ZkNode;
import com.google.common.base.Strings;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.nio.file.Files;
import java.util.List;
import java.util.Objects;

public class TreeNodeViewController {

    private static final Logger log = LoggerFactory.getLogger(TreeNodeViewController.class);

    @FXML
    private TreeView<ZkNode> zkNodeTreeView;

    @FXML
    private ListView<ZkServerConfigVO> serverListView;

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

    @FXML
    private Button serverDeleteButton;

    @FXML
    private Button serverAddButton;

    @FXML
    private Button exportButton;

    @FXML
    private Button importButton;

    @FXML
    private TextField searchTextField;

    @FXML
    private ListView<ZkNodeSearchResult> searchResultList;

    public static final String ROOT_PATH = "/";

    private PrettyZooConfigVO prettyZooConfigVO = new PrettyZooConfigVO();

    private ZkNodeOperationVO zkNodeOperationVO = new ZkNodeOperationVO();

    private ZkNodeVO zkNodeVO = new ZkNodeVO();

    private AddServerViewController addServerViewController;

    private AddNodeViewController addNodeViewController;

    private PrettyZooFacade prettyZooFacade = new PrettyZooFacade();

    @FXML
    private void onAddServerAction(ActionEvent event) {
        addServerViewController.show();
    }

    @FXML
    private void onRemoveServerAction() {
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
        Transitions.scale(button, Duration.millis(400d), null).play();
        if (Strings.isNullOrEmpty(zkNodeOperationVO.getAbsolutePath())) {
            VToast.toastFailure("select node first");
            return;
        }
        zkNodeOperationVO.onDelete(e -> VToast.toastFailure(e.getMessage()));
        zkNodeTreeView.getSelectionModel().clearSelection();
    }

    @FXML
    private void onNodeAddAction(ActionEvent actionEvent) {
        Button button = (Button) actionEvent.getSource();
        Transitions.scale(button, Duration.millis(400d), null).play();
        if (Strings.isNullOrEmpty(zkNodeOperationVO.getAbsolutePath())) {
            VToast.toastFailure("select node first");
            return;
        }
        addNodeViewController.show(zkNodeOperationVO.getAbsolutePath());
    }

    @FXML
    private void onExportAction(ActionEvent actionEvent) {
        var button = (Button) actionEvent.getSource();
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Choose your target directory");
        fileChooser.setInitialFileName("prettyZoo-config");
        var file = fileChooser.showSaveDialog(PrimaryStageContext.get());
        Platform.runLater(() -> prettyZooConfigVO.export(file));
    }

    @FXML
    private void onImportAction(ActionEvent actionEvent) {
        var button = (Button) actionEvent.getSource();
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Choose config file");
        File configFile = fileChooser.showOpenDialog(PrimaryStageContext.get());
        Platform.runLater(() -> prettyZooConfigVO.importConfig(configFile));
    }


    @FXML
    private void initialize() {
        initSearchResultList();
        initSearchTextField();
        zkNodePropertyBind();
        zkOperationPropertyBind();
        registerTreeViewListener();
        initServerListView();
        recursiveModeCheckBox.setTooltip(new Tooltip("开启递归操作模式"));
        serverAddButton.setTooltip(new Tooltip("添加 zookeeper server"));
        serverDeleteButton.setTooltip(new Tooltip("删除选定的 zookeeper server"));
        exportButton.setTooltip(new Tooltip("导出配置文件"));
        importButton.setTooltip(new Tooltip("导入配置文件"));


        final ZookeeperNodeListener zookeeperNodeListener = new ZookeeperNodeListener() {

            @Override
            public void onNodeUpdate(NodeEvent event) {
                if (event.getNode().getPath().equalsIgnoreCase(zkNodeVO.getPath())
                        && Objects.equals(event.getServer(), ActiveServerContext.get())) {
                    Platform.runLater(() -> zkNodeVO.change(event.getNode()));
                }
            }
        };
        prettyZooFacade.registerNodeListener(zookeeperNodeListener);
        prettyZooFacade.registerNodeListener(new DefaultTreeNodeListener());
        recursiveModeCheckBox.selectedProperty().addListener(JfxListenerManager.getRecursiveModeChangeListener(prettyZooLabel));
        addServerViewController = FXMLs.getController("fxml/AddServerView.fxml");
        addNodeViewController = FXMLs.getController("fxml/AddNodeView.fxml");
    }

    private void initSearchTextField() {
        zkNodeOperationVO.searchNameProperty().bind(searchTextField.textProperty());
        searchTextField.textProperty().addListener((o, old, cur) -> {
            searchResultList.getItems().clear();
            final List<ZkNodeSearchResult> items = zkNodeOperationVO.onSearch();
            if (!items.isEmpty()) {
                searchResultList.getItems().addAll(items);
                searchResultList.getSelectionModel().select(0);
                if (!searchResultList.isVisible()) {
                    searchResultList.setVisible(true);
                }
            } else {
                if (searchResultList.isVisible()) {
                    searchResultList.setVisible(false);
                }
            }

        });
    }

    private void initSearchResultList() {
        searchResultList.setCellFactory(callback -> new ListCell<ZkNodeSearchResult>() {
            @Override
            protected void updateItem(ZkNodeSearchResult item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    setText(null);
                    setGraphic(item.getTextFlow());
                    setOnMouseClicked(mouseEvent -> {
                        if (mouseEvent.getClickCount() == 2) {
                            ListCell<ZkNodeSearchResult> clickedRow = (ListCell<ZkNodeSearchResult>) mouseEvent.getSource();
                            zkNodeTreeView.getSelectionModel().select(clickedRow.getItem().getItem());
                            zkNodeTreeView.scrollTo(zkNodeTreeView.getSelectionModel().getSelectedIndex());
                            if (searchResultList.isVisible()) {
                                searchResultList.setVisible(false);
                            }
                        }
                    });
                }
            }
        });
        searchResultList.setOnMouseExited(e -> {
            if (searchResultList.isVisible()) {
                searchResultList.setVisible(false);
            }
        });
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
                        Platform.runLater(() -> zkNodeVO.change(newValue.getValue()));
                    }
                });
    }

    private void initServerListView() {
        serverListView.itemsProperty().set(prettyZooConfigVO.getServers());
        serverListView.setCellFactory(cellCallback -> new ZkServerListCell(this::switchServer));
    }

    private void switchServer(ZkServerConfigVO server) {
        try {
            log.debug("begin to switch server to {}", server.getHost());
            server.connectIfNecessary();
        } catch (Exception e) {
            log.debug("switch server {} failed: {}", server.getHost(), e.getMessage());
            VToast.toastFailure("Failed: " + e.getMessage());
            return;
        }


        zkNodeTreeView.setCellFactory(view -> new ZkNodeTreeCell());
        final TreeItem<ZkNode> root = getOrCreateTreeRoot(server);
        zkNodeTreeView.setRoot(root);
        zkNodeVO.change(root.getValue());
        ActiveServerContext.set(server.getHost());
        server.syncIfNecessary();
        log.debug("switch server {} success", server.getHost());
    }

    private TreeItem<ZkNode> getOrCreateTreeRoot(ZkServerConfigVO server) {
        String host = server.getHost();
        final String root = "/";
        final TreeItemCache treeItemCache = TreeItemCache.getInstance();
        if (!treeItemCache.hasNode(host, root)) {
            final ZkNode zkNode = new ZkNode(root, root);
            zkNode.resetStat();
            treeItemCache.add(host, root, new TreeItem<>(zkNode));
        }
        return treeItemCache.get(host, root);
    }
}
