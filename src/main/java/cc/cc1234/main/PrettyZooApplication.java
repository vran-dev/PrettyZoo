package cc.cc1234.main;

import cc.cc1234.main.controller.TreeNodeViewController;
import cc.cc1234.main.service.ZkServerService;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;
import java.net.URL;

public class PrettyZooApplication extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        final URL resource = Thread.currentThread().getContextClassLoader().getResource("icon.jpg");
        primaryStage.getIcons().add(new Image(resource.openStream()));
        showNodeTreeView(primaryStage);
    }

    public static void showNodeTreeView(Stage primary) throws IOException {
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(TreeNodeViewController.class.getResource("TreeNodeView.fxml"));
        final AnchorPane anchorPane = loader.load();
        final Scene scene = new Scene(anchorPane);
        primary.setScene(scene);
        TreeNodeViewController controller = loader.getController();
        controller.setPrimaryStage(primary);
        primary.show();

    }

    @Override
    public void stop() throws Exception {
        super.stop();
        ZkServerService.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
