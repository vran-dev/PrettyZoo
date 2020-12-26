package cc.cc1234.domain.zookeeper.entity;

import cc.cc1234.spi.connection.ZookeeperConnection;
import cc.cc1234.zookeeper.ZooKeeperMain;
import lombok.Getter;
import org.apache.zookeeper.KeeperException;

import java.io.IOException;

public class Terminal {

    @Getter
    private String host;

    private ZookeeperConnection connection;

    private ZooKeeperMain zooKeeperMain;

    public Terminal(String host,
                    ZookeeperConnection connection,
                    ZooKeeperMain zooKeeperMain) {
        this.host = host;
        this.connection = connection;
        this.zooKeeperMain = zooKeeperMain;
    }

    public void close() {
        connection.close();
    }

    public void executeLine(String command) throws InterruptedException, IOException, KeeperException {
        zooKeeperMain.executeLine(command);
    }
}
