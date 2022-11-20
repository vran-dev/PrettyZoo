package cc.cc1234.app.view.cell;

import cc.cc1234.app.facade.PrettyZooFacade;
import cc.cc1234.app.util.ResourceBundleUtils;
import cc.cc1234.specification.node.ZkNode;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXTreeCell;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;

import java.util.ResourceBundle;

public class ZkNodeTreeCell extends JFXTreeCell<ZkNode> {

    private PrettyZooFacade prettyZooFacade = new PrettyZooFacade();

    private Text node = new Text();

    public ZkNodeTreeCell(Runnable createAction, Runnable deleteAction) {
        node.getStyleClass().add("black-text");

        ResourceBundle rb = ResourceBundleUtils.get(prettyZooFacade.getLocale());

        String addButtonText = rb.getString("nodeList.button.add");
        var add = new JFXButton(addButtonText);
        Label addGraphic = new Label();
        addGraphic.getStyleClass().add("add-button");
        add.setGraphic(addGraphic);
        add.setOnAction(e -> createAction.run());

        String deleteButtonText = rb.getString("nodeList.button.delete");
        var delete = new JFXButton(deleteButtonText);
        Label deleteGraphic = new Label();
        deleteGraphic.getStyleClass().add("remove-button");
        delete.setGraphic(deleteGraphic);
        delete.setOnAction(e -> {
            deleteAction.run();
        });

        ContextMenu contextMenu = new ContextMenu(
                new CustomMenuItem(add),
                new CustomMenuItem(delete)
        );
        this.setContextMenu(contextMenu);
    }

    @Override
    protected void updateItem(ZkNode item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else {
            node.setText(item.getName());
            // ephemeral node
            if (item.getEphemeralOwner() != 0) {
                node.setFill(Color.valueOf("#ffab00"));
            }
            final HBox hbox = new HBox();
            hbox.getChildren().add(node);
            setGraphic(hbox);
            setText(null);
        }
    }
}
