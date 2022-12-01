package cc.cc1234.core.configuration.entity;

import cc.cc1234.specification.listener.ConfigurationChangeListener;
import org.junit.Assert;
import org.junit.Test;

import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.UUID;

public class ConfigurationTest {

    @Test
    public void testAdd() {
        var listener = new ConfigurationChangeListener() {
        };
        var configuration = create(listener);

        // add duplicate server
        Assert.assertThrows(IllegalStateException.class, () -> {
            Assert.assertTrue(configuration.existsById("localhost:2181"));
            var server = createServerConfiguration("localhost", 2181);
            configuration.add(server);
        });

        // normal add
        var server = createServerConfiguration("local.test.add", 2181);
        configuration.add(server);
        Assert.assertTrue(configuration.existsById("local.test.add:2181"));
    }

    @Test
    public void testUpdateLocale() {
        var listener = new ConfigurationChangeListener() {
        };
        var configuration = create(listener);
        Assert.assertEquals(configuration.getLocaleConfiguration().getLocale(), Locale.CHINESE);

        var newLocaleConfig = new Configuration.LocaleConfiguration(Locale.ENGLISH);
        configuration.updateLocale(newLocaleConfig);
        Assert.assertEquals(configuration.getLocaleConfiguration().getLocale(), Locale.ENGLISH);
        Assert.assertThrows(NullPointerException.class, () -> configuration.updateLocale(null));
    }

    @Test
    public void testExists() {
        var listener = new ConfigurationChangeListener() {
        };
        var configuration = create(listener);
        Assert.assertTrue(configuration.existsById("localhost:2181"));
        Assert.assertTrue(configuration.existsById("localhost:2182"));
        Assert.assertTrue(configuration.existsById("local.test:2181"));
        Assert.assertFalse(configuration.existsById("localhost:21"));
        Assert.assertFalse(configuration.existsById(""));
        Assert.assertFalse(configuration.existsById(null));
    }

    @Test
    public void testGet() {
        var listener = new ConfigurationChangeListener() {
        };
        var configuration = create(listener);
        var l = configuration.getById("localhost:2181").orElseThrow();
        Assert.assertEquals("localhost", l.getHost());
        Assert.assertEquals(2181, l.getPort().intValue());
        Assert.assertEquals("localhost:2181", l.getAlias());
        Assert.assertEquals(false, l.getSshTunnelEnabled());
        Assert.assertNotNull(l.getAclList());
        Assert.assertTrue(l.getAclList().isEmpty());

        Assert.assertFalse(configuration.getById(null).isPresent());
    }

    @Test
    public void testDelete() {
        var listener = new ConfigurationChangeListener() {
        };
        var configuration = create(listener);
        configuration.deleteById("localhost:2181");
        Assert.assertFalse(configuration.existsById("localhost:2181"));

        Assert.assertThrows(NoSuchElementException.class, () -> configuration.deleteById("localhost"));
    }

    @Test
    public void testIncrementConnectTimes() {
        var listener = new ConfigurationChangeListener() {
        };
        var configuration = create(listener);
        var serverConfiguration = configuration.getById("localhost:2181").orElseThrow();
        var expectConnectTimes = serverConfiguration.getConnectTimes() + 1;
        configuration.incrementConnectTimes("localhost:2181");
        Assert.assertEquals(expectConnectTimes, serverConfiguration.getConnectTimes());

        // test increment not exists server
        configuration.incrementConnectTimes(null);
        configuration.incrementConnectTimes(UUID.randomUUID().toString());
    }

    private Configuration create(ConfigurationChangeListener listener) {
        var localeConfiguration = new Configuration.LocaleConfiguration(Locale.CHINESE);
        var fontConfiguration = new Configuration.FontConfiguration(18);
        return Configuration.builder()
                .configurationChangeListener(listener)
                .configurationChangeListener(listener)
                .localeConfiguration(localeConfiguration)
                .fontConfiguration(fontConfiguration)
                .serverConfiguration(createServerConfiguration("localhost", 2181))
                .serverConfiguration(createServerConfiguration("localhost", 2182))
                .serverConfiguration(createServerConfiguration("local.test", 2181))
                .build();
    }

    private ServerConfiguration createServerConfiguration(String host, int port) {
        String url = host + ":" + port;
        return ServerConfiguration.builder()
                .url(url)
                .host(host)
                .port(port)
                .alias(url)
                .build();
    }

}
