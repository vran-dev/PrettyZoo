package cc.cc1234.app.dialog;

import cc.cc1234.app.util.FXMLs;

public class Dialog {

    private static final DialogController dialogController = FXMLs.getController("fxml/Dialog.fxml");

    public static void confirm(String title,
                               String content,
                               Runnable confirmAction) {
        dialogController.showAndWait(title, content, confirmAction);
    }
}
