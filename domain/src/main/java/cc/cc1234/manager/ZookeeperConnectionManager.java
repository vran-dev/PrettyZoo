package cc.cc1234.manager;

import cc.cc1234.client.curator.CuratorZookeeperConnectionFactory;
import cc.cc1234.spi.config.model.ServerConfig;
import cc.cc1234.spi.connection.ZookeeperConnection;
import cc.cc1234.spi.connection.ZookeeperConnectionFactory;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class ZookeeperConnectionManager {

    /**
     * singleton instance
     */
    private static final ZookeeperConnectionManager instance = new ZookeeperConnectionManager();

    private Map<String, ZookeeperConnection> connectionsMap = new ConcurrentHashMap<>();

    private ZookeeperConnectionFactory factory = new CuratorZookeeperConnectionFactory();

    public static ZookeeperConnectionManager instance() {
        return instance;
    }

    public Optional<ZookeeperConnection> getConnectionOpt(String server) {
        return Optional.ofNullable(connectionsMap.get(server));
    }

    public ZookeeperConnection getConnection(String server) {
        return connectionsMap.get(server);
    }

    private void saveConnection(String server, ZookeeperConnection connection) {
        connectionsMap.put(server, connection);
    }

    public void closeAll() {
        connectionsMap.values().forEach(ZookeeperConnection::close);
    }

    public void close(String server) {
        final ZookeeperConnection conn = connectionsMap.remove(server);
        if (conn != null) {
            conn.close();
        }
    }

    public ZookeeperConnection connect(ServerConfig serverConfig) throws Exception {
        if (connectionsMap.containsKey(serverConfig.getHost())) {
            return connectionsMap.get(serverConfig.getHost());
        }
        final ZookeeperConnection connection = factory.create(serverConfig);
        saveConnection(serverConfig.getHost(), connection);
        connectionsMap.put(serverConfig.getHost(), connection);
        return connection;
    }
}
