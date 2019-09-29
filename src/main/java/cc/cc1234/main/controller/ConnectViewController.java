package cc.cc1234.main.controller;

import cc.cc1234.main.history.History;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.geometry.Side;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextField;
import javafx.scene.input.ContextMenuEvent;
import javafx.stage.Stage;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.RetryOneTime;

import java.io.IOException;
import java.util.Comparator;
import java.util.Map;
import java.util.concurrent.Executors;

public class ConnectViewController {

    @FXML
    private TextField serverTextFields;

    private Stage primaryStage;

    private CuratorFramework client;

    private History history;

    private ContextMenu contextMenu = new ContextMenu();

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    @FXML
    private void initialize() {
        history = History.createIfAbsent("server-input.history");
        final Map<String, String> allHistoryServers = history.getAll();
        if (!allHistoryServers.isEmpty()) {
            final MenuItem clearMenu = new MenuItem("clear all history");
            clearMenu.setStyle("-fx-text-fill: crimson");
            clearMenu.setOnAction(event -> {
                contextMenu.getItems().clear();
                history.clear();
            });
            contextMenu.getItems().add(clearMenu);
            serverTextFields.addEventFilter(ContextMenuEvent.ANY, Event::consume);
            serverTextFields.setContextMenu(contextMenu);
        }


        allHistoryServers.entrySet().stream()
                .map(e -> new ZkServer(e.getKey(), e.getValue()))
                .sorted(Comparator.comparing(s -> s.connectTimes))
                .forEach(server -> {
                    final MenuItem item = new MenuItem(server.server);
                    item.setOnAction(event -> {
                        MenuItem source = (MenuItem) event.getSource();
                        serverTextFields.setText(source.getText());
                    });
                    contextMenu.getItems().add(item);
                });
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
            Executors.newSingleThreadExecutor().execute(() -> {
                final String value = history.get(server, "0");
                history.save(server, String.valueOf(Integer.parseInt(value) + 1));
                history.store();
            });
            NodeTreeViewController.showNodeTreeView(client, primaryStage);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @FXML
    private void onInputChange() {
        if (!contextMenu.isShowing()) {
            contextMenu.show(serverTextFields, Side.BOTTOM, 0, 0);
        }
    }

    @FXML
    private void onConnectCancel() {
        primaryStage.close();
    }


    private final class ZkServer {
        String server;
        int connectTimes;

        ZkServer(String server, String connectTimes) {
            this.server = server;
            this.connectTimes = Integer.parseInt(connectTimes);
        }
    }
}
