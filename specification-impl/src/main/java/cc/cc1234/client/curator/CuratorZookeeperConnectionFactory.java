package cc.cc1234.client.curator;

import cc.cc1234.specification.connection.ZookeeperConnection;
import cc.cc1234.specification.connection.ZookeeperConnectionFactory;
import cc.cc1234.specification.connection.ZookeeperParams;
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
import java.util.stream.Collectors;

public class CuratorZookeeperConnectionFactory implements ZookeeperConnectionFactory<CuratorFramework> {

    private static final Logger log = LoggerFactory.getLogger(CuratorZookeeperConnectionFactory.class);

    @Override
    public ZookeeperConnection<CuratorFramework> create(ZookeeperParams params) {
        final RetryOneTime retryPolicy = new RetryOneTime(3000);
        final CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder()
                .connectString(params.getHost())
                .retryPolicy(retryPolicy);

        if (!params.getAclList().isEmpty()) {
            final List<AuthInfo> acls = params.getAclList().stream().map(ACLs::parseDigest).collect(Collectors.toList());
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
        try {
            if (!client.blockUntilConnected(5, TimeUnit.SECONDS)) {
                client.close();
                throw new IllegalStateException("连接超时");
            }
        } catch (InterruptedException e) {
            throw new IllegalStateException("连接失败", e);
        }
        return new CuratorZookeeperConnection(client);
    }
}
