open module domain.main {
    exports cc.cc1234.manager;
    exports cc.cc1234.service;

    requires static lombok;
    requires curator.framework;
    requires curator.recipes;
    requires curator.client;
    requires zookeeper;
    requires org.slf4j;
    requires log4j.slf4j.impl;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires com.google.common;

    requires infrastructure.main;
    requires spi.main;
    requires sshj;


    uses org.apache.logging.log4j.message.ThreadDumpMessage.ThreadInfoFactory;
    uses org.apache.logging.log4j.spi.Provider;
    uses org.apache.logging.log4j.util.PropertySource;
}