package gui.dialogs;

import entity.KhachHang;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class Dialog_ThemKhachHangController {

    @FXML private TextField txtMa;
    @FXML private TextField txtHoTen;
    @FXML private TextField txtSdt;
    @FXML private TextField txtDiaChi;
    @FXML private TextField txtDiem;

    @FXML private Label lblErrHoTen;
    @FXML private Label lblErrSdt;
    @FXML private Label lblErrDiaChi;

    private KhachHang resultKhachHang; // Result passed back

    public void setMaKhachHang(String nextIdSeq) {
        txtMa.setText(nextIdSeq);
        txtDiem.setText("0");
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
        
        String ten = txtHoTen.getText();
        String sdt = txtSdt.getText();
        String diaChi = txtDiaChi.getText();
        
        ten = utils.ValidationUtils.capitalizeName(ten);
        sdt = utils.ValidationUtils.normalizeString(sdt);
        diaChi = utils.ValidationUtils.normalizeString(diaChi);

        txtHoTen.setText(ten);
        txtSdt.setText(sdt);
        txtDiaChi.setText(diaChi);

        if (!utils.ValidationUtils.isValidTenKhachHang(ten)) {
            lblErrHoTen.setText("Họ tên phải từ 2-100 ký tự và chứa ít nhất 1 chữ cái!");
            lblErrHoTen.setVisible(true);
            lblErrHoTen.setManaged(true);
            valid = false;
        } else {
            lblErrHoTen.setVisible(false);
            lblErrHoTen.setManaged(false);
        }

        if (!utils.ValidationUtils.isValidSdt(sdt)) {
            lblErrSdt.setText("Số điện thoại phải gồm 10 số và bắt đầu bằng số 0!");
            lblErrSdt.setVisible(true);
            lblErrSdt.setManaged(true);
            valid = false;
        } else {
            // Kiểm tra trùng SĐT
            dao.DAO_KhachHang daoKh = new dao.DAO_KhachHang();
            KhachHang existingKh = daoKh.getBySdt(sdt);
            if (existingKh != null) {
                lblErrSdt.setText("Số điện thoại này đã tồn tại trong hệ thống!");
                lblErrSdt.setVisible(true);
                lblErrSdt.setManaged(true);
                valid = false;
            } else {
                lblErrSdt.setVisible(false);
                lblErrSdt.setManaged(false);
            }
        }
        
        if (!utils.ValidationUtils.isValidDiaChi(diaChi)) {
            lblErrDiaChi.setText("Địa chỉ phải từ 2-255 ký tự và chứa ít nhất 1 chữ cái hoặc số!");
            lblErrDiaChi.setVisible(true);
            lblErrDiaChi.setManaged(true);
            valid = false;
        } else {
            lblErrDiaChi.setVisible(false);
            lblErrDiaChi.setManaged(false);
        }

        if (!valid) {
            new Alert(Alert.AlertType.ERROR, "Dữ liệu nhập không hợp lệ! Vui lòng kiểm tra lại các ô bị bôi đỏ bên dưới.").show();
            Stage stage = (Stage) txtMa.getScene().getWindow();
            stage.sizeToScene();
            return;
        }

        dao.DAO_KhachHang daoKh = new dao.DAO_KhachHang();
        KhachHang newKh = new KhachHang(txtMa.getText(), ten, sdt, diaChi, 0);
        if (daoKh.themKhachHang(newKh)) {
            new Alert(Alert.AlertType.INFORMATION, "Thêm khách hàng thành công!").show();
            this.resultKhachHang = newKh;
        } else {
            new Alert(Alert.AlertType.ERROR, "Thất bại khi thêm vào cơ sở dữ liệu!").show();
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
