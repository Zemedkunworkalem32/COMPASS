module com.campus {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires java.net.http;
    requires javafx.graphics;

    opens com.campus.controller to javafx.fxml;
    opens com.campus.model to javafx.base;
    exports com.campus;
    exports com.campus.controller;
    exports com.campus.model;
    exports com.campus.service;
    exports com.campus.util;
}

