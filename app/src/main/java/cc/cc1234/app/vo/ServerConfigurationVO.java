package cc.cc1234.app.vo;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ServerConfigurationVO {

    private SimpleStringProperty zkServer = new SimpleStringProperty("");

    private ObjectProperty<ObservableList<String>> aclList = new SimpleObjectProperty<>(FXCollections.observableArrayList());

    private SimpleBooleanProperty connected = new SimpleBooleanProperty(false);

    private SimpleBooleanProperty sshEnabled = new SimpleBooleanProperty(false);

    private SimpleStringProperty sshServer = new SimpleStringProperty("");

    private SimpleStringProperty sshUsername = new SimpleStringProperty("");

    private SimpleStringProperty sshPassword = new SimpleStringProperty("");

    private SimpleStringProperty remoteServer = new SimpleStringProperty("");

    public void unbind() {
        zkServer.unbind();
        aclList.unbind();
        connected.unbind();
        sshEnabled.unbind();
        sshServer.unbind();
        sshUsername.unbind();
        sshPassword.unbind();
        remoteServer.unbind();
    }


    public String getZkServer() {
        return zkServer.get();
    }

    public SimpleStringProperty zkServerProperty() {
        return zkServer;
    }

    public void setZkServer(String zkServer) {
        this.zkServer.set(zkServer);
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

    public boolean isConnected() {
        return connected.get();
    }

    public SimpleBooleanProperty connectedProperty() {
        return connected;
    }

    public void setConnected(boolean connected) {
        this.connected.set(connected);
    }


    public boolean isSshEnabled() {
        return sshEnabled.get();
    }

    public SimpleBooleanProperty sshEnabledProperty() {
        return sshEnabled;
    }

    public void setSshEnabled(boolean sshEnabled) {
        this.sshEnabled.set(sshEnabled);
    }

    public String getSshServer() {
        return sshServer.get();
    }

    public SimpleStringProperty sshServerProperty() {
        return sshServer;
    }

    public void setSshServer(String sshServer) {
        this.sshServer.set(sshServer);
    }

    public String getSshUsername() {
        return sshUsername.get();
    }

    public SimpleStringProperty sshUsernameProperty() {
        return sshUsername;
    }

    public void setSshUsername(String sshUsername) {
        this.sshUsername.set(sshUsername);
    }

    public String getSshPassword() {
        return sshPassword.get();
    }

    public SimpleStringProperty sshPasswordProperty() {
        return sshPassword;
    }

    public void setSshPassword(String sshPassword) {
        this.sshPassword.set(sshPassword);
    }

    public String getRemoteServer() {
        return remoteServer.get();
    }

    public SimpleStringProperty remoteServerProperty() {
        return remoteServer;
    }

    public void setRemoteServer(String remoteServer) {
        this.remoteServer.set(remoteServer);
    }
}
