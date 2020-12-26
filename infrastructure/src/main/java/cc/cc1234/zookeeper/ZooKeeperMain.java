/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package cc.cc1234.zookeeper;

import cc.cc1234.spi.util.StringWriter;
import org.apache.zookeeper.*;
import org.apache.zookeeper.AsyncCallback.DataCallback;
import org.apache.zookeeper.ZooDefs.Ids;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * The command line client to ZooKeeper.
 */
public class ZooKeeperMain {
    private static final Logger LOG = LoggerFactory.getLogger(ZooKeeperMain.class);
    protected static final Map<String, String> commandMap = new HashMap<String, String>();

    protected MyCommandOptions cl;
    protected HashMap<Integer, String> history = new HashMap<Integer, String>();
    protected int commandCount = 0;
    protected boolean printWatches = true;

    private StringWriter outputStream;

    protected ZooKeeper zk;
    protected String host = "";

    public boolean getPrintWatches() {
        return printWatches;
    }

    static {
//        commandMap.put("connect", "host:port");
//        commandMap.put("close", "");
//        commandMap.put("quit", "");

        commandMap.put("create", "[-s] [-e] path data acl");
        commandMap.put("delete", "path [version]");
        commandMap.put("rmr", "path");
        commandMap.put("set", "path data [version]");
        commandMap.put("get", "path [watch]");
        commandMap.put("ls", "path [watch]");
        commandMap.put("ls2", "path [watch]");
        commandMap.put("getAcl", "path");
        commandMap.put("setAcl", "path acl");
        commandMap.put("stat", "path [watch]");
        commandMap.put("sync", "path");
        commandMap.put("setquota", "-n|-b val path");
        commandMap.put("listquota", "path");
        commandMap.put("delquota", "[-n|-b] path");
        commandMap.put("history", "");
        commandMap.put("redo", "cmdno");
        commandMap.put("printwatches", "on|off");
        commandMap.put("addauth", "scheme auth");
    }

