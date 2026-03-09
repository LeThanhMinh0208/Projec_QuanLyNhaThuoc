package gui.main;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import javafx.scene.Node;

public class GUI_DangNhapController {

    @FXML
    private TextField txtTenDangNhap;

    @FXML
    private PasswordField txtMatKhau;

    // Chuỗi kết nối SQL Server - Đổi sa/123456 cho đúng cái máy m nha!
    private final String URL = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyNhaThuoc_LongNguyen;encrypt=true;trustServerCertificate=true;";
    private final String USER = "sa"; 
    private final String PASS = "sapassword";

    @FXML
    void handleDangNhap(ActionEvent event) {
        String taiKhoan = txtTenDangNhap.getText();
        String matKhau = txtMatKhau.getText();

        if (taiKhoan.isEmpty() || matKhau.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Khoan đã!", "Mày chưa nhập tài khoản hay mật khẩu kìa ba!");
            return;
        }

        // Cắm ống hút vào Database kiểm tra
        try (Connection conn = DriverManager.getConnection(URL, USER, PASS)) {
            String sql = "SELECT hoTen, chucVu FROM NhanVien WHERE tenDangNhap = ? AND matKhau = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, taiKhoan);
            pst.setString(2, matKhau);
            
            ResultSet rs = pst.executeQuery();
            
            if (rs.next()) {
                String hoTen = rs.getString("hoTen");
                String chucVu = rs.getString("chucVu");
                
                System.out.println("Đăng nhập thành công! Xin chào " + hoTen);

                // 1. Lấy Stage (cửa sổ) hiện tại và Đóng nó lại
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                currentStage.close();

                // 2. Mở cửa sổ Trang Chủ lên
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("GUI_TrangChu.fxml"));
                    Parent root = loader.load();
                    
                    Stage trangChuStage = new Stage();
                    trangChuStage.setTitle("Hệ Thống Quản Lý Nhà Thuốc - " + chucVu);
                    trangChuStage.setScene(new Scene(root));
                    trangChuStage.show();

                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Lỗi Giao Diện", "Không mở được trang chủ: " + e.getMessage());
                }

            } else {
                showAlert(Alert.AlertType.ERROR, "Toang", "Tài khoản hay mật khẩu sai cmnr!");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Mất kết nối Database! Bật SQL Server lên chưa má ơi?\nLỗi: " + e.getMessage());
        }
    }

    // Hàm tiện ích để gọi cái bảng thông báo popup cho lẹ
    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert alert = new Alert(type);
        alert.setTitle(title);
        alert.setHeaderText(null);
        alert.setContentText(content);
        alert.showAndWait();
    }
}