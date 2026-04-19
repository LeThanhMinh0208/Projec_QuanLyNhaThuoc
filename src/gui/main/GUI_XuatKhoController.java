package gui.main;

import dao.DAO_PhieuXuat;
import entity.PhieuXuat;
import gui.dialogs.Dialog_ChiTietPhieuXuatController;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.*;
import javafx.geometry.Pos;
import javafx.scene.*;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.*;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ResourceBundle;

public class GUI_XuatKhoController implements Initializable {
    @FXML private TableView<PhieuXuat> tableDanhMucPhieuXuat;
    @FXML private TableColumn<PhieuXuat, Integer> colSTT, colLoaiPhieu;
    @FXML private TableColumn<PhieuXuat, String> colMaPhieu, colNguoiLap, colKhoNhan;
    @FXML private TableColumn<PhieuXuat, LocalDateTime> colThoiGian;
    @FXML private TableColumn<PhieuXuat, Void> colThaoTac;
    
    // Khai báo thêm các component lọc
    @FXML private TextField txtTimPhieu;
    @FXML private ComboBox<String> cbLocLoaiPhieu;
    @FXML private DatePicker dpTuNgay;
    @FXML private DatePicker dpDenNgay;

    private DAO_PhieuXuat daoPX = new DAO_PhieuXuat();
    private ObservableList<PhieuXuat> masterData = FXCollections.observableArrayList();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        loadPhieuXuat();
        setupSearch();
    }

    private void setupTable() {
        colSTT.setCellValueFactory(p -> new ReadOnlyObjectWrapper<>(tableDanhMucPhieuXuat.getItems().indexOf(p.getValue()) + 1));
        colMaPhieu.setCellValueFactory(new PropertyValueFactory<>("maPhieuXuat"));
        colNguoiLap.setCellValueFactory(new PropertyValueFactory<>("maNhanVien"));
        
     // =======================================================
        // 1. CỘT NƠI NHẬN (Đồng bộ màu theo Nút chức năng)
        // =======================================================
        colKhoNhan.setCellValueFactory(new PropertyValueFactory<>("khoNhan"));
        colKhoNhan.setCellFactory(c -> new TableCell<>() {
            private dao.DAO_NhaCungCap daoNCC = new dao.DAO_NhaCungCap(); 

            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null); setStyle("");
                } else {
                    PhieuXuat px = getTableRow().getItem();
                    
                    if (px.getLoaiPhieu() == 1) {
                        // 🟢 CHUYỂN KHO -> MÀU XANH LÁ TỪ CSS
                        String tenKho = "KHO_BAN_HANG".equals(item) ? "Kho Bán Hàng" : ("KHO_DU_TRU".equals(item) ? "Kho Dự Trữ" : item);
                        setText(tenKho);
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;"); 
                        
                    } else if (px.getLoaiPhieu() == 2) {
                        // 🔵 TRẢ NHÀ CUNG CẤP -> MÀU XANH DƯƠNG TỪ CSS
                        String tenNCC = "Không rõ NCC";
                        if (px.getMaNhaCungCap() != null) {
                            entity.NhaCungCap ncc = daoNCC.getNhaCungCapByMa(px.getMaNhaCungCap());
                            if (ncc != null) {
                                tenNCC = ncc.getTenNhaCungCap();
                            }
                        }
                        setText(tenNCC); 
                        setStyle("-fx-text-fill: #0ea5e9; -fx-font-weight: bold;"); 
                        
                    } else if (px.getLoaiPhieu() == 3) {
                        // 🟠 XUẤT HỦY -> MÀU VÀNG CAM TỪ CSS
                        setText("Hủy rác y tế"); 
                        setStyle("-fx-text-fill: #fb8500; -fx-font-weight: bold;");
                        
                    } else {
                        setText(item == null ? "---" : item); 
                        setStyle("");
                    }
                }
            }
        });

        // =======================================================
        // 2. CỘT LOẠI PHIẾU (Đồng bộ màu với cột Nơi Nhận)
        // =======================================================
        colLoaiPhieu.setCellValueFactory(new PropertyValueFactory<>("loaiPhieu"));
        colLoaiPhieu.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { 
                    setText(null); setStyle(""); 
                } else {
                    if (item == 1) { 
                        setText("🚚 Chuyển Kho"); 
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                    } else if (item == 2) { 
                        setText("🔙 Trả NCC"); 
                        setStyle("-fx-text-fill: #0ea5e9; -fx-font-weight: bold;");
                    } else if (item == 3) { 
                        setText("🗑 Xuất Hủy"); 
                        setStyle("-fx-text-fill: #fb8500; -fx-font-weight: bold;");
                    } else {
                        setText("---"); setStyle("");
                    }
                }
            }
        });
        // =======================================================
        // 3. CỘT THỜI GIAN VÀ NÚT THAO TÁC (Giữ nguyên)
        // =======================================================
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
        colThoiGian.setCellValueFactory(new PropertyValueFactory<>("ngayXuat"));
        colThoiGian.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(LocalDateTime item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : dtf.format(item));
            }
        });

        colThaoTac.setCellFactory(c -> new TableCell<>() {
            private final Button btn = new Button("👁 Xem");
            { 
                btn.setStyle("-fx-background-color: #0ea5e9; -fx-text-fill: white; -fx-cursor: hand;");
                btn.setOnAction(e -> handleXemChiTiet(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void i, boolean e) {
                super.updateItem(i, e);
                if (e) setGraphic(null); else { setGraphic(btn); setAlignment(Pos.CENTER); }
            }
        });
    }

    public void loadPhieuXuat() { masterData.setAll(daoPX.getAllPhieuXuat()); }

    // ========================================================
    // BỘ LỌC 3 TRONG 1: THEO TÊN + THEO LOẠI + THEO NGÀY
    // ========================================================
    private void setupSearch() {
        cbLocLoaiPhieu.getItems().addAll("Tất cả", "Chuyển Kho", "Trả NCC", "Xuất Hủy");
        cbLocLoaiPhieu.getSelectionModel().selectFirst();

        FilteredList<PhieuXuat> filteredData = new FilteredList<>(masterData, p -> true);

        // Lắng nghe sự thay đổi của cả 4 ô nhập liệu
        javafx.beans.value.ChangeListener<Object> filterListener = (obs, oldVal, newVal) -> {
            filteredData.setPredicate(px -> {
                // 1. Kiểm tra Text (Từ khóa tìm kiếm)
                String searchText = txtTimPhieu.getText() == null ? "" : txtTimPhieu.getText().toLowerCase();
                boolean matchText = searchText.isEmpty() || 
                                    px.getMaPhieuXuat().toLowerCase().contains(searchText) || 
                                    px.getMaNhanVien().toLowerCase().contains(searchText);

                // 2. Kiểm tra Loại Phiếu
                String loaiPhieu = cbLocLoaiPhieu.getValue();
                boolean matchLoai = true;
                if (loaiPhieu != null && !loaiPhieu.equals("Tất cả")) {
                    if (loaiPhieu.equals("Chuyển Kho") && px.getLoaiPhieu() != 1) matchLoai = false;
                    if (loaiPhieu.equals("Trả NCC") && px.getLoaiPhieu() != 2) matchLoai = false;
                    if (loaiPhieu.equals("Xuất Hủy") && px.getLoaiPhieu() != 3) matchLoai = false;
                }

                // 3. Kiểm tra Ngày tháng
                boolean matchNgay = true;
                LocalDate tuNgay = dpTuNgay.getValue();
                LocalDate denNgay = dpDenNgay.getValue();
                LocalDate ngayLap = px.getNgayXuat() != null ? px.getNgayXuat().toLocalDate() : null;

                if (ngayLap != null) {
                    if (tuNgay != null && ngayLap.isBefore(tuNgay)) matchNgay = false;
                    if (denNgay != null && ngayLap.isAfter(denNgay)) matchNgay = false;
                } else if (tuNgay != null || denNgay != null) {
                    matchNgay = false; // Phiếu không có ngày thì rớt lọc
                }

                return matchText && matchLoai && matchNgay;
            });
        };

        // Gắn listener vào các nút
        txtTimPhieu.textProperty().addListener(filterListener);
        cbLocLoaiPhieu.valueProperty().addListener(filterListener);
        dpTuNgay.valueProperty().addListener(filterListener);
        dpDenNgay.valueProperty().addListener(filterListener);

        tableDanhMucPhieuXuat.setItems(filteredData);
    }

    private void handleXemChiTiet(PhieuXuat px) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_ChiTietPhieuXuat.fxml"));
            Parent root = loader.load();
            
            Dialog_ChiTietPhieuXuatController controller = loader.getController();
            controller.setPhieuXuat(px); 

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Chi Tiết Phiếu " + px.getMaPhieuXuat());
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (IOException e) { e.printStackTrace(); }
    }

    @FXML private void handleMoDialogChuyenKho() { moDialog("/gui/dialogs/Dialog_XuatChuyenKho.fxml", "Chuyển Kho Nội Bộ"); }
    @FXML private void handleMoDialogTraNCC() { moDialog("/gui/dialogs/Dialog_XuatTraNCC.fxml", "Trả Nhà Cung Cấp"); }
    @FXML private void handleMoDialogXuatHuy() { moDialog("/gui/dialogs/Dialog_XuatHuy.fxml", "Xuất Hủy Thuốc"); }

    private void moDialog(String fxml, String title) {
        try {
            FXMLLoader l = new FXMLLoader(getClass().getResource(fxml));
            Parent r = l.load();
            Stage s = new Stage();
            s.initModality(Modality.APPLICATION_MODAL);
            s.setTitle(title); s.setScene(new Scene(r));
            s.showAndWait();
            loadPhieuXuat(); // Load lại bảng sau khi tắt Dialog lập phiếu
        } catch (IOException e) { e.printStackTrace(); }
    }
}