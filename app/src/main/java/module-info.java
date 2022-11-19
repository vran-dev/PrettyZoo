open module app.main {
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.controls;
    requires org.slf4j;
    requires log4j.slf4j.impl;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires com.google.common;
    requires java.desktop;
    requires java.net.http;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires org.antlr.antlr4.runtime;
    requires zookeeper;
    requires com.jfoenix;
    requires org.apache.commons.io;
    requires org.fxmisc.richtext;
    requires org.fxmisc.flowless;
    // internal module
    requires core.main;
    requires specification.main;
}
