package gui.dialogs;

import dao.DAO_DonNhapHang;
import dao.DAO_DonViQuyDoi;
import dao.DAO_NhaCungCap;
import dao.DAO_Thuoc;
import entity.ChiTietDonNhapHang;
import entity.DonNhapHang;
import entity.DonViQuyDoi;
import entity.NhaCungCap;
import entity.NhanVien;
import entity.Thuoc;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;

import java.sql.Date;
import java.time.LocalDate;
import java.util.ArrayList;

public class Dialog_TaoDonNhapHangController {

    @FXML private Label lblMaDon, lblTongTien;
    @FXML private TextField txtTimThuoc, txtGhiChu;
    @FXML private ComboBox<NhaCungCap> cmbNhaCungCap;
    @FXML private ComboBox<DonViQuyDoi> cmbDonVi;
    @FXML private DatePicker dpNgayHenGiao;
    @FXML private ListView<Thuoc> listViewThuoc;
    @FXML private Spinner<Integer> spnSoLuong;

    @FXML private TableView<ChiTietDonNhapHang> tableChiTietDon;
    @FXML private TableColumn<ChiTietDonNhapHang, String> colTenThuoc, colDonVi;
    @FXML private TableColumn<ChiTietDonNhapHang, Integer> colSoLuong;
    @FXML private TableColumn<ChiTietDonNhapHang, Double> colGiaDuKien, colThanhTien;

    private ObservableList<ChiTietDonNhapHang> listDatHang = FXCollections.observableArrayList();
    private ObservableList<Thuoc> masterThuocList = FXCollections.observableArrayList();
    
    private DAO_Thuoc daoThuoc = new DAO_Thuoc();
    private DAO_NhaCungCap daoNCC = new DAO_NhaCungCap();
    private DAO_DonViQuyDoi daoDonVi = new DAO_DonViQuyDoi();
    private DAO_DonNhapHang daoDonNhap = new DAO_DonNhapHang();

    @FXML
    public void initialize() {
        setupTable();
        setupListViewThuoc();
        setupInputs();
        loadDuLieu();
        lblMaDon.setText(daoDonNhap.getMaDonMoi()); // Sinh mã DB
    }

