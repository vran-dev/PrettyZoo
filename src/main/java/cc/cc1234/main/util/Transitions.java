package cc.cc1234.main.util;

import javafx.animation.Interpolator;
import javafx.animation.RotateTransition;
import javafx.animation.ScaleTransition;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.util.Duration;

public class Transitions {

    public static RotateTransition rotate(Node node) {
        final RotateTransition rotateTransition = new RotateTransition(Duration.millis(1000), node);
        rotateTransition.setFromAngle(0);
        rotateTransition.setToAngle(720);
        rotateTransition.setCycleCount(2);
        rotateTransition.setInterpolator(Interpolator.LINEAR);
        rotateTransition.setAutoReverse(true);
        return rotateTransition;
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

}
