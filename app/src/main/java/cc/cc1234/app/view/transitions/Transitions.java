package cc.cc1234.app.view.transitions;

import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.util.Duration;

public class Transitions {

    public static ScaleTransition zoomIn(Node node) {
        return zoomIn(node, e -> {
        });
    }

    public static ScaleTransition zoomIn(Node node, EventHandler<ActionEvent> finishedEvent) {
        return zoom(node, Duration.millis(500), 0, 0, 1, 1, 1, false, finishedEvent);
    }

    public static ScaleTransition zoom(Node node,
                                       Duration duration,
                                       double fromX,
                                       double fromY,
                                       double toX,
                                       double toY,
                                       int cycleCount,
                                       boolean autoReverse,
                                       EventHandler<ActionEvent> finishedEvent) {
        final ScaleTransition transition = new ScaleTransition(duration, node);
        transition.setFromX(fromX);
        transition.setFromY(fromY);
        transition.setToX(toX);
        transition.setToY(toY);
        transition.setCycleCount(cycleCount);
        transition.setAutoReverse(autoReverse);
        transition.setOnFinished(finishedEvent);
        return transition;
    }

    public static ScaleTransition zoomInLittleAndReverse(Node node) {
        return zoom(node, Duration.millis(500), 1, 1, 1.2, 1.2, 2, true, e -> {
        });
    }

    public static void rotate(Node node, Runnable runnable) {
        RotateTransition transition = new RotateTransition(Duration.millis(400), node);
        transition.setFromAngle(0);
        transition.setToAngle(360);
        transition.setAutoReverse(true);
        transition.setOnFinished(e -> runnable.run());
        transition.playFromStart();
    }
}
