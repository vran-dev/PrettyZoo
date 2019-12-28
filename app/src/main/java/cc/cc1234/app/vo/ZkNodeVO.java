package cc.cc1234.app.vo;

import cc.cc1234.spi.node.ZkNode;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleLongProperty;
import javafx.beans.property.SimpleStringProperty;

public class ZkNodeVO {

    /**
     * 节点名称
     */
    private SimpleStringProperty name = new SimpleStringProperty();

    /**
     * path
     */
    private SimpleStringProperty path = new SimpleStringProperty();

    private SimpleLongProperty czxid = new SimpleLongProperty();
    private SimpleLongProperty mzxid = new SimpleLongProperty();
    private SimpleLongProperty ctime = new SimpleLongProperty();
    private SimpleLongProperty mtime = new SimpleLongProperty();
    private SimpleIntegerProperty version = new SimpleIntegerProperty();
    private SimpleIntegerProperty cversion = new SimpleIntegerProperty();
    private SimpleIntegerProperty aversion = new SimpleIntegerProperty();
    private SimpleLongProperty ephemeralOwner = new SimpleLongProperty();
    private SimpleIntegerProperty dataLength = new SimpleIntegerProperty();
    private SimpleIntegerProperty numChildren = new SimpleIntegerProperty();
    private SimpleLongProperty pzxid = new SimpleLongProperty();

    private SimpleStringProperty data = new SimpleStringProperty();


    public ZkNodeVO() {
    }

    public ZkNodeVO(ZkNode node) {
        change(node);
    }

    public void change(ZkNode node) {
        setName(node.getName());
        setPath(node.getPath());
        setCzxid(node.getCzxid());
        setMzxid(node.getMzxid());
        setCtime(node.getCtime());
        setMtime(node.getMtime());
        setVersion(node.getVersion());
        setCversion(node.getCversion());
        setAversion(node.getAversion());
        setEphemeralOwner(node.getEphemeralOwner());
        setDataLength(node.getDataLength());
        setNumChildren(node.getNumChildren());
        setPzxid(node.getPzxid());
        setData(node.getData());
    }


    public String getName() {
        return name.get();
    }

    public SimpleStringProperty nameProperty() {
        return name;
    }

    public void setName(String name) {
        this.name.set(name);
    }

    public String getPath() {
        return path.get();
    }

    public SimpleStringProperty pathProperty() {
        return path;
    }

    public void setPath(String path) {
        this.path.set(path);
    }

    public long getCzxid() {
        return czxid.get();
    }

    public SimpleLongProperty czxidProperty() {
        return czxid;
    }

    public void setCzxid(long czxid) {
        this.czxid.set(czxid);
    }

    public long getMzxid() {
        return mzxid.get();
    }

    public SimpleLongProperty mzxidProperty() {
        return mzxid;
    }

    public void setMzxid(long mzxid) {
        this.mzxid.set(mzxid);
    }

    public long getCtime() {
        return ctime.get();
    }

    public SimpleLongProperty ctimeProperty() {
        return ctime;
    }

    public void setCtime(long ctime) {
        this.ctime.set(ctime);
    }

    public long getMtime() {
        return mtime.get();
    }

    public SimpleLongProperty mtimeProperty() {
        return mtime;
    }

    public void setMtime(long mtime) {
        this.mtime.set(mtime);
    }

    public int getVersion() {
        return version.get();
    }

    public SimpleIntegerProperty versionProperty() {
        return version;
    }

    public void setVersion(int version) {
        this.version.set(version);
    }

    public int getCversion() {
        return cversion.get();
    }

    public SimpleIntegerProperty cversionProperty() {
        return cversion;
    }

    public void setCversion(int cversion) {
        this.cversion.set(cversion);
    }

    public int getAversion() {
        return aversion.get();
    }

    public SimpleIntegerProperty aversionProperty() {
        return aversion;
    }

    public void setAversion(int aversion) {
        this.aversion.set(aversion);
    }

    public long getEphemeralOwner() {
        return ephemeralOwner.get();
    }

    public SimpleLongProperty ephemeralOwnerProperty() {
        return ephemeralOwner;
    }

    public void setEphemeralOwner(long ephemeralOwner) {
        this.ephemeralOwner.set(ephemeralOwner);
    }

    public int getDataLength() {
        return dataLength.get();
    }

    public SimpleIntegerProperty dataLengthProperty() {
        return dataLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength.set(dataLength);
    }

    public int getNumChildren() {
        return numChildren.get();
    }

    public SimpleIntegerProperty numChildrenProperty() {
        return numChildren;
    }

    public void setNumChildren(int numChildren) {
        this.numChildren.set(numChildren);
    }

    public long getPzxid() {
        return pzxid.get();
    }

    public SimpleLongProperty pzxidProperty() {
        return pzxid;
    }

    public void setPzxid(long pzxid) {
        this.pzxid.set(pzxid);
    }

    public String getData() {
        return data.get();
    }

    public SimpleStringProperty dataProperty() {
        return data;
    }

    public void setData(String data) {
        this.data.set(data);
    }

}
