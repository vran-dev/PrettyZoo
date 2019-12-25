package cc.cc1234.vo;

import cc.cc1234.cache.TreeItemCache;
import cc.cc1234.context.ActiveServerContext;
import cc.cc1234.context.ApplicationContext;
import cc.cc1234.context.RecursiveModeContext;
import cc.cc1234.service.ZkNodeService;
import cc.cc1234.util.Fills;
import com.google.common.base.Strings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
import org.apache.zookeeper.CreateMode;

import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class ZkNodeOperationVO {

    private StringProperty absolutePath = new SimpleStringProperty();

    private StringProperty relativePath = new SimpleStringProperty();

    private BooleanProperty seq = new SimpleBooleanProperty();

    private BooleanProperty eph = new SimpleBooleanProperty();

    private StringProperty data = new SimpleStringProperty();

    private StringProperty searchName = new SimpleStringProperty();

    private ZkNodeService zkNodeService = ApplicationContext.get().getBean(ZkNodeService.class);

    private TreeItemCache treeItemCache = TreeItemCache.getInstance();

    public void onAdd() throws Exception {
        final boolean recursive = RecursiveModeContext.get();
        zkNodeService.add(getAbsolutePath(), getData(), createMode(), recursive);
    }

    public void onDelete(Consumer<Exception> errorHandler) {
        zkNodeService.delete(getAbsolutePath(), RecursiveModeContext.get(), errorHandler);
    }

    public void updateData(Consumer<Exception> errorCallback) {
        zkNodeService.setData(getAbsolutePath(), getData(), errorCallback);
    }

    public List<ZkNodeSearchResult> onSearch() {
        final String host = ActiveServerContext.get();
        if (Strings.isNullOrEmpty(getSearchName())) {
            return Collections.emptyList();
        }
        final List<ZkNodeSearchResult> res = treeItemCache.search(host, getSearchName())
                .stream()
                .map(item -> {
                    String path = item.getValue().getPath();
                    final List<Text> highlights = Fills.fill(path, getSearchName(), Text::new,
                            s -> {
                                final Text highlight = new Text(s);
                                highlight.setFill(Color.RED);
                                return highlight;
                            });
                    final TextFlow textFlow = new TextFlow(highlights.toArray(new Text[0]));
                    return new ZkNodeSearchResult(path, textFlow, item);
                })
                .collect(Collectors.toList());
        return res;
    }

    public boolean nodeExists() {
        return treeItemCache.hasNode(ActiveServerContext.get(), getAbsolutePath());
    }

    private CreateMode createMode() {
        if (isSeq() && isEph()) {
            return CreateMode.EPHEMERAL_SEQUENTIAL;
        }

        if (isSeq()) {
            return CreateMode.PERSISTENT_SEQUENTIAL;
        }

        if (isEph()) {
            return CreateMode.EPHEMERAL;
        }

        // TODO  how to support CreateMode.CONTAINER ?
        return CreateMode.PERSISTENT;
    }

    public String getAbsolutePath() {
        return absolutePath.get();
    }

    public StringProperty absolutePathProperty() {
        return absolutePath;
    }

    public void setAbsolutePath(String absolutePath) {
        this.absolutePath.set(absolutePath);
    }

    public String getRelativePath() {
        return relativePath.get();
    }

    public StringProperty relativePathProperty() {
        return relativePath;
    }

    public void setRelativePath(String relativePath) {
        this.relativePath.set(relativePath);
    }

    public boolean isSeq() {
        return seq.get();
    }

    public BooleanProperty seqProperty() {
        return seq;
    }

    public void setSeq(boolean seq) {
        this.seq.set(seq);
    }

    public boolean isEph() {
        return eph.get();
    }

    public BooleanProperty ephProperty() {
        return eph;
    }

    public void setEph(boolean eph) {
        this.eph.set(eph);
    }

    public String getData() {
        return data.get();
    }

    public StringProperty dataProperty() {
        return data;
    }

    public void setData(String data) {
        this.data.set(data);
    }

    public String getSearchName() {
        return searchName.get();
    }

    public StringProperty searchNameProperty() {
        return searchName;
    }

    public void setSearchName(String searchName) {
        this.searchName.set(searchName);
    }
}
