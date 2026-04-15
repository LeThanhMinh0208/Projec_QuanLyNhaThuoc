package gui.main;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import dao.DAO_HoaDon;
import dao.DAO_PhieuDoiTra;
import entity.HoaDonView;
import entity.PhieuDoiTraView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
<<<<<<< HEAD
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
=======
<<<<<<< HEAD
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
>>>>>>> main
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
=======
import javafx.scene.control.Button;
>>>>>>> 372975594d8f1063277fa68b18264d82aa24f969
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleButton;
import javafx.scene.control.ToggleGroup;
<<<<<<< HEAD
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
<<<<<<< HEAD
=======
=======
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
>>>>>>> 372975594d8f1063277fa68b18264d82aa24f969
>>>>>>> main
import utils.DoiTraSession;
import utils.SceneUtils;

public class GUI_XuLyDoiTraController {

    @FXML private ToggleGroup tabGroup;
    @FXML private ToggleButton tabXuLyDoiTra;
    @FXML private ToggleButton tabDanhSachPhieuDoiTra;
    @FXML private VBox viewXuLyDoiTra;
    @FXML private VBox viewDanhSachPhieuDoiTra;

    @FXML private DatePicker dpTuNgay;
    @FXML private DatePicker dpDenNgay;
    @FXML private ComboBox<String> cbHinhThuc;
    @FXML private TextField txtTimKiem;

    @FXML private TableView<HoaDonView> tableHoaDon;
    @FXML private TableColumn<HoaDonView, String> colMaHD;
    @FXML private TableColumn<HoaDonView, String> colNgayLap;
    @FXML private TableColumn<HoaDonView, String> colKhachHang;
    @FXML private TableColumn<HoaDonView, String> colNhanVien;
    @FXML private TableColumn<HoaDonView, String> colTamTinh;
    @FXML private TableColumn<HoaDonView, String> colVAT;
    @FXML private TableColumn<HoaDonView, String> colTongSauVAT;
    @FXML private TableColumn<HoaDonView, String> colHinhThuc;
    @FXML private TableColumn<HoaDonView, Void> colHanhDong;

    @FXML private Label lblTongHoaDon;
    @FXML private Label lblTongDoanhThu;

    @FXML private TextField txtTimKiemPhieuDoiTra;
    @FXML private TableView<PhieuDoiTraView> tablePhieuDoiTra;
    @FXML private TableColumn<PhieuDoiTraView, Void> colChiTietPDT;
    @FXML private TableColumn<PhieuDoiTraView, String> colMaPhieuDoiTra;
    @FXML private TableColumn<PhieuDoiTraView, String> colNgayDoiTra;
    @FXML private TableColumn<PhieuDoiTraView, String> colMaHoaDonPDT;
    @FXML private TableColumn<PhieuDoiTraView, String> colKhachHangPDT;
    @FXML private TableColumn<PhieuDoiTraView, String> colNhanVienPDT;
    @FXML private TableColumn<PhieuDoiTraView, String> colHinhThucXuLyPDT;
    @FXML private TableColumn<PhieuDoiTraView, String> colPhiPhatPDT;
    @FXML private TableColumn<PhieuDoiTraView, String> colLyDoPDT;

    private final DAO_HoaDon daoHoaDon = new DAO_HoaDon();
    private final DAO_PhieuDoiTra daoPhieuDoiTra = new DAO_PhieuDoiTra();
    private final ObservableList<HoaDonView> masterData = FXCollections.observableArrayList();
    private final ObservableList<PhieuDoiTraView> dsPhieuDoiTra = FXCollections.observableArrayList();
    private javafx.collections.transformation.FilteredList<HoaDonView> filteredData;
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @FXML
    public void initialize() {
        setupTabs();
        setupComboBox();
        setupTableHoaDon();
        setupTablePhieuDoiTra();

        dpTuNgay.setValue(null);
        dpDenNgay.setValue(null);
        loadHoaDon();

        txtTimKiem.textProperty().addListener((obs, oldVal, newVal) -> filterByKeyword(newVal.trim()));
        txtTimKiemPhieuDoiTra.textProperty().addListener((obs, oldVal, newVal) -> loadDanhSachPhieuDoiTra(newVal));
<<<<<<< HEAD

        tableHoaDon.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.ENTER) {
                HoaDonView selected = tableHoaDon.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    moDialogChiTiet(selected);
                }
                event.consume();
            }
        });
