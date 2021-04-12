package cc.cc1234.app.validator;

import com.jfoenix.validation.base.ValidatorBase;
import javafx.scene.control.TextInputControl;

public class NotNullValidator extends ValidatorBase {

    @Override
    protected void eval() {
        if (srcControl.get() instanceof TextInputControl) {
            TextInputControl textField = (TextInputControl) srcControl.get();
            if (textField.getText() == null) {
                hasErrors.set(true);
            } else {
                hasErrors.set(false);
            }
        }
    }

}
