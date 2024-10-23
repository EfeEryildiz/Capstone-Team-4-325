module org.example.studystack {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.net.http;
    requires org.json;

    opens org.example.studystack to javafx.fxml;
    exports org.example.studystack;
}
