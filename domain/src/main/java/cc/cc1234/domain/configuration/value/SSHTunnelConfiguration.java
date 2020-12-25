package cc.cc1234.domain.configuration.value;

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

    private String remoteHost;

    private Integer remotePort;

}
