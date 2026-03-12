package gui.dialogs;

import dao.DAO_Thuoc;
import entity.Thuoc;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class Dialog_XoaThuocController {
    @FXML private Label lblTenThuoc;
    @FXML private Button btnHuy;
    
    private DAO_Thuoc dao = new DAO_Thuoc();
    private Thuoc thuocXoa;

    public void setThuocData(Thuoc t) {
        this.thuocXoa = t;
        lblTenThuoc.setText(t.getTenThuoc() + " (" + t.getMaThuoc() + ")");
    }

    @FXML void handleXoa() {
        if(thuocXoa != null) {
            // Giả sử DAO có hàm xoaThuoc(String ma)
            // dao.xoaThuoc(thuocXoa.getMaThuoc());
            handleHuy();
        }
    }

    @FXML void handleHuy() { ((Stage) btnHuy.getScene().getWindow()).close(); }
}