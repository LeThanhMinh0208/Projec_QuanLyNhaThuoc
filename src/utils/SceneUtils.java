package utils;

import gui.main.GUI_TrangChuController;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.layout.BorderPane;

public class SceneUtils {
    private static BorderPane mainControl;

    public static void init(BorderPane borderPane) {
        mainControl = borderPane;
    }

    public static void switchPage(String fxmlPath) {
        try {
            if (mainControl == null) {
                System.err.println("Lỗi: SceneUtils chưa được khởi tạo!");
                return;
            }

            FXMLLoader loader = new FXMLLoader(SceneUtils.class.getResource(fxmlPath));
            Parent root = loader.load();

            // Xử lý tự động load lại dữ liệu nếu trang đích là Trang Chủ
            Object controller = loader.getController();
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
}