package cc.cc1234.main.cache;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CuratorCache {

    private static final Map<String, CuratorFramework> curatorClientCache = new ConcurrentHashMap<>();

    private static final Map<String, TreeCache> curatorTreeCache = new ConcurrentHashMap<>();

    public static boolean put(String host, CuratorFramework client) {
        curatorClientCache.put(host, client);
        return true;
    }

    public static boolean put(String host, TreeCache treeCache) {
        curatorTreeCache.put(host, treeCache);
        return true;
    }

    public static Optional<CuratorFramework> getClientOption(String host) {
        return Optional.ofNullable(curatorClientCache.get(host));
    }

    public static CuratorFramework getClient(String host) {
        return getClientOption(host).orElseThrow(() -> new IllegalArgumentException("no client found by " + host));
    }

    public static Optional<TreeCache> getTreeCache(String host) {
        return Optional.ofNullable(curatorTreeCache.get(host));
    }

    public static void close(String host) {
        getTreeCache(host).ifPresent(TreeCache::close);
        getClientOption(host).ifPresent(CuratorFramework::close);
    }

    public static void closeAll() {
        curatorTreeCache.values().forEach(TreeCache::close);
        curatorClientCache.values().forEach(CuratorFramework::close);
    }
}
