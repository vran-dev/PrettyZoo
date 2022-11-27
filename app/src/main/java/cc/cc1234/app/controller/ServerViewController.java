package cc.cc1234.app.controller;

import cc.cc1234.app.facade.PrettyZooFacade;
import cc.cc1234.app.fp.Try;
import cc.cc1234.app.listener.DefaultTreeNodeListener;
import cc.cc1234.app.util.Asserts;
import cc.cc1234.app.util.FXMLs;
import cc.cc1234.app.validator.NotNullValidator;
import cc.cc1234.app.validator.PortValidator;
import cc.cc1234.app.validator.StringNotBlankValidator;
import cc.cc1234.app.validator.StringNotEmptyValidator;
import cc.cc1234.app.view.toast.VToast;
import cc.cc1234.app.vo.ConnectionConfigurationVO;
import cc.cc1234.app.vo.ServerConfigurationVO;
import cc.cc1234.app.vo.ServerStatus;
import cc.cc1234.specification.listener.ServerListener;
import com.google.common.base.Strings;
import com.jfoenix.controls.*;
import com.jfoenix.validation.NumberValidator;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Tab;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
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

    public void delete(String zkServer) {
        prettyZooFacade.deleteServerConfiguration(zkServer);
        if (prettyZooFacade.getServerConfigurations().isEmpty()) {
            onClose();
        }
        VToast.info("Delete success");
    }

    public void disconnect(String zkServer) {
        if (nodeViewControllerMap.containsKey(zkServer)) {
            NodeViewController nodeViewController = nodeViewControllerMap.remove(zkServer);
            nodeViewController.disconnect(zkServer);
        }
    }

    private void showNewServerView(StackPane stackPane) {
        zkHost.setEditable(true);
        zkPort.setEditable(true);
        buttonHBox.getChildren().remove(deleteButton);
        buttonHBox.getChildren().remove(connectButton);
        propertyReset();
        switchIfNecessary(stackPane);
        zkHost.requestFocus();
    }

    private void showServerInfoView(StackPane stackPane, ServerConfigurationVO config) {
        if (config.getStatus() == ServerStatus.CONNECTING) {
            buttonHBox.setDisable(true);
        } else if (config.getStatus() == ServerStatus.DISCONNECTED) {
            buttonHBox.setDisable(false);
        }
        zkHost.setEditable(false);
        zkPort.setEditable(false);
        connectButton.setOnMouseClicked(e -> onConnect(stackPane, config));
        propertyBind(config);
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

    private void propertyReset() {
        zkHost.textProperty().unbind();
        zkHost.textProperty().setValue("");
        zkPort.textProperty().setValue("");
        zkAlias.textProperty().setValue("");
        sshServer.textProperty().setValue("");
        sshUsername.textProperty().setValue("");
        sshPassword.textProperty().setValue("");
        remoteServer.textProperty().setValue("");
        aclTextArea.textProperty().setValue("");
        sshTunnelCheckbox.setSelected(false);
        connectionConfigCheckbox.setSelected(false);
        connectionTimeoutInput.textProperty().setValue("");
        sessionTimeoutInput.textProperty().setValue("");
        maxRetriesInput.textProperty().setValue("");
        retryIntervalTimeInput.textProperty().setValue("");
    }

    private void propertyBind(ServerConfigurationVO config) {
        zkHost.textProperty().setValue(config.getZkHost());
        zkPort.textProperty().setValue(config.getZkPort() + "");
        zkAlias.textProperty().setValue(config.getZkAlias());
        sshServer.textProperty().setValue(config.getSshServer());
        sshServerPort.textProperty().setValue(Objects.toString(config.getSshServerPort(), ""));
        sshUsername.textProperty().setValue(config.getSshUsername());
        sshPassword.textProperty().setValue(config.getSshPassword());
        remoteServer.textProperty().setValue(config.getRemoteServer());
        remoteServerPort.textProperty().setValue(Objects.toString(config.getRemoteServerPort(), ""));
        sshTunnelCheckbox.selectedProperty().setValue(config.isSshEnabled());
        final String acl = String.join("\n", config.getAclList());
        aclTextArea.textProperty().setValue(acl);

        connectionConfigCheckbox.selectedProperty().setValue(config.isEnableConnectionAdvanceConfiguration());
        ConnectionConfigurationVO connectionAdvanceCfg = config.getConnectionConfiguration();
        connectionTimeoutInput.textProperty()
                .setValue(Objects.toString(connectionAdvanceCfg.getConnectionTimeout(), ""));
        sessionTimeoutInput.textProperty()
                .setValue(Objects.toString(connectionAdvanceCfg.getSessionTimeout(), ""));
        maxRetriesInput.textProperty()
                .setValue(Objects.toString(connectionAdvanceCfg.getMaxRetries(), ""));
        retryIntervalTimeInput.textProperty()
                .setValue(Objects.toString(connectionAdvanceCfg.getRetryIntervalTime(), ""));
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

        initConfigTabPaneBinding();
        aclTextArea.setPromptText("ACL:\r"
                + "digest:test:test\r"
                + "auth:test:test\r"
                + "\n");
        initPasswordComponent();
        initValidator();
    }

    private void initConfigTabPaneBinding() {
        extendConfigTabPane.getTabs().clear();

        // when check tunnel config box
        sshTunnelCheckbox.selectedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                if (!extendConfigTabPane.getTabs().contains(tunnelConfigTab)) {
                    extendConfigTabPane.getTabs().add(0, tunnelConfigTab);
                    extendConfigTabPane.getSelectionModel().select(tunnelConfigTab);
                }
            } else {
                extendConfigTabPane.getTabs().remove(tunnelConfigTab);
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
            if (c.getList().isEmpty()) {
                serverBasicInfoPane.getChildren().remove(extendConfigTabPane);
                GridPane.setRowIndex(buttonHBox, 3);
            } else {
                if (!serverBasicInfoPane.getChildren().contains(extendConfigTabPane)) {
                    serverBasicInfoPane.getChildren().add(extendConfigTabPane);
                    extendConfigTabPane.getSelectionModel().select(c.getList().iterator().next());
                    GridPane.setColumnIndex(extendConfigTabPane, 0);
                    GridPane.setRowIndex(extendConfigTabPane, 3);
                    GridPane.setRowIndex(buttonHBox, 4);
                }
            }
        });
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
        sshUsername.setValidators(new NotNullValidator());
        sshPassword.setValidators(new NotNullValidator());

        connectionTimeoutInput.setValidators(new StringNotBlankValidator(), new NumberValidator());
        maxRetriesInput.setValidators(new StringNotBlankValidator(), new NumberValidator());
        sessionTimeoutInput.setValidators(new StringNotBlankValidator(), new NumberValidator());
        retryIntervalTimeInput.setValidators(new StringNotBlankValidator(), new NumberValidator());
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
            String serverUrl = zkHost.getText() + ":" + zkPort.getText();
            if (zkHost.isEditable() && prettyZooFacade.hasServerConfiguration(serverUrl)) {
                // new server must be unique
                VToast.error(serverUrl + " already exists");
            } else {
                var serverConfigVO = new ServerConfigurationVO();
                // zookeeper server config
                serverConfigVO.setZkAlias(zkAlias.textProperty().get());
                serverConfigVO.setZkHost(zkHost.textProperty().get());
                serverConfigVO.setZkPort(Integer.parseInt(zkPort.getText()));
                serverConfigVO.setZkUrl(zkHost.getText() + ":" + zkPort.getText());
                // ssh-tunnel config
                serverConfigVO.setRemoteServer(remoteServer.textProperty().get());
                if (Strings.isNullOrEmpty(remoteServerPort.getText())) {
                    serverConfigVO.setRemoteServerPort(null);
                } else {
                    serverConfigVO.setRemoteServerPort(Integer.parseInt(remoteServerPort.getText()));
                }
                serverConfigVO.setSshUsername(sshUsername.textProperty().get());
                serverConfigVO.setSshPassword(sshPassword.textProperty().get());
                serverConfigVO.setSshServer(sshServer.textProperty().get());
                if (Strings.isNullOrEmpty(sshServerPort.getText())) {
                    serverConfigVO.setSshServerPort(null);
                } else {
                    serverConfigVO.setSshServerPort(Integer.parseInt(sshServerPort.getText()));
                }
                serverConfigVO.setSshEnabled(sshTunnelCheckbox.isSelected());
                serverConfigVO.setEnableConnectionAdvanceConfiguration(connectionConfigCheckbox.isSelected());
                if (connectionConfigCheckbox.isSelected()) {
                    ConnectionConfigurationVO advanceConfig = new ConnectionConfigurationVO();
                    advanceConfig.setConnectionTimeout(Integer.parseInt(connectionTimeoutInput.getText()));
                    advanceConfig.setSessionTimeout(Integer.parseInt(sessionTimeoutInput.getText()));
                    advanceConfig.setMaxRetries(Integer.parseInt(maxRetriesInput.getText()));
                    advanceConfig.setRetryIntervalTime(Integer.parseInt(retryIntervalTimeInput.getText()));
                    serverConfigVO.setConnectionConfiguration(advanceConfig);
                }
                // zookeeper ACL config
                List<String> acls = Arrays.stream(aclTextArea.textProperty().get().split("\n"))
                        .filter(acl -> !Strings.isNullOrEmpty(acl))
                        .collect(Collectors.toList());
                serverConfigVO.getAclList().addAll(acls);

                Try.of(() -> prettyZooFacade.saveServerConfiguration(serverConfigVO))
                        .onSuccess(e -> {
                            if (zkHost.isEditable()) {
                                onClose();
                            }
                            VToast.info("save success");
                        })
                        .onFailure(e -> {
                            VToast.error(e.getMessage());
                        });
            }
        }
    }

    private boolean baseValidateBeforeSave() {
        boolean baseValidate = true;
        if (connectionConfigCheckbox.isSelected()) {
            baseValidate = baseValidate && Stream.of(connectionTimeoutInput.validate(),
                            sessionTimeoutInput.validate(),
                            maxRetriesInput.validate(),
                            retryIntervalTimeInput.validate())
                    .allMatch(t -> t);
        }
        if (sshTunnelCheckbox.isSelected()) {
            baseValidate = baseValidate && Stream.of(
                    zkHost.validate(),
                    zkPort.validate(),
                    zkAlias.validate(),
                    remoteServer.validate(),
                    remoteServerPort.validate(),
                    sshUsername.validate(),
                    sshPassword.validate(),
                    sshServer.validate(),
                    sshServerPort.validate()
            ).allMatch(t -> t);
        }
        baseValidate = baseValidate && Stream.of(zkHost.validate(), zkPort.validate(), zkAlias.validate())
                .allMatch(t -> t);
        return baseValidate;
    }

    private void onDelete() {
        Asserts.notBlank(zkHost.getText(), "server can not be null");
        Asserts.notBlank(zkPort.getText(), "port can not be null");
        String url = zkHost.getText() + ":" + zkPort.getText();
        prettyZooFacade.deleteServerConfiguration(url);
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
            Asserts.assertTrue(prettyZooFacade.hasServerConfiguration(serverConfigurationVO.getZkUrl()),
                    "save config first");
        }).onSuccess(o -> {
            if (serverConfigurationVO.getStatus() == ServerStatus.DISCONNECTED) {
                serverConfigurationVO.setStatus(ServerStatus.CONNECTING);
            }
            buttonHBox.setDisable(true);
            NodeViewController nodeViewController = retrieveNodeViewController(serverConfigurationVO.getZkUrl());
            prettyZooFacade.connect(serverConfigurationVO.getZkUrl(),
                            List.of(new DefaultTreeNodeListener()),
                            List.of(new ServerListener() {
                                @Override
                                public void onClose(String serverUrl) {
                                    if (serverUrl.equals(serverConfigurationVO.getZkUrl())) {
                                        Platform.runLater(() -> {
                                            serverConfigurationVO.setStatus(ServerStatus.DISCONNECTED);
                                            if (closeHook != null) {
                                                closeHook.run();
                                            }
                                        });
                                    }
                                }

                                @Override
                                public void onReconnecting(String serverHost) {
                                    if (serverHost.equals(serverConfigurationVO.getZkUrl())) {
                                        Platform.runLater(() -> {
                                            serverConfigurationVO.setStatus(ServerStatus.RECONNECTING);
                                            VToast.error(serverHost + " lost connection");
                                        });
                                    }
                                }

                                @Override
                                public void onConnected(String serverHost) {
                                    if (serverHost.equals(serverConfigurationVO.getZkUrl())) {
                                        Platform.runLater(() -> {
                                            if (serverConfigurationVO.getStatus() == ServerStatus.RECONNECTING) {
                                                VToast.info("reconnect " + serverHost + " success");
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

    private NodeViewController retrieveNodeViewController(String server) {
        if (nodeViewControllerMap.containsKey(server)) {
            return nodeViewControllerMap.get(server);
        } else {
            NodeViewController nodeViewController = FXMLs.getController("fxml/NodeListView.fxml");
            nodeViewControllerMap.put(server, nodeViewController);
            return nodeViewController;
        }
    }

    private void connectSuccessCallback(StackPane parent,
                                        NodeViewController nodeViewController,
                                        ServerConfigurationVO serverConfigurationVO) {
        Platform.runLater(() -> {
            nodeViewController.show(parent, serverConfigurationVO.getZkUrl());
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
