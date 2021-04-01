package cc.cc1234.app.view;

import cc.cc1234.app.util.Highlights;
import org.fxmisc.richtext.CodeArea;

import java.util.Objects;

public class NodeDataArea extends CodeArea {

    public NodeDataArea() {
        super("");

        this.setEditable(true);
        this.getStyleClass().add("vTextArea");
        this.textProperty().addListener((obs, oldText, newText) -> {
            this.setStyleSpans(0, Highlights.computeHighlighting(newText));
        });
    }

    public void setText(String data) {
        if (Objects.equals(data, this.getText())) {
            this.clear();
        }
        this.replaceText(data);
    }
}
