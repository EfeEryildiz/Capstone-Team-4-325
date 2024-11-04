module org.example.studystack {
    requires javafx.controls;
    requires javafx.fxml;
    requires firebase.admin;
    requires google.cloud.core;
    requires google.cloud.firestore;
    requires com.google.auth.oauth2;
    requires com.google.auth;
    requires com.google.api.apicommon;
    requires com.google.api.client;
    requires com.google.api.client.auth;
    requires com.google.api.client.extensions.appengine;


    opens org.example.studystack to javafx.fxml;
    exports org.example.studystack;
    exports org.example.studystack.controllers;
    opens org.example.studystack.controllers to javafx.fxml;
    exports org.example.studystack.utils;
    opens org.example.studystack.utils to javafx.fxml;
}