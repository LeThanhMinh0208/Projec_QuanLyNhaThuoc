package gui.dialogs;

import dao.DAO_NhaCungCap;
import entity.NhaCungCap;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class Dialog_ThemNhaCungCapController {

    @FXML private TextField txtMa;
    @FXML private TextField txtTen;
    @FXML private TextField txtSdt;
    @FXML private TextField txtDiaChi;
    @FXML private TextField txtCongNo;

    private DAO_NhaCungCap daoNCC = new DAO_NhaCungCap();

    @FXML
    public void initialize() {
        txtMa.setText(daoNCC.getMaNhaCungCapMoi());
        txtMa.setEditable(false);
        txtMa.setDisable(true);
        txtCongNo.setTooltip(new javafx.scene.control.Tooltip("Công nợ được cập nhật tự động từ phiếu nhập hàng"));
    }

    @FXML
    private void handleLuu() {
        String ten = txtTen.getText();
        String sdt = txtSdt.getText();
        String diaChi = txtDiaChi.getText();
        String congNoStr = txtCongNo.getText();

        ten = utils.ValidationUtils.normalizeString(ten);
        sdt = utils.ValidationUtils.normalizeString(sdt);
        diaChi = utils.ValidationUtils.normalizeString(diaChi);
        congNoStr = utils.ValidationUtils.normalizeString(congNoStr);
        
        txtTen.setText(ten);
        txtSdt.setText(sdt);
        txtDiaChi.setText(diaChi);
        txtCongNo.setText(congNoStr);

        StringBuilder err = new StringBuilder();
        if (!utils.ValidationUtils.isValidTenNhaCungCap(ten)) err.append("- Tên nhà cung cấp phải từ 2-150 ký tự và chứa ít nhất 1 chữ cái.\n");
        if (!utils.ValidationUtils.isValidSdt(sdt)) err.append("- Số điện thoại phải gồm 10 số và bắt đầu bằng số 0.\n");
        if (!utils.ValidationUtils.isValidDiaChi(diaChi)) err.append("- Địa chỉ phải từ 2-255 ký tự và chứa ít nhất 1 chữ cái hoặc số.\n");
        
        if (err.length() > 0) {
            new Alert(Alert.AlertType.ERROR, "Dữ liệu nhập không hợp lệ:\n" + err.toString()).show();
            return;
        }

        if (daoNCC.existsByTenNhaCungCap(ten)) {
            new Alert(Alert.AlertType.ERROR, "Tên nhà cung cấp này đã tồn tại trong hệ thống!").show();
            return;
        }

        // Validate công nợ
        double congNo = 0.0;
        // Bỏ validate công nợ cho dialog thêm mới vì bị disable
        // try {
        //     if (!congNoStr.isEmpty()) {
        //         congNo = Double.parseDouble(congNoStr);
        //     }
        // } catch (NumberFormatException e) {
        //     new Alert(Alert.AlertType.ERROR, "Công nợ phải là số hợp lệ (ví dụ: 0 hoặc 5000000)!").show();
        //     return;
        // }

        NhaCungCap ncc = new NhaCungCap();
        ncc.setMaNhaCungCap(txtMa.getText());
        ncc.setTenNhaCungCap(ten);
        ncc.setSdt(sdt);
        ncc.setDiaChi(diaChi);
        ncc.setCongNo(congNo);

        if (daoNCC.themNhaCungCap(ncc)) {
            new Alert(Alert.AlertType.INFORMATION, "Thêm nhà cung cấp thành công!").show();
            handleHuy();
        } else {
            new Alert(Alert.AlertType.ERROR, "Lỗi! Không thể lưu vào CSDL.").show();
        }
    }

    @FXML
    private void handleHuy() {
        Stage stage = (Stage) txtTen.getScene().getWindow();
        stage.close();
    }
}