    private void setupTable() {
        colTenThuoc.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getThuoc().getTenThuoc()));
        colDonVi.setCellValueFactory(cell -> {
            DonViQuyDoi dv = cell.getValue().getDonViQuyDoi();
            return new SimpleStringProperty(dv != null ? dv.getTenDonVi() : "Chưa chọn");
        });
        colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuongDat"));
        colGiaDuKien.setCellValueFactory(new PropertyValueFactory<>("donGiaDuKien"));
        colGiaDuKien.setCellFactory(column -> formatCurrencyCell());
        colThanhTien.setCellValueFactory(new PropertyValueFactory<>("thanhTien"));
        colThanhTien.setCellFactory(column -> formatCurrencyCell());
        
        tableChiTietDon.setItems(listDatHang);
        tableChiTietDon.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY); // Fix cột xám
    }

    private TableCell<ChiTietDonNhapHang, Double> formatCurrencyCell() {
        return new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : String.format("%,.0f đ", item));
            }
        };
    }

    private void setupInputs() {
        spnSoLuong.setValueFactory(new SpinnerValueFactory.IntegerSpinnerValueFactory(1, 10000, 1));
        dpNgayHenGiao.setValue(LocalDate.now().plusDays(2)); 
    }

    private void loadDuLieu() {
        masterThuocList.setAll(daoThuoc.getAllThuoc());
        
        FilteredList<Thuoc> filteredData = new FilteredList<>(masterThuocList, p -> true);
        txtTimThuoc.textProperty().addListener((obs, oldV, newV) -> {
            filteredData.setPredicate(t -> {
                if (newV == null || newV.isEmpty()) return true;
                return t.getTenThuoc().toLowerCase().contains(newV.toLowerCase());
            });
        });
        listViewThuoc.setItems(filteredData);

        // Load Quy Cách khi chọn Thuốc
        listViewThuoc.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                ArrayList<DonViQuyDoi> listDV = daoDonVi.getDonViByMaThuoc(newSel.getMaThuoc());
                cmbDonVi.setItems(FXCollections.observableArrayList(listDV));
                if (!listDV.isEmpty()) cmbDonVi.getSelectionModel().selectFirst();
            }
        });

        // Load Danh sách NCC
        cmbNhaCungCap.setItems(FXCollections.observableArrayList(daoNCC.getAllNhaCungCap()));
        cmbNhaCungCap.setConverter(new javafx.util.StringConverter<NhaCungCap>() {
            @Override public String toString(NhaCungCap ncc) { return ncc == null ? "" : ncc.getTenNhaCungCap(); }
            @Override public NhaCungCap fromString(String string) { return null; }
        });
    }

    @FXML
    void handleThemVaoDon() {
        Thuoc selectedThuoc = listViewThuoc.getSelectionModel().getSelectedItem();
        DonViQuyDoi selectedDonVi = cmbDonVi.getValue();
        int soLuong = spnSoLuong.getValue();

        if (selectedThuoc == null || selectedDonVi == null) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng chọn thuốc và quy cách đóng gói!").show();
            return;
        }

        ChiTietDonNhapHang chiTiet = new ChiTietDonNhapHang();
        chiTiet.setThuoc(selectedThuoc);
        chiTiet.setDonViQuyDoi(selectedDonVi); 
        chiTiet.setSoLuongDat(soLuong);
        chiTiet.setDonGiaDuKien(0.0); // Giá dự kiến — người dùng chỉnh sau khi nhận hàng
        
        listDatHang.add(chiTiet);
        tinhTongTien();
    }

    private void tinhTongTien() {
        double tong = listDatHang.stream().mapToDouble(ChiTietDonNhapHang::getThanhTien).sum();
        lblTongTien.setText(String.format("%,.0f VNĐ", tong));
    }

    @FXML
    void handleTaoDonHang(ActionEvent event) {
        if (cmbNhaCungCap.getValue() == null) {
            new Alert(Alert.AlertType.ERROR, "Chưa chọn Nhà Cung Cấp!").show();
            return;
        }
        if (listDatHang.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Đơn hàng đang trống, vui lòng thêm thuốc!").show();
            return;
        }

        DonNhapHang donMoi = new DonNhapHang();
        donMoi.setMaDonNhap(lblMaDon.getText().trim());
        donMoi.setNhaCungCap(cmbNhaCungCap.getValue());
        
        NhanVien nv = new NhanVien(); 
        nv.setMaNhanVien("NV001"); // Tạm gắn cứng, sau sếp đổi thành nhân viên login nhé
        donMoi.setNhanVien(nv);
        
        donMoi.setNgayLap(Date.valueOf(LocalDate.now()));
        if (dpNgayHenGiao.getValue() != null) {
            donMoi.setNgayHenGiao(Date.valueOf(dpNgayHenGiao.getValue()));
        }
        donMoi.setTongTienDuTinh(listDatHang.stream().mapToDouble(ChiTietDonNhapHang::getThanhTien).sum());
        donMoi.setTrangThai("CHO_DUYET");
        donMoi.setGhiChu(txtGhiChu.getText());

        // Gọi DAO lưu vào CSDL
        boolean isSuccess = daoDonNhap.themDonNhapHangVaChiTiet(donMoi, new ArrayList<>(listDatHang));
        
        if (isSuccess) {
            Alert alert = new Alert(Alert.AlertType.INFORMATION, "✅ Tạo Đơn Đặt Hàng Thành Công!");
            alert.showAndWait();
            handleDongDialog(event); // Lưu xong thì tắt Dialog
        } else {
            new Alert(Alert.AlertType.ERROR, "❌ Lưu thất bại!").show();
        }
    }

    // ĐÓNG DIALOG (Dùng cho cả nút Hủy và khi Tạo đơn thành công)
    @FXML
    void handleDongDialog(ActionEvent event) {
        Stage stage = (Stage) ((Node) event.getSource()).getScene().getWindow();
        stage.close();
    }

    private void setupListViewThuoc() {
        listViewThuoc.setCellFactory(param -> new ListCell<Thuoc>() {
            private final ImageView imageView = new ImageView();
            private final Label label = new Label();
            private final HBox box = new HBox(imageView, label);
            {
                box.setSpacing(15); box.setAlignment(Pos.CENTER_LEFT);
                imageView.setFitHeight(45); imageView.setFitWidth(45); imageView.setPreserveRatio(true);
                label.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
            }
            @Override protected void updateItem(Thuoc item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setGraphic(null); } 
                else {
                    label.setText(item.getTenThuoc());
                    try {
                        imageView.setImage(new Image(getClass().getResourceAsStream("/resources/images/images_thuoc/" + item.getHinhAnh())));
                    } catch (Exception e) {}
                    setGraphic(box);
                }
            }
        });
    }
}