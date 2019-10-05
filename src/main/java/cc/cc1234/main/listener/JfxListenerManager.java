package cc.cc1234.main.listener;

import cc.cc1234.main.cache.RecursiveModeContext;
import cc.cc1234.main.util.Transitions;
import javafx.animation.ScaleTransition;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Label;
import javafx.scene.layout.AnchorPane;

public class JfxListenerManager {

    public static ChangeListener<Boolean> getRecursiveModeChangeListener(Label prettyZooLabel, AnchorPane serverViewMenuItems) {
        return (observable, oldValue, newValue) -> {
            if (newValue) {
                prettyZooLabel.getStyleClass().remove(RecursiveModeContext.PRETTYZOO);
                prettyZooLabel.getStyleClass().add(RecursiveModeContext.PRETTYZOO_RECURSIVE);
            } else {
                prettyZooLabel.getStyleClass().remove(RecursiveModeContext.PRETTYZOO_RECURSIVE);
                prettyZooLabel.getStyleClass().add(RecursiveModeContext.PRETTYZOO);
            }
            RecursiveModeContext.change(newValue);
            final ScaleTransition transition = Transitions.scale(prettyZooLabel);
            transition.play();
            serverViewMenuItems.setVisible(false);
        };
    }



}
