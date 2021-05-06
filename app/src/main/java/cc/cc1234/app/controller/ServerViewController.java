package cc.cc1234.app.controller;

import cc.cc1234.app.facade.PrettyZooFacade;
import cc.cc1234.app.fp.Try;
import cc.cc1234.app.listener.DefaultTreeNodeListener;
import cc.cc1234.app.util.Asserts;
import cc.cc1234.app.util.FXMLs;
import cc.cc1234.app.validator.NotNullValidator;
import cc.cc1234.app.validator.StringNotEmptyValidator;
import cc.cc1234.app.validator.ZkServerIdentityValidator;
import cc.cc1234.app.view.toast.VToast;
import cc.cc1234.app.view.transitions.Transitions;
import cc.cc1234.app.vo.ServerConfigurationVO;
import cc.cc1234.app.vo.ServerStatus;
import cc.cc1234.specification.listener.ServerListener;
import com.google.common.base.Strings;
import com.jfoenix.controls.JFXTextField;
import com.jfoenix.controls.JFXToggleButton;
import com.jfoenix.validation.RegexValidator;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextArea;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ServerViewController {

    private static final Logger log = LoggerFactory.getLogger(ServerViewController.class);

    @FXML
    private AnchorPane serverInfoPane;

    @FXML
    private JFXToggleButton sshTunnelCheckbox;

    @FXML
    private AnchorPane sshTunnelPane;

    @FXML
    private ProgressBar sshTunnelProgressBarTo;

    @FXML
    private ProgressBar sshTunnelProgressBarFrom;

    @FXML
    private TextArea aclTextArea;

    @FXML
    private JFXTextField zkServer;

    @FXML
    private JFXTextField zkAlias;

    @FXML
    private JFXTextField sshServer;

    @FXML
    private JFXTextField sshUsername;

    @FXML
    private JFXTextField sshPassword;

    @FXML
    private JFXTextField remoteServer;

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
        zkServer.setEditable(true);
        buttonHBox.getChildren().remove(deleteButton);
        buttonHBox.getChildren().remove(connectButton);
        propertyReset();
        switchIfNecessary(stackPane);
        zkServer.requestFocus();
    }

    private void showServerInfoView(StackPane stackPane, ServerConfigurationVO config) {
        if (config.getStatus() == ServerStatus.CONNECTING) {
            buttonHBox.setDisable(true);
        } else if (config.getStatus() == ServerStatus.DISCONNECTED) {
            buttonHBox.setDisable(false);
        }
        zkServer.setEditable(false);
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
        zkServer.textProperty().unbind();
        zkServer.textProperty().setValue("");
        zkAlias.textProperty().setValue("");
        sshServer.textProperty().setValue("");
        sshUsername.textProperty().setValue("");
        sshPassword.textProperty().setValue("");
        remoteServer.textProperty().setValue("");
        aclTextArea.textProperty().setValue("");
    }

    private void propertyBind(ServerConfigurationVO config) {
        zkServer.textProperty().setValue(config.getZkServer());
        zkAlias.textProperty().setValue(config.getZkAlias());
        sshServer.textProperty().setValue(config.getSshServer());
        sshUsername.textProperty().setValue(config.getSshUsername());
        sshPassword.textProperty().setValue(config.getSshPassword());
        remoteServer.textProperty().setValue(config.getRemoteServer());
        sshTunnelCheckbox.selectedProperty().setValue(config.isSshEnabled());
        final String acl = String.join("\n", config.getAclList());
        aclTextArea.textProperty().setValue(acl);
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
        sshTunnelViewPropertyBind();
        closeButton.setOnMouseClicked(e -> onClose());
        saveButton.setOnMouseClicked(e -> onSave());
        deleteButton.setOnMouseClicked(e -> onDelete());
        serverInfoPane.setEffect(new DropShadow(15, 1, 1, Color.valueOf("#DDD")));
        sshTunnelPane.getChildren().forEach(node -> {
            node.setOnMouseClicked(e -> {
                if (!sshTunnelCheckbox.isSelected()) {
                    Transitions.zoomInLittleAndReverse(sshTunnelCheckbox).playFromStart();
                }
                e.consume();
            });
        });

        zkServer.setFont(Font.font("", FontWeight.BOLD, 14));
        aclTextArea.setPromptText("ACL:\r" +
                "digest:test:test\r" +
                "auth:test:test\r" +
                "\n");
        initValidator();
    }

    private void initValidator() {
        var serverMatchPattern = new RegexValidator("must match pattern: [host:port]");
        serverMatchPattern.setRegexPattern(".*\\:\\d+$");
        var identityValidator = new ZkServerIdentityValidator();
        zkServer.setValidators(serverMatchPattern, identityValidator);
        zkAlias.setValidators(new StringNotEmptyValidator());

        var remoteServerMatchPattern = new RegexValidator("should be [host:port]");
        remoteServerMatchPattern.setRegexPattern(".*\\:\\d+$");
        remoteServer.setValidators(remoteServerMatchPattern);
        sshUsername.setValidators(new NotNullValidator());
        sshPassword.setValidators(new NotNullValidator());

        var sshServerMatchPattern = new RegexValidator("should be [host:port]");
        sshServerMatchPattern.setRegexPattern(".*\\:\\d+$");
        sshServer.setValidators(sshServerMatchPattern);
    }

    private void resetValidate() {
        zkServer.resetValidation();
        zkAlias.resetValidation();
        remoteServer.resetValidation();
        sshUsername.resetValidation();
        sshPassword.resetValidation();
        sshServer.resetValidation();
    }

    private void sshTunnelViewPropertyBind() {
        var sshTunnelEnabledProperty = sshTunnelCheckbox.selectedProperty();
        var disableBinding = Bindings.createBooleanBinding(() -> !sshTunnelEnabledProperty.get(), sshTunnelEnabledProperty);
        sshServer.disableProperty().bind(disableBinding);
        sshUsername.disableProperty().bind(disableBinding);
        sshPassword.disableProperty().bind(disableBinding);
        remoteServer.disableProperty().bind(disableBinding);
    }

    private void onSave() {
        resetValidate();

        boolean passed;
        if (sshTunnelCheckbox.isSelected()) {
            passed = Stream.of(
                    zkServer.validate(),
                    zkAlias.validate(),
                    remoteServer.validate(),
                    sshUsername.validate(),
                    sshPassword.validate(),
                    sshServer.validate()
            ).allMatch(t -> t);
        } else {
            passed = Stream.of(zkServer.validate(), zkAlias.validate()).allMatch(t -> t);
        }

        if (passed) {
            var serverConfigVO = new ServerConfigurationVO();
            serverConfigVO.setZkServer(zkServer.textProperty().get());
            serverConfigVO.setRemoteServer(remoteServer.textProperty().get());
            serverConfigVO.setSshUsername(sshUsername.textProperty().get());
            serverConfigVO.setSshPassword(sshPassword.textProperty().get());
            serverConfigVO.setSshServer(sshServer.textProperty().get());
            serverConfigVO.setZkAlias(zkAlias.textProperty().get());
            serverConfigVO.setSshEnabled(sshTunnelCheckbox.isSelected());
            List<String> acls = Arrays.stream(aclTextArea.textProperty().get().split("\n"))
                    .filter(acl -> !Strings.isNullOrEmpty(acl))
                    .collect(Collectors.toList());
            serverConfigVO.getAclList().addAll(acls);
            prettyZooFacade.saveServerConfiguration(serverConfigVO);
            if (zkServer.isEditable()) {
                onClose();
            }
            VToast.info("save success");
        }
    }

    private void onDelete() {
        Asserts.notBlank(zkServer.getText(), "server can not be null");
        prettyZooFacade.deleteServerConfiguration(zkServer.getText());
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
            Asserts.assertTrue(prettyZooFacade.hasServerConfiguration(serverConfigurationVO.getZkServer()), "save config first");
        }).onSuccess(o -> {
            if (serverConfigurationVO.getStatus() == ServerStatus.DISCONNECTED) {
                serverConfigurationVO.setStatus(ServerStatus.CONNECTING);
            }
            buttonHBox.setDisable(true);
            NodeViewController nodeViewController = retrieveNodeViewController(serverConfigurationVO.getZkServer());
            prettyZooFacade.connect(serverConfigurationVO.getZkServer(), List.of(new DefaultTreeNodeListener()), List.of(new ServerListener() {
                @Override
                public void onClose(String serverHost) {
                    if (serverHost.equals(serverConfigurationVO.getZkServer())) {
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
                    if (serverHost.equals(serverConfigurationVO.getZkServer())) {
                        Platform.runLater(() -> {
                            serverConfigurationVO.setStatus(ServerStatus.RECONNECTING);
                            VToast.error(serverHost + " lost connection");
                        });
                    }
                }

                @Override
                public void onConnected(String serverHost) {
                    if (serverHost.equals(serverConfigurationVO.getZkServer())) {
                        Platform.runLater(() -> {
                            if (serverConfigurationVO.getStatus() == ServerStatus.RECONNECTING) {
                                VToast.info("reconnect " + serverHost + " success");
                            }
                            serverConfigurationVO.setStatus(ServerStatus.CONNECTED);
                        });
                    }
                }
            })).thenAccept(v -> Platform.runLater(() -> {
                nodeViewController.show(parent, serverConfigurationVO.getZkServer());
                if (currentNodeViewController != null) {
                    currentNodeViewController.hideIfNotActive();
                }
                currentNodeViewController = nodeViewController;
                parent.getChildren().remove(serverInfoPane);
                if (serverConfigurationVO.getStatus() == ServerStatus.CONNECTING) {
                    serverConfigurationVO.setStatus(ServerStatus.CONNECTED);
                }
                buttonHBox.setDisable(false);
            })).exceptionally(e -> {
                log.error("connect server error", e);
                Platform.runLater(() -> {
                    buttonHBox.setDisable(false);
                    serverConfigurationVO.setStatus(ServerStatus.DISCONNECTED);
                    VToast.error(e.getCause().getMessage());
                });
                return null;
            });
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

    public void setOnClose(Runnable runnable) {
        this.closeHook = runnable;
    }
}
