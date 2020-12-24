package cc.cc1234.app.view.dialog;

import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class DialogController {

    private Stage stage = new Stage(StageStyle.UNDECORATED);

    @FXML
    private AnchorPane dialogPane;

    @FXML
    private Button closeButton;

    @FXML
    private Button confirmButton;

    @FXML
    private Button cancelButton;

    @FXML
    private Label titleLabel;

    @FXML
    private TextArea contentTextArea;

    @FXML
    public void initialize() {
        final Scene scene = new Scene(dialogPane);
        stage.setScene(scene);
        stage.centerOnScreen();
        stage.initModality(Modality.APPLICATION_MODAL);
        contentTextArea.setWrapText(true);
        cancelButton.setOnAction(e -> stage.hide());
        closeButton.setOnAction(e -> stage.hide());
    }

    public void showAndWait(String title, String content, Runnable runnable) {
        titleLabel.setText(title);
        contentTextArea.setText(content);
        confirmButton.setOnAction(e -> {
            stage.hide();
            runnable.run();
        });
        stage.showAndWait();
    }
}
