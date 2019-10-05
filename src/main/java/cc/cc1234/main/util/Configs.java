package cc.cc1234.main.util;

import cc.cc1234.main.model.PrettyZooConfig;
import cc.cc1234.main.model.ZkServer;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class Configs {

    private static final String SERVER_HISTORY = "server-input.history";

    private static final String HISTORY_FORMAT = System.getProperty("user.home") + "/.prettyZoo/%s";

    public static ObservableList<ZkServer> getHistoryServers() {
        final Properties properties = getOrCreate();
        Map<String, String> copy = new HashMap<>();
        properties.forEach((key, value) -> copy.put(String.valueOf(key), String.valueOf(value)));
        final List<ZkServer> sortedZkServers = copy.entrySet()
                .stream()
                .map(e -> {
                    final ZkServer zkServer = new ZkServer(e.getKey());
                    zkServer.setConnectTimes(Integer.parseInt(e.getValue()));
                    return zkServer;
                })
                .sorted(Comparator.comparingInt(ZkServer::getConnectTimes))
                .collect(Collectors.toList());
        Collections.reverse(sortedZkServers);
        return FXCollections.observableArrayList(sortedZkServers);
    }

    private static Properties getOrCreate() {
        final String historyDst = String.format(HISTORY_FORMAT, SERVER_HISTORY);
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
            return properties;
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public static void store(PrettyZooConfig config) {
        final Map<String, String> prop = config.getServers()
                .stream()
                .collect(Collectors.toMap(ZkServer::getServer, z -> z.getConnectTimes() + ""));
        final Properties properties = new Properties();
        properties.putAll(prop);
        store(properties);
    }

    private static void store(Properties properties) {
        try {
            final String historyDst = String.format(HISTORY_FORMAT, SERVER_HISTORY);
            properties.store(Files.newBufferedWriter(Paths.get(historyDst)), "");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


    public static void clear() {
        store(new Properties());
    }

}
