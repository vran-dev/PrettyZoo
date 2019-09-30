package cc.cc1234.main.cache;

import cc.cc1234.main.manager.CuratorlistenerManager;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ZkListenerCache {

    private static final Map<String, CuratorlistenerManager> CACHE = new ConcurrentHashMap<>();

    private static final ZkListenerCache ZK_LISTENER_CACHE = new ZkListenerCache();

    public static ZkListenerCache getInstance() {
        return ZK_LISTENER_CACHE;
    }

    public void put(String server, CuratorlistenerManager manager) {
        CACHE.put(server, manager);
    }

    public CuratorlistenerManager get(String server) {
        return CACHE.get(server);
    }

    public static boolean contains(String server) {
        return CACHE.containsKey(server);
    }

    public void closeAll() {
        CACHE.forEach((key, manager) -> manager.close());
    }
}
