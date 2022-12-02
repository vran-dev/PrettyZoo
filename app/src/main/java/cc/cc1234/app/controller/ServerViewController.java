package cc.cc1234.app.controller;

import cc.cc1234.app.context.PrimaryStageContext;
import cc.cc1234.app.converter.IntegerNumberConverter;
import cc.cc1234.app.facade.PrettyZooFacade;
import cc.cc1234.app.fp.Try;
import cc.cc1234.app.listener.DefaultTreeNodeListener;
import cc.cc1234.app.util.Asserts;
import cc.cc1234.app.util.FXMLs;
import cc.cc1234.app.util.ResourceBundleUtils;
import cc.cc1234.app.validator.PortValidator;
import cc.cc1234.app.validator.StringNotBlankValidator;
import cc.cc1234.app.validator.StringNotEmptyValidator;
import cc.cc1234.app.view.toast.VToast;
import cc.cc1234.app.vo.ConnectionConfigurationVO;
import cc.cc1234.app.vo.ServerConfigurationVO;
import cc.cc1234.app.vo.ServerStatus;
import cc.cc1234.specification.listener.ServerListener;
import com.jfoenix.controls.*;
import com.jfoenix.validation.NumberValidator;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Stream;

public class ServerViewController {

    private static final Logger log = LoggerFactory.getLogger(ServerViewController.class);

    @FXML
    private AnchorPane serverInfoPane;

    @FXML
    private GridPane serverBasicInfoPane;

    @FXML
    private JFXToggleButton sshTunnelCheckbox;

    @FXML
    private JFXToggleButton connectionConfigCheckbox;

    @FXML
    private JFXTabPane extendConfigTabPane;

    @FXML
    private Tab tunnelConfigTab;

    @FXML
    private Tab connectionConfigTab;

    @FXML
    private ProgressBar sshTunnelProgressBarTo;

    @FXML
    private JFXTextArea aclTextArea;

    @FXML
    private JFXTextField zkHost;

    @FXML
    private JFXTextField zkPort;

    @FXML
    private JFXTextField zkAlias;

    @FXML
    private JFXTextField sshServer;

    @FXML
    private JFXTextField sshServerPort;

    @FXML
    private JFXTextField sshUsername;

    @FXML
    private JFXPasswordField sshPassword;

    @FXML
    private JFXTextField sshKeyFileField;

    @FXML
    private Button sshKeyFileClearButton;

    @FXML
    private JFXButton sshPasswordVisibleButton;

    @FXML
    private JFXTextField remoteServer;

    @FXML
    private JFXTextField remoteServerPort;

    @FXML
    private JFXTextField connectionTimeoutInput;

    @FXML
    private JFXTextField sessionTimeoutInput;

    @FXML
    private JFXTextField maxRetriesInput;

    @FXML
    private JFXTextField retryIntervalTimeInput;

    @FXML
    private Button closeButton;

    @FXML
    private Button saveButton;

    @FXML
    private Button deleteButton;

    @FXML
    private Button connectButton;

    @FXML
    private HBox buttonHBox;

    private PrettyZooFacade prettyZooFacade = new PrettyZooFacade();

    private ServerConfigurationVO serverConfiguration = new ServerConfigurationVO();

    private Map<String, NodeViewController> nodeViewControllerMap = new ConcurrentHashMap<>();

    private volatile NodeViewController currentNodeViewController = null;

    private Runnable closeHook;

    public void show(StackPane stackPane) {
        show(stackPane, null);
    }

    public void show(StackPane stackPane, ServerConfigurationVO config) {
        resetValidate();
        if (config == null) {
            showNewServerView(stackPane);
        } else if (config.getStatus() == ServerStatus.CONNECTED || config.getStatus() == ServerStatus.RECONNECTING) {
            showNodeListView(stackPane, config);
        } else {
            showServerInfoView(stackPane, config);
        }
    }

    public void connect(StackPane stackPane, ServerConfigurationVO configurationVO) {
        onConnect(stackPane, configurationVO);
    }

    public void deleteById(String id) {
        prettyZooFacade.deleteServerConfigurationById(id);
        if (prettyZooFacade.getServerConfigurations().isEmpty()) {
            onClose();
        }
        VToast.info("Delete success");
    }

