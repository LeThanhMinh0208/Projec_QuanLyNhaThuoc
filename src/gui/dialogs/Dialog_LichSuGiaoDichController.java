package gui.dialogs;

import dao.DAO_GiaoDichKhachHang;
import entity.GiaoDichKhachHang;
import entity.HoaDonView;
import entity.KhachHang;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class Dialog_LichSuGiaoDichController {

    @FXML private Label lblTieuDeKH;
    @FXML private Label lblTongHD, lblTongTien, lblDiem, lblLanMuaCuoi, lblSoGiaoDich;
    @FXML private TextField txtTimKiem;
    @FXML private ComboBox<String> cbHinhThuc;
    @FXML private DatePicker dpTuNgay, dpDenNgay;
    
    @FXML private TableView<GiaoDichKhachHang> tableGiaoDich;
    @FXML private TableColumn<GiaoDichKhachHang, String> colMaHD, colNgayLap, colNhanVien;
    @FXML private TableColumn<GiaoDichKhachHang, String> colTamTinh, colVAT, colTongTT, colHinhThuc;
    @FXML private TableColumn<GiaoDichKhachHang, Void> colHanhDong;

    private KhachHang khachHang;
    private final DAO_GiaoDichKhachHang dao = new DAO_GiaoDichKhachHang();
    private final ObservableList<GiaoDichKhachHang> masterData = FXCollections.observableArrayList();
    private FilteredList<GiaoDichKhachHang> filteredData;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.of("vi", "VN"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    private final SimpleDateFormat shortDateFormat = new SimpleDateFormat("dd/MM/yyyy");

    @FXML
    public void initialize() {
        cbHinhThuc.setItems(FXCollections.observableArrayList("Tất cả", "Tiền mặt", "Chuyển khoản", "Thẻ tín dụng"));
        cbHinhThuc.getSelectionModel().selectFirst();
        setupTable();
    }

    public void setKhachHang(KhachHang kh) {
        this.khachHang = kh;
        lblTieuDeKH.setText("Xem toàn bộ lịch sử mua hàng của khách hàng: " + kh.getHoTen() + " - " + kh.getSdt());
        lblDiem.setText(String.valueOf(kh.getDiemTichLuy()));
        
        loadData();
    }

    private void setupTable() {
        colMaHD.setCellValueFactory(new PropertyValueFactory<>("maHoaDon"));
        colNhanVien.setCellValueFactory(new PropertyValueFactory<>("tenNhanVien"));
        
        colNgayLap.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getNgayLap() != null ? dateFormat.format(c.getValue().getNgayLap()) : ""));
            
        colTamTinh.setCellValueFactory(c -> new SimpleStringProperty(currencyFormat.format(c.getValue().getTamTinh())));
        colVAT.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getThueVAT() + "%"));
        
        // Đổi màu xanh cho cột Tổng TT giống ảnh thiết kế
        colTongTT.setCellValueFactory(c -> new SimpleStringProperty(currencyFormat.format(c.getValue().getTongSauVAT())));
        colTongTT.setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");

        colHinhThuc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHinhThucLabel()));

        colHanhDong.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Chi tiết");
            {
                // Nút bo tròn nền trắng chữ xám giống ảnh mockup
                btn.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 6; -fx-background-radius: 6; -fx-cursor: hand; -fx-text-fill: #475569;");
                btn.setOnAction(e -> moDialogChiTiet(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void loadData() {
        List<GiaoDichKhachHang> list = dao.getByKhachHang(khachHang.getMaKhachHang(), null, null, null);
        masterData.setAll(list);
        
        // Tính toán các Thẻ Thống Kê
        double[] thongKe = dao.getThongKe(khachHang.getMaKhachHang());
        lblTongHD.setText(String.valueOf((int) thongKe[0]));
        lblTongTien.setText(currencyFormat.format(thongKe[1]));
        
        // Tìm ngày mua gần nhất
        Optional<GiaoDichKhachHang> gdMoiNhat = list.stream()
                .filter(g -> g.getNgayLap() != null)
                .max(Comparator.comparing(GiaoDichKhachHang::getNgayLap));
        
        if (gdMoiNhat.isPresent()) {
            lblLanMuaCuoi.setText(shortDateFormat.format(gdMoiNhat.get().getNgayLap()));
        } else {
            lblLanMuaCuoi.setText("Chưa mua");
        }

        setupSearchAndFilter();
    }

    private void setupSearchAndFilter() {
        filteredData = new FilteredList<>(masterData, p -> true);
        
        txtTimKiem.textProperty().addListener((obs, old, newVal) -> updateFilter());
        cbHinhThuc.valueProperty().addListener((obs, old, newVal) -> updateFilter());
        dpTuNgay.valueProperty().addListener((obs, old, newVal) -> updateFilter());
        dpDenNgay.valueProperty().addListener((obs, old, newVal) -> updateFilter());
        
        tableGiaoDich.setItems(filteredData);
        updateFilter();
    }

    private void updateFilter() {
        filteredData.setPredicate(gd -> {
            String keyword = txtTimKiem.getText().toLowerCase().trim();
            boolean matchText = keyword.isEmpty() || gd.getMaHoaDon().toLowerCase().contains(keyword);
                                
            String ht = cbHinhThuc.getValue();
            boolean matchHT = "Tất cả".equals(ht) || ht == null || gd.getHinhThucLabel().equals(ht);
            
            boolean matchDate = true;
            if (gd.getNgayLap() != null) {
                LocalDate date = gd.getNgayLap().toLocalDateTime().toLocalDate();
                if (dpTuNgay.getValue() != null && date.isBefore(dpTuNgay.getValue())) matchDate = false;
                if (dpDenNgay.getValue() != null && date.isAfter(dpDenNgay.getValue())) matchDate = false;
            }
            return matchText && matchHT && matchDate;
        });
        lblSoGiaoDich.setText(filteredData.size() + " giao dịch");
    }

    @FXML void handleXoaLoc(ActionEvent event) {
        txtTimKiem.clear();
        cbHinhThuc.getSelectionModel().selectFirst();
        dpTuNgay.setValue(null);
        dpDenNgay.setValue(null);
    }

    private void moDialogChiTiet(GiaoDichKhachHang gd) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_ChiTietHoaDon.fxml"));
            Parent root = loader.load();
            gui.dialogs.Dialog_ChiTietHoaDonController ctrl = loader.getController();
            
            HoaDonView hd = new HoaDonView();
            hd.setMaHoaDon(gd.getMaHoaDon());
            hd.setNgayLap(gd.getNgayLap());
            hd.setTenNhanVien(gd.getTenNhanVien());
            hd.setTenKhachHang(khachHang.getHoTen());
            hd.setSdt(khachHang.getSdt());
            hd.setTamTinh(gd.getTamTinh());
            hd.setThueVAT(gd.getThueVAT());
            hd.setTongSauVAT(gd.getTongSauVAT());
            hd.setHinhThucThanhToan(gd.getHinhThucThanhToan());
            hd.setGhiChu(gd.getGhiChu());
            ctrl.setHoaDon(hd);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Chi Tiết Hóa Đơn — " + gd.getMaHoaDon());
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }
}