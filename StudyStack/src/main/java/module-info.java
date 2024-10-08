module org.example.studystack {
    requires javafx.controls;
    requires javafx.fxml;


    opens org.example.studystack to javafx.fxml;
    exports org.example.studystack;
}