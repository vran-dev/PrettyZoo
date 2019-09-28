package cc.cc1234.main.controller;

import cc.cc1234.main.PrettyZooApplication;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;

import java.io.IOException;

public class ConnectViewController {

    @FXML
    private TextField serverTextFields;


    private PrettyZooApplication prettyZooApplication;

    private CuratorFramework client;

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
            prettyZooApplication.nodeTreeView();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


    @FXML
    private void onConnectCancel() {
        prettyZooApplication.close();
    }

    public void setPrettyZooApplication(PrettyZooApplication prettyZooApplication) {
        this.prettyZooApplication = prettyZooApplication;
    }

    public CuratorFramework getClient() {
        return client;
    }
}
