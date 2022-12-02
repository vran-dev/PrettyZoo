package cc.cc1234.specification.config.model;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class ServerConfigData {

    /**
     * nullable
     */
    private String id;

    private String host;

    private Integer port;

    private String alias;

    private int connectTimes = 0;

    private List<String> aclList = new ArrayList<>();

    private Boolean sshTunnelEnabled = false;

    private Boolean enableConnectionAdvanceConfiguration = false;

    private Optional<SSHTunnelConfigData> sshTunnelConfig = Optional.empty();

    private ConnectionConfigData connectionConfig = new ConnectionConfigData();

}
