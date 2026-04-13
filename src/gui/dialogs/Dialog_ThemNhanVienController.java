package gui.dialogs;

import dao.DAO_NhanVien;
import entity.NhanVien;
import gui.main.GUI_QuanLyNguoiDungController;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.text.Normalizer;
import java.util.regex.Pattern;

public class Dialog_ThemNhanVienController {
    @FXML private TextField txtHoTen, txtSdt, txtTaiKhoan;

    private DAO_NhanVien dao = new DAO_NhanVien();
    private GUI_QuanLyNguoiDungController parentController;

    public void setParentController(GUI_QuanLyNguoiDungController parent) { this.parentController = parent; }

    @FXML
    public void initialize() {
        txtHoTen.focusedProperty().addListener((obs, wasFocused, isNowFocused) -> {
            if (!isNowFocused) taoTaiKhoanTuDong();
        });
    }

    private String removeAccents(String s) {
        String temp = Normalizer.normalize(s, Normalizer.Form.NFD);
        Pattern pattern = Pattern.compile("\\p{InCombiningDiacriticalMarks}+");
        return pattern.matcher(temp).replaceAll("").replace('đ','d').replace('Đ','D');
    }

    private void taoTaiKhoanTuDong() {
        String name = txtHoTen.getText().trim();
        if (name.isEmpty()) {
            txtTaiKhoan.clear(); return;
        }
        String baseUser = removeAccents(name).replaceAll("\\s+", "").toLowerCase();
        String finalUser = baseUser;
        int count = 1;
        while (dao.kiemTraTaiKhoanTonTai(finalUser)) {
            finalUser = baseUser + count;
            count++;
        }
        txtTaiKhoan.setText(finalUser);
    }

    @FXML void handleLuu(ActionEvent event) {
        String ten = txtHoTen.getText().trim();
        String sdt = txtSdt.getText().trim();

        if (ten.isEmpty() || sdt.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng nhập đủ Họ tên và Số điện thoại!").show(); return;
        }
        if (!ten.matches("^[\\p{L}\\s]+$")) { 
            new Alert(Alert.AlertType.ERROR, "Họ tên không hợp lệ (Không chứa số hoặc ký tự đặc biệt)!").show(); return;
        }
        if (!sdt.matches("^0\\d{9}$")) { 
            new Alert(Alert.AlertType.ERROR, "Số điện thoại không hợp lệ (Phải bắt đầu bằng số 0 và đủ 10 số)!").show(); return;
        }

        // 🚨 CHẶN TRÙNG SĐT KHI THÊM MỚI 🚨
        if (dao.kiemTraSdtTonTai(sdt)) {
            new Alert(Alert.AlertType.ERROR, "Số điện thoại này đã được sử dụng bởi một nhân viên khác!").show();
            return;
        }

        taoTaiKhoanTuDong();
        String taiKhoanMoi = txtTaiKhoan.getText();

        String maDaXoa = dao.kiemTraNhanVienDaXoa(ten, sdt);
        if (maDaXoa != null) {
            if (dao.khoiPhucNhanVien(maDaXoa, taiKhoanMoi)) {
                new Alert(Alert.AlertType.INFORMATION, "Phát hiện tài khoản cũ. Đã KHÔI PHỤC thành công nhân viên!").show();
                parentController.loadData(); handleHuy(null);
            }
        } else {
            NhanVien nv = new NhanVien(dao.phatSinhMaMoi(), taiKhoanMoi, "123456", ten, "Nhân Viên", "", sdt, 1);
            if (dao.themNhanVien(nv)) {
                new Alert(Alert.AlertType.INFORMATION, "Thêm nhân viên mới thành công!").show();
                parentController.loadData(); handleHuy(null);
            }
        }
    }

    @FXML void handleHuy(ActionEvent event) { ((Stage) txtHoTen.getScene().getWindow()).close(); }
}