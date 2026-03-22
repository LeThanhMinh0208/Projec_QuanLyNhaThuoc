package gui.dialogs;

import dao.DAO_NhaCungCap;
import entity.NhaCungCap;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.stage.Stage;

public class Dialog_XoaNhaCungCapController {

    @FXML private Label lblTenNcc;          // Label hiển thị tên NCC để xác nhận
    @FXML private Button btnHuy;            // Nút "Quay Lại"

    private DAO_NhaCungCap daoNCC = new DAO_NhaCungCap();
    private NhaCungCap nhaCungCapCanXoa;

    /**
     * Nhận dữ liệu nhà cung cấp từ bảng chính và hiển thị thông tin xác nhận
     */
    public void setNhaCungCapData(NhaCungCap ncc) {
        this.nhaCungCapCanXoa = ncc;
        if (ncc != null) {
            lblTenNcc.setText(ncc.getTenNhaCungCap());
        } else {
            lblTenNcc.setText("[Không có dữ liệu]");
        }
    }

    /**
     * Xử lý khi bấm nút "VẪN XÓA 🔥"
     */
    @FXML
    private void handleXoa() {
        if (nhaCungCapCanXoa == null) {
            new Alert(Alert.AlertType.WARNING, "Không có nhà cung cấp nào được chọn để xóa!").show();
            closeDialog();
            return;
        }

        // Xác nhận lần cuối (tăng an toàn)
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText("Xóa nhà cung cấp");
        confirm.setContentText("Bạn có chắc chắn muốn xóa nhà cung cấp '" + 
                               nhaCungCapCanXoa.getTenNhaCungCap() + 
                               "' (Mã: " + nhaCungCapCanXoa.getMaNhaCungCap() + ") không?\n" +
                               "Hành động này không thể hoàn tác!");
        
        confirm.showAndWait().ifPresent(response -> {
            if (response == javafx.scene.control.ButtonType.OK) {
                // Thực hiện xóa
                boolean xoaThanhCong = daoNCC.xoaNhaCungCap(nhaCungCapCanXoa.getMaNhaCungCap());

                if (xoaThanhCong) {
                    new Alert(Alert.AlertType.INFORMATION, 
                              "Đã xóa nhà cung cấp thành công!").show();
                } else {
                    new Alert(Alert.AlertType.ERROR, 
                              "Không thể xóa nhà cung cấp!\n" +
                              "Có thể NCC này đang được sử dụng trong đơn nhập hàng hoặc dữ liệu khác.").show();
                }
                
                closeDialog();
            }
        });
    }

    /**
     * Đóng dialog khi bấm "Quay Lại"
     */
    @FXML
    private void handleHuy() {
        closeDialog();
    }

    /**
     * Đóng cửa sổ dialog
     */
    private void closeDialog() {
        Stage stage = (Stage) btnHuy.getScene().getWindow();
        stage.close();
    }
}