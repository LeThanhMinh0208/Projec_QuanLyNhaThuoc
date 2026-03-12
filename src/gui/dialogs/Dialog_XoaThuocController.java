package gui.dialogs;

import dao.DAO_Thuoc;
import entity.Thuoc;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class Dialog_XoaThuocController {
    @FXML private Label lblTenThuoc;
    @FXML private Button btnHuy;
    
    private DAO_Thuoc daoThuoc = new DAO_Thuoc();
    private Thuoc thuocCanXoa;

    public void setThuocData(Thuoc data) {
        this.thuocCanXoa = data;
        lblTenThuoc.setText(data.getTenThuoc());
    }

    @FXML
    private void handleXoa() {
        if (daoThuoc.xoaThuoc(thuocCanXoa.getMaThuoc())) {
            // Thông báo và đóng cửa sổ
            ((Stage) lblTenThuoc.getScene().getWindow()).close();
        }
    }

    @FXML void handleHuy() { ((Stage) btnHuy.getScene().getWindow()).close(); }
}