package gui.main;

import dao.DAO_NhanVien;
import entity.NhanVien;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import utils.AlertUtils;
import utils.UserSession;
import utils.WindowUtils;

public class GUI_DangNhapController {

    @FXML private TextField txtTenDangNhap;
    @FXML private PasswordField txtMatKhau;

    private DAO_NhanVien nhanVienDao = new DAO_NhanVien();

    @FXML
    void handleDangNhap(ActionEvent event) {
        String taiKhoan = txtTenDangNhap.getText().trim();
        String matKhau  = txtMatKhau.getText();

        // 1. Kiểm tra trống (Dùng logic đơn giản hoặc Validator Utils)
        if (taiKhoan.isEmpty() || matKhau.isEmpty()) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập đầy đủ tài khoản và mật khẩu!");
            return;
        }

        // 2. Gọi DAO xác thực
        NhanVien nv = nhanVienDao.dangNhap(taiKhoan, matKhau);

        if (nv != null) {
            // 3. Lưu nhân viên vào Session dùng chung cho toàn App
            UserSession.getInstance().setUser(nv);

            // 4. Đóng cửa sổ đăng nhập
            WindowUtils.closeWindow(event);

            // 5. Mở Trang Chủ
            String title = "Long Nguyên Pharma — " + nv.getChucVu() + ": " + nv.getHoTen();
            WindowUtils.openWindow("/gui/main/GUI_TrangChu.fxml", title, 1280, 720);
            
        } else {
            // 6. Xử lý khi đăng nhập thất bại
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Đăng nhập thất bại", "Tài khoản hoặc mật khẩu không đúng!");
            txtMatKhau.clear();
            txtMatKhau.requestFocus();
        }
    }
}