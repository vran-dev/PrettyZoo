package cc.cc1234.main.vo;

import cc.cc1234.main.cache.TreeItemCache;
import cc.cc1234.main.context.ActiveServerContext;
import cc.cc1234.main.context.ApplicationContext;
import cc.cc1234.main.context.RecursiveModeContext;
import cc.cc1234.main.service.ZkNodeService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.zookeeper.CreateMode;

import java.util.function.Consumer;

public class ZkNodeOperationVO {

    private StringProperty absolutePath = new SimpleStringProperty();

    private StringProperty relativePath = new SimpleStringProperty();

    private BooleanProperty seq = new SimpleBooleanProperty();

    private BooleanProperty eph = new SimpleBooleanProperty();

    private StringProperty data = new SimpleStringProperty();

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
}
