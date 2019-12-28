package cc.cc1234.app.vo;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.util.Duration;

public class Transitions {

    public static RotateTransition rotate(Node node) {
        return rotate(node, 720);
    }

    public static RotateTransition rotate(Node node, int angle) {
        final RotateTransition rotateTransition = new RotateTransition(Duration.millis(1000), node);
        rotateTransition.setFromAngle(0);
        rotateTransition.setToAngle(angle);
        rotateTransition.setCycleCount(2);
        rotateTransition.setInterpolator(Interpolator.LINEAR);
        rotateTransition.setAutoReverse(true);
        return rotateTransition;
    }

    public static void scaleAndRotate(Node node) {
        scale(node, Duration.millis(200), e -> rotate(node, 360).play()).play();
    }


    public static ScaleTransition scale(Node node) {
        return scale(node, Duration.millis(800), null);
    }

    public static ScaleTransition scale(Node node, Duration duration, EventHandler<ActionEvent> finishedEvent) {
        return scale(node, duration, 0.8, 0.8, 2, true, finishedEvent);
    }

    public static ScaleTransition scale(Node node,
                                        Duration duration,
                                        double toX,
                                        double toY,
                                        int cycleCount,
                                        boolean autoReverse,
                                        EventHandler<ActionEvent> finishedEvent) {
        final ScaleTransition transition = new ScaleTransition(duration, node);
        transition.setToX(toX);
        transition.setToY(toY);
        transition.setCycleCount(cycleCount);
        transition.setAutoReverse(autoReverse);
        transition.setOnFinished(finishedEvent);
        return transition;
    }

    public static void fade(Node node, int fadeInDelay, int fadeOutDelay, EventHandler<ActionEvent> finishedEvent) {
        Timeline fadeInTimeline = new Timeline();
        final KeyValue fadeInKey = new KeyValue(node.getScene().getRoot().opacityProperty(), 1);
        KeyFrame fadeIn = new KeyFrame(Duration.millis(fadeInDelay), fadeInKey);
        fadeInTimeline.getKeyFrames().add(fadeIn);
        fadeInTimeline.setOnFinished((ae) -> {
            Timeline fadeOutTimeline = new Timeline();
            final KeyValue fadeOutKey = new KeyValue(node.getScene().getRoot().opacityProperty(), 0);
            KeyFrame fadeOut = new KeyFrame(Duration.millis(fadeOutDelay), fadeOutKey);
            fadeOutTimeline.getKeyFrames().add(fadeOut);
            fadeOutTimeline.setOnFinished(finishedEvent);
            fadeOutTimeline.play();
        });
        fadeInTimeline.play();
    }
}
