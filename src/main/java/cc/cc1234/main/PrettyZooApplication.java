package cc.cc1234.main;

import cc.cc1234.main.controller.ConnectViewController;
import cc.cc1234.main.controller.NodeTreeViewController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Stage;

import java.io.IOException;

public class PrettyZooApplication extends Application {

    private Stage primaryStage;

    private ConnectViewController connectViewController;

    private NodeTreeViewController nodeTreeViewController;

    @Override
    public void start(Stage primaryStage) throws Exception {
        this.primaryStage = primaryStage;
        connectView();
    }

    private void connectView() throws IOException {
        final FXMLLoader loader = load("controller/ConnectView.fxml");
        final AnchorPane panel = loader.load();
        this.connectViewController = loader.getController();
        this.connectViewController.setPrettyZooApplication(this);
        final Scene scene = new Scene(panel);
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    public void nodeTreeView() throws IOException {
        final FXMLLoader loader = load("controller/NodeTreeView.fxml");
        final AnchorPane anchorPane = loader.load();
        this.nodeTreeViewController = loader.getController();
        primaryStage.getScene().setRoot(anchorPane);
        primaryStage.sizeToScene();
        nodeTreeViewController.viewInit(connectViewController.getClient());
    }

    private FXMLLoader load(String fxml) {
        final FXMLLoader loader = new FXMLLoader();
        loader.setLocation(PrettyZooApplication.class.getResource(fxml));
        return loader;
    }

    public void close() {
        primaryStage.close();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
