package cc.cc1234.client.curator;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CuratorCache {

    private static final Map<String, CuratorZookeeperConnection> curatorClientCache = new ConcurrentHashMap<>();

    private static final Map<String, TreeCache> curatorTreeCache = new ConcurrentHashMap<>();


    public static Optional<CuratorZookeeperConnection> getConnection(String host) {
        return Optional.ofNullable(curatorClientCache.get(host));
    }

    public static CuratorFramework getClient(String host) {
        return getConnection(host).map(CuratorZookeeperConnection::getClient).orElseThrow(() -> new IllegalArgumentException("no client found by " + host));
    }

    public static Optional<TreeCache> getTreeCache(String host) {
        return Optional.ofNullable(curatorTreeCache.get(host));
    }

}
