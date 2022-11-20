package cc.cc1234.app.view.cell;

import cc.cc1234.app.facade.PrettyZooFacade;
import cc.cc1234.app.util.ResourceBundleUtils;
import cc.cc1234.app.vo.ServerConfigurationVO;
import cc.cc1234.app.vo.ServerStatus;
import com.jfoenix.controls.JFXButton;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.util.ResourceBundle;
import java.util.function.Consumer;

public class ZkServerListCell extends ListCell<ServerConfigurationVO> {

    private static final String CONNECTED_SYMBOL = "assets/img/connected-symbol.png";

    private static final String RECONNECTING_SYMBOL = "assets/img/reconnecting-symbol.png";

    private PrettyZooFacade prettyZooFacade = new PrettyZooFacade();

    private CustomMenuItem connectMenu;

    private CustomMenuItem disConnectMenu;

    private CustomMenuItem deleteMenu;

    private ImageView connectedSymbol;

    private ImageView reconnectingSymbol;

    private ProgressIndicator statusIndicator;

    private HBox hbox;

    public ZkServerListCell(Consumer<ServerConfigurationVO> connectAction,
                            Consumer<ServerConfigurationVO> deleteAction,
                            Consumer<ServerConfigurationVO> disconnectAction) {
        connectedSymbol = new ImageView(CONNECTED_SYMBOL);
        connectedSymbol.setFitWidth(8);
        connectedSymbol.setFitHeight(8);

        reconnectingSymbol = new ImageView(RECONNECTING_SYMBOL);
        reconnectingSymbol.setFitWidth(8);
        reconnectingSymbol.setFitHeight(8);

        statusIndicator = new ProgressIndicator();
        statusIndicator.setPrefSize(12, 12);

        hbox = new HBox(10);
        hbox.getStyleClass().add("server-item");
        hbox.setAlignment(Pos.CENTER);

        ResourceBundle rb = ResourceBundleUtils.get(prettyZooFacade.getLocale());
        String connectText = rb.getString("server.button.connect");
        var connectButton = new JFXButton(connectText);
        Label connectGraphic = new Label();
        connectGraphic.getStyleClass().add("connect-button");
        connectButton.setGraphic(connectGraphic);

        String deleteText = rb.getString("server.button.delete");
        var deleteButton = new JFXButton(deleteText);
        Label deleteButtonLabel = new Label();
        deleteButtonLabel.getStyleClass().add("delete-button");
        deleteButton.setGraphic(deleteButtonLabel);

        String disconnectText = rb.getString("server.button.disconnect");
        var disconnectButton = new JFXButton(disconnectText);
        Label disconnectButtonLabel = new Label();
        disconnectButtonLabel.getStyleClass().add("stop-button");
        disconnectButton.setGraphic(disconnectButtonLabel);

        deleteButton.setOnAction(e -> deleteAction.accept(getItem()));
        connectButton.setOnAction(e -> connectAction.accept(getItem()));
        disconnectButton.setOnAction(e -> disconnectAction.accept(getItem()));

        deleteMenu = new CustomMenuItem(deleteButton);
        connectMenu = new CustomMenuItem(connectButton);
        disConnectMenu = new CustomMenuItem(disconnectButton);

        ContextMenu contextMenu = new ContextMenu(connectMenu, deleteMenu);
        super.setContextMenu(contextMenu);
        super.setPadding(new Insets(5, 5, 5, 5));
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
        var nameLabel = new Label();
        nameLabel.textProperty().bind(serverNameBinding(item));

        hbox.setId(item.getZkUrl());
        hbox.getChildren().clear();
        hbox.getChildren().add(nameLabel);
        super.setGraphic(hbox);

        item.statusProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                initStatusIcon(newValue);
                initContextMenu(newValue);
                initStatusIndicator(newValue);
            }
        });

        initStatusIcon(item.getStatus());
        initContextMenu(item.getStatus());
        initStatusIndicator(item.getStatus());

        // change css
        super.selectedProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue) {
                updateItemSelectedCss(hbox);
            } else {
                updateItemUnSelectedCss(hbox);
            }
        }));

        if (super.isSelected()) {
            updateItemSelectedCss(hbox);
        } else {
            updateItemUnSelectedCss(hbox);
        }
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

    private void initContextMenu(ServerStatus newValue) {
        ObservableList<MenuItem> items = super.getContextMenu().getItems();
        items.clear();
        if (newValue == ServerStatus.CONNECTED) {
            items.add(disConnectMenu);
        } else if (newValue == ServerStatus.DISCONNECTED) {
            items.add(connectMenu);
            items.add(deleteMenu);
        } else if (newValue == ServerStatus.RECONNECTING) {
            items.add(disConnectMenu);
        }
    }

    private void initStatusIcon(ServerStatus newValue) {
        ObservableList<Node> children = hbox.getChildren();
        if (newValue == ServerStatus.CONNECTED) {
            children.remove(reconnectingSymbol);
            if (!children.contains(connectedSymbol)) {
                children.add(0, connectedSymbol);
            }
        } else if (newValue == ServerStatus.CONNECTING || newValue == ServerStatus.RECONNECTING) {
            children.remove(connectedSymbol);
            if (!children.contains(reconnectingSymbol)) {
                children.add(0, reconnectingSymbol);
            }
        } else if (newValue == ServerStatus.DISCONNECTED) {
            children.remove(connectedSymbol);
            children.remove(reconnectingSymbol);
        }
    }

    private void initStatusIndicator(ServerStatus newValue) {
        ObservableList<Node> children = hbox.getChildren();
        if (newValue == ServerStatus.CONNECTED) {
            children.removeIf(node -> node instanceof ProgressIndicator);
        } else if (newValue == ServerStatus.RECONNECTING) {
            if (children.stream().noneMatch(n -> n instanceof ProgressIndicator)) {
                children.add(statusIndicator);
            }
        } else if (newValue == ServerStatus.CONNECTING) {
            if (children.stream().noneMatch(n -> n instanceof ProgressIndicator)) {
                children.add(statusIndicator);
            }
        } else if (newValue == ServerStatus.DISCONNECTED) {
            children.removeIf(node -> node instanceof ProgressIndicator);
        }
    }

    private void updateItemSelectedCss(HBox hbox) {
        hbox.getStyleClass().add("server-item-select");
        statusIndicator.getStyleClass().removeAll("server-progress-indicator");
        statusIndicator.getStyleClass().addAll("server-select-progress-indicator");
        hbox.getStyleClass().remove("server-item");
    }

    private void updateItemUnSelectedCss(HBox hbox) {
        hbox.getStyleClass().remove("server-item-select");
        statusIndicator.getStyleClass().removeAll("server-select-progress-indicator");
        statusIndicator.getStyleClass().addAll("server-progress-indicator");
        hbox.getStyleClass().add("server-item");
    }
}
