package gui.main;

import dao.DAO_GiaoDichKhachHang;
import entity.GiaoDichKhachHang;
import entity.HoaDonView;
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
import java.util.List;
import java.util.Locale;

public class GUI_LichSuGiaoDichController {

    @FXML private TextField txtTimKiem;
    @FXML private ComboBox<String> cbHinhThuc;
    @FXML private DatePicker dpTuNgay, dpDenNgay;
    
    @FXML private TableView<GiaoDichKhachHang> tableGiaoDich;
    // Khai báo cột mới
    @FXML private TableColumn<GiaoDichKhachHang, String> colMaHD, colNgayLap, colKhachHang, colSdt;
    @FXML private TableColumn<GiaoDichKhachHang, String> colTamTinh, colVAT, colTongTT, colHinhThuc;
    @FXML private TableColumn<GiaoDichKhachHang, Void> colHanhDong;
    
    @FXML private Label lblTongGiaoDich, lblTongDoanhThu;

    private final DAO_GiaoDichKhachHang dao = new DAO_GiaoDichKhachHang();
    private final ObservableList<GiaoDichKhachHang> masterData = FXCollections.observableArrayList();
    private FilteredList<GiaoDichKhachHang> filteredData;

    private final NumberFormat currencyFormat = NumberFormat.getCurrencyInstance(Locale.of("vi", "VN"));
    private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        setupComboBox();
        setupTable();
        loadData(); 
        setupFilter();
    }

    private void setupComboBox() {
        cbHinhThuc.setItems(FXCollections.observableArrayList("Tất cả", "Tiền mặt", "Chuyển khoản", "Thẻ tín dụng"));
        cbHinhThuc.getSelectionModel().selectFirst();
        dpTuNgay.setValue(null);
        dpDenNgay.setValue(null);
    }

    private void setupTable() {
        colMaHD.setCellValueFactory(new PropertyValueFactory<>("maHoaDon"));
        colKhachHang.setCellValueFactory(new PropertyValueFactory<>("tenKhachHang")); // Cột Khách hàng
        colSdt.setCellValueFactory(new PropertyValueFactory<>("sdtKhachHang"));       // Cột SĐT
        
        colNgayLap.setCellValueFactory(c -> new SimpleStringProperty(
            c.getValue().getNgayLap() != null ? dateFormat.format(c.getValue().getNgayLap()) : ""));
            
        colTamTinh.setCellValueFactory(c -> new SimpleStringProperty(currencyFormat.format(c.getValue().getTamTinh())));
        colVAT.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getThueVAT() + "%"));
        colTongTT.setCellValueFactory(c -> new SimpleStringProperty(currencyFormat.format(c.getValue().getTongSauVAT())));
        colHinhThuc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getHinhThucLabel()));

        colHanhDong.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("👁 Chi tiết");
            {
                btn.setStyle("-fx-background-color:#2563eb;-fx-text-fill:white;-fx-font-size:12px;-fx-padding:4 10;-fx-cursor:hand;");
                btn.setOnAction(e -> moDialogChiTiet(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void loadData() {
        List<GiaoDichKhachHang> list = dao.getAllGiaoDich(); 
        masterData.setAll(list);
    }

    private void setupFilter() {
        filteredData = new FilteredList<>(masterData, p -> true);
        tableGiaoDich.setItems(filteredData);
        applyFilter(); 
        
        txtTimKiem.textProperty().addListener((obs, oldVal, newVal) -> applyFilter());
    }

    @FXML void handleLoc(ActionEvent event) { applyFilter(); }

    @FXML void handleXoaLoc(ActionEvent event) {
        txtTimKiem.clear();
        cbHinhThuc.getSelectionModel().selectFirst();
        dpTuNgay.setValue(null);
        dpDenNgay.setValue(null);
        applyFilter();
    }

    private void applyFilter() {
        filteredData.setPredicate(gd -> {
            String keyword = txtTimKiem.getText().toLowerCase().trim();
            
            // Tìm theo Mã HĐ, Tên KH, hoặc SĐT KH
            boolean matchText = keyword.isEmpty() || 
                                (gd.getMaHoaDon() != null && gd.getMaHoaDon().toLowerCase().contains(keyword)) ||
                                (gd.getTenKhachHang() != null && gd.getTenKhachHang().toLowerCase().contains(keyword)) ||
                                (gd.getSdtKhachHang() != null && gd.getSdtKhachHang().contains(keyword));
                                
            String ht = cbHinhThuc.getValue();
            boolean matchHT = "Tất cả".equals(ht) || ht == null || (gd.getHinhThucLabel() != null && gd.getHinhThucLabel().equals(ht));
            
            boolean matchDate = true;
            if (gd.getNgayLap() != null) {
                LocalDate date = gd.getNgayLap().toLocalDateTime().toLocalDate();
                if (dpTuNgay.getValue() != null && date.isBefore(dpTuNgay.getValue())) matchDate = false;
                if (dpDenNgay.getValue() != null && date.isAfter(dpDenNgay.getValue())) matchDate = false;
            }
            return matchText && matchHT && matchDate;
        });
        
        double tongThu = 0;
        for(GiaoDichKhachHang gd : filteredData) { tongThu += gd.getTongSauVAT(); }
        
        lblTongGiaoDich.setText("Tổng: " + filteredData.size() + " giao dịch");
        lblTongDoanhThu.setText(currencyFormat.format(tongThu));
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
            hd.setTenKhachHang(gd.getTenKhachHang());
            hd.setSdt(gd.getSdtKhachHang());
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