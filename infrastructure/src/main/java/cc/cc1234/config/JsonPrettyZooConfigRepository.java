package cc.cc1234.config;

import cc.cc1234.spi.config.PrettyZooConfigRepository;
import cc.cc1234.spi.config.model.RootConfig;
import cc.cc1234.spi.config.model.ServerConfig;
import cc.cc1234.util.JsonUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class JsonPrettyZooConfigRepository implements PrettyZooConfigRepository {

    @Override
    public RootConfig get() {
        return doLoad();
    }

    private RootConfig doLoad() {
        final RootConfig config = JsonUtils.from(CONFIG_PATH, RootConfig.class);
        final List<ServerConfig> sortedServers = config.getServers()
                .stream()
                .sorted(Comparator.comparingInt(ServerConfig::getConnectTimes))
                .collect(Collectors.toList());
        // sort by connect times desc
        Collections.reverse(sortedServers);
        config.setServers(sortedServers);
        return config;
    }

    @Override
    public void save(RootConfig config) {
        try {
            final String json = JsonUtils.to(config);
            Files.write(Paths.get(CONFIG_PATH), json.getBytes());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
