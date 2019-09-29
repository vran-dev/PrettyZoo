package cc.cc1234.main.controller;

import javafx.fxml.FXML;
import javafx.scene.control.TextField;
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
            NodeTreeViewController.showNodeTreeView(client, primaryStage);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


    @FXML
    private void onConnectCancel() {
        primaryStage.close();
    }

}
