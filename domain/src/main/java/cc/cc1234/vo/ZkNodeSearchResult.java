package cc.cc1234.vo;

import cc.cc1234.model.ZkNode;
import javafx.scene.control.TreeItem;
import javafx.scene.text.TextFlow;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ZkNodeSearchResult {

    private final String path;

    private final TextFlow textFlow;

    private final TreeItem<ZkNode> item;

}
