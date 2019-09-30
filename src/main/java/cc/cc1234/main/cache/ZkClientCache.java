package cc.cc1234.main.cache;

import org.apache.curator.framework.CuratorFramework;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ZkClientCache {

    private static final Map<String, CuratorFramework> CACHE = new ConcurrentHashMap<>();

    private static final ZkClientCache ZK_CLIENT_CACHE = new ZkClientCache();

    public static ZkClientCache getInstance() {
        return ZK_CLIENT_CACHE;
    }

    public boolean contains(String server) {
        return CACHE.containsKey(server);
    }

    public CuratorFramework get(String server) {
        return CACHE.get(server);
    }

    public CuratorFramework put(String server, CuratorFramework client) {
        return CACHE.put(server, client);
    }

    public void closeAll() {
        CACHE.forEach((key, client) -> {
            client.close();
        });
    }

}