=======
>>>>>>> 372975594d8f1063277fa68b18264d82aa24f969
    }

    private void setupTabs() {
        tabGroup.selectedToggleProperty().addListener((obs, oldToggle, newToggle) -> {
            if (newToggle == null) {
                if (oldToggle != null) {
                    oldToggle.setSelected(true);
                }
                return;
            }

            boolean isXuLy = newToggle == tabXuLyDoiTra;
            viewXuLyDoiTra.setVisible(isXuLy);
            viewXuLyDoiTra.setManaged(isXuLy);
            viewDanhSachPhieuDoiTra.setVisible(!isXuLy);
            viewDanhSachPhieuDoiTra.setManaged(!isXuLy);

            if (!isXuLy) {
                loadDanhSachPhieuDoiTra(txtTimKiemPhieuDoiTra.getText());
            }
        });
    }

    private void setupComboBox() {
<<<<<<< HEAD
        cbHinhThuc.setItems(FXCollections.observableArrayList(
                "Tất cả", "Tiền mặt", "Chuyển khoản", "Thẻ tín dụng"));
=======
        cbHinhThuc.setItems(FXCollections.observableArrayList("Tất cả", "Tiền mặt", "Chuyển khoản", "Thẻ tín dụng"));
>>>>>>> 372975594d8f1063277fa68b18264d82aa24f969
        cbHinhThuc.setValue("Tất cả");
    }

    private void setupTableHoaDon() {
        colMaHD.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaHoaDon()));
        colNgayLap.setCellValueFactory(d -> new SimpleStringProperty(
<<<<<<< HEAD
                d.getValue().getNgayLap() != null
                        ? d.getValue().getNgayLap().toLocalDateTime().format(FMT) : ""));
=======
                d.getValue().getNgayLap() != null ? d.getValue().getNgayLap().toLocalDateTime().format(FMT) : ""));
>>>>>>> 372975594d8f1063277fa68b18264d82aa24f969
        colKhachHang.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenKhachHang()));
        colNhanVien.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenNhanVien()));
        colTamTinh.setCellValueFactory(d -> new SimpleStringProperty(String.format("%,.0f VND", d.getValue().getTamTinh())));
        colVAT.setCellValueFactory(d -> new SimpleStringProperty(String.format("%.0f%%", d.getValue().getThueVAT())));
        colTongSauVAT.setCellValueFactory(d -> new SimpleStringProperty(String.format("%,.0f VND", d.getValue().getTongSauVAT())));
        colHinhThuc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHinhThucLabel()));

        colHanhDong.setCellFactory(col -> new TableCell<>() {
            private final Button btnXuLy = new Button("Xử lý đổi trả");
            private final HBox actions = new HBox(8, btnXuLy);
            {
                btnXuLy.setStyle("-fx-background-color:#16a34a;-fx-text-fill:white;-fx-font-size:12px;-fx-padding:4 10;-fx-cursor:hand;");
                btnXuLy.setOnAction(e -> xuLyDoiTra(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : actions);
            }
        });

        tableHoaDon.setItems(masterData);
    }

    private void setupTablePhieuDoiTra() {
        colChiTietPDT.setCellFactory(col -> new TableCell<>() {
            private final Button btnChiTiet = new Button("Chi tiết");
            {
                btnChiTiet.setStyle("-fx-background-color:#2563eb;-fx-text-fill:white;-fx-font-size:12px;-fx-padding:4 10;-fx-cursor:hand;");
                btnChiTiet.setOnAction(e -> moChiTietPhieuDoiTra(getTableView().getItems().get(getIndex())));
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnChiTiet);
            }
        });
        colMaPhieuDoiTra.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaPhieuDoiTra()));
        colNgayDoiTra.setCellValueFactory(d -> new SimpleStringProperty(
<<<<<<< HEAD
                d.getValue().getNgayDoiTra() != null
                        ? d.getValue().getNgayDoiTra().toLocalDateTime().format(FMT) : ""));
=======
                d.getValue().getNgayDoiTra() != null ? d.getValue().getNgayDoiTra().toLocalDateTime().format(FMT) : ""));
