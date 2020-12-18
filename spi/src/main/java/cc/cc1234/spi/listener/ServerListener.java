package cc.cc1234.spi.listener;

public interface ServerListener {

    default void onClose(String serverHost) {

    }
}
