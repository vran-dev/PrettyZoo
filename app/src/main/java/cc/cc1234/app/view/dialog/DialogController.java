package cc.cc1234.app.view.dialog;

import cc.cc1234.app.context.RootPaneContext;
import com.jfoenix.controls.JFXDialog;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import org.slf4j.Logger;

import java.util.function.Consumer;

public class DialogController {

    private static final Logger log = org.slf4j.LoggerFactory.getLogger(DialogController.class);

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

    public void showReadonly(String title, String content, Runnable runnable) {
        contentTextArea.setPrefHeight(130);
        contentTextArea.setPrefWidth(320);
        contentTextArea.setText(content);
        contentTextArea.setEditable(false);
        initDialog(title, runnable);
    }

    public void showEditable(String title, String content, Consumer<String> runnable) {
        contentTextArea.setEditable(true);
        contentTextArea.setPrefHeight(-1);
        contentTextArea.setPrefWidth(-1);
        contentTextArea.setText(content);
        initDialog(title, runnable);
    }

    private void initDialog(String title, Runnable runnable) {
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

    private void initDialog(String title, Consumer<String> runnable) {
        titleLabel.setText(title);
        final JFXDialog dialog = new JFXDialog();
        dialog.setContent(dialogPane);
        dialog.show(RootPaneContext.get());
        cancelButton.setOnAction(e -> dialog.close());
        confirmButton.setOnAction(e -> {
            try {
                if (contentTextArea.getText() == null) {
                    runnable.accept("");
                } else {
                    runnable.accept(contentTextArea.getText());
                }
                dialog.close();
            } catch (Exception ex) {
                log.error("dialog confirm error", ex);
            }
        });
    }
}
