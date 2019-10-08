package cc.cc1234.main.controller;

import cc.cc1234.main.vo.AddServerVO;
import com.google.common.base.Strings;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.fxmisc.flowless.VirtualizedScrollPane;
import org.fxmisc.richtext.CodeArea;
import org.fxmisc.richtext.model.StyleSpans;
import org.fxmisc.richtext.model.StyleSpansBuilder;

import java.time.Duration;
import java.util.Collection;
import java.util.Collections;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class AddServerViewController {

    @FXML
    private TextField serverTextField;

    @FXML
    private AnchorPane addServerPane;

    private CodeArea aclArea;

    private volatile boolean hasError = false;

    private Stage stage;

    private AddServerVO addServerVO = new AddServerVO();

    @FXML
    public void initialize() {
        stage = new Stage();
        stage.initStyle(StageStyle.UNDECORATED);
        stage.initModality(Modality.APPLICATION_MODAL);
        final VirtualizedScrollPane<CodeArea> scrollPane = initHighlightPane();
        addServerPane.getChildren().add(scrollPane);
        final Scene scene = new Scene(addServerPane);
        scene.setFill(Color.TRANSPARENT);

        // property bind
        addServerVO.hostProperty().bind(serverTextField.textProperty());
        addServerVO.aclProperty().bind(aclArea.textProperty());

        scene.getStylesheets().add(Thread.currentThread().getContextClassLoader().getResource("assets/acl.css").toExternalForm());
        stage.setScene(scene);

    }

    public void show(Window parent) {
        serverTextField.setText("127.0.0.1:2181");
        aclArea.replaceText("");
        hasError = false;
        double x = parent.getX() + parent.getWidth() / 2 - addServerPane.getPrefWidth() / 2;
        double y = parent.getY() + parent.getHeight() / 2 - addServerPane.getPrefHeight() / 2;
        stage.setX(x);
        stage.setY(y);
        stage.show();
    }

    private VirtualizedScrollPane<CodeArea> initHighlightPane() {
        aclArea = new CodeArea();
        aclArea.multiPlainChanges()
                .successionEnds(Duration.ofMillis(300))
                .subscribe(ignore -> aclArea.setStyleSpans(0, computeHighlighting(aclArea.getText())));
        aclArea.setStyle("-fx-background-color: #FFF;-fx-font-size: 16;");
        final VirtualizedScrollPane<CodeArea> pane = new VirtualizedScrollPane<>(aclArea);
        AnchorPane.setTopAnchor(pane, 160.0);
        AnchorPane.setBottomAnchor(pane, 60.0);
        AnchorPane.setLeftAnchor(pane, 20.0);
        AnchorPane.setRightAnchor(pane, 20.0);
        return pane;
    }

    private StyleSpans<Collection<String>> computeHighlighting(String text) {
        String pattern = "(?<schema>auth|digest)\\:(?<username>.+)\\:(?<password>.+)(\n*)";
        Matcher matcher = Pattern.compile(pattern).matcher(text);
        StyleSpansBuilder<Collection<String>> spansBuilder = new StyleSpansBuilder<>();
        int end = 0;
        while (matcher.find()) {
            if (matcher.group("schema") != null) {
                spansBuilder.add(Collections.emptyList(), matcher.start("schema") - end);
                spansBuilder.add(Collections.singleton("schema"), matcher.end("schema") - matcher.start("schema"));
                end = matcher.end("schema");
            }

            if (matcher.group("username") != null) {
                spansBuilder.add(Collections.emptyList(), matcher.start("username") - end);
                spansBuilder.add(Collections.singleton("username"), matcher.end("username") - matcher.start("username"));
                end = matcher.end("username");
            }

            if (matcher.group("password") != null) {
                spansBuilder.add(Collections.emptyList(), matcher.start("password") - end);
                spansBuilder.add(Collections.singleton("password"), matcher.end("password") - matcher.start("password"));
                end = matcher.end("password");
            }
        }
        hasError = text.length() > end;
        if ((text.length() - end) == 1 && text.endsWith("\n")) {
            hasError = false;
        }
        spansBuilder.add(Collections.singleton("error"), text.length() - end);
        return spansBuilder.create();
    }

    @FXML
    private void onCancel() {
        stage.close();
    }

    @FXML
    private void onConfirm() {
        if (hasError) {
            VToast.toastFailure(stage, "ACL is invalid");
            return;
        }

        if (Strings.isNullOrEmpty(addServerVO.getHost())) {
            VToast.toastFailure(stage, "server must not be empty");
            return;
        }

        if (addServerVO.exists()) {
            VToast.toastFailure(stage, "server exists!");
            return;
        }

        addServerVO.onConfirm();
        VToast.toastSuccess("add success");
        stage.close();
    }

}
