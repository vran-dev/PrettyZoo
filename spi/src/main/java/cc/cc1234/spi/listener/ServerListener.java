package cc.cc1234.spi.listener;

public interface ServerListener {

    default void onConnected(String serverHost) {

    }

    default void onClose(String serverHost) {

    }
}
