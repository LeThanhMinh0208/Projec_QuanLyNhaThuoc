package gui.dialogs;

import dao.DAO_DonNhapHang;
import entity.ChiTietDonNhapHang;
import entity.DonNhapHang;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.text.SimpleDateFormat;
import java.util.List;

public class Dialog_ChiTietDonDatHangController {
    // ĐÃ XÓA lblTongTien Ở ĐÂY
    @FXML private Label lblMaDon, lblTrangThai, lblNgayLap, lblNhaCungCap, lblNhanVien, lblNgayHen, lblGhiChu;
    @FXML private TableView<ChiTietDonNhapHang> tableChiTiet;
    @FXML private TableColumn<ChiTietDonNhapHang, String> colTenThuoc, colDonVi;
    @FXML private TableColumn<ChiTietDonNhapHang, Integer> colSoLuongDat, colSoLuongNhan;
    @FXML private TableColumn<ChiTietDonNhapHang, Double> colGiaNhap, colThanhTien;
    @FXML private Label lblTongTienDuKien;
    private DAO_DonNhapHang daoDonNhap = new DAO_DonNhapHang();
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    private DonNhapHang currentDon;

    public void setDetailData(DonNhapHang don) {
        // 1. Set thông tin cơ bản
    	this.currentDon = don;
        lblMaDon.setText("Mã đơn: " + don.getMaDonNhap());
        lblNgayLap.setText("Ngày lập: " + sdf.format(don.getNgayLap()));
        lblNhaCungCap.setText(don.getNhaCungCap().getTenNhaCungCap());
        lblNhanVien.setText(don.getNhanVien().getMaNhanVien());
        lblNgayHen.setText(don.getNgayHenGiao() != null ? sdf.format(don.getNgayHenGiao()) : "---");
        
        // Xử lý ghi chú: nếu trống thì hiện "Không có" cho chuyên nghiệp
        String ghiChu = (don.getGhiChu() == null || don.getGhiChu().trim().isEmpty()) ? "Không có" : don.getGhiChu();
        lblGhiChu.setText(ghiChu);

        // 2. Xử lý Badge Trạng thái
        lblTrangThai.getStyleClass().removeAll("status-badge", "status-waiting", "status-completed");
        lblTrangThai.getStyleClass().add("status-badge");

        if ("DA_NHAP_KHO".equals(don.getTrangThai()) || "HOAN_THANH".equals(don.getTrangThai())) {
            lblTrangThai.setText("HOÀN THÀNH");
            lblTrangThai.getStyleClass().add("status-completed");
        } else {
            lblTrangThai.setText("ĐANG CHỜ");
            lblTrangThai.getStyleClass().add("status-waiting");
        }

        // 3. Khởi tạo bảng và nạp dữ liệu
        setupTable();
        loadDuLieuThuoc(don.getMaDonNhap());
    }

    private void setupTable() {
        colTenThuoc.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getThuoc().getTenThuoc()));
        colDonVi.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDonViQuyDoi().getTenDonVi()));
        colSoLuongDat.setCellValueFactory(new PropertyValueFactory<>("soLuongDat"));
        colSoLuongNhan.setCellValueFactory(new PropertyValueFactory<>("soLuongDaNhan"));
        colGiaNhap.setCellValueFactory(new PropertyValueFactory<>("donGiaDuKien"));
        colGiaNhap.setCellFactory(column -> formatCurrencyCell());
        
        // FIX: Thành tiền trên bảng = SL Đặt * Đơn giá (Cho đúng nghĩa dự tính)
        colThanhTien.setCellValueFactory(cell -> {
            ChiTietDonNhapHang ct = cell.getValue();
            double result = ct.getSoLuongDat() * ct.getDonGiaDuKien();
            return new javafx.beans.property.SimpleDoubleProperty(result).asObject();
        });
        colThanhTien.setCellFactory(column -> formatCurrencyCell());
       
        tableChiTiet.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
    }

    private void loadDuLieuThuoc(String maDon) {
        List<ChiTietDonNhapHang> list = daoDonNhap.getChiTietByMaDon(maDon);
        tableChiTiet.setItems(FXCollections.observableArrayList(list));
        
        double tongDuKien = 0;

        for (ChiTietDonNhapHang ct : list) {
            // Tổng dự kiến: Theo số lượng sếp ĐẶT
            tongDuKien += ct.getSoLuongDat() * ct.getDonGiaDuKien();
        }

        lblTongTienDuKien.setText(String.format("%,.0f VNĐ", tongDuKien));
    }
    
    private TableCell<ChiTietDonNhapHang, Double> formatCurrencyCell() {
        return new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : String.format("%,.0f đ", item));
            }
        };
    }
    
    @FXML
    void handleInDonHang(ActionEvent event) {
        if (currentDon != null) {
            // Lấy lại danh sách chi tiết mới nhất từ database
            List<ChiTietDonNhapHang> list = daoDonNhap.getChiTietByMaDon(currentDon.getMaDonNhap());
            
            // Gọi service in hóa đơn của sếp
            service.Print_HoaDonDatHang.inHoaDon(currentDon, list);
        } else {
            new Alert(Alert.AlertType.WARNING, "Không tìm thấy dữ liệu đơn hàng để in!").show();
        }
    }

    @FXML void handleDong(ActionEvent event) {
        ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
    }
}