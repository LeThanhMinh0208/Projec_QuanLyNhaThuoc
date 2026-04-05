package gui.dialogs;

import dao.DAO_NhaCungCap;
import entity.NhaCungCap;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class Dialog_XoaNhaCungCapController {

    @FXML private Label lblTenNcc;
    @FXML private Button btnHuy;
    @FXML private Button btnXoa;

    private DAO_NhaCungCap daoNCC = new DAO_NhaCungCap();
    private NhaCungCap nhaCungCapCanXoa;

    public void setNhaCungCapData(NhaCungCap ncc) {
        this.nhaCungCapCanXoa = ncc;
        if (ncc != null) {
            lblTenNcc.setText(ncc.getTenNhaCungCap());
        }
    }

    @FXML
    private void handleXoa() {
        if (nhaCungCapCanXoa == null) {
            closeDialog();
            return;
        }

        boolean thanhCong = daoNCC.xoaNhaCungCap(nhaCungCapCanXoa.getMaNhaCungCap());

        if (thanhCong) {
            new Alert(Alert.AlertType.INFORMATION, "Đã xóa thành công.").showAndWait();
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