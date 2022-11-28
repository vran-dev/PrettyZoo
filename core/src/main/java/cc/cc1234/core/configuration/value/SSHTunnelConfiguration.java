package cc.cc1234.core.configuration.value;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class SSHTunnelConfiguration {

    private String localhost;

    private Integer localPort;

    private String sshHost;

    private Integer sshPort;

    private String sshUsername;

    private String sshPassword;

    private String sshKeyFilePath;

    private String remoteHost;

    private Integer remotePort;

}
