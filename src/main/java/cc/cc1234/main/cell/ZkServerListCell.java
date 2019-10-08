package cc.cc1234.main.cell;

import cc.cc1234.main.util.Transitions;
import cc.cc1234.main.vo.ZkServerConfigVO;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.util.Duration;

import java.util.function.Consumer;

public class ZkServerListCell extends ListCell<ZkServerConfigVO> {

    private final Consumer<ZkServerConfigVO> callback;

    public ZkServerListCell(Consumer<ZkServerConfigVO> callback) {
        this.callback = callback;
    }

    @Override
    protected void updateItem(ZkServerConfigVO item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            final String server = item.getHost();
            final StringBinding bindings = Bindings.createStringBinding(() -> item.isConnect() ? "√ " + server :
                    "× " + server, item.hostProperty(), item.connectProperty());
            final Label label = new Label();
            label.textProperty().bind(bindings);
            setText(null);
            setGraphic(label);
            setOnMouseClicked(mouseEvent -> {
                ListCell<ZkServerConfigVO> clickedRow = (ListCell<ZkServerConfigVO>) mouseEvent.getSource();
                if (!clickedRow.isEmpty()) {
                    final ZkServerConfigVO zkServer = clickedRow.getItem();
                    if (zkServer.isConnect()) {
                        callback.accept(zkServer);
                    } else {
                        // double click to connect zk
                        if (mouseEvent.getClickCount() == 2) {
                            Transitions.scale(this, Duration.millis(200), e -> callback.accept(zkServer)).play();
                        }
                    }
                }
            });
        }
    }

}
