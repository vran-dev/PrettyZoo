package cc.cc1234.main.cache;

import org.apache.curator.framework.CuratorFramework;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class CuratorClientCache {

    private static final Map<String, CuratorFramework> curatorClientCache = new ConcurrentHashMap<>();

    public static boolean put(String host, CuratorFramework client) {
        curatorClientCache.put(host, client);
        return true;
    }

    public static Optional<CuratorFramework> getOption(String host) {
        return Optional.ofNullable(curatorClientCache.get(host));
    }

    public static void close(String host) {
        getOption(host).ifPresent(CuratorFramework::close);
    }

    public static void closeAll() {
        curatorClientCache.values().forEach(CuratorFramework::close);
    }
}
