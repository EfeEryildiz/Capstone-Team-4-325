module group.flashcardstudytool {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.web;


    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires net.synedra.validatorfx;
    requires org.kordamp.ikonli.javafx;
    requires org.kordamp.bootstrapfx.core;
    requires eu.hansolo.tilesfx;
    requires com.almasb.fxgl.all;
    requires firebase.admin;
    requires google.cloud.firestore;
    requires com.google.auth.oauth2;
    requires com.google.auth;
    requires com.google.api.apicommon;
    requires com.google.api.client;
    requires com.google.api.client.auth;


    opens group.flashcardstudytool.controllers to javafx.fxml;
    exports group.flashcardstudytool;
    exports group.flashcardstudytool.controllers;
}