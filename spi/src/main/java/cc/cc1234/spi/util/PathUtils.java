package cc.cc1234.spi.util;


public class PathUtils {

    public static String getLastPath(String path) {
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("path must be starts with /");
        }

        // Root
        if (path.length() == 1) {
            return path;
        }

        if (path.endsWith("/")) {
            throw new IllegalStateException("path must not ends with /");
        }

        final int mark = path.lastIndexOf("/");
        // ignore /
        return path.substring(mark + 1);
    }


    public static String getStartPath(String path) {
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("path must be starts with /");
        }

        // Root
        if (path.length() == 1) {
            return path;
        }

        if (path.endsWith("/")) {
            throw new IllegalStateException("path must not ends with /");
        }

        int offset = 1;
        while (offset < path.length() && path.charAt(offset) != '/') {
            offset++;
        }
        return path.substring(1, offset);
    }

    public static String getParent(String path) {
        if (!path.startsWith("/")) {
            throw new IllegalArgumentException("path must be starts with /");
        }

        // Root
        if (path.length() == 1) {
            return path;
        }

        if (path.endsWith("/")) {
            throw new IllegalStateException("path must not ends with /");
        }

        final String lastPath = getLastPath(path);
        final int offset = path.lastIndexOf("/" + lastPath);
        if (offset == 0) {
            return "/";
        }
        return path.substring(0, offset);
    }


}
