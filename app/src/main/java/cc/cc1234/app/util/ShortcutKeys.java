package cc.cc1234.app.util;

import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyCodeCombination;

public enum ShortcutKeys {

    NEW_SERVER {
        @Override
        public KeyCodeCombination key() {
            if (isMac()) {
                return new KeyCodeCombination(KeyCode.N, KeyCodeCombination.META_DOWN);
            } else {
                return new KeyCodeCombination(KeyCode.N, KeyCodeCombination.CONTROL_DOWN);
            }
        }
    };

    public abstract KeyCodeCombination key();

    boolean isMac() {
        return System.getProperty("os.name").toLowerCase().contains("mac");
    }

}
