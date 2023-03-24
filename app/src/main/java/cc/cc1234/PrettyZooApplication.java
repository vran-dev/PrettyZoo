package cc.cc1234;

import cc.cc1234.app.context.HostServiceContext;
import cc.cc1234.app.context.PrimaryStageContext;
import cc.cc1234.app.controller.MainViewController;
import cc.cc1234.app.facade.PrettyZooFacade;
import cc.cc1234.app.util.FXMLs;
import cc.cc1234.specification.config.PrettyZooConfigRepository;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;
import java.util.Optional;

public class PrettyZooApplication extends Application {

    private static final PrettyZooFacade facade = new PrettyZooFacade();

    public static void main(String[] args) {
        facade.initZookeeperSystemProperties();
        initIconImage();
        Application.launch(args);
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
        InputStream stream = PrettyZooApplication.class.getClassLoader()
                .getSystemResourceAsStream("assets/icon/icon.png");
        return Optional.ofNullable(stream);
    }

    @Override
    public void start(Stage primaryStage) {
        v2(primaryStage);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        facade.closeAll();
    }

    private void v2(Stage primaryStage) {
        PrimaryStageContext.set(primaryStage);
        HostServiceContext.set(getHostServices());
        MainViewController controller = FXMLs.getController("fxml/MainView.fxml");
        final StackPane stackPane = controller.getRootStackPane();

        Scene scene = new Scene(stackPane);
        String theme = facade.getThemeFromConfig();
        scene.getStylesheets().add("assets/css/default/style.css");
        if (Objects.equals(theme, PrettyZooConfigRepository.THEME_DARK)) {
            scene.getStylesheets().add("assets/css/dark/style.css");
        }
        primaryStage.setScene(scene);
        primaryStage.setTitle("PrettyZoo");
        getIconStream().ifPresent(stream -> primaryStage.getIcons().add(new Image(stream)));
        primaryStage.setOnShown(e -> {
            controller.checkForUpdate();
            controller.bindShortcutKey();
        });
        primaryStage.show();
    }

}
