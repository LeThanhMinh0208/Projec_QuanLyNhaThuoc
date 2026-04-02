package gui.dialogs;

import dao.DAO_PhieuChi;
import entity.NhaCungCap;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import utils.AlertUtils;
import java.text.DecimalFormat;

public class Dialog_LapPhieuChiController {

    @FXML private Label lblTenNCC;
    @FXML private Label lblCongNo;
    @FXML private TextField txtSoTien;
    @FXML private CheckBox chkTatToan;
    @FXML private ComboBox<String> cbHinhThuc;
    @FXML private TextArea txtGhiChu;
    @FXML private Button btnHuy;

    private NhaCungCap nccHienTai;
    private DAO_PhieuChi daoPhieuChi = new DAO_PhieuChi();
    private DecimalFormat df = new DecimalFormat("#,###");

    @FXML
    public void initialize() {
        // Nạp dữ liệu cho ComboBox
        cbHinhThuc.getItems().addAll("Tiền Mặt", "Chuyển Khoản", "Thẻ");
        cbHinhThuc.getSelectionModel().selectFirst();

        // LOGIC TIỆN ÍCH: Tick "Tất toán" là auto điền full tiền nợ
        chkTatToan.selectedProperty().addListener((obs, oldVal, isChecked) -> {
            if (isChecked && nccHienTai != null) {
                // Điền số tiền nguyên bản (không có dấu phẩy để hệ thống dễ đọc)
                txtSoTien.setText(String.format("%.0f", nccHienTai.getCongNo()));
                txtSoTien.setEditable(false); // Khóa lại không cho sửa bậy
                txtSoTien.setStyle("-fx-background-color: #d1fae5; -fx-font-size: 16px; -fx-font-weight: bold;"); // Đổi màu xanh
            } else {
                txtSoTien.clear();
                txtSoTien.setEditable(true);
                txtSoTien.setStyle("-fx-background-color: white; -fx-font-size: 16px; -fx-font-weight: bold;");
            }
        });
    }

    public void setNhaCungCap(NhaCungCap ncc) {
        this.nccHienTai = ncc;
        lblTenNCC.setText(ncc.getTenNhaCungCap());
        lblCongNo.setText(df.format(ncc.getCongNo()) + " VNĐ");
    }

    @FXML
    void handleXacNhan(ActionEvent event) {
        String tienStr = txtSoTien.getText().replaceAll("[,\\s]", "");
        
        if (tienStr.isEmpty()) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập số tiền cần chi!");
            return;
        }

        double soTienChi = 0;
        try {
            soTienChi = Double.parseDouble(tienStr);
            if (soTienChi <= 0) throw new Exception();
        } catch (Exception e) {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Số tiền chi phải là số lớn hơn 0!");
            return;
        }

        if (soTienChi > nccHienTai.getCongNo()) {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi Nghiệp Vụ", 
                "Số tiền chi vượt quá công nợ hiện tại!");
            return;
        }

        // ĐÃ FIX: Lấy mã nhân viên thực tế từ UserSession của sếp
        String maNhanVienDangNhap = utils.UserSession.getInstance().getUser().getMaNhanVien(); 

        // Bộ phiên dịch hình thức chi
        String hinhThucUI = cbHinhThuc.getValue();
        String hinhThucDB = "TIEN_MAT"; 
        if (hinhThucUI != null) {
            if (hinhThucUI.equals("Chuyển Khoản")) hinhThucDB = "CHUYEN_KHOAN";
            else if (hinhThucUI.equals("Thẻ")) hinhThucDB = "THE";
        }

        // Lưu xuống Database
        boolean thanhCong = daoPhieuChi.lapPhieuChi(
            nccHienTai.getMaNhaCungCap(), 
            maNhanVienDangNhap, 
            soTienChi, 
            hinhThucDB, 
            txtGhiChu.getText()
        );

        if (thanhCong) {
            utils.AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã tạo Phiếu Chi thành công!");
            handleHuy(null); 
        } else {
            utils.AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Thao tác thất bại!");
        }
    }
    @FXML
    void handleHuy(ActionEvent event) {
        Stage stage = (Stage) btnHuy.getScene().getWindow();
        stage.close();
    }
}