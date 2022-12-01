package cc.cc1234.app.vo;

import javafx.beans.property.*;

public class ServerConfigurationVO {

    private SimpleStringProperty id = new SimpleStringProperty("");

    private SimpleStringProperty zkHost = new SimpleStringProperty("");

    private SimpleIntegerProperty zkPort = new SimpleIntegerProperty(2181);

    private SimpleStringProperty zkAlias = new SimpleStringProperty("");

    private StringProperty acl = new SimpleStringProperty();

    private SimpleObjectProperty<ServerStatus> status = new SimpleObjectProperty<>(ServerStatus.DISCONNECTED);

    private SimpleBooleanProperty sshEnabled = new SimpleBooleanProperty(false);

    private SimpleStringProperty sshServer = new SimpleStringProperty("");

    private SimpleIntegerProperty sshServerPort = new SimpleIntegerProperty();

    private SimpleStringProperty sshUsername = new SimpleStringProperty("");

    private SimpleStringProperty sshPassword = new SimpleStringProperty("");

    private SimpleStringProperty sshKeyFilePath = new SimpleStringProperty("");

    private SimpleStringProperty remoteServer = new SimpleStringProperty("");

    private SimpleIntegerProperty remoteServerPort = new SimpleIntegerProperty();

    private SimpleBooleanProperty enableConnectionAdvanceConfiguration = new SimpleBooleanProperty(false);

    private ObjectProperty<ConnectionConfigurationVO> connectionConfiguration
            = new SimpleObjectProperty<>(new ConnectionConfigurationVO());

    public void update(ServerConfigurationVO config) {
        id.set(config.getId());
        zkHost.set(config.getZkHost());
        zkPort.set(config.getZkPort());
        zkAlias.set(config.getZkAlias());
        acl.set(config.getAcl());

        sshEnabled.set(config.isSshEnabled());
        sshServer.set(config.getSshServer());
        sshServerPort.set(config.getSshServerPort());
        sshUsername.set(config.getSshUsername());
        sshPassword.set(config.getSshPassword());
        sshKeyFilePath.set(config.getSshKeyFilePath());
        remoteServer.set(config.getRemoteServer());
        remoteServerPort.set(config.getRemoteServerPort());
        enableConnectionAdvanceConfiguration.set(config.isEnableConnectionAdvanceConfiguration());
        ConnectionConfigurationVO connectionConfig = config.getConnectionConfiguration();
        connectionConfiguration.get().setConnectionTimeout(connectionConfig.getConnectionTimeout());
        connectionConfiguration.get().setMaxRetries(connectionConfig.getMaxRetries());
        connectionConfiguration.get().setRetryIntervalTime(connectionConfig.getRetryIntervalTime());
        connectionConfiguration.get().setSessionTimeout(connectionConfig.getSessionTimeout());
    }

    public void reset() {
        id.set("");
        zkHost.set("");
        zkPort.set(2181);
        zkAlias.set("");
        acl.set("");
        sshEnabled.set(false);
        sshServer.set("");
        sshServerPort.set(22);
        sshUsername.set("");
        sshPassword.set("");
        sshKeyFilePath.set("");
        remoteServer.set("");
        remoteServerPort.set(2181);
        enableConnectionAdvanceConfiguration.set(false);
        connectionConfiguration.get().setConnectionTimeout(5000);
        connectionConfiguration.get().setSessionTimeout(6000);
        connectionConfiguration.get().setMaxRetries(3);
        connectionConfiguration.get().setRetryIntervalTime(1000);
    }

    public void unbind() {
        zkHost.unbind();
        zkPort.unbind();
        zkAlias.unbind();
        acl.unbind();
        sshEnabled.unbind();
        sshServer.unbind();
        sshServerPort.unbind();
        sshUsername.unbind();
        sshPassword.unbind();
        remoteServer.unbind();
        remoteServerPort.unbind();
        enableConnectionAdvanceConfiguration.unbind();
        connectionConfiguration.unbind();
    }

    public String getId() {
        return id.get();
    }

    public SimpleStringProperty idProperty() {
        return id;
    }

    public void setId(String id) {
        this.id.set(id);
    }

    public String getZkHost() {
        return zkHost.get();
    }

    public SimpleStringProperty zkHostProperty() {
        return zkHost;
    }

    public void setZkHost(String zkHost) {
        this.zkHost.set(zkHost);
    }

    public int getZkPort() {
        return zkPort.get();
    }

    public SimpleIntegerProperty zkPortProperty() {
        return zkPort;
    }

    public void setZkPort(int zkPort) {
        this.zkPort.set(zkPort);
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

    public String getAcl() {
        return acl.get();
    }

    public StringProperty aclProperty() {
        return acl;
    }

    public void setAcl(String acl) {
        this.acl.set(acl);
    }

    public ServerStatus getStatus() {
        return status.get();
    }

    public SimpleObjectProperty<ServerStatus> statusProperty() {
        return status;
    }

    public void setStatus(ServerStatus status) {
        this.status.set(status);
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

    public int getSshServerPort() {
        return sshServerPort.get();
    }

    public SimpleIntegerProperty sshServerPortProperty() {
        return sshServerPort;
    }

    public void setSshServerPort(int sshServerPort) {
        this.sshServerPort.set(sshServerPort);
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

    public String getSshKeyFilePath() {
        return sshKeyFilePath.get();
    }

    public SimpleStringProperty sshKeyFilePathProperty() {
        return sshKeyFilePath;
    }

    public void setSshKeyFilePath(String sshKeyFilePath) {
        this.sshKeyFilePath.set(sshKeyFilePath);
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

    public int getRemoteServerPort() {
        return remoteServerPort.get();
    }

    public SimpleIntegerProperty remoteServerPortProperty() {
        return remoteServerPort;
    }

    public void setRemoteServerPort(int remoteServerPort) {
        this.remoteServerPort.set(remoteServerPort);
    }

    public boolean isEnableConnectionAdvanceConfiguration() {
        return enableConnectionAdvanceConfiguration.get();
    }

    public SimpleBooleanProperty enableConnectionAdvanceConfigurationProperty() {
        return enableConnectionAdvanceConfiguration;
    }

    public void setEnableConnectionAdvanceConfiguration(boolean enableConnectionAdvanceConfiguration) {
        this.enableConnectionAdvanceConfiguration.set(enableConnectionAdvanceConfiguration);
    }

    public ConnectionConfigurationVO getConnectionConfiguration() {
        return connectionConfiguration.get();
    }

    public ObjectProperty<ConnectionConfigurationVO> connectionConfigurationProperty() {
        return connectionConfiguration;
    }

    public void setConnectionConfiguration(ConnectionConfigurationVO connectionConfiguration) {
        this.connectionConfiguration.set(connectionConfiguration);
    }
}
