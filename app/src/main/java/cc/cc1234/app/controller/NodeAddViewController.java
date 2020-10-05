package cc.cc1234.app.controller;

import cc.cc1234.app.context.ActiveServerContext;
import cc.cc1234.app.facade.PrettyZooFacade;
import cc.cc1234.app.util.PathConcat;
import cc.cc1234.app.util.Transitions;
import cc.cc1234.app.util.VToast;
import cc.cc1234.spi.node.NodeMode;
import cc.cc1234.spi.node.ZkNode;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

public class NodeAddViewController {

    @FXML
    private TextField nodeNameTextField;

    @FXML
    private CheckBox isNodeSeq;

    @FXML
    private CheckBox isNodeEph;

    @FXML
    private TextArea nodeDataTextArea;

    @FXML
    private TextField currentPathField;

    @FXML
    private AnchorPane nodeAddPane;

    @FXML
    private Button cancelButton;

    @FXML
    private Button confirmButton;

    private PrettyZooFacade prettyZooFacade = new PrettyZooFacade();


    @FXML
    private void initialize() {
        cancelButton.setOnMouseClicked(e -> hide());
        confirmButton.setOnMouseClicked(e -> onSave());
    }

    public void show(StackPane parent) {
        show(parent, null);
    }

    public void show(StackPane parent, ZkNode zkNode) {
        if (!parent.getChildren().contains(nodeAddPane)) {
            parent.getChildren().add(nodeAddPane);
            Transitions.zoomInY(nodeAddPane).playFromStart();
        }
        String parentPath = zkNode == null ? "/" : zkNode.getPath();
        currentPathField.setText(parentPath);
    }

    public void hide() {
        Transitions
                .zoomOutY(nodeAddPane, event -> {
                    final StackPane parent = (StackPane) nodeAddPane.getParent();
                    if (parent != null && parent.getChildren().contains(nodeAddPane)) {
                        parent.getChildren().remove(nodeAddPane);
                    }
                })
                .playFromStart();
    }

    public void onSave() {
        String server = ActiveServerContext.get();
        final NodeMode mode = createMode();
        boolean recursive = true;
        String path = PathConcat.concat(currentPathField.getText(), nodeNameTextField.getText());
        String data = nodeDataTextArea.getText();
        try {
            prettyZooFacade.addNode(server, path, data, recursive, mode);
            hide();
            VToast.info("success");
        } catch (Exception e) {
            VToast.error(e.getMessage());
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
