package cc.cc1234.app.controller;

import cc.cc1234.app.util.Asserts;
import cc.cc1234.app.facade.PrettyZooFacade;
import cc.cc1234.app.fp.Try;
import cc.cc1234.app.util.FXMLs;
import cc.cc1234.app.view.transitions.Transitions;
import cc.cc1234.app.view.toast.VToast;
import cc.cc1234.app.vo.ServerConfigurationVO;
import cc.cc1234.spi.listener.ServerListener;
import com.google.common.base.Strings;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ServerViewController {

    @FXML
    private AnchorPane serverInfoPane;

    @FXML
    private CheckBox sshTunnelCheckbox;

    @FXML
    private AnchorPane sshTunnelPane;

    @FXML
    private ProgressBar sshTunnelProgressBarTo;

    @FXML
    private ProgressBar sshTunnelProgressBarFrom;

    @FXML
    private TextArea aclTextArea;

    @FXML
    private TextField zkServer;

    @FXML
    private TextField sshServer;

    @FXML
    private TextField sshUsername;

    @FXML
    private TextField sshPassword;

    @FXML
    private TextField remoteServer;

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

    private NodeViewController nodeViewController = FXMLs.getController("fxml/NodeListView.fxml");

    public void show(StackPane stackPane) {
        show(stackPane, null);
    }

    public void show(StackPane stackPane, ServerConfigurationVO config) {
        if (config == null) {
            showNewServerView(stackPane);
        } else if (config.isConnected()) {
            showNodeListView(stackPane, config);
        } else {
            showServerInfoView(stackPane, config);
        }
    }

    private void showNewServerView(StackPane stackPane) {
        zkServer.setEditable(true);
        buttonHBox.getChildren().remove(deleteButton);
        buttonHBox.getChildren().remove(connectButton);
        propertyReset();
        switchIfNecessary(stackPane);
    }

    private void showServerInfoView(StackPane stackPane, ServerConfigurationVO config) {
        zkServer.setEditable(false);
        connectButton.setOnMouseClicked(e -> onConnect(stackPane, config));
        propertyBind(config);
        showConnectAndSaveButton();
        switchIfNecessary(stackPane);
    }

    private void switchIfNecessary(StackPane stackPane) {
        if (stackPane.getChildren().contains(serverInfoPane)) {
            Transitions.zoomInY(serverInfoPane).playFromStart();
        } else {
            nodeViewController.hideAndThen(() -> {
                stackPane.getChildren().add(serverInfoPane);
                Transitions.zoomInY(serverInfoPane).playFromStart();
            });
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
        sshServer.textProperty().setValue("");
        sshUsername.textProperty().setValue("");
        sshPassword.textProperty().setValue("");
        remoteServer.textProperty().setValue("");
        aclTextArea.textProperty().setValue("");
    }

    private void propertyBind(ServerConfigurationVO config) {
        zkServer.textProperty().setValue(config.getZkServer());
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
            Transitions.zoomOut(serverInfoPane, e -> parent.getChildren().remove(serverInfoPane)).playFromStart();
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
        Try.of(() -> {
            Asserts.matchHost(zkServer.textProperty().get(), "server must match pattern: [host:port]");
            Asserts.validAcl(aclTextArea.textProperty().get(), "ACL syntax not support");
            if (zkServer.isEditable()) {
                if (prettyZooFacade.hasServerConfig(zkServer.getText())) {
                    throw new IllegalStateException(zkServer.getText() + " already exists");
                }
            }
            if (sshTunnelCheckbox.isSelected()) {
                Asserts.matchHost(remoteServer.textProperty().get(), "remoteServer must match pattern: [host:port]");
                Asserts.notNull(sshUsername.textProperty().get(), "sshUsername cannot be null");
                Asserts.notNull(sshPassword.textProperty().get(), "sshPassword cannot be null");
                Asserts.matchHost(sshServer.textProperty().get(), "sshServer must match pattern: [host:port]");
            }
        }).onSuccess(obj -> {
            var serverConfigVO = new ServerConfigurationVO();
            serverConfigVO.setZkServer(zkServer.textProperty().get());
            serverConfigVO.setRemoteServer(remoteServer.textProperty().get());
            serverConfigVO.setSshUsername(sshUsername.textProperty().get());
            serverConfigVO.setSshPassword(sshPassword.textProperty().get());
            serverConfigVO.setSshServer(sshServer.textProperty().get());
            if (sshTunnelCheckbox.isSelected()) {
                serverConfigVO.setSshEnabled(true);
            }
            List<String> acls = Arrays.stream(aclTextArea.textProperty().get().split("\n"))
                    .filter(acl -> !Strings.isNullOrEmpty(acl))
                    .collect(Collectors.toList());
            serverConfigVO.getAclList().addAll(acls);
            prettyZooFacade.saveConfig(serverConfigVO);
            if (zkServer.isEditable()) {
                onClose();
            }
            VToast.info("save success");
        }).onFailure(ex -> VToast.error(ex.getMessage()));
    }

    private void onDelete() {
        Asserts.notBlank(zkServer.getText(), "server can not be null");
        prettyZooFacade.removeConfig(zkServer.getText());
        if (prettyZooFacade.loadConfigs(null).isEmpty()) {
            onClose();
        }
    }

    private void onConnect(StackPane parent, ServerConfigurationVO serverConfigurationVO) {
        Try.of(() -> {
            Asserts.notNull(serverConfigurationVO, "save config first");
            Asserts.assertTrue(prettyZooFacade.hasServerConfig(serverConfigurationVO.getZkServer()), "save config first");
            nodeViewController.show(parent, serverConfigurationVO.getZkServer(), new ServerListener() {
                @Override
                public void onClose(String serverHost) {
                    if (serverHost.equals(serverConfigurationVO.getZkServer())) {
                        serverConfigurationVO.setConnected(false);
                    }
                }
            });
            parent.getChildren().remove(serverInfoPane);
            serverConfigurationVO.setConnected(true);
        }).onFailure(e -> VToast.error(e.getMessage()));
    }
}
