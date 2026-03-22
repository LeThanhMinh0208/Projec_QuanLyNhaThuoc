package gui.dialogs;

import dao.DAO_DanhMucDonThuoc;
import entity.DonThuoc;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class Dialog_DonThuocController {

    @FXML private TextField txtMaDon;
    @FXML private TextField txtMaHoaDon;
    @FXML private TextField txtBacSi;
    @FXML private TextArea  txtChanDoan;
    @FXML private TextArea  txtBenhNhan;
    @FXML private TextField txtHinhAnh;
    @FXML private Label     lblTieuDe;

    private DAO_DanhMucDonThuoc dao = new DAO_DanhMucDonThuoc();
    private Runnable onSuccess;        // callback sau khi lưu thành công
    private DonThuoc donThuocSua;      // null = thêm mới, có giá trị = sửa

    // ===== 2 METHOD CÒN THIẾU =====
    public void setOnSuccess(Runnable callback) {
        this.onSuccess = callback;
    }

    public void setDonThuocSua(DonThuoc dt) {
        this.donThuocSua = dt;
        if (dt != null) {
            // Chế độ SỬA: điền sẵn dữ liệu vào form
            lblTieuDe.setText("SỬA ĐƠN THUỐC");
            txtMaDon.setText(dt.getMaDonThuoc());
            txtMaDon.setEditable(false);
            txtMaDon.setStyle("-fx-background-color: #e0e0e0;");
            txtMaHoaDon.setText(dt.getMaHoaDon());
            txtBacSi.setText(dt.getTenBacSi());
            txtChanDoan.setText(dt.getChanDoan());
            txtBenhNhan.setText(dt.getThongTinBenhNhan());
            txtHinhAnh.setText(dt.getHinhAnhDon());
        }
    }
    // ================================

    @FXML
    public void initialize() {
        // Chỉ sinh mã mới khi thêm (donThuocSua == null)
        // setDonThuocSua() sẽ được gọi SAU initialize() nếu là chế độ sửa
        try {
            String maMoi = dao.taoMaMoi();
            txtMaDon.setText(maMoi);
            txtMaDon.setEditable(false);
            txtMaDon.setStyle("-fx-background-color: #e0e0e0;");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void handleLuu() {
        // Ràng buộc 1: Mã đơn thuốc
        if (txtMaDon.getText().trim().isEmpty()) {
            showAlert("❌ Mã đơn thuốc không được để trống!"); return;
        }

        // Ràng buộc 2: Tên bác sĩ
        if (txtBacSi.getText().trim().isEmpty()) {
            showAlert("❌ Tên bác sĩ không được để trống!"); return;
        }

        // Ràng buộc 3: Thông tin bệnh nhân
        if (txtBenhNhan.getText().trim().isEmpty()) {
            showAlert("❌ Thông tin bệnh nhân không được để trống!"); return;
        }

        // Ràng buộc 4: Mã hóa đơn phải tồn tại trong DB
        String maHoaDon = txtMaHoaDon.getText().trim();
        if (!maHoaDon.isEmpty() && !dao.kiemTraMaHoaDonTonTai(maHoaDon)) {
            showAlert("❌ Mã hóa đơn '" + maHoaDon + "' không tồn tại trong hệ thống!\n"
                    + "👉 Vui lòng kiểm tra lại mã hóa đơn trong danh sách Hóa Đơn."); 
            return;
        }

        try {
            DonThuoc dt = new DonThuoc(
                txtMaDon.getText().trim(),
                maHoaDon.isEmpty() ? null : maHoaDon,
                txtBacSi.getText().trim(),
                txtChanDoan.getText().trim(),
                txtHinhAnh.getText().trim(),
                txtBenhNhan.getText().trim()
            );

            boolean ok = (donThuocSua == null) ? dao.them(dt) : dao.sua(dt);
            if (ok) {
                if (onSuccess != null) onSuccess.run();
                ((Stage) txtMaDon.getScene().getWindow()).close();
            } else {
                showAlert("❌ Lưu thất bại!\n"
                        + "👉 Kiểm tra lại:\n"
                        + "   • Mã hóa đơn có tồn tại không?\n"
                        + "   • Kết nối database có ổn không?");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert("❌ Lỗi hệ thống: " + e.getMessage());
        }
    }

    @FXML
    private void handleHuy() {
        ((Stage) txtMaDon.getScene().getWindow()).close();
    }

    @FXML
    private void handleChonAnh() {
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.getExtensionFilters().add(
            new javafx.stage.FileChooser.ExtensionFilter("Ảnh", "*.png","*.jpg","*.jpeg")
        );
        java.io.File file = fc.showOpenDialog(txtHinhAnh.getScene().getWindow());
        if (file != null) txtHinhAnh.setText(file.getAbsolutePath());
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK);
        alert.showAndWait();
    }
}