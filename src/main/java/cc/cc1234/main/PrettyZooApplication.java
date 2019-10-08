package cc.cc1234.main;

import cc.cc1234.main.cache.CuratorCache;
import cc.cc1234.main.cache.PrettyZooConfigCache;
import cc.cc1234.main.context.ApplicationContext;
import cc.cc1234.main.model.PrettyZooConfig;
import cc.cc1234.main.service.PrettyZooConfigService;
import cc.cc1234.main.service.ZkNodeService;
import cc.cc1234.main.util.FXMLs;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

public class PrettyZooApplication extends Application {

    private ApplicationContext context = ApplicationContext.get();

    @Override
    public void start(Stage primaryStage) throws Exception {
        setup();

        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(FXMLs.loadFXML("fxml/TreeNodeView.fxml"));
        final AnchorPane anchorPane = loader.load();
        primaryStage.setScene(new Scene(anchorPane));
        primaryStage.setTitle("PrettyZoo");
        ApplicationContext.get().setPrimaryStage(primaryStage);

        primaryStage.show();
    }

    private void setup() {
        context.setBean(new PrettyZooConfigService());
        context.setBean(new ZkNodeService());
    }

    @Override
    public void stop() throws Exception {
        super.stop();
        final PrettyZooConfig config = PrettyZooConfigCache.get();
        context.getBean(PrettyZooConfigService.class).save(config);
        CuratorCache.closeAll();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
