package cc.cc1234.app.context;

import com.google.common.base.Strings;

import java.util.Objects;

public class ActiveServerContext {

    /**
     * server id
     */
    private static volatile String active;

    public static void set(String change) {
        active = change;
    }

    public static String get() {
        return active;
    }

    public static boolean exists() {
        return !Strings.isNullOrEmpty(active);
    }

    public static boolean isSame(String id) {
        return Objects.equals(id, active);
    }

    public static void invalidate() {
        active = null;
    }
}
