open module app.main {
    requires javafx.fxml;
    requires javafx.graphics;
    requires javafx.controls;
    requires org.slf4j;
    requires log4j.slf4j.impl;
    requires org.apache.logging.log4j;
    requires org.apache.logging.log4j.core;
    requires com.google.common;
    requires domain.main;
    requires spi.main;
    requires java.desktop;
    requires java.net.http;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires com.fasterxml.jackson.dataformat.xml;
}