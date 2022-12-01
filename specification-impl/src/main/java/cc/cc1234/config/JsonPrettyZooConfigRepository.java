package cc.cc1234.config;

import cc.cc1234.specification.config.PrettyZooConfigRepository;
import cc.cc1234.specification.config.model.ConfigData;
import cc.cc1234.specification.config.model.ServerConfigData;
import cc.cc1234.util.JsonUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class JsonPrettyZooConfigRepository implements PrettyZooConfigRepository {

    private static final Logger logger = LoggerFactory.getLogger(JsonPrettyZooConfigRepository.class);

    @Override
    public ConfigData get() {
        return doLoad();
    }

    private ConfigData doLoad() {
        migrateIfNecessary();
        final ConfigData config = JsonUtils.from(CONFIG_PATH, ConfigData.class);
        final List<ServerConfigData> sortedServers = config.getServers()
                .stream()
                .filter(serverConfigData -> serverConfigData.getConnectTimes() > 0)
                .sorted(Comparator.comparingInt(ServerConfigData::getConnectTimes))
                .collect(Collectors.toList());
        // sort by connect times desc
        Collections.reverse(sortedServers);
        List<ServerConfigData> unConnectServers = config.getServers()
                .stream()
                .filter(serverConfigData -> serverConfigData.getConnectTimes() == 0)
                .collect(Collectors.toList());

        List<ServerConfigData> servers = new ArrayList<>();
        servers.addAll(sortedServers);
        servers.addAll(unConnectServers);
        servers.forEach(s -> {
            if (s.getId() == null) {
                s.setId(UUID.randomUUID().toString());
            }
        });
        config.setServers(servers);
        return config;
    }

    /**
     * compatibility function
     * since: v1.5.0
     */
    private void migrateIfNecessary() {
        if (!Files.exists(Paths.get(CONFIG_PATH)) && Files.exists(Paths.get(OLD_CONFIG_PATH))) {
            var config = JsonUtils.from(OLD_CONFIG_PATH, ConfigData.class);
            save(config);
        }
    }

    @Override
    public void save(ConfigData config) {
        try {
            final String json = JsonUtils.to(config);
            Files.write(Paths.get(CONFIG_PATH), json.getBytes(StandardCharsets.UTF_8));
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
        // add new Server
        originConfig.getServers().addAll(newConfig.getServers());
        originConfig.getServers().forEach(s -> {
            if (s.getId() == null) {
                s.setId(UUID.randomUUID().toString());
            }
        });

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
