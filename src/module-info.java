module QuanLyNhaThuoc_LongNguyen {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;
    requires java.sql;
    requires com.microsoft.sqlserver.jdbc;
    requires java.desktop;
    

    requires itextpdf; 

    opens service to itextpdf;

    opens gui.main to javafx.fxml;
    opens gui.dialogs to javafx.fxml;
    opens entity to javafx.base;

    exports gui.main;
}