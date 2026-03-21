package gui.main;

import dao.DAO_DanhMucDonThuoc;
import entity.DonThuoc;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;

public class GUI_DialogDonThuocController {

    @FXML private Label     lblTieuDe;
    @FXML private TextField txtMaDon, txtBacSi, txtHinhAnh;
    @FXML private TextArea  txtChanDoan, txtBenhNhan;
    @FXML private Button    btnLuu;

    private final DAO_DanhMucDonThuoc dao = new DAO_DanhMucDonThuoc();
    private DonThuoc donThuocSua = null;
    private Runnable onSuccess;

    public void setOnSuccess(Runnable r) { this.onSuccess = r; }

    @FXML
    public void initialize() {
        try {
            String maMoi = dao.taoMaMoi();
            txtMaDon.setText(maMoi);
            txtMaDon.setDisable(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void setDonThuocSua(DonThuoc dt) {
        this.donThuocSua = dt;
        lblTieuDe.setText("CẬP NHẬT ĐƠN THUỐC");
        btnLuu.setText("💾  Cập Nhật");

        txtMaDon.setText(dt.getMaDonThuoc());
        txtMaDon.setDisable(true);
        txtBacSi.setText(dt.getTenBacSi());
        txtChanDoan.setText(dt.getChanDoan());
        txtBenhNhan.setText(dt.getThongTinBenhNhan());
        txtHinhAnh.setText(dt.getHinhAnhDon());
    }

    @FXML
    private void handleChonAnh() {
        FileChooser fc = new FileChooser();
        fc.setTitle("Chọn ảnh đơn thuốc");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Ảnh", "*.png", "*.jpg", "*.jpeg"));
        File f = fc.showOpenDialog(btnLuu.getScene().getWindow());
        if (f != null) txtHinhAnh.setText(f.toURI().toString());
    }

    @FXML
    private void handleLuu() {
        if (txtMaDon.getText().trim().isEmpty()) {
            showWarn("Mã đơn thuốc không được để trống!"); return;
        }
        if (txtBacSi.getText().trim().isEmpty()) {
            showWarn("Tên bác sĩ không được để trống!"); return;
        }
        if (txtBenhNhan.getText().trim().isEmpty()) {
            showWarn("Thông tin bệnh nhân không được để trống!"); return;
        }

        try {
            DonThuoc dt = new DonThuoc(
                txtMaDon.getText().trim(),
                "",
                txtBacSi.getText().trim(),
                txtChanDoan.getText().trim(),
                txtHinhAnh.getText().trim(),
                txtBenhNhan.getText().trim()
            );

            boolean ok = (donThuocSua == null) ? dao.them(dt) : dao.sua(dt);
            if (ok) {
                if (onSuccess != null) onSuccess.run();
                close();
            } else {
                showWarn("Lưu thất bại! Kiểm tra lại dữ liệu.");
            }
        } catch (IllegalArgumentException e) {
            showWarn(e.getMessage());
        }
    }

    @FXML
    private void handleHuy() { close(); }

    private void close() { ((Stage) btnLuu.getScene().getWindow()).close(); }

    private void showWarn(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }
}