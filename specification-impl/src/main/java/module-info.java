open module specificationImpl.main {
    requires specification.main;

    requires curator.framework;
    requires curator.recipes;
    requires org.slf4j;
    requires zookeeper;
    requires curator.client;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.datatype.jdk8;

    exports cc.cc1234.client.curator;
    exports cc.cc1234.config;
    exports cc.cc1234.zookeeper;
}
