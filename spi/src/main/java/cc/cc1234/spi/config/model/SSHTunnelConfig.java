package cc.cc1234.spi.config.model;

public class SSHTunnelConfig {

    private static final int DEFAULT_SSH_PORT = 22;

    private static final String DEFAULT_LOCALHOST = "127.0.0.1";

    private String localhost = DEFAULT_LOCALHOST;

    private int localPort;

    private String sshHost;

    private int sshPort = DEFAULT_SSH_PORT;

    private String remoteHost;

    private int remotePort;

    private String sshUsername;

    private String password;

    public String getLocalhost() {
        return localhost;
    }

    public void setLocalhost(String localhost) {
        this.localhost = localhost;
    }

    public int getLocalPort() {
        return localPort;
    }

    public void setLocalPort(int localPort) {
        this.localPort = localPort;
    }

    public String getSshHost() {
        return sshHost;
    }

    public void setSshHost(String sshHost) {
        this.sshHost = sshHost;
    }

    public int getSshPort() {
        return sshPort;
    }

    public void setSshPort(int sshPort) {
        this.sshPort = sshPort;
    }

    public String getRemoteHost() {
        return remoteHost;
    }

    public void setRemoteHost(String remoteHost) {
        this.remoteHost = remoteHost;
    }

    public int getRemotePort() {
        return remotePort;
    }

    public void setRemotePort(int remotePort) {
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
                '}';
    }
}
