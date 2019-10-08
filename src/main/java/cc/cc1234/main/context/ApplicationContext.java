package cc.cc1234.main.context;

import cc.cc1234.main.cache.CuratorCache;
import cc.cc1234.main.cache.TreeItemCache;
import javafx.stage.Stage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ApplicationContext {

    private static final ApplicationContext singleton = new ApplicationContext();

    private Map<Class<?>, Object> singletonBeanTypes = new ConcurrentHashMap<>();

    private Stage primaryStage;

    public static ApplicationContext get() {
        return singleton;
    }

    public <T> T getBean(Class<T> tClass) {
        return (T) singletonBeanTypes.get(tClass);
    }

    public void setBean(Object bean) {
        if (singletonBeanTypes.containsKey(bean.getClass())) {
            throw new IllegalArgumentException("duplicate beans: " + bean.getClass().getName());
        }
        singletonBeanTypes.put(bean.getClass(), bean);
    }

    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }

    public static void close(String host) {
        CuratorCache.close(host);
        if (host.equals(ActiveServerContext.get())) {
            ActiveServerContext.invalidate();
        }
        TreeItemCache.getInstance().remove(host);
    }
}
