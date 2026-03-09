package gui.main;

import entity.NhanVien;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.text.Text;
import javafx.stage.Stage;

public class GUI_TrangChuController {
    
    private static NhanVien nhanVienDangNhap;

    @FXML private Text txtXinChao;

    public static void setNhanVienDangNhap(NhanVien nv) {
        nhanVienDangNhap = nv;
    }

    @FXML
    public void initialize() {
        if (txtXinChao != null && nhanVienDangNhap != null) {
            txtXinChao.setText("Xin chào " + nhanVienDangNhap.getChucVu() + ": " + nhanVienDangNhap.getHoTen());
        }
    }

    @FXML
    void handleDangXuat(ActionEvent event) {
        try {
            nhanVienDangNhap = null; // Xóa thông tin phiên làm việc

            // Đóng cửa sổ Trang Chủ
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

            // Mở lại form Đăng Nhập
            Parent root = FXMLLoader.load(getClass().getResource("GUI_DangNhap.fxml"));
            Stage loginStage = new Stage();
            loginStage.setTitle("Hệ thống quản lý nhà thuốc - Đăng nhập");
            loginStage.setScene(new Scene(root));
            loginStage.setResizable(false);
            loginStage.show();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}