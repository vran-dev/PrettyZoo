package cc.cc1234.app.vo;

public enum ServerStatus {

    CONNECTED,

    DISCONNECTED,

    CONNECTING,

    RECONNECTING;

    public boolean isConnecting() {
        return this == CONNECTING || this == RECONNECTING;
    }
}
