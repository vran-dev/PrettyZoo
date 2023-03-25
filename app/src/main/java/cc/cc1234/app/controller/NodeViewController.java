package cc.cc1234.app.controller;

import cc.cc1234.app.cache.TreeItemCache;
import cc.cc1234.app.context.ActiveServerContext;
import cc.cc1234.app.facade.PrettyZooFacade;
import cc.cc1234.app.fp.Try;
import cc.cc1234.app.util.FXMLs;
import cc.cc1234.app.util.ResourceBundleUtils;
import cc.cc1234.app.view.cell.ZkNodeTreeCell;
import cc.cc1234.app.view.dialog.Dialog;
import cc.cc1234.app.view.toast.VToast;
import cc.cc1234.app.vo.ZkNodeSearchResult;
import cc.cc1234.core.configuration.entity.ServerConfiguration;
import cc.cc1234.specification.node.ZkNode;
import cc.cc1234.specification.util.StringWriter;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.BlurType;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;

public class NodeViewController {

    private static final Logger log = LoggerFactory.getLogger(NodeViewController.class);

    @FXML
    private TabPane nodeViewPane;

    @FXML
    private SplitPane nodeViewSplitPane;

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
    private Tab homeTab;

    @FXML
    private Tab terminalTab;

    @FXML
    private Tab fourLetterCommandTab;

    @FXML
    private TextArea fourLetterCommandResponseArea;

    @FXML
    private TextField fourLetterCommandRequestArea;

    @FXML
    private TextArea terminalArea;

    @FXML
    private TextField terminalInput;

    private PrettyZooFacade prettyZooFacade = new PrettyZooFacade();

    private String serverId;

    private NodeInfoViewController nodeInfoViewController = FXMLs.getController("fxml/NodeInfoView.fxml");

    private NodeAddViewController nodeAddViewController = FXMLs.getController("fxml/NodeAddView.fxml");

    @FXML
    public void initialize() {
        nodeViewPane.setEffect(new DropShadow(BlurType.GAUSSIAN, Color.valueOf("#EEE"), 5, 0.1, 3, 5));
        nodeViewSplitPane.setDividerPositions(prettyZooFacade.getNodeViewSplitPaneDividerPosition());
        nodeViewSplitPane.getDividers().stream().findFirst().ifPresent(divider -> {
            divider.positionProperty().addListener(((observable, oldValue, newValue) -> {
                prettyZooFacade.changeNodeViewSplitPaneDividerPosition(newValue.doubleValue());
            }));
        });
        initSearchResultList();
        initSearchTextField();
        initZkNodeTreeView();
        initTerminalArea();
        initFourLetterTab();
    }

    public void show(StackPane parent,
                     String serverId) {
        if (serverId != null) {
            switchServer(serverId);
        }

        if (!parent.getChildren().contains(nodeViewPane)) {
            parent.getChildren().add(nodeViewPane);
        }
        this.serverId = serverId;
    }

    public void disconnectById(String id) {
        prettyZooFacade.disconnect(id);
        hideAndThen(() -> {
            VToast.info("disconnect " + prettyZooFacade.getServerConfigurationById(id).getLabel() + " success");
        });
    }

    public void hide() {
        hideAndThen(() -> {
        });
    }