    public void disconnect(String id) {
        if (nodeViewControllerMap.containsKey(id)) {
            NodeViewController nodeViewController = nodeViewControllerMap.remove(id);
            nodeViewController.disconnectById(id);
        }
    }

    private void showNewServerView(StackPane stackPane) {
        buttonHBox.getChildren().remove(deleteButton);
        buttonHBox.getChildren().remove(connectButton);
        serverConfiguration.reset();
        switchIfNecessary(stackPane);
        zkHost.requestFocus();
    }

    private void showServerInfoView(StackPane stackPane, ServerConfigurationVO config) {
        zkHost.setEditable(true);
        zkPort.setEditable(true);
        if (config.getStatus() == ServerStatus.CONNECTING) {
            buttonHBox.setDisable(true);
            zkHost.setEditable(false);
            zkPort.setEditable(false);
        } else if (config.getStatus() == ServerStatus.DISCONNECTED) {
            buttonHBox.setDisable(false);
        }

        connectButton.setOnMouseClicked(e -> onConnect(stackPane, config));
        propertyUpdate(config);
        showConnectAndSaveButton();
        switchIfNecessary(stackPane);
    }

    private void switchIfNecessary(StackPane stackPane) {
        if (!stackPane.getChildren().contains(serverInfoPane)) {
            if (currentNodeViewController == null) {
                stackPane.getChildren().add(serverInfoPane);
            } else {
                currentNodeViewController.hideAndThen(() -> {
                    stackPane.getChildren().add(serverInfoPane);
                });
            }
        } else {
            stackPane.getChildren().remove(serverInfoPane);
            stackPane.getChildren().add(serverInfoPane);
        }
    }

    private void showNodeListView(StackPane stackPane, ServerConfigurationVO config) {
        onConnect(stackPane, config);
    }

    private void showConnectAndSaveButton() {
        if (!buttonHBox.getChildren().contains(connectButton)) {
            buttonHBox.getChildren().add(connectButton);
        }
        if (!buttonHBox.getChildren().contains(deleteButton)) {
            buttonHBox.getChildren().add(deleteButton);
        }
    }

    private void propertyUpdate(ServerConfigurationVO config) {
        this.serverConfiguration.update(config);
    }

    public void onClose() {
        final StackPane parent = (StackPane) serverInfoPane.getParent();
        if (parent != null) {
            parent.getChildren().remove(serverInfoPane);
            if (closeHook != null) {
                closeHook.run();
            }
        }
    }

    @FXML
    private void initialize() {
        sshTunnelProgressBarTo.setProgress(ProgressBar.INDETERMINATE_PROGRESS);
        closeButton.setOnMouseClicked(e -> onClose());
        saveButton.setOnMouseClicked(e -> onSave());
        deleteButton.setOnMouseClicked(e -> onDelete());
        sshKeyFileField.setOnMouseClicked(e -> onChooseSshKeyFile());
        sshKeyFileClearButton.setOnAction(e -> sshKeyFileField.setText(""));

        initConfigTabPaneBinding();
        aclTextArea.setPromptText("ACL:\r"
                + "digest:test:test\r"
                + "auth:test:test\r"
                + "\n");
        initPasswordComponent();
        initValidator();
        propertyBind();
    }

