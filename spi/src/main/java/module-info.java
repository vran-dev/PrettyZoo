open module spi.main {
    exports cc.cc1234.spi.connection;
    exports cc.cc1234.spi.config;
    exports cc.cc1234.spi.config.model;
    exports cc.cc1234.spi.node;
    exports cc.cc1234.spi.listener;
    exports cc.cc1234.spi.util;
    requires zookeeper;
}