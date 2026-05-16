package gui.main;

import connectDB.ConnectDB;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {

    @Override
    public void start(Stage primaryStage) {
        try {
            // Khởi tạo kết nối Database
            ConnectDB.getInstance().connect();

            // Nạp file giao diện Đăng Nhập
            Parent root = FXMLLoader.load(getClass().getResource("GUI_DangNhap.fxml"));

            // 🚨 ĐÃ SỬA: Ép cứng kích thước màn hình Đăng nhập là 950x600
            Scene scene = new Scene(root, 950, 600);

            primaryStage.setTitle("Hệ thống quản lý nhà thuốc - Đăng nhập");
            primaryStage.setScene(scene);

            // Khóa kéo giãn để form không bị méo
            primaryStage.setResizable(false);

            // 🚨 ĐÃ THÊM: Canh cái form tự động nhảy vào ngay chính giữa màn hình
            primaryStage.centerOnScreen();

            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void stop() throws Exception {
        ConnectDB.getInstance().disconnect();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
    }
}