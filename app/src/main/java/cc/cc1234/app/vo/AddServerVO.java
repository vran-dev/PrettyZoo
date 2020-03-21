package cc.cc1234.app.vo;

import cc.cc1234.facade.PrettyZooFacade;
import cc.cc1234.spi.config.model.SSHTunnelConfig;
import cc.cc1234.spi.config.model.ServerConfig;
import com.google.common.base.Strings;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class AddServerVO {

    private PrettyZooFacade prettyZooFacade = new PrettyZooFacade();

    private StringProperty host = new SimpleStringProperty();

    private StringProperty acl = new SimpleStringProperty();

    private BooleanProperty useSSH = new SimpleBooleanProperty(false);

    private StringProperty sshServerHost = new SimpleStringProperty();

    private StringProperty sshUsername = new SimpleStringProperty();

    private StringProperty sshPassword = new SimpleStringProperty();

    private StringProperty remoteServerHost = new SimpleStringProperty();

    public void onConfirm() {
        final ServerConfig zkServerConfig = new ServerConfig();
        zkServerConfig.setHost(getHost());

        if (isUseSSH()) {
            SSHTunnelConfig sshTunnelConfig = new SSHTunnelConfig();
            String[] zkHost = getHost().split(":");
            sshTunnelConfig.setLocalhost(zkHost[0]);
            sshTunnelConfig.setLocalPort(Integer.parseInt(zkHost[1]));

            String[] sshHostAndPort = getSshServerHost().split(":");
            sshTunnelConfig.setSshHost(sshHostAndPort[0]);
            sshTunnelConfig.setSshPort(Integer.parseInt(sshHostAndPort[1]));
            sshTunnelConfig.setSshUsername(getSshUsername());
            sshTunnelConfig.setPassword(getSshPassword());

            String[] remoteHostAndPort = getRemoteServerHost().split(":");
            sshTunnelConfig.setRemoteHost(remoteHostAndPort[0]);
            sshTunnelConfig.setRemotePort(Integer.parseInt(remoteHostAndPort[1]));

            zkServerConfig.setSshTunnelConfig(Optional.of(sshTunnelConfig));
        }

        if (!Strings.isNullOrEmpty(acl.get())) {
            final List<String> acls = Arrays.stream(acl.get().split("\n"))
                    .filter(acl -> !Strings.isNullOrEmpty(acl))
                    .collect(Collectors.toList());
            zkServerConfig.getAclList().addAll(acls);
        }
        prettyZooFacade.addConfig(zkServerConfig);
    }

    public boolean exists() {
        return prettyZooFacade.hasServerConfig(getHost());
    }

    public String getHost() {
        return host.get();
    }

    public StringProperty hostProperty() {
        return host;
    }

    public void setHost(String host) {
        this.host.set(host);
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

    public boolean isUseSSH() {
        return useSSH.get();
    }

    public BooleanProperty useSSHProperty() {
        return useSSH;
    }

    public void setUseSSH(boolean useSSH) {
        this.useSSH.set(useSSH);
    }

    public String getSshServerHost() {
        return sshServerHost.get();
    }

    public StringProperty sshServerHostProperty() {
        return sshServerHost;
    }

    public void setSshServerHost(String sshServerHost) {
        this.sshServerHost.set(sshServerHost);
    }

    public String getRemoteServerHost() {
        return remoteServerHost.get();
    }

    public StringProperty remoteServerHostProperty() {
        return remoteServerHost;
    }

    public void setRemoteServerHost(String remoteServerHost) {
        this.remoteServerHost.set(remoteServerHost);
    }

    public String getSshUsername() {
        return sshUsername.get();
    }

    public StringProperty sshUsernameProperty() {
        return sshUsername;
    }

    public void setSshUsername(String sshUsername) {
        this.sshUsername.set(sshUsername);
    }

    public String getSshPassword() {
        return sshPassword.get();
    }

    public StringProperty sshPasswordProperty() {
        return sshPassword;
    }

    public void setSshPassword(String sshPassword) {
        this.sshPassword.set(sshPassword);
    }
}
