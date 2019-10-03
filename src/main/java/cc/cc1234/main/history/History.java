package cc.cc1234.main.history;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

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

    public void store() {
        try {
            properties.store(Files.newBufferedWriter(Paths.get(file)), "");
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public boolean remove(String key) {
        return properties.remove(key) != null;
    }

    public void clear() {
        properties.clear();
        store();
    }
}
