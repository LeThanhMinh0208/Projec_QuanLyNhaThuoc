package gui.dialogs;

import dao.DAO_NhanVien;
import entity.NhanVien;
import gui.main.GUI_QuanLyNguoiDungController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class Dialog_SuaNhanVienController {
    @FXML private TextField txtMaNV, txtHoTen, txtSdt, txtTaiKhoan;
    @FXML private Button btnLuu, btnResetPass;

    private DAO_NhanVien dao = new DAO_NhanVien();
    private GUI_QuanLyNguoiDungController parentController;
    private NhanVien nvDangSua;

    // Cờ đánh dấu đã bấm reset mật khẩu chưa
    private boolean isResetPassword = false;

    public void setParentController(GUI_QuanLyNguoiDungController parent) { this.parentController = parent; }

    public void setNhanVien(NhanVien nv) {
        this.nvDangSua = nv;
        txtMaNV.setText(nv.getMaNhanVien());
        txtHoTen.setText(nv.getHoTen());
        txtSdt.setText(nv.getSdt());
        txtTaiKhoan.setText(nv.getTenDangNhap());

        // LẮNG NGHE SỰ THAY ĐỔI DỮ LIỆU ĐỂ MỞ KHÓA NÚT LƯU
        txtHoTen.textProperty().addListener((obs, oldV, newV) -> kiemTraThayDoi());
        txtSdt.textProperty().addListener((obs, oldV, newV) -> kiemTraThayDoi());
    }

    // 🚨 HÀM KIỂM TRA: CHỈ KHI NÀO CÓ SỬA CHỮ HOẶC BẤM RESET MỚI ĐƯỢC LƯU 🚨
    private void kiemTraThayDoi() {
        boolean isNameChanged = !txtHoTen.getText().trim().equals(nvDangSua.getHoTen());
        boolean isPhoneChanged = !txtSdt.getText().trim().equals(nvDangSua.getSdt());

        // Bật sáng nút LƯU nếu có bất kỳ thay đổi nào
        btnLuu.setDisable(!(isNameChanged || isPhoneChanged || isResetPassword));
    }

    @FXML void handleResetPass(ActionEvent event) {
        isResetPassword = true;
        btnResetPass.setText("✅ Đã Reset");
        btnResetPass.setDisable(true); // Ngăn bấm nhiều lần
        kiemTraThayDoi(); // Gọi lại hàm check để mở khóa nút LƯU
    }

    @FXML void handleLuu(ActionEvent event) {
        String ten = txtHoTen.getText().trim();
        String sdt = txtSdt.getText().trim();

        if (ten.isEmpty() || sdt.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Họ tên và SĐT không được để trống!").show(); return;
        }
        if (!ten.matches("^[\\p{L}\\s]+$")) {
            new Alert(Alert.AlertType.ERROR, "Họ tên không hợp lệ!").show(); return;
        }
        if (!sdt.matches("^0\\d{9}$")) {
            new Alert(Alert.AlertType.ERROR, "Số điện thoại không hợp lệ!").show(); return;
        }

        nvDangSua.setHoTen(ten);
        nvDangSua.setSdt(sdt);

        if (dao.capNhatThongTin(nvDangSua, isResetPassword)) {
            new Alert(Alert.AlertType.INFORMATION, "Cập nhật thành công!").show();
            parentController.loadData();
            handleHuy(null);
        } else {
            new Alert(Alert.AlertType.ERROR, "Cập nhật thất bại!").show();
        }
    }

    @FXML void handleHuy(ActionEvent event) { ((Stage) txtHoTen.getScene().getWindow()).close(); }
}