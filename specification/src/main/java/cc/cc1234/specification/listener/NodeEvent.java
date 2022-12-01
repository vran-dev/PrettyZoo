package cc.cc1234.specification.listener;

import cc.cc1234.specification.node.ZkNode;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class NodeEvent {

    private ZkNode node;

    private String server;

    private String id;

}
