package cc.cc1234.app.view.cell;

import cc.cc1234.app.vo.ServerConfigurationVO;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;

import java.util.Collection;

public class ZkServerListCell extends ListCell<ServerConfigurationVO> {

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
                    Platform.runLater(() -> {
                        switch (newValue) {
                            case CONNECTING:
                                addIfNecessary(hBox.getChildren(), progressIndicator);
                                break;
                            case DISCONNECTED:
                            case CONNECTED:
                                removeIfNecessary(hBox.getChildren(), progressIndicator);
                                break;
                        }
                    });
                }
            });
            item.connectedProperty().addListener(((observable, oldValue, newValue) -> {
                Platform.runLater(() -> {
                    if (newValue != null) {
                        if (newValue) {
                            hBox.getChildren().add(0, symbolImage);
                        } else {
                            hBox.getChildren().remove(symbolImage);
                        }
                    }
                });
            }));
            setGraphic(hBox);
        }
    }

    private StringBinding serverNameBinding(ServerConfigurationVO item) {
        return Bindings.createStringBinding(() -> serverNameFormat(item), item.zkServerProperty(), item.zkAliasProperty(), item.connectedProperty());
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

    private <T> void addIfNecessary(Collection<T> collection, T value) {
        if (!collection.contains(value)) {
            collection.add(value);
        }
    }

    private <T> void removeIfNecessary(Collection<T> collection, T value) {
        collection.remove(value);
    }

}
