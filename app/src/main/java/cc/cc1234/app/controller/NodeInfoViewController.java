package cc.cc1234.app.controller;

import cc.cc1234.app.context.ActiveServerContext;
import cc.cc1234.app.facade.PrettyZooFacade;
import cc.cc1234.app.util.Formatters;
import cc.cc1234.app.view.toast.VToast;
import cc.cc1234.app.view.transitions.Transitions;
import cc.cc1234.specification.node.ZkNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

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
    private Label mtimeLabel;

    @FXML
    private Label ctimeLabel;

    @FXML
    private Button jsonFormatButton;

    @FXML
    private Button rawFormatButton;

    @FXML
    private Button xmlFormatButton;

    @FXML
    private ChoiceBox<String> charsetChoice;

    @FXML
    private PrettyZooFacade prettyZooFacade = new PrettyZooFacade();

    private List<Button> formatButtons = List.of();

    @FXML
    private void initialize() {
        nodeUpdateButton.setOnMouseClicked(e -> onNodeUpdate());

        final Tooltip timeLabelTooltip = new Tooltip("Click to change format");
        mtimeLabel.setTooltip(timeLabelTooltip);
        ctimeLabel.setTooltip(timeLabelTooltip);
        mtimeLabel.setOnMouseClicked(e -> changeTimeFormat());
        ctimeLabel.setOnMouseClicked(e -> changeTimeFormat());
        jsonFormatButton.setOnAction(e -> dataJsonFormat());
        rawFormatButton.setOnAction(e -> dataRawFormat());
        xmlFormatButton.setOnAction(e -> dataXmlFormat());
        formatButtons = List.of(jsonFormatButton, xmlFormatButton, rawFormatButton);
        charsetChoice.getItems().addAll("UTF-8", "GBK", "GB2312", "ISO-8859-1", "UTF-16");
        charsetChoice.getSelectionModel().select("UTF-8");
        charsetChoice.getSelectionModel().selectedItemProperty().addListener((event, ov, nv) -> {
            if (nv == null) {
                return;
            }
            byte[] rawBytes = (byte[]) dataField.getProperties().get("rawBytes");
            if (rawBytes == null) {
                return;
            }
            try {
                dataField.setText(new String(rawBytes, nv));
            } catch (UnsupportedEncodingException unsupportedEncodingException) {
                VToast.error("Not supported Charset:"+nv);
            }
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
        switchFormatButton(rawFormatButton);
    }

    private void onNodeUpdate() {
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
        Transitions.rotate(nodeUpdateButton, () -> {
            prettyZooFacade.updateData(ActiveServerContext.get(), path, data, ex -> VToast.error(ex.getMessage()));
            dataField.getProperties().put("raw", data);
            VToast.info("update success");
        });
    }

    private void initTextField(ZkNode node) {
        dataField.setText(transformData(node));
        dataField.getProperties().put("raw", transformData(node));
        dataField.getProperties().put("rawBytes", node.getDataBytes());

        ephemeralOwnerField.setText(String.valueOf(node.getEphemeralOwner()));
        cZxidField.setText(String.valueOf(node.getCzxid()));
        pZxidField.setText(String.valueOf(node.getPzxid()));
        mZxidField.setText(String.valueOf(node.getMzxid()));
        dataLengthField.setText(String.valueOf(node.getDataLength()));
        numChildrenField.setText(String.valueOf(node.getNumChildren()));
        dataVersionField.setText(String.valueOf(node.getVersion()));
        aclVersionField.setText(String.valueOf(node.getAversion()));
        cVersionField.setText(String.valueOf(node.getCversion()));
        pathField.setText(node.getPath());

        mtimeField.getProperties().put("timestamp", node.getMtime());
        mtimeField.getProperties().put("dateTime", format(node.getMtime()));
        ctimeField.getProperties().put("timestamp", node.getCtime());
        ctimeField.getProperties().put("dateTime", format(node.getCtime()));
        showDateTime();
    }

    private String transformData(ZkNode node) {
        final String charset = charsetChoice.getSelectionModel().getSelectedItem();
        try {
            return new String(node.getDataBytes(), charset);
        } catch (UnsupportedEncodingException e) {
            return node.getData();
        }
    }

    private void changeTimeFormat() {
        if ("timestamp".equals(mtimeLabel.getProperties().get("format"))) {
            showDateTime();
        } else {
            showTimestamp();
        }
    }

    private void showDateTime() {
        final Object ctime = ctimeField.getProperties().getOrDefault("dateTime", "");
        ctimeField.setText(ctime.toString());
        final Object mtime = mtimeField.getProperties().getOrDefault("dateTime", "");
        mtimeField.setText(mtime.toString());
        ctimeLabel.getProperties().put("format", "dateTime");
        mtimeLabel.getProperties().put("format", "dateTime");
    }

    private void showTimestamp() {
        final Object ctime = ctimeField.getProperties().getOrDefault("timestamp", "");
        ctimeField.setText(ctime.toString());
        final Object mtime = mtimeField.getProperties().getOrDefault("timestamp", "");
        mtimeField.setText(mtime.toString());
        ctimeLabel.getProperties().put("format", "timestamp");
        mtimeLabel.getProperties().put("format", "timestamp");
    }

    private String format(long timestamp) {
        if (timestamp == 0) {
            return "无";
        } else {
            return OffsetDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
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

    private void dataJsonFormat() {
        final Object data = dataField.getProperties().get("raw");
        if (data == null) {
            return;
        }
        final String prettyJson;
        try {
            prettyJson = Formatters.prettyJson(data.toString());
            dataField.setText(prettyJson);
            switchFormatButton(jsonFormatButton);
        } catch (JsonProcessingException e) {
            VToast.error("JSON 格式错误");
        }
    }

    private void dataXmlFormat() {
        final Object data = dataField.getProperties().get("raw");
        if (data == null) {
            return;
        }
        final String prettyXML;
        try {
            prettyXML = Formatters.prettyXml(data.toString());
            dataField.setText(prettyXML);
            switchFormatButton(xmlFormatButton);
        } catch (Exception e) {
            VToast.error("XML 格式错误");
        }
    }

    private void dataRawFormat() {
        final Object data = dataField.getProperties().get("raw");
        if (data == null) {
            return;
        }
        dataField.setText(data.toString());
        switchFormatButton(rawFormatButton);
    }

    private void switchFormatButton(Button button) {
        button.setTextFill(Color.valueOf("#3F51B5"));
        formatButtons.stream().filter(b -> b != button).forEach(b -> b.setTextFill(Color.valueOf("#000")));
    }
}
