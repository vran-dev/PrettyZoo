package cc.cc1234;

import cc.cc1234.app.context.HostServiceContext;
import cc.cc1234.app.context.PrimaryStageContext;
import cc.cc1234.app.controller.MainViewController;
import cc.cc1234.app.dialog.Dialog;
import cc.cc1234.app.facade.PrettyZooFacade;
import cc.cc1234.app.util.FXMLs;
import cc.cc1234.version.Version;
import cc.cc1234.version.VersionChecker;
import javafx.application.Application;
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
    public void start(Stage primaryStage) {
        v2(primaryStage);
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        facade.closeAll();
    }

    public static void main(String[] args) {
        initIconImage();
        Application.launch(args);
    }

    private void v2(Stage primaryStage) {
        PrimaryStageContext.set(primaryStage);
        HostServiceContext.set(getHostServices());
        MainViewController controller = FXMLs.getController("fxml/MainView.fxml");
        final StackPane stackPane = controller.getRootStackPane();

        primaryStage.setScene(new Scene(stackPane));
        primaryStage.setTitle("PrettyZoo");
        getIconStream().ifPresent(stream -> primaryStage.getIcons().add(new Image(stream)));
        primaryStage.setOnShown(e -> {
            VersionChecker.hasNewVersion(latestVersion -> {
                final String content = String.format("你当前使用的是 %s, 目前最新版本为 %s, 请前往 Github 下载",
                        Version.VERSION, latestVersion);
                controller.showNewVersionLabel(latestVersion);
                Dialog.confirm("升级提醒", content, HostServiceContext::jumpToReleases);
            });
        });
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
