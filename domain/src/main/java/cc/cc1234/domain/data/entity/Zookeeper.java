package cc.cc1234.domain.data.entity;

import cc.cc1234.spi.connection.ZookeeperConnection;
import cc.cc1234.spi.listener.ServerListener;
import cc.cc1234.spi.listener.ZookeeperNodeListener;
import org.apache.zookeeper.CreateMode;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class Zookeeper {

    private final String host;

    private final ZookeeperConnection connection;

    private final SSHTunnel sshTunnel;

    private List<ZookeeperNodeListener> nodeListeners = List.of();

    private List<ServerListener> serverListeners = List.of();

    public Zookeeper(String host,
                     Supplier<ZookeeperConnection> connectionSupplier,
                     SSHTunnel sshTunnel,
                     List<ZookeeperNodeListener> nodeListeners,
                     List<ServerListener> serverListeners) {
        Objects.requireNonNull(host);
        Objects.requireNonNull(connectionSupplier);
        this.host = host;
        this.sshTunnel = sshTunnel;
        if (sshTunnel != null) {
            sshTunnel.create();
        }
        this.connection = connectionSupplier.get();
        this.nodeListeners = nodeListeners;
        this.serverListeners = serverListeners;
    }

    public void disconnect() {
        if (sshTunnel != null) {
            sshTunnel.close();
        }
        connection.close();
        serverListeners.forEach(l -> l.onClose(host));
    }

    public void sync() {
        connection.sync(nodeListeners);
    }

    public void set(String path, String data) throws Exception {
        connection.setData(path, data);
    }

    public void delete(String path) throws Exception {
        connection.delete(path, true);
    }

    public void create(String path, String data, CreateMode mode) throws Exception {
        connection.create(path, data, true, mode);
    }
}
