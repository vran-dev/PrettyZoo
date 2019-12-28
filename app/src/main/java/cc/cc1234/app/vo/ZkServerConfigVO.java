package cc.cc1234.app.vo;

import cc.cc1234.facade.PrettyZooFacade;
import cc.cc1234.spi.config.model.ServerConfig;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ZkServerConfigVO {

    private SimpleStringProperty host = new SimpleStringProperty();

    private SimpleBooleanProperty connect = new SimpleBooleanProperty(false);

    private SimpleIntegerProperty connectTimes = new SimpleIntegerProperty(0);

    private ObjectProperty<ObservableList<String>> aclList = new SimpleObjectProperty<>(FXCollections.observableArrayList());

    private PrettyZooFacade prettyZooFacade = new PrettyZooFacade();

    public ZkServerConfigVO() {
    }

    public ZkServerConfigVO(ServerConfig config) {
        this.setHost(config.getHost());
        this.setConnectTimes(config.getConnectTimes());
        aclList.get().addAll(config.getAclList());
    }

    public void connectIfNecessary() throws Exception {
        final ServerConfig config = new ServerConfig();
        config.setHost(getHost());
        config.getAclList().addAll(getAclList());
        prettyZooFacade.connect(config);
        connectSuccess();
    }

    public void syncIfNecessary() {
        prettyZooFacade.syncIfNecessary(getHost());
    }

    private void connectSuccess() {
        this.setConnect(true);
        this.setConnectTimes(getConnectTimes() + 1);
        prettyZooFacade.increaseConnectTimes(getHost());
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
        return aclList.get();
    }

    public ObjectProperty<ObservableList<String>> aclListProperty() {
        return aclList;
    }

    public void setAclList(ObservableList<String> aclList) {
        this.aclList.set(aclList);
    }

}
