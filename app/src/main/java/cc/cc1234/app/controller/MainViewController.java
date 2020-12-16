package cc.cc1234.app.controller;

import cc.cc1234.app.cell.ZkServerListCell;
import cc.cc1234.app.context.PrimaryStageContext;
import cc.cc1234.app.context.RootPaneContext;
import cc.cc1234.app.util.FXMLs;
import cc.cc1234.app.util.VToast;
import cc.cc1234.app.vo.PrettyZooConfigVO;
import cc.cc1234.app.vo.ServerConfigVO;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.SplitPane;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;

public class MainViewController {

    private static final Logger log = LoggerFactory.getLogger(MainViewController.class);

    @FXML
    private StackPane rootStackPane;

    @FXML
    private SplitPane mainSplitPane;

    @FXML
    private AnchorPane mainLeftPane;

    @FXML
    private StackPane mainRightPane;

    @FXML
    private ListView<ServerConfigVO> serverListView;

    @FXML
    private HBox serverButtons;

    @FXML
    private Button serverAddButton;

    @FXML
    private Button exportButton;

    @FXML
    private Button importButton;

    private ServerViewController serverViewController = FXMLs.getController("fxml/ServerView.fxml");


    private PrettyZooConfigVO prettyZooConfigVO = new PrettyZooConfigVO();

    @FXML
    private void initialize() {
        showServerInfoView();
        serverAddButton.setOnMouseClicked(event -> {
            serverViewController.show(mainRightPane);
        });

        RootPaneContext.set(rootStackPane);
        exportButton.setOnMouseClicked(e -> onExportAction());
        importButton.setOnMouseClicked(e -> onImportAction());
    }

    private void onExportAction() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Choose your target directory");
        fileChooser.setInitialFileName("prettyZoo-config");
        var file = fileChooser.showSaveDialog(PrimaryStageContext.get());
        Platform.runLater(() -> {
            try {
                prettyZooConfigVO.export(file);
            } catch (Exception e) {
                VToast.error(e.getMessage());
            }
        });
    }

    private void onImportAction() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Choose config file");
        File configFile = fileChooser.showOpenDialog(PrimaryStageContext.get());
        Platform.runLater(() -> {
            try {
                prettyZooConfigVO.importConfig(configFile);
            } catch (Exception e) {
                VToast.error("Failed to load config, file is not support");
            }
        });
    }


    private void showServerInfoView() {
        serverListView.itemsProperty().set(prettyZooConfigVO.getServers());
        serverListView.setCellFactory(cellCallback -> {
            final ZkServerListCell cell = new ZkServerListCell();
            cell.setOnMouseClicked(e -> {
                final ListCell<ServerConfigVO> serverCell = ((ListCell<ServerConfigVO>) e.getSource());
                var vo = serverCell.getItem();
                if (vo != null) {
                    serverViewController.show(mainRightPane, vo);
                }
                e.consume();
            });
            return cell;
        });
        var selectedItemProperty = serverListView.getSelectionModel().selectedItemProperty();
        selectedItemProperty.addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.unbind();
            }
        });
    }

}
