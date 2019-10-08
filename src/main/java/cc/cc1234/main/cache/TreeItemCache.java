package cc.cc1234.main.cache;

import cc.cc1234.main.model.ZkNode;
import javafx.scene.control.TreeItem;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TreeItemCache {

    /**
     * [ host : [ path : TreeItem ] ]
     */
    private static final Map<String, Map<String, TreeItem<ZkNode>>> treeItemCache = new ConcurrentHashMap<>();

    private static final TreeItemCache INSTANCE = new TreeItemCache();

    private TreeItemCache() {
    }

    public static TreeItemCache getInstance() {
        return INSTANCE;
    }

    public boolean hasServer(String server) {
        return treeItemCache.containsKey(server);
    }

    public boolean hasNode(String server, String path) {
        return hasServer(server) && get(server, path) != null;
    }

    public void add(String server, String path, TreeItem<ZkNode> item) {
        final Map<String, TreeItem<ZkNode>> map = treeItemCache.computeIfAbsent(server, key -> new ConcurrentHashMap<>());
        map.put(path, item);
    }

    public TreeItem<ZkNode> get(String server, String path) {
        return treeItemCache.get(server).get(path);
    }

    public void remove(String server, String path) {
        treeItemCache.get(server).remove(path);
    }

}
