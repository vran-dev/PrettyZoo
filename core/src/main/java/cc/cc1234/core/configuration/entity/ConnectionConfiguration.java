package cc.cc1234.core.configuration.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ConnectionConfiguration {

    @Builder.Default
    private int maxRetries = 2;

    @Builder.Default
    private int retryIntervalTime = 1000;

    /**
     * mill seconds
     */
    @Builder.Default
    private int connectionTimeout = 5000;

    /**
     * mill seconds
     */
    @Builder.Default
    private int sessionTimeout = 6000;

    public void update(ConnectionConfiguration config) {
        this.maxRetries = config.getMaxRetries();
        this.retryIntervalTime = config.getRetryIntervalTime();
        this.connectionTimeout = config.getConnectionTimeout();
        this.sessionTimeout = config.getSessionTimeout();
    }
}
