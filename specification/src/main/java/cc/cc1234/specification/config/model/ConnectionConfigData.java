package cc.cc1234.specification.config.model;

import lombok.Data;

@Data
public class ConnectionConfigData {

    private int maxRetries = 2;

    private int retryIntervalTime = 1000;

    /**
     * mill seconds
     */
    private int connectionTimeout = 5000;

    /**
     * mill seconds
     */
    private int sessionTimeout = 6000;

}
