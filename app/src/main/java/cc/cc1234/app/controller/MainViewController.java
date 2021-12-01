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
import com.jfoenix.controls.JFXListView;
import com.jfoenix.controls.JFXSlider;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ResourceBundle;

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
    private JFXListView<ServerConfigurationVO> serverListView;

    @FXML
    private HBox serverButtons;

    @FXML
    private Button serverAddButton;

    @FXML
    private MenuItem exportMenuItem;

    @FXML
    private MenuItem importMenuItem;

    @FXML
    private MenuItem logMenuItem;

    @FXML
    private Menu langMenu;

    @FXML
    private MenuButton fontMenuButton;

    @FXML
    private Label newVersionLabel;

    @FXML
    private Hyperlink prettyZooLink;

    private ServerViewController serverViewController = FXMLs.getController("fxml/ServerView.fxml");

    private LogViewController logViewController = FXMLs.getController("fxml/LogView.fxml");

    private PrettyZooFacade prettyZooFacade = new PrettyZooFacade();

    @FXML
    private void initialize() {
        initServerListView();
        RootPaneContext.set(rootStackPane);
        mainRightPane.setPadding(new Insets(30, 30, 30, 30));
        serverAddButton.setOnMouseClicked(event -> {
            serverListView.getSelectionModel().clearSelection();
            serverViewController.show(mainRightPane);
        });
        exportMenuItem.setOnAction(e -> onExportAction());
        importMenuItem.setOnAction(e -> onImportAction());
        logMenuItem.setOnAction(e -> {
            logViewController.show(mainRightPane);
            serverListView.selectionModelProperty().get().clearSelection();
        });
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
        Integer fontSize = prettyZooFacade.getFontSize();
        rootStackPane.setStyle("-fx-font-size: " + fontSize);
        var jfxSlider = new JFXSlider(8, 25, fontSize);
        jfxSlider.valueProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null) {
                rootStackPane.setStyle("-fx-font-size: " + newValue);
                prettyZooFacade.changeFontSize(newValue.intValue());
            }
        }));
        var jfxSliderItem = new MenuItem("", jfxSlider);
        var valueTextBinding = Bindings.createStringBinding(() -> jfxSlider.valueProperty().intValue() + "",
                jfxSlider.valueProperty());
        jfxSliderItem.textProperty().bind(valueTextBinding);
        fontMenuButton.getItems().add(jfxSliderItem);

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
        final ConfigurationVO configurationVO = new ConfigurationVO();
        prettyZooFacade.loadServerConfigurations(new DefaultConfigurationListener(configurationVO));
        serverListView.itemsProperty().set(configurationVO.getServers());
        serverListView.setCellFactory(cellCallback -> {
            var cell = new ZkServerListCell(
                    server -> serverViewController.connect(mainRightPane, server),
                    server -> serverViewController.delete(server.getZkUrl()),
                    server -> serverViewController.disconnect(server.getZkUrl())
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

    public StackPane getRootStackPane() {
        return rootStackPane;
    }

    public void showNewVersionLabel() {
        VersionChecker.hasNewVersion((latestVersion, features) -> {
            String title = "New version";
            final String content = new StringBuilder()
                    .append("current: ").append(latestVersion).append("\r\n")
                    .append("release: v").append(Version.VERSION).append("\r\n")
                    .append("features: \r\n").append(features)
                    .toString();
            newVersionLabel.setTooltip(new Tooltip("New version " + latestVersion + " released"));
            newVersionLabel.setVisible(true);
            Dialog.confirm(title, content, HostServiceContext::jumpToReleases);
        });
    }
}
