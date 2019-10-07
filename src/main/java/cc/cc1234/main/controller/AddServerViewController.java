package cc.cc1234.main.controller;

import cc.cc1234.main.cache.PrettyZooConfigContext;
import cc.cc1234.main.vo.PrettyZooConfigVO;
import cc.cc1234.main.vo.ZkServerConfigVO;
import com.google.common.base.Strings;
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

    private ZkServerConfigVO zkServerConfig;

    @FXML
    public void initialize() {
        stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);
        final Scene scene = new Scene(addServerPane);
        scene.setFill(Color.TRANSPARENT);
        stage.setOnCloseRequest(e -> zkServerConfig = null);
        stage.setScene(scene);
    }


    public void show(Window parent) {
        zkServerConfig = new ZkServerConfigVO();
        zkServerConfig.hostProperty().bind(serverTextField.textProperty());
        // TODO add and bind ACL property
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
        if (Strings.isNullOrEmpty(zkServerConfig.getHost())) {
            VToast.toastFailure(stage, "server must not be empty");
            return;
        }

        final PrettyZooConfigVO config = PrettyZooConfigContext.get();
        if (config.contains(zkServerConfig.getHost())) {
            VToast.toastFailure(stage, "server exists!");
            return;
        }
        config.save(zkServerConfig);
        VToast.toastSuccess(stage);
        stage.close();
    }
}
