package cc.cc1234.core.configuration.entity;

import cc.cc1234.core.configuration.value.SSHTunnelConfiguration;
import org.junit.Assert;
import org.junit.Test;

import java.util.List;

public class ServerConfigurationTest {

    @Test
    public void testUpdateBaseInfo() {
        var serverConfiguration = ServerConfiguration.builder()
                .url("localhost:2181")
                .host("localhost")
                .port(2181)
                .aclList(List.of())
                .alias("localhost")
                .sshTunnelEnabled(false)
                .connectTimes(100)
                .build();

        var updateInfo = ServerConfiguration.builder()
                .url("local.me:2182")
                .host("local.me")
                .aclList(List.of("digest:digest:digest"))
                .port(2182)
                .alias("local.me")
                .connectTimes(10)
                .build();
        serverConfiguration.update(updateInfo);

        // assert ignore
        Assert.assertEquals("localhost", serverConfiguration.getHost());
        Assert.assertEquals(2181, serverConfiguration.getPort().intValue());
        Assert.assertEquals(100, serverConfiguration.getConnectTimes());

        // assert not ignore
        Assert.assertEquals("local.me", serverConfiguration.getAlias());
        var aclList = serverConfiguration.getAclList();
        Assert.assertNotNull(aclList);
        Assert.assertEquals(1, aclList.size());
        Assert.assertTrue(aclList.contains("digest:digest:digest"));
        Assert.assertFalse(serverConfiguration.getSshTunnelEnabled());
        Assert.assertNull(serverConfiguration.getSshTunnel());
    }

    @Test
    public void testUpdateSSHTunnelInfo() {
        var serverConfiguration = ServerConfiguration.builder()
                .url("localhost:2181")
                .host("localhost")
                .port(2181)
                .aclList(List.of())
                .alias("localhost")
                .sshTunnelEnabled(false)
                .connectTimes(100)
                .build();
        var tunnelConfiguration = SSHTunnelConfiguration.builder()
                .localhost("localhost")
                .localPort(2181)
                .remoteHost("remoteHost")
                .remotePort(2182)
                .sshHost("sshHost")
                .sshPort(21)
                .sshUsername("username")
                .sshPassword("password")
                .build();
        var updateInfo = ServerConfiguration.builder()
                .sshTunnelEnabled(true)
                .sshTunnel(tunnelConfiguration)
                .build();
        serverConfiguration.update(updateInfo);

        Assert.assertEquals(true, serverConfiguration.getSshTunnelEnabled());
        Assert.assertNotNull("sshTunnel Configuration shouldn't be null", serverConfiguration.getSshTunnel());
        Assert.assertEquals("localhost", serverConfiguration.getSshTunnel().getLocalhost());
        Assert.assertEquals(2181, serverConfiguration.getSshTunnel().getLocalPort().intValue());
        Assert.assertEquals("sshHost", serverConfiguration.getSshTunnel().getSshHost());
        Assert.assertEquals(21, serverConfiguration.getSshTunnel().getSshPort().intValue());
        Assert.assertEquals("username", serverConfiguration.getSshTunnel().getSshUsername());
        Assert.assertEquals("password", serverConfiguration.getSshTunnel().getSshPassword());
        Assert.assertEquals("remoteHost", serverConfiguration.getSshTunnel().getRemoteHost());
        Assert.assertEquals(2182, serverConfiguration.getSshTunnel().getRemotePort().intValue());
    }

    @Test
    public void testIncrementConnectTimes() {
        var serverConfiguration = ServerConfiguration.builder()
                .url("localhost:2181")
                .host("localhost")
                .port(2181)
                .aclList(List.of())
                .alias("localhost")
                .sshTunnelEnabled(false)
                .connectTimes(100)
                .build();
        serverConfiguration.incrementConnectTimes();
        Assert.assertEquals(101, serverConfiguration.getConnectTimes());
    }
}
