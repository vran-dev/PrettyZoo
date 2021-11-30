package cc.cc1234.specification.node;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.zookeeper.data.Stat;

@Data
@NoArgsConstructor
public class ZkNode {

    /**
     * 节点名称
     */
    private String name;

    /**
     * path
     */
    private String path;

    private long czxid;

    private long mzxid;

    private long ctime;

    private long mtime;

    private int version;

    private int cversion;

    private int aversion;

    private long ephemeralOwner;

    private int dataLength;

    private int numChildren;

    private long pzxid;

    private String data;

    private byte[] dataBytes;

    public ZkNode(String name, String path) {
        setName(name);
        setPath(path);
    }

    public void setStat(Stat stat) {
        setCzxid(stat.getCzxid());
        setMzxid(stat.getMzxid());
        setCtime(stat.getCtime());
        setMtime(stat.getMtime());
        setVersion(stat.getVersion());
        setCversion(stat.getCversion());
        setAversion(stat.getAversion());
        setEphemeralOwner(stat.getEphemeralOwner());
        setDataLength(stat.getDataLength());
        setNumChildren(stat.getNumChildren());
        setPzxid(stat.getPzxid());
    }

    public void resetStat() {
        setStat(new Stat());
    }

    public void copyField(ZkNode node) {
        setName(node.getName());
        setPath(node.getPath());
        setCzxid(node.getCzxid());
        setMzxid(node.getMzxid());
        setCtime(node.getCtime());
        setMtime(node.getMtime());
        setVersion(node.getVersion());
        setCversion(node.getCversion());
        setAversion(node.getAversion());
        setEphemeralOwner(node.getEphemeralOwner());
        setDataLength(node.getDataLength());
        setNumChildren(node.getNumChildren());
        setPzxid(node.getPzxid());
        setData(node.getData());
        setDataBytes(node.getDataBytes());
    }
}
