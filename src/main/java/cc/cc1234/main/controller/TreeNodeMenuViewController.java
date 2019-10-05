package cc.cc1234.main.controller;

import cc.cc1234.main.cache.RecursiveModeContext;
import cc.cc1234.main.cache.TreeViewCache;
import cc.cc1234.main.model.ZkNode;
import cc.cc1234.main.service.ZkServerService;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.DeleteBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class TreeNodeMenuViewController {

    private static final Logger log = LoggerFactory.getLogger(TreeNodeMenuViewController.class);

    private TreeItem<ZkNode> selectedItem;

    private Window parent;

    private Stage stage;

    private Node node;

    private double width;

    private double toX;

    @FXML
    private Button nodeDeleteButton;

    @FXML
    private Button nodeAddButton;


    public static void show(TreeItem<ZkNode> selectedItem, double x, double y) throws Exception {
        if (selectedItem == null || selectedItem.getValue() == null) {
            return;
        }
        final FXMLLoader fxmlLoader = new FXMLLoader();
        fxmlLoader.setLocation(TreeNodeMenuViewController.class.getResource("TreeNodeMenuView.fxml"));
        final AnchorPane anchorPane = fxmlLoader.load();
        final TreeNodeMenuViewController controller = fxmlLoader.getController();
        controller.show(anchorPane, selectedItem, x, y);
    }


    @FXML
    private void onNodeDeleteAction() {
        shrink(e -> stage.close());
        final CuratorFramework client = ZkServerService.getActive().getClient();
        final String path = selectedItem.getValue().getPath();
        final DeleteBuilder deleteBuilder = client.delete();
        try {
            if (RecursiveModeContext.get()) {
                deleteBuilder.deletingChildrenIfNeeded().forPath(path);
            } else {
                deleteBuilder.forPath(path);
            }
        } catch (Exception e) {
            log.error("delete node failed", e);
            VToast.toastFailure(stage, "delete failed");
        }
        TreeViewCache.getInstance().getTreeView().getSelectionModel().clearSelection();
        VToast.toastSuccess(parent);
    }

    @FXML
    private void onNodeAddAction() {
        shrink(e -> stage.close());
        final CuratorFramework client = ZkServerService.getActive().getClient();
        try {
            AddNodeViewController.initController(selectedItem.getValue().getPath(), client);
        } catch (IOException e) {
            log.error("open add node view failed", e);
            VToast.toastFailure(parent, "Unknown error");
        }
    }

    public void show(AnchorPane anchorPane, TreeItem<ZkNode> selectedItem, double x, double y) {
        final TreeViewCache<ZkNode> cache = TreeViewCache.getInstance();
        final TreeView<ZkNode> treeView = cache.getTreeView();
        double width = treeView.getWidth();
        final Window owner = treeView.getParent().getScene().getWindow();

        // init stage
        final Stage stage = initStage(owner, anchorPane, x, y);
        anchorPane.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> shrink(e -> stage.close()));

        // set controller
        this.setStage(stage);
        this.setParent(owner);
        this.setNode(anchorPane);
        this.setSelectedItem(selectedItem);
        this.setToX(toX);
        this.setWidth(width);
        hideButtonConditional(selectedItem);
        // show
        anchorPane.setVisible(false);
        stage.show();
        expand();
    }

    private Stage initStage(Window parent,
                            AnchorPane anchorPane,
                            double x,
                            double y) {
        final Scene scene = new Scene(anchorPane);
        scene.setFill(Color.TRANSPARENT);

        final Stage stage = new Stage();
        stage.setScene(scene);
        stage.initOwner(parent);
        stage.initModality(Modality.WINDOW_MODAL);
        stage.initStyle(StageStyle.TRANSPARENT);
        double formatY = y < 30 ? 0 : y - 30;
        stage.setX(x);
        stage.setY(formatY);
        return stage;
    }

    private void hideButtonConditional(TreeItem<ZkNode> selectedItem) {
        final ZkNode node = selectedItem.getValue();
        // leaf && ephemeral: can not add
        if (selectedItem.isLeaf() && node.getEphemeralOwner() != 0) {
            hideAddButton();
        }

        // only delete leaf node in common mode
        if (!RecursiveModeContext.get() && !selectedItem.isLeaf()) {
            hideDeleteButton();
        }
    }

    private void expand() {
        node.setTranslateX(-width);
        node.setVisible(true);
        final TranslateTransition transition = new TranslateTransition(Duration.millis(500), node);
        transition.setToX(0);
        transition.setAutoReverse(false);
        transition.play();
    }

    private void shrink(EventHandler<ActionEvent> finishedEvent) {
        final TranslateTransition transition = new TranslateTransition(Duration.millis(500), node);
        transition.setOnFinished(finishedEvent);
        transition.setToX(-width);
        transition.setAutoReverse(false);
        transition.play();
    }

    public void hideAddButton() {
        nodeAddButton.setVisible(false);
        if (nodeDeleteButton.isVisible()) {
            nodeAddButton.setTranslateX(10);
        }
    }

    public void hideDeleteButton() {
        nodeDeleteButton.setVisible(false);
    }


    private void setSelectedItem(TreeItem<ZkNode> selectedItem) {
        this.selectedItem = selectedItem;
    }

    private void setParent(Window parent) {
        this.parent = parent;
    }

    private void setStage(Stage stage) {
        this.stage = stage;
    }

    private void setNode(Node node) {
        this.node = node;
    }

    private void setWidth(double width) {
        this.width = width;
    }

    private void setToX(double toX) {
        this.toX = toX;
    }
}
