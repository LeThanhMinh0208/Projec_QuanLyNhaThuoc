package gui.dialogs;

import connectDB.ConnectDB;
import entity.PhieuNhap;
import service.Print_PhieuNhap; 
import utils.AlertUtils; 
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.scene.control.Alert;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Dialog_ChiTietPhieuNhapController {

    @FXML private Label lblMaPhieu, lblNhaCungCap, lblNgayNhap, lblNguoiLap, lblTongTien;

    @FXML private TableView<ChiTietUI> tableChiTiet;
    @FXML private TableColumn<ChiTietUI, String> colTenThuoc, colDonVi, colMaLo;
    @FXML private TableColumn<ChiTietUI, Integer> colSoLuong;
    @FXML private TableColumn<ChiTietUI, String> colNgaySX, colHanDung, colGiaNhap, colThanhTien;

    private DecimalFormat df = new DecimalFormat("#,### VNĐ");
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    
    // Biến lưu Phiếu Nhập hiện tại để In
    private PhieuNhap phieuHienTai;

    @FXML public void initialize() {
        colTenThuoc.setCellValueFactory(new PropertyValueFactory<>("tenThuoc"));
        colDonVi.setCellValueFactory(new PropertyValueFactory<>("donVi"));
        colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        colSoLuong.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: #059669;");
        
        colMaLo.setCellValueFactory(new PropertyValueFactory<>("maLo"));
        colMaLo.setStyle("-fx-alignment: CENTER;");
        
        colNgaySX.setCellValueFactory(new PropertyValueFactory<>("ngaySXStr"));
        colHanDung.setCellValueFactory(new PropertyValueFactory<>("hanDungStr"));
        
        colGiaNhap.setCellValueFactory(new PropertyValueFactory<>("giaNhapStr"));
        colGiaNhap.setStyle("-fx-alignment: CENTER-RIGHT;");
        
        colThanhTien.setCellValueFactory(new PropertyValueFactory<>("thanhTienStr"));
        colThanhTien.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: #e11d48;");
    }

    // Hàm nhận dữ liệu từ trang Nhập Kho truyền sang
    public void setPhieuNhap(PhieuNhap pn) {
        this.phieuHienTai = pn; // Gán vào biến toàn cục để dành cho việc IN PDF
        
        lblMaPhieu.setText("Mã phiếu: " + pn.getMaPhieuNhap());
        lblNhaCungCap.setText(pn.getNhaCungCap().getTenNhaCungCap());
        lblNguoiLap.setText(pn.getNhanVien().getHoTen());
        lblNgayNhap.setText(sdf.format(pn.getNgayNhap()));
        lblTongTien.setText(df.format(pn.getTongTien()));

        loadChiTietTuDatabase(pn.getMaPhieuNhap());
    }

    private void loadChiTietTuDatabase(String maPhieu) {
        ObservableList<ChiTietUI> list = FXCollections.observableArrayList();
        
        String sql = "SELECT t.tenThuoc, ctpn.maQuyDoi AS tenDonVi, ctpn.soLuong, ctpn.donGiaNhap, " +
                     "lt.maLoThuoc, lt.ngaySanXuat, lt.hanSuDung " +
                     "FROM ChiTietPhieuNhap ctpn " +
                     "JOIN LoThuoc lt ON ctpn.maLoThuoc = lt.maLoThuoc " +
                     "JOIN Thuoc t ON lt.maThuoc = t.maThuoc " +
                     "WHERE ctpn.maPhieuNhap = ?";

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maPhieu);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                ChiTietUI ct = new ChiTietUI();
                ct.setTenThuoc(rs.getString("tenThuoc"));
                ct.setDonVi(rs.getString("tenDonVi")); 
                ct.setSoLuong(rs.getInt("soLuong"));
                ct.setGiaNhap(rs.getDouble("donGiaNhap"));
                ct.setMaLo(rs.getString("maLoThuoc"));
                ct.setNgaySX(rs.getDate("ngaySanXuat"));
                ct.setHanDung(rs.getDate("hanSuDung"));
                list.add(ct);
            }
            tableChiTiet.setItems(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ===============================================
    // XỬ LÝ NÚT IN PHIẾU NHẬP (TÍNH NĂNG MỚI BỔ SUNG)
    // ===============================================
    @FXML 
    void handleInPhieu(ActionEvent event) {

        if (phieuHienTai != null && tableChiTiet.getItems() != null && !tableChiTiet.getItems().isEmpty()) {
            
            try {
             
                Print_PhieuNhap.inPhieu(phieuHienTai, tableChiTiet.getItems());
                
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Không thể khởi tạo tiến trình in: " + e.getMessage());
            }
            
        } else {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Thông báo", "Phiếu này chưa có chi tiết hàng hóa hoặc dữ liệu trống, không thể in PDF!");
        }
    }

    @FXML void handleDong(ActionEvent event) {
        Stage stage = (Stage) lblMaPhieu.getScene().getWindow();
        stage.close();
    }

    // =========================================================
    // INNER CLASS DÙNG ĐỂ HIỂN THỊ LÊN BẢNG (MÌ ĂN LIỀN)
    // =========================================================
    public class ChiTietUI {
        private String tenThuoc, donVi, maLo;
        private int soLuong;
        private double giaNhap;
        private Date ngaySX, hanDung;

        public String getTenThuoc() { return tenThuoc; }
        public void setTenThuoc(String tenThuoc) { this.tenThuoc = tenThuoc; }
        public String getDonVi() { return donVi; }
        public void setDonVi(String donVi) { this.donVi = donVi; }
        public String getMaLo() { return maLo; }
        public void setMaLo(String maLo) { this.maLo = maLo; }
        public int getSoLuong() { return soLuong; }
        public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
        public double getGiaNhap() { return giaNhap; }
        public void setGiaNhap(double giaNhap) { this.giaNhap = giaNhap; }
        public Date getNgaySX() { return ngaySX; }
        public void setNgaySX(Date ngaySX) { this.ngaySX = ngaySX; }
        public Date getHanDung() { return hanDung; }
        public void setHanDung(Date hanDung) { this.hanDung = hanDung; }

        public String getGiaNhapStr() { return df.format(giaNhap); }
        public String getThanhTienStr() { return df.format(giaNhap * soLuong); }
        public String getNgaySXStr() { return ngaySX != null ? sdf.format(ngaySX) : "---"; }
        public String getHanDungStr() { return hanDung != null ? sdf.format(hanDung) : "---"; }
    }
}