    private void initConfigTabPaneBinding() {
        // when check tunnel config box
        sshTunnelCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                if (!extendConfigTabPane.getTabs().contains(tunnelConfigTab)) {
                    extendConfigTabPane.getTabs().add(0, tunnelConfigTab);
                    extendConfigTabPane.getSelectionModel().select(tunnelConfigTab);
                }
                zkHost.setDisable(true);
                if (serverConfiguration.getZkHost() != null && !serverConfiguration.getZkHost().isBlank()) {
                    zkHost.getProperties().put("originValue", serverConfiguration.getZkHost());
                }
                serverConfiguration.setZkHost("127.0.0.1");
            } else {
                extendConfigTabPane.getTabs().remove(tunnelConfigTab);
                zkHost.setDisable(false);
                if (zkHost.getProperties().containsKey("originValue")) {
                    serverConfiguration.setZkHost((String) zkHost.getProperties().get("originValue"));
                }
            }
        });

        // when check connection config box
        connectionConfigCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                if (!extendConfigTabPane.getTabs().contains(connectionConfigTab)) {
                    int index = extendConfigTabPane.getTabs().size() > 0 ? 1 : 0;
                    extendConfigTabPane.getTabs().add(index, connectionConfigTab);
                    extendConfigTabPane.getSelectionModel().select(connectionConfigTab);
                }
            } else {
                extendConfigTabPane.getTabs().remove(connectionConfigTab);
            }
        });

        extendConfigTabPane.getTabs().addListener((ListChangeListener<Tab>) c -> {
            ObservableList<Node> child = serverBasicInfoPane.getChildren();
            if (c.getList().isEmpty()) {
                child.remove(extendConfigTabPane);
                GridPane.setRowIndex(buttonHBox, 3);
            } else {
                if (!child.contains(extendConfigTabPane)) {
                    child.add(extendConfigTabPane);
                    extendConfigTabPane.getSelectionModel().select(c.getList().iterator().next());
                    GridPane.setColumnIndex(extendConfigTabPane, 0);
                    GridPane.setRowIndex(extendConfigTabPane, 3);
                    GridPane.setRowIndex(buttonHBox, 4);
                }
            }
        });
        extendConfigTabPane.getTabs().clear();
    }

    private void initPasswordComponent() {
        final var originPromptKey = "originPromptText";
        final var originTextKey = "originText";
        sshPasswordVisibleButton.setOnMousePressed(e -> {
            sshPassword.getProperties().put(originPromptKey, sshPassword.getPromptText());
            if (sshPassword.getText() != null && !sshPassword.getText().isEmpty()) {
                sshPassword.getProperties().put(originTextKey, sshPassword.getText());
                sshPassword.promptTextProperty().set(sshPassword.getText());
                sshPassword.setText("");
            }
        });
        sshPasswordVisibleButton.setOnMouseReleased(e -> {
            var originPromptText = ((String) sshPassword.getProperties()
                    .getOrDefault(originPromptKey, ""));
            var originText = ((String) sshPassword.getProperties()
                    .getOrDefault(originTextKey, ""));
            sshPassword.promptTextProperty().set(originPromptText);
            sshPassword.textProperty().set(originText);

            sshPassword.getProperties().remove(originTextKey);
            sshPassword.getProperties().remove(originPromptKey);
        });
    }

    private void initValidator() {
        zkHost.setValidators(new StringNotBlankValidator());
        zkPort.setValidators(new PortValidator());
        zkPort.textProperty().addListener(((observable, oldValue, newValue) -> {
            if (newValue != null && !newValue.equals("")) {
                zkPort.validate();
            }
        }));
        zkAlias.setValidators(new StringNotEmptyValidator());

        remoteServer.setValidators(new StringNotBlankValidator());
        remoteServerPort.setValidators(new PortValidator());

        sshServer.setValidators(new StringNotBlankValidator());
        sshServerPort.setValidators(new PortValidator());
        sshUsername.setValidators(new StringNotBlankValidator());

        connectionTimeoutInput.setValidators(new StringNotBlankValidator(), new NumberValidator());
        maxRetriesInput.setValidators(new StringNotBlankValidator(), new NumberValidator());
        sessionTimeoutInput.setValidators(new StringNotBlankValidator(), new NumberValidator());
        retryIntervalTimeInput.setValidators(new StringNotBlankValidator(), new NumberValidator());
    }

    private void propertyBind() {
        // basic config
        zkHost.textProperty().bindBidirectional(serverConfiguration.zkHostProperty());
        zkPort.textProperty().bindBidirectional(serverConfiguration.zkPortProperty(), IntegerNumberConverter.INSTANCE);
        zkAlias.textProperty().bindBidirectional(serverConfiguration.zkAliasProperty());
        aclTextArea.textProperty().bindBidirectional(serverConfiguration.aclProperty());

        // advance connection config
        connectionConfigCheckbox.selectedProperty()
                .bindBidirectional(serverConfiguration.enableConnectionAdvanceConfigurationProperty());
        ConnectionConfigurationVO connectionConfig = serverConfiguration.getConnectionConfiguration();
        connectionTimeoutInput.textProperty()
                .bindBidirectional(connectionConfig.connectionTimeoutProperty(), IntegerNumberConverter.INSTANCE);
        maxRetriesInput.textProperty()
                .bindBidirectional(connectionConfig.maxRetriesProperty(), IntegerNumberConverter.INSTANCE);
        sessionTimeoutInput.textProperty()
                .bindBidirectional(connectionConfig.sessionTimeoutProperty(), IntegerNumberConverter.INSTANCE);
        retryIntervalTimeInput.textProperty()
                .bindBidirectional(connectionConfig.retryIntervalTimeProperty(), IntegerNumberConverter.INSTANCE);

        // ssh tunnel config
        sshServer.textProperty().bindBidirectional(serverConfiguration.sshServerProperty());
        sshServerPort.textProperty()
                .bindBidirectional(serverConfiguration.sshServerPortProperty(), IntegerNumberConverter.INSTANCE);
        sshUsername.textProperty().bindBidirectional(serverConfiguration.sshUsernameProperty());
        sshPassword.textProperty().bindBidirectional(serverConfiguration.sshPasswordProperty());
        sshKeyFileField.textProperty().bindBidirectional(serverConfiguration.sshKeyFilePathProperty());
        remoteServer.textProperty().bindBidirectional(serverConfiguration.remoteServerProperty());
        remoteServerPort.textProperty()
                .bindBidirectional(serverConfiguration.remoteServerPortProperty(), IntegerNumberConverter.INSTANCE);
        sshTunnelCheckbox.selectedProperty().bindBidirectional(serverConfiguration.sshEnabledProperty());
    }

    private void resetValidate() {
        zkHost.resetValidation();
        zkPort.resetValidation();
        zkAlias.resetValidation();
        remoteServer.resetValidation();
        remoteServerPort.resetValidation();
        sshUsername.resetValidation();
        sshPassword.resetValidation();
        sshServer.resetValidation();
        sshServerPort.resetValidation();
        // advance connection config validator reset
        connectionTimeoutInput.resetValidation();
        maxRetriesInput.resetValidation();
        sessionTimeoutInput.resetValidation();
        retryIntervalTimeInput.resetValidation();
    }

    private void onSave() {
        resetValidate();
        boolean passed = baseValidateBeforeSave();
        if (passed) {
            Try.of(() -> prettyZooFacade.saveServerConfiguration(serverConfiguration))
                    .onSuccess(e -> {
                        if (serverConfiguration.getId() == null || serverConfiguration.getId().isBlank()) {
                            onClose();
                        }
                        VToast.info("save success");
                    })
                    .onFailure(e -> {
                        VToast.error(e.getMessage());
                    });
        }
    }

    private boolean baseValidateBeforeSave() {
        boolean baseValidate = true;
        if (connectionConfigCheckbox.isSelected()) {
            baseValidate = Stream.of(connectionTimeoutInput.validate(),
                            sessionTimeoutInput.validate(),
                            maxRetriesInput.validate(),
                            retryIntervalTimeInput.validate())
                    .allMatch(t -> t);
        }
        if (sshTunnelCheckbox.isSelected()) {
            baseValidate = baseValidate && Stream.of(
                    zkPort.validate(),
                    zkAlias.validate(),
                    remoteServer.validate(),
                    remoteServerPort.validate(),
                    sshUsername.validate(),
                    sshPassword.validate(),
                    sshServer.validate(),
                    sshServerPort.validate()
            ).allMatch(t -> t);
        } else {
            baseValidate = baseValidate && Stream.of(zkHost.validate(), zkPort.validate(), zkAlias.validate())
                    .allMatch(t -> t);
        }
        return baseValidate;
    }

    private void onChooseSshKeyFile() {
        var fileChooser = new FileChooser();
        fileChooser.setTitle(ResourceBundleUtils.getContent("server.input.ssh.key-file.prompt"));
        File configFile = fileChooser.showOpenDialog(PrimaryStageContext.get());
        // configFile is null means click cancel
        if (configFile == null) {
            return;
        }
        String path = configFile.getAbsolutePath();
        sshKeyFileField.setText(path);
    }

    private void onDelete() {
        Asserts.notBlank(zkHost.getText(), "server can not be null");
        Asserts.notBlank(zkPort.getText(), "port can not be null");
        String id = serverConfiguration.getId();
        prettyZooFacade.deleteServerConfigurationById(id);
        if (prettyZooFacade.getServerConfigurations().isEmpty()) {
            onClose();
        }
        VToast.info("Delete success");
    }

    private void onConnect(StackPane parent, ServerConfigurationVO serverConfigurationVO) {
        if (serverConfigurationVO.getStatus() == ServerStatus.CONNECTING) {
            return;
        }
        Try.of(() -> {
            Asserts.notNull(serverConfigurationVO, "save config first");
            Asserts.assertTrue(prettyZooFacade.hasServerConfiguration(serverConfigurationVO.getId()),
                    "save config first");
        }).onSuccess(o -> {
            if (serverConfigurationVO.getStatus() == ServerStatus.DISCONNECTED) {
                serverConfigurationVO.setStatus(ServerStatus.CONNECTING);
            }
            buttonHBox.setDisable(true);
            NodeViewController nodeViewController = retrieveNodeViewController(serverConfigurationVO.getId());
            prettyZooFacade.connect(serverConfigurationVO.getId(),
                            List.of(new DefaultTreeNodeListener()),
                            List.of(new ServerListener() {
                                @Override
                                public void onClose(String id) {
                                    if (id.equals(serverConfigurationVO.getId())) {
                                        log.info("server [{}] {} closed", id,
                                                prettyZooFacade.getServerConfigurationById(id).getLabel());
                                        Platform.runLater(() -> {
                                            serverConfigurationVO.setStatus(ServerStatus.DISCONNECTED);
                                            if (closeHook != null) {
                                                closeHook.run();
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onReconnecting(String id) {
                                    if (id.equals(serverConfigurationVO.getId())) {
                                        Platform.runLater(() -> {
                                            serverConfigurationVO.setStatus(ServerStatus.RECONNECTING);
                                            VToast.error(prettyZooFacade.getServerConfigurationById(id).getLabel()
                                                    + " lost connection");
                                        });
                                    }
                                }

                                @Override
                                public void onConnected(String id) {
                                    if (id.equals(serverConfigurationVO.getId())) {
                                        Platform.runLater(() -> {
                                            if (serverConfigurationVO.getStatus() == ServerStatus.RECONNECTING) {
                                                VToast.info("reconnect "
                                                        + prettyZooFacade.getServerConfigurationById(id).getLabel()
                                                        + " success");
                                            }
                                            serverConfigurationVO.setStatus(ServerStatus.CONNECTED);
                                        });
                                    }
                                }
                            }))
                    .thenAccept(v -> connectSuccessCallback(parent, nodeViewController, serverConfigurationVO))
                    .exceptionally(e -> connectErrorCallback(e, serverConfigurationVO));
        }).onFailure(e -> {
            log.error("connect server error", e);
            VToast.error(e.getMessage());
        });
    }

    private NodeViewController retrieveNodeViewController(String id) {
        if (nodeViewControllerMap.containsKey(id)) {
            return nodeViewControllerMap.get(id);
        } else {
            NodeViewController nodeViewController = FXMLs.getController("fxml/NodeListView.fxml");
            nodeViewControllerMap.put(id, nodeViewController);
            return nodeViewController;
        }
    }

    private void connectSuccessCallback(StackPane parent,
                                        NodeViewController nodeViewController,
                                        ServerConfigurationVO serverConfigurationVO) {
        Platform.runLater(() -> {
            nodeViewController.show(parent, serverConfigurationVO.getId());
            if (currentNodeViewController != null) {
                currentNodeViewController.hideIfNotActive();
            }
            currentNodeViewController = nodeViewController;
            parent.getChildren().remove(serverInfoPane);

            if (serverConfigurationVO.getStatus() == ServerStatus.CONNECTING) {
                serverConfigurationVO.setStatus(ServerStatus.CONNECTED);
            }
            buttonHBox.setDisable(false);
        });
    }

    private Void connectErrorCallback(Throwable e, ServerConfigurationVO serverConfigurationVO) {
        log.error("connect server error", e);
        Platform.runLater(() -> {
            buttonHBox.setDisable(false);
            serverConfigurationVO.setStatus(ServerStatus.DISCONNECTED);
            VToast.error(e.getCause().getMessage());
        });
        return null;
    }

    public void setOnClose(Runnable runnable) {
        this.closeHook = runnable;
    }
}
