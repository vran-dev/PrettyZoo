package cc.cc1234.app.view.cell;

import cc.cc1234.specification.node.ZkNode;
import com.jfoenix.controls.JFXTreeCell;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

public class ZkNodeTreeCell extends JFXTreeCell<ZkNode> {

    @Override
    protected void updateItem(ZkNode item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            final Text node = new Text(item.getName());
            // ephemeral node
            if (item.getEphemeralOwner() != 0) {
                node.setFill(Color.valueOf("#ffab00"));
            }

            if (this.isSelected() && item.getEphemeralOwner() == 0) {
                node.setFill(Color.valueOf("#FFF"));
            }

            final HBox hbox = new HBox();
            hbox.getChildren().add(node);
            setGraphic(hbox);
            setText(null);
        }
    }
}
