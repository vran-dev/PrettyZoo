package cc.cc1234.app.view.transitions;

import javafx.animation.*;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.shape.HLineTo;
import javafx.scene.shape.MoveTo;
import javafx.scene.shape.Path;
import javafx.util.Duration;

public class Transitions {

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
                                        boolean autoReverse) {
        return scale(node, duration, toX, toY, cycleCount, autoReverse, e -> {
        });
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
        final KeyValue fadeInKey = new KeyValue(node.opacityProperty(), node.opacityProperty().doubleValue());
        KeyFrame fadeIn = new KeyFrame(Duration.millis(fadeInDelay), fadeInKey);
        fadeInTimeline.getKeyFrames().add(fadeIn);
        fadeInTimeline.setOnFinished((ae) -> {
            Timeline fadeOutTimeline = new Timeline();
            final KeyValue fadeOutKey = new KeyValue(node.opacityProperty(), 0);
            KeyFrame fadeOut = new KeyFrame(Duration.millis(fadeOutDelay), fadeOutKey);
            fadeOutTimeline.getKeyFrames().add(fadeOut);
            fadeOutTimeline.setOnFinished(finishedEvent);

            node.setOnMouseEntered(event -> fadeOutTimeline.pause());
            node.setOnMouseExited(event -> fadeOutTimeline.play());
            fadeOutTimeline.playFromStart();
        });

        node.setOnMouseEntered(event -> fadeInTimeline.pause());
        node.setOnMouseExited(event -> fadeInTimeline.play());
        fadeInTimeline.playFromStart();
    }

    public static void move(Node node,
                            double moveX, double moveY,
                            EventHandler<ActionEvent> callback) {
        move(node, 0, 0, moveX, moveY, callback);
    }

    public static void move(Node node,
                            double startX, double startY,
                            double moveX, double moveY,
                            EventHandler<ActionEvent> callback) {


        final double x = node.getParent().getLayoutX();
        final double y = node.getParent().getLayoutY();

        final Path path = new Path();
        final MoveTo from = new MoveTo(x, y);
        path.getElements().add(from);

        final HLineTo to = new HLineTo(80);
        to.setAbsolute(false);
        path.getElements().add(to);

        PathTransition transition = new PathTransition();
        transition.setDuration(Duration.millis(300));
        transition.setNode(node);
        transition.setPath(path);
        transition.setAutoReverse(false);
        transition.setOnFinished(callback);
        transition.play();
    }

    public static ScaleTransition zoomInY(Node node) {
        return zoom(node,
                Duration.millis(300),
                0.6,
                1,
                1,
                1,
                1,
                false,
                e -> {
                }
        );
    }

    public static ScaleTransition zoomOutY(Node node, EventHandler<ActionEvent> finishedEvent) {
        return zoom(node,
                Duration.millis(500),
                1,
                1,
                0,
                1,
                1,
                false,
                finishedEvent
        );
    }

    public static ScaleTransition zoomIn(Node node) {
        return zoomIn(node, e -> {
        });
    }

    public static ScaleTransition zoomIn(Node node, EventHandler<ActionEvent> finishedEvent) {
        return zoom(node, Duration.millis(500), 0, 0, 1, 1, 1, false, finishedEvent);
    }

    public static ScaleTransition zoomOut(Node node, EventHandler<ActionEvent> finishedEvent) {
        return zoom(node, Duration.millis(500), 1, 1, 0, 0, 1, false, finishedEvent);
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
}
