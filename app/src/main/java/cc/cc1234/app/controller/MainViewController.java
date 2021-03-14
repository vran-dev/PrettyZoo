package cc.cc1234.app.controller;

import cc.cc1234.app.context.HostServiceContext;
import cc.cc1234.app.context.PrimaryStageContext;
import cc.cc1234.app.context.RootPaneContext;
import cc.cc1234.app.facade.PrettyZooFacade;
import cc.cc1234.app.fp.Try;
import cc.cc1234.app.listener.DefaultConfigurationListener;
import cc.cc1234.app.util.FXMLs;
import cc.cc1234.app.util.ShortcutKeys;
import cc.cc1234.app.view.cell.ZkServerListCell;
import cc.cc1234.app.view.dialog.Dialog;
import cc.cc1234.app.view.toast.VToast;
import cc.cc1234.app.vo.ConfigurationVO;
import cc.cc1234.app.vo.ServerConfigurationVO;
import cc.cc1234.version.Version;
import cc.cc1234.version.VersionChecker;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;

import java.io.File;

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
    private MenuItem exportMenuItem;

    @FXML
    private MenuItem importMenuItem;

    @FXML
    private MenuButton fontMenuButton;

    @FXML
    private Label newVersionLabel;

    @FXML
    private Hyperlink prettyZooLink;

    private ServerViewController serverViewController = FXMLs.getController("fxml/ServerView.fxml");

    private PrettyZooFacade prettyZooFacade = new PrettyZooFacade();

    @FXML
    private void initialize() {
        initServerListView();
        RootPaneContext.set(rootStackPane);
        mainRightPane.setPadding(new Insets(30, 30, 30, 30));
        serverAddButton.setOnMouseClicked(event -> serverViewController.show(mainRightPane));
        exportMenuItem.setOnAction(e -> onExportAction());
        importMenuItem.setOnAction(e -> onImportAction());
        newVersionLabel.setOnMouseClicked(e -> HostServiceContext.jumpToReleases());
        serverViewController.setOnClose(() -> this.serverListView.selectionModelProperty().get().clearSelection());
        prettyZooLink.setOnMouseClicked(e -> HostServiceContext.get().showDocument(prettyZooLink.getText()));
        initFontChangeButton();
    }

    public void bindShortcutKey() {
        serverAddButton.setTooltip(new Tooltip(ShortcutKeys.NEW_SERVER.key().getDisplayText()));
        rootStackPane.getScene()
                .getAccelerators()
                .put(ShortcutKeys.NEW_SERVER.key(), () -> serverViewController.show(mainRightPane));
    }

    private void initFontChangeButton() {
        rootStackPane.setStyle("-fx-font-size:13px;");
        var svf = new SpinnerValueFactory.IntegerSpinnerValueFactory(8, 25, 13);
        var sp = new Spinner<Integer>();
        sp.setValueFactory(svf);
        sp.setPrefWidth(100d);
        sp.valueProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                rootStackPane.setStyle("-fx-font-size: " + ((Integer) newValue));
            }
        }));
        fontMenuButton.getItems().add(new MenuItem("", sp));
    }

    private void onExportAction() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Choose your target directory");
        fileChooser.setInitialFileName("prettyZoo-config");
        var file = fileChooser.showSaveDialog(PrimaryStageContext.get());
        // configFile is null means click cancel
        if (file == null) {
            return;
        }
        Platform.runLater(() -> Try.of(() -> prettyZooFacade.exportConfig(file))
                .onFailure(e -> VToast.error(e.getMessage())));
    }

    private void onImportAction() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle("Choose config file");
        File configFile = fileChooser.showOpenDialog(PrimaryStageContext.get());
        // configFile is null means click cancel
        if (configFile == null) {
            return;
        }
        Try.of(() -> prettyZooFacade.importConfig(configFile))
                .onFailure(e -> Platform.runLater(() -> VToast.error("Failed to load config, file is not support")));
    }

    private void initServerListView() {
        final ConfigurationVO configurationVO = new ConfigurationVO();
        prettyZooFacade.loadServerConfigurations(new DefaultConfigurationListener(configurationVO));
        serverListView.itemsProperty().set(configurationVO.getServers());
        serverListView.setCellFactory(cellCallback -> {
            ZkServerListCell cell = new ZkServerListCell();
            cell.setOnMouseClicked(event -> {
                if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
                    serverViewController.connect(mainRightPane, cell.getItem());
                }
            });
            return cell;
        });
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
        VersionChecker.hasNewVersion((latestVersion, features) -> {
            String title = "发现新版本";
            final String content = new StringBuilder()
                    .append("最新版本: ").append(latestVersion).append("\r\n")
                    .append("当前版本: v").append(Version.VERSION).append("\r\n")
                    .append("新特性: \r\n").append(features)
                    .toString();
            newVersionLabel.setTooltip(new Tooltip("最新版 " + latestVersion + "已发布"));
            newVersionLabel.setVisible(true);
            Dialog.confirm(title, content, HostServiceContext::jumpToReleases);
        });
    }
}
