package cc.cc1234.spi.config.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 客户端核心配置
 */
public class ConfigData {

    private List<ServerConfigData> servers = new ArrayList<>();

    public List<ServerConfigData> getServers() {
        return servers;
    }

    public void setServers(List<ServerConfigData> servers) {
        this.servers = servers;
    }
}
