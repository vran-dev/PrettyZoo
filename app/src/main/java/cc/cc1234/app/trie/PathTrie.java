package cc.cc1234.app.trie;

import cc.cc1234.spi.node.ZkNode;
import javafx.scene.control.TreeItem;

import java.util.*;

public class PathTrie {

    private Entry root;

    private static class Entry {

        private String name;

        private Entry parent;

        private TreeItem<ZkNode> treeItem;

        private List<Entry> subEntries;

    }

    public PathTrie() {
        final Entry rootEntry = new Entry();
        rootEntry.name = "/";
        rootEntry.subEntries = new LinkedList<>();
        root = rootEntry;
    }

    public static PathTrie map(Map<String, TreeItem<ZkNode>> tree) {
        PathTrie trie = new PathTrie();
        trie.root.treeItem = tree.get("/");
        tree.forEach((key, value) -> {
            final Entry entry = createIfNotExists(trie.root, key.split("/"), 0);
            entry.treeItem = value;
        });
        return trie;
    }

    private static Entry createIfNotExists(Entry parent, String[] pathArray, int depth) {
        if (depth == pathArray.length) {
            return parent;
        }

        for (Entry subEntry : parent.subEntries) {
            if (subEntry.equals(pathArray[depth])) {
                return createIfNotExists(subEntry, pathArray, depth + 1);
            }
        }
        Entry temp = tempEntry(pathArray[depth]);
        parent.subEntries.add(temp);
        temp.parent = parent;
        return createIfNotExists(temp, pathArray, depth + 1);
    }

    private static Entry tempEntry(String name) {
        final Entry temp = new Entry();
        temp.name = name;
        temp.subEntries = new LinkedList<>();
        return temp;
    }

    private Optional<Entry> find(String path) {
        final String[] pathArray = path.split("/");
        final List<Entry> entries = root.subEntries;
        return isMatch(entries, 0, pathArray);
    }

    private Optional<Entry> isMatch(List<Entry> curr, int i, String[] pathArray) {
        if (curr == null || curr.isEmpty() || i == pathArray.length) {
            return Optional.empty();
        }

        for (Entry entry : curr) {
            if (entry.name.equals(pathArray[i])) {
                if (i == pathArray.length - 1) {
                    return Optional.of(entry);
                } else {
                    return isMatch(entry.subEntries, i + 1, pathArray);
                }
            } else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    public List<TreeItem<ZkNode>> search(String keyword) {
        Entry curr = root;
        List<TreeItem<ZkNode>> result = new LinkedList<>();
        search(curr, keyword, result);
        return result;
    }

    private void search(Entry entry, String keyword, List<TreeItem<ZkNode>> result) {
        if (entry != null) {
            if (entry.name.contains(keyword) && entry.treeItem != null) {
                result.add(entry.treeItem);
            }
            entry.subEntries.forEach(subEntry -> search(subEntry, keyword, result));
        }
    }

    public void add(String path, TreeItem<ZkNode> item) {
        final Entry entry = createIfNotExists(root, path.split("/"), 0);
        entry.treeItem = item;
    }

    public void remove(String path) {
        final String[] pathArray = path.split("/");
        isMatch(root.subEntries, 0, pathArray).ifPresent(e -> e.parent.subEntries.remove(e));
    }

}
