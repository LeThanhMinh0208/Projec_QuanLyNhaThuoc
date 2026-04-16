package gui.main;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import dao.DAO_HoaDon;
import entity.HoaDonView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GUI_DanhSachHoaDonController {

    @FXML private DatePicker dpTuNgay;
    @FXML private DatePicker dpDenNgay;
    @FXML private ComboBox<String> cbHinhThuc;
    @FXML private ComboBox<String> cbLoaiBan;
    @FXML private TextField txtTimKiem;

    @FXML private TableView<HoaDonView> tableHoaDon;
    @FXML private TableColumn<HoaDonView, String> colMaHD;
    @FXML private TableColumn<HoaDonView, String> colNgayLap;
    @FXML private TableColumn<HoaDonView, String> colKhachHang;
    @FXML private TableColumn<HoaDonView, String> colNhanVien;
    @FXML private TableColumn<HoaDonView, String> colLoaiBan;
    @FXML private TableColumn<HoaDonView, String> colTamTinh;
    @FXML private TableColumn<HoaDonView, String> colVAT;
    @FXML private TableColumn<HoaDonView, String> colTongSauVAT;
    @FXML private TableColumn<HoaDonView, String> colHinhThuc;
    @FXML private TableColumn<HoaDonView, Void>   colHanhDong;

    @FXML private Label lblTongHoaDon;
    @FXML private Label lblTongDoanhThu;

    private final DAO_HoaDon dao = new DAO_HoaDon();
    private ObservableList<HoaDonView> masterData = FXCollections.observableArrayList();
    private javafx.collections.transformation.FilteredList<HoaDonView> filteredData;
    private ObservableList<HoaDonView> data = FXCollections.observableArrayList();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        setupComboBox();
        setupTable();
        // Mặc định: đầu tháng → hôm nay
        dpTuNgay.setValue(LocalDate.now().withDayOfMonth(1));
        dpDenNgay.setValue(LocalDate.now());
        loadData();
        // Realtime search listener (filter local, không query DB)
        txtTimKiem.textProperty().addListener((obs, oldVal, newVal) -> filterByKeyword(newVal.trim()));
        // ENTER trên row hóa đơn → mở chi tiết
        tableHoaDon.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                HoaDonView selected = tableHoaDon.getSelectionModel().getSelectedItem();
                if (selected != null) moDialogChiTiet(selected);
                event.consume();
            }
        });
    }

    private void setupComboBox() {
        cbHinhThuc.setItems(FXCollections.observableArrayList(
                "Tất cả", "Tiền mặt", "Chuyển khoản", "Thẻ tín dụng"));
        cbHinhThuc.setValue("Tất cả");

        cbLoaiBan.setItems(FXCollections.observableArrayList(
                "Tất cả", "Bán lẻ", "Bán theo đơn"));
        cbLoaiBan.setValue("Tất cả");
    }

    private void setupTable() {
        colMaHD      .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaHoaDon()));
        colNgayLap   .setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getNgayLap() != null
                ? d.getValue().getNgayLap().toLocalDateTime().format(FMT) : ""));
        colKhachHang .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenKhachHang()));
        colNhanVien  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenNhanVien()));
        colTamTinh   .setCellValueFactory(d -> new SimpleStringProperty(
                String.format("%,.0f ₫", d.getValue().getTamTinh())));
        colVAT       .setCellValueFactory(d -> new SimpleStringProperty(
                String.format("%.0f%%", d.getValue().getThueVAT())));
        colTongSauVAT.setCellValueFactory(d -> new SimpleStringProperty(
                String.format("%,.0f ₫", d.getValue().getTongSauVAT())));
        colHinhThuc  .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHinhThucLabel()));

        // Cột loại bán — hiển thị màu khác nhau
        colLoaiBan.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getLoaiBan() != null ? d.getValue().getLoaiBan() : "BAN_LE"));
        colLoaiBan.setCellFactory(col -> new TableCell<HoaDonView, String>() {
            @Override
            protected void updateItem(String val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) {
                    setText(null);
                    setStyle("");
                } else if ("BAN_THEO_DON".equals(val)) {
                    setText("Theo đơn");
                    setStyle("-fx-text-fill: #1565C0; -fx-font-weight: bold;");
                } else {
                    setText("Bán lẻ");
                    setStyle("-fx-text-fill: #2E7D32;");
                }
            }
        });

        colHanhDong.setCellFactory(col -> new TableCell<>() {
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

        tableHoaDon.setItems(filteredData != null ? filteredData : data);
    }

    private void loadData() {
        LocalDate tu   = dpTuNgay.getValue();
        LocalDate den  = dpDenNgay.getValue();
        String hinhThuc = null;
        if (cbHinhThuc.getValue() != null) {
            switch (cbHinhThuc.getValue()) {
                case "Tiền mặt":    hinhThuc = "TIEN_MAT";    break;
                case "Chuyển khoản": hinhThuc = "CHUYEN_KHOAN"; break;
                case "Thẻ tín dụng": hinhThuc = "THE";          break;
                default: hinhThuc = null;
            }
        }

        // Lọc loại bán
        String loaiBan = null;
        if (cbLoaiBan.getValue() != null) {
            switch (cbLoaiBan.getValue()) {
                case "Bán lẻ":       loaiBan = "BAN_LE";       break;
                case "Bán theo đơn": loaiBan = "BAN_THEO_DON"; break;
                default: loaiBan = null;
            }
        }

        // Load tất cả vào masterData (không gửi keyword xuống DB)
        List<HoaDonView> list = dao.getDanhSach(tu, den, hinhThuc, null, loaiBan);
        masterData.setAll(list);

        // Wrap bằng FilteredList để filter keyword local
        filteredData = new javafx.collections.transformation.FilteredList<>(masterData, hd -> true);
        tableHoaDon.setItems(filteredData);

        // Áp lại keyword nếu đang có
        String kw = txtTimKiem.getText().trim();
        filterByKeyword(kw);

        updateFooter();
    }

    /** Filter keyword trên data đã load (không query DB) */
    private void filterByKeyword(String keyword) {
        if (filteredData == null) return;
        if (keyword == null || keyword.isEmpty()) {
            filteredData.setPredicate(hd -> true);
        } else {
            String lower = keyword.toLowerCase();
            filteredData.setPredicate(hd -> {
                if (hd.getMaHoaDon() != null && hd.getMaHoaDon().toLowerCase().contains(lower)) return true;
                if (hd.getTenKhachHang() != null && hd.getTenKhachHang().toLowerCase().contains(lower)) return true;
                if (hd.getSdt() != null && hd.getSdt().toLowerCase().contains(lower)) return true;
                return false;
            });
        }
        updateFooter();
    }

    private void updateFooter() {
        int count = filteredData != null ? filteredData.size() : masterData.size();
        lblTongHoaDon.setText("Tổng: " + count + " hóa đơn");
        double tongDT = (filteredData != null ? filteredData : masterData)
                .stream().mapToDouble(HoaDonView::getTongSauVAT).sum();
        lblTongDoanhThu.setText(String.format("%,.0f ₫", tongDT));
    }

    @FXML
    void handleTimKiem(ActionEvent event) {
        // Lại load data từ DB (xài filter ngày/hình thức/loại bán) rồi filter local
        loadData();
    }

    @FXML
    void onFilterLoaiBan(ActionEvent event) {
        // Khi thay đổi ComboBox loại bán → reload data từ DB
        loadData();
    }

    @FXML
    void handleXoaBoLoc(ActionEvent event) {
        dpTuNgay.setValue(LocalDate.now().withDayOfMonth(1));
        dpDenNgay.setValue(LocalDate.now());
        cbHinhThuc.setValue("Tất cả");
        cbLoaiBan.setValue("Tất cả");
        txtTimKiem.clear();
        loadData();
    }

    private void moDialogChiTiet(HoaDonView hd) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_ChiTietHoaDon.fxml"));
            Parent root = loader.load();
            gui.dialogs.Dialog_ChiTietHoaDonController ctrl = loader.getController();
            ctrl.setHoaDon(hd);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Chi Tiết Hóa Đơn — " + hd.getMaHoaDon());
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
            Alert a = new Alert(Alert.AlertType.ERROR, "Không thể mở chi tiết: " + e.getMessage(), ButtonType.OK);
            a.showAndWait();
        }
    }
}
