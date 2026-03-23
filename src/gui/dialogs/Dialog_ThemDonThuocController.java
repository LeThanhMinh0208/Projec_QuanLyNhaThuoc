package gui.dialogs;

import entity.DonThuoc;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class Dialog_ThemDonThuocController {

    @FXML private Label lblTitle;
    @FXML private TextField txtMaDon;
    @FXML private TextField txtTenBacSi;
    @FXML private TextField txtBenhNhan;
    @FXML private TextField txtChanDoan;

    @FXML private Label lblErrBacSi;
    @FXML private Label lblErrBenhNhan;

    private DonThuoc resultDonThuoc;
    private boolean isEdit = false;

    public void setDonThuoc(DonThuoc dt, String nextMa) {
        if (dt != null) {
            isEdit = true;
            lblTitle.setText("SỬA THÔNG TIN ĐƠN THUỐC");
            txtMaDon.setText(dt.getMaDonThuoc());
            txtTenBacSi.setText(dt.getTenBacSi());
            txtBenhNhan.setText(dt.getThongTinBenhNhan());
            txtChanDoan.setText(dt.getChanDoan());
        } else {
            isEdit = false;
            lblTitle.setText("THÊM THÔNG TIN ĐƠN THUỐC");
            txtMaDon.setText(nextMa);
        }
    }

    public DonThuoc getResultDonThuoc() {
        return resultDonThuoc;
    }

    @FXML
    void handleLuu(ActionEvent event) {
        boolean valid = true;
        
        String bsi = txtTenBacSi.getText().trim();
        String bnhan = txtBenhNhan.getText().trim();
        String cdoan = txtChanDoan.getText().trim();

        if (bsi.isEmpty()) {
            lblErrBacSi.setText("Tên bác sĩ không được để trống!");
            lblErrBacSi.setVisible(true);
            lblErrBacSi.setManaged(true);
            valid = false;
        } else {
            lblErrBacSi.setVisible(false);
            lblErrBacSi.setManaged(false);
        }

        if (bnhan.isEmpty()) {
            lblErrBenhNhan.setText("Thông tin bệnh nhân không được để trống!");
            lblErrBenhNhan.setVisible(true);
            lblErrBenhNhan.setManaged(true);
            valid = false;
        } else {
            lblErrBenhNhan.setVisible(false);
            lblErrBenhNhan.setManaged(false);
        }

        if (!valid) return;

        resultDonThuoc = new DonThuoc();
        resultDonThuoc.setMaDonThuoc(txtMaDon.getText());
        resultDonThuoc.setTenBacSi(bsi);
        resultDonThuoc.setThongTinBenhNhan(bnhan);
        resultDonThuoc.setChanDoan(cdoan);
        
        closeDialog();
    }

    @FXML
    void handleDong(ActionEvent event) {
        resultDonThuoc = null;
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) txtMaDon.getScene().getWindow();
        stage.close();
    }
}
