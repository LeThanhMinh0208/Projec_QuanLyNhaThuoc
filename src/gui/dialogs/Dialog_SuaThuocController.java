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

public class Dialog_SuaThuocController {

    @FXML private TextField txtMa, txtTen, txtHoatChat, txtHangSX, txtNuocSX, txtHamLuong;
    @FXML private ComboBox<String> cbDanhMuc, cbTrangThai, cbDonVi; // Đổi txtDonVi thành cbDonVi
    @FXML private CheckBox chkKeDon;
    @FXML private TextArea txtCongDung, txtTrieuChung;
    @FXML private ImageView imgPreview;
    @FXML private Button btnHuy;

    private String tenFileAnh;
    private DAO_Thuoc daoThuoc = new DAO_Thuoc();
    private DAO_DanhMucThuoc daoDM = new DAO_DanhMucThuoc();
    private ArrayList<DanhMucThuoc> dsDM;

    @FXML 
    public void initialize() {
        // Nạp danh mục
        dsDM = daoDM.getAllDanhMuc();
        for (DanhMucThuoc dm : dsDM) cbDanhMuc.getItems().add(dm.getTenDanhMuc());

        // Nạp trạng thái và đơn vị tính
        cbTrangThai.getItems().setAll("DANG_BAN", "HET_HANG", "NGUNG_BAN");
        cbDonVi.setItems(FXCollections.observableArrayList("Hộp", "Vỉ", "Viên", "Tuýp", "Chai"));

        kichHoatTuDongXoaLoi();
    }

    // --- HỆ THỐNG BÔI ĐỎ BÁO LỖI ---
    private void setLoi(Control control, String thongBao) {
        control.setStyle("-fx-border-color: #ef4444; -fx-border-width: 2px; -fx-border-radius: 8; -fx-background-color: #fef2f2; -fx-background-radius: 8;");
        Tooltip tooltip = new Tooltip("❌ " + thongBao);
        tooltip.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-size: 13px; -fx-font-weight: bold; -fx-padding: 5px 10px;");
        control.setTooltip(tooltip);
    }

    private void xoaLoi(Control control) {
        control.setStyle(""); 
        control.setTooltip(null);
    }

    private void kichHoatTuDongXoaLoi() {
        Control[] danhSachO = {txtTen, txtHoatChat, txtHangSX, txtNuocSX, txtHamLuong, cbDonVi, cbDanhMuc, txtCongDung, txtTrieuChung, cbTrangThai};
        for (Control c : danhSachO) {
            c.focusedProperty().addListener((obs, oldVal, newVal) -> {
                if (newVal) xoaLoi(c);
            });
        }
    }

    // --- ĐỔ DỮ LIỆU TỪ BẢNG VÀO FORM SỬA ---
    public void setThuocData(Thuoc t) {
        this.tenFileAnh = t.getHinhAnh(); 

        txtMa.setText(t.getMaThuoc());
        txtTen.setText(t.getTenThuoc());
        txtHoatChat.setText(t.getHoatChat());
        txtHangSX.setText(t.getHangSanXuat());
        txtNuocSX.setText(t.getNuocSanXuat());
        txtHamLuong.setText(t.getHamLuong());
        cbDonVi.setValue(t.getDonViCoBan()); // Set value cho ComboBox
        cbDanhMuc.setValue(t.getTenDanhMuc());
        cbTrangThai.setValue(t.getTrangThai());
        chkKeDon.setSelected(t.isCanKeDon());
        txtCongDung.setText(t.getCongDung());
        txtTrieuChung.setText(t.getTrieuChung());

        try {
            String path = "/resources/images/images_thuoc/" + t.getHinhAnh();
            Image img = new Image(getClass().getResourceAsStream(path));
            imgPreview.setImage(img);
        } catch (Exception e) {}
    }

