package cc.cc1234.specification.listener;

public interface ServerListener {

    default void onConnected(String id) {

    }

    default void onClose(String id) {

    }

    default void onReconnecting(String id) {

    }
}
