package cc.cc1234.main.cache;

import com.google.common.base.Strings;

public class ActiveServerContext {

    private static volatile String active;

    public static void change(String change) {
        active = change;
    }

    public static String get() {
        return active;
    }

    public static boolean exists() {
        return !Strings.isNullOrEmpty(active);
    }

    public static void invalidate() {
        active = null;
    }
}
