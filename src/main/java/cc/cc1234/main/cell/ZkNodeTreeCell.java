package cc.cc1234.main.cell;

import cc.cc1234.main.model.ZkNode;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ZkNodeTreeCell extends TreeCell<ZkNode> {

    private static final Logger log = LoggerFactory.getLogger(ZkNodeTreeCell.class);


    public ZkNodeTreeCell() {
    }

    @Override
    protected void updateItem(ZkNode item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            final TreeItem<ZkNode> treeItem = getTreeItem();
            final Text graphic = new Text(item.getName());
            // ephemeral node
            if (treeItem.getValue().getEphemeralOwner() != 0) {
                graphic.setFill(Color.valueOf("#ffab00"));
                setGraphic(graphic);
                setText(null);
            } else {
                setText(treeItem.getValue().getName());
            }
        }
    }

}
