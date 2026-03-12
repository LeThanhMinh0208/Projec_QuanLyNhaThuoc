package gui.dialogs;

import dao.DAO_DanhMucThuoc;
import dao.DAO_Thuoc;
import entity.DanhMucThuoc;
import entity.Thuoc;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.image.Image; // Cần thêm import này
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.FileChooser;
import java.io.File;
import java.util.ArrayList;

public class Dialog_SuaThuocController {
    // 1. KHAI BÁO ĐẦY ĐỦ CÁC TRƯỜNG THEO FXML
    @FXML private TextField txtMa, txtTen, txtHoatChat, txtHangSX, txtNuocSX, txtHamLuong, txtDonVi;
    @FXML private ComboBox<String> cbDanhMuc, cbTrangThai;
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
        // Nạp danh mục từ DB
        dsDM = daoDM.getAllDanhMuc();
        for (DanhMucThuoc dm : dsDM) cbDanhMuc.getItems().add(dm.getTenDanhMuc());

        // Nạp trạng thái chuẩn theo Constraint của SQL (DANG_BAN, HET_HANG, NGUNG_BAN)
        cbTrangThai.getItems().setAll("DANG_BAN", "HET_HANG", "NGUNG_BAN");
    }

    // ĐỔ DỮ LIỆU TỪ TABLE VÀO FORM
    public void setThuocData(Thuoc t) {
        this.tenFileAnh = t.getHinhAnh(); // Lưu tên ảnh cũ

        txtMa.setText(t.getMaThuoc());
        txtTen.setText(t.getTenThuoc());
        txtHoatChat.setText(t.getHoatChat());
        txtHangSX.setText(t.getHangSanXuat());
        txtNuocSX.setText(t.getNuocSanXuat());
        txtHamLuong.setText(t.getHamLuong());
        txtDonVi.setText(t.getDonViCoBan());
        cbDanhMuc.setValue(t.getTenDanhMuc());
        cbTrangThai.setValue(t.getTrangThai());
        chkKeDon.setSelected(t.isCanKeDon());
        txtCongDung.setText(t.getCongDung());
        txtTrieuChung.setText(t.getTrieuChung());

        // Hiển thị ảnh cũ lên preview
        try {
            String path = "/resources/images/images_thuoc/" + t.getHinhAnh();
            Image img = new Image(getClass().getResourceAsStream(path));
            imgPreview.setImage(img);
        } catch (Exception e) {
            // Nếu không có ảnh thì để mặc định
        }
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
        // 1. Tìm mã danh mục từ tên đã chọn
        String maDM = dsDM.stream()
            .filter(d -> d.getTenDanhMuc().equals(cbDanhMuc.getValue()))
            .findFirst().map(DanhMucThuoc::getMaDanhMuc).orElse("");

        // 2. Đóng gói dữ liệu
        Thuoc t = new Thuoc();
        t.setMaThuoc(txtMa.getText());
        t.setTenThuoc(txtTen.getText());
        t.setMaDanhMuc(maDM);
        t.setHoatChat(txtHoatChat.getText());
        t.setHangSanXuat(txtHangSX.getText());
        t.setNuocSanXuat(txtNuocSX.getText());
        t.setHamLuong(txtHamLuong.getText());
        t.setDonViCoBan(txtDonVi.getText());
        t.setCanKeDon(chkKeDon.isSelected());
        t.setCongDung(txtCongDung.getText());
        t.setTrieuChung(txtTrieuChung.getText());
        t.setTrangThai(cbTrangThai.getValue());
        t.setHinhAnh(tenFileAnh);

        // 3. Cập nhật vào DB
        if (daoThuoc.capNhatThuoc(t)) {
            new Alert(Alert.AlertType.INFORMATION, "Cập nhật thành công!").show();
            handleHuy();
        } else {
            new Alert(Alert.AlertType.ERROR, "Cập nhật thất bại!").show();
        }
    }

    @FXML void handleHuy() { ((Stage) btnHuy.getScene().getWindow()).close(); }
}