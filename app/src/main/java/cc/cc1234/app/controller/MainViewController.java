package cc.cc1234.app.controller;

import cc.cc1234.PrettyZooApplication;
import cc.cc1234.app.context.HostServiceContext;
import cc.cc1234.app.context.PrimaryStageContext;
import cc.cc1234.app.context.RootPaneContext;
import cc.cc1234.app.facade.PrettyZooFacade;
import cc.cc1234.app.fp.Try;
import cc.cc1234.app.listener.DefaultConfigurationListener;
import cc.cc1234.app.util.FXMLs;
import cc.cc1234.app.util.ResourceBundleUtils;
import cc.cc1234.app.util.ShortcutKeys;
import cc.cc1234.app.view.cell.ZkServerListCell;
import cc.cc1234.app.view.dialog.Dialog;
import cc.cc1234.app.view.toast.VToast;
import cc.cc1234.app.vo.ConfigurationVO;
import cc.cc1234.app.vo.ServerConfigurationVO;
import cc.cc1234.specification.config.model.ConfigData;
import cc.cc1234.version.Version;
import cc.cc1234.version.VersionChecker;
import com.jfoenix.controls.JFXSlider;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Properties;
import java.util.ResourceBundle;

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
    private ListView<ServerConfigurationVO> serverListView;

    @FXML
    private VBox serverButtons;

    @FXML
    private Button serverAddButton;

    @FXML
    private Button checkUpdateButton;

    @FXML
    private Button logsButton;

    @FXML
    private Button darkModeSwitchButton;

    @FXML
    private MenuItem exportMenuItem;

    @FXML
    private MenuItem importMenuItem;

    @FXML
    private MenuItem zookeeperPropsMenuItem;

    @FXML
    private MenuItem resetMenuItem;

    @FXML
    private Menu langMenu;

    @FXML
    private JFXSlider fontSizeSlider;

    @FXML
    private MenuItem fontSizeMenuItem;

    @FXML
    private Hyperlink prettyZooLink;

    private ServerViewController serverViewController = FXMLs.getController("fxml/ServerView.fxml");

    private LogViewController logViewController = FXMLs.getController("fxml/LogView.fxml");

    private PrettyZooFacade prettyZooFacade = new PrettyZooFacade();

    @FXML
    private void initialize() {
        initServerListView();
        initConfigs();

        RootPaneContext.set(rootStackPane);
        if (serverListView.getItems().isEmpty()) {
            mainSplitPane.setDividerPositions(calculateDividerPositions());
        } else {
            mainSplitPane.setDividerPositions(prettyZooFacade.getMainSplitPaneDividerPosition());
        }
        mainSplitPane.getDividers().stream().findFirst().ifPresent(divider -> {
            divider.positionProperty().addListener(((observable, oldValue, newValue) -> {
                prettyZooFacade.changeMainSplitPaneDividerPosition(newValue.doubleValue());
            }));
        });

        initMenuAction();
        serverViewController.setOnClose(() -> this.serverListView.selectionModelProperty().get().clearSelection());
        prettyZooLink.setOnMouseClicked(e -> HostServiceContext.get().showDocument(prettyZooLink.getText()));
        initFontChangeButton();
    }

    public StackPane getRootStackPane() {
        return rootStackPane;
    }

    public void checkForUpdate() {
        doCheckForUpdate(true);
    }

    private void doCheckForUpdate(Boolean ignoreToast) {
        if (checkUpdateButton.getGraphic() != null) {
            return;
        }
        ProgressIndicator indicator = new ProgressIndicator();
        indicator.setPrefSize(12, 12);
        indicator.getStyleClass().add("check-update-progress");

        checkUpdateButton.getStyleClass().remove("check-update-button");
        checkUpdateButton.setGraphic(indicator);
        VersionChecker.hasNewVersion((latestVersion, features) -> {
                    String title = "New version";
                    final String content = new StringBuilder()
                            .append("latest: ").append(latestVersion).append("\r\n")
                            .append("yours: v").append(Version.VERSION).append("\r\n")
                            .append("features: \r\n").append(features)
                            .toString();
                    checkUpdateButton.setOnAction(e2 ->
                            Dialog.confirm(title, content, HostServiceContext::jumpToReleases));
                    checkUpdateButton.getStyleClass().add("new-version");
                    checkUpdateButton.setTooltip(new Tooltip("New version " + latestVersion + " released"));
                    checkUpdateButton.setGraphic(null);
                },
                () -> {
                    checkUpdateButton.getStyleClass().add("check-update-button");
                    checkUpdateButton.setGraphic(null);
                    if (!ignoreToast) {
                        VToast.info(ResourceBundleUtils.getContent("action.check-update.no-change"));
                    }
                },
                ex -> {
                    checkUpdateButton.getStyleClass().add("check-update-button");
                    checkUpdateButton.setGraphic(null);
                    if (!ignoreToast) {
                        VToast.info(ex.getMessage());
                    }
                });
    }

    private void initMenuAction() {
        checkUpdateButton.setOnAction(e -> {
            doCheckForUpdate(false);
        });
        serverAddButton.setOnMouseClicked(event -> {
            serverListView.getSelectionModel().clearSelection();
            serverViewController.show(mainRightPane);
        });
        logsButton.setOnAction(e -> {
            logViewController.show(mainRightPane);
            serverListView.selectionModelProperty().get().clearSelection();
        });
        darkModeSwitchButton.setOnAction(e -> {
            prettyZooFacade.changeTheme();
        });
        exportMenuItem.setOnAction(e -> onExportAction());
        importMenuItem.setOnAction(e -> onImportAction());
        resetMenuItem.setOnAction(e -> {
            prettyZooFacade.resetConfiguration();
            PrimaryStageContext.get().close();
            Platform.runLater(() -> new PrettyZooApplication().start(new Stage()));
        });
        zookeeperPropsMenuItem.setOnAction(e -> {
            Properties properties = prettyZooFacade.loadZookeeperSystemProperties();
            try (StringWriter writer = new StringWriter()) {
                properties.store(writer, null);
                Dialog.confirmEditable(ResourceBundleUtils.getContent("main.menuBar.config.zookeeper-prop"),
                        writer.toString(),
                        content -> {
                            prettyZooFacade.saveZookeeperSystemProperties(content);
                            Platform.runLater(() ->
                                    VToast.info(ResourceBundleUtils.getContent("notification.save.success")));
                        });
            } catch (IOException ex) {
                log.error("load zookeeper properties error", ex);
                throw new RuntimeException(ex);
            }
        });
    }

    public void bindShortcutKey() {
        serverAddButton.setTooltip(new Tooltip(ShortcutKeys.NEW_SERVER.key().getDisplayText()));
        rootStackPane.getScene()
                .getAccelerators()
                .put(ShortcutKeys.NEW_SERVER.key(), () -> serverViewController.show(mainRightPane));
    }

    private void initFontChangeButton() {
        Integer fontSize = prettyZooFacade.getFontSize();
        rootStackPane.setStyle("-fx-font-size: " + fontSize);
        fontSizeSlider.setValue(fontSize);

        fontSizeSlider.valueProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                rootStackPane.setStyle("-fx-font-size: " + newValue);
                prettyZooFacade.changeFontSize(newValue.intValue());
            }
        }));
        var valueTextBinding = Bindings.createStringBinding(() -> fontSizeSlider.valueProperty().intValue() + "",
                fontSizeSlider.valueProperty());
        fontSizeMenuItem.textProperty().bind(valueTextBinding);

        var langToggleGroup = new ToggleGroup();
        for (ConfigData.Lang value : ConfigData.Lang.values()) {
            var radioMenuItem = new RadioMenuItem(value.getLocale().toLanguageTag());
            radioMenuItem.setId(value.name());
            radioMenuItem.setToggleGroup(langToggleGroup);
            if (prettyZooFacade.getLocale().equals(value.getLocale())) {
                radioMenuItem.setSelected(true);
            }
            langMenu.getItems().add(radioMenuItem);
        }
        langToggleGroup.selectedToggleProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                var item = (RadioMenuItem) newValue;
                final ConfigData.Lang newLang = ConfigData.Lang.valueOf(item.getId());
                prettyZooFacade.updateLocale(newLang);
                ResourceBundle rb = ResourceBundleUtils.get(newLang.getLocale());
                String title = rb.getString("lang.change.confirm.title");
                String content = rb.getString("lang.change.confirm.content");
                Dialog.confirm(title, content, () -> {
                    PrimaryStageContext.get().close();
                    Platform.runLater(() -> new PrettyZooApplication().start(new Stage()));
                });
            }
        });
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
                .onFailure(e -> Platform.runLater(() -> VToast.error(e.getMessage())));
    }

    private void initServerListView() {
        serverListView.setCellFactory(cellCallback -> {
            var cell = new ZkServerListCell(
                    server -> serverViewController.connect(mainRightPane, server),
                    server -> serverViewController.deleteById(server.getId()),
                    server -> serverViewController.disconnect(server.getId())
            );
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

    private void initConfigs() {
        final ConfigurationVO configurationVO = new ConfigurationVO();
        configurationVO.getServers()
                .addListener((ListChangeListener<? super ServerConfigurationVO>) (change) -> {
                    if (change.getList().isEmpty()) {
                        mainSplitPane.setDividerPositions(calculateDividerPositions());
                    } else {
                        mainSplitPane.setDividerPositions(0.25);
                    }
                });
        prettyZooFacade.loadServerConfigurations(new DefaultConfigurationListener(configurationVO));
        serverListView.itemsProperty().set(configurationVO.getServers());
    }

    private double calculateDividerPositions() {
        double buttonBarWidth = serverButtons.getPrefWidth();
        double mainPaneWidth = rootStackPane.getPrefWidth();
        return (buttonBarWidth + 6) / mainPaneWidth;
    }

}
