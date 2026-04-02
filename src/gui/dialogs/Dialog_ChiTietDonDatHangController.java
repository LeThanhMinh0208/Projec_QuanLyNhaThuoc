package gui.dialogs;

import dao.DAO_DonDatHang;
import entity.ChiTietDonDatHang;
import entity.DonDatHang;
import service.Print_HoaDonDatHang;
import utils.AlertUtils; 

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

public class Dialog_ChiTietDonDatHangController {

    @FXML private Label lblMaDon, lblNgayLap, lblNhaCungCap, lblNhanVien, lblGhiChu, lblTongTienDuKien;
    @FXML private TableView<ChiTietDonDatHang> tableChiTiet;
    
    @FXML private TableColumn<ChiTietDonDatHang, String> colTenThuoc, colDonVi, colMaLo, colHanDung, colGiaNhap, colThanhTien, colTinhTrang, colTienDo;
    @FXML private TableColumn<ChiTietDonDatHang, Integer> colSoLuongDat, colSoLuongNhan;
    
    @FXML private Button btnHuyDon;
    
    private DAO_DonDatHang dao = new DAO_DonDatHang();
    private DecimalFormat df = new DecimalFormat("#,### VNĐ");
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    
    private DonDatHang donHienTai;
    private List<ChiTietDonDatHang> listChiTiet;

    public void setDonDatHang(DonDatHang don) {
        this.donHienTai = don;
        
        lblMaDon.setText("Mã đơn: " + don.getMaDonDatHang());
        lblNgayLap.setText(sdf.format(don.getNgayDat()));
        lblNhaCungCap.setText(don.getNhaCungCap().getTenNhaCungCap());
        lblNhanVien.setText(don.getNhanVien().getHoTen());
        lblGhiChu.setText(don.getGhiChu() != null && !don.getGhiChu().isEmpty() ? don.getGhiChu() : "---");

        // ==========================================
        // LOGIC KHÓA NÚT HỦY ĐƠN (Đã chuẩn)
        // ==========================================
        String trangThai = don.getTrangThaiHang();
        String trangThaiGocDB = don.getTrangThai(); 

        if (trangThai != null && 
           (trangThai.contains("Hoàn Thành") || 
            trangThai.contains("Hủy") || 
            "GIAO_MOT_PHAN".equals(trangThaiGocDB) || 
            "DONG_DON_THIEU".equals(trangThaiGocDB) || 
            "GIAO_DU".equals(trangThaiGocDB))) {
            
            btnHuyDon.setDisable(true);
            btnHuyDon.setStyle("-fx-background-color: #fca5a5; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
            
        } else {
            btnHuyDon.setDisable(false);
            btnHuyDon.setStyle("-fx-background-color: linear-gradient(to bottom, #ef4444, #dc2626); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        }

        setupTable();

        listChiTiet = dao.getChiTietByMaDon(don.getMaDonDatHang());
        tableChiTiet.setItems(FXCollections.observableArrayList(listChiTiet));
        
        tinhTongTienDuKien();
    }

    private void tinhTongTienDuKien() {
        double tong = 0;
        if (listChiTiet != null) {
            for (ChiTietDonDatHang ct : listChiTiet) {
                tong += ct.getThanhTien();
            }
        }
        lblTongTienDuKien.setText(df.format(tong));
    }

    private void setupTable() {
        colTenThuoc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getThuoc().getTenThuoc()));
        colDonVi.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDonViQuyDoi().getTenDonVi()));
        
        colSoLuongDat.setCellValueFactory(new PropertyValueFactory<>("soLuongDat"));
        colSoLuongDat.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");
        
        colSoLuongNhan.setCellValueFactory(new PropertyValueFactory<>("soLuongDaNhan"));
        colSoLuongNhan.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: #10b981;");

        colMaLo.setCellValueFactory(c -> {
            String lo = c.getValue().getMaLo();
            return new SimpleStringProperty((lo != null && !lo.isEmpty()) ? lo : "---");
        });
        colMaLo.setStyle("-fx-alignment: CENTER;");

        colHanDung.setCellValueFactory(c -> {
            String hd = c.getValue().getHanSuDung();
            return new SimpleStringProperty((hd != null && !hd.isEmpty()) ? hd : "---");
        });
        colHanDung.setStyle("-fx-alignment: CENTER;");
        
        colGiaNhap.setCellValueFactory(c -> new SimpleStringProperty(df.format(c.getValue().getDonGiaDuKien())));
        colGiaNhap.setStyle("-fx-alignment: CENTER-RIGHT;");
        
        colThanhTien.setCellValueFactory(c -> new SimpleStringProperty(df.format(c.getValue().getThanhTien())));
        colThanhTien.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: #ef4444;");

