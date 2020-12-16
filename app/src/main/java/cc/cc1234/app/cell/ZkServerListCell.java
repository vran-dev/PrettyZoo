package cc.cc1234.app.cell;

import cc.cc1234.app.vo.ServerConfigVO;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;

public class ZkServerListCell extends ListCell<ServerConfigVO> {

    @Override
    protected void updateItem(ServerConfigVO item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            final Label label = new Label();
            label.textProperty().bind(connectBinding(item));
            setText(null);
            setGraphic(label);
        }
    }

    private StringBinding connectBinding(ServerConfigVO item) {
        return Bindings.createStringBinding(() -> connectStr(item), item.zkServerProperty(), item.connectedProperty());
    }

    private String connectStr(ServerConfigVO item) {
        String server = item.getZkServer();
        return item.isConnected() ? "√ " + server : "× " + server;
    }
}
