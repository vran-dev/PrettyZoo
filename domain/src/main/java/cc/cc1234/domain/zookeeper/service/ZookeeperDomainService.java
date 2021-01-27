package cc.cc1234.domain.zookeeper.service;

import cc.cc1234.domain.configuration.entity.ServerConfiguration;
import cc.cc1234.domain.zookeeper.entity.FourLetterCommand;
import cc.cc1234.domain.zookeeper.entity.Terminal;
import cc.cc1234.domain.zookeeper.entity.Zookeeper;
import cc.cc1234.domain.zookeeper.factory.ZookeeperFactory;
import cc.cc1234.spi.listener.ServerListener;
import cc.cc1234.spi.listener.ZookeeperNodeListener;
import cc.cc1234.spi.node.NodeMode;
import cc.cc1234.spi.util.StringWriter;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ZookeeperDomainService {

    private static final Map<String, Zookeeper> zookeeperMap = new ConcurrentHashMap<>();

    private static final Map<String, Terminal> terminalMap = new ConcurrentHashMap<>();

    public void connect(ServerConfiguration serverConfig,
                        List<ZookeeperNodeListener> nodeListeners,
                        List<ServerListener> serverListeners) {
        if (!zookeeperMap.containsKey(serverConfig.getHost())) {
            Zookeeper zookeeper = new ZookeeperFactory().create(serverConfig, nodeListeners, serverListeners);
            zookeeperMap.put(serverConfig.getHost(), zookeeper);
        }
    }

    public void disconnect(String host) {
        if (zookeeperMap.containsKey(host)) {
            zookeeperMap.get(host).disconnect();
            zookeeperMap.remove(host);
        }
        // TODO 考虑将终端独立化
        closeTerminal(host);
    }

    public void disconnectAll() {
        zookeeperMap.values().forEach(Zookeeper::disconnect);
        zookeeperMap.clear();
    }

    public void sync(String host) {
        assertZookeeperExists(host);
        zookeeperMap.get(host).sync();
    }

    public void set(String host, String path, String data) throws Exception {
        assertZookeeperExists(host);
        zookeeperMap.get(host).set(path, data);
    }

    public void delete(String host, String path) throws Exception {
        assertZookeeperExists(host);
        zookeeperMap.get(host).delete(path);
    }

    public void create(String host, String path, String data, NodeMode mode) throws Exception {
        assertZookeeperExists(host);
        zookeeperMap.get(host).create(path, data, mode);
    }

    private void assertZookeeperExists(String host) {
        if (!zookeeperMap.containsKey(host)) {
            throw new IllegalStateException("connect zookeeper first " + host);
        }
    }

    public void initTerminal(String host, StringWriter writer) {
        if (!terminalMap.containsKey(host)) {
            try {
                final Terminal terminal = new ZookeeperFactory().createTerminal(host, writer);
                terminalMap.put(host, terminal);
            } catch (Exception e) {
                throw new IllegalStateException(e);
            }
        }
    }

    public void closeTerminal(String host) {
        final Terminal terminal = terminalMap.remove(host);
        if (terminal != null) {
            terminal.close();
        }
    }

    public void closeAllTerminal() {
        Set<String> terminals = new HashSet<>(terminalMap.keySet());
        terminals.forEach(this::closeTerminal);
    }

    public void execute(String host, String command) throws Exception {
        final Terminal terminal = terminalMap.get(host);
        terminal.execute(command);
    }

    public String execute4LetterCommand(String host, String command) {
        final String[] hostAndPort = host.split(":");
        return new FourLetterCommand(hostAndPort[0], Integer.parseInt(hostAndPort[1])).request(command);
    }
}
