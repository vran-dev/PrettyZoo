package cc.cc1234.main.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

public class JsonUtils {

    public static <T> T from(String jsonFile, Class<T> clazz) {
        final ObjectMapper mapper = new ObjectMapper();
        mapper.configure(JsonParser.Feature.AUTO_CLOSE_SOURCE, true);
        try {
            final Path path = Paths.get(jsonFile);
            if (!Files.exists(path)) {
                Files.createFile(path);
                // TODO customize deserializer?
                Files.write(path, "{}".getBytes(), StandardOpenOption.WRITE);
            }
            return mapper.readValue(path.toFile(), clazz);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }


    public static <T> String to(T t) {
        final ObjectMapper mapper = new ObjectMapper();
        try {
            final String json = mapper.writeValueAsString(t);
            return json;
        } catch (JsonProcessingException e) {
            throw new IllegalStateException(e);
        }
    }
}
