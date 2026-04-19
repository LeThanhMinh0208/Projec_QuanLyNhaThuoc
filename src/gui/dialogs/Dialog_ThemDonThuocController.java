package gui.dialogs;

import entity.DonThuoc;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

public class Dialog_ThemDonThuocController {

    @FXML private Label lblTitle;
    @FXML private TextField txtMaDon;
    @FXML private TextField txtTenBacSi;
    @FXML private TextField txtBenhNhan;
    @FXML private TextField txtChanDoan;

    @FXML private Label lblErrBacSi;
    @FXML private Label lblErrBenhNhan;

    // Ảnh đơn thuốc
    @FXML private ImageView imgPreviewDon;
    @FXML private Button btnChonAnhDon;
    @FXML private Label lblTenAnhDon;

    private DonThuoc resultDonThuoc;
    private boolean isEdit = false;
    private File selectedAnhDonFile = null;

    private static final String DON_THUOC_IMAGE_DIR =
        "src/resources/images/images_donthuoc/";

    public void setDonThuoc(DonThuoc dt, String nextMa) {
        if (dt != null) {
            isEdit = true;
            lblTitle.setText("SỬA / TÁI LẬP THÔNG TIN ĐƠN THUỐC");
            
            // 1. Đổ dữ liệu TEXT vào các ô nhập liệu (Sếp lỡ xóa mất đoạn này nè)
            txtMaDon.setText(dt.getMaDonThuoc());
            txtTenBacSi.setText(dt.getTenBacSi());
            txtBenhNhan.setText(dt.getThongTinBenhNhan());
            txtChanDoan.setText(dt.getChanDoan());

            // 2. Đổ dữ liệu ẢNH vào form và đánh lừa hệ thống
            if (dt.getHinhAnhDon() != null && !dt.getHinhAnhDon().trim().isEmpty() && !dt.getHinhAnhDon().equals("url_hinh_anh")) {
                

                selectedAnhDonFile = new File(DON_THUOC_IMAGE_DIR + dt.getHinhAnhDon());
                
                // Hiển thị text báo thành công
                lblTenAnhDon.setText("✔ Đã tái lập ảnh: " + dt.getHinhAnhDon());
                lblTenAnhDon.setStyle("-fx-text-fill: #388e3c; -fx-font-weight: bold;");

                // Load ảnh lên khung Preview
                try {
                    if (selectedAnhDonFile.exists()) {
                        imgPreviewDon.setImage(new Image(selectedAnhDonFile.toURI().toString()));
                    } else {
                        var stream = getClass().getResourceAsStream("/resources/images/images_donthuoc/" + dt.getHinhAnhDon());
                        if (stream != null) imgPreviewDon.setImage(new Image(stream));
                    }
                } catch (Exception ignored) {
                    System.err.println("Không thể load preview ảnh.");
                }
            }
        } else {
            isEdit = false;
            lblTitle.setText("THÊM THÔNG TIN ĐƠN THUỐC");
            txtMaDon.setText(nextMa);
        }
    }

    public DonThuoc getResultDonThuoc() {
        return resultDonThuoc;
    }

    @FXML
    void handleChonAnhDon(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Chọn ảnh đơn thuốc");
        fc.getExtensionFilters().add(
            new FileChooser.ExtensionFilter(
                "Ảnh", "*.jpg", "*.jpeg", "*.png", "*.gif"));
        File file = fc.showOpenDialog(btnChonAnhDon.getScene().getWindow());
        if (file != null) {
            selectedAnhDonFile = file;
            imgPreviewDon.setImage(new Image(file.toURI().toString()));
            lblTenAnhDon.setText("✔ " + file.getName());
            lblTenAnhDon.setStyle(
                "-fx-text-fill: #388e3c; -fx-font-size: 11px;");
        }
    }

    private String copyAnhDonVaoThuMuc(File sourceFile, String maDonThuoc)
            throws IOException {
        Path destDir = Paths.get(DON_THUOC_IMAGE_DIR);
        Files.createDirectories(destDir);

        String original = sourceFile.getName();
        String ext = original.substring(original.lastIndexOf('.'));
        String newFileName = maDonThuoc + ext; // ví dụ: DT0001.jpg
        Path destPath = destDir.resolve(newFileName);

        Files.copy(sourceFile.toPath(), destPath,
            StandardCopyOption.REPLACE_EXISTING);
        return newFileName;
    }

    @FXML
    void handleLuu(ActionEvent event) {
        boolean valid = true;
        
        String bsi = txtTenBacSi.getText().trim();
        String bnhan = txtBenhNhan.getText().trim();
        String cdoan = txtChanDoan.getText().trim();

        if (bsi.isEmpty()) {
            lblErrBacSi.setText("Tên bác sĩ không được để trống!");
            lblErrBacSi.setVisible(true);
            lblErrBacSi.setManaged(true);
            valid = false;
        } else {
            lblErrBacSi.setVisible(false);
            lblErrBacSi.setManaged(false);
        }

        if (bnhan.isEmpty()) {
            lblErrBenhNhan.setText("Thông tin bệnh nhân không được để trống!");
            lblErrBenhNhan.setVisible(true);
            lblErrBenhNhan.setManaged(true);
            valid = false;
        } else {
            lblErrBenhNhan.setVisible(false);
            lblErrBenhNhan.setManaged(false);
        }

        // Validate bắt buộc ảnh — chặn lưu nếu chưa chọn
        if (selectedAnhDonFile == null) {
            new Alert(Alert.AlertType.WARNING,
                "Đơn thuốc bắt buộc phải có ảnh!\n" +
                "Vui lòng bấm 'Chọn ảnh đơn thuốc' trước khi lưu.").showAndWait();
            valid = false;
        }

        if (!valid) return;

        // Copy ảnh
        String tenAnhDon = null;
        try {
            String maDon = txtMaDon.getText();
            // Nếu file đã nằm trong thư mục đích (trường hợp sửa, ko chọn ảnh mới)
            Path destDir = Paths.get(DON_THUOC_IMAGE_DIR);
            if (selectedAnhDonFile.toPath().startsWith(destDir)) {
                // Ảnh cũ, giữ nguyên tên
                tenAnhDon = selectedAnhDonFile.getName();
            } else {
                tenAnhDon = copyAnhDonVaoThuMuc(selectedAnhDonFile, maDon);
            }
        } catch (IOException e) {
            new Alert(Alert.AlertType.ERROR,
                "Không thể lưu ảnh đơn thuốc: " + e.getMessage()).showAndWait();
            return;
        }

        resultDonThuoc = new DonThuoc();
        resultDonThuoc.setMaDonThuoc(txtMaDon.getText());
        resultDonThuoc.setTenBacSi(bsi);
        resultDonThuoc.setThongTinBenhNhan(bnhan);
        resultDonThuoc.setChanDoan(cdoan);
        resultDonThuoc.setHinhAnhDon(tenAnhDon);
        
        closeDialog();
    }

    @FXML
    void handleDong(ActionEvent event) {
        resultDonThuoc = null;
        closeDialog();
    }

    private void closeDialog() {
        Stage stage = (Stage) txtMaDon.getScene().getWindow();
        stage.close();
    }
}
