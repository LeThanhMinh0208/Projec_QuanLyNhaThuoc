package gui.main;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class Main extends Application {
    
    @Override
    public void start(Stage primaryStage) {
        try {
            // Đọc file FXML mà ông vừa thiết kế bên Scene Builder
            Parent root = FXMLLoader.load(getClass().getResource("GUI_DangNhap.fxml"));
            
            // Đưa bản thiết kế đó vào một cái Scene (khung cảnh)
            Scene scene = new Scene(root);
            
            // Thiết lập cửa sổ hiển thị
            primaryStage.setTitle("Hệ thống Quản lý Nhà thuốc Long Nguyên");
            primaryStage.setScene(scene);
            
            // Ngăn người dùng kéo dãn cửa sổ làm vỡ layout (tuỳ chọn)
            primaryStage.setResizable(false); 
            
            // Bật cửa sổ lên!
            primaryStage.show();
            
        } catch(Exception e) {
            System.out.println("Lỗi load giao diện: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        launch(args);
    }
}