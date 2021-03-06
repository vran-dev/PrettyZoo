package cc.cc1234.app.cache;

import cc.cc1234.app.trie.PathTrie;
import cc.cc1234.specification.node.ZkNode;
import javafx.scene.control.TreeItem;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TreeItemCache {

    /**
     * [ host : [ path : TreeItem ] ]
     */
    private static final Map<String, Map<String, TreeItem<ZkNode>>> treeItemCache = new ConcurrentHashMap<>();

    // TODO combine pathTreeCache and treeItemCache
    private static final Map<String, PathTrie<TreeItem<ZkNode>>> pathTreeCache = new ConcurrentHashMap<>();

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
        var map = treeItemCache.computeIfAbsent(server, key -> new ConcurrentHashMap<>());
        var pathTrie = pathTreeCache.computeIfAbsent(server, key -> new PathTrie<>());
        map.put(path, item);
        pathTrie.add(path, item);
    }

    public List<TreeItem<ZkNode>> search(String host, String node) {
        if (host == null || !treeItemCache.containsKey(host)) {
            return Collections.emptyList();
        }
        return pathTreeCache.get(host).search(node);
    }

    public TreeItem<ZkNode> get(String server, String path) {
        return treeItemCache.get(server).get(path);
    }

    public void remove(String server, String path) {
        treeItemCache.get(server).remove(path);
        pathTreeCache.get(server).remove(path);
    }

    public void remove(String server) {
        treeItemCache.remove(server);
        pathTreeCache.remove(server);
    }
}
