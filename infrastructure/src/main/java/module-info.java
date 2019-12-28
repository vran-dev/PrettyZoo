open module infrastructure.main {
    requires spi.main;

    requires curator.framework;
    requires curator.recipes;
    requires org.slf4j;
    requires zookeeper;
    requires curator.client;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;

    exports cc.cc1234.client.curator;
    exports cc.cc1234.config;
}