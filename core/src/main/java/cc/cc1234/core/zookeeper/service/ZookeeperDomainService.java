package cc.cc1234.core.zookeeper.service;

import cc.cc1234.core.configuration.entity.ServerConfiguration;
import cc.cc1234.core.zookeeper.entity.FourLetterCommand;
import cc.cc1234.core.zookeeper.entity.Terminal;
import cc.cc1234.core.zookeeper.entity.Zookeeper;
import cc.cc1234.core.zookeeper.factory.ZookeeperFactory;
import cc.cc1234.specification.listener.ServerListener;
import cc.cc1234.specification.listener.ZookeeperNodeListener;
import cc.cc1234.specification.node.NodeMode;
import cc.cc1234.specification.util.StringWriter;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.data.Stat;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class ZookeeperDomainService {

    private static final Map<String, Zookeeper> zookeeperMap = new ConcurrentHashMap<>();

    private static final Map<String, Terminal> terminalMap = new ConcurrentHashMap<>();

    public void connect(ServerConfiguration serverConfig,
                        List<ZookeeperNodeListener> nodeListeners,
                        List<ServerListener> serverListeners) {
        if (!zookeeperMap.containsKey(serverConfig.getId())) {
            Zookeeper zookeeper = new ZookeeperFactory().create(serverConfig, nodeListeners, serverListeners);
            zookeeperMap.put(serverConfig.getId(), zookeeper);
        }
    }

    public void disconnect(String id) {
        if (zookeeperMap.containsKey(id)) {
            zookeeperMap.get(id).disconnect();
            zookeeperMap.remove(id);
        }
        // TODO 考虑将终端独立化
        closeTerminal(id);
    }

    public void disconnectAll() {
        zookeeperMap.values().forEach(Zookeeper::disconnect);
        zookeeperMap.clear();
    }

    public void sync(String serverId) {
        assertZookeeperExists(serverId);
        zookeeperMap.get(serverId).sync();
    }

    public Stat set(String serverId, String path, String data) {
        assertZookeeperExists(serverId);
        try {
            return zookeeperMap.get(serverId).set(path, data);
        } catch (Exception e) {
            log.error("set data error " + serverId + " -> " + path, e);
            throw new IllegalStateException(e);
        }
    }

    public void delete(String serverId, List<String> pathList) throws Exception {
        Objects.requireNonNull(pathList);
        assertZookeeperExists(serverId);
        if (pathList.size() < 20) {
            for (String path : pathList) {
                zookeeperMap.get(serverId).delete(path);
            }
        } else {
            zookeeperMap.get(serverId).deleteAsync(pathList);
        }
    }

    public void create(String serverId, String path, String data, NodeMode mode) throws Exception {
        assertZookeeperExists(serverId);
        zookeeperMap.get(serverId).create(path, data, mode);
    }

    private void assertZookeeperExists(String serverId) {
        if (!zookeeperMap.containsKey(serverId)) {
            throw new IllegalStateException("connect zookeeper first " + serverId);
        }
    }

    public void initTerminal(String id, String url, StringWriter writer) {
        if (!terminalMap.containsKey(id)) {
            try {
                final Terminal terminal = new ZookeeperFactory().createTerminal(id, url, writer);
                terminalMap.put(id, terminal);
            } catch (Exception e) {
                log.error("init terminal error", e);
                throw new IllegalStateException(e);
            }
        }
    }

    public void closeTerminal(String id) {
        final Terminal terminal = terminalMap.remove(id);
        if (terminal != null) {
            terminal.close();
        }
    }

    public void closeAllTerminal() {
        Set<String> terminals = new HashSet<>(terminalMap.keySet());
        terminals.forEach(this::closeTerminal);
    }

    public void execute(String serverId, String command) throws Exception {
        final Terminal terminal = terminalMap.get(serverId);
        terminal.execute(command);
    }

    public String execute4LetterCommand(String serverId, String url, String command) {
        if (command == null || "".equals(command)) {
            throw new IllegalArgumentException("command must not be empty");
        }
        final String[] hostAndPort = url.split(":");
        return new FourLetterCommand(hostAndPort[0], Integer.parseInt(hostAndPort[1])).request(command);
    }
}
