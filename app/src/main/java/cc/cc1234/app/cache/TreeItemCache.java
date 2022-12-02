package cc.cc1234.app.cache;

import cc.cc1234.app.trie.PathTrie;
import cc.cc1234.specification.node.ZkNode;
import javafx.scene.control.TreeItem;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TreeItemCache {

    private static final Map<String, PathTrie<TreeItem<ZkNode>>> CACHE = new ConcurrentHashMap<>();

    private static final TreeItemCache INSTANCE = new TreeItemCache();

    private TreeItemCache() {
    }

    public static TreeItemCache getInstance() {
        return INSTANCE;
    }

    public boolean exists(String serverId) {
        return CACHE.containsKey(serverId);
    }

    public boolean hasNode(String serverId, String path) {
        return exists(serverId) && get(serverId, path) != null;
    }

    public void add(String server, String path, TreeItem<ZkNode> item) {
        var pathTrie = CACHE.computeIfAbsent(server, key -> new PathTrie<>());
        pathTrie.add(path, item);
    }

    public List<TreeItem<ZkNode>> search(String id, String node) {
        if (id == null || !this.exists(id)) {
            return Collections.emptyList();
        }
        return CACHE.get(id).search(node);
    }

    public TreeItem<ZkNode> get(String serverId, String path) {
        return CACHE.get(serverId).getByPath(path);
    }

    public void remove(String serverId, String path) {
        CACHE.get(serverId).remove(path);
    }

    public void remove(String serverId) {
        CACHE.remove(serverId);
    }
}
