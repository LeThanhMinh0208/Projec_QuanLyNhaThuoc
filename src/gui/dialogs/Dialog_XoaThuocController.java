package gui.dialogs;

import dao.DAO_Thuoc;
import dao.DAO_NhatKyHoatDong;
import entity.Thuoc;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class Dialog_XoaThuocController {
    @FXML private Label lblTenThuoc;
    @FXML private Button btnHuy;
    @FXML private Button btnXoa;

    private DAO_Thuoc daoThuoc = new DAO_Thuoc();
    private Thuoc thuocCanXoa;

    public void setThuocData(Thuoc data) {
        this.thuocCanXoa = data;
        lblTenThuoc.setText(data.getTenThuoc());
    }

    @FXML
    private void handleXoa() {
        if (thuocCanXoa == null) {
            closeDialog();
            return;
        }

        boolean thanhCong = daoThuoc.xoaThuoc(thuocCanXoa.getMaThuoc());

        if (thanhCong) {
            DAO_NhatKyHoatDong.ghiLog("XOA", "Thuốc", thuocCanXoa.getMaThuoc(), "Xóa mềm thuốc: " + thuocCanXoa.getTenThuoc());
            new Alert(Alert.AlertType.INFORMATION, "Xóa thuốc thành công!").showAndWait();
        } else {
            new Alert(Alert.AlertType.ERROR, "Có lỗi xảy ra! Vui lòng thử lại.").showAndWait();
        }
        closeDialog();
    }

    @FXML
    void handleHuy() {
        closeDialog();
    }

    private void closeDialog() {
        ((Stage) btnHuy.getScene().getWindow()).close();
    }
}