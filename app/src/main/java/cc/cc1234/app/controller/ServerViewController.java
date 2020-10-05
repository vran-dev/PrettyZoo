package cc.cc1234.app.controller;

import cc.cc1234.app.checker.Checkers;
import cc.cc1234.app.facade.PrettyZooFacade;
import cc.cc1234.app.util.FXMLs;
import cc.cc1234.app.util.Transitions;
import cc.cc1234.app.util.VToast;
import cc.cc1234.app.vo.ServerConfigVO;
import com.google.common.base.Strings;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
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

    public void show(StackPane stackPane, ServerConfigVO config) {
        if (config == null) {
            propertyReset();
            zkServer.setEditable(true);
            if (buttonHBox.getChildren().contains(deleteButton)) {
                buttonHBox.getChildren().remove(deleteButton);
            }
        } else {
            propertyBind(config);
            zkServer.setEditable(false);
            if (!buttonHBox.getChildren().contains(deleteButton)) {
                buttonHBox.getChildren().add(deleteButton);
            }

            if (config.isConnected()) {
                onConnect(stackPane, config);
                return;
            }
        }

        if (stackPane.getChildren().contains(serverInfoPane)) {
//            var scaleTransition = Transitions.scale(serverInfoPane, Duration.millis(500), 1.05, 1.05, 2, true);
//            scaleTransition.setFromX(1);
//            scaleTransition.setFromY(1);
//            scaleTransition.playFromStart();
            Transitions.zoomInY(serverInfoPane).playFromStart();
        } else {
            nodeViewController.hideAndThen(() -> {
                stackPane.setPadding(new Insets(30, 30, 30, 30));
                stackPane.getChildren().add(serverInfoPane);
                Transitions.zoomInY(serverInfoPane).playFromStart();
            });
        }
        connectButton.setOnMouseClicked(e -> onConnect(stackPane, config));
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

    private void propertyBind(ServerConfigVO config) {
        zkServer.textProperty().setValue(config.getZkServer());
        sshServer.textProperty().setValue(config.getSshServer());
        sshUsername.textProperty().setValue(config.getSshUsername());
        sshPassword.textProperty().setValue(config.getSshPassword());
        remoteServer.textProperty().setValue(config.getRemoteServer());
        sshTunnelCheckbox.selectedProperty().setValue(config.isSshEnabled());

        final String acl = String.join("\n", config.getAclList());
        aclTextArea.textProperty().setValue(acl);
    }


    public void hide() {
        final StackPane parent = (StackPane) serverInfoPane.getParent();
        if (parent != null) {
            Transitions
                    .zoomOut(serverInfoPane, e -> {
                        parent.getChildren().remove(serverInfoPane);
                    })
                    .playFromStart();
        }
    }

    @FXML
    private void initialize() {
        sshTunnelViewPropertyBind();
        closeButton.setOnMouseClicked(e -> hide());
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
        if (Checkers.isHostNotMatch(zkServer.textProperty().get())) {
            VToast.error("server must match pattern: [host:port]");
            return;
        }
        if (zkServer.isEditable()) {
            if (prettyZooFacade.hasServerConfig(zkServer.getText())) {
                VToast.error(zkServer.getText() + " already exists");
                return;
            }
        }


        if (sshTunnelCheckbox.isSelected()) {
            if (Checkers.isHostNotMatch(remoteServer.getText())) {
                VToast.error("remoteServer must match pattern: [host:port]");
                return;
            }
            if (Checkers.isNull(sshUsername.textProperty().get())) {
                VToast.error("sshUsername cannot be null");
                return;
            }
            if (Checkers.isNull(sshPassword.textProperty().get())) {
                VToast.error("sshPassword cannot be null");
                return;
            }

            if (Checkers.isHostNotMatch(sshServer.getText())) {
                VToast.error("sshServer must match pattern: [host:port]");
                return;
            }
        }


        var serverConfigVO = new ServerConfigVO();
        serverConfigVO.setZkServer(zkServer.textProperty().get());
        // TODO acl List
        if (sshTunnelCheckbox.isSelected()) {
            serverConfigVO.setSshEnabled(true);
        }
        serverConfigVO.setRemoteServer(remoteServer.getText());
        serverConfigVO.setSshUsername(sshUsername.getText());
        serverConfigVO.setSshPassword(sshPassword.getText());
        serverConfigVO.setSshServer(sshServer.getText());

        if (Checkers.aclIsInvalid(aclTextArea.getText())) {
            VToast.error("ACL syntax not support");
            return;
        } else {
            final List<String> acls = Arrays.stream(aclTextArea.getText().split("\n"))
                    .filter(acl -> !Strings.isNullOrEmpty(acl))
                    .collect(Collectors.toList());
            serverConfigVO.getAclList().addAll(acls);
        }
        prettyZooFacade.saveConfig(serverConfigVO);
    }

    private void onDelete() {
        Checkers.ifBlank(zkServer.getText(), () -> {
            throw new RuntimeException();
        });
        prettyZooFacade.removeConfig(zkServer.getText());
        hide();
    }

    private void onConnect(StackPane parent, ServerConfigVO serverConfigVO) {
        if (serverConfigVO == null || !prettyZooFacade.hasServerConfig(serverConfigVO.getZkServer())) {
            VToast.error("save config first");
            return;
        }

        Platform.runLater(() -> {
            try {
                nodeViewController.show(parent, serverConfigVO.getZkServer());
                parent.getChildren().remove(serverInfoPane);
                serverConfigVO.setConnected(true);
            } catch (Exception e) {
                VToast.error("连接 " + serverConfigVO.getZkServer() + " 失败");
            }
        });
    }
}
