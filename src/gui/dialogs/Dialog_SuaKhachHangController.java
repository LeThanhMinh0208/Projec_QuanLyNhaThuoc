package gui.dialogs;

import dao.DAO_KhachHang;
import dao.DAO_NhatKyHoatDong;
import entity.KhachHang;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class Dialog_SuaKhachHangController {

    @FXML private TextField txtMa;
    @FXML private TextField txtHoTen;
    @FXML private TextField txtSdt;
    @FXML private TextField txtDiaChi;
    @FXML private TextField txtDiem;
    @FXML private Button btnHuy;
    @FXML private Button btnLuu;

    private DAO_KhachHang daoKhachHang = new DAO_KhachHang();
    private KhachHang khachHang;

    public void setKhachHangData(KhachHang kh) {
        this.khachHang = kh;
        if (kh != null) {
            txtMa.setText(kh.getMaKhachHang());
            txtHoTen.setText(kh.getHoTen());
            txtSdt.setText(kh.getSdt());
            txtDiaChi.setText(kh.getDiaChi());
            txtDiem.setText(String.valueOf(kh.getDiemTichLuy()));
            setupChangeDetection();
        }
    }

    private void setupChangeDetection() {
        String snapshot_ten = txtHoTen.getText();
        String snapshot_sdt = txtSdt.getText();
        String snapshot_diachi = txtDiaChi.getText();

        Runnable checkChanged = () -> {
            boolean changed = !txtHoTen.getText().equals(snapshot_ten) ||
                              !txtSdt.getText().equals(snapshot_sdt) ||
                              !txtDiaChi.getText().equals(snapshot_diachi);
            btnLuu.setDisable(!changed);
        };

        txtHoTen.textProperty().addListener((o, ov, nv) -> checkChanged.run());
        txtSdt.textProperty().addListener((o, ov, nv) -> checkChanged.run());
        txtDiaChi.textProperty().addListener((o, ov, nv) -> checkChanged.run());
        btnLuu.setDisable(true);
    }

    @FXML
    private void handleLuu() {
        String ten = txtHoTen.getText();
        String sdt = txtSdt.getText();
        String diaChi = txtDiaChi.getText();

        ten = utils.ValidationUtils.capitalizeName(ten);
        sdt = utils.ValidationUtils.normalizeString(sdt);
        diaChi = utils.ValidationUtils.normalizeString(diaChi);

        txtHoTen.setText(ten);
        txtSdt.setText(sdt);
        txtDiaChi.setText(diaChi);

        StringBuilder err = new StringBuilder();
        if (!utils.ValidationUtils.isValidTenKhachHang(ten)) {
			err.append("- Họ tên phải từ 2-100 ký tự và chứa ít nhất 1 chữ cái.\n");
		}
        if (!utils.ValidationUtils.isValidSdt(sdt)) {
			err.append("- Số điện thoại phải gồm 10 số và bắt đầu bằng số 0.\n");
		}
        if (!utils.ValidationUtils.isValidDiaChi(diaChi)) {
			err.append("- Địa chỉ phải từ 2-255 ký tự và chứa ít nhất 1 chữ cái hoặc số.\n");
		}

        if (err.length() > 0) {
            new Alert(Alert.AlertType.ERROR, "Dữ liệu nhập không hợp lệ:\n" + err.toString()).show();
            return;
        }

        KhachHang existingKh = daoKhachHang.getBySdt(sdt);
        if (existingKh != null && !existingKh.getMaKhachHang().equals(khachHang.getMaKhachHang())) {
            new Alert(Alert.AlertType.ERROR, "Số điện thoại này đã tồn tại trong hệ thống!").show();
            return;
        }

        khachHang.setHoTen(ten);
        khachHang.setSdt(sdt);
        khachHang.setDiaChi(diaChi);

        if (daoKhachHang.capNhatKhachHang(khachHang)) {
            DAO_NhatKyHoatDong.ghiLog("SUA", "Khách Hàng", khachHang.getMaKhachHang(), "Cập nhật thông tin khách hàng: " + khachHang.getHoTen());
            new Alert(Alert.AlertType.INFORMATION, "Cập nhật khách hàng thành công!").showAndWait();
            handleHuy();
        } else {
            new Alert(Alert.AlertType.ERROR, "Cập nhật thất bại! Vui lòng kiểm tra dữ liệu.").show();
        }
    }

    @FXML
    private void handleHuy() {
        Stage stage = (Stage) btnHuy.getScene().getWindow();
        stage.close();
    }
}
