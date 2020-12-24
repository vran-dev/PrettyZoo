package cc.cc1234.app.view.cell;

import cc.cc1234.app.vo.ServerConfigurationVO;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;

public class ZkServerListCell extends ListCell<ServerConfigurationVO> {

    @Override
    protected void updateItem(ServerConfigurationVO item, boolean empty) {
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

    private StringBinding connectBinding(ServerConfigurationVO item) {
        return Bindings.createStringBinding(() -> connectSymbol(item), item.zkServerProperty(), item.connectedProperty());
    }

    private String connectSymbol(ServerConfigurationVO item) {
        String server = item.getZkServer();
        return item.isConnected() ? "√ " + server : "× " + server;
    }
}
