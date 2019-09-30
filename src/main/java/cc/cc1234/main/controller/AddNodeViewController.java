package cc.cc1234.main.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
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
    private Label parentPathLabel;

    private CuratorFramework curatorFramework;

    private Stage stage;

    public static void initController(String parentPath, CuratorFramework curatorFramework) throws IOException {
        String fxml = "AddNodeView.fxml";
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(AddNodeViewController.class.getResource(fxml));
        AnchorPane panel = loader.load();
        final AddNodeViewController controller = loader.getController();
        controller.setCuratorFramework(curatorFramework);
        controller.setParentPath(parentPath);

        final Scene scene = new Scene(panel);
        final Stage stage = new Stage();
        stage.setScene(scene);
        controller.setStage(stage);
        stage.showAndWait();
    }

    public void setParentPath(String parentPath) {
        this.parentPathLabel.setText(parentPath);
    }

    public void setStage(Stage stage) {
        this.stage = stage;
    }

    public void setCuratorFramework(CuratorFramework curatorFramework) {
        this.curatorFramework = curatorFramework;
    }

    @FXML
    private void onNodeAddAction() {
        final String nodeName = nodeNameTextField.getText();
        final String nodeData = nodeDataTextArea.getText();
        try {
            String path = parentPathLabel.getText() + "/" + nodeName;
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
