package cc.cc1234.main.vo;

import cc.cc1234.main.model.ZkServerConfig;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ZkServerConfigVO {

    private SimpleStringProperty host = new SimpleStringProperty();

    private SimpleBooleanProperty connect = new SimpleBooleanProperty(false);

    private SimpleIntegerProperty connectTimes = new SimpleIntegerProperty(0);

    private ObservableList<String> aclList = FXCollections.observableArrayList();

    public ZkServerConfigVO() {
    }

    public ZkServerConfigVO(ZkServerConfig config) {
        this.setHost(config.getHost());
        this.setConnectTimes(config.getConnectTimes());
        aclList.addAll(config.getAclList());
    }

    public void connectSuccess() {
        setConnect(true);
        this.setConnectTimes(getConnectTimes() + 1);
    }

    public String getHost() {
        return host.get();
    }

    public SimpleStringProperty hostProperty() {
        return host;
    }

    public void setHost(String host) {
        this.host.set(host);
    }

    public boolean isConnect() {
        return connect.get();
    }

    public SimpleBooleanProperty connectProperty() {
        return connect;
    }

    public void setConnect(boolean connect) {
        this.connect.set(connect);
    }

    public int getConnectTimes() {
        return connectTimes.get();
    }

    public SimpleIntegerProperty connectTimesProperty() {
        return connectTimes;
    }

    public void setConnectTimes(int connectTimes) {
        this.connectTimes.set(connectTimes);
    }

    public ObservableList<String> getAclList() {
        return aclList;
    }

    public void setAclList(ObservableList<String> aclList) {
        this.aclList = aclList;
    }
}
