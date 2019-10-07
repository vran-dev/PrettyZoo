package cc.cc1234.main.cache;

import javafx.stage.Stage;

public class PrimaryStageContext {

    private static Stage primaryStage;

    public static void set(Stage primaryStage) {
        PrimaryStageContext.primaryStage = primaryStage;
    }

    public static Stage get() {
        return primaryStage;
    }
}
