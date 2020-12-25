package cc.cc1234.spi.connection;

import java.util.List;

public class ZookeeperParams {

    private String host;

    private List<String> aclList;

    public ZookeeperParams(String host, List<String> aclList) {
        this.host = host;
        this.aclList = aclList;
    }

    public String getHost() {
        return host;
    }

    public List<String> getAclList() {
        return aclList;
    }
}
