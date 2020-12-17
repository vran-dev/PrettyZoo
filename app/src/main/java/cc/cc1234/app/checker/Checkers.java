package cc.cc1234.app.checker;

import java.util.regex.Pattern;

public class Checkers {

    public static <T> boolean isNull(T t) {
        return t == null;
    }

    public static boolean isBlank(String t) {
        return t.trim().length() == 0;
    }

    public static boolean isHostNotMatch(String host) {
        // IP:Port -> 127.0.0.1:2181
        // URL -> zk.dev:2181
        // simple match
        var pattern = Pattern.compile(".*\\:\\d+$");
        return host == null || !pattern.matcher(host).matches();
    }

    public static <T> void ifNull(T t, Runnable then) {
        if (t == null) {
            then.run();
        }
    }

    public static void ifBlank(String t, Runnable then) {
        if (t.trim().length() == 0) {
            then.run();
        }
    }

    public static void ifHostNotMatch(String host, Runnable then) {
        // IP:Port -> 127.0.0.1:2181
        // URL -> zk.dev:2181
        // simple match
        var pattern = Pattern.compile(".*\\:\\d+$");
        if (host == null || !pattern.matcher(host).matches()) {
            then.run();
        }
    }

    public static boolean aclIsInvalid(String acl) {
        if (acl == null || acl.trim().length() == 0) {
            return false;
        }
        // TODO check acl pattern
        return false;
    }
}
