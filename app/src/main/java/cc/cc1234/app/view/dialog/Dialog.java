package cc.cc1234.app.view.dialog;

import cc.cc1234.app.util.FXMLs;

import java.util.function.Consumer;

public class Dialog {

    private static final DialogController dialogController = FXMLs.getController("fxml/Dialog.fxml");

    public static void confirm(String title,
                               String content,
                               Runnable confirmAction) {
        dialogController.showReadonly(title, content, confirmAction);
    }

    public static void confirmEditable(String title,
                                       String content,
                                       Consumer<String> confirmAction) {
        DialogController controller = FXMLs.getController("fxml/Dialog.fxml");
        controller.showEditable(title, content, confirmAction);
    }
}
