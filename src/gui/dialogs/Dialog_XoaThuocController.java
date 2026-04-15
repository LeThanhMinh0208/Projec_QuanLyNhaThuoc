package gui.dialogs;

import dao.DAO_Thuoc;
import entity.Thuoc;
import javafx.fxml.FXML;
import javafx.scene.control.*;
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
            new Alert(Alert.AlertType.INFORMATION, "Đã xóa thành công.").showAndWait();
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