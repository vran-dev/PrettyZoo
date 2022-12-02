package cc.cc1234.app.validator;

import com.jfoenix.validation.base.ValidatorBase;
import javafx.scene.control.TextInputControl;

public class StringNotBlankValidator extends ValidatorBase {

    @Override
    protected void eval() {
        if (srcControl.get() instanceof TextInputControl) {
            TextInputControl textField = (TextInputControl) srcControl.get();
            String alias = textField.getText();
            if (alias == null || alias.isBlank()) {
                hasErrors.set(true);
                setMessage("value required");
            } else {
                hasErrors.set(false);
            }
        }
    }
}
