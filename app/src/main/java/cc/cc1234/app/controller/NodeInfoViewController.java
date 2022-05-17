package cc.cc1234.app.controller;

import cc.cc1234.app.cache.TreeItemCache;
import cc.cc1234.app.context.ActiveServerContext;
import cc.cc1234.app.facade.PrettyZooFacade;
import cc.cc1234.app.util.Formatters;
import cc.cc1234.app.util.ResourceBundleUtils;
import cc.cc1234.app.view.NodeDataArea;
import cc.cc1234.app.view.dialog.Dialog;
import cc.cc1234.app.view.toast.VToast;
import cc.cc1234.app.view.transitions.Transitions;
import cc.cc1234.specification.node.ZkNode;
import com.fasterxml.jackson.core.JsonProcessingException;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import org.apache.zookeeper.data.Stat;
import org.fxmisc.flowless.VirtualizedScrollPane;

import java.io.UnsupportedEncodingException;
import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;
import java.util.ResourceBundle;

@SuppressWarnings("all")
public class NodeInfoViewController {

    @FXML
    private AnchorPane nodeInfoPane;

    @FXML
    private SplitPane nodeInfoSplitPane;

    @FXML
    private AnchorPane nodeDataPane;

    @FXML
    @SuppressWarnings("all")
    private TextField cZxidField;

    @FXML
    @SuppressWarnings("all")
    private TextField pZxidField;

    @FXML
    private TextField mtimeField;

    @FXML
    private TextField ephemeralOwnerField;

    @FXML
    private TextField ctimeField;

    @FXML
    @SuppressWarnings("all")
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
    @SuppressWarnings("all")
    private TextField cVersionField;

    @FXML
    private TextField pathField;

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
    private HBox dataMenuBar;

    @FXML
    private ChoiceBox<String> charsetChoice;

    private NodeDataArea dataCodeArea = new NodeDataArea();

    @FXML
    private PrettyZooFacade prettyZooFacade = new PrettyZooFacade();

    private List<Button> formatButtons = List.of();

    public final static String SYS_LF;

    static {
        String lf;
        try {
            lf = System.getProperty("line.separator");
        } catch (Throwable t) {
            lf = "\n"; // fallback when security manager denies access
        }
        SYS_LF = lf;
    }

