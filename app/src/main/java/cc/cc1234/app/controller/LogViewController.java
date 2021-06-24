package cc.cc1234.app.controller;

import cc.cc1234.app.facade.PrettyZooFacade;
import com.jfoenix.controls.JFXTextArea;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Paths;

public class LogViewController {

    private static final Logger logger = LoggerFactory.getLogger(LogViewController.class);

    @FXML
    private JFXTextArea logContentArea;

    @FXML
    private AnchorPane logViewPane;

    private PrettyZooFacade prettyZooFacade = new PrettyZooFacade();

    @FXML
    public void initialize() {
        logContentArea.setWrapText(true);
    }

    public void show(StackPane parent) {
        if (!parent.getChildren().contains(logViewPane)) {
            parent.getChildren().add(logViewPane);
            var userHome = System.getProperty("user.home");
            var path = Paths.get(userHome + "/.prettyZoo/log/prettyZoo.log");
            prettyZooFacade.startLogTailer(line -> {
                Platform.runLater(() -> {
                    logContentArea.appendText("\n");
                    logContentArea.appendText(line);
                });
            }, ex -> Platform.runLater(() -> logContentArea.appendText(ex.toString())));
        } else {
            parent.getChildren().remove(logViewPane);
            parent.getChildren().add(logViewPane);
        }
    }
}
