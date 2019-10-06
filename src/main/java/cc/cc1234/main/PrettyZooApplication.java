package cc.cc1234.main;

import cc.cc1234.main.cache.PrettyZooConfigContext;
import cc.cc1234.main.service.ZkServerService;
import cc.cc1234.main.util.Configs;
import cc.cc1234.main.util.FXMLs;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class PrettyZooApplication extends Application {

    @Override
    public void start(Stage primaryStage) throws Exception {
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(FXMLs.loadFXML("fxml/TreeNodeView.fxml"));
        final AnchorPane anchorPane = loader.load();
        primaryStage.setScene(new Scene(anchorPane));
        primaryStage.setTitle("PrettyZoo");
        primaryStage.show();
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        Configs.store(PrettyZooConfigContext.get());
        ZkServerService.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
