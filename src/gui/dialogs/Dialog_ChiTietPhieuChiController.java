package gui.dialogs;

import entity.PhieuChi;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class Dialog_ChiTietPhieuChiController {

    @FXML private Label lblMaPhieu, lblNgayChi, lblNhanVien, lblNhaCungCap, lblTongTien, lblHinhThuc;
    @FXML private TextArea txtGhiChu;
    @FXML private Button btnDong;

    private PhieuChi phieuHienTai; // Lưu lại để dùng khi In
    private DecimalFormat df = new DecimalFormat("#,### VNĐ");
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public void setPhieuChi(PhieuChi pc) {
        this.phieuHienTai = pc;
        
        lblMaPhieu.setText(pc.getMaPhieuChi());
        lblNgayChi.setText(pc.getNgayChi() != null ? sdf.format(pc.getNgayChi()) : "---");
        lblNhanVien.setText(pc.getNhanVien() != null ? pc.getNhanVien().getHoTen() : "---");
        lblNhaCungCap.setText(pc.getNhaCungCap() != null ? pc.getNhaCungCap().getTenNhaCungCap() : "---");
        lblTongTien.setText(df.format(pc.getTongTienChi()));
        
        // VIỆT HÓA HÌNH THỨC Ở ĐÂY SẾP ƠI
        String ht = pc.getHinhThucChi();
        if ("CHUYEN_KHOAN".equals(ht)) lblHinhThuc.setText("Chuyển Khoản");
        else if ("THE".equals(ht)) lblHinhThuc.setText("Thẻ");
        else lblHinhThuc.setText("Tiền Mặt");

        txtGhiChu.setText(pc.getGhiChu() != null ? pc.getGhiChu() : "Không có ghi chú");
    }

    @FXML
    void handleInPhieu() {
        if (phieuHienTai != null) {
           
            service.Print_PhieuChi.inPhieu(phieuHienTai);
        } else {
            utils.AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Dữ liệu phiếu chi trống, không thể in!");
        }
    }

    @FXML
    void handleDong() {
        ((Stage) btnDong.getScene().getWindow()).close();
    }
}