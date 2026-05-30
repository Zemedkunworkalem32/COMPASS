module com.compass {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;

    requires java.sql;
    requires mysql.connector.j;
    requires jbcrypt;

    opens com.compass.controllers to javafx.fxml;

    exports com.compass;
    exports com.compass.config;
    exports com.compass.controllers;
    exports com.compass.models;
}
