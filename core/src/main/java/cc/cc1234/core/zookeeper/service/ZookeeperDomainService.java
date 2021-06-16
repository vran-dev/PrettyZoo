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
        if (!zookeeperMap.containsKey(serverConfig.getUrl())) {
            Zookeeper zookeeper = new ZookeeperFactory().create(serverConfig, nodeListeners, serverListeners);
            zookeeperMap.put(serverConfig.getUrl(), zookeeper);
        }
    }

    public void disconnect(String url) {
        if (zookeeperMap.containsKey(url)) {
            zookeeperMap.get(url).disconnect();
            zookeeperMap.remove(url);
        }
        // TODO 考虑将终端独立化
        closeTerminal(url);
    }

    public void disconnectAll() {
        zookeeperMap.values().forEach(Zookeeper::disconnect);
        zookeeperMap.clear();
    }

    public void sync(String url) {
        assertZookeeperExists(url);
        zookeeperMap.get(url).sync();
    }

    public Stat set(String url, String path, String data) {
        assertZookeeperExists(url);
        try {
            return zookeeperMap.get(url).set(path, data);
        } catch (Exception e) {
            log.error("set data error " + url + " -> " + path, e);
            throw new IllegalStateException(e);
        }
    }

    public void delete(String url, List<String> pathList) throws Exception {
        Objects.requireNonNull(pathList);
        assertZookeeperExists(url);
        if (pathList.size() < 20) {
            for (String path : pathList) {
                zookeeperMap.get(url).delete(path);
            }
        } else {
            zookeeperMap.get(url).deleteAsync(pathList);
        }
    }

    public void create(String url, String path, String data, NodeMode mode) throws Exception {
        assertZookeeperExists(url);
        zookeeperMap.get(url).create(path, data, mode);
    }

    private void assertZookeeperExists(String url) {
        if (!zookeeperMap.containsKey(url)) {
            throw new IllegalStateException("connect zookeeper first " + url);
        }
    }

    public void initTerminal(String url, StringWriter writer) {
        if (!terminalMap.containsKey(url)) {
            try {
                final Terminal terminal = new ZookeeperFactory().createTerminal(url, writer);
                terminalMap.put(url, terminal);
            } catch (Exception e) {
                log.error("init terminal error", e);
                throw new IllegalStateException(e);
            }
        }
    }

    public void closeTerminal(String url) {
        final Terminal terminal = terminalMap.remove(url);
        if (terminal != null) {
            terminal.close();
        }
    }

    public void closeAllTerminal() {
        Set<String> terminals = new HashSet<>(terminalMap.keySet());
        terminals.forEach(this::closeTerminal);
    }

    public void execute(String url, String command) throws Exception {
        final Terminal terminal = terminalMap.get(url);
        terminal.execute(command);
    }

    public String execute4LetterCommand(String url, String command) {
        if (command == null || "".equals(command)) {
            throw new IllegalArgumentException("command must not be empty");
        }
        final String[] hostAndPort = url.split(":");
        return new FourLetterCommand(hostAndPort[0], Integer.parseInt(hostAndPort[1])).request(command);
    }
}
