package cc.cc1234.app.util;

import com.google.common.base.Strings;

public class PathConcat {

    public static String concat(String parent, String child) {
        if (Strings.isNullOrEmpty(child)) {
            return parent;
        }
        final boolean end = parent.endsWith("/");
        final boolean start = child.startsWith("/");
        if (end && start) {
            return parent + child.substring(1);
        }

        if (end || start) {
            return parent + child;
        }

        return parent + "/" + child;
    }
}