    @FXML
    private void handleChonAnh() {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Chọn ảnh thuốc mới");
        fileChooser.getExtensionFilters().add(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.jpeg"));
        
        File selectedFile = fileChooser.showOpenDialog(imgPreview.getScene().getWindow());
        if (selectedFile != null) {
            imgPreview.setImage(new Image(selectedFile.toURI().toString()));
            this.tenFileAnh = selectedFile.getName(); 
        }
    }

    @FXML
    private void handleLuu() {
        boolean hopLe = true;

        Control[] danhSachO = {txtTen, txtHoatChat, txtHangSX, txtNuocSX, txtHamLuong, cbDonVi, cbDanhMuc, cbTrangThai, txtCongDung, txtTrieuChung};
        for (Control c : danhSachO) xoaLoi(c);

        // --- QUÉT RÀNG BUỘC RỖNG & ĐỘ DÀI ---
        String tenDMSelected = cbDanhMuc.getValue();
        if (tenDMSelected == null || tenDMSelected.trim().isEmpty()) { setLoi(cbDanhMuc, "Chưa chọn Danh Mục!"); hopLe = false; }

        String trangThaiSelected = cbTrangThai.getValue();
        if (trangThaiSelected == null || trangThaiSelected.trim().isEmpty()) { setLoi(cbTrangThai, "Chưa chọn Trạng Thái!"); hopLe = false; }

        String donViSelected = cbDonVi.getValue();
        if (donViSelected == null || donViSelected.trim().isEmpty()) { setLoi(cbDonVi, "Chưa chọn Đơn Vị!"); hopLe = false; }

        String tenThuoc = txtTen.getText().trim();
        if (tenThuoc.isEmpty()) { setLoi(txtTen, "Không được để trống!"); hopLe = false; }
        else if (tenThuoc.length() > 100) { setLoi(txtTen, "Tối đa 100 ký tự!"); hopLe = false; }

        String hoatChat = txtHoatChat.getText().trim();
        if (hoatChat.isEmpty()) { setLoi(txtHoatChat, "Không được để trống!"); hopLe = false; }
        else if (hoatChat.length() > 100) { setLoi(txtHoatChat, "Tối đa 100 ký tự!"); hopLe = false; }

        String hangSX = txtHangSX.getText().trim();
        if (hangSX.isEmpty()) { setLoi(txtHangSX, "Không được để trống!"); hopLe = false; }
        else if (hangSX.length() > 100) { setLoi(txtHangSX, "Tối đa 100 ký tự!"); hopLe = false; }

        String nuocSX = txtNuocSX.getText().trim();
        if (nuocSX.isEmpty()) { setLoi(txtNuocSX, "Không được để trống!"); hopLe = false; }
        else if (nuocSX.length() > 100) { setLoi(txtNuocSX, "Tối đa 100 ký tự!"); hopLe = false; }

        String hamLuong = txtHamLuong.getText().trim();
        if (hamLuong.isEmpty()) { setLoi(txtHamLuong, "Không được để trống!"); hopLe = false; }
        else if (hamLuong.length() > 100) { setLoi(txtHamLuong, "Tối đa 100 ký tự!"); hopLe = false; }

        String congDung = txtCongDung.getText().trim();
        if (congDung.isEmpty()) { setLoi(txtCongDung, "Không được để trống!"); hopLe = false; }
        else if (congDung.length() > 500) { setLoi(txtCongDung, "Tối đa 500 ký tự!"); hopLe = false; }

        String trieuChung = txtTrieuChung.getText().trim();
        if (trieuChung.isEmpty()) { setLoi(txtTrieuChung, "Không được để trống!"); hopLe = false; }
        else if (trieuChung.length() > 500) { setLoi(txtTrieuChung, "Tối đa 500 ký tự!"); hopLe = false; }

        if (!hopLe) {
            new Alert(Alert.AlertType.WARNING, "Dữ liệu chưa hợp lệ! Vui lòng kiểm tra các ô bị bôi đỏ.").show();
            return;
        }

        // --- LƯU DATABASE KHI ĐÃ HỢP LỆ ---
        String maDM = dsDM.stream()
            .filter(d -> d.getTenDanhMuc().equals(tenDMSelected))
            .findFirst().map(DanhMucThuoc::getMaDanhMuc).orElse("");

        Thuoc t = new Thuoc();
        t.setMaThuoc(txtMa.getText());
        t.setTenThuoc(tenThuoc);
        t.setMaDanhMuc(maDM);
        t.setHoatChat(hoatChat);
        t.setHangSanXuat(hangSX);
        t.setNuocSanXuat(nuocSX);
        t.setHamLuong(hamLuong);
        t.setDonViCoBan(donViSelected);
        t.setCanKeDon(chkKeDon.isSelected());
        t.setCongDung(congDung);
        t.setTrieuChung(trieuChung);
        t.setTrangThai(trangThaiSelected);
        t.setHinhAnh(tenFileAnh);

        if (daoThuoc.capNhatThuoc(t)) {
            new Alert(Alert.AlertType.INFORMATION, "Cập nhật thành công!").show();
            handleHuy();
        } else {
            new Alert(Alert.AlertType.ERROR, "Cập nhật thất bại!").show();
        }
    }

    @FXML void handleHuy() { ((Stage) btnHuy.getScene().getWindow()).close(); }
}