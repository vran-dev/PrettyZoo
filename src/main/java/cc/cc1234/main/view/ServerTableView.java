package cc.cc1234.main.view;

import cc.cc1234.main.controller.AddServerViewController;
import cc.cc1234.main.history.History;
import cc.cc1234.main.model.ZkServer;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ServerTableView {

    public static void init(TableView<ZkServer> serversTableView,
                            Consumer<ZkServer> callback,
                            History history) {
        // items
        serversTableView.setItems(historyToItems(history));
        configTableViewEvent(serversTableView);
        configRowEvent(serversTableView, callback);
        configColumnHead(serversTableView);
    }

    private static ObservableList<ZkServer> historyToItems(History history) {
        final ObservableList<ZkServer> items = FXCollections.observableArrayList();
        final List<ZkServerHistory> historyServers = history.getAll().entrySet()
                .stream()
                .map(e -> new ZkServerHistory(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingInt(e -> e.times))
                .collect(Collectors.toList());
        Collections.reverse(historyServers);
        historyServers.forEach(zs -> items.add(new ZkServer(zs.server)));
        return items;
    }

    private static void configTableViewEvent(TableView<ZkServer> serversTableView) {
        serversTableView.addEventHandler(MouseEvent.MOUSE_CLICKED, e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                AddServerViewController.show(serversTableView);
            }
        });
    }

    private static void configRowEvent(TableView<ZkServer> serversTableView,
                                       Consumer<ZkServer> callback) {
        serversTableView.setRowFactory(event -> {
            final TableRow<ZkServer> row = new TableRow<>();
            row.setOnMouseClicked(mouseEvent -> {
                TableRow<ZkServer> clickedRow = (TableRow<ZkServer>) mouseEvent.getSource();
                if (!clickedRow.isEmpty()) {
                    final ZkServer zkServer = clickedRow.getItem();
                    if (zkServer.getConnect()) {
                        callback.accept(zkServer);
                    } else {
                        // double click to connect zk
                        if (mouseEvent.getClickCount() == 2) {
                            callback.accept(zkServer);
                        }
                    }
                }

            });
            return row;
        });
    }

    private static void configColumnHead(TableView<ZkServer> serversTableView) {
        final TableColumn<ZkServer, String> serverColumn = new TableColumn<>("server");
        serversTableView.getColumns().setAll(serverColumn);
        // property binding
        serverColumn.setCellValueFactory(f -> {
            final ZkServer zkServer = f.getValue();
            final StringBinding bindings = Bindings.createStringBinding(() -> {
                final String server = zkServer.getServer();
                return zkServer.getConnect() ? "√ " + server : "× " + server;
            }, zkServer.serverProperty(), zkServer.connectProperty());
            return bindings;
        });
    }

    private static class ZkServerHistory {
        String server;
        int times;

        ZkServerHistory(String server, String times) {
            this.server = server;
            this.times = Integer.parseInt(times);
        }

        public String getServer() {
            return server;
        }

        public int getTimes() {
            return times;
        }
    }
}
