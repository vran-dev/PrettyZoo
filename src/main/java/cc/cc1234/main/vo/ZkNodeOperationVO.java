package cc.cc1234.main.vo;

import cc.cc1234.main.cache.RecursiveModeContext;
import cc.cc1234.main.service.ZkServerService;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.api.CreateBuilder;
import org.apache.zookeeper.CreateMode;

import java.util.function.Consumer;

public class ZkNodeOperationVO {

    private StringProperty absolutePath = new SimpleStringProperty();

    private StringProperty relativePath = new SimpleStringProperty();

    private BooleanProperty seq = new SimpleBooleanProperty();

    private BooleanProperty eph = new SimpleBooleanProperty();

    private StringProperty data = new SimpleStringProperty();

    public void onAdd(CuratorFramework client) throws Exception {
        final CreateBuilder createBuilder = client.create();
        if (RecursiveModeContext.get()) {
            createBuilder.creatingParentsIfNeeded()
                    .withMode(createMode())
                    .forPath(getAbsolutePath(), getData().getBytes());
        } else {
            createBuilder.withMode(createMode())
                    .forPath(getAbsolutePath(), getData().getBytes());
        }
    }

    public void onDelete(Consumer<Exception>  errorHandler) {
        ZkServerService.getActive()
                .delete(getAbsolutePath(), RecursiveModeContext.get(), errorHandler);
    }

    public void updateData(Consumer<Exception> errorCallback) {
        ZkServerService.getActive().setData(getAbsolutePath(), getData(), errorCallback);
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
