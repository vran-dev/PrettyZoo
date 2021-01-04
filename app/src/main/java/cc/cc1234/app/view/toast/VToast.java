package cc.cc1234.app.view.toast;

import cc.cc1234.app.context.RootPaneContext;
import cc.cc1234.app.view.transitions.Transitions;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;

public class VToast {

    public static void error(String message) {
        error(RootPaneContext.get(), message);
    }

    public static void error(StackPane pane, String message) {
        notification(pane, message, ToastType.ERROR);

    }

    public static void info(String message) {
        info(RootPaneContext.get(), message);
    }

    public static void info(StackPane pane, String message) {
        notification(pane, message, ToastType.INFO);
    }

    private static void notification(StackPane pane, String message, ToastType type) {
        final Region notification = createNotification(message, type);
        pane.getChildren().add(notification);
        Transitions.fade(notification, 1000, 3000, fadeEvent -> {
            pane.getChildren().remove(notification);
        });
    }

    private static Region createNotification(String message, ToastType type) {
        Label label = new Label();
        label.setStyle("-fx-background-image: url('" + type.getIcon() + "');" +
                "-fx-background-repeat: no-repeat;" +
                "-fx-background-position: center;" +
                "-fx-background-size: 25;" +
                "-fx-start-margin: 10;");
        label.setPrefHeight(30d);
        label.setPrefWidth(30d);

        Text text = new Text(message);
        text.setWrappingWidth(200);
        text.setTextAlignment(TextAlignment.CENTER);
        text.setFont(Font.font("Verdana", 13));
        text.setFill(Color.WHITE);

        HBox hBox = new HBox(2, label, text);
        hBox.setStyle("-fx-background-radius: 3; " +
                "-fx-background-color: " + type.getColor() + ";" +
                "-fx-opacity: 0.7");
        hBox.setPrefHeight(20);
        hBox.setMinHeight(20);
        hBox.setMaxHeight(60);
        hBox.setPadding(new Insets(3, 3, 3, 6));
        hBox.autosize();

        hBox.setPrefWidth(-1);
        hBox.setMinWidth(150);
        hBox.setMaxWidth(250);

        hBox.setAlignment(Pos.CENTER_LEFT);
        return hBox;
    }

    public enum ToastType {
        ERROR {
            @Override
            String getIcon() {
                return "assets/img/notification/error.png";
            }

            @Override
            String getColor() {
                return "#BB401E";
            }
        },
        INFO {
            @Override
            String getIcon() {
                return "assets/img/notification/ok.png";
            }

            @Override
            String getColor() {
                return "#469F95";
            }
        };

        abstract String getIcon();

        abstract String getColor();
    }
}
