module com.compass {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires java.sql;
    requires java.net.http;
    requires mysql.connector.j;

    requires com.google.gson;
    requires org.slf4j;
    requires ch.qos.logback.classic;
    requires jbcrypt;
    requires java.base;

    opens com.compass.controller to javafx.fxml;

    exports com.compass;
    exports com.compass.db;
    exports com.compass.model;
    exports com.compass.repository;
    exports com.compass.service;
    exports com.compass.controller;
    exports com.compass.util;
    exports com.compass.navigation;
    exports com.compass.thread;
}
