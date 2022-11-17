package cc.cc1234.app.view.cell;

import cc.cc1234.app.facade.PrettyZooFacade;
import cc.cc1234.app.util.ResourceBundleUtils;
import cc.cc1234.specification.node.ZkNode;
import com.jfoenix.controls.JFXTreeCell;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.ResourceBundle;

public class ZkNodeTreeCell extends JFXTreeCell<ZkNode> {

    private PrettyZooFacade prettyZooFacade = new PrettyZooFacade();

    public ZkNodeTreeCell(Runnable createAction, Runnable deleteAction) {
        ResourceBundle rb = ResourceBundleUtils.get(prettyZooFacade.getLocale());
        String addButtonText = rb.getString("nodeList.button.add");
        ImageView addGraphic = new ImageView("assets/img/add.png");
        addGraphic.setFitWidth(18);
        addGraphic.setFitHeight(18);

        MenuItem add = new MenuItem(addButtonText, addGraphic);
        add.setOnAction(e -> createAction.run());

        ImageView deleteGraphic = new ImageView("assets/img/delete.png");
        deleteGraphic.setFitHeight(18);
        deleteGraphic.setFitWidth(18);
        String deleteButtonText = rb.getString("nodeList.button.delete");
        MenuItem delete = new MenuItem(deleteButtonText, deleteGraphic);
        delete.setOnAction(e -> {
            deleteAction.run();
        });

        ContextMenu contextMenu = new ContextMenu(add, delete);
        this.setContextMenu(contextMenu);
    }

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