        // ==========================================
        // FIX: LOGIC CỘT TÌNH TRẠNG 
        // ==========================================
        colTinhTrang.setCellValueFactory(c -> {
            int dat = c.getValue().getSoLuongDat();
            int nhan = c.getValue().getSoLuongDaNhan();
            String maTrangThaiDB = donHienTai.getTrangThai(); // Lấy mã gốc DB
            
            String status = (nhan == 0) ? "Chờ Nhập" : (nhan < dat) ? "Thiếu Hàng" : "Nhập Đủ";
            
            if ("DA_HUY".equals(maTrangThaiDB)) {
                status = "Đã Hủy";
            } 
            else if ("GIAO_MOT_PHAN".equals(maTrangThaiDB) || "DONG_DON_THIEU".equals(maTrangThaiDB)) {
                 if (nhan > 0 && nhan < dat) {
                     status = "Chốt Thiếu"; 
                 }
            }
            return new SimpleStringProperty(status);
        });
        colTinhTrang.setStyle("-fx-alignment: CENTER; -fx-text-fill: #0284c7; -fx-font-weight: bold;");

        // ==========================================
        // FIX: LOGIC CỘT TIẾN ĐỘ 
        // ==========================================
        colTienDo.setCellValueFactory(c -> {
            int dat = c.getValue().getSoLuongDat();
            int nhan = c.getValue().getSoLuongDaNhan();
            String maTrangThaiDB = donHienTai.getTrangThai();
            
            String tienDoStr = "Đang Xử Lý";
            
            if ("DA_HUY".equals(maTrangThaiDB)) {
                tienDoStr = "Đã Hủy";
            } 
            else if ("GIAO_DU".equals(maTrangThaiDB) || "GIAO_MOT_PHAN".equals(maTrangThaiDB) || "DONG_DON_THIEU".equals(maTrangThaiDB)) {
                tienDoStr = "Hoàn Thành";
            } 
            else if (nhan >= dat && dat > 0) {
                tienDoStr = "Hoàn Thành";
            }
            
            return new SimpleStringProperty(tienDoStr);
        });
        
        // TÔ MÀU CHO CỘT TIẾN ĐỘ TỰ ĐỘNG
        colTienDo.setCellFactory(column -> new TableCell<ChiTietDonDatHang, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item);
                    if (item.equals("Hoàn Thành")) {
                        setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: #10b981;"); // Màu xanh lá
                    } else if (item.equals("Đã Hủy")) {
                        setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: #ef4444;"); // Màu đỏ
                    } else {
                        setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: #f59e0b;"); // Màu cam cho Đang Xử lý
                    }
                }
            }
        });
    }

    // ==========================================
    // XỬ LÝ SỰ KIỆN NÚT HỦY ĐƠN
    // ==========================================
    @FXML void handleHuyDon(ActionEvent event) {
        if (donHienTai.getTrangThaiHang().contains("Hoàn Thành") || "GIAO_MOT_PHAN".equals(donHienTai.getTrangThai())) {
            AlertUtils.showAlert(AlertType.WARNING, "Cảnh báo", "Đơn hàng này đã chốt/hoàn thành, không thể hủy!");
            return;
        }
        
        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận hủy đơn");
        alert.setHeaderText("Bạn có chắc chắn muốn HỦY đơn đặt hàng " + donHienTai.getMaDonDatHang() + " không?");
        alert.setContentText("Lưu ý: NCC sẽ không giao hàng cho đơn này nữa.\nThao tác này không thể hoàn tác!");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            
            boolean isSuccess = dao.updateTrangThaiDonHang(donHienTai.getMaDonDatHang(), "DA_HUY");
            
            if (isSuccess) {
                donHienTai.setTrangThai("DA_HUY"); // Cập nhật Object hiện tại
                tableChiTiet.refresh(); // Làm mới cột tiến độ thành "Đã Hủy"
                btnHuyDon.setDisable(true); // Khóa nút sau khi hủy xong
                btnHuyDon.setStyle("-fx-background-color: #fca5a5; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
                
                AlertUtils.showAlert(AlertType.INFORMATION, "Thành công", "Đã hủy đơn hàng thành công!");
            } else {
                AlertUtils.showAlert(AlertType.ERROR, "Lỗi", "Có lỗi xảy ra khi cập nhật CSDL. Vui lòng thử lại!");
            }
        }
    }

    @FXML void handleInDonHang(ActionEvent event) {
        if(donHienTai != null && listChiTiet != null) {
            Print_HoaDonDatHang.inHoaDon(donHienTai, listChiTiet);
        }
    }

    @FXML void handleDong(ActionEvent event) {
        Stage stage = (Stage) lblMaDon.getScene().getWindow();
        stage.close();
    }
}