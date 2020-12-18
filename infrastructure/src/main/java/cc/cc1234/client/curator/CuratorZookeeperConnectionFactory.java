package cc.cc1234.client.curator;

import cc.cc1234.spi.config.model.ServerConfig;
import cc.cc1234.spi.connection.ZookeeperConnection;
import cc.cc1234.spi.connection.ZookeeperConnectionFactory;
import org.apache.curator.framework.AuthInfo;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.retry.RetryOneTime;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.stream.Collectors;

public class CuratorZookeeperConnectionFactory implements ZookeeperConnectionFactory<CuratorFramework> {

    private static final Logger log = LoggerFactory.getLogger(CuratorZookeeperConnectionFactory.class);

    @Override
    public ZookeeperConnection<CuratorFramework> create(ServerConfig config) throws Exception {
        final RetryOneTime retryPolicy = new RetryOneTime(3000);
        final CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(config.getHost())
                .retryPolicy(retryPolicy);

        if (!config.getAclList().isEmpty()) {
            final List<AuthInfo> acls = config.getAclList().stream().map(ACLs::parseDigest).collect(Collectors.toList());
            builder.authorization(acls)
                    .aclProvider(new ACLProvider() {
                        @Override
                        public List<ACL> getDefaultAcl() {
                            return ZooDefs.Ids.CREATOR_ALL_ACL;
                        }

                        @Override
                        public List<ACL> getAclForPath(String path) {
                            return ZooDefs.Ids.CREATOR_ALL_ACL;
                        }
                    });
        }

        final CuratorFramework client = builder.build();
        client.start();

        // TODO use async
        if (!client.blockUntilConnected(5, TimeUnit.SECONDS)) {
            client.close();
            throw new TimeoutException("连接超时");
        }
        return new CuratorZookeeperConnection(client);
    }
}
