package gui.main;

import dao.DAO_DonNhapHang;
import entity.DonNhapHang;
import gui.dialogs.Dialog_ChiTietPhieuNhapController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.*;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ResourceBundle;

public class GUI_DanhMucPhieuNhapController implements Initializable {
    @FXML private TableView<DonNhapHang> tablePhieuNhap;
    @FXML private TableColumn<DonNhapHang, String> colMaPhieu, colNhaCungCap, colNhanVien, colGhiChu;
    @FXML private TableColumn<DonNhapHang, java.sql.Date> colNgayNhap;
    @FXML private TableColumn<DonNhapHang, Double> colTongTien;
    @FXML private DatePicker dpTuNgay, dpDenNgay;
    @FXML private TextField txtTimKiem;

    private DAO_DonNhapHang daoDonNhap = new DAO_DonNhapHang();
    private ObservableList<DonNhapHang> listPhieu = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadData();
        setupSearch();
    }

    private void setupTable() {
        colMaPhieu.setCellValueFactory(new PropertyValueFactory<>("maDonNhap"));
        colNgayNhap.setCellValueFactory(new PropertyValueFactory<>("ngayLap"));
        colNgayNhap.setCellFactory(c -> new TableCell<>() {
            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
            @Override protected void updateItem(java.sql.Date item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : format.format(item));
            }
        });
        colNhaCungCap.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNhaCungCap().getTenNhaCungCap()));
        colNhanVien.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getNhanVien().getHoTen()));
        colTongTien.setCellValueFactory(new PropertyValueFactory<>("tongTienDuTinh"));
        colTongTien.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) setText(null);
                else {
                    setText(String.format("%,.0f VNĐ", item));
                    setStyle("-fx-text-fill: #be123c; -fx-font-weight: bold;");
                }
            }
        });
        colGhiChu.setCellValueFactory(new PropertyValueFactory<>("ghiChu"));
    }

    private void loadData() { listPhieu.setAll(daoDonNhap.getDonHangDaNhap()); tablePhieuNhap.setItems(listPhieu); }

    private void setupSearch() {
        FilteredList<DonNhapHang> filteredData = new FilteredList<>(listPhieu, p -> true);
        javafx.beans.value.ChangeListener<Object> listener = (o, oldV, newV) -> {
            filteredData.setPredicate(phieu -> {
                String search = txtTimKiem.getText() == null ? "" : txtTimKiem.getText().toLowerCase();
                boolean mSearch = phieu.getMaDonNhap().toLowerCase().contains(search) || phieu.getNhaCungCap().getTenNhaCungCap().toLowerCase().contains(search);
                boolean mDate = true;
                java.time.LocalDate ngayPhieu = phieu.getNgayLap().toLocalDate();
                if (dpTuNgay.getValue() != null && ngayPhieu.isBefore(dpTuNgay.getValue())) mDate = false;
                if (dpDenNgay.getValue() != null && ngayPhieu.isAfter(dpDenNgay.getValue())) mDate = false;
                return mSearch && mDate;
            });
        };
        txtTimKiem.textProperty().addListener(listener); dpTuNgay.valueProperty().addListener(listener); dpDenNgay.valueProperty().addListener(listener);
        tablePhieuNhap.setItems(filteredData);
    }

    @FXML void handleChuyenTrangLapPhieu(ActionEvent event) { utils.SceneUtils.switchPage("/gui/main/GUI_NhapKho.fxml"); }
    @FXML void handleChuyenTrangDanhSach(ActionEvent event) {}
    @FXML void handleLamMoi(ActionEvent event) { loadData(); }

    @FXML
    void handleXemChiTiet(ActionEvent event) {
        DonNhapHang phieu = tablePhieuNhap.getSelectionModel().getSelectedItem();
        if (phieu == null) { new Alert(Alert.AlertType.WARNING, "Chọn phiếu cần xem!").show(); return; }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_ChiTietPhieuNhap.fxml"));
            Stage stage = new Stage(); stage.setScene(new Scene(loader.load()));
            loader.<Dialog_ChiTietPhieuNhapController>getController().setDuLieu(phieu.getMaDonNhap());
            stage.setTitle("Chi tiết: " + phieu.getMaDonNhap());
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    void handleInPhieu(ActionEvent event) {
        DonNhapHang phieu = tablePhieuNhap.getSelectionModel().getSelectedItem();
        if (phieu == null) { new Alert(Alert.AlertType.WARNING, "Chọn phiếu cần in!").show(); return; }
        service.Print_HoaDonDatHang.inHoaDon(phieu, daoDonNhap.getChiTietByMaDon(phieu.getMaDonNhap()));
    }
}