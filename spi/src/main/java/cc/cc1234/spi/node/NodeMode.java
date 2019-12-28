package cc.cc1234.spi.node;

import org.apache.zookeeper.CreateMode;

public enum NodeMode {

    PERSISTENT,

    PERSISTENT_SEQUENTIAL,

    EPHEMERAL,

    EPHEMERAL_SEQUENTIAL;

    public CreateMode createMode() {
        return CreateMode.valueOf(this.name());
    }
}
