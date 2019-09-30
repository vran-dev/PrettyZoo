package cc.cc1234.main.controller;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.scene.Scene;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class VToast {

    public static void toastSuccess(Stage primaryStage) {
        toast(primaryStage, successPanel("√ success"));
    }

    public static void toastFailure(Stage primaryStage) {

        toast(primaryStage, failurePanel("× failure"));
    }

    public static void toast(Stage primaryStage, StackPane panel) {
        toast(primaryStage, panel, 1000, 3000);
    }

    public static void toast(Stage primaryStage, StackPane root, int fadeInDelay, int fadeOutDelay) {
        final Stage toastStage = initToastStage(primaryStage, root);
        toastStage.show();
        initAnimation(toastStage, fadeInDelay, fadeOutDelay);
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

    private static Stage initToastStage(Stage primaryStage, StackPane root) {
        Stage toastStage = new Stage();
        toastStage.initOwner(primaryStage);
        toastStage.setResizable(false);
        toastStage.initStyle(StageStyle.TRANSPARENT);

        // set position
        toastStage.setX(primaryStage.getX());
        toastStage.setY(primaryStage.getY() + primaryStage.getScene().getY() + 10);

        Scene scene = new Scene(root);
        scene.setFill(Color.TRANSPARENT);
        toastStage.setScene(scene);
        return toastStage;
    }

    private static void initAnimation(Stage toastStage, int fadeInDelay, int fadeOutDelay) {
        Timeline fadeInTimeline = new Timeline();
        final KeyValue fadeInKey = new KeyValue(toastStage.getScene().getRoot().opacityProperty(), 1);
        KeyFrame fadeIn = new KeyFrame(Duration.millis(fadeInDelay), fadeInKey);
        fadeInTimeline.getKeyFrames().add(fadeIn);

        fadeInTimeline.setOnFinished((ae) -> {
            Timeline fadeOutTimeline = new Timeline();
            final KeyValue fadeOutKey = new KeyValue(toastStage.getScene().getRoot().opacityProperty(), 0);
            KeyFrame fadeOut = new KeyFrame(Duration.millis(fadeOutDelay), fadeOutKey);
            fadeOutTimeline.getKeyFrames().add(fadeOut);
            fadeOutTimeline.setOnFinished((aeb) -> toastStage.close());
            fadeOutTimeline.play();
        });
        fadeInTimeline.play();
    }

}
