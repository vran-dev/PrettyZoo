package cc.cc1234.main;

import cc.cc1234.main.controller.ConnectViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class PrettyZooApplication extends Application {

    private Stage primaryStage;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        showConnectView();
    }

    private void showConnectView() throws IOException {
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(PrettyZooApplication.class.getResource("controller/ConnectView.fxml"));
        final AnchorPane panel = loader.load();
        final Scene scene = new Scene(panel);
        primaryStage.setScene(scene);
        primaryStage.show();

        ConnectViewController controller = loader.getController();
        controller.setPrimaryStage(primaryStage);
    }

    public static void main(String[] args) {
        launch(args);
    }
}
