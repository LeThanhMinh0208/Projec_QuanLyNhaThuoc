package gui.dialogs;

import dao.DAO_BangGia;
import dao.DAO_DonViQuyDoi;
import dao.DAO_LoThuoc;
import entity.DonViQuyDoi;
import entity.LoThuoc;
import entity.Thuoc;
import javafx.scene.control.Button;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.List;

public class Dialog_ChonSoLuongDonViController {

    @FXML private Label lblTenThuoc;
    @FXML private ComboBox<DonViQuyDoi> cbDonVi;
    @FXML private Spinner<Integer> spSoLuong;
    @FXML private Label lblTon;
    @FXML private Label lblTonUnit;  // VĐ6: hiện tên đơn vị tùy chọn
    @FXML private Label lblCanhBao;
    @FXML private Label lblDonGia;   // label hiển thị giá tự động
    @FXML private Button btnDongY;
    @FXML private javafx.scene.image.ImageView imgThuoc;

    private final DAO_DonViQuyDoi daoDonViQuyDoi = new DAO_DonViQuyDoi();
    private final DAO_LoThuoc     daoLoThuoc     = new DAO_LoThuoc();
    private final DAO_BangGia     daoBangGia     = new DAO_BangGia();

    private Thuoc thuoc;
    private DonViQuyDoi donViChon;
    private int soLuongChon;
    private int soLuongToiDaTheoDonVi;
    private double donGiaChon;      // giá lấy từ BangGia/ChiTietBangGia
    private String maBangGiaChon;   // mã bảng giá đang áp dụng

    public void setThuoc(Thuoc thuoc) {
        this.thuoc = thuoc;
        lblTenThuoc.setText(thuoc.getTenThuoc());
        
        try {
            if (thuoc.getHinhAnh() != null && !thuoc.getHinhAnh().isEmpty()) {
                javafx.scene.image.Image img = new javafx.scene.image.Image(getClass().getResourceAsStream("/resources/images/images_thuoc/" + thuoc.getHinhAnh()));
                imgThuoc.setImage(img);
            }
        } catch (Exception e) {
            System.err.println("Không thể tải ảnh thuốc: " + thuoc.getHinhAnh());
        }

        spSoLuong.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 1000, 1));
        spSoLuong.valueProperty().addListener((obs, oldV, newV) -> validateSoLuong());
        cbDonVi.valueProperty().addListener((obs, o, n) -> capNhatTonVaGiaTheoDonVi());

        List<DonViQuyDoi> donVis = daoDonViQuyDoi.getDonViByMaThuoc(thuoc.getMaThuoc());
        cbDonVi.getItems().setAll(donVis);
        if (!donVis.isEmpty()) {
            cbDonVi.getSelectionModel().selectFirst();
            capNhatTonVaGiaTheoDonVi();
        }
    }

    private void capNhatTonVaGiaTheoDonVi() {
        DonViQuyDoi dv = cbDonVi.getSelectionModel().getSelectedItem();
        if (dv == null || thuoc == null) {
            lblTon.setText("0");
            soLuongToiDaTheoDonVi = 0;
            setGiaHienThi(null, null);
            return;
        }
        // --- Tồn kho ---
        int tongTonCoBan = daoLoThuoc.getLoThuocBanDuocByMaThuoc(thuoc.getMaThuoc())
                .stream().mapToInt(LoThuoc::getSoLuongTon).sum();
        soLuongToiDaTheoDonVi = (dv.getTyLeQuyDoi() <= 0) ? 0 : tongTonCoBan / dv.getTyLeQuyDoi();
        lblTon.setText(String.valueOf(soLuongToiDaTheoDonVi));

        // VĐ6: Cập nhật tên đơn vị tùy theo ComboBox
        if (lblTonUnit != null && dv.getTenDonVi() != null) {
            lblTonUnit.setText(dv.getTenDonVi());
        }

        // --- Lấy giá từ BangGia ---
        Object[] giaInfo = daoBangGia.getGiaVaMaBangGia(dv.getMaQuyDoi());
        if (giaInfo != null) {
            setGiaHienThi((BigDecimal) giaInfo[0], (String) giaInfo[1]);
        } else {
            setGiaHienThi(null, null);
        }
        validateSoLuong();
    }

    private void setGiaHienThi(BigDecimal gia, String maBG) {
        donGiaChon    = (gia != null) ? gia.doubleValue() : 0;
        maBangGiaChon = maBG;
        if (lblDonGia != null) {
            if (gia != null) {
                lblDonGia.setText(String.format("%,.0f ₫", donGiaChon));
                lblDonGia.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
            } else {
                lblDonGia.setText("⚠ Chưa có bảng giá hiệu lực");
                lblDonGia.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
            }
        }
        if (btnDongY != null) {
            btnDongY.setDisable(gia == null);
        }
    }

    private void validateSoLuong() {
        if (soLuongToiDaTheoDonVi <= 0) {
            lblCanhBao.setText("Không còn tồn kho cho đơn vị đã chọn.");
            return;
        }
        Integer valueObj = spSoLuong.getValue();
        if (valueObj == null) return;
        
        int value = valueObj;
        if (value > soLuongToiDaTheoDonVi) {
            lblCanhBao.setText("Số lượng vượt quá tồn cho đơn vị này.");
        } else {
            lblCanhBao.setText("");
        }
    }

    @FXML
    private void handleDongY() {
        DonViQuyDoi dv = cbDonVi.getSelectionModel().getSelectedItem();
        if (dv == null || soLuongToiDaTheoDonVi <= 0 || maBangGiaChon == null) {
            return;
        }
        Integer slObj = spSoLuong.getValue();
        if (slObj == null) return;
        int sl = slObj;
        if (sl <= 0 || sl > soLuongToiDaTheoDonVi) {
            return;
        }
        this.donViChon = dv;
        this.soLuongChon = sl;
        close();
    }

    @FXML
    private void handleHuy() {
        this.donViChon = null;
        this.soLuongChon = 0;
        close();
    }

    private void close() {
        Stage stage = (Stage) lblTenThuoc.getScene().getWindow();
        stage.close();
    }

    public DonViQuyDoi getDonViChon() { return donViChon; }
    public int getSoLuongChon()       { return soLuongChon; }
    public double getDonGiaChon()     { return donGiaChon; }
    public String getMaBangGiaChon()  { return maBangGiaChon; }
}