package gui.main;

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

import java.sql.*;

public class GUI_DangNhapController {

    @FXML private TextField txtTenDangNhap;
    @FXML private PasswordField txtMatKhau;

    // ĐỔI thông tin kết nối cho đúng máy mày nha!
    private static final String DB_URL  = "jdbc:sqlserver://localhost:1433;databaseName=QuanLyNhaThuoc_LongNguyen;encrypt=true;trustServerCertificate=true;";
    private static final String DB_USER = "sa";
    private static final String DB_PASS = "sapassword";

    @FXML
    void handleDangNhap(ActionEvent event) {
        String taiKhoan = txtTenDangNhap.getText().trim();
        String matKhau  = txtMatKhau.getText();

        if (taiKhoan.isEmpty() || matKhau.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập đầy đủ tài khoản và mật khẩu!");
            return;
        }

        try (Connection conn = DriverManager.getConnection(DB_URL, DB_USER, DB_PASS)) {
            String sql = "SELECT hoTen, chucVu FROM NhanVien WHERE tenDangNhap = ? AND matKhau = ?";
            PreparedStatement pst = conn.prepareStatement(sql);
            pst.setString(1, taiKhoan);
            pst.setString(2, matKhau);
            ResultSet rs = pst.executeQuery();

            if (rs.next()) {
                String hoTen  = rs.getString("hoTen");
                String chucVu = rs.getString("chucVu");

                // Truyền thông tin nhân viên sang màn hình trang chủ
                GUI_TrangChuController.setNhanVienInfo(hoTen, chucVu);

                // Đóng cửa sổ đăng nhập
                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                currentStage.close();

                // Mở trang chủ
                FXMLLoader loader = new FXMLLoader(getClass().getResource("GUI_TrangChu.fxml"));
                Parent root = loader.load();

                Stage trangChuStage = new Stage();
                trangChuStage.setTitle("Long Nguyên Pharma  —  " + chucVu + ": " + hoTen);
                trangChuStage.setScene(new Scene(root, 1280, 720));
                trangChuStage.setMinWidth(1024);
                trangChuStage.setMinHeight(600);
                trangChuStage.show();

            } else {
                showAlert(Alert.AlertType.ERROR, "Đăng nhập thất bại", "Tài khoản hoặc mật khẩu không đúng!");
                txtMatKhau.clear();
                txtMatKhau.requestFocus();
            }

        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi kết nối",
                    "Không kết nối được database!\nKiểm tra SQL Server có đang chạy không.\n\nChi tiết: " + e.getMessage());
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
