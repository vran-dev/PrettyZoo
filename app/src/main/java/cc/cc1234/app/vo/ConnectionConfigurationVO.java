package cc.cc1234.app.vo;

import javafx.beans.property.SimpleIntegerProperty;

public class ConnectionConfigurationVO {

    private SimpleIntegerProperty maxRetries = new SimpleIntegerProperty(3);

    private SimpleIntegerProperty retryIntervalTime = new SimpleIntegerProperty(1000);

    private SimpleIntegerProperty connectionTimeout = new SimpleIntegerProperty(5000);

    private SimpleIntegerProperty sessionTimeout = new SimpleIntegerProperty(6000);

    public void unbind() {
        maxRetries.unbind();
        retryIntervalTime.unbind();
        connectionTimeout.unbind();
        sessionTimeout.unbind();
    }

    public int getMaxRetries() {
        return maxRetries.get();
    }

    public SimpleIntegerProperty maxRetriesProperty() {
        return maxRetries;
    }

    public void setMaxRetries(int maxRetries) {
        this.maxRetries.set(maxRetries);
    }

    public int getRetryIntervalTime() {
        return retryIntervalTime.get();
    }

    public SimpleIntegerProperty retryIntervalTimeProperty() {
        return retryIntervalTime;
    }

    public void setRetryIntervalTime(int retryIntervalTime) {
        this.retryIntervalTime.set(retryIntervalTime);
    }

    public int getConnectionTimeout() {
        return connectionTimeout.get();
    }

    public SimpleIntegerProperty connectionTimeoutProperty() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout.set(connectionTimeout);
    }

    public int getSessionTimeout() {
        return sessionTimeout.get();
    }

    public SimpleIntegerProperty sessionTimeoutProperty() {
        return sessionTimeout;
    }

    public void setSessionTimeout(int sessionTimeout) {
        this.sessionTimeout.set(sessionTimeout);
    }
}
