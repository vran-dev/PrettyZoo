package cc.cc1234.app.util;

import javafx.fxml.FXMLLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

public class FXMLs {

    private static final Logger log = LoggerFactory.getLogger(FXMLs.class);

    public static URL loadFXML(String path) {
        return Thread.currentThread().getContextClassLoader().getResource(path);
    }

    public static <T> T getController(String path) {
        try {
            final FXMLLoader loader = new FXMLLoader();
            loader.setLocation(FXMLs.loadFXML(path));
            loader.load();
            return loader.getController();
        } catch (IOException e) {
            log.error("init AddServerView failed", e);
            throw new IllegalStateException(e);
        }
    }

}
