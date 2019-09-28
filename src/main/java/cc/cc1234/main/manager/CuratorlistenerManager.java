package cc.cc1234.main.manager;

import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;

public class CuratorlistenerManager {

    private TreeCache treeCache;

    private CuratorFramework framework;

    public CuratorlistenerManager(CuratorFramework framework) {
        this.framework = framework;
    }

    public void start(TreeCacheListener listener) {
        try {
            this.treeCache = new TreeCache(framework, "/");
            treeCache.getListenable().addListener(listener);
            this.treeCache.start();
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

}
