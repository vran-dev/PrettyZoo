open module domain.main {
    requires static lombok;
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.controls;
    requires curator.framework;
    requires curator.recipes;
    requires curator.client;
    requires zookeeper;
    requires org.slf4j;
    requires log4j.slf4j.impl;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.google.common;
    requires richtextfx;
    requires flowless;
    uses org.apache.logging.log4j.message.ThreadDumpMessage.ThreadInfoFactory;
    uses org.apache.logging.log4j.spi.Provider;
    uses org.apache.logging.log4j.util.PropertySource;
}