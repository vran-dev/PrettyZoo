package cc.cc1234;

import cc.cc1234.app.context.PrimaryStageContext;
import cc.cc1234.app.facade.PrettyZooFacade;
import cc.cc1234.app.util.FXMLs;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

public class PrettyZooApplication extends Application {

    private PrettyZooFacade facade = new PrettyZooFacade();

    @Override
    public void start(Stage primaryStage) throws Exception {
        v2(primaryStage);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        facade.close();
    }

    public static void main(String[] args) {
        initIconImage();
        Application.launch(args);
    }

    private void v2(Stage primaryStage) throws IOException {
        PrimaryStageContext.set(primaryStage);
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(FXMLs.loadFXML("fxml/MainView.fxml"));
        final StackPane anchorPane = loader.load();
        primaryStage.setScene(new Scene(anchorPane));
        primaryStage.setTitle("PrettyZoo");
        getIconStream().ifPresent(stream -> primaryStage.getIcons().add(new Image(stream)));
        primaryStage.show();
    }

    private static void initIconImage() {
        getIconStream()
                .ifPresent(inputStream -> {
                    try {
                        Taskbar.getTaskbar().setIconImage(ImageIO.read(inputStream));
                    } catch (UnsupportedOperationException e) {
                        // ignore not support platform, such as windows
                    } catch (IOException e) {
                        // ignore icon load failed
                    }
                });
    }

    private static Optional<InputStream> getIconStream() {
        InputStream stream = PrettyZooApplication.class.getClassLoader().getSystemResourceAsStream("assets/img/prettyzoo-logo.png");
        return Optional.ofNullable(stream);
    }

}
