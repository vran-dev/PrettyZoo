package cc.cc1234.app.context;

public class LogTailerThreadContext {

    private static Thread thread;

    public static void set(Thread tailerThread) {
        thread = tailerThread;
    }

    public static void stop() {
        if (thread != null) {
            thread.interrupt();
        }
    }
}
