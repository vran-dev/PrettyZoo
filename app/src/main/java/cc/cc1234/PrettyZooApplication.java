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
        primaryStage.getIcons().add(new Image(this.getClass().getClassLoader().getSystemResourceAsStream("assets/img/prettyzoo-logo.png")));
        primaryStage.show();
    }


    @Override
    public void stop() throws Exception {
        super.stop();
        facade.close();
    }

    public static void main(String[] args) {
        Application.launch(args);
    }
}
