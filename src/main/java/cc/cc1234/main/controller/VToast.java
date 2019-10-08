package cc.cc1234.main.controller;

import cc.cc1234.main.context.ApplicationContext;
import cc.cc1234.main.util.Transitions;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;

public class VToast {

    public static void toastSuccess(String message) {
        toastSuccess(ApplicationContext.get().getPrimaryStage(), message);
    }

    public static void toastFailure(String message) {
        toastFailure(ApplicationContext.get().getPrimaryStage(), message);
    }

    public static void toastSuccess(Window parent) {
        toast(parent, successPanel("√ success"));
    }

    public static void toastFailure(Window parent) {
        toast(parent, failurePanel("× failure"));
    }

    public static void toastSuccess(Window parent, String message) {
        toast(parent, successPanel(message));
    }

    public static void toastFailure(Window parent, String message) {
        toast(parent, failurePanel(message));
    }

    public static void toastInfo(Window parent, String message) {
        toast(parent, infoPanel(message));
    }

    private static void toast(Window parent, StackPane panel) {
        toast(parent, panel, 1000, 3000);
    }

    private static void toast(Window parent, StackPane root, int fadeInDelay, int fadeOutDelay) {
        final Stage toastStage = initToastStage(parent, root);
        toastStage.show();
        Transitions.fade(root, fadeInDelay, fadeOutDelay, e -> toastStage.close());
    }

    private static StackPane successPanel(String message) {
        Text text = new Text(message);
        text.setFont(Font.font("Verdana", 16));
        text.setFill(Color.WHITE);

        StackPane root = new StackPane(text);
        root.setStyle("-fx-background-radius: 20; -fx-background-color: #1e88e5; -fx-padding: 6px;");
        root.setOpacity(0);
        return root;
    }

    private static StackPane failurePanel(String message) {
        Text text = new Text(message);
        text.setFont(Font.font("Verdana", 16));
        text.setFill(Color.WHITE);

        StackPane root = new StackPane(text);
        root.setStyle("-fx-background-radius: 20; -fx-background-color: #d81b60; -fx-padding: 6px;");
        root.setOpacity(0);
        return root;
    }

    private static StackPane infoPanel(String message) {
        Text text = new Text(message);
        text.setFont(Font.font("Verdana", 16));
        text.setFill(Color.WHITE);

        StackPane root = new StackPane(text);
        root.setStyle("-fx-background-radius: 20; -fx-background-color: #66bb6a; -fx-padding: 6px;");
        root.setOpacity(0);
        return root;
    }

    private static Stage initToastStage(Window parent, StackPane root) {
        Stage toastStage = new Stage();
        toastStage.initOwner(parent);
        toastStage.setResizable(false);
        toastStage.initStyle(StageStyle.TRANSPARENT);

        // set position
        toastStage.setX(parent.getX());
        toastStage.setY(parent.getY() + parent.getScene().getY() + 10);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        toastStage.setScene(scene);
        return toastStage;
    }

}
