package cc.cc1234.main.context;

public class RecursiveModeContext {

    private static volatile boolean recursive = false;

    public static final String PRETTYZOO = "prettyZoo";

    public static final String PRETTYZOO_RECURSIVE = "prettyZoo-recursive";

    public static void change(boolean mode) {
        recursive = mode;
    }

    public static boolean get() {
        return recursive;
    }
}
