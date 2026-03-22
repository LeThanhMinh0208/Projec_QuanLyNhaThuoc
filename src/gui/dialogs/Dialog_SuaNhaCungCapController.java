package gui.dialogs;

import dao.DAO_NhaCungCap;
import entity.NhaCungCap;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class Dialog_SuaNhaCungCapController {

    @FXML private TextField txtMa;
    @FXML private TextField txtTen;
    @FXML private TextField txtSdt;
    @FXML private TextField txtDiaChi;
    @FXML private TextField txtCongNo;
    @FXML private Button btnHuy;

    private DAO_NhaCungCap daoNCC = new DAO_NhaCungCap();
    private NhaCungCap nhaCungCap;

    /**
     * Đổ dữ liệu nhà cung cấp được chọn từ bảng vào form sửa
     */
    public void setNhaCungCapData(NhaCungCap ncc) {
        this.nhaCungCap = ncc;
        if (ncc != null) {
            txtMa.setText(ncc.getMaNhaCungCap());
            txtTen.setText(ncc.getTenNhaCungCap());
            txtSdt.setText(ncc.getSdt());
            txtDiaChi.setText(ncc.getDiaChi());
            txtCongNo.setText(String.valueOf(ncc.getCongNo()));
            
            // Khóa mã không cho sửa
            txtMa.setEditable(false);
            txtMa.setDisable(true);
        }
    }

    @FXML
    private void handleLuu() {
        // 1. Validate dữ liệu bắt buộc
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

        if (!nhaCungCap.getTenNhaCungCap().equalsIgnoreCase(ten) && daoNCC.existsByTenNhaCungCap(ten)) {
            new Alert(Alert.AlertType.ERROR, "Tên nhà cung cấp này đã bị trùng lắp, vui lòng chọn tên khác!").show();
            return;
        }

        // Validate công nợ là số
        double congNo;
        try {
            congNo = Double.parseDouble(congNoStr);
        } catch (NumberFormatException e) {
            new Alert(Alert.AlertType.ERROR, "Công nợ phải là số hợp lệ!").show();
            return;
        }

        // 2. Cập nhật dữ liệu vào object
        nhaCungCap.setTenNhaCungCap(ten);
        nhaCungCap.setSdt(sdt);
        nhaCungCap.setDiaChi(diaChi);
        nhaCungCap.setCongNo(congNo);

        // 3. Gọi DAO cập nhật (bạn cần implement hàm này trong DAO)
        if (daoNCC.capNhatNhaCungCap(nhaCungCap)) {
            new Alert(Alert.AlertType.INFORMATION, "Cập nhật nhà cung cấp thành công!").show();
            handleHuy();
        } else {
            new Alert(Alert.AlertType.ERROR, "Cập nhật thất bại! Vui lòng kiểm tra dữ liệu.").show();
        }
    }

    @FXML
    private void handleHuy() {
        Stage stage = (Stage) btnHuy.getScene().getWindow();
        stage.close();
    }
}