    void usage() {
        try {
            outputStream.write("ZooKeeper -server host:port cmd args\r\n".getBytes());
            for (String cmd : commandMap.keySet()) {
                outputStream.write(("\t" + cmd + " " + commandMap.get(cmd) + "\r\n").getBytes());
            }
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private class MyWatcher implements Watcher {
        public void process(WatchedEvent event) {
            if (getPrintWatches()) {
                ZooKeeperMain.printMessage("WATCHER::");
                ZooKeeperMain.printMessage(event.toString());
            }
        }
    }

    private int getPermFromString(String permString) throws IOException {
        int perm = 0;
        for (int i = 0; i < permString.length(); i++) {
            switch (permString.charAt(i)) {
                case 'r':
                    perm |= ZooDefs.Perms.READ;
                    break;
                case 'w':
                    perm |= ZooDefs.Perms.WRITE;
                    break;
                case 'c':
                    perm |= ZooDefs.Perms.CREATE;
                    break;
                case 'd':
                    perm |= ZooDefs.Perms.DELETE;
                    break;
                case 'a':
                    perm |= ZooDefs.Perms.ADMIN;
                    break;
                default:
                    outputStream.write(("Unknown perm type: " + permString.charAt(i)).getBytes());
            }
        }
        return perm;
    }

    private void printStat(Stat stat, StringWriter stream) {
        try {
            stream.write(("cZxid = 0x" + Long.toHexString(stat.getCzxid()) + "\r\n").getBytes());
            stream.write(("ctime = " + new Date(stat.getCtime()).toString() + "\r\n").getBytes());
            stream.write(("mZxid = 0x" + Long.toHexString(stat.getMzxid()) + "\r\n").getBytes());
            stream.write(("mtime = " + new Date(stat.getMtime()).toString() + "\r\n").getBytes());
            stream.write(("pZxid = 0x" + Long.toHexString(stat.getPzxid()) + "\r\n").getBytes());
            stream.write(("cversion = " + stat.getCversion() + "\r\n").getBytes());
            stream.write(("dataVersion = " + stat.getVersion() + "\r\n").getBytes());
            stream.write(("aclVersion = " + stat.getAversion() + "\r\n").getBytes());
            stream.write(("ephemeralOwner = 0x" + Long.toHexString(stat.getEphemeralOwner()) + "\r\n").getBytes());
            stream.write(("dataLength = " + stat.getDataLength() + "\r\n").getBytes());
            stream.write(("numChildren = " + stat.getNumChildren() + "\r\n").getBytes());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * A storage class for both command line options and shell commands.
     */
    static class MyCommandOptions {

        private Map<String, String> options = new HashMap<String, String>();
        private List<String> cmdArgs = null;
        private String command = null;
        private StringWriter outputStream;

        public MyCommandOptions(StringWriter outputStream) {
            options.put("server", "localhost:2181");
            options.put("timeout", "30000");
            this.outputStream = outputStream;
        }

        public String getOption(String opt) {
            return options.get(opt);
        }

        public String getCommand() {
            return command;
        }

        public String getCmdArgument(int index) {
            return cmdArgs.get(index);
        }

        public int getNumArguments() {
            return cmdArgs.size();
        }

        public String[] getArgArray() {
            return cmdArgs.toArray(new String[0]);
        }

        /**
         * Parses a command line that may contain one or more flags
         * before an optional command string
         *
         * @param args command line arguments
         * @return true if parsing succeeded, false otherwise.
         */
        public boolean parseOptions(String[] args) {
            List<String> argList = Arrays.asList(args);
            Iterator<String> it = argList.iterator();

            while (it.hasNext()) {
                String opt = it.next();
                try {
                    if (opt.equals("-server")) {
                        options.put("server", it.next());
                    } else if (opt.equals("-timeout")) {
                        options.put("timeout", it.next());
                    } else if (opt.equals("-r")) {
                        options.put("readonly", "true");
                    }
                } catch (NoSuchElementException e) {
                    try {
                        outputStream.write(("Error: no argument found for option " + opt).getBytes());
                    } catch (IOException ioException) {
                        throw new IllegalStateException(ioException);
                    }
                    return false;
                }

                if (!opt.startsWith("-")) {
                    command = opt;
                    cmdArgs = new ArrayList<String>();
                    cmdArgs.add(command);
                    while (it.hasNext()) {
                        cmdArgs.add(it.next());
                    }
                    return true;
                }
            }
            return true;
        }

        /**
         * Breaks a string into command + arguments.
         *
         * @param cmdstring string of form "cmd arg1 arg2..etc"
         * @return true if parsing succeeded.
         */
        public boolean parseCommand(String cmdstring) {
            StringTokenizer cmdTokens = new StringTokenizer(cmdstring, " ");
            String[] args = new String[cmdTokens.countTokens()];
            int tokenIndex = 0;
            while (cmdTokens.hasMoreTokens()) {
                args[tokenIndex] = cmdTokens.nextToken();
                tokenIndex++;
            }
            if (args.length == 0) {
                return false;
            }
            command = args[0];
            cmdArgs = Arrays.asList(args);
            return true;
        }
    }


    /**
     * Makes a list of possible completions, either for commands
     * or for zk nodes if the token to complete begins with /
     */
    protected void addToHistory(int i, String cmd) {
        history.put(i, cmd);
    }

    public static List<String> getCommands() {
        return new LinkedList<String>(commandMap.keySet());
    }

    protected String getPrompt() {
        return "[zk: " + host + "(" + zk.getState() + ")" + " " + commandCount + "] ";
    }

    public static void printMessage(String msg) {
        System.out.println("\n" + msg);
    }

    protected void connectToZK(String newHost) throws InterruptedException, IOException {
        if (zk != null && zk.getState().isAlive()) {
            zk.close();
        }
        host = newHost;
        boolean readOnly = cl.getOption("readonly") != null;
        zk = new ZooKeeper(host,
                Integer.parseInt(cl.getOption("timeout")),
                new MyWatcher(), readOnly);
    }

    public ZooKeeperMain(String args[]) throws IOException, InterruptedException {
        cl.parseOptions(args);
        System.out.println("Connecting to " + cl.getOption("server"));
        connectToZK(cl.getOption("server"));
    }

    public ZooKeeperMain(ZooKeeper zk, StringWriter outputStream) {
        this.zk = zk;
        this.outputStream = outputStream;
        cl = new MyCommandOptions(outputStream);
    }

    public void executeLine(String line)
            throws InterruptedException, IOException, KeeperException {
        if (!line.equals("")) {
            cl.parseCommand(line);
            addToHistory(commandCount, line);
            processCmd(cl);
            commandCount++;
        }
    }

    /**
     * trim the quota tree to recover unwanted tree elements
     * in the quota's tree
     *
     * @param zk   the zookeeper client
     * @param path the path to start from and go up and see if their
     *             is any unwanted parent in the path.
     * @return true if sucessful
     * @throws KeeperException
     * @throws IOException
     * @throws InterruptedException
     */
    private static boolean trimProcQuotas(ZooKeeper zk, String path)
            throws KeeperException, IOException, InterruptedException {
        if (Quotas.quotaZookeeper.equals(path)) {
            return true;
        }
        List<String> children = zk.getChildren(path, false);
        if (children.size() == 0) {
            zk.delete(path, -1);
            String parent = path.substring(0, path.lastIndexOf('/'));
            return trimProcQuotas(zk, parent);
        } else {
            return true;
        }
    }

    /**
     * this method deletes quota for a node.
     *
     * @param zk       the zookeeper client
     * @param path     the path to delete quota for
     * @param bytes    true if number of bytes needs to
     *                 be unset
     * @param numNodes true if number of nodes needs
     *                 to be unset
     * @return true if quota deletion is successful
     * @throws KeeperException
     * @throws IOException
     * @throws InterruptedException
     */
    public static boolean delQuota(ZooKeeper zk, String path,
                                   boolean bytes, boolean numNodes)
            throws KeeperException, IOException, InterruptedException {
        String parentPath = Quotas.quotaZookeeper + path;
        String quotaPath = Quotas.quotaZookeeper + path + "/" + Quotas.limitNode;
        if (zk.exists(quotaPath, false) == null) {
            System.out.println("Quota does not exist for " + path);
            return true;
        }
        byte[] data = null;
        try {
            data = zk.getData(quotaPath, false, new Stat());
        } catch (KeeperException.NoNodeException ne) {
            System.err.println("quota does not exist for " + path);
            return true;
        }
        StatsTrack strack = new StatsTrack(new String(data));
        if (bytes && !numNodes) {
            strack.setBytes(-1L);
            zk.setData(quotaPath, strack.toString().getBytes(), -1);
        } else if (!bytes && numNodes) {
            strack.setCount(-1);
            zk.setData(quotaPath, strack.toString().getBytes(), -1);
        } else if (bytes && numNodes) {
            // delete till you can find a node with more than
            // one child
            List<String> children = zk.getChildren(parentPath, false);
            /// delete the direct children first
            for (String child : children) {
                zk.delete(parentPath + "/" + child, -1);
            }
            // cut the tree till their is more than one child
            trimProcQuotas(zk, parentPath);
        }
        return true;
    }

    private static void checkIfParentQuota(ZooKeeper zk, String path)
            throws InterruptedException, KeeperException {
        final String[] splits = path.split("/");
        String quotaPath = Quotas.quotaZookeeper;
        for (String str : splits) {
            if (str.length() == 0) {
                // this should only be for the beginning of the path
                // i.e. "/..." - split(path)[0] is empty string before first '/'
                continue;
            }
            quotaPath += "/" + str;
            List<String> children = null;
            try {
                children = zk.getChildren(quotaPath, false);
            } catch (KeeperException.NoNodeException ne) {
                LOG.debug("child removed during quota check", ne);
                return;
            }
            if (children.size() == 0) {
                return;
            }
            for (String child : children) {
                if (Quotas.limitNode.equals(child)) {
                    throw new IllegalArgumentException(path + " has a parent "
                            + quotaPath + " which has a quota");
                }
            }
        }
    }

    /**
     * this method creates a quota node for the path
     *
     * @param zk       the ZooKeeper client
     * @param path     the path for which quota needs to be created
     * @param bytes    the limit of bytes on this path
     * @param numNodes the limit of number of nodes on this path
     * @return true if its successful and false if not.
     */
    public static boolean createQuota(ZooKeeper zk, String path,
                                      long bytes, int numNodes)
            throws KeeperException, IOException, InterruptedException {
        // check if the path exists. We cannot create
        // quota for a path that already exists in zookeeper
        // for now.
        Stat initStat = zk.exists(path, false);
        if (initStat == null) {
            throw new IllegalArgumentException(path + " does not exist.");
        }
        // now check if their is already existing
        // parent or child that has quota

        String quotaPath = Quotas.quotaZookeeper;
        // check for more than 2 children --
        // if zookeeper_stats and zookeeper_qutoas
        // are not the children then this path
        // is an ancestor of some path that
        // already has quota
        String realPath = Quotas.quotaZookeeper + path;
        try {
            List<String> children = zk.getChildren(realPath, false);
            for (String child : children) {
                if (!child.startsWith("zookeeper_")) {
                    throw new IllegalArgumentException(path + " has child " +
                            child + " which has a quota");
                }
            }
        } catch (KeeperException.NoNodeException ne) {
            // this is fine
        }

        //check for any parent that has been quota
        checkIfParentQuota(zk, path);

        // this is valid node for quota
        // start creating all the parents
        if (zk.exists(quotaPath, false) == null) {
            try {
                zk.create(Quotas.procZookeeper, null, Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
                zk.create(Quotas.quotaZookeeper, null, Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            } catch (KeeperException.NodeExistsException ne) {
                // do nothing
            }
        }

        // now create the direct children
        // and the stat and quota nodes
        String[] splits = path.split("/");
        StringBuilder sb = new StringBuilder();
        sb.append(quotaPath);
        for (int i = 1; i < splits.length; i++) {
            sb.append("/" + splits[i]);
            quotaPath = sb.toString();
            try {
                zk.create(quotaPath, null, Ids.OPEN_ACL_UNSAFE,
                        CreateMode.PERSISTENT);
            } catch (KeeperException.NodeExistsException ne) {
                //do nothing
            }
        }
        String statPath = quotaPath + "/" + Quotas.statNode;
        quotaPath = quotaPath + "/" + Quotas.limitNode;
        StatsTrack strack = new StatsTrack(null);
        strack.setBytes(bytes);
        strack.setCount(numNodes);
        try {
            zk.create(quotaPath, strack.toString().getBytes(),
                    Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            StatsTrack stats = new StatsTrack(null);
            stats.setBytes(0L);
            stats.setCount(0);
            zk.create(statPath, stats.toString().getBytes(),
                    Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
        } catch (KeeperException.NodeExistsException ne) {
            byte[] data = zk.getData(quotaPath, false, new Stat());
            StatsTrack strackC = new StatsTrack(new String(data));
            if (bytes != -1L) {
                strackC.setBytes(bytes);
            }
            if (numNodes != -1) {
                strackC.setCount(numNodes);
            }
            zk.setData(quotaPath, strackC.toString().getBytes(), -1);
        }
        return true;
    }

    protected boolean processCmd(MyCommandOptions co)
            throws KeeperException, IOException, InterruptedException {
        try {
            return processZKCmd(co);
        } catch (IllegalArgumentException e) {
            outputStream.write(("Command failed: " + e).getBytes());
        } catch (KeeperException.NoNodeException e) {
            outputStream.write(("Node does not exist: " + e.getPath()).getBytes());
        } catch (KeeperException.NoChildrenForEphemeralsException e) {
            outputStream.write(("Ephemerals cannot have children: " + e.getPath()).getBytes());
        } catch (KeeperException.NodeExistsException e) {
            outputStream.write(("Node already exists: " + e.getPath()).getBytes());
        } catch (KeeperException.NotEmptyException e) {
            outputStream.write(("Node not empty: " + e.getPath()).getBytes());
        } catch (KeeperException.NotReadOnlyException e) {
            outputStream.write(("Not a read-only call: " + e.getPath()).getBytes());
        } catch (KeeperException.InvalidACLException e) {
            outputStream.write(("Acl is not valid : " + e.getPath()).getBytes());
        } catch (KeeperException.NoAuthException e) {
            outputStream.write(("Authentication is not valid : " + e.getPath()).getBytes());
        } catch (KeeperException.BadArgumentsException e) {
            outputStream.write(("Arguments are not valid : " + e.getPath()).getBytes());
        } catch (KeeperException.BadVersionException e) {
            outputStream.write(("version No is not valid : " + e.getPath()).getBytes());
        }
        return false;
    }

    protected boolean processZKCmd(MyCommandOptions co)
            throws KeeperException, IOException, InterruptedException {
        Stat stat = new Stat();
        String[] args = co.getArgArray();
        String cmd = co.getCommand();
        if (args.length < 1) {
            usage();
            return false;
        }

        if (!commandMap.containsKey(cmd)) {
            usage();
            return false;
        }

        boolean watch = args.length > 2;
        String path = null;
        List<ACL> acl = Ids.OPEN_ACL_UNSAFE;
        LOG.debug("Processing " + cmd);

        if (cmd.equals("quit")) {
            outputStream.write("Quitting...".getBytes());
        } else if (cmd.equals("redo") && args.length >= 2) {
            Integer i = Integer.decode(args[1]);
            if (commandCount <= i) { // don't allow redoing this redo
                outputStream.write("Command index out of range".getBytes());
                return false;
            }
            cl.parseCommand(history.get(i));
            if (cl.getCommand().equals("redo")) {
                outputStream.write("No redoing redos".getBytes());
                return false;
            }
            history.put(commandCount, history.get(i));
            processCmd(cl);
        } else if (cmd.equals("history")) {
            for (int i = commandCount - 10; i <= commandCount; ++i) {
                if (i < 0) continue;
                outputStream.write((i + " - " + history.get(i) + "\r\n").getBytes());
            }
        } else if (cmd.equals("printwatches")) {
            if (args.length == 1) {
                outputStream.write(("printwatches is " + (printWatches ? "on" : "off")).getBytes());
            } else {
                printWatches = args[1].equals("on");
            }
        }
//        else if (cmd.equals("connect")) {
//            if (args.length >= 2) {
//                connectToZK(args[1]);
//            } else {
//                connectToZK(host);
//            }
//        }

        // Below commands all need a live connection
        if (zk == null || !zk.getState().isAlive()) {
            outputStream.write("Not connected".getBytes());
            return false;
        }

        if (cmd.equals("create") && args.length >= 2) {
            int first = 0;
            CreateMode flags = CreateMode.PERSISTENT;
            if ((args[1].equals("-e") && args[2].equals("-s"))
                    || (args[1]).equals("-s") && (args[2].equals("-e"))) {
                first += 2;
                flags = CreateMode.EPHEMERAL_SEQUENTIAL;
            } else if (args[1].equals("-e")) {
                first++;
                flags = CreateMode.EPHEMERAL;
            } else if (args[1].equals("-s")) {
                first++;
                flags = CreateMode.PERSISTENT_SEQUENTIAL;
            }
            if (args.length == first + 4) {
                acl = parseACLs(args[first + 3]);
            }
            path = args[first + 1];
            String data;
            if (first + 2 >= args.length) {
                data = "";
            } else {
                data = args[first + 2];
            }
            String newPath = zk.create(path, data.getBytes(), acl, flags);
            outputStream.write(("Created " + newPath).getBytes());
        } else if (cmd.equals("delete") && args.length >= 2) {
            path = args[1];
            zk.delete(path, watch ? Integer.parseInt(args[2]) : -1);
        } else if (cmd.equals("rmr") && args.length >= 2) {
            path = args[1];
            ZKUtil.deleteRecursive(zk, path);
        } else if (cmd.equals("set") && args.length >= 3) {
            path = args[1];
            stat = zk.setData(path, args[2].getBytes(),
                    args.length > 3 ? Integer.parseInt(args[3]) : -1);
            printStat(stat, outputStream);
        } else if (cmd.equals("aget") && args.length >= 2) {
            path = args[1];
            var callBack = new DataCallback() {

                public void processResult(int rc, String path, Object ctx, byte[] data,
                                          Stat stat) {
                    System.out.println("rc = " + rc + " path = " + path + " data = "
                            + (data == null ? "null" : new String(data)) + " stat = ");
                    printStat(stat, outputStream);
                }
            };
            zk.getData(path, watch, callBack, path);
        } else if (cmd.equals("get") && args.length >= 2) {
            path = args[1];
            byte data[] = zk.getData(path, watch, stat);
            data = (data == null) ? "null".getBytes() : data;
            outputStream.write(new String(data).getBytes());
            outputStream.write("\r\n");
            printStat(stat, outputStream);
        } else if (cmd.equals("ls") && args.length >= 2) {
            path = args[1];
            List<String> children = zk.getChildren(path, watch);
            outputStream.write(children.toString().getBytes(StandardCharsets.UTF_8));
        } else if (cmd.equals("ls2") && args.length >= 2) {
            path = args[1];
            List<String> children = zk.getChildren(path, watch, stat);
            outputStream.write(children.toString().getBytes());
            printStat(stat, outputStream);
        } else if (cmd.equals("getAcl") && args.length >= 2) {
            path = args[1];
            acl = zk.getACL(path, stat);
            for (ACL a : acl) {
                outputStream.write((a.getId() + ": " + getPermString(a.getPerms())+"\r\n").getBytes());
            }
        } else if (cmd.equals("setAcl") && args.length >= 3) {
            path = args[1];
            stat = zk.setACL(path, parseACLs(args[2]),
                    args.length > 4 ? Integer.parseInt(args[3]) : -1);
            printStat(stat, outputStream);
        } else if (cmd.equals("stat") && args.length >= 2) {
            path = args[1];
            stat = zk.exists(path, watch);
            if (stat == null) {
                throw new KeeperException.NoNodeException(path);
            }
            printStat(stat, outputStream);
        } else if (cmd.equals("listquota") && args.length >= 2) {
            path = args[1];
            String absolutePath = Quotas.quotaZookeeper + path + "/" + Quotas.limitNode;
            byte[] data = null;
            try {
                outputStream.write(("absolute path is " + absolutePath +"\r\n").getBytes());
                data = zk.getData(absolutePath, false, stat);
                StatsTrack st = new StatsTrack(new String(data));
                outputStream.write(("Output quota for " + path + " " + st.toString()+"\r\n").getBytes());

                data = zk.getData(Quotas.quotaZookeeper + path + "/" +
                        Quotas.statNode, false, stat);
                outputStream.write(("Output stat for " + path + " " + new StatsTrack(new String(data)).toString()).getBytes());
            } catch (KeeperException.NoNodeException ne) {
                outputStream.write(("quota for " + path + " does not exist.\r\n").getBytes());
            }
        } else if (cmd.equals("setquota") && args.length >= 4) {
            String option = args[1];
            String val = args[2];
            path = args[3];
            System.err.println("Comment: the parts are " +
                    "option " + option +
                    " val " + val +
                    " path " + path);
            if ("-b".equals(option)) {
                // we are setting the bytes quota
                createQuota(zk, path, Long.parseLong(val), -1);
            } else if ("-n".equals(option)) {
                // we are setting the num quota
                createQuota(zk, path, -1L, Integer.parseInt(val));
            } else {
                usage();
            }

        } else if (cmd.equals("delquota") && args.length >= 2) {
            //if neither option -n or -b is specified, we delete
            // the quota node for thsi node.
            if (args.length == 3) {
                //this time we have an option
                String option = args[1];
                path = args[2];
                if ("-b".equals(option)) {
                    delQuota(zk, path, true, false);
                } else if ("-n".equals(option)) {
                    delQuota(zk, path, false, true);
                }
            } else if (args.length == 2) {
                path = args[1];
                // we dont have an option specified.
                // just delete whole quota node
                delQuota(zk, path, true, true);
            } else if (cmd.equals("help")) {
                usage();
            }
        } else if (cmd.equals("sync") && args.length >= 2) {
            path = args[1];
            zk.sync(path, new AsyncCallback.VoidCallback() {
                public void processResult(int rc, String path, Object ctx) {
                    try {
                        outputStream.write(("Sync returned " + rc).getBytes());
                    } catch (IOException e) {
                        throw new IllegalStateException(e);
                    }
                }
            }, null);
        } else if (cmd.equals("addauth") && args.length >= 2) {
            byte[] b = null;
            if (args.length >= 3)
                b = args[2].getBytes();

            zk.addAuthInfo(args[1], b);
        } else if (!commandMap.containsKey(cmd)) {
            usage();
        }
        return watch;
    }

    private static String getPermString(int perms) {
        StringBuilder p = new StringBuilder();
        if ((perms & ZooDefs.Perms.CREATE) != 0) {
            p.append('c');
        }
        if ((perms & ZooDefs.Perms.DELETE) != 0) {
            p.append('d');
        }
        if ((perms & ZooDefs.Perms.READ) != 0) {
            p.append('r');
        }
        if ((perms & ZooDefs.Perms.WRITE) != 0) {
            p.append('w');
        }
        if ((perms & ZooDefs.Perms.ADMIN) != 0) {
            p.append('a');
        }
        return p.toString();
    }

    private List<ACL> parseACLs(String aclString) throws IOException {
        List<ACL> acl;
        String acls[] = aclString.split(",");
        acl = new ArrayList<ACL>();
        for (String a : acls) {
            int firstColon = a.indexOf(':');
            int lastColon = a.lastIndexOf(':');
            if (firstColon == -1 || lastColon == -1 || firstColon == lastColon) {
                outputStream.write((a + " does not have the form scheme:id:perm").getBytes());
                continue;
            }
            ACL newAcl = new ACL();
            newAcl.setId(new Id(a.substring(0, firstColon), a.substring(
                    firstColon + 1, lastColon)));
            newAcl.setPerms(getPermFromString(a.substring(lastColon + 1)));
            acl.add(newAcl);
        }
        return acl;
    }
}
