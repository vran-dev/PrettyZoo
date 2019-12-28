package cc.cc1234.spi.listener;

import cc.cc1234.spi.node.ZkNode;

public class NodeEvent {

    private ZkNode node;

    private String server;

    public NodeEvent(ZkNode node, String server) {
        this.node = node;
        this.server = server;
    }

    public ZkNode getNode() {
        return node;
    }

    public void setNode(ZkNode node) {
        this.node = node;
    }

    public String getServer() {
        return server;
    }

    public void setServer(String server) {
        this.server = server;
    }
}
