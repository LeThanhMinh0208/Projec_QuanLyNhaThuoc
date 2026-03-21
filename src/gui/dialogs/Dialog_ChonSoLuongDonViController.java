package gui.dialogs;

import dao.DAO_DonViQuyDoi;
import dao.DAO_LoThuoc;
import entity.DonViQuyDoi;
import entity.LoThuoc;
import entity.Thuoc;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.stage.Stage;

import java.util.List;

public class Dialog_ChonSoLuongDonViController {

    @FXML
    private Label lblTenThuoc;
    @FXML
    private ComboBox<DonViQuyDoi> cbDonVi;
    @FXML
    private Spinner<Integer> spSoLuong;
    @FXML
    private Label lblTon;
    @FXML
    private Label lblCanhBao;
    @FXML
    private javafx.scene.image.ImageView imgThuoc;

    private final DAO_DonViQuyDoi daoDonViQuyDoi = new DAO_DonViQuyDoi();
    private final DAO_LoThuoc daoLoThuoc = new DAO_LoThuoc();

    private Thuoc thuoc;
    private DonViQuyDoi donViChon;
    private int soLuongChon;
    private int soLuongToiDaTheoDonVi;

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
        cbDonVi.valueProperty().addListener((obs, o, n) -> capNhatTonTheoDonVi());

        List<DonViQuyDoi> donVis = daoDonViQuyDoi.getDonViByMaThuoc(thuoc.getMaThuoc());
        cbDonVi.getItems().setAll(donVis);
        if (!donVis.isEmpty()) {
            cbDonVi.getSelectionModel().selectFirst();
            capNhatTonTheoDonVi();
        }
    }

    private void capNhatTonTheoDonVi() {
        DonViQuyDoi dv = cbDonVi.getSelectionModel().getSelectedItem();
        if (dv == null || thuoc == null) {
            lblTon.setText("0");
            soLuongToiDaTheoDonVi = 0;
            return;
        }
        int tongTonCoBan = daoLoThuoc.getLoThuocBanDuocByMaThuoc(thuoc.getMaThuoc())
                .stream().mapToInt(LoThuoc::getSoLuongTon).sum();
        if (dv.getTyLeQuyDoi() <= 0) {
            soLuongToiDaTheoDonVi = 0;
        } else {
            soLuongToiDaTheoDonVi = tongTonCoBan / dv.getTyLeQuyDoi();
        }
        lblTon.setText(String.valueOf(soLuongToiDaTheoDonVi));
        validateSoLuong();
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
        if (dv == null || soLuongToiDaTheoDonVi <= 0) {
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

    public DonViQuyDoi getDonViChon() {
        return donViChon;
    }

    public int getSoLuongChon() {
        return soLuongChon;
    }
}

