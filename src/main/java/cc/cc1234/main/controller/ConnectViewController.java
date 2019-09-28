package cc.cc1234.main.controller;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;

import java.io.IOException;

public class ConnectViewController {

    @FXML
    private TextField serverTextFields;

    private Stage primaryStage;

    private CuratorFramework client;

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void onConnectConfirm() {
        final String server = serverTextFields.getText();
        final RetryOneTime retryPolicy = new RetryOneTime(3000);
        this.client = CuratorFrameworkFactory.newClient(server, retryPolicy);
        try {
            client.start();
            // TODO @vran 增加 progress bar
            client.blockUntilConnected();
        } catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }

        try {
            showNodeTreeView();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


    @FXML
    private void onConnectCancel() {
        primaryStage.close();
    }

    private void showNodeTreeView() throws IOException {
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(ConnectViewController.class.getResource("NodeTreeView.fxml"));
        final AnchorPane anchorPane = loader.load();
        primaryStage.getScene().setRoot(anchorPane);
        primaryStage.sizeToScene();

        NodeTreeViewController controller = loader.getController();
        controller.viewInit(client);
    }
}
