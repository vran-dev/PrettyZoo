package cc.cc1234.app.controller;

import cc.cc1234.app.context.PrimaryStageContext;
import cc.cc1234.app.vo.Transitions;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.stage.Window;

public class VToast {

    public static void info(String message) {
        info(PrimaryStageContext.get(), message);
    }

    public static void error(String message) {
        error(PrimaryStageContext.get(), message);
    }

    public static void info(Window parent) {
        toast(parent, createNotification("√ success", ToastType.INFO));
    }

    public static void info(Window parent, String message) {

        toast(parent, createNotification(message, ToastType.INFO));
    }

    public static void error(Window parent, String message) {
        toast(parent, createNotification(message, ToastType.ERROR));
    }


    private static void toast(Window parent, Region panel) {
        toast(parent, panel, 1000, 3000);
    }

    private static void toast(Window parent, Region node, int fadeInDelay, int fadeOutDelay) {
        showNotification(parent, node);
        double x = parent.getWidth() - 20 - node.getWidth() / 2;
        double y = node.getHeight() / 2 + 250;
        Transitions.move(node, x, y, x, y - 240, e -> {
            Transitions.fade(node, fadeInDelay, fadeOutDelay, fadeEvent -> {
                removeNotification(parent, node);
            });
        });

    }


    private static void showNotification(Window parent, Region node) {
        Pane root = (Pane) parent.getScene().getRoot();
        root.getChildren().add(node);
    }

    private static void removeNotification(Window parent, Region node) {
        Pane root = (Pane) parent.getScene().getRoot();
        root.getChildren().remove(node);
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
        text.setWrappingWidth(170);
        text.setTextAlignment(TextAlignment.CENTER);
        text.setFont(Font.font("Verdana", 14));
        text.setFill(Color.WHITE);

        HBox hBox = new HBox(8, label, text);
        hBox.setStyle("-fx-background-radius: 3; " +
                "-fx-background-color: " + type.getColor() + ";" +
                "-fx-opacity: 0.7");
        hBox.setPrefHeight(-1);
        hBox.setMinHeight(38d);
        hBox.setMaxHeight(-1);
        hBox.autosize();

        hBox.setPrefWidth(-1);
        hBox.setMinWidth(180);
        hBox.setMaxWidth(250);
        hBox.setAlignment(Pos.CENTER);
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