    public void hideIfNotActive() {
        if (!ActiveServerContext.isSame(serverId)) {
            hide();
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

    private void onNodeAdd() {
        final TreeItem<ZkNode> selectedItem = zkNodeTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem == null) {
            nodeAddViewController.show(nodeViewRightPane);
        } else {
            final ZkNode zkNode = selectedItem.getValue();
            nodeAddViewController.show(nodeViewRightPane, zkNode);
        }
    }

    private void onNodeDelete() {
        final ObservableList<TreeItem<ZkNode>> selectedItems = zkNodeTreeView.getSelectionModel().getSelectedItems();
        if (selectedItems.isEmpty()) {
            VToast.error("select node first");
        } else {
            var pathList = selectedItems.stream().map(item -> item.getValue().getPath()).collect(Collectors.toList());
            var nodes = String.join("\n", pathList);
            ResourceBundle rb = ResourceBundleUtils.get(prettyZooFacade.getLocale());
            String title = rb.getString("nodeDelete.action.confirm.title");
            String content = String.format(rb.getString("nodeDelete.action.confirm.content"), nodes);
            Dialog.confirm(title, content, () -> {
                Try.of(() -> prettyZooFacade.deleteNode(ActiveServerContext.get(), pathList))
                    .onFailure(exception -> VToast.error("delete failed:" + exception.getMessage()))
                    .onSuccess(t -> VToast.info("Request success"));
            });
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
                            var clickedRow = (ListCell<ZkNodeSearchResult>) mouseEvent.getSource();
                            TreeItem<ZkNode> clickedNode = clickedRow.getItem().getItem();
                            if (searchResultList.isVisible()) {
                                searchResultList.getItems().clear();
                                searchResultList.setVisible(false);
                            }
                            zkNodeTreeView.getSelectionModel().clearSelection();
                            zkNodeTreeView.getSelectionModel().select(clickedNode);
                            zkNodeTreeView.scrollTo(zkNodeTreeView.getSelectionModel().getSelectedIndex());
                            zkNodeTreeView.requestFocus();
                            return;
                        }

                        if (mouseEvent.getButton() == MouseButton.PRIMARY
                            && mouseEvent.getClickCount() == 1) {
                            nodeInfoViewController.show(nodeViewRightPane, item.getItem().getValue());
                            return;
                        }
                    });
                }
            }
        });
    }

    private void initZkNodeTreeView() {
        zkNodeTreeView.setCellFactory(view -> new ZkNodeTreeCell(this::onNodeAdd, this::onNodeDelete));
        zkNodeTreeView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        zkNodeTreeView.getSelectionModel()
            .selectedItemProperty()
            .addListener((observable, oldValue, newValue) -> {
                nodeAddViewController.hide();
                if (newValue != null) {
                    nodeInfoViewController.show(nodeViewRightPane, newValue.getValue());
                }
            });
    }

    private void switchServer(String serverId) {
        initRootTreeNode(serverId);
        ActiveServerContext.set(serverId);
        prettyZooFacade.syncIfNecessary(serverId);
        final TreeItem<ZkNode> selectedItem = zkNodeTreeView.getSelectionModel().getSelectedItem();
        if (selectedItem != null) {
            nodeInfoViewController.show(nodeViewRightPane, selectedItem.getValue());
        } else {
            nodeInfoViewController.show(nodeViewRightPane);
        }
        log.debug("switch server {} success", serverId);
    }

    private void initRootTreeNode(String serverId) {
        final String root = "/";
        final TreeItemCache treeItemCache = TreeItemCache.getInstance();
        if (!treeItemCache.hasNode(serverId, root)) {
            final ZkNode zkNode = new ZkNode(root, root);
            zkNode.resetStat();
            final TreeItem<ZkNode> rootTreeItem = new TreeItem<>(zkNode);
            treeItemCache.add(serverId, root, rootTreeItem);
            zkNodeTreeView.setRoot(rootTreeItem);
        }
    }

    private void initTerminalArea() {
        terminalTab.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                prettyZooFacade.startTerminal(ActiveServerContext.get(), new StringWriter() {
                    @Override
                    public void write(String str) throws IOException {
                        terminalArea.appendText(str);
                    }

                    @Override
                    public void write(byte[] bytes) throws IOException {
                        terminalArea.appendText(new String(bytes));
                    }
                });
            }
        });

        terminalArea.setEditable(false);
        terminalArea.setWrapText(true);
        terminalArea.textProperty()
            .addListener((ob, old, newValue) -> terminalArea.setScrollTop(Double.MAX_VALUE));
        terminalInput.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                final String currentServerId = ActiveServerContext.get();
                ServerConfiguration server = prettyZooFacade.getServerConfigurationById(currentServerId);
                if ("clear".equals(terminalInput.getText())) {
                    terminalInput.clear();
                    terminalArea.clear();
                    terminalArea.appendText(server.getLabel() + " $ " + terminalInput.getText());
                } else {
                    terminalArea.appendText(server.getLabel() + " $ " + terminalInput.getText() + "\r\n");
                    prettyZooFacade.executeCommand(currentServerId, terminalInput.getText());
                    terminalInput.clear();
                }
                terminalArea.appendText("\r\n");
            } else if (e.getCode() == KeyCode.TAB) {
                terminalInput.appendText("\t");
            }
        });
    }

    private void initFourLetterTab() {
        fourLetterCommandRequestArea.setOnKeyPressed(e -> {
            if (e.getCode() == KeyCode.ENTER) {
                String command = fourLetterCommandRequestArea.getText();
                if (command == null || command.trim().equals("") || command.length() != 4) {
                    VToast.error("command is invalid: must be 4 words!");
                } else {
                    fourLetterCommandRequestArea.clear();
                    String currentServer = ActiveServerContext.get();
                    String response = prettyZooFacade.executeFourLetterCommand(currentServer, command);
                    fourLetterCommandResponseArea.clear();
                    fourLetterCommandResponseArea.setText(response);
                }
            }
        });
    }

}
