package cc.cc1234.app.vo;

import cc.cc1234.spi.node.ZkNode;
import javafx.scene.control.TreeItem;
import javafx.scene.text.TextFlow;

public class ZkNodeSearchResult {

    private final String path;

    private final TextFlow textFlow;

    private final TreeItem<ZkNode> item;

    public ZkNodeSearchResult(String path, TextFlow textFlow, TreeItem<ZkNode> item) {
        this.path = path;
        this.textFlow = textFlow;
        this.item = item;
    }

    public String getPath() {
        return path;
    }

    public TextFlow getTextFlow() {
        return textFlow;
    }

    public TreeItem<ZkNode> getItem() {
        return item;
    }
}
