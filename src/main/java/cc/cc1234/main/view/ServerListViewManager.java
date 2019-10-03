package cc.cc1234.main.view;

import cc.cc1234.main.history.History;
import cc.cc1234.main.model.ZkServer;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ServerListViewManager {

    public static void init(ListView<ZkServer> serverListView,
                            Consumer<ZkServer> callback,
                            History history) {
        // items
        serverListView.setItems(historyToItems(history));
        configCellEvent(serverListView, callback);
    }

    private static ObservableList<ZkServer> historyToItems(History history) {
        final ObservableList<ZkServer> items = FXCollections.observableArrayList();
        final List<ServerListViewManager.ZkServerHistory> historyServers = history.getAll().entrySet()
                .stream()
                .map(e -> new ServerListViewManager.ZkServerHistory(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingInt(e -> e.times))
                .collect(Collectors.toList());
        Collections.reverse(historyServers);
        historyServers.forEach(zs -> items.add(new ZkServer(zs.server)));
        return items;
    }

    private static void configCellEvent(ListView<ZkServer> serverListView,
                                        Consumer<ZkServer> callback) {


        serverListView.setCellFactory(cellCallback -> new ListCell<ZkServer>() {
            @Override
            protected void updateItem(ZkServer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    final String server = item.getServer();
                    final StringBinding bindings = Bindings.createStringBinding(() -> {
                        return item.getConnect() ? "√ " + server : "× " + server;
                    }, item.serverProperty(), item.connectProperty());
                    final Label label = new Label();
                    label.textProperty().bind(bindings);
                    setText(null);
                    setGraphic(label);
                    setOnMouseClicked(mouseEvent -> {
                        ListCell<ZkServer> clickedRow = (ListCell<ZkServer>) mouseEvent.getSource();
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
                }
            }
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
