package gui.dialogs;

import java.text.DecimalFormat;

import dao.DAO_NhaCungCap;
// import dao.DAO_PhieuThu; // Sếp mở comment dòng này nếu đã tạo DAO_PhieuThu
import entity.NhaCungCap;
import entity.NhanVien;
// import entity.PhieuThu; // Sếp mở comment dòng này nếu đã tạo entity PhieuThu
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import utils.AlertUtils;
import utils.UserSession;
import utils.WindowUtils;

public class Dialog_NCCThanhToanController {

    @FXML private Label lblTenNCC;
    @FXML private Label lblCongNoNCCNo;
    @FXML private TextField txtSoTienThu;
    @FXML private CheckBox chkTatToan;
    @FXML private ComboBox<String> cbHinhThuc;
    @FXML private TextArea txtGhiChu;

    private NhaCungCap nhaCungCap;
    private double soTienNCCDangNo = 0; // Số tiền thực tế NCC nợ mình

    private DAO_NhaCungCap daoNhaCungCap = new DAO_NhaCungCap();
    // private DAO_PhieuThu daoPhieuThu = new DAO_PhieuThu(); // Nếu sếp có bảng PhieuThu trong CSDL

    private DecimalFormat df = new DecimalFormat("#,###");

    @FXML
    public void initialize() {
        // 1. Khởi tạo danh sách Hình thức thu
        cbHinhThuc.setItems(FXCollections.observableArrayList("Tiền mặt", "Chuyển khoản"));
        cbHinhThuc.getSelectionModel().selectFirst();

        // 2. Xử lý sự kiện khi tick vào ô "Thu đủ toàn bộ số nợ"
        chkTatToan.setOnAction(e -> {
            if (chkTatToan.isSelected()) {
                // Điền thẳng số tiền NCC nợ vào ô nhập liệu
                txtSoTienThu.setText(String.format("%.0f", soTienNCCDangNo));
                txtSoTienThu.setDisable(true); // Khóa ô nhập liệu lại
            } else {
                txtSoTienThu.clear();
                txtSoTienThu.setDisable(false); // Mở lại ô nhập liệu
            }
        });

        // 3. Format text field chỉ cho phép nhập số
        txtSoTienThu.textProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.matches("\\d*")) {
                txtSoTienThu.setText(newValue.replaceAll("[^\\d]", ""));
            }
        });
    }

    /**
     * Hàm này được gọi từ GUI_QuanLyCongNoController để truyền dữ liệu NCC sang
     */
    public void setNhaCungCap(NhaCungCap ncc) {
        this.nhaCungCap = ncc;
        lblTenNCC.setText(ncc.getTenNhaCungCap());

        // Công nợ bị Âm nghĩa là NCC đang nợ mình. Ta lấy giá trị tuyệt đối để hiển thị cho đẹp
        this.soTienNCCDangNo = Math.abs(ncc.getCongNo());

        lblCongNoNCCNo.setText(df.format(soTienNCCDangNo) + " VNĐ");
    }

    @FXML
    void handleXacNhan(ActionEvent event) {
        // 1. Lấy dữ liệu
        String tienStr = txtSoTienThu.getText().trim();
        String hinhThuc = cbHinhThuc.getValue();
        String ghiChu = txtGhiChu.getText().trim();

        // 2. Kiểm tra tính hợp lệ (Validation)
        if (tienStr.isEmpty()) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Lỗi nhập liệu", "Vui lòng nhập số tiền muốn thu!");
            txtSoTienThu.requestFocus();
            return;
        }

        double soTienThu = Double.parseDouble(tienStr);

        if (soTienThu <= 0) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Lỗi nhập liệu", "Số tiền thu phải lớn hơn 0!");
            return;
        }

        if (soTienThu > soTienNCCDangNo) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Lỗi nhập liệu", "Số tiền thu (" + df.format(soTienThu) + ") không được lớn hơn số tiền NCC đang nợ (" + df.format(soTienNCCDangNo) + ")!");
            return;
        }

        // 3. Tiến hành cập nhật Database
        NhanVien nhanVienLap = UserSession.getInstance().getUser();

        // Thu tiền vào thì cộng (+) thêm vào Công Nợ.
        // Ví dụ: Đang nợ -500k, thu vào 200k -> (-500k) + 200k = -300k (Còn nợ 300k)
        double congNoMoi = nhaCungCap.getCongNo() + soTienThu;

        // Sếp gọi hàm update bên DAO (Giả sử sếp có hàm capNhatCongNo)
        boolean updateSuccess = daoNhaCungCap.capNhatCongNo(nhaCungCap.getMaNhaCungCap(), congNoMoi);

        if (updateSuccess) {

            // 💡 LƯU Ý CHO SẾP:
            // Nếu hệ thống của sếp CÓ bảng `PhieuThu` trong SQL Server để lưu lịch sử dòng tiền vào,
            // Sếp mở comment đoạn code dưới đây và chỉnh sửa lại tên hàm cho khớp với DAO của sếp nhé!

            /*
            String maPhieuThu = daoPhieuThu.taoMaPhieuThuTuDong();
            PhieuThu pt = new PhieuThu(maPhieuThu, nhaCungCap, nhanVienLap, new Timestamp(System.currentTimeMillis()), soTienThu, hinhThuc, ghiChu);
            daoPhieuThu.themPhieuThu(pt);
            */

            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã thu thành công " + df.format(soTienThu) + " VNĐ từ nhà cung cấp!");
            WindowUtils.closeWindow(event); // Đóng cửa sổ Dialog
        } else {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Thất bại", "Đã xảy ra lỗi khi cập nhật công nợ vào cơ sở dữ liệu!");
        }
    }

    @FXML
    void handleHuy(ActionEvent event) {
        WindowUtils.closeWindow(event);
    }
}