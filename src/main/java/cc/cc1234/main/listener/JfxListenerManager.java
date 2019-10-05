package cc.cc1234.main.listener;

import cc.cc1234.main.cache.RecursiveModeContext;
import javafx.animation.ScaleTransition;
import javafx.beans.value.ChangeListener;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;
import javafx.util.Duration;

public class JfxListenerManager {

    public static ChangeListener<Boolean> getRecursiveModeCheckChangeListener(Label prettyZooLabel, AnchorPane serverViewMenuItems) {
        return (observable, oldValue, newValue) -> {
            if (newValue) {
                prettyZooLabel.getStyleClass().remove(RecursiveModeContext.PRETTYZOO);
                prettyZooLabel.getStyleClass().add(RecursiveModeContext.PRETTYZOO_RECURSIVE);
            } else {
                prettyZooLabel.getStyleClass().remove(RecursiveModeContext.PRETTYZOO_RECURSIVE);
                prettyZooLabel.getStyleClass().add(RecursiveModeContext.PRETTYZOO);
            }
            RecursiveModeContext.change(newValue);
            final ScaleTransition transition = transition(prettyZooLabel);
            transition.play();
            serverViewMenuItems.setVisible(false);
        };
    }

    private static ScaleTransition transition(Node node) {
        final ScaleTransition transition  = new ScaleTransition(Duration.seconds(1), node);
        transition.setToX(1.1);
        transition.setToY(1.3);
        transition.setCycleCount(2);
        transition.setAutoReverse(true);
        return transition;
    }

}
