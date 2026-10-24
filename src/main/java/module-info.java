module com.example.arvoremorse {
    requires javafx.controls;
    requires javafx.fxml;


    opens com.example.arvoremorse to javafx.fxml;
    exports com.example.arvoremorse;
}