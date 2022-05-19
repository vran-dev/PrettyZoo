package cc.cc1234.app.trie;

import java.util.*;

public class PathTrie<T> {

    private Entry<T> root;

    private Map<String, T> pathAndDataMap = new HashMap<>();

    public PathTrie() {
        final Entry<T> rootEntry = new Entry<>();
        rootEntry.name = "/";
        rootEntry.subEntries = new LinkedList<>();
        root = rootEntry;
        pathAndDataMap.put("/", root.data);
    }

    public static <T> PathTrie<T> of(Map<String, T> map) {
        PathTrie<T> trie = new PathTrie<>();
        trie.root.data = map.get("/");
        map.forEach((key, value) -> {
            Entry<T> entry = buildEntries(trie.root, paths(key), 0);
            entry.data = value;
        });
        return trie;
    }

    private static <T> Entry<T> buildEntries(Entry<T> parent, String[] pathArray, int depth) {
        if (depth == pathArray.length) {
            return parent;
        }

        for (Entry<T> subEntry : parent.subEntries) {
            if (subEntry.name.equals(pathArray[depth])) {
                return buildEntries(subEntry, pathArray, depth + 1);
            }
        }
        Entry<T> virtualEntry = virtualEntry(pathArray[depth]);
        parent.subEntries.add(virtualEntry);
        virtualEntry.parent = parent;
        return buildEntries(virtualEntry, pathArray, depth + 1);
    }

    private static <T> Entry<T> virtualEntry(String name) {
        final Entry<T> temp = new Entry<T>();
        temp.name = name;
        temp.subEntries = new LinkedList<>();
        return temp;
    }

    private static String[] paths(String path) {
        if (path.startsWith("/")) {
            return path.substring(1).split("/");
        } else {
            return path.split("/");
        }
    }

    public boolean contains(String path) {
        return findEntry(path).isPresent();
    }

    public Optional<T> find(String path) {
        return findEntry(path).map(e -> e.data);
    }

    private Optional<Entry<T>> findEntry(String path) {
        final String[] pathArray = paths(path);
        final List<Entry<T>> entries = root.subEntries;
        return isMatch(entries, 0, pathArray);
    }

    private Optional<Entry<T>> isMatch(List<Entry<T>> curr, int i, String[] pathArray) {
        if (curr == null || curr.isEmpty() || i == pathArray.length) {
            return Optional.empty();
        }

        for (Entry<T> entry : curr) {
            if (entry.name.equals(pathArray[i])) {
                if (i == pathArray.length - 1) {
                    return Optional.of(entry);
                } else {
                    return isMatch(entry.subEntries, i + 1, pathArray);
                }
            }
        }
        return Optional.empty();
    }

    public List<T> search(String keyword) {
        Entry<T> curr = root;
        List<T> result = new LinkedList<>();
        search(curr, keyword, result);
        return result;
    }

    private void search(Entry<T> entry, String keyword, List<T> result) {
        if (entry != null) {
            if (entry.name.contains(keyword) && entry.data != null) {
                result.add(entry.data);
            }
            entry.subEntries.forEach(subEntry -> search(subEntry, keyword, result));
        }
    }

    public void add(String path, T data) {
        final Entry<T> entry = buildEntries(root, paths(path), 0);
        entry.data = data;
        this.pathAndDataMap.put(path, data);
    }

    public void remove(String path) {
        final String[] pathArray = paths(path);
        isMatch(root.subEntries, 0, pathArray).ifPresent(e -> e.parent.subEntries.remove(e));
        this.pathAndDataMap.remove(path);
    }

    public T getByPath(String path) {
        return this.pathAndDataMap.get(path);
    }

    private static class Entry<T> {

        private String name;

        private Entry<T> parent;

        private T data;

        private List<Entry<T>> subEntries = new ArrayList<>();

        @Override
        public String toString() {
            return parent == null ? name : parent.name + "/" + name;
        }
    }

}
