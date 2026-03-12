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
            // Mở kết nối Database khi khởi động
            ConnectDB.getInstance().connect();

            // Gọi màn hình Đăng Nhập lên
            Parent root = FXMLLoader.load(getClass().getResource("GUI_DangNhap.fxml"));
            Scene scene = new Scene(root);
            
            primaryStage.setTitle("Hệ thống quản lý nhà thuốc - Đăng nhập");
            primaryStage.setScene(scene);
            primaryStage.setResizable(false); 
            primaryStage.show();
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    // Đóng Database khi tắt phần mềm
    @Override
    public void stop() throws Exception {
        ConnectDB.getInstance().disconnect();
        super.stop();
    }

    public static void main(String[] args) {
        launch(args);
       }
}