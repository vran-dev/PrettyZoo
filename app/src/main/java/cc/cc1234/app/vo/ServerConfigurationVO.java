package cc.cc1234.app.vo;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ServerConfigurationVO {

    private SimpleStringProperty zkServer = new SimpleStringProperty("");

    private SimpleStringProperty zkAlias= new SimpleStringProperty("");

    private ObjectProperty<ObservableList<String>> aclList = new SimpleObjectProperty<>(FXCollections.observableArrayList());

    private SimpleBooleanProperty connected = new SimpleBooleanProperty(false);

    private SimpleBooleanProperty sshEnabled = new SimpleBooleanProperty(false);

    private SimpleStringProperty sshServer = new SimpleStringProperty("");

    private SimpleStringProperty sshUsername = new SimpleStringProperty("");

    private SimpleStringProperty sshPassword = new SimpleStringProperty("");

    private SimpleStringProperty remoteServer = new SimpleStringProperty("");

    public void unbind() {
        zkServer.unbind();
        zkAlias.unbind();
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

    public void setZkServer(String zkServer) {
        this.zkServer.set(zkServer);
    }

    public SimpleStringProperty zkServerProperty() {
        return zkServer;
    }

    public ObservableList<String> getAclList() {
        return aclList.get();
    }

    public void setAclList(ObservableList<String> aclList) {
        this.aclList.set(aclList);
    }

    public ObjectProperty<ObservableList<String>> aclListProperty() {
        return aclList;
    }

    public boolean isConnected() {
        return connected.get();
    }

    public void setConnected(boolean connected) {
        this.connected.set(connected);
    }

    public SimpleBooleanProperty connectedProperty() {
        return connected;
    }

    public boolean isSshEnabled() {
        return sshEnabled.get();
    }

    public void setSshEnabled(boolean sshEnabled) {
        this.sshEnabled.set(sshEnabled);
    }

    public SimpleBooleanProperty sshEnabledProperty() {
        return sshEnabled;
    }

    public String getSshServer() {
        return sshServer.get();
    }

    public void setSshServer(String sshServer) {
        this.sshServer.set(sshServer);
    }

    public SimpleStringProperty sshServerProperty() {
        return sshServer;
    }

    public String getSshUsername() {
        return sshUsername.get();
    }

    public void setSshUsername(String sshUsername) {
        this.sshUsername.set(sshUsername);
    }

    public SimpleStringProperty sshUsernameProperty() {
        return sshUsername;
    }

    public String getSshPassword() {
        return sshPassword.get();
    }

    public void setSshPassword(String sshPassword) {
        this.sshPassword.set(sshPassword);
    }

    public SimpleStringProperty sshPasswordProperty() {
        return sshPassword;
    }

    public String getRemoteServer() {
        return remoteServer.get();
    }

    public void setRemoteServer(String remoteServer) {
        this.remoteServer.set(remoteServer);
    }

    public SimpleStringProperty remoteServerProperty() {
        return remoteServer;
    }

    public String getZkAlias() {
        return zkAlias.get();
    }

    public SimpleStringProperty zkAliasProperty() {
        return zkAlias;
    }

    public void setZkAlias(String zkAlias) {
        this.zkAlias.set(zkAlias);
    }
}
