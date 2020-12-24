package cc.cc1234.app.controller;

import cc.cc1234.app.view.cell.ZkServerListCell;
import cc.cc1234.app.context.HostServiceContext;
import cc.cc1234.app.context.PrimaryStageContext;
import cc.cc1234.app.context.RootPaneContext;
import cc.cc1234.app.view.dialog.Dialog;
import cc.cc1234.app.facade.PrettyZooFacade;
import cc.cc1234.app.fp.Try;
import cc.cc1234.app.listener.DefaultConfigurationListener;
import cc.cc1234.app.util.FXMLs;
import cc.cc1234.app.view.toast.VToast;
import cc.cc1234.app.vo.ConfigurationVO;
import cc.cc1234.app.vo.ServerConfigurationVO;
import cc.cc1234.version.Version;
import cc.cc1234.version.VersionChecker;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;
import java.util.List;

public class MainViewController {

    @FXML
    private StackPane rootStackPane;

    @FXML
    private SplitPane mainSplitPane;

    @FXML
    private AnchorPane mainLeftPane;

    @FXML
    private StackPane mainRightPane;

    @FXML
    private ListView<ServerConfigurationVO> serverListView;

    @FXML
    private HBox serverButtons;

    @FXML
    private Button serverAddButton;

    @FXML
    private Button exportButton;

    @FXML
    private Button importButton;

    @FXML
    private Label newVersionLabel;

    private ServerViewController serverViewController = FXMLs.getController("fxml/ServerView.fxml");

    private PrettyZooFacade prettyZooFacade = new PrettyZooFacade();

    @FXML
    private void initialize() {
        initServerListView();
        RootPaneContext.set(rootStackPane);
        mainRightPane.setPadding(new Insets(30, 30, 30, 30));
        serverAddButton.setOnMouseClicked(event -> serverViewController.show(mainRightPane));
        exportButton.setOnMouseClicked(e -> onExportAction());
        importButton.setOnMouseClicked(e -> onImportAction());
        newVersionLabel.setOnMouseClicked(e -> HostServiceContext.jumpToReleases());
    }

    private void onExportAction() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Choose your target directory");
        fileChooser.setInitialFileName("prettyZoo-config");
        var file = fileChooser.showSaveDialog(PrimaryStageContext.get());
        Platform.runLater(() -> Try.of(() -> prettyZooFacade.exportConfig(file))
                .onFailure(e -> VToast.error(e.getMessage())));
    }

    private void onImportAction() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Choose config file");
        File configFile = fileChooser.showOpenDialog(PrimaryStageContext.get());
        Try.of(() -> prettyZooFacade.importConfig(configFile))
                .onFailure(e -> Platform.runLater(() -> VToast.error("Failed to load config, file is not support")));
    }

    private void initServerListView() {
        final ConfigurationVO configurationVO = new ConfigurationVO();
        final DefaultConfigurationListener configurationListener = new DefaultConfigurationListener(configurationVO);
        final List<ServerConfigurationVO> serverConfigurations = prettyZooFacade.loadConfigs(configurationListener);
        configurationVO.getServers().addAll(serverConfigurations);
        serverListView.itemsProperty().set(configurationVO.getServers());
        serverListView.setCellFactory(cellCallback -> new ZkServerListCell());
        var selectedItemProperty = serverListView.getSelectionModel().selectedItemProperty();
        selectedItemProperty.addListener((observable, oldValue, newValue) -> {
            if (oldValue != null) {
                oldValue.unbind();
            }
            if (newValue != null) {
                serverViewController.show(mainRightPane, newValue);
            }
        });
    }

    public StackPane getRootStackPane() {
        return rootStackPane;
    }

    public void showNewVersionLabel() {
        VersionChecker.hasNewVersion(latestVersion -> {
            final String content = String.format("你当前使用的是 %s, 目前最新版本为 %s, 请前往 Github 下载",
                    Version.VERSION, latestVersion);
            newVersionLabel.setVisible(true);
            newVersionLabel.setTooltip(new Tooltip("最新版 " + latestVersion + "已发布"));
            Dialog.confirm("升级提醒", content, HostServiceContext::jumpToReleases);
        });
    }
}
