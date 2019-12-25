package cc.cc1234.listener;

import cc.cc1234.model.ZkNode;
import com.google.common.collect.Lists;

import java.util.List;
import java.util.function.Consumer;

public class TreeNodeListeners {

    private List<Consumer<ZkNode>> nodeUpdateListeners = Lists.newArrayList();

    private List<Consumer<ZkNode>> nodeAddListeners = Lists.newArrayList();

    private List<Consumer<ZkNode>> nodeDeleteListeners = Lists.newArrayList();


    public void addNodeUpdateListener(Consumer<ZkNode> listener) {
        this.nodeUpdateListeners.add(listener);
    }

    public void addNodeAddListener(Consumer<ZkNode> listener) {
        this.nodeAddListeners.add(listener);
    }

    public void addNodeDeleteListener(Consumer<ZkNode> listener) {
        this.nodeDeleteListeners.add(listener);
    }

    public void onNodeUpdate(ZkNode node) {
        this.nodeUpdateListeners.forEach(zkNodeConsumer -> zkNodeConsumer.accept(node));
    }

    public void onNodeDelete(ZkNode node) {
        this.nodeDeleteListeners.forEach(zkNodeConsumer -> zkNodeConsumer.accept(node));
    }

    public void onNodeAdd(ZkNode node) {
        this.nodeAddListeners.forEach(zkNodeConsumer -> zkNodeConsumer.accept(node));
    }
}
