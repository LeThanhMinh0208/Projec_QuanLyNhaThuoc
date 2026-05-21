package utils;

import gui.main.GUI_TrangChuController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.layout.BorderPane;

public class SceneUtils {
    private static BorderPane mainControl;
    private static Object currentController;

    public static void init(BorderPane borderPane) {
        mainControl = borderPane;
    }

    public static void switchPage(String fxmlPath) {
        try {
            if (mainControl == null) {
                System.err.println("Lỗi: SceneUtils chưa được khởi tạo!");
                return;
            }

            // Dừng background tasks của controller cũ
            if (currentController != null) {
                try {
                    java.lang.reflect.Method stopMethod = currentController.getClass().getMethod("stopBackgroundTasks");
                    stopMethod.invoke(currentController);
                } catch (Exception e) {
                    // Controller cũ không có method này - không sao
                }
            }

            FXMLLoader loader = new FXMLLoader(SceneUtils.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Xử lý tự động load lại dữ liệu nếu trang đích là Trang Chủ
            Object controller = loader.getController();
            currentController = controller; // Lưu reference controller mới
            
            if (controller instanceof GUI_TrangChuController) {
                ((GUI_TrangChuController) controller).loadDataTrangChu();
            }

            // Thay đổi nội dung vùng giữa
            mainControl.setCenter(root);
        } catch (Exception e) {
            System.err.println("Lỗi nạp file FXML tại: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public static void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}