package gui.dialogs;

import dao.DAO_KhachHang;
import dao.DAO_NhatKyHoatDong;
import entity.KhachHang;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class Dialog_XoaKhachHangController {

    @FXML private Label lblTenKhachHang;
    @FXML private Button btnHuy;
    @FXML private Button btnXoa;

    private DAO_KhachHang daoKhachHang = new DAO_KhachHang();
    private KhachHang khachHangCanXoa;

    public void setKhachHangData(KhachHang kh) {
        this.khachHangCanXoa = kh;
        if (kh != null) {
            lblTenKhachHang.setText(kh.getHoTen());
        }
    }

    @FXML
    private void handleXoa() {
        if (khachHangCanXoa == null) {
            closeDialog();
            return;
        }

        boolean thanhCong = daoKhachHang.xoaKhachHang(khachHangCanXoa.getMaKhachHang());

        if (thanhCong) {
            DAO_NhatKyHoatDong.ghiLog("XOA", "Khách Hàng", khachHangCanXoa.getMaKhachHang(), "Xóa mềm khách hàng: " + khachHangCanXoa.getHoTen());
            new Alert(Alert.AlertType.INFORMATION, "Xóa khách hàng thành công!").showAndWait();
        } else {
            new Alert(Alert.AlertType.ERROR, "Có lỗi xảy ra! Vui lòng thử lại.").showAndWait();
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
