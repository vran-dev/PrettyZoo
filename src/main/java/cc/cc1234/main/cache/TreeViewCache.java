package cc.cc1234.main.cache;

import cc.cc1234.main.model.ZkNode;
import javafx.scene.control.TreeItem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TreeViewCache {

    private static final Map<String, Map<String, TreeItem<ZkNode>>> CACHE = new ConcurrentHashMap<>();

    private static final TreeViewCache INSTANCE = new TreeViewCache();

    private TreeViewCache() {
    }

    public static TreeViewCache getInstance() {
        return INSTANCE;
    }

    public boolean hasServer(String server) {
        return CACHE.containsKey(server);
    }

    public void add(String server, String path, TreeItem<ZkNode> item) {
        final Map<String, TreeItem<ZkNode>> map = CACHE.computeIfAbsent(server, key -> new ConcurrentHashMap<>());
        map.put(path, item);
    }

    public TreeItem<ZkNode> get(String server, String path) {
        return CACHE.get(server).get(path);
    }

    public void remove(String server, String path) {
        CACHE.get(server).remove(path);
    }

    public void clear(String server) {
        CACHE.remove(server);
    }
}
