package cc.cc1234.specification.util;

import java.util.Iterator;

public class PathIterator implements Iterator<String> {

    private String path;

    private int mark = 1;

    private int offset = 1;

    public PathIterator(String path) {
        this.path = path;
    }

    @Override
    public boolean hasNext() {
        return mark < path.length();
    }

    @Override
    public String next() {
        if (!path.startsWith("/")) {
            throw new IllegalStateException("path must be starts with /");
        }
        if (path.length() == 1) {
            offset++;
            return path;
        }
        if (path.endsWith("/")) {
            throw new IllegalStateException("path must not be ends with /");
        }

        while (offset < path.length() && path.charAt(offset) != '/') {
            offset++;
        }
        String current = path.substring(mark, offset);
        mark = ++offset;
        return current;
    }
}
