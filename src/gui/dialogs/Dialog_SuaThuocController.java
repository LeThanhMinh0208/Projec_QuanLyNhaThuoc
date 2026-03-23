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
        if (donViSelected == null || donViSelected.trim().isEmpty()) { setLoi(cbDonVi, "Vui lòng chọn đơn vị tính!"); hopLe = false; }

        String tenThuoc = txtTen.getText();
        tenThuoc = utils.ValidationUtils.capitalizeName(tenThuoc);
        txtTen.setText(tenThuoc);

        if (!utils.ValidationUtils.isValidTenThuoc(tenThuoc)) {
            setLoi(txtTen, "Tên thuốc phải từ 2-150 ký tự và chứa ít nhất 1 chữ cái!");
            hopLe = false;
        } else if (!txtMa.getText().isEmpty()) {
            Thuoc currThuoc = daoThuoc.getThuocByMa(txtMa.getText());
            if (currThuoc != null && !currThuoc.getTenThuoc().equalsIgnoreCase(tenThuoc) && daoThuoc.existsByTenThuoc(tenThuoc)) {
                setLoi(txtTen, "Tên thuốc này đã tồn tại trong hệ thống!");
                hopLe = false;
            }
        }

        String hoatChat = txtHoatChat.getText();
        hoatChat = utils.ValidationUtils.normalizeString(hoatChat);
        txtHoatChat.setText(hoatChat);

        if (!utils.ValidationUtils.isSoftValidText(hoatChat)) { setLoi(txtHoatChat, "Hoạt chất phải từ 2-255 ký tự và chứa ít nhất 1 chữ/số!"); hopLe = false; }

        String hangSX = txtHangSX.getText();
        hangSX = utils.ValidationUtils.capitalizeName(hangSX);
        txtHangSX.setText(hangSX);

        if (!utils.ValidationUtils.isValidHangSanXuat(hangSX)) { setLoi(txtHangSX, "Hãng sản xuất phải từ 2-100 ký tự và chứa ít nhất 1 chữ cái!"); hopLe = false; }

        String nuocSX = txtNuocSX.getText();
        nuocSX = utils.ValidationUtils.capitalizeName(nuocSX);
        txtNuocSX.setText(nuocSX);

        if (!utils.ValidationUtils.isValidHangSanXuat(nuocSX)) { setLoi(txtNuocSX, "Nước sản xuất phải từ 2-100 ký tự và chứa ít nhất 1 chữ cái!"); hopLe = false; }

        String hamLuong = txtHamLuong.getText();
        hamLuong = utils.ValidationUtils.normalizeString(hamLuong);
        txtHamLuong.setText(hamLuong);

        if (!utils.ValidationUtils.isSoftValidText(hamLuong)) { setLoi(txtHamLuong, "Hàm lượng phải từ 2-255 ký tự và chứa ít nhất 1 chữ/số!"); hopLe = false; }

        String congDung = txtCongDung.getText();
        congDung = utils.ValidationUtils.normalizeString(congDung);
        txtCongDung.setText(congDung);
        if (!utils.ValidationUtils.isSoftValidText(congDung)) { setLoi(txtCongDung, "Công dụng phải từ 2-255 ký tự và chứa ít nhất 1 chữ/số!"); hopLe = false; }

        String trieuChung = txtTrieuChung.getText();
        trieuChung = utils.ValidationUtils.normalizeString(trieuChung);
        txtTrieuChung.setText(trieuChung);
        if (!utils.ValidationUtils.isSoftValidText(trieuChung)) { setLoi(txtTrieuChung, "Triệu chứng phải từ 2-255 ký tự và chứa ít nhất 1 chữ/số!"); hopLe = false; }

        // Nếu có bất kỳ ô nào vi phạm -> Dừng lại và hiện Alert cảnh báo
        if (!hopLe) {
            StringBuilder errorMsg = new StringBuilder("Số liệu chỉnh sửa không hợp lệ:\n");
            
            if (!utils.ValidationUtils.isValidTenThuoc(tenThuoc)) errorMsg.append("- Tên thuốc không hợp lệ.\n");
            else if (!txtMa.getText().isEmpty()) {
                Thuoc currThuoc = daoThuoc.getThuocByMa(txtMa.getText());
                if (currThuoc != null && !currThuoc.getTenThuoc().equalsIgnoreCase(tenThuoc) && daoThuoc.existsByTenThuoc(tenThuoc)) {
                    errorMsg.append("- Tên thuốc mới đã bị trùng với tên thuốc khác trong hệ thống.\n");
                }
            }
            
            if (tenDMSelected == null || tenDMSelected.trim().isEmpty()) errorMsg.append("- Chưa chọn danh mục.\n");
            if (donViSelected == null || donViSelected.trim().isEmpty()) errorMsg.append("- Chưa chọn đơn vị tính.\n");

            errorMsg.append("\nVui lòng kiểm tra lại các ô bị bôi đỏ!");
            new Alert(Alert.AlertType.ERROR, errorMsg.toString()).show();
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