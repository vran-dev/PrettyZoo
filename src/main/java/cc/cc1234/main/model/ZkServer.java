package cc.cc1234.main.model;

import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;

public class ZkServer {

    private SimpleStringProperty server = new SimpleStringProperty();

    private SimpleBooleanProperty connect = new SimpleBooleanProperty(false);

    private SimpleIntegerProperty connectTimes = new SimpleIntegerProperty();

    public ZkServer(String server) {
        this.setServer(server);
    }

    public String getServer() {
        return server.get();
    }

    public SimpleStringProperty serverProperty() {
        return server;
    }

    public void setServer(String server) {
        this.server.set(server);
    }

    public boolean getConnect() {
        return connect.get();
    }

    public SimpleBooleanProperty connectProperty() {
        return connect;
    }

    public void setConnect(boolean connect) {
        this.connect.set(connect);
    }

    public boolean isConnect() {
        return connect.get();
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
}
