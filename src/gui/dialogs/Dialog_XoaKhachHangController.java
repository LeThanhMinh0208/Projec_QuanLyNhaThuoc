package gui.dialogs;

import dao.DAO_KhachHang;
import entity.KhachHang;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class Dialog_XoaKhachHangController {

    @FXML private Label lblTenKhachHang;
    @FXML private Button btnHuy;

    private DAO_KhachHang daoKhachHang = new DAO_KhachHang();
    private KhachHang khachHangCanXoa;

    public void setKhachHangData(KhachHang kh) {
        this.khachHangCanXoa = kh;
        if (kh != null) {
            lblTenKhachHang.setText(kh.getHoTen());
        } else {
            lblTenKhachHang.setText("[Không có dữ liệu]");
        }
    }

    @FXML
    private void handleXoa() {
        if (khachHangCanXoa == null) {
            new Alert(Alert.AlertType.WARNING, "Không có khách hàng nào được chọn để xóa!").show();
            closeDialog();
            return;
        }

        boolean xoaThanhCong = daoKhachHang.xoaKhachHang(khachHangCanXoa.getMaKhachHang());

        if (xoaThanhCong) {
            new Alert(Alert.AlertType.INFORMATION, "Đã xóa khách hàng thành công!").show();
        } else {
            new Alert(Alert.AlertType.ERROR, 
                      "Không thể xóa khách hàng!\n" +
                      "Có thể khách hàng này đang có dữ liệu hóa đơn giao dịch, không thể xóa!").show();
        }
        
        closeDialog();
    }

    @FXML
    private void handleHuy() {
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) btnHuy.getScene().getWindow();
        stage.close();
    }
}
