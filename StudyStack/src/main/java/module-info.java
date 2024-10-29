module org.example.studystack {
    requires javafx.controls;
    requires javafx.fxml;
    requires firebase.admin;
    requires google.cloud.firestore;
    requires com.google.auth.oauth2;
    requires com.google.auth;
    requires com.google.api.apicommon;
    requires com.google.api.client;
    requires com.google.api.client.auth;
    requires java.net.http;
    requires org.json;


    opens org.example.studystack to javafx.fxml;
    exports org.example.studystack;
}