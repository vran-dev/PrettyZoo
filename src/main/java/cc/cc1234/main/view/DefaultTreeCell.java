package cc.cc1234.main.view;

import cc.cc1234.main.controller.AddNodeViewController;
import cc.cc1234.main.controller.VToast;
import cc.cc1234.main.model.ZkNode;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import org.apache.curator.framework.CuratorFramework;

import java.io.IOException;

public class DefaultTreeCell extends TreeCell<ZkNode> {

    private ContextMenu operationMenus;

    private final Stage primaryStage;

    private final CuratorFramework client;

    private final MenuItem deleteMenu;

    private final MenuItem addMenu;


    public DefaultTreeCell(Stage primaryStage, CuratorFramework client) {
        this.primaryStage = primaryStage;
        this.client = client;
        deleteMenu = new MenuItem("Delete");
        deleteMenu.setOnAction(event -> {
            try {
                client.delete().forPath(getTreeItem().getValue().getPath());
            } catch (Exception e) {
                VToast.toastFailure(primaryStage);
                throw new IllegalStateException(e);
            }
        });

        addMenu = new MenuItem("Add");
        addMenu.setOnAction(event -> {
            try {
                AddNodeViewController.initController(getTreeItem().getValue().getPath(), client);
            } catch (IOException e) {
                VToast.toastFailure(primaryStage);
                throw new IllegalStateException(e);
            }
        });
        operationMenus = new ContextMenu();
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
            if (treeItem.getValue().getEphemeralOwner() != 0) {
                graphic.setFill(Color.valueOf("#ffab00"));
                setGraphic(graphic);
                setText(null);
            } else {
                setText(treeItem.getValue().getName());
            }
            setContextMenu(operationMenus);
            if (item.getEphemeralOwner() == 0) {
                addIfAbsent(addMenu);
            } else {
                operationMenus.getItems().remove(addMenu);
            }

            // add delete menu for leaf node
            if (treeItem.isLeaf() && treeItem.getParent() != null) {
                addIfAbsent(deleteMenu);
            } else {
                operationMenus.getItems().remove(deleteMenu);
            }

        }
    }

    private void addIfAbsent(MenuItem menuItem) {
        if (!operationMenus.getItems().contains(menuItem)) {
            operationMenus.getItems().add(menuItem);
        }
    }
}
