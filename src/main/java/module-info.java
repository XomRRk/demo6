module org.example.demo6 {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires com.dlsc.formsfx;
    requires org.kordamp.bootstrapfx.core;
    requires java.desktop;
    requires java.sql;
    requires org.postgresql.jdbc;
    requires jlibmodbus;


    opens org.example.demo6 to javafx.fxml;
    exports org.example.demo6;
}