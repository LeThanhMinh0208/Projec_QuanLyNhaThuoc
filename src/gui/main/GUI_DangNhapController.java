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

        // 1. Kiểm tra rỗng
        if (taiKhoan.isEmpty() || matKhau.isEmpty()) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập đầy đủ tài khoản và mật khẩu!");
            return;
        }

        // 2. Gọi DAO xác thực
        NhanVien nv = nhanVienDao.dangNhap(taiKhoan, matKhau);

        if (nv != null) {
            // 🚨 KIỂM TRA TRẠNG THÁI: NẾU BỊ KHÓA THÌ CHẶN LẠI NGAY 🚨
            if (nv.getTrangThai() == 2) {
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Tài khoản bị khóa", "Tài khoản của bạn đã bị khóa!\nVui lòng liên hệ Quản lý để được hỗ trợ.");
                return; // Dừng hàm, không cho đăng nhập
            }

            // 3. Nếu trạng thái bình thường (1) -> Lưu Session dùng chung
            UserSession.getInstance().setUser(nv);

            // 4. Đóng cửa sổ đăng nhập
            WindowUtils.closeWindow(event);

            // 5. Mở Trang Chủ
            String title = "Long Nguyên Pharma — " + nv.getChucVu() + ": " + nv.getHoTen();
            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/gui/main/GUI_TrangChu.fxml"));
                javafx.scene.Parent root = loader.load();
                javafx.stage.Stage mainStage = new javafx.stage.Stage();
                mainStage.setTitle(title);
                mainStage.setScene(new javafx.scene.Scene(root, 1280, 720));
                mainStage.show();
                mainStage.setMaximized(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
            
        } else {
            // 6. Xử lý khi đăng nhập thất bại (Sai pass hoặc Sai user)
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Đăng nhập thất bại", "Tài khoản hoặc mật khẩu không đúng!");
            txtMatKhau.clear();
            txtMatKhau.requestFocus();
        }
    }
}