package cc.cc1234.main.context;

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
}
