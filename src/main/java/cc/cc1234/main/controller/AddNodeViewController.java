package cc.cc1234.main.controller;

import cc.cc1234.main.cache.RecursiveModeContext;
import cc.cc1234.main.util.PathUtils;
import com.google.common.base.Strings;
import javafx.beans.binding.Bindings;
import javafx.beans.binding.StringBinding;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CreateBuilder;
import org.apache.zookeeper.CreateMode;

import java.util.Objects;

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

    @FXML
    private AnchorPane addNodePane;

    private CuratorFramework client;

    private Stage stage;

    private SimpleStringProperty parentPath = new SimpleStringProperty();

    @FXML
    private void initialize() {
        this.stage = new Stage();
        this.stage.setScene(new Scene(addNodePane));
        this.stage.initModality(Modality.APPLICATION_MODAL);

        final StringBinding bind = Bindings.createStringBinding(() -> PathUtils.concat(parentPath.get(), nodeNameTextField.getText()),
                parentPath,
                nodeNameTextField.textProperty());
        currentPathLabel.textProperty().bind(bind);
    }

    public void show(String parentPath, CuratorFramework client) {
        this.client = client;
        this.parentPath.set(parentPath);
        this.nodeNameTextField.setText("");
        this.stage.show();
    }

    @FXML
    private void onNodeAddAction() {
        String path = currentPathLabel.getText();
        if (Strings.isNullOrEmpty(path) || Objects.equals(path, parentPath.get())) {
            VToast.toastFailure(stage, "node must not be empty");
            return;
        }
        final String nodeData = nodeDataTextArea.getText();
        final CreateBuilder createBuilder = client.create();
        try {
            // must use Platform to close stage
            if (RecursiveModeContext.get()) {
                createBuilder.creatingParentsIfNeeded()
                        .withMode(createMode())
                        .forPath(path, nodeData.getBytes());
            } else {
                createBuilder.withMode(createMode())
                        .forPath(path, nodeData.getBytes());
            }
            VToast.toastSuccess(stage);
            stage.close();
        } catch (Exception e) {
            VToast.toastFailure(stage, e.getMessage());
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
