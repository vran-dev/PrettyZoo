package cc.cc1234.main.controller;

import cc.cc1234.main.cache.PrettyZooConfigContext;
import cc.cc1234.main.model.PrettyZooConfig;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class AddServerViewController {

    @FXML
    private TextField serverTextField;

    @FXML
    private AnchorPane addServerPane;

    private Stage stage;

    @FXML
    public void initialize() {
        stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);
        final Scene scene = new Scene(addServerPane);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
    }


    public void show(Window parent) {
        double x = parent.getX() + parent.getWidth() / 2 - addServerPane.getPrefWidth() / 2;
        double y = parent.getY() + parent.getHeight() / 2 - addServerPane.getPrefHeight() / 2;
        stage.setX(x);
        stage.setY(y);
        stage.show();
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

        final PrettyZooConfig config = PrettyZooConfigContext.get();
        if (config.contains(server)) {
            VToast.toastFailure(stage, "server exists!");
            return;
        }
        config.save(server);
        VToast.toastSuccess(stage);
        stage.close();
    }
}
