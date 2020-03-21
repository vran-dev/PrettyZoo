package cc.cc1234;

import cc.cc1234.app.context.PrimaryStageContext;
import cc.cc1234.app.util.FXMLs;
import cc.cc1234.facade.PrettyZooFacade;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;

public class PrettyZooApplication extends Application {

    private PrettyZooFacade facade = new PrettyZooFacade();

    @Override
    public void start(Stage primaryStage) throws Exception {
        PrimaryStageContext.set(primaryStage);
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(FXMLs.loadFXML("fxml/TreeNodeView.fxml"));
        final AnchorPane anchorPane = loader.load();
        primaryStage.setScene(new Scene(anchorPane));
        primaryStage.setTitle("PrettyZoo");
        primaryStage.getIcons().add(new Image(getIconStream()));
        primaryStage.show();
    }

    private static InputStream getIconStream() {
        return PrettyZooApplication.class.getClassLoader().getSystemResourceAsStream("assets/img/prettyzoo-logo.png");
    }


    @Override
    public void stop() throws Exception {
        super.stop();
        facade.close();
    }

    public static void main(String[] args) {
        try {
            Taskbar.getTaskbar().setIconImage(ImageIO.read(getIconStream()));
        } catch (UnsupportedOperationException e) {
            // ignore not support platform, such as windows
        } catch (IOException e) {
            // ignore icon load failed
        }
        Application.launch(args);
    }
}
