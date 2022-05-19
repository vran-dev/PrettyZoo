package cc.cc1234.app.cache;

import cc.cc1234.app.trie.PathTrie;
import cc.cc1234.specification.node.ZkNode;
import javafx.scene.control.TreeItem;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TreeItemCache {

    // TODO combine pathTreeCache and treeItemCache
    private static final Map<String, PathTrie<TreeItem<ZkNode>>> pathTreeCache = new ConcurrentHashMap<>();

    private static final TreeItemCache INSTANCE = new TreeItemCache();

    private TreeItemCache() {
    }

    public static TreeItemCache getInstance() {
        return INSTANCE;
    }

    public boolean hasServer(String server) {
        return pathTreeCache.containsKey(server);
    }

    public boolean hasNode(String server, String path) {
        return hasServer(server) && get(server, path) != null;
    }

    public void add(String server, String path, TreeItem<ZkNode> item) {
        var pathTrie = pathTreeCache.computeIfAbsent(server, key -> new PathTrie<>());
        pathTrie.add(path, item);
    }

    public List<TreeItem<ZkNode>> search(String host, String node) {
        if (host == null || !this.hasServer(host)) {
            return Collections.emptyList();
        }
        return pathTreeCache.get(host).search(node);
    }

    public TreeItem<ZkNode> get(String server, String path) {
        return pathTreeCache.get(server).getByPath(path);
    }

    public void remove(String server, String path) {
        pathTreeCache.get(server).remove(path);
    }

    public void remove(String server) {
        pathTreeCache.remove(server);
    }
}
