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
    requires invoker.coinbasepro.api;
    requires invoker.commons;
    requires invoker.security;

    // Tells the Java compiler that you module needs this module from the JDK
    requires java.net.http;

    opens com.github.therealjlb.claude to javafx.graphics;
}