package cc.cc1234.main.controller;

import cc.cc1234.main.util.PathUtils;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.curator.framework.CuratorFramework;
import org.apache.zookeeper.CreateMode;

import java.io.IOException;

public class AddNodeViewController {

    @FXML
    private TextField nodeNameTextField;

    @FXML
    private TextArea nodeDataTextArea;

    @FXML
    private CheckBox isNodeSeq;

    @FXML
    private CheckBox isNodeEph;

    @FXML
    private Label currentPathLabel;

    private CuratorFramework curatorFramework;

    private Stage stage;

    private String parentPath;

    public static void initController(String parentPath, CuratorFramework curatorFramework) throws IOException {
        String fxml = "AddNodeView.fxml";
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(AddNodeViewController.class.getResource(fxml));
        AnchorPane panel = loader.load();
        final AddNodeViewController controller = loader.getController();
        controller.setCuratorFramework(curatorFramework);
        controller.setCurrentPath(parentPath);

        final Scene scene = new Scene(panel);
        final Stage stage = new Stage();
        stage.setScene(scene);
        stage.initModality(Modality.APPLICATION_MODAL);
        controller.setStage(stage);
        stage.show();
    }

    @FXML
    private void initialize() {
        this.nodeNameTextField.addEventHandler(KeyEvent.KEY_RELEASED, event -> {
            TextField textField = (TextField) event.getSource();
            final String input = textField.getText();
            if (input != null && !input.trim().equals("")) {
                currentPathLabel.setText(PathUtils.concat(parentPath, input));
            } else {
                currentPathLabel.setText(parentPath);
            }
        });

    }

    public void setCurrentPath(String parentPath) {
        this.currentPathLabel.setText(parentPath);
        this.parentPath = parentPath;
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setCuratorFramework(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
    }

    @FXML
    private void onNodeAddAction() {
        final String nodeData = nodeDataTextArea.getText();
        try {
            String path = currentPathLabel.getText();
            curatorFramework.create()
                    .withMode(createMode())
                    // must use Platform to close stage
                    .inBackground((client, event) -> {
                        Platform.runLater(() -> stage.close());
                    })
                    .forPath(path, nodeData.getBytes());
        } catch (Exception e) {
            VToast.toastFailure(stage);
            throw new IllegalStateException(e);
        }
    }

    private CreateMode createMode() {
        if (isNodeSeq.isSelected() && isNodeEph.isSelected()) {
            return CreateMode.EPHEMERAL_SEQUENTIAL;
        }

        if (isNodeSeq.isSelected()) {
            return CreateMode.PERSISTENT_SEQUENTIAL;
        }

        if (isNodeEph.isSelected()) {
            return CreateMode.EPHEMERAL;
        }

        // TODO  how to support CreateMode.CONTAINER ?
        return CreateMode.PERSISTENT;
    }
}
