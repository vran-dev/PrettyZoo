package cc.cc1234.main.cache;

import cc.cc1234.main.model.ZkNode;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class TreeViewCache<T> {

    private static final Map<String, Map<String, TreeItem<ZkNode>>> CACHE = new ConcurrentHashMap<>();

    private static final TreeViewCache INSTANCE = new TreeViewCache<>();

    private TreeView<T> treeView;

    private TreeViewCache() {
    }

    public static <T> TreeViewCache<T> getInstance() {
        return INSTANCE;
    }

    public boolean hasServer(String server) {
        return CACHE.containsKey(server);
    }

    public void setTreeView(TreeView<T> treeView) {
        this.treeView = treeView;
    }

    public TreeView<T> getTreeView() {
        return treeView;
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
}
