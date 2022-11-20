package cc.cc1234.core.zookeeper.entity;

import lombok.Builder;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.LocalPortForwarder;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

@Builder
@Getter
@Slf4j
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

    public void createAsync() {
        try {
            sshClient = new SSHClient();
            sshClient.addHostKeyVerifier(new PromiscuousVerifier());
            sshClient.connect(getSshHost(), getSshPort());
            sshClient.authPassword(getSshUsername(), getSshPassword());

            proxySocket = new ServerSocket();
            proxySocket.setReuseAddress(true);
            proxySocket.bind(new InetSocketAddress(localhost, localPort));
            new Thread(() -> {
                try {
                    var param = new LocalPortForwarder.Parameters(localhost, localPort, remoteHost, remotePort);
                    sshClient.newLocalPortForwarder(param, proxySocket).listen();
                } catch (IOException e) {
                    throw new IllegalStateException(e.getMessage(), e);
                }
            }).start();
        } catch (IOException e) {
            if (e.getClass().getSimpleName().contains("Timeout")) {
                throw new IllegalStateException("SSH connect error by timeout: " + sshHost, e);
            }
            if (e.getClass().getSimpleName().contains("UnknownHost")) {
                throw new IllegalStateException("SSH connect error by Unknown host " + sshHost, e);
            }
            log.error("create ssh-tunnel failed", e);
            throw new IllegalStateException("create ssh-tunnel failed", e);
        }
    }

    public void blockUntilConnected() {
        // block until connected
        try {
            int times = 1;
            while (!isConnected() && times < 7) {
                log.info("Try to connect SSH-Tunnel " + times + " times, tunnel = " + this);
                Thread.sleep(1000);
                times++;
            }
        } catch (Exception e) {
            this.close();
            throw new IllegalStateException(e);
        }

        if (!isConnected()) {
            this.close();
            throw new IllegalStateException("connect SSH Tunnel failed");
        }
    }

    public boolean isConnected() {
        return sshClient.isConnected();
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
