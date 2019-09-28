package cc.cc1234.main.model;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import org.apache.zookeeper.data.Stat;

public class ZkNode {

    /**
     * 节点名称
     */
    private SimpleStringProperty name = new SimpleStringProperty();

    /**
     * path
     */
    private SimpleStringProperty path = new SimpleStringProperty();

    private SimpleIntegerProperty numOfChildren = new SimpleIntegerProperty();

    private ObjectProperty<Stat> stat = new SimpleObjectProperty<>();

    private SimpleStringProperty data = new SimpleStringProperty();

    public ZkNode() {
    }

    public ZkNode(String name, String  path) {
        setName(name);
        setPath(path);
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

    public int getNumOfChildren() {
        return numOfChildren.get();
    }

    public SimpleIntegerProperty numOfChildrenProperty() {
        return numOfChildren;
    }

    public void setNumOfChildren(int numOfChildren) {
        this.numOfChildren.set(numOfChildren);
    }

    public String toString() {
        return this.getName();
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

    public Stat getStat() {
        return stat.get();
    }

    public ObjectProperty<Stat> statProperty() {
        return stat;
    }

    public void setStat(Stat stat) {
        this.stat.set(stat);
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