    @FXML
    private void initialize() {
        nodeUpdateButton.setOnMouseClicked(e -> onNodeUpdate());
        initCodeArea();
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
            byte[] rawBytes = (byte[]) dataCodeArea.getProperties().get("rawBytes");
            if (rawBytes == null) {
                return;
            }
            try {
                setCodeAreaData(new String(rawBytes, nv));
            } catch (UnsupportedEncodingException unsupportedEncodingException) {
                VToast.error("Not supported Charset:" + nv);
            }
        });

        Tooltip tooltip = new Tooltip("Double click");
        Tooltip.install(dataMenuBar, tooltip);
        dataMenuBar.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && e.getButton() == MouseButton.PRIMARY) {
                StackPane parent = (StackPane) nodeInfoPane.getParent();
                if (!parent.getChildren().contains(nodeDataPane)) {
                    nodeInfoSplitPane.getItems().remove(nodeDataPane);
                    parent.getChildren().add(nodeDataPane);
                    Transitions.zoomIn(nodeDataPane).play();
                } else {
                    parent.getChildren().remove(nodeDataPane);
                    nodeInfoSplitPane.getItems().add(nodeDataPane);
                }
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

    private void initCodeArea() {
        var pane = new VirtualizedScrollPane<>(dataCodeArea);
        AnchorPane.setTopAnchor(pane, 40d);
        AnchorPane.setLeftAnchor(pane, 0d);
        AnchorPane.setRightAnchor(pane, 0d);
        AnchorPane.setBottomAnchor(pane, 5d);
        nodeDataPane.getChildren().add(pane);
    }

    private void setCodeAreaData(String data) {
        dataCodeArea.setText(data);
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
        final String data = dataCodeArea.getText();
        TreeItem<ZkNode> currentData = TreeItemCache.getInstance().get(ActiveServerContext.get(), path);

        Runnable action = () -> {
            Transitions.rotate(nodeUpdateButton, () -> {
                Stat stat = prettyZooFacade.updateData(ActiveServerContext.get(), path, data,
                        ex -> VToast.error(ex.getMessage()));
                dataCodeArea.getProperties().put("raw", data);
                dataCodeArea.getProperties().put("rawBytes", data.getBytes());
                updateField(stat);
                VToast.info("update success");
            });
        };

        ResourceBundle bundle = ResourceBundleUtils.get(prettyZooFacade.getLocale());
        String title = bundle.getString("nodeData.refresh.conflict.title");
        String content = bundle.getString("nodeData.refresh.conflict.content");
        if (!Objects.equals(currentData.getValue().getData(), data)) {
            Dialog.confirm(title, String.format(content, currentData.getValue().getData()), action);
        } else {
            action.run();
        }
    }

    /**
     * TODO use data bind to instead of manual bind
     */
    private void updateField(Stat node) {
        ephemeralOwnerField.setText(String.valueOf(node.getEphemeralOwner()));
        cZxidField.setText(String.valueOf(node.getCzxid()));
        pZxidField.setText(String.valueOf(node.getPzxid()));
        mZxidField.setText(String.valueOf(node.getMzxid()));
        dataLengthField.setText(String.valueOf(node.getDataLength()));
        numChildrenField.setText(String.valueOf(node.getNumChildren()));
        dataVersionField.setText(String.valueOf(node.getVersion()));
        aclVersionField.setText(String.valueOf(node.getAversion()));
        cVersionField.setText(String.valueOf(node.getCversion()));
        mtimeField.getProperties().put("timestamp", node.getMtime());
        mtimeField.getProperties().put("dateTime", format(node.getMtime()));
        ctimeField.getProperties().put("timestamp", node.getCtime());
        ctimeField.getProperties().put("dateTime", format(node.getCtime()));
        showDateTime();
    }

    private void initTextField(ZkNode node) {
        setCodeAreaData(transformData(node));
        dataCodeArea.getProperties().put("raw", transformData(node));
        dataCodeArea.getProperties().put("rawBytes", node.getDataBytes());

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
            return "-";
        } else {
            return OffsetDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
        }
    }

    private void resetTextField() {
        dataCodeArea.clear();
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
        final Object data = dataCodeArea.getProperties().get("raw");
        if (data == null) {
            return;
        }
        String prettyJson;
        try {
            String raw = data.toString();
            prettyJson = Formatters.prettyJson(raw);
            if (Objects.equals(SYS_LF, "\r\n")) {
                prettyJson = prettyJson.replaceAll("\r\n", "\n");
            }
            if (!Objects.equals(prettyJson, raw)) {
                setCodeAreaData(prettyJson);
            }
            switchFormatButton(jsonFormatButton);
        } catch (JsonProcessingException e) {
            VToast.error("JSON format failed");
        }
    }

    private void dataXmlFormat() {
        final Object data = dataCodeArea.getProperties().get("raw");
        if (data == null) {
            return;
        }
        final String prettyXML;
        try {
            prettyXML = Formatters.prettyXml(data.toString());
            setCodeAreaData(prettyXML);
            switchFormatButton(xmlFormatButton);
        } catch (Exception e) {
            VToast.error("XML format failed");
        }
    }

    private void dataRawFormat() {
        final Object data = dataCodeArea.getProperties().get("raw");
        if (data == null) {
            return;
        }
        setCodeAreaData(data.toString());
        switchFormatButton(rawFormatButton);
    }

    private void switchFormatButton(Button button) {
        button.setTextFill(Color.valueOf("#3F51B5"));
        formatButtons.stream().filter(b -> b != button).forEach(b -> b.setTextFill(Color.valueOf("#000")));
    }
}
