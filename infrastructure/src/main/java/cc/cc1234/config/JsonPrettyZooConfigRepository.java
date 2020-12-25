package cc.cc1234.config;

import cc.cc1234.spi.config.PrettyZooConfigRepository;
import cc.cc1234.spi.config.model.ConfigData;
import cc.cc1234.spi.config.model.ServerConfigData;
import cc.cc1234.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class JsonPrettyZooConfigRepository implements PrettyZooConfigRepository {

    private static final Logger logger = LoggerFactory.getLogger(JsonPrettyZooConfigRepository.class);

    @Override
    public ConfigData get() {
        return doLoad();
    }

    private ConfigData doLoad() {
        final ConfigData config = JsonUtils.from(CONFIG_PATH, ConfigData.class);
        final List<ServerConfigData> sortedServers = config.getServers()
                .stream()
                .sorted(Comparator.comparingInt(ServerConfigData::getConnectTimes))
                .collect(Collectors.toList());
        // sort by connect times desc
        Collections.reverse(sortedServers);
        config.setServers(sortedServers);
        return config;
    }

    @Override
    public void save(ConfigData config) {
        try {
            final String json = JsonUtils.to(config);
            Files.write(Paths.get(CONFIG_PATH), json.getBytes());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    @Override
    public void importConfig(InputStream stream) {
        var reader = new BufferedReader(new InputStreamReader(stream));
        StringBuilder builder = new StringBuilder();
        while (true) {
            try {
                String s = null;
                if ((s = reader.readLine()) != null) {
                    builder.append(s);
                } else {
                    break;
                }
            } catch (IOException e) {
                logger.error("import config failed", e);
                throw new RuntimeException("import config failed, cause " + e.getMessage());
            }
        }
        String jsonConfig = builder.toString();
        ConfigData newConfig = JsonUtils.fromJson(jsonConfig, ConfigData.class);
        merge(get(), newConfig);
    }

    private void merge(ConfigData originConfig, ConfigData newConfig) {
        // ignore exists server
        Set<String> originServers = originConfig.getServers()
                .stream()
                .map(ServerConfigData::getHost)
                .collect(Collectors.toSet());
        List<ServerConfigData> newServers = newConfig.getServers()
                .stream()
                .filter(server -> !originServers.contains(server.getHost()))
                .collect(Collectors.toList());

        // add new Server
        originConfig.getServers().addAll(newServers);

        // serialize
        save(originConfig);
    }

    @Override
    public void exportConfig(OutputStream targetStream) {
        try {
            final ConfigData config = get();
            final String json = JsonUtils.to(config);
            targetStream.write(json.getBytes());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
