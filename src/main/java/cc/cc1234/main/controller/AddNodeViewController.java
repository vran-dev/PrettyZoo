package cc.cc1234.main.controller;

import cc.cc1234.main.util.PathUtils;
import cc.cc1234.main.vo.ZkNodeOperationVO;
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

    private Stage stage;

    private SimpleStringProperty parentPathProperty = new SimpleStringProperty();

    private ZkNodeOperationVO zkNodeOperationVO = new ZkNodeOperationVO();

    @FXML
    private void initialize() {
        this.stage = new Stage();
        this.stage.setScene(new Scene(addNodePane));
        this.stage.initModality(Modality.APPLICATION_MODAL);

        zkNodeOperationVO.relativePathProperty().bind(nodeNameTextField.textProperty());
        zkNodeOperationVO.ephProperty().bind(isNodeEph.selectedProperty());
        zkNodeOperationVO.seqProperty().bind(isNodeSeq.selectedProperty());
        zkNodeOperationVO.dataProperty().bind(nodeDataTextArea.textProperty());
        final StringBinding bind = Bindings.createStringBinding(() -> PathUtils.concat(parentPathProperty.get(), nodeNameTextField.getText()),
                parentPathProperty,
                nodeNameTextField.textProperty());
        zkNodeOperationVO.absolutePathProperty().bind(bind);
        currentPathLabel.textProperty().bind(zkNodeOperationVO.absolutePathProperty());
    }

    public void show(String parentPath) {
        parentPathProperty.set(parentPath);
        this.nodeNameTextField.setText("");
        this.isNodeEph.setSelected(false);
        this.isNodeSeq.setSelected(false);
        this.nodeDataTextArea.setText("");
        this.stage.show();
    }

    @FXML
    private void onNodeAddAction() {
        if (Strings.isNullOrEmpty(zkNodeOperationVO.getRelativePath())) {
            VToast.toastFailure(stage, "node must not be empty");
            return;
        }

        try {
            zkNodeOperationVO.onAdd();
        } catch (Exception e) {
            VToast.toastFailure(stage, e.getMessage());
            throw new IllegalStateException(e);
        }

        VToast.toastSuccess(stage);
        stage.close();
    }


}
