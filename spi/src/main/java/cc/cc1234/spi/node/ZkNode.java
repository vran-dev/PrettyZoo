package cc.cc1234.spi.node;

import org.apache.zookeeper.data.Stat;


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

    public void resetStat() {
        setStat(new Stat());
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public long getCzxid() {
        return czxid;
    }

    public void setCzxid(long czxid) {
        this.czxid = czxid;
    }

    public long getMzxid() {
        return mzxid;
    }

    public void setMzxid(long mzxid) {
        this.mzxid = mzxid;
    }

    public long getCtime() {
        return ctime;
    }

    public void setCtime(long ctime) {
        this.ctime = ctime;
    }

    public long getMtime() {
        return mtime;
    }

    public void setMtime(long mtime) {
        this.mtime = mtime;
    }

    public int getVersion() {
        return version;
    }

    public void setVersion(int version) {
        this.version = version;
    }

    public int getCversion() {
        return cversion;
    }

    public void setCversion(int cversion) {
        this.cversion = cversion;
    }

    public int getAversion() {
        return aversion;
    }

    public void setAversion(int aversion) {
        this.aversion = aversion;
    }

    public long getEphemeralOwner() {
        return ephemeralOwner;
    }

    public void setEphemeralOwner(long ephemeralOwner) {
        this.ephemeralOwner = ephemeralOwner;
    }

    public int getDataLength() {
        return dataLength;
    }

    public void setDataLength(int dataLength) {
        this.dataLength = dataLength;
    }

    public int getNumChildren() {
        return numChildren;
    }

    public void setNumChildren(int numChildren) {
        this.numChildren = numChildren;
    }

    public long getPzxid() {
        return pzxid;
    }

    public void setPzxid(long pzxid) {
        this.pzxid = pzxid;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public byte[] getDataBytes() {
        return dataBytes;
    }

    public void setDataBytes(byte[] dataBytes) {
        this.dataBytes = dataBytes;
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
