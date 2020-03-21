
package cc.cc1234.manager;

import cc.cc1234.spi.config.model.SSHTunnelConfig;
import net.schmizz.sshj.SSHClient;
import net.schmizz.sshj.connection.channel.direct.LocalPortForwarder;
import net.schmizz.sshj.transport.verification.PromiscuousVerifier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class SSHTunnelManager {

    private static final Logger logger = LoggerFactory.getLogger(SSHTunnelManager.class);

    private static final SSHTunnelManager INSTANCE = new SSHTunnelManager();

    private Map<String, SSHTunnelConfig> configMap = new ConcurrentHashMap<>();

    private Map<String, ServerSocket> socketMap = new ConcurrentHashMap<>();

    private Map<String, SSHClient> sshClientMap = new ConcurrentHashMap<>();

    public static SSHTunnelManager instance() {
        return INSTANCE;
    }

    public void forwarding(SSHTunnelConfig sshTunnelConfig) {
        String key = sshTunnelConfig.getLocalhost() + ":" + sshTunnelConfig.getLocalPort();
        if (configMap.containsKey(key)) {
            logger.info("ignore  {}, because it was exists", sshTunnelConfig.toString());
            return;
        }

        logger.info("create ssh tunnel: " + sshTunnelConfig.toString());
        // TODO use JavaFX service to instead of
        new Thread(() -> {
            try {
                SSHClient sshClient = new SSHClient();
                sshClient.addHostKeyVerifier(new PromiscuousVerifier());
                sshClient.connect(sshTunnelConfig.getSshHost(), sshTunnelConfig.getSshPort());
                sshClient.authPassword(sshTunnelConfig.getSshUsername(), sshTunnelConfig.getPassword());
                LocalPortForwarder.Parameters param =
                        new LocalPortForwarder.Parameters(sshTunnelConfig.getLocalhost(), sshTunnelConfig.getLocalPort(),
                                sshTunnelConfig.getRemoteHost(), sshTunnelConfig.getRemotePort());
                ServerSocket socket = new ServerSocket();
                socket.setReuseAddress(true);
                socket.bind(new InetSocketAddress(param.getLocalHost(), param.getLocalPort()));
                sshClient.newLocalPortForwarder(param, socket).listen();
            } catch (IOException e) {
                logger.error("creat ssh tunnel failed", e);
            }
        }).start();
    }

    public void close(String host) {
        // TODO optimize and log error
        try {
            if (socketMap.containsKey(host)) {
                socketMap.get(host).close();
            }
        } catch (IOException e) {
            logger.error("close ssh tunnel socket error", e);
        }

        try {
            if (sshClientMap.containsKey(host)) {
                sshClientMap.get(host).close();
            }
        } catch (IOException e) {
            logger.error("close ssh tunnel error", e);
        }
    }

    public void closeAll() {
        socketMap.keySet().forEach(this::close);
        sshClientMap.keySet().forEach(this::close);
    }


}
