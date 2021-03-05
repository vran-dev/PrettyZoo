package cc.cc1234.core.zookeeper.entity;

import lombok.Builder;
import lombok.Getter;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.LocalPortForwarder;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

@Builder
@Getter
public class SSHTunnel {

    private String localhost;

    private int localPort;

    private String sshHost;

    private int sshPort;

    private String sshUsername;

    private String sshPassword;

    private String remoteHost;

    private int remotePort;

    private SSHClient sshClient;

    private ServerSocket proxySocket;

    public void create() {
        try {
            sshClient = new SSHClient();
            sshClient.addHostKeyVerifier(new PromiscuousVerifier());
            sshClient.connect(getSshHost(), getSshPort());
            sshClient.authPassword(getSshUsername(), getSshPassword());
            LocalPortForwarder.Parameters param = new LocalPortForwarder.Parameters(localhost, localPort, remoteHost, remotePort);

            proxySocket = new ServerSocket();
            proxySocket.setReuseAddress(true);
            proxySocket.bind(new InetSocketAddress(localhost, localPort));
            sshClient.newLocalPortForwarder(param, proxySocket).listen();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public void close() {
        if (proxySocket != null) {
            try {
                proxySocket.close();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }

        if (sshClient != null) {
            try {
                sshClient.close();
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
    }
}