>>>>>>> 372975594d8f1063277fa68b18264d82aa24f969
        colMaHoaDonPDT.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaHoaDon()));
        colKhachHangPDT.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenKhachHang()));
        colNhanVienPDT.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenNhanVien()));
        colHinhThucXuLyPDT.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getHinhThucXuLyLabel()));
        colPhiPhatPDT.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().isDoiSanPham()
                        ? d.getValue().getMoTaChenhLechDoiSanPham()
                        : String.format("%,.0f VND", d.getValue().getPhiPhat())));
        colLyDoPDT.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getLyDoHienThi()));
        tablePhieuDoiTra.setItems(dsPhieuDoiTra);
    }

    private void loadHoaDon() {
        LocalDate tu = dpTuNgay.getValue();
        LocalDate den = dpDenNgay.getValue();
        String hinhThuc = null;
        if (cbHinhThuc.getValue() != null) {
            switch (cbHinhThuc.getValue()) {
                case "Tiền mặt":
                    hinhThuc = "TIEN_MAT";
                    break;
                case "Chuyển khoản":
                    hinhThuc = "CHUYEN_KHOAN";
                    break;
                case "Thẻ tín dụng":
                    hinhThuc = "THE";
                    break;
                default:
                    hinhThuc = null;
                    break;
            }
        }

        List<HoaDonView> list = daoHoaDon.getDanhSach(tu, den, hinhThuc, null);
        masterData.setAll(list);
        filteredData = new javafx.collections.transformation.FilteredList<>(masterData, hd -> true);
        tableHoaDon.setItems(filteredData);
        filterByKeyword(txtTimKiem.getText() != null ? txtTimKiem.getText().trim() : "");
        updateFooter();
    }

    private void loadDanhSachPhieuDoiTra(String tuKhoa) {
        dsPhieuDoiTra.setAll(daoPhieuDoiTra.getAllPhieuDoiTra(tuKhoa));
    }

    private void filterByKeyword(String keyword) {
        if (filteredData == null) {
            return;
        }
<<<<<<< HEAD

=======
>>>>>>> 372975594d8f1063277fa68b18264d82aa24f969
        if (keyword == null || keyword.isEmpty()) {
            filteredData.setPredicate(hd -> true);
        } else {
            String lower = keyword.toLowerCase();
            filteredData.setPredicate(hd -> {
                if (hd.getMaHoaDon() != null && hd.getMaHoaDon().toLowerCase().contains(lower)) {
                    return true;
                }
                if (hd.getTenKhachHang() != null && hd.getTenKhachHang().toLowerCase().contains(lower)) {
                    return true;
                }
                return hd.getSdt() != null && hd.getSdt().toLowerCase().contains(lower);
            });
        }
        updateFooter();
    }

    private void updateFooter() {
        int count = filteredData != null ? filteredData.size() : masterData.size();
<<<<<<< HEAD
        lblTongHoaDon.setText("Tổng: " + count + " hóa đơn");
=======
<<<<<<< HEAD
        lblTongHoaDon.setText("Tong: " + count + " hoa don");
=======
        lblTongHoaDon.setText("Tổng: " + count + " hóa đơn");
>>>>>>> 372975594d8f1063277fa68b18264d82aa24f969
>>>>>>> main
        double tongDT = (filteredData != null ? filteredData : masterData).stream().mapToDouble(HoaDonView::getTongSauVAT).sum();
        lblTongDoanhThu.setText(String.format("%,.0f VND", tongDT));
    }

    @FXML
    void handleTimKiem(ActionEvent event) {
        loadHoaDon();
    }

    @FXML
    void handleXoaBoLoc(ActionEvent event) {
<<<<<<< HEAD
        dpTuNgay.setValue(null);
        dpDenNgay.setValue(null);
=======
        dpTuNgay.setValue(LocalDate.now().withDayOfMonth(1));
        dpDenNgay.setValue(LocalDate.now());
>>>>>>> main
        cbHinhThuc.setValue("Tất cả");
        txtTimKiem.clear();
        loadHoaDon();
    }

    @FXML
    void handleLamMoiDanhSachPhieu(ActionEvent event) {
        txtTimKiemPhieuDoiTra.clear();
        loadDanhSachPhieuDoiTra("");
    }

    private void xuLyDoiTra(HoaDonView hd) {
        DoiTraSession.setHoaDonDangXuLy(hd);
        SceneUtils.switchPage("/gui/main/GUI_ChiTietDoiTra.fxml");
    }
<<<<<<< HEAD

    private void moChiTietPhieuDoiTra(PhieuDoiTraView pdt) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_ChiTietPhieuDoiTra.fxml"));
            Parent root = loader.load();
            gui.dialogs.Dialog_ChiTietPhieuDoiTraController controller = loader.getController();
            controller.setPhieuDoiTra(pdt);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Chi tiết phiếu đổi trả - " + pdt.getMaPhieuDoiTra());
=======
<<<<<<< HEAD

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void moDialogChiTiet(HoaDonView hd) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_ChiTietHoaDon.fxml"));
            Parent root = loader.load();
            gui.dialogs.Dialog_ChiTietHoaDonController ctrl = loader.getController();
            ctrl.setHoaDon(hd);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Chi tiet hoa don - " + hd.getMaHoaDon());
>>>>>>> main
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
<<<<<<< HEAD
        }
    }
=======
            showAlert(Alert.AlertType.ERROR, "Không thể mở chi tiết: " + e.getMessage());
        }
    }
=======
>>>>>>> 372975594d8f1063277fa68b18264d82aa24f969
>>>>>>> main
}
