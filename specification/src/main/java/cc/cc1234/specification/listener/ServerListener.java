package cc.cc1234.specification.listener;

public interface ServerListener {

    default void onConnected(String id) {

    }

    default void onClose(String id) {
        this.onClose(id, null);
    }

    default void onClose(String id, String reason) {

    }

    default void onReconnecting(String id) {

    }

    default void onAuthFailed(String id) {

    }
}
