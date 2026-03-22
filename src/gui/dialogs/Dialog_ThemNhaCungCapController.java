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
    }

    @FXML
    private void handleLuu() {
        String ten = txtTen.getText().trim();
        String sdt = txtSdt.getText().trim();
        String diaChi = txtDiaChi.getText().trim();
        String congNoStr = txtCongNo.getText().trim();

        if (ten.isEmpty() || sdt.isEmpty() || diaChi.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Vui lòng nhập đầy đủ: Tên NCC, SĐT và Địa chỉ!").show();
            return;
        }

        // Validate SĐT (cơ bản)
        if (!sdt.matches("^0\\d{9}$")) {
            new Alert(Alert.AlertType.ERROR, "Số điện thoại phải là 10 chữ số và bắt đầu bằng 0!").show();
            return;
        }

        // Validate công nợ
        double congNo = 0.0;
        try {
            if (!congNoStr.isEmpty()) {
                congNo = Double.parseDouble(congNoStr);
            }
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Công nợ phải là số hợp lệ (ví dụ: 0 hoặc 5000000)!").show();
            return;
        }

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