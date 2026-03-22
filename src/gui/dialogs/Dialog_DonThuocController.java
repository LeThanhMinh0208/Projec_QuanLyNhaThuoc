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
            lblTieuDe.setText("SỬA ĐƠN THUỐC");
            txtMaDon.setText(dt.getMaDonThuoc());
            txtMaDon.setEditable(false);
            txtMaDon.setStyle("-fx-background-color: #e0e0e0;");
            txtMaHoaDon.setText(dt.getMaHoaDon());
            txtMaHoaDon.setEditable(false); // 🔒 Khóa không cho sửa
            txtMaHoaDon.setStyle("-fx-background-color: #e0e0e0;");
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
        StringBuilder loi = new StringBuilder();

        if (txtMaDon.getText().trim().isEmpty())
            loi.append("❌ Mã đơn thuốc không được để trống!\n");

        if (txtBacSi.getText().trim().isEmpty())
            loi.append("❌ Tên bác sĩ không được để trống!\n");

        if (txtChanDoan.getText().trim().isEmpty())
            loi.append("❌ Chẩn đoán không được để trống!\n");

        if (txtBenhNhan.getText().trim().isEmpty())
            loi.append("❌ Thông tin bệnh nhân không được để trống!\n");

        String maHoaDon = txtMaHoaDon.getText().trim();
        if (donThuocSua == null) {
            if (maHoaDon.isEmpty()) {
                loi.append("❌ Mã hóa đơn không được để trống!\n");
            } else if (!dao.kiemTraMaHoaDonTonTai(maHoaDon)) {
                loi.append("❌ Mã hóa đơn '" + maHoaDon + "' không tồn tại trong hệ thống!\n");
            }
        }

        // Nếu có lỗi thì hiện tất cả 1 lần
        if (loi.length() > 0) {
            showAlert(loi.toString());
            return;
        }

        // Chế độ SỬA: dùng lại mã hóa đơn gốc
        if (donThuocSua != null) {
            maHoaDon = donThuocSua.getMaHoaDon();
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
                showAlert("❌ Lưu thất bại!\n👉 Kiểm tra lại kết nối database.");
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