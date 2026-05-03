package gui.main;

import dao.DAO_NhanVien;
import dao.DAO_NhatKyHoatDong;
import entity.NhanVien;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import utils.UserSession; 

public class GUI_DoiMatKhauController {

    @FXML private TextField txtHoTen, txtTaiKhoan, txtSdt;
    @FXML private PasswordField txtMatKhauCu, txtMatKhauMoi, txtXacNhanMatKhau;

    private DAO_NhanVien dao = new DAO_NhanVien();
    private NhanVien currentUser;

    @FXML
    public void initialize() {
        // 1. Lấy thông tin user tạm thời từ Session
        NhanVien sessionUser = UserSession.getInstance().getUser(); 
        
        if (sessionUser != null) {
            // 🚨 ĐỒNG BỘ DỮ LIỆU: Lôi dữ liệu MỚI NHẤT từ Database để lấy SĐT/Tên mới nhất
            for (NhanVien nv : dao.getChiNhanVien()) {
                if (nv.getMaNhanVien().equals(sessionUser.getMaNhanVien())) {
                    currentUser = nv; 
                    // Cập nhật lại Session để các trang khác dùng chung dữ liệu mới
                    UserSession.getInstance().setUser(nv); 
                    break;
                }
            }
            
            // Backup an toàn nếu DB gặp sự cố
            if (currentUser == null) {
                currentUser = sessionUser;
            }

            // Gán dữ liệu lên giao diện
            txtHoTen.setText(currentUser.getHoTen());
            txtTaiKhoan.setText(currentUser.getTenDangNhap());
        } else {
            new Alert(Alert.AlertType.ERROR, "Lỗi: Không tìm thấy phiên đăng nhập hiện tại!").show();
        }
    }

    @FXML
    void handleLuuThayDoi(ActionEvent event) {
        if (currentUser == null) {
            return;
        }

        String sdtXacThuc = txtSdt.getText().trim();
        String mkCu = txtMatKhauCu.getText();
        String mkMoi = txtMatKhauMoi.getText();
        String mkXacNhan = txtXacNhanMatKhau.getText();

        // 1. KIỂM TRA RỖNG
        if (sdtXacThuc.isEmpty() || mkCu.isEmpty() || mkMoi.isEmpty() || mkXacNhan.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng nhập đầy đủ các ô bắt buộc (*)").show();
            return;
        }

        // 2. XÁC THỰC SỐ ĐIỆN THOẠI (Dữ liệu đã đồng bộ từ DB ở trên)
        if (!sdtXacThuc.equals(currentUser.getSdt())) {
            new Alert(Alert.AlertType.ERROR, "Số điện thoại xác thực không khớp với hồ sơ của bạn!").show();
            return;
        }

        // 3. XÁC THỰC MẬT KHẨU CŨ
        if (!mkCu.equals(currentUser.getMatKhau())) {
            new Alert(Alert.AlertType.ERROR, "Mật khẩu hiện tại không chính xác!").show();
            return;
        }

        // 4. KIỂM TRA MẬT KHẨU MỚI
        if (mkMoi.equals(mkCu)) {
            new Alert(Alert.AlertType.WARNING, "Mật khẩu mới không được trùng với mật khẩu cũ!").show();
            return;
        }

        if (mkMoi.length() < 6) {
            new Alert(Alert.AlertType.WARNING, "Mật khẩu mới quá ngắn. Phải có ít nhất 6 ký tự!").show();
            return;
        }

        if (!mkMoi.equals(mkXacNhan)) {
            new Alert(Alert.AlertType.ERROR, "Xác nhận mật khẩu không khớp!").show();
            return;
        }

        // 5. GỌI DAO ĐỂ LƯU
        if (dao.doiMatKhau(currentUser.getMaNhanVien(), mkMoi)) {
            DAO_NhatKyHoatDong.ghiLog("DOI_MAT_KHAU", "Hệ thống", currentUser.getMaNhanVien(), "Đổi mật khẩu tài khoản");
            
            // Cập nhật Session ngay lập tức để đồng bộ toàn hệ thống
            currentUser.setMatKhau(mkMoi); 
            UserSession.getInstance().setUser(currentUser); 
            
            Alert success = new Alert(Alert.AlertType.INFORMATION);
            success.setTitle("Thành Công");
            success.setHeaderText("Đổi mật khẩu thành công!");
            success.setContentText("Lần đăng nhập tới, vui lòng sử dụng mật khẩu mới.");
            success.showAndWait();

            handleLamMoi(null);
        } else {
            new Alert(Alert.AlertType.ERROR, "Lỗi hệ thống: Không thể đổi mật khẩu lúc này!").show();
        }
    }

    @FXML
    void handleLamMoi(ActionEvent event) {
        txtSdt.clear();
        txtMatKhauCu.clear();
        txtMatKhauMoi.clear();
        txtXacNhanMatKhau.clear();
    }
}