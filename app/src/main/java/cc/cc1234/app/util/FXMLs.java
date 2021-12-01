package cc.cc1234.app.util;

import cc.cc1234.app.facade.PrettyZooFacade;
import javafx.fxml.FXMLLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;

public class FXMLs {

    private static final Logger log = LoggerFactory.getLogger(FXMLs.class);

    private static final PrettyZooFacade prettyZooFacade = new PrettyZooFacade();

    public static URL loadFXML(String path) {
        return Thread.currentThread().getContextClassLoader().getResource(path);
    }

    public static <T> T getController(String path) {
        try {
            final FXMLLoader loader = new FXMLLoader();
            loader.setResources(ResourceBundleUtils.get(prettyZooFacade.getLocale()));
            loader.setLocation(FXMLs.loadFXML(path));
            loader.load();
            return loader.getController();
        } catch (IOException e) {
            log.error("init AddServerView failed", e);
            throw new IllegalStateException(e);
        }
    }

}
