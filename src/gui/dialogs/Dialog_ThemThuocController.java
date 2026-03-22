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
    @FXML private TextField txtMa, txtTen, txtHoatChat, txtHangSX, txtNuocSX, txtHamLuong;
    @FXML private ComboBox<String> cbDanhMuc, cbDonVi; 
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
        cbDonVi.setItems(FXCollections.observableArrayList("Hộp", "Vỉ", "Viên", "Tuýp", "Chai"));

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
        Control[] danhSachO = {txtTen, txtHoatChat, txtHangSX, txtNuocSX, txtHamLuong, cbDonVi, cbDanhMuc, txtCongDung, txtTrieuChung};
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
        Control[] danhSachO = {txtTen, txtHoatChat, txtHangSX, txtNuocSX, txtHamLuong, cbDonVi, cbDanhMuc, txtCongDung, txtTrieuChung};
        for (Control c : danhSachO) xoaLoi(c);

        // --- KIỂM TRA TỪNG Ô (RỖNG VÀ ĐỘ DÀI) ---

        String tenDMSelected = cbDanhMuc.getValue();
        if (tenDMSelected == null || tenDMSelected.trim().isEmpty()) {
            setLoi(cbDanhMuc, "Bạn chưa chọn Danh Mục!");
            hopLe = false;
        }

        String tenThuoc = txtTen.getText().trim();
        if (tenThuoc.isEmpty()) {
            setLoi(txtTen, "Tên thuốc không được để trống!");
            hopLe = false;
        } else if (tenThuoc.length() > 100) {
            setLoi(txtTen, "Tên thuốc quá dài (tối đa 100 ký tự)!");
            hopLe = false;
        }

        String hoatChat = txtHoatChat.getText().trim();
        if (hoatChat.isEmpty()) {
            setLoi(txtHoatChat, "Hoạt chất không được để trống!");
            hopLe = false;
        } else if (hoatChat.length() > 100) {
            setLoi(txtHoatChat, "Hoạt chất quá dài (tối đa 100 ký tự)!");
            hopLe = false;
        }

        String hangSX = txtHangSX.getText().trim();
        if (hangSX.isEmpty()) {
            setLoi(txtHangSX, "Hãng sản xuất không được để trống!");
            hopLe = false;
        } else if (hangSX.length() > 100) {
            setLoi(txtHangSX, "Hãng sản xuất quá dài (tối đa 100 ký tự)!");
            hopLe = false;
        }

        String nuocSX = txtNuocSX.getText().trim();
        if (nuocSX.isEmpty()) {
            setLoi(txtNuocSX, "Nước sản xuất không được để trống!");
            hopLe = false;
        } else if (nuocSX.length() > 100) {
            setLoi(txtNuocSX, "Nước sản xuất quá dài (tối đa 100 ký tự)!");
            hopLe = false;
        }

        String hamLuong = txtHamLuong.getText().trim();
        if (hamLuong.isEmpty()) {
            setLoi(txtHamLuong, "Hàm lượng không được để trống!");
            hopLe = false;
        } else if (hamLuong.length() > 100) {
            setLoi(txtHamLuong, "Hàm lượng quá dài (tối đa 100 ký tự)!");
            hopLe = false;
        }

        String donViSelected = cbDonVi.getValue();
        if (donViSelected == null || donViSelected.trim().isEmpty()) {
            setLoi(cbDonVi, "Bạn chưa chọn Đơn Vị!");
            hopLe = false;
        }

        String congDung = txtCongDung.getText().trim();
        if (congDung.isEmpty()) {
            setLoi(txtCongDung, "Công dụng không được để trống!");
            hopLe = false;
        } else if (congDung.length() > 500) {
            setLoi(txtCongDung, "Công dụng quá dài (tối đa 500 ký tự)!");
            hopLe = false;
        }

        String trieuChung = txtTrieuChung.getText().trim();
        if (trieuChung.isEmpty()) {
            setLoi(txtTrieuChung, "Triệu chứng không được để trống!");
            hopLe = false;
        } else if (trieuChung.length() > 500) {
            setLoi(txtTrieuChung, "Triệu chứng quá dài (tối đa 500 ký tự)!");
            hopLe = false;
        }

        // Nếu có bất kỳ ô nào vi phạm -> Dừng lại và hiện Alert cảnh báo
        if (!hopLe) {
            new Alert(Alert.AlertType.WARNING, "Dữ liệu chưa hợp lệ! Vui lòng kiểm tra các ô bị bôi đỏ.").show();
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