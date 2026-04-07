package gui.dialogs;

import dao.DAO_DanhMucThuoc;
import dao.DAO_Thuoc;
import entity.DanhMucThuoc;
import entity.Thuoc;
import javafx.collections.FXCollections;
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
    @FXML private TextField txtMa, txtTen, txtHoatChat, txtHangSX, txtHamLuong;
    @FXML private ComboBox<String> cbDanhMuc, cbDonVi, cbNuocSX; 
    @FXML private CheckBox chkKeDon;
    @FXML private TextArea txtCongDung, txtTrieuChung;

    private DAO_DanhMucThuoc daoDM = new DAO_DanhMucThuoc();
    private DAO_Thuoc daoThuoc = new DAO_Thuoc();
    private String tenFileAnh = "default.png";
    private ArrayList<DanhMucThuoc> dsDanhMucToanBo;

    @FXML
    public void initialize() {
        txtMa.setText(daoThuoc.getMaThuocMoi());
        txtMa.setEditable(false); 

        // Nạp danh mục
        dsDanhMucToanBo = daoDM.getAllDanhMuc();
        ArrayList<String> dsTenDM = new ArrayList<>();
        for (DanhMucThuoc dm : dsDanhMucToanBo) {
            dsTenDM.add(dm.getTenDanhMuc());
        }
        cbDanhMuc.getItems().setAll(dsTenDM);

        // Nạp Đơn Vị Tính
        cbDonVi.setItems(FXCollections.observableArrayList( "Lọ", "Viên", "Tuýp", "Chai"));

        // Nạp Nước Sản Xuất
        cbNuocSX.getItems().setAll(
            "Việt Nam", "Mỹ", "Pháp", "Đức", "Nhật Bản",
            "Hàn Quốc", "Ấn Độ", "Trung Quốc", "Anh",
            "Thụy Sĩ", "Ý", "Tây Ban Nha", "Úc", "Canada",
            "Singapore", "Thái Lan", "Indonesia"
        );

        // KÍCH HOẠT TÍNH NĂNG: Click vào ô nào là ô đó tự xóa màu đỏ báo lỗi
        kichHoatTuDongXoaLoi();
    }

    // --- HỆ THỐNG CẢNH BÁO LỖI THÔNG MINH ---
    private void setLoi(Control control, String thongBao) {
        // Đổi viền thành màu đỏ, nền hơi hồng để đập vào mắt
        control.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2px; -fx-border-radius: 8; -fx-background-color: #fef2f2; -fx-background-radius: 8;");
        
        // Gắn Tooltip thông báo lỗi (Hiện ra khi rê chuột vào)
        Tooltip tooltip = new Tooltip("❌ " + thongBao);
        tooltip.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 5px 10px;");
        control.setTooltip(tooltip);
    }

    private void xoaLoi(Control control) {
        control.setStyle(""); // Trả lại style CSS mặc định ban đầu
        control.setTooltip(null);
    }

    private void kichHoatTuDongXoaLoi() {
        Control[] danhSachO = {txtTen, txtHoatChat, txtHangSX, cbNuocSX, txtHamLuong, cbDonVi, cbDanhMuc, txtCongDung, txtTrieuChung};
        for (Control c : danhSachO) {
            // Lắng nghe sự kiện: Chỉ cần click chuột vào (Focus) là tự động xóa báo lỗi đỏ
            c.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) xoaLoi(c);
            });
        }
    }

    @FXML
    private void handleChonAnh() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh thuốc");
        fileChooser.getExtensionFilters().add(
            new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg")
        );
        
        File file = fileChooser.showOpenDialog(imgPreview.getScene().getWindow());
        if (file != null) {
            imgPreview.setImage(new Image(file.toURI().toString()));
            tenFileAnh = file.getName(); 
        }
    }

    @FXML
    private void handleLuu() {
        boolean hopLe = true;

        // Xóa sạch các báo lỗi đỏ cũ trước khi kiểm tra lại
        Control[] danhSachO = {txtTen, txtHoatChat, txtHangSX, cbNuocSX, txtHamLuong, cbDonVi, cbDanhMuc, txtCongDung, txtTrieuChung};
        for (Control c : danhSachO) xoaLoi(c);

        // --- KIỂM TRA TỪNG Ô (RỖNG VÀ ĐỘ DÀI) ---

        String tenDMSelected = cbDanhMuc.getValue();
        if (tenDMSelected == null || tenDMSelected.trim().isEmpty()) {
            setLoi(cbDanhMuc, "Bạn chưa chọn Danh Mục!");
            hopLe = false;
        }

        String tenThuoc = txtTen.getText();
        tenThuoc = utils.ValidationUtils.capitalizeName(tenThuoc);
        txtTen.setText(tenThuoc);

        if (!utils.ValidationUtils.isValidTenThuoc(tenThuoc)) {
            setLoi(txtTen, "Tên thuốc phải từ 2-150 ký tự và chứa ít nhất 1 chữ cái!");
            hopLe = false;
        } else if (daoThuoc.existsByTenThuoc(tenThuoc)) {
            setLoi(txtTen, "Tên thuốc này đã tồn tại trong hệ thống!");
            hopLe = false;
        }

        String hoatChat = txtHoatChat.getText();
        hoatChat = utils.ValidationUtils.normalizeString(hoatChat);
        txtHoatChat.setText(hoatChat);

        if (!utils.ValidationUtils.isSoftValidText(hoatChat)) {
            setLoi(txtHoatChat, "Hoạt chất phải từ 2-255 ký tự và chứa ít nhất 1 chữ/số!");
            hopLe = false;
        }

        String hangSX = txtHangSX.getText();
        hangSX = utils.ValidationUtils.capitalizeName(hangSX);
        txtHangSX.setText(hangSX);

        if (!utils.ValidationUtils.isValidHangSanXuat(hangSX)) {
            setLoi(txtHangSX, "Hãng sản xuất phải từ 2-100 ký tự và chứa ít nhất 1 chữ cái!");
            hopLe = false;
        }

        String nuocSX = cbNuocSX.getValue();
        if (nuocSX == null || nuocSX.trim().isEmpty()) {
            setLoi(cbNuocSX, "Vui lòng chọn Nước sản xuất!");
            hopLe = false;
        }

        String hamLuong = txtHamLuong.getText();
        hamLuong = utils.ValidationUtils.normalizeString(hamLuong);
        txtHamLuong.setText(hamLuong);

        if (!utils.ValidationUtils.isSoftValidText(hamLuong)) {
            setLoi(txtHamLuong, "Hàm lượng phải từ 2-255 ký tự và chứa ít nhất 1 chữ/số!");
            hopLe = false;
        }

        String donViSelected = cbDonVi.getValue();
        if (donViSelected == null || donViSelected.trim().isEmpty()) {
            setLoi(cbDonVi, "Vui lòng chọn đơn vị tính!");
            hopLe = false;
        }

        String congDung = txtCongDung.getText();
        congDung = utils.ValidationUtils.normalizeString(congDung);
        txtCongDung.setText(congDung);
        
        if (!utils.ValidationUtils.isSoftValidText(congDung)) {
            setLoi(txtCongDung, "Công dụng phải từ 2-255 ký tự và chứa ít nhất 1 chữ/số!");
            hopLe = false;
        }

        String trieuChung = txtTrieuChung.getText();
        trieuChung = utils.ValidationUtils.normalizeString(trieuChung);
        txtTrieuChung.setText(trieuChung);

        if (!utils.ValidationUtils.isSoftValidText(trieuChung)) {
            setLoi(txtTrieuChung, "Triệu chứng phải từ 2-255 ký tự và chứa ít nhất 1 chữ/số!");
            hopLe = false;
        }

        // Nếu có bất kỳ ô nào vi phạm -> Dừng lại và hiện Alert cảnh báo
        if (!hopLe) {
            StringBuilder errorMsg = new StringBuilder("Dữ liệu chưa hợp lệ:\n");
            
            // Re-check for specific critical errors to put in the Alert
            if (!utils.ValidationUtils.isValidTenThuoc(tenThuoc)) errorMsg.append("- Tên thuốc không hợp lệ.\n");
            else if (daoThuoc.existsByTenThuoc(tenThuoc)) errorMsg.append("- Tên thuốc này đã tồn tại trong hệ thống.\n");
            
            if (tenDMSelected == null || tenDMSelected.trim().isEmpty()) errorMsg.append("- Chưa chọn danh mục.\n");
            if (donViSelected == null || donViSelected.trim().isEmpty()) errorMsg.append("- Chưa chọn đơn vị tính.\n");
            
            errorMsg.append("\nVui lòng kiểm tra lại các ô bị bôi đỏ!");
            new Alert(Alert.AlertType.ERROR, errorMsg.toString()).show();
            return;
        }

        // --- TÌM MÃ DANH MỤC VÀ LƯU DATABASE KHI ĐÃ HỢP LỆ ---
        String maDM = "";
        for (DanhMucThuoc dm : dsDanhMucToanBo) {
            if (dm.getTenDanhMuc().equals(tenDMSelected)) {
                maDM = dm.getMaDanhMuc();
                break;
            }
        }

        Thuoc t = new Thuoc();
        t.setMaThuoc(txtMa.getText()); 
        t.setMaDanhMuc(maDM);         
        t.setTenThuoc(tenThuoc);
        t.setHoatChat(hoatChat);
        t.setHangSanXuat(hangSX);
        t.setNuocSanXuat(nuocSX);
        t.setHamLuong(hamLuong);
        t.setDonViCoBan(donViSelected); 
        t.setCanKeDon(chkKeDon.isSelected());
        t.setCongDung(congDung);
        t.setTrieuChung(trieuChung);
        t.setHinhAnh(tenFileAnh);
        t.setTrangThai("DANG_BAN");

        if (daoThuoc.themThuoc(t)) {
            new Alert(Alert.AlertType.INFORMATION, "Thêm thuốc thành công!").showAndWait();
            handleHuy(); // Đóng form
        } else {
            new Alert(Alert.AlertType.ERROR, "Lỗi! Không thể lưu thuốc vào CSDL.").show();
        }
    }

    @FXML
    private void handleHuy() {
        Stage stage = (Stage) txtTen.getScene().getWindow();
        stage.close();
    }
}