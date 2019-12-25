package cc.cc1234.model;

import lombok.Data;
import org.apache.zookeeper.data.Stat;


@Data
public class ZkNode {

    /**
     * 节点名称
     */
    private String  name ;

    /**
     * path
     */
    private String path ;

    private long czxid;
    private long mzxid;
    private long ctime;
    private long mtime;
    private int version ;
    private int cversion ;
    private int aversion ;
    private long ephemeralOwner;
    private int dataLength ;
    private int numChildren ;
    private long pzxid;

    private String data;

    public ZkNode() {
    }

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

}
