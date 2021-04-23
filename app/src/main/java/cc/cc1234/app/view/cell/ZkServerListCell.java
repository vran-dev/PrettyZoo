package cc.cc1234.app.view.cell;

import cc.cc1234.app.facade.PrettyZooFacade;
import cc.cc1234.app.vo.ServerConfigurationVO;
import cc.cc1234.app.vo.ServerStatus;
import com.jfoenix.controls.JFXButton;
import com.jfoenix.controls.JFXListCell;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.geometry.Pos;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.CustomMenuItem;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.util.Collection;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class ZkServerListCell extends JFXListCell<ServerConfigurationVO> {

    private PrettyZooFacade prettyZooFacade = new PrettyZooFacade();

    private CustomMenuItem connectMenu;

    private CustomMenuItem disConnectMenu;

    private CustomMenuItem deleteMenu;

    public ZkServerListCell(Consumer<ServerConfigurationVO> connectAction,
                            Consumer<ServerConfigurationVO> deleteAction) {
        ResourceBundle rb = ResourceBundle.getBundle("i18n", prettyZooFacade.getLocale());
        String connectText = rb.getString("server.button.connect");
        String deleteText = rb.getString("server.button.delete");
        String disconnectText = rb.getString("server.button.disconnect");

        ImageView connectGraphic = new ImageView("assets/img/connect.png");
        connectGraphic.setFitWidth(18);
        connectGraphic.setFitHeight(18);
        var connectButton = new JFXButton(connectText);
        connectButton.setGraphic(connectGraphic);

        ImageView deleteGraphic = new ImageView("assets/img/delete.png");
        deleteGraphic.setFitWidth(18);
        deleteGraphic.setFitHeight(18);
        var deleteButton = new JFXButton(deleteText);
        deleteButton.setGraphic(deleteGraphic);

        ImageView disconnectGraphic = new ImageView("assets/img/disconnect.png");
        disconnectGraphic.setFitWidth(18);
        disconnectGraphic.setFitHeight(18);
        var disconnectButton = new JFXButton(disconnectText);
        disconnectButton.setGraphic(disconnectGraphic);

        deleteButton.setOnAction(e -> deleteAction.accept(getItem()));
        connectButton.setOnAction(e -> connectAction.accept(getItem()));
        disconnectButton.setOnAction(e -> prettyZooFacade.disconnect(getItem().getZkServer()));

        deleteMenu = new CustomMenuItem(deleteButton);
        connectMenu = new CustomMenuItem(connectButton);
        disConnectMenu = new CustomMenuItem(disconnectButton);

        ContextMenu contextMenu = new ContextMenu(connectMenu, deleteMenu);
        this.setContextMenu(contextMenu);
    }

    @Override
    protected void updateItem(ServerConfigurationVO item, boolean empty) {
        super.updateItem(item, empty);
        if (empty || item == null) {
            setText(null);
            setGraphic(null);
        } else if (getGraphic() == null) {
            setText(null);

            var label = new Label();
            label.textProperty().bind(serverNameBinding(item));

            var symbolImage = new ImageView("assets/img/connected-symbol.png");
            symbolImage.setFitWidth(10);
            symbolImage.setFitHeight(10);

            var progressIndicator = new ProgressIndicator();
            progressIndicator.setPrefSize(14, 14);

            var hBox = new HBox(10, label);
            hBox.setAlignment(Pos.CENTER_LEFT);
            item.statusProperty().addListener((observable, oldValue, newValue) -> {
                if (newValue != null) {
                    onServerStatusChange(newValue, hBox, progressIndicator);
                    onServerStatusChange(newValue, hBox, symbolImage);
                }
            });
            onServerStatusChange(item.getStatus(), hBox, progressIndicator);
            onServerStatusChange(item.getStatus(), hBox, symbolImage);
            setGraphic(hBox);
        }
    }

    private StringBinding serverNameBinding(ServerConfigurationVO item) {
        return Bindings.createStringBinding(() -> serverNameFormat(item), item.zkServerProperty(), item.zkAliasProperty());
    }

    private String serverNameFormat(ServerConfigurationVO item) {
        String server = item.getZkServer();
        String alias = item.getZkAlias();
        if (alias != null && !alias.isBlank()) {
            return alias;
        } else {
            return server;
        }
    }

    private void onServerStatusChange(ServerStatus newValue, HBox hBox, ImageView child) {
        Platform.runLater(() -> {
            switch (newValue) {
                case DISCONNECTED:
                    hBox.getChildren().remove(child);
                    if (!getContextMenu().getItems().contains(connectMenu)) {
                        getContextMenu().getItems().add(0, connectMenu);
                    }
                    if (!getContextMenu().getItems().contains(deleteMenu)) {
                        getContextMenu().getItems().add( deleteMenu);
                    }
                    break;
                case CONNECTING:
                case RECONNECTING:
                    hBox.getChildren().remove(child);
                    getContextMenu().getItems().clear();
                    break;
                case CONNECTED:
                    hBox.getChildren().add(0, child);
                    if (!getContextMenu().getItems().contains(disConnectMenu)) {
                        getContextMenu().getItems().add(0, disConnectMenu);
                    }
                    getContextMenu().getItems().remove(connectMenu);
                    getContextMenu().getItems().remove(deleteMenu);
                    break;
            }
        });
    }

    private void onServerStatusChange(ServerStatus newValue, HBox hBox, ProgressIndicator child) {
        Platform.runLater(() -> {
            switch (newValue) {
                case CONNECTING:
                case RECONNECTING:
                    addIfNecessary(hBox.getChildren(), child);
                    break;
                case DISCONNECTED:
                case CONNECTED:
                    removeIfNecessary(hBox.getChildren(), child);
                    break;
            }
        });
    }

    private <T> void addIfNecessary(Collection<T> collection, T value) {
        if (!collection.contains(value)) {
            collection.add(value);
        }
    }

    private <T> void removeIfNecessary(Collection<T> collection, T value) {
        collection.remove(value);
    }

}
