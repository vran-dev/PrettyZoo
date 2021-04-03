package cc.cc1234.app.view.dialog;

import cc.cc1234.app.context.RootPaneContext;
import com.jfoenix.controls.JFXDialog;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;

public class DialogController {

    @FXML
    private AnchorPane dialogPane;

    @FXML
    private Label titleLabel;

    @FXML
    private Button confirmButton;

    @FXML
    private Button cancelButton;

    @FXML
    private TextArea contentTextArea;

    @FXML
    public void initialize() {
        contentTextArea.setWrapText(true);
    }

    public void showAndWait(String title, String content, Runnable runnable) {
        contentTextArea.setText(content);
        titleLabel.setText(title);
        final JFXDialog dialog = new JFXDialog();
        dialog.setContent(dialogPane);
        dialog.show(RootPaneContext.get());
        cancelButton.setOnAction(e -> dialog.close());
        confirmButton.setOnAction(e -> {
            runnable.run();
            dialog.close();
        });
    }
}
