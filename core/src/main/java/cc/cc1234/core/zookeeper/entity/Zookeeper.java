package cc.cc1234.core.zookeeper.entity;

import cc.cc1234.specification.connection.ZookeeperConnection;
import cc.cc1234.specification.listener.ServerListener;
import cc.cc1234.specification.listener.ZookeeperNodeListener;
import cc.cc1234.specification.node.NodeMode;
import lombok.Getter;
import org.apache.zookeeper.data.Stat;

import java.util.List;
import java.util.Objects;
import java.util.function.Supplier;

public class Zookeeper {

    @Getter
    private final String host;

    @Getter
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

    public Stat set(String path, String data) throws Exception {
        return connection.setData(path, data);
    }

    public void delete(String path) throws Exception {
        connection.delete(path, true);
    }

    public void create(String path, String data, NodeMode mode) throws Exception {
        connection.create(path, data, true, mode);
    }
}
