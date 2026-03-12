package gui.dialogs;

import dao.DAO_DanhMucThuoc;
import dao.DAO_Thuoc;
import entity.DanhMucThuoc;
import entity.Thuoc;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;

public class Dialog_ThemThuocController {

    @FXML private ImageView imgPreview;
    @FXML private TextField txtMa, txtTen, txtHoatChat, txtHangSX, txtNuocSX, txtHamLuong, txtDonVi;
    @FXML private ComboBox<String> cbDanhMuc;
    @FXML private CheckBox chkKeDon;
    @FXML private TextArea txtCongDung, txtTrieuChung;

    private DAO_DanhMucThuoc daoDM = new DAO_DanhMucThuoc();
    private DAO_Thuoc daoThuoc = new DAO_Thuoc();
    private String tenFileAnh = "default.png";
    private ArrayList<DanhMucThuoc> dsDanhMucToanBo;

    @FXML
    public void initialize() {
        // 1. TỰ ĐỘNG SINH MÃ VÀ HIỂN THỊ
        txtMa.setText(daoThuoc.getMaThuocMoi());
        txtMa.setEditable(false); // Đảm bảo người dùng không sửa được mã tự sinh

        // 2. Nạp danh mục và lưu lại danh sách object để lấy mã sau này
        dsDanhMucToanBo = daoDM.getAllDanhMuc();
        ArrayList<String> dsTenDM = new ArrayList<>();
        for (DanhMucThuoc dm : dsDanhMucToanBo) {
            dsTenDM.add(dm.getTenDanhMuc());
        }
        cbDanhMuc.getItems().setAll(dsTenDM);
    }

    // --- HÀM GIẢI QUYẾT LỖI CỦA BẠN ---
    @FXML
    private void handleChonAnh() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh thuốc");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        File file = fileChooser.showOpenDialog(imgPreview.getScene().getWindow());
        if (file != null) {
            // Hiển thị ảnh vừa chọn lên ImageView
            imgPreview.setImage(new Image(file.toURI().toString()));
            // Lưu lại tên file để tí nữa lưu vào CSDL
            tenFileAnh = file.getName(); 
        }
    }

    @FXML
    private void handleLuu() {
        String tenDMSelected = cbDanhMuc.getValue();
        if (txtTen.getText().isEmpty() || tenDMSelected == null) {
            new Alert(Alert.AlertType.ERROR, "Vui lòng nhập tên thuốc và chọn danh mục!").show();
            return;
        }

        // Tìm Mã Danh Mục tương ứng với Tên đã chọn
        String maDM = "";
        for (DanhMucThuoc dm : dsDanhMucToanBo) {
            if (dm.getTenDanhMuc().equals(tenDMSelected)) {
                maDM = dm.getMaDanhMuc();
                break;
            }
        }

        // 2. Tạo đối tượng Thuoc và đóng gói dữ liệu
        Thuoc t = new Thuoc();
        t.setMaThuoc(txtMa.getText()); // Lấy mã tự sinh từ ô text
        t.setMaDanhMuc(maDM);         // Truyền MÃ danh mục vào đây
        t.setTenThuoc(txtTen.getText());
        t.setHoatChat(txtHoatChat.getText());
        t.setHangSanXuat(txtHangSX.getText());
        t.setNuocSanXuat(txtNuocSX.getText());
        t.setHamLuong(txtHamLuong.getText());
        t.setDonViCoBan(txtDonVi.getText());
        t.setCanKeDon(chkKeDon.isSelected());
        t.setCongDung(txtCongDung.getText());
        t.setTrieuChung(txtTrieuChung.getText());
        t.setHinhAnh(tenFileAnh);
        t.setTrangThai("DANG_BAN");

        // 3. Gọi DAO lưu vào CSDL
        if (daoThuoc.themThuoc(t)) {
            new Alert(Alert.AlertType.INFORMATION, "Thêm thuốc thành công!").show();
            handleHuy(); // Đóng cửa sổ
        } else {
            new Alert(Alert.AlertType.ERROR, "Lỗi! Không thể lưu thuốc vào CSDL.").show();
        }
    }

    @FXML
    private void handleHuy() {
        // Đóng cửa sổ hiện tại
        Stage stage = (Stage) txtTen.getScene().getWindow();
        stage.close();
    }
}