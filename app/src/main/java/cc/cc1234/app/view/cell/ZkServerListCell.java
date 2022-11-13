package cc.cc1234.app.view.cell;

import cc.cc1234.app.facade.PrettyZooFacade;
import cc.cc1234.app.util.ResourceBundleUtils;
import cc.cc1234.app.vo.ServerConfigurationVO;
import cc.cc1234.app.vo.ServerStatus;
import com.jfoenix.controls.JFXButton;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.util.Collection;
import java.util.ResourceBundle;
import java.util.function.Consumer;

public class ZkServerListCell extends ListCell<ServerConfigurationVO> {

    private static final String CONNECTED_SYMBOL = "assets/img/connected-symbol.png";

    private static final String RECONNECTING_SYMBOL = "assets/img/reconnecting-symbol.png";

    private PrettyZooFacade prettyZooFacade = new PrettyZooFacade();

    private CustomMenuItem connectMenu;

    private CustomMenuItem disConnectMenu;

    private CustomMenuItem deleteMenu;

    public ZkServerListCell(Consumer<ServerConfigurationVO> connectAction,
                            Consumer<ServerConfigurationVO> deleteAction,
                            Consumer<ServerConfigurationVO> disconnectAction) {
        ResourceBundle rb = ResourceBundleUtils.get(prettyZooFacade.getLocale());
        ImageView connectGraphic = new ImageView("assets/img/connect.png");
        connectGraphic.setFitWidth(18);
        connectGraphic.setFitHeight(18);
        String connectText = rb.getString("server.button.connect");
        var connectButton = new JFXButton(connectText);
        connectButton.setGraphic(connectGraphic);

        ImageView deleteGraphic = new ImageView("assets/img/delete.png");
        deleteGraphic.setFitWidth(18);
        deleteGraphic.setFitHeight(18);
        String deleteText = rb.getString("server.button.delete");
        var deleteButton = new JFXButton(deleteText);
        deleteButton.setGraphic(deleteGraphic);

        ImageView disconnectGraphic = new ImageView("assets/img/disconnect.png");
        disconnectGraphic.setFitWidth(18);
        disconnectGraphic.setFitHeight(18);
        String disconnectText = rb.getString("server.button.disconnect");
        var disconnectButton = new JFXButton(disconnectText);
        disconnectButton.setGraphic(disconnectGraphic);

        deleteButton.setOnAction(e -> deleteAction.accept(getItem()));
        connectButton.setOnAction(e -> connectAction.accept(getItem()));
        disconnectButton.setOnAction(e -> disconnectAction.accept(getItem()));

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
            super.setText(null);
            super.setGraphic(null);
            return;
        }

        setText(null);
        var label = new Label();
        label.textProperty().bind(serverNameBinding(item));

        var symbolImage = new ImageView(CONNECTED_SYMBOL);
        symbolImage.setFitWidth(8);
        symbolImage.setFitHeight(8);

        var progressIndicator = new ProgressIndicator();
        progressIndicator.setPrefSize(12, 12);
        progressIndicator.getStyleClass().add("red");

        var hbox = new HBox(10, label);
        hbox.setId(item.getZkUrl());
        hbox.getStyleClass().add("server-item");
        hbox.setAlignment(Pos.CENTER);
        item.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                onServerStatusChange(newValue, hbox, progressIndicator);
                onServerStatusChange(newValue, hbox, symbolImage);
            }
        });
        onServerStatusChange(item.getStatus(), hbox, progressIndicator);
        onServerStatusChange(item.getStatus(), hbox, symbolImage);
        super.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue) {
                updateItemSelectedCss(hbox, progressIndicator);
            } else {
                updateItemUnSelectedCss(hbox, progressIndicator);
            }
        }));
        if (this.isSelected()) {
            updateItemSelectedCss(hbox, progressIndicator);
        } else {
            updateItemUnSelectedCss(hbox, progressIndicator);
        }
        super.setPadding(new Insets(5, 5, 5, 5));
        super.setGraphic(hbox);
    }

    private StringBinding serverNameBinding(ServerConfigurationVO item) {
        return Bindings.createStringBinding(() -> serverNameFormat(item), item.zkUrlProperty(), item.zkAliasProperty());
    }

    private String serverNameFormat(ServerConfigurationVO item) {
        String server = item.getZkUrl();
        String alias = item.getZkAlias();
        if (alias != null && !alias.isBlank()) {
            return alias;
        } else {
            return server;
        }
    }

    private void onServerStatusChange(ServerStatus newValue, HBox hbox, ImageView child) {
        Platform.runLater(() -> {
            switch (newValue) {
                case DISCONNECTED:
                    hbox.getChildren().remove(child);
                    if (!getContextMenu().getItems().contains(connectMenu)) {
                        getContextMenu().getItems().add(0, connectMenu);
                    }
                    if (!getContextMenu().getItems().contains(deleteMenu)) {
                        getContextMenu().getItems().add(deleteMenu);
                    }
                    getContextMenu().getItems().remove(disConnectMenu);
                    break;
                case CONNECTING:
                case RECONNECTING:
                    child.setImage(new Image(RECONNECTING_SYMBOL));
                    getContextMenu().getItems().clear();
                    break;
                case CONNECTED:
                    child.setImage(new Image(CONNECTED_SYMBOL));
                    hbox.getChildren().add(0, child);
                    if (!getContextMenu().getItems().contains(disConnectMenu)) {
                        getContextMenu().getItems().add(0, disConnectMenu);
                    }
                    getContextMenu().getItems().remove(connectMenu);
                    getContextMenu().getItems().remove(deleteMenu);
                    break;
                default:
                    break;
            }
        });
    }

    private void onServerStatusChange(ServerStatus newValue, HBox hbox, ProgressIndicator child) {
        Platform.runLater(() -> {
            switch (newValue) {
                case CONNECTING:
                case RECONNECTING:
                    addIfNecessary(hbox.getChildren(), child);
                    break;
                case DISCONNECTED:
                case CONNECTED:
                    removeIfNecessary(hbox.getChildren(), child);
                    break;
                default:
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

    private void updateItemSelectedCss(HBox hbox, ProgressIndicator indicator) {
        hbox.getStyleClass().add("server-item-select");
        indicator.getStyleClass().addAll("server-select-progress-indicator");
        hbox.getStyleClass().remove("server-item");
    }

    private void updateItemUnSelectedCss(HBox hbox, ProgressIndicator indicator) {
        hbox.getStyleClass().remove("server-item-select");
        indicator.getStyleClass().removeAll("server-progress-indicator");
        hbox.getStyleClass().add("server-item");
    }
}
