package cc.cc1234.app.controller;

import cc.cc1234.app.context.ActiveServerContext;
import cc.cc1234.app.facade.PrettyZooFacade;
import cc.cc1234.app.fp.Try;
import cc.cc1234.app.util.PathConcat;
import cc.cc1234.app.view.NodeDataArea;
import cc.cc1234.app.view.toast.VToast;
import cc.cc1234.specification.node.NodeMode;
import cc.cc1234.specification.node.ZkNode;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.validation.RequiredFieldValidator;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.fxmisc.flowless.VirtualizedScrollPane;

public class NodeAddViewController {

    @FXML
    private JFXTextField nodeNameTextField;

    @FXML
    private CheckBox isNodeSeq;

    @FXML
    private CheckBox isNodeEph;

    @FXML
    private TextField currentPathField;

    @FXML
    private AnchorPane nodeAddPane;

    @FXML
    private Button cancelButton;

    @FXML
    private Button confirmButton;

    private NodeDataArea dataCodeArea = new NodeDataArea();

    private PrettyZooFacade prettyZooFacade = new PrettyZooFacade();

    @FXML
    private void initialize() {
        cancelButton.setOnMouseClicked(e -> hide());
        confirmButton.setOnMouseClicked(e -> onSave());

        var pane = new VirtualizedScrollPane<>(dataCodeArea);
        AnchorPane.setTopAnchor(pane, 175d);
        AnchorPane.setLeftAnchor(pane, 70d);
        AnchorPane.setRightAnchor(pane, 70d);
        AnchorPane.setBottomAnchor(pane, 55d);
        nodeAddPane.getChildren().add(pane);
        nodeNameTextField.setValidators(new RequiredFieldValidator("Required and must not be Empty"));
    }

    public void show(StackPane parent) {
        show(parent, null);
    }

    public void show(StackPane parent, ZkNode zkNode) {
        nodeNameTextField.resetValidation();
        if (!parent.getChildren().contains(nodeAddPane)) {
            parent.getChildren().add(nodeAddPane);
        }
        String parentPath = zkNode == null ? "/" : zkNode.getPath();
        currentPathField.setText(parentPath);
        nodeNameTextField.requestFocus();
    }

    public void hide() {
        final StackPane parent = (StackPane) nodeAddPane.getParent();
        if (parent != null && parent.getChildren().contains(nodeAddPane)) {
            parent.getChildren().remove(nodeAddPane);
        }
    }

    public void onSave() {
        if (nodeNameTextField.validate()) {
            final NodeMode mode = createMode();
            String path = PathConcat.concat(currentPathField.getText(), nodeNameTextField.getText());
            String data = dataCodeArea.getText();
            Try.of(() -> prettyZooFacade.createNode(ActiveServerContext.get(), path, data, mode))
                    .onSuccess(r -> {
                        hide();
                        VToast.info("success");
                    })
                    .onFailure(e -> VToast.error(e.getMessage()));
        }
    }

    private NodeMode createMode() {
        if (isNodeEph.isSelected() && isNodeSeq.isSelected()) {
            return NodeMode.EPHEMERAL_SEQUENTIAL;
        }

        if (isNodeSeq.isSelected()) {
            return NodeMode.PERSISTENT_SEQUENTIAL;
        }

        if (isNodeEph.isSelected()) {
            return NodeMode.EPHEMERAL;
        }

        // TODO  how to support CreateMode.CONTAINER ?
        return NodeMode.PERSISTENT;
    }
}
