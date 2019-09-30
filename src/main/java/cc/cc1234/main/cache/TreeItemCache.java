package cc.cc1234.main.cache;

import javafx.scene.control.TreeItem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TreeItemCache {

    private static final Map<String, Map<String, TreeItem>> CACHE = new ConcurrentHashMap<>();

    private static final TreeItemCache TREE_ITEM_CACHE = new TreeItemCache();

    public static TreeItemCache getInstance() {
        return TREE_ITEM_CACHE;
    }

    public boolean hasServer(String server) {
        return CACHE.containsKey(server);
    }

    public void cacheItemByPath(String server, String path, TreeItem item) {
        final Map<String, TreeItem> map = CACHE.computeIfAbsent(server, key -> new ConcurrentHashMap<>());
        map.put(path, item);
    }

    public TreeItem getItemByPath(String server, String path) {
        return CACHE.get(server).get(path);
    }
}
