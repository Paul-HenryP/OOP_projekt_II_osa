module com.example.oopfx {
    requires javafx.controls;
    requires javafx.fxml;


    opens oopfx to javafx.fxml;
    exports oopfx;
}