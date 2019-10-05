package cc.cc1234.main.view;

import cc.cc1234.main.model.ZkServer;
import cc.cc1234.main.util.Transitions;
import javafx.animation.ScaleTransition;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.util.Duration;

import java.util.function.Consumer;

public class ZkServerListCell extends ListCell<ZkServer> {

    private final Consumer<ZkServer> callback;

    public ZkServerListCell(Consumer<ZkServer> callback) {
        this.callback = callback;
    }

    @Override
    protected void updateItem(ZkServer item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            final String server = item.getServer();
            final StringBinding bindings = Bindings.createStringBinding(() -> item.getConnect() ? "√ " + server : "× " + server, item.serverProperty(), item.connectProperty());
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
                            final ScaleTransition scale = Transitions.scale(this, Duration.millis(200),
                                    e -> callback.accept(zkServer));
                            scale.play();
                        }
                    }
                }
            });
        }
    }

}
