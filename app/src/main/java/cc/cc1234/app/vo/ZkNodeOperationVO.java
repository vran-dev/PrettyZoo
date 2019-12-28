package cc.cc1234.app.vo;

import cc.cc1234.app.cache.TreeItemCache;
import cc.cc1234.app.context.ActiveServerContext;
import cc.cc1234.app.context.RecursiveModeContext;
import cc.cc1234.app.util.Fills;
import cc.cc1234.facade.PrettyZooFacade;
import cc.cc1234.spi.node.NodeMode;
import com.google.common.base.Strings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;

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

    private TreeItemCache treeItemCache = TreeItemCache.getInstance();

    private PrettyZooFacade prettyZooFacade = new PrettyZooFacade();

    public void onAdd() throws Exception {
        final boolean recursive = RecursiveModeContext.get();
        prettyZooFacade.addNode(ActiveServerContext.get(), getAbsolutePath(), getData(), recursive, createMode());
    }

    public void onDelete(Consumer<Exception> errorHandler) {
        try {
            prettyZooFacade.deleteNode(ActiveServerContext.get(), getAbsolutePath(), RecursiveModeContext.get());
        } catch (Exception e) {
            errorHandler.accept(e);
        }
    }

    public void updateData(Consumer<Exception> errorCallback) {
        try {
            prettyZooFacade.setData(ActiveServerContext.get(), getAbsolutePath(), getData());
        } catch (Exception e) {
            errorCallback.accept(e);
        }
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

    private NodeMode createMode() {
        if (isSeq() && isEph()) {
            return NodeMode.EPHEMERAL_SEQUENTIAL;
        }

        if (isSeq()) {
            return NodeMode.PERSISTENT_SEQUENTIAL;
        }

        if (isEph()) {
            return NodeMode.EPHEMERAL;
        }

        // TODO  how to support CreateMode.CONTAINER ?
        return NodeMode.PERSISTENT;
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
