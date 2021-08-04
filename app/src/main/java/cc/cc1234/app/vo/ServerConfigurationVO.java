package cc.cc1234.app.vo;

import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ServerConfigurationVO {

    private SimpleStringProperty zkUrl = new SimpleStringProperty("");

    private SimpleStringProperty zkHost = new SimpleStringProperty("");

    private SimpleIntegerProperty zkPort = new SimpleIntegerProperty(0);

    private SimpleStringProperty zkAlias = new SimpleStringProperty("");

    private ObjectProperty<ObservableList<String>> aclList = new SimpleObjectProperty<>(FXCollections.observableArrayList());

    private SimpleObjectProperty<ServerStatus> status = new SimpleObjectProperty<>(ServerStatus.DISCONNECTED);

    private SimpleBooleanProperty sshEnabled = new SimpleBooleanProperty(false);

    private SimpleStringProperty sshServer = new SimpleStringProperty("");

    private ObjectProperty<Integer> sshServerPort = new SimpleObjectProperty<>();

    private SimpleStringProperty sshUsername = new SimpleStringProperty("");

    private SimpleStringProperty sshPassword = new SimpleStringProperty("");

    private SimpleStringProperty remoteServer = new SimpleStringProperty("");

    private ObjectProperty<Integer> remoteServerPort = new SimpleObjectProperty<>();

    public void unbind() {
        zkUrl.unbind();
        zkHost.unbind();
        zkPort.unbind();
        zkAlias.unbind();
        aclList.unbind();
        sshEnabled.unbind();
        sshServer.unbind();
        sshServerPort.unbind();
        sshUsername.unbind();
        sshPassword.unbind();
        remoteServer.unbind();
        remoteServerPort.unbind();
    }

    public String getZkUrl() {
        return zkUrl.get();
    }

    public void setZkUrl(String zkUrl) {
        this.zkUrl.set(zkUrl);
    }

    public SimpleStringProperty zkUrlProperty() {
        return zkUrl;
    }

    public ServerStatus getStatus() {
        return status.get();
    }

    public void setStatus(ServerStatus status) {
        this.status.set(status);
    }

    public SimpleObjectProperty<ServerStatus> statusProperty() {
        return status;
    }

    public String getZkHost() {
        return zkHost.get();
    }

    public void setZkHost(String zkHost) {
        this.zkHost.set(zkHost);
    }

    public SimpleStringProperty zkHostProperty() {
        return zkHost;
    }

    public int getZkPort() {
        return zkPort.get();
    }

    public void setZkPort(int zkPort) {
        this.zkPort.set(zkPort);
    }

    public SimpleIntegerProperty zkPortProperty() {
        return zkPort;
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

    public Integer getSshServerPort() {
        return sshServerPort.get();
    }

    public void setSshServerPort(Integer sshServerPort) {
        this.sshServerPort.set(sshServerPort);
    }

    public ObjectProperty<Integer> sshServerPortProperty() {
        return sshServerPort;
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

    public Integer getRemoteServerPort() {
        return remoteServerPort.get();
    }

    public void setRemoteServerPort(Integer remoteServerPort) {
        this.remoteServerPort.set(remoteServerPort);
    }

    public ObjectProperty<Integer> remoteServerPortProperty() {
        return remoteServerPort;
    }

    public String getZkAlias() {
        return zkAlias.get();
    }

    public void setZkAlias(String zkAlias) {
        this.zkAlias.set(zkAlias);
    }

    public SimpleStringProperty zkAliasProperty() {
        return zkAlias;
    }
}
