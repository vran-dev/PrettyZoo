package cc.cc1234.main.model;

import cc.cc1234.main.util.Configs;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class PrettyZooConfig {

    private ObservableList<ZkServer> servers = FXCollections.observableArrayList();

    public void save(String server) {
        servers.stream()
                .filter(z -> z.getServer().equals(server))
                .findFirst()
                .map(z -> {
                    z.setConnectTimes(z.getConnectTimes() + 1);
                    return true;
                })
                .orElseGet(() -> {
                    servers.add(new ZkServer(server));
                    return true;
                });
        Platform.runLater(() -> Configs.store(this));
    }

    public boolean contains(String server) {
        return servers.stream().anyMatch(z -> z.getServer().equals(server));
    }

    public void remove(String server) {
        servers.stream()
                .filter(z -> z.getServer().equals(server))
                .forEach(zk -> servers.remove(zk));
        Platform.runLater(() -> Configs.store(this));
    }

    public ObservableList<ZkServer> getServers() {
        return servers;
    }

    public void setServers(ObservableList<ZkServer> servers) {
        this.servers = servers;
    }
}
