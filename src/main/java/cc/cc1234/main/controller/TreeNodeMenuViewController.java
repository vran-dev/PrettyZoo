package cc.cc1234.main.controller;

import cc.cc1234.main.cache.ActiveServerContext;
import cc.cc1234.main.cache.RecursiveModeContext;
import cc.cc1234.main.model.ZkNode;
import cc.cc1234.main.service.ZkServerService;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.TreeItem;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
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

    @FXML
    private Button nodeDeleteButton;

    @FXML
    private Button nodeAddButton;

    @FXML
    private void onNodeDeleteAction() {
        stage.close();
        final CuratorFramework client = ZkServerService.getInstance(ActiveServerContext.get()).getClient();
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
    }

    @FXML
    private void onNodeAddAction() {
        stage.close();
        final CuratorFramework client = ZkServerService.getInstance(ActiveServerContext.get()).getClient();
        try {
            AddNodeViewController.initController(selectedItem.getValue().getPath(), client);
        } catch (IOException e) {
            log.error("open add node view failed", e);
            VToast.toastFailure(parent, "Unknown error");
        }
    }

    public static void show(Window parent,
                            TreeItem<ZkNode> selectedItem,
                            double x,
                            double y,
                            double width) throws Exception {
        if (selectedItem == null || selectedItem.getValue() == null) {
            return;
        }
        final FXMLLoader fxmlLoader = new FXMLLoader(TreeNodeMenuViewController.class.getResource("TreeNodeMenuView.fxml"));
        AnchorPane anchorPane = fxmlLoader.load();
        // init stage
        final Stage stage = initStage(parent, anchorPane, x, y);
        anchorPane.addEventHandler(MouseEvent.MOUSE_CLICKED, event -> {
            shrink(-width, anchorPane, e -> stage.close());
        });

        // set controller
        final TreeNodeMenuViewController controller = fxmlLoader.getController();
        controller.setParent(parent);
        controller.setSelectedItem(selectedItem);
        controller.setStage(stage);
        hideButtonConditional(selectedItem, controller);
        // show
        stage.show();
        expand(width, anchorPane);
    }

    private static Stage initStage(Window parent,
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

    private static void hideButtonConditional(TreeItem<ZkNode> selectedItem, TreeNodeMenuViewController controller) {
        final ZkNode node = selectedItem.getValue();
        // leaf && ephemeral: can not add
        if (selectedItem.isLeaf() && node.getEphemeralOwner() != 0) {
            controller.hideAddButton();
        }

        // only delete leaf node in common mode
        if (!RecursiveModeContext.get() && !selectedItem.isLeaf()) {
            controller.hideDeleteButton();
        }
    }

    private static void expand(double width, Pane node) {
        node.setTranslateX(-width);
        final TranslateTransition transition = new TranslateTransition(Duration.millis(500), node);
        transition.setToX(0);
        transition.setAutoReverse(false);
        transition.play();
    }

    private static void shrink(double toX, Pane node, EventHandler<ActionEvent> finishedEvent) {
        final TranslateTransition transition = new TranslateTransition(Duration.millis(500), node);
        transition.setOnFinished(finishedEvent);
        transition.setToX(toX);
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


    public void setSelectedItem(TreeItem<ZkNode> selectedItem) {
        this.selectedItem = selectedItem;
    }

    public void setParent(Window parent) {
        this.parent = parent;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

}
