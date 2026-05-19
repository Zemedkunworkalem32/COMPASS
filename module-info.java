module com.compass {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.net.http;
    requires javafx.graphics;

    opens com.compass.controller to javafx.fxml;
    opens com.compass.model to javafx.base;
    exports com.compass;
    exports com.compass.controller;
    exports com.compass.model;
    exports com.compass.service;
    exports com.compass.util;
}
