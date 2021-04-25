module com.github.therealjlb.claude {
    requires javafx.base;
    requires javafx.controls;
    requires javafx.graphics;
    requires java.datatransfer;
    requires java.desktop;
    requires java.sql;
    requires com.fasterxml.jackson.annotation;
    requires com.fasterxml.jackson.core;
    requires com.fasterxml.jackson.databind;
    requires spring.core;
    requires spring.web;

    // Tells the Java compiler that you module needs this module from the JDK
    requires java.net.http;
    requires spring.context;
    requires spring.websocket;
    requires spring.messaging;

    opens com.github.therealjlb.claude to javafx.graphics;
}