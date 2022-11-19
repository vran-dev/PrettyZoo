package cc.cc1234.app.vo;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ConfigurationVO {

    private ObservableList<ServerConfigurationVO> servers = FXCollections.observableArrayList();

    private SimpleStringProperty theme = new SimpleStringProperty();

    public ObservableList<ServerConfigurationVO> getServers() {
        return servers;
    }

    public void setServers(ObservableList<ServerConfigurationVO> servers) {
        this.servers = servers;
    }

    public String getTheme() {
        return theme.get();
    }

    public SimpleStringProperty themeProperty() {
        return theme;
    }

    public void setTheme(String theme) {
        this.theme.set(theme);
    }
}
