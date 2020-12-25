package cc.cc1234.app.controller;

import cc.cc1234.app.cache.TreeItemCache;
import cc.cc1234.app.context.ActiveServerContext;
import cc.cc1234.app.facade.PrettyZooFacade;
import cc.cc1234.app.fp.Try;
import cc.cc1234.app.listener.DefaultTreeNodeListener;
import cc.cc1234.app.util.FXMLs;
import cc.cc1234.app.view.cell.ZkNodeTreeCell;
import cc.cc1234.app.view.dialog.Dialog;
import cc.cc1234.app.view.toast.VToast;
import cc.cc1234.app.view.transitions.Transitions;
import cc.cc1234.app.vo.ZkNodeSearchResult;
import cc.cc1234.spi.listener.ServerListener;
import cc.cc1234.spi.node.ZkNode;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class NodeViewController {

    private static final Logger log = LoggerFactory.getLogger(NodeViewController.class);

    @FXML
    private AnchorPane nodeViewPane;

    @FXML
    private AnchorPane nodeViewLeftPane;

    @FXML
    private TextField searchTextField;

    @FXML
    private ListView<ZkNodeSearchResult> searchResultList;

    @FXML
    private TreeView<ZkNode> zkNodeTreeView;

    @FXML
    private StackPane nodeViewRightPane;

    @FXML
    private Button nodeAddButton;

    @FXML
    private Button nodeDeleteButton;

    @FXML
    private Button disconnectButton;

    private PrettyZooFacade prettyZooFacade = new PrettyZooFacade();

    private NodeInfoViewController nodeInfoViewController = FXMLs.getController("fxml/NodeInfoView.fxml");

    private NodeAddViewController nodeAddViewController = FXMLs.getController("fxml/NodeAddView.fxml");

    @FXML
    public void initialize() {
        nodeViewPane.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.valueOf("#EEE"), 5, 0.1, 3, 5));

        initSearchResultList();
        initSearchTextField();
        initNodeChangeListener();

        nodeAddButton.setOnMouseClicked(e -> {
            final TreeItem<ZkNode> selectedItem = zkNodeTreeView.getSelectionModel().getSelectedItem();
            if (selectedItem == null) {
                nodeAddViewController.show(nodeViewRightPane);
            } else {
                final ZkNode zkNode = selectedItem.getValue();
                nodeAddViewController.show(nodeViewRightPane, zkNode);
            }
        });

        disconnectButton.setTooltip(new Tooltip("disconnect server"));
        disconnectButton.setOnAction(e -> {
            final String server = ActiveServerContext.get();
            prettyZooFacade.disconnect(server);
            hideAndThen(() -> VToast.info("disconnect " + server + " success"));
        });

        nodeDeleteButton.setOnMouseClicked(e -> {
            final String path = zkNodeTreeView.getSelectionModel().getSelectedItem().getValue().getPath();
            Dialog.confirm("删除节点", "该操作将删除 " + path + " 该节点及其对应的子节点，操作不可恢复，请谨慎执行", () -> {
                Try.of(() -> prettyZooFacade.deleteNode(ActiveServerContext.get(), path))
                        .onFailure(exception -> VToast.error("delete failed:" + exception.getMessage()))
                        .onSuccess(t -> VToast.info("delete success"));
            });
        });
    }

    public void show(StackPane parent,
                     String server,
                     ServerListener serverListener) {
        if (server != null) {
            switchServer(server, serverListener);
        }
        if (!parent.getChildren().contains(nodeViewPane)) {
            parent.getChildren().add(nodeViewPane);
            Transitions.zoomInY(nodeViewPane).play();
        }
    }

    public void hideAndThen(Runnable runnable) {
        final StackPane parent = (StackPane) nodeViewPane.getParent();
        if (parent != null) {
            parent.getChildren().remove(nodeViewPane);
            runnable.run();
        } else {
            runnable.run();
        }
    }

    private void initSearchTextField() {
        searchTextField.textProperty().addListener((o, old, cur) -> {
            searchResultList.getItems().clear();
            final List<ZkNodeSearchResult> items = prettyZooFacade.onSearch(searchTextField.getText());
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

    private void initNodeChangeListener() {
        zkNodeTreeView.getSelectionModel()
                .selectedItemProperty()
                .addListener((observable, oldValue, newValue) -> {
                    nodeAddViewController.hide();
                    if (newValue != null) {
                        nodeInfoViewController.show(nodeViewRightPane, newValue.getValue());
                    }
                });
    }

    private void switchServer(String host, ServerListener serverListener) {
        try {
            log.debug("begin to switch server to {}", host);
            prettyZooFacade.connect(host, List.of(new DefaultTreeNodeListener()), List.of(serverListener));
        } catch (Exception e) {
            log.debug("switch server {} failed: {}", host, e.getMessage());
            throw new IllegalStateException("connect to " + host + " failed", e);
        }

        zkNodeTreeView.setCellFactory(view -> new ZkNodeTreeCell());
        final TreeItem<ZkNode> root = getOrCreateTreeRoot(host);
        zkNodeTreeView.setRoot(root);
        ActiveServerContext.set(host);
        prettyZooFacade.syncIfNecessary(host);
        nodeInfoViewController.show(nodeViewRightPane);
        log.debug("switch server {} success", host);
    }

    private TreeItem<ZkNode> getOrCreateTreeRoot(String host) {
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
