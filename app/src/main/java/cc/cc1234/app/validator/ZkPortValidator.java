package cc.cc1234.app.validator;

import com.jfoenix.validation.base.ValidatorBase;
import javafx.scene.control.TextInputControl;

public class ZkPortValidator extends ValidatorBase {

    @Override
    protected void eval() {
        if (srcControl.get() instanceof TextInputControl) {
            TextInputControl textField = (TextInputControl) srcControl.get();
            String portStr = textField.getText();
            try {
                var port = Integer.parseInt(portStr);
                if (port < 1 || port > 65535) {
                    markAsErrors();
                } else {
                    hasErrors.set(false);
                }
            } catch (NumberFormatException e) {
                markAsErrors();
            }
        }
    }

    private void markAsErrors() {
        hasErrors.set(true);
        setMessage("1~65535");
    }
}
