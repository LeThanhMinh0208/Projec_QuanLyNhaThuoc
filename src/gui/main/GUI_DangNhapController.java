package gui.main;

import entity.NhanVien;
import dao.DAO_NhanVien;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class GUI_DangNhapController {

    @FXML private TextField txtTenDangNhap;
    @FXML private PasswordField txtMatKhau;
    

    private DAO_NhanVien nhanVienDao = new DAO_NhanVien();

    @FXML
    void handleDangNhap(ActionEvent event) {
        String taiKhoan = txtTenDangNhap.getText().trim();
        String matKhau  = txtMatKhau.getText();

        if (taiKhoan.isEmpty() || matKhau.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập đầy đủ tài khoản và mật khẩu!");
            return;
        }

        // 1. Gọi DAO để lấy đối tượng NhanVien 
        NhanVien nv = nhanVienDao.dangNhap(taiKhoan, matKhau);

        // 2. Kiểm tra nếu tìm thấy nhân viên (nv không bị null)
        if (nv != null) {
            // Truyền cả đối tượng nv sang Trang Chủ
            GUI_TrangChuController.setNhanVienDangNhap(nv);

            // Đóng cửa sổ đăng nhập
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();

            // Mở trang chủ
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("GUI_TrangChu.fxml"));
                Parent root = loader.load();

                Stage trangChuStage = new Stage();
                // Lấy chức vụ và họ tên trực tiếp từ đối tượng nv để đặt tiêu đề
                trangChuStage.setTitle("Long Nguyên Pharma  —  " + nv.getChucVu() + ": " + nv.getHoTen());
                trangChuStage.setScene(new Scene(root, 1280, 720));
                trangChuStage.show();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            // Nếu nv == null tức là sai tài khoản/mật khẩu
            showAlert(Alert.AlertType.ERROR, "Đăng nhập thất bại", "Tài khoản hoặc mật khẩu không đúng!");
            txtMatKhau.clear();
            txtMatKhau.requestFocus();
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}