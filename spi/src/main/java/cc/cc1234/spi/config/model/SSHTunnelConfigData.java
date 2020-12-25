package cc.cc1234.spi.config.model;

public class SSHTunnelConfigData {

    private String localhost;

    private Integer localPort;

    private String sshHost;

    private Integer sshPort;

    private String remoteHost;

    private Integer remotePort;

    private String sshUsername;

    private String password;

    public String getLocalhost() {
        return localhost;
    }

    public void setLocalhost(String localhost) {
        this.localhost = localhost;
    }

    public Integer getLocalPort() {
        return localPort;
    }

    public void setLocalPort(Integer localPort) {
        this.localPort = localPort;
    }

    public String getSshHost() {
        return sshHost;
    }

    public void setSshHost(String sshHost) {
        this.sshHost = sshHost;
    }

    public Integer getSshPort() {
        return sshPort;
    }

    public void setSshPort(Integer sshPort) {
        this.sshPort = sshPort;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public Integer getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(Integer remotePort) {
        this.remotePort = remotePort;
    }

    public String getSshUsername() {
        return sshUsername;
    }

    public void setSshUsername(String sshUsername) {
        this.sshUsername = sshUsername;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Override
    public String toString() {
        return "SSHTunnelConfig{" +
                "localhost='" + localhost + '\'' +
                ", localPort=" + localPort +
                ", sshHost='" + sshHost + '\'' +
                ", sshPort=" + sshPort +
                ", remoteHost='" + remoteHost + '\'' +
                ", remotePort=" + remotePort +
                ", sshUsername='" + sshUsername + '\'' +
                ", password='" + password + '\'' +
                '}';
    }
}
