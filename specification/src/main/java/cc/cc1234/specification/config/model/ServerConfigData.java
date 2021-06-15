package cc.cc1234.specification.config.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class ServerConfigData {

    /**
     * url = host:port
     */
    private String url;

    private String host;

    /**
     * compatible: v1.9.3 will be required
     */
    private Optional<Integer> port = Optional.empty();

    private String alias;

    private int connectTimes = 0;

    private List<String> aclList = new ArrayList<>();

    private Boolean sshTunnelEnabled = false;

    private Optional<SSHTunnelConfigData> sshTunnelConfig = Optional.empty();

}
