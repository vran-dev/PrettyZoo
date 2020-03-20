package cc.cc1234.app.listener;

import cc.cc1234.app.context.RecursiveModeContext;
import javafx.beans.value.ChangeListener;
import javafx.scene.control.Label;

public class JfxListenerManager {

    public static  ChangeListener<Boolean> getRecursiveModeChangeListener(Label prettyZooLabel) {
        return (observable, oldValue, newValue) -> {
            if (newValue) {
                prettyZooLabel.getStyleClass().remove(RecursiveModeContext.PRETTYZOO);
                prettyZooLabel.getStyleClass().add(RecursiveModeContext.PRETTYZOO_RECURSIVE);
            } else {
                prettyZooLabel.getStyleClass().remove(RecursiveModeContext.PRETTYZOO_RECURSIVE);
                prettyZooLabel.getStyleClass().add(RecursiveModeContext.PRETTYZOO);
            }
            RecursiveModeContext.change(newValue);
//            final ScaleTransition transition = Transitions.scale(prettyZooLabel);
//            transition.play();
        };
    }



}
