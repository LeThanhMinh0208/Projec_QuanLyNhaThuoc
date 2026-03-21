package gui.dialogs;

import entity.KhachHang;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class Dialog_ThemKhachHangController {

    @FXML private Label lblTitle;
    @FXML private TextField txtMa;
    @FXML private TextField txtHoTen;
    @FXML private TextField txtSdt;
    @FXML private TextField txtDiaChi;
    @FXML private TextField txtDiem;

    @FXML private Label lblErrHoTen;
    @FXML private Label lblErrSdt;
    @FXML private Label lblErrDiaChi;

    private KhachHang khachHang;     // Holds existing KH if edit mode
    private KhachHang resultKhachHang; // Result passed back
    private boolean isEditMode = false;

    public void setKhachHang(KhachHang khachHang, String nextIdSeq) {
        this.khachHang = khachHang;
        if (khachHang != null) {
            isEditMode = true;
            lblTitle.setText("SỬA THÔNG TIN KHÁCH HÀNG");
            txtMa.setText(khachHang.getMaKhachHang());
            txtHoTen.setText(khachHang.getHoTen());
            txtSdt.setText(khachHang.getSdt());
            txtDiaChi.setText(khachHang.getDiaChi());
            txtDiem.setText(String.valueOf(khachHang.getDiemTichLuy()));
        } else {
            isEditMode = false;
            lblTitle.setText("THÊM KHÁCH HÀNG MỚI");
            txtMa.setText(nextIdSeq);
        }
    }

    public void setSdt(String sdt) {
        if (sdt != null && !sdt.trim().isEmpty()) {
            txtSdt.setText(sdt.trim());
        }
    }

    public KhachHang getResultKhachHang() {
        return resultKhachHang;
    }

    @FXML
    void handleLuu(ActionEvent event) {
        boolean valid = true;
        
        String ten = txtHoTen.getText().trim();
        String sdt = txtSdt.getText().trim();
        String diaChi = txtDiaChi.getText().trim();

        if (ten.isEmpty()) {
            lblErrHoTen.setText("Họ tên không được để trống!");
            lblErrHoTen.setVisible(true);
            lblErrHoTen.setManaged(true);
            valid = false;
        } else {
            lblErrHoTen.setVisible(false);
            lblErrHoTen.setManaged(false);
        }

        if (sdt.isEmpty()) {
            lblErrSdt.setText("SĐT không được để trống!");
            lblErrSdt.setVisible(true);
            lblErrSdt.setManaged(true);
            valid = false;
        } else if (!sdt.matches("^0\\d{9}$")) {
            lblErrSdt.setText("SĐT phải 10 số và bắt đầu bằng số 0!");
            lblErrSdt.setVisible(true);
            lblErrSdt.setManaged(true);
            valid = false;
        } else {
            // Kiểm tra trùng SĐT
            dao.DAO_KhachHang daoKh = new dao.DAO_KhachHang();
            KhachHang existingKh = daoKh.getBySdt(sdt);
            if (existingKh != null && (!isEditMode || !existingKh.getMaKhachHang().equals(khachHang.getMaKhachHang()))) {
                lblErrSdt.setText("SĐT này đã tồn tại trong hệ thống!");
                lblErrSdt.setVisible(true);
                lblErrSdt.setManaged(true);
                valid = false;
            } else {
                lblErrSdt.setVisible(false);
                lblErrSdt.setManaged(false);
            }
        }
        
        if (diaChi.isEmpty()) {
            lblErrDiaChi.setText("Địa chỉ không được để trống!");
            lblErrDiaChi.setVisible(true);
            lblErrDiaChi.setManaged(true);
            valid = false;
        } else {
            lblErrDiaChi.setVisible(false);
            lblErrDiaChi.setManaged(false);
        }

        if (!valid) {
            Stage stage = (Stage) txtMa.getScene().getWindow();
            stage.sizeToScene();
            return;
        }

        if (isEditMode) {
            khachHang.setHoTen(ten);
            khachHang.setSdt(sdt);
            khachHang.setDiaChi(diaChi);
            this.resultKhachHang = khachHang;
        } else {
            this.resultKhachHang = new KhachHang(txtMa.getText(), ten, sdt, diaChi, 0);
        }
        
        closeDialog();
    }

    @FXML
    void handleDong(ActionEvent event) {
        this.resultKhachHang = null;
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) txtMa.getScene().getWindow();
        stage.close();
    }
}
