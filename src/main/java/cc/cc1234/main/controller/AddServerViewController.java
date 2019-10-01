package cc.cc1234.main.controller;

import cc.cc1234.main.history.History;
import cc.cc1234.main.model.ZkServer;
import javafx.animation.FadeTransition;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import javafx.util.Duration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

public class AddServerViewController {

    private static final Logger LOG = LoggerFactory.getLogger(AddServerViewController.class);

    @FXML
    private TextField serverTextField;

    private Stage stage;


    private TableView<ZkServer> serversTableView;

    public static void show(TableView<ZkServer> serversTableView) {
        String fxml = "AddServerView.fxml";
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(AddServerViewController.class.getResource(fxml));
        AnchorPane panel = null;
        try {
            panel = loader.load();
        } catch (IOException e) {
            LOG.error("init AddServerView failed", e);
            return;
        }

        final Scene scene = new Scene(panel);
        scene.setFill(Color.TRANSPARENT);
        final Stage stage = new Stage();
        stage.setScene(scene);
        stage.initStyle(StageStyle.TRANSPARENT);
        stage.initModality(Modality.APPLICATION_MODAL);

        final AddServerViewController controller = loader.getController();
        controller.setStage(stage);
        controller.setServersTableView(serversTableView);

        final Window parent = serversTableView.getParent().getScene().getWindow();
        double x = parent.getX() + parent.getWidth() / 2 - panel.getPrefWidth() / 2;
        double y = parent.getY() + parent.getHeight() / 2 - panel.getPrefHeight() / 2;
        stage.setX(x);
        stage.setY(y);
        animation(panel);
        stage.show();
    }

    private static void animation(Node node) {
        final FadeTransition transition = new FadeTransition();
        transition.setFromValue(0.3);
        transition.setToValue(1.0);
        transition.setDuration(Duration.millis(800));
        transition.setNode(node);
        transition.play();
    }


    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setServersTableView(TableView<ZkServer> serversTableView) {
        this.serversTableView = serversTableView;
    }

    @FXML
    private void onCancel() {
        stage.close();
    }

    @FXML
    private void onConfirm() {
        final String server = serverTextField.getText();
        if (server == null || server.trim().isEmpty()) {
            VToast.toastFailure(stage, "server must not be empty");
            return;
        }
        final History history = History.createIfAbsent(History.SERVER_HISTORY);
        if (history.contains(server)) {
            VToast.toastFailure(stage, "server exists!");
            return;
        }
        history.save(server, "0");
        serversTableView.getItems().add(new ZkServer(server));
        VToast.toastSuccess(stage);
        stage.close();
    }
}
