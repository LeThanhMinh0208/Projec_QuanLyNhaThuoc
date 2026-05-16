package utils;

import javafx.event.ActionEvent;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class WindowUtils {

    // 1. Dùng cho Đăng nhập: Đóng cửa sổ hiện tại
    public static void closeWindow(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    // 2. Dùng cho cả hai: Mở cửa sổ mới (Trả về loader để lấy Controller nếu cần)
    public static FXMLLoader openWindow(String fxmlPath, String title, double width, double height) {
        try {
            FXMLLoader loader = new FXMLLoader(WindowUtils.class.getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root, width, height));
            stage.show();
            return loader;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // 3. Dùng cho Danh Mục Thuốc: Mở cửa sổ dạng Pop-up (Modal)
    public static FXMLLoader openModal(String fxmlPath, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(WindowUtils.class.getResource(fxmlPath));
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle(title);
            stage.setScene(new Scene(root));

            // Dòng này cực kỳ quan trọng: Ngăn không cho tương tác với trang chính khi đang mở Pop-up
            stage.initModality(Modality.APPLICATION_MODAL);

            return loader;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}