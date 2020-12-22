package cc.cc1234.app.checker;

import java.util.Objects;
import java.util.regex.Pattern;

public class Asserts {

    public static void assertTrue(Boolean b, String message) {
        if (!b) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void assertFalse(Boolean b, String message) {
        assertTrue(!b, message);
    }

    public static void notNull(Object o, String message) {
        if (o == null) {
            Objects.requireNonNull(o, message);
        }
    }

    public static void notBlank(String str, String message) {
        if (str == null || str.isBlank()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void matchHost(String host, String message) {
        // IP:Port -> 127.0.0.1:2181
        // URL -> zk.dev:2181
        // simple match
        var pattern = Pattern.compile(".*\\:\\d+$");
        if (host == null || !pattern.matcher(host).matches()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void validAcl(String acl, String message) {
        // TODO
    }

}
