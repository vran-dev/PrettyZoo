package cc.cc1234.app.controller;

import cc.cc1234.app.context.ActiveServerContext;
import cc.cc1234.app.facade.PrettyZooFacade;
import cc.cc1234.app.util.Transitions;
import cc.cc1234.app.util.VToast;
import cc.cc1234.spi.node.ZkNode;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;

public class NodeInfoViewController {

    @FXML
    private AnchorPane nodeInfoPane;

    @FXML
    private TextField cZxidField;

    @FXML
    private TextField pZxidField;

    @FXML
    private TextField mtimeField;

    @FXML
    private TextField ephemeralOwnerField;

    @FXML
    private TextField ctimeField;

    @FXML
    private TextField mZxidField;

    @FXML
    private TextField dataLengthField;

    @FXML
    private TextField numChildrenField;

    @FXML
    private TextField dataVersionField;

    @FXML
    private TextField aclVersionField;

    @FXML
    private TextField cVersionField;

    @FXML
    private TextField pathField;

    @FXML
    private TextArea dataField;

    @FXML
    private Button nodeUpdateButton;

    @FXML
    private PrettyZooFacade prettyZooFacade = new PrettyZooFacade();

    @FXML
    private void initialize() {
        nodeUpdateButton.setOnMouseClicked(e -> {
            final String path = pathField.getText();
            if (!ActiveServerContext.exists()) {
                VToast.error("Error: connect zookeeper first");
                return;
            }
            if (!prettyZooFacade.nodeExists(path)) {
                VToast.error("Node not exists");
                return;
            }
            final String data = dataField.getText();
            prettyZooFacade.updateData(path, data, ex -> VToast.error(ex.getMessage()));
            VToast.info("update success");
        });
    }

    public void show(StackPane parent) {
        show(parent, null);
    }


    public void show(StackPane parent, ZkNode zkNode) {

        if (!parent.getChildren().contains(nodeInfoPane)) {
            parent.getChildren().add(nodeInfoPane);
        }

        if (zkNode == null) {
            resetTextField();
        } else {
            initTextField(zkNode);
        }
    }


    private void initTextField(ZkNode node) {
        dataField.setText(node.getData());
        ephemeralOwnerField.setText(String.valueOf(node.getEphemeralOwner()));
        cZxidField.setText(String.valueOf(node.getCzxid()));
        mtimeField.setText(String.valueOf(node.getMtime()));
        pZxidField.setText(String.valueOf(node.getPzxid()));
        ctimeField.setText(String.valueOf(node.getCtime()));
        mZxidField.setText(String.valueOf(node.getMzxid()));
        dataLengthField.setText(String.valueOf(node.getDataLength()));
        numChildrenField.setText(String.valueOf(node.getNumChildren()));
        dataVersionField.setText(String.valueOf(node.getVersion()));
        aclVersionField.setText(String.valueOf(node.getAversion()));
        cVersionField.setText(String.valueOf(node.getCversion()));
        pathField.setText(node.getPath());
    }

    private void resetTextField() {
        dataField.clear();
        ephemeralOwnerField.clear();
        cZxidField.clear();
        mtimeField.clear();
        pZxidField.clear();
        ctimeField.clear();
        mZxidField.clear();
        dataLengthField.clear();
        numChildrenField.clear();
        dataVersionField.clear();
        aclVersionField.clear();
        cVersionField.clear();
        pathField.clear();
    }

}
