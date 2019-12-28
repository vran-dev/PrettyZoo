package cc.cc1234.spi.config.model;

import java.util.ArrayList;
import java.util.List;

/**
 *  客户端核心配置
 */
public class RootConfig {

    private List<ServerConfig> servers = new ArrayList<>();

    public List<ServerConfig> getServers() {
        return servers;
    }

    public void setServers(List<ServerConfig> servers) {
        this.servers = servers;
    }
}
