package cc.cc1234.version;

import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.function.Consumer;

public class VersionChecker {

    private static final Logger logger = LoggerFactory.getLogger(VersionChecker.class);

    public static void hasNewVersion(Consumer<String> runnable) {
        var request = HttpRequest.newBuilder(URI.create("https://api.github.com/repos/vran-dev/PrettyZoo/releases/latest"))
                .build();
        var client = HttpClient.newHttpClient();
        client.sendAsync(request, HttpResponse.BodyHandlers.ofString())
                .thenAccept(response -> {
                    try {
                        final JsonMapper mapper = new JsonMapper();
                        final ObjectNode node = mapper.readValue(response.body(), ObjectNode.class);
                        final String latestVersion = node.get("tag_name").asText();
                        compareAndRun(latestVersion, runnable);
                    } catch (Exception exception) {
                        logger.error("check update failed", exception);
                    }
                });
    }

    private static void compareAndRun(String latestVersion, Consumer<String> runnable) {
        if (isLargerThanCurrent(latestVersion)) {
            Platform.runLater(() -> runnable.accept(latestVersion));
        }
    }

    private static Boolean isLargerThanCurrent(String remoteVersion) {
        final String[] arr = remoteVersion.split("v");
        String r = remoteVersion;
        if (arr.length == 2) {
            r = arr[1];
        }

        final String[] localVersionArr = Version.VERSION.split("\\.");
        final String[] remoteVersionArr = r.split("\\.");
        for (int i = 0; i < localVersionArr.length; i++) {
            try {
                final int localVersionSymbol = Integer.parseInt(localVersionArr[i]);
                final int remoteVersionSymbol = Integer.parseInt(remoteVersionArr[i]);
                if (localVersionSymbol < remoteVersionSymbol) {
                    return true;
                } else if (localVersionSymbol > remoteVersionSymbol) {
                    return false;
                }
            } catch (Exception e) {
                return true;
            }
        }
        return false;
    }
}
