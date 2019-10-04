package cc.cc1234.main.history;

import cc.cc1234.main.model.ZkServer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class History {

    public static final String SERVER_HISTORY = "server-input.history";

    private static final String HISTORY_FORMAT = System.getProperty("user.home") + "/.prettyZoo/%s";

    private final Properties properties;

    private final String file;

    public History(String file, Properties properties) {
        this.file = file;
        this.properties = properties;
    }

    public static History createIfAbsent(String historyName) {
        final String historyDst = String.format(HISTORY_FORMAT, historyName);
        final Path path = Paths.get(historyDst);
        if (!Files.exists(path)) {
            try {
                Files.createDirectories(path.getParent());
                Files.createFile(path);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            }
        }
        final Properties properties = new Properties();
        try {
            properties.load(Files.newInputStream(path));
            return new History(historyDst, properties);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public ObservableList<ZkServer> getHistoryServers() {
        final List<ZkServer> sortedZkServers = this.getAll().entrySet()
                .stream()
                .map(e -> new ZkServerHistory(e.getKey(), e.getValue()))
                .sorted(Comparator.comparingInt(e -> e.times))
                .map(h -> new ZkServer(h.server))
                .collect(Collectors.toList());
        Collections.reverse(sortedZkServers);
        return FXCollections.observableArrayList(sortedZkServers);
    }

    public Map<String, String> getAll() {
        Map<String, String> copy = new HashMap<>();
        properties.forEach((key, value) -> {
            copy.put(String.valueOf(key), String.valueOf(value));
        });
        return copy;
    }

    public void save(String key, String value) {
        properties.put(key, value);
    }

    public boolean contains(String key) {
        return properties.containsKey(key);
    }

    public String get(String key, String def) {
        return properties.getProperty(key, def);
    }

    public History store() {
        try {
            properties.store(Files.newBufferedWriter(Paths.get(file)), "");
            return this;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public History remove(String key) {
        properties.remove(key);
        return this;
    }

    public History clear() {
        properties.clear();
        store();
        return this;
    }

    private static class ZkServerHistory {
        String server;
        int times;

        ZkServerHistory(String server, String times) {
            this.server = server;
            this.times = Integer.parseInt(times);
        }
    }
}
