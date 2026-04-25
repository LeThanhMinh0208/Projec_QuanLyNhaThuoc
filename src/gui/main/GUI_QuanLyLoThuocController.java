package gui.main;

import connectDB.ConnectDB;
import dao.DAO_LoThuoc;
import dao.DAO_NhaCungCap; // Thêm DAO để lấy NCC
import entity.LoThuoc;
import entity.NhaCungCap;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import utils.AlertUtils;

import java.io.InputStream;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Date;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

public class GUI_QuanLyLoThuocController implements Initializable {

    @FXML private TextField txtTimKiem;
    @FXML private ComboBox<String> cbTrangThai;

    @FXML private HBox paneFilter;
    @FXML private DatePicker dpHsdTu;
    @FXML private DatePicker dpHsdDen;
    @FXML private TextField txtTonTu;
    @FXML private TextField txtTonDen;
    @FXML private ComboBox<String> cbKho;
    
    // 🚨 KHAI BÁO COMBOBOX LỌC NHÀ CUNG CẤP TỪ FXML 🚨
    @FXML private ComboBox<String> cbLocNhaCungCap;

    @FXML private TableView<LoThuoc> tableLoThuoc;
    @FXML private TableColumn<LoThuoc, String> colMaLo;
    @FXML private TableColumn<LoThuoc, LoThuoc> colAnh; 
    @FXML private TableColumn<LoThuoc, String> colTenThuoc;
    @FXML private TableColumn<LoThuoc, Date> colHanSuDung;
    @FXML private TableColumn<LoThuoc, Integer> colSoLuong;
    @FXML private TableColumn<LoThuoc, String> colViTri;
    @FXML private TableColumn<LoThuoc, String> colTrangThai; 
    @FXML private TableColumn<LoThuoc, LoThuoc> colThaoTac; 

    @FXML private VBox paneChiTiet;
    @FXML private Label lblNgayNhap;
    @FXML private Label lblGiaNhap;
    @FXML private Label lblNhaCungCap;
    @FXML private Label lblSlNhapBanDau;
    @FXML private Label lblSlDaBan;
    @FXML private Label lblSlTraNcc;
    @FXML private Label lblSlXuatHuy;
    @FXML private Label lblSlDoiTra;

    private DAO_LoThuoc daoLoThuoc;
    private ObservableList<LoThuoc> dsLoThuoc;
    private FilteredList<LoThuoc> filteredData;
    
    // Format ngày VN
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        daoLoThuoc = new DAO_LoThuoc();
        dsLoThuoc = FXCollections.observableArrayList();

        // 1. Bơm data cho ComboBox
        cbTrangThai.setItems(FXCollections.observableArrayList("Tất cả", "Đang hoạt động", "Đã khóa"));
        cbTrangThai.getSelectionModel().selectFirst();

        cbKho.setItems(FXCollections.observableArrayList("Tất cả", "Kho Bán Hàng", "Kho Dự Trữ"));
        cbKho.getSelectionModel().selectFirst();

        // 🚨 2. BƠM DATA CHO COMBOBOX NHÀ CUNG CẤP 🚨
        try {
            ObservableList<String> tenNccList = FXCollections.observableArrayList("Tất cả");
            List<NhaCungCap> dsNcc = new DAO_NhaCungCap().getAllNhaCungCap();
            
            if (dsNcc != null) {
                for (NhaCungCap ncc : dsNcc) {
                    if (ncc.getTenNhaCungCap() != null) {
                        tenNccList.add(ncc.getTenNhaCungCap());
                    }
                }
            }
            if (cbLocNhaCungCap != null) {
                cbLocNhaCungCap.setItems(tenNccList);
                cbLocNhaCungCap.getSelectionModel().selectFirst();
            }
        } catch (Exception e) {
            System.err.println("Lỗi load NCC: " + e.getMessage());
        }

        setupTable();
        setupColorsAndImages();
        loadData(); // Tự động khóa lô rác ngay lần load đầu tiên
        setupRowSelection();
        setupSmartFilter();
    }

    private void setupSmartFilter() {
        filteredData = new FilteredList<>(dsLoThuoc, b -> true);

        txtTonTu.textProperty().addListener((obs, old, newV) -> { if (!newV.matches("\\d*")) txtTonTu.setText(newV.replaceAll("[^\\d]", "")); updateFilter(); });
        txtTonDen.textProperty().addListener((obs, old, newV) -> { if (!newV.matches("\\d*")) txtTonDen.setText(newV.replaceAll("[^\\d]", "")); updateFilter(); });

        txtTimKiem.textProperty().addListener((obs, oldV, newV) -> updateFilter());
        cbTrangThai.valueProperty().addListener((obs, oldV, newV) -> updateFilter());
        dpHsdTu.valueProperty().addListener((obs, oldV, newV) -> updateFilter());
        dpHsdDen.valueProperty().addListener((obs, oldV, newV) -> updateFilter());
        cbKho.valueProperty().addListener((obs, oldV, newV) -> updateFilter());
        
        // 🚨 Bắt sự kiện lọc của NCC 🚨
        if(cbLocNhaCungCap != null) {
            cbLocNhaCungCap.valueProperty().addListener((obs, oldV, newV) -> updateFilter());
        }

        SortedList<LoThuoc> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableLoThuoc.comparatorProperty());
        tableLoThuoc.setItems(sortedData);
    }

    private void updateFilter() {
        filteredData.setPredicate(lo -> {
            String search = txtTimKiem.getText() != null ? txtTimKiem.getText().toLowerCase() : "";
            boolean matchSearch = search.isEmpty() || lo.getMaLoThuoc().toLowerCase().contains(search) ||
                                  (lo.getThuoc() != null && lo.getThuoc().getTenThuoc().toLowerCase().contains(search));

            String status = cbTrangThai.getValue();
            boolean matchStatus = true;
            if ("Đang hoạt động".equals(status)) matchStatus = (lo.getTrangThai() == 1);
            else if ("Đã khóa".equals(status)) matchStatus = (lo.getTrangThai() == 0);

            String kho = cbKho.getValue();
            boolean matchKho = true;
            if ("Kho Bán Hàng".equals(kho)) matchKho = "KHO_BAN_HANG".equals(lo.getViTriKho());
            else if ("Kho Dự Trữ".equals(kho)) matchKho = "KHO_DU_TRU".equals(lo.getViTriKho());

            boolean matchTon = true;
            try {
                int tonTu = txtTonTu.getText().isEmpty() ? 0 : Integer.parseInt(txtTonTu.getText());
                int tonDen = txtTonDen.getText().isEmpty() ? Integer.MAX_VALUE : Integer.parseInt(txtTonDen.getText());
                matchTon = (lo.getSoLuongTon() >= tonTu && lo.getSoLuongTon() <= tonDen);
            } catch (NumberFormatException e) { }

            boolean matchHsd = true;
            if (lo.getHanSuDung() != null) {
                LocalDate hsd = lo.getHanSuDung().toLocalDate();
                if (dpHsdTu.getValue() != null) matchHsd = matchHsd && !hsd.isBefore(dpHsdTu.getValue());
                if (dpHsdDen.getValue() != null) matchHsd = matchHsd && !hsd.isAfter(dpHsdDen.getValue());
            }
            
            // 🚨 KIỂM TRA ĐIỀU KIỆN NHÀ CUNG CẤP 🚨
            boolean matchNCC = true;
            if (cbLocNhaCungCap != null && cbLocNhaCungCap.getValue() != null) {
                String nccFilter = cbLocNhaCungCap.getValue();
                if (!"Tất cả".equals(nccFilter)) {
                    matchNCC = (lo.getNhaCungCap() != null && nccFilter.equals(lo.getNhaCungCap().getTenNhaCungCap()));
                }
            }

            return matchSearch && matchStatus && matchKho && matchTon && matchHsd && matchNCC;
        });
    }

    @FXML
    void toggleFilter(ActionEvent event) {
        boolean isVisible = paneFilter.isVisible();
        paneFilter.setVisible(!isVisible);
        paneFilter.setManaged(!isVisible);
    }

    @FXML
    void clearFilter(ActionEvent event) {
        dpHsdTu.setValue(null);
        dpHsdDen.setValue(null);
        txtTonTu.clear();
        txtTonDen.clear();
        cbKho.getSelectionModel().selectFirst();
        if(cbLocNhaCungCap != null) cbLocNhaCungCap.getSelectionModel().selectFirst();
    }

    private void setupTable() {
        colMaLo.setCellValueFactory(new PropertyValueFactory<>("maLoThuoc"));
        colTenThuoc.setCellValueFactory(cellData -> {
            if (cellData.getValue().getThuoc() != null) return new SimpleStringProperty(cellData.getValue().getThuoc().getTenThuoc());
            return new SimpleStringProperty("Không xác định");
        });
        colHanSuDung.setCellValueFactory(new PropertyValueFactory<>("hanSuDung"));
        colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuongTon"));
        colViTri.setCellValueFactory(new PropertyValueFactory<>("viTriKho"));

        colTrangThai.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().getTrangThai() == 1 ? "Đang hoạt động" : "Đã khóa"));
        
        colAnh.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue()));
        colThaoTac.setCellValueFactory(cellData -> new SimpleObjectProperty<>(cellData.getValue()));
    }

    private void setupColorsAndImages() {
        colAnh.setCellFactory(param -> new TableCell<LoThuoc, LoThuoc>() {
            private final ImageView iv = new ImageView();
            private final HBox box = new HBox(iv);
            { box.setAlignment(Pos.CENTER); }
            @Override
            protected void updateItem(LoThuoc lo, boolean empty) {
                super.updateItem(lo, empty);
                String file = (lo != null && lo.getThuoc() != null) ? lo.getThuoc().getHinhAnh() : null;
                if (empty || file == null || file.trim().isEmpty()) {
                    iv.setImage(null);
                    setGraphic(empty ? null : box);
                } else {
                    try {
                        InputStream is = getClass().getResourceAsStream("/resources/images/images_thuoc/" + file.trim());
                        if (is != null) iv.setImage(new Image(is, 60, 60, true, true));
                        else iv.setImage(null);
                    } catch (Exception e) { iv.setImage(null); }
                    setGraphic(box);
                }
            }
        });

        // HSD format dd-MM-yyyy
        colHanSuDung.setCellFactory(column -> new TableCell<LoThuoc, Date>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); } 
                else {
                    setText(item.toLocalDate().format(formatter)); 
                    long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), item.toLocalDate());
                    
                    // NẾU ĐÃ HẾT HẠN (daysBetween < 0) -> Đen xì, gạch ngang
                    if (daysBetween < 0) {
                        setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: bold; -fx-alignment: CENTER; -fx-strikethrough: true;"); 
                    } 
                    // CẬN DATE (<= 90 ngày) -> Màu Đỏ chót
                    else if (daysBetween <= 90) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-alignment: CENTER;"); 
                    } 
                    else {
                        setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: normal; -fx-alignment: CENTER;"); 
                    }
                }
            }
        });

        // SL < 100 TÔ ĐỎ, SL = 0 Gạch ngang
        colSoLuong.setCellFactory(column -> new TableCell<LoThuoc, Integer>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); } 
                else {
                    setText(String.valueOf(item));
                    if (item == 0) {
                        setStyle("-fx-text-fill: #7f8c8d; -fx-font-weight: bold; -fx-alignment: CENTER; -fx-strikethrough: true;"); 
                    } else if (item < 100) {
                        setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    } else {
                        setStyle("-fx-text-fill: #2c3e50; -fx-font-weight: normal; -fx-alignment: CENTER;"); 
                    }
                }
            }
        });

        colViTri.setCellFactory(column -> new TableCell<LoThuoc, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); } 
                else {
                    if ("KHO_BAN_HANG".equals(item)) {
                        setText("Kho Bán Hàng");
                        setStyle("-fx-text-fill: #27ae60; -fx-font-weight: bold; -fx-alignment: CENTER;"); 
                    } else {
                        setText("Kho Dự Trữ");
                        setStyle("-fx-text-fill: #e67e22; -fx-font-weight: bold; -fx-alignment: CENTER;"); 
                    }
                }
            }
        });

        colTrangThai.setCellFactory(column -> new TableCell<LoThuoc, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); } 
                else {
                    setText(item);
                    if (item.equals("Đã khóa")) setStyle("-fx-text-fill: #e74c3c; -fx-font-weight: bold; -fx-alignment: CENTER;");
                    else setStyle("-fx-text-fill: #2980b9; -fx-alignment: CENTER;");
                }
            }
        });

        colThaoTac.setCellFactory(param -> new TableCell<LoThuoc, LoThuoc>() {
            private final Button btnAction = new Button();

            @Override
            protected void updateItem(LoThuoc lo, boolean empty) {
                super.updateItem(lo, empty);
                if (empty || lo == null) {
                    setGraphic(null);
                } else {
                    // Bước 1: Xóa sạch các class cũ để tránh xung đột
                    btnAction.getStyleClass().clear();
                    
                    // Bước 2: Kiểm tra trạng thái để gán Class viên thuốc và Text tương ứng
                    if (lo.getTrangThai() == 1) { // Lô đang hoạt động
                        btnAction.setText("Khóa Lô");
                        btnAction.getStyleClass().add("btn-lock-pill"); // Class đỏ nhạt viên thuốc
                        btnAction.setOnAction(e -> xuLyKhoaLo(lo.getMaLoThuoc(), 0));
                        
                        // Logic kiểm tra Hết hàng hoặc Hết đát
                        long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(
                            java.time.LocalDate.now(), 
                            lo.getHanSuDung().toLocalDate()
                        );
                        
                        if(lo.getSoLuongTon() == 0 || daysBetween < 0) {
                            btnAction.setDisable(true); 
                            btnAction.setText("Hết Hàng/Date");
                            // Khi disable, JavaFX tự mờ đi nên vẫn giữ được dáng viên thuốc
                        } else {
                            btnAction.setDisable(false);
                        }
                    } else { // Lô đang bị khóa
                        btnAction.setText("Mở Khóa");
                        btnAction.getStyleClass().add("btn-unlock-pill"); // Class xanh lá viên thuốc
                        btnAction.setOnAction(e -> xuLyKhoaLo(lo.getMaLoThuoc(), 1));
                        btnAction.setDisable(false);
                    }
                    
                    setGraphic(btnAction);
                    setAlignment(Pos.CENTER);
                }
            }
        });
    }

    private void setupRowSelection() {
        tableLoThuoc.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                paneChiTiet.setVisible(true);
                paneChiTiet.setManaged(true);
                lblGiaNhap.setText(String.format("%,.0f VNĐ", newSelection.getGiaNhap()));
                
                String donViGoc = "";
                if (newSelection.getThuoc() != null && newSelection.getThuoc().getDonViCoBan() != null) {
                    donViGoc = newSelection.getThuoc().getDonViCoBan();
                }
                loadThongKeTheLo(newSelection.getMaLoThuoc(), donViGoc);
            } else {
                paneChiTiet.setVisible(false);
                paneChiTiet.setManaged(false);
            }
        });
    }

    private void loadThongKeTheLo(String maLoThuoc, String donVi) {
        String sql = "SELECT " +
            "l.ngayNhapKho, " +
            "ncc.tenNhaCungCap AS tenNCC, " +
            "l.soLuongTon, " +
            "(SELECT ISNULL(SUM(ctpn.soLuong * dq.tyLeQuyDoi), 0) FROM ChiTietPhieuNhap ctpn JOIN DonViQuyDoi dq ON ctpn.maQuyDoi = dq.maQuyDoi WHERE ctpn.maLoThuoc = l.maLoThuoc) AS slNhapThucTe, " +
            "(SELECT ISNULL(SUM(cthd.soLuong * dq.tyLeQuyDoi), 0) FROM ChiTietHoaDon cthd JOIN DonViQuyDoi dq ON cthd.maQuyDoi = dq.maQuyDoi WHERE cthd.maLoThuoc = l.maLoThuoc) AS slBan, " +
            "(SELECT ISNULL(SUM(ctpx.soLuong), 0) FROM ChiTietPhieuXuat ctpx JOIN PhieuXuat px ON ctpx.maPhieuXuat = px.maPhieuXuat WHERE ctpx.maLoThuoc = l.maLoThuoc AND px.loaiPhieu = 2) AS slTraNcc, " +
            "(SELECT ISNULL(SUM(ctpx.soLuong), 0) FROM ChiTietPhieuXuat ctpx JOIN PhieuXuat px ON ctpx.maPhieuXuat = px.maPhieuXuat WHERE ctpx.maLoThuoc = l.maLoThuoc AND px.loaiPhieu = 3) AS slXuatHuy, " +
            "(SELECT ISNULL(SUM(ctdt.soLuong), 0) FROM ChiTietDoiTra ctdt WHERE ctdt.maLoThuoc = l.maLoThuoc) AS slDoiTra " +
            "FROM LoThuoc l " +
            "LEFT JOIN NhaCungCap ncc ON l.maNhaCungCap = ncc.maNhaCungCap " +
            "WHERE l.maLoThuoc = ?";

        try (Connection con = ConnectDB.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {
            ps.setString(1, maLoThuoc);
            
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                java.sql.Date dNgayNhap = rs.getDate("ngayNhapKho");
                lblNgayNhap.setText(dNgayNhap != null ? dNgayNhap.toLocalDate().format(formatter) : "Không xác định");

                String tenNCC = rs.getString("tenNCC");
                lblNhaCungCap.setText(tenNCC != null ? tenNCC : "Không xác định");

                // Lấy các biến số lượng từ CSDL
                int slNhap = rs.getInt("slNhapThucTe"); 
                int slBan = rs.getInt("slBan");         
                int slTraNcc = rs.getInt("slTraNcc");
                int slXuatHuy = rs.getInt("slXuatHuy");
                int slDoiTra = rs.getInt("slDoiTra");   
                int tonKho = rs.getInt("soLuongTon");

    
                if (slNhap == 0 && (tonKho > 0 || slBan > 0 || slTraNcc > 0 || slXuatHuy > 0)) {
                    slNhap = tonKho + slBan + slTraNcc + slXuatHuy - slDoiTra;
                    if (slNhap < 0) slNhap = 0; 
                }

                // Đổ dữ liệu lên màn hình
                lblSlNhapBanDau.setText(slNhap + " " + donVi);
                lblSlDaBan.setText(slBan + " " + donVi);
                lblSlTraNcc.setText(slTraNcc + " " + donVi);
                lblSlXuatHuy.setText(slXuatHuy + " " + donVi);
                lblSlDoiTra.setText(slDoiTra + " " + donVi);
            }
        } catch (Exception e) { 
            e.printStackTrace(); 
        }
    }
    private void kiemTraVaKhoaLoTuDong(List<LoThuoc> list) {
        boolean coLoBiKhoaThuDong = false;
        
        for (LoThuoc lo : list) {
            if (lo.getTrangThai() == 1) {
                boolean canKhoa = false;
                
                if (lo.getSoLuongTon() <= 0) {
                    canKhoa = true;
                } 
                else if (lo.getHanSuDung() != null) {
                    long daysBetween = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), lo.getHanSuDung().toLocalDate());
                    if (daysBetween < 0) {
                        canKhoa = true;
                    }
                }

                if (canKhoa) {
                    daoLoThuoc.capNhatTrangThaiLo(lo.getMaLoThuoc(), 0);
                    lo.setTrangThai(0);
                    coLoBiKhoaThuDong = true;
                }
            }
        }
    }

    private void loadData() {
        dsLoThuoc.clear();
        List<LoThuoc> list = daoLoThuoc.getAllLoThuoc();
        
        kiemTraVaKhoaLoTuDong(list);
        
        dsLoThuoc.addAll(list);
        if (filteredData != null) updateFilter();
        paneChiTiet.setVisible(false);
        paneChiTiet.setManaged(false);
    }

    @FXML
    void handleLamMoi(ActionEvent event) {
        txtTimKiem.clear();
        cbTrangThai.getSelectionModel().selectFirst();
        clearFilter(null);
        loadData();
    }

    private void xuLyKhoaLo(String maLo, int trangThaiMoi) {
        boolean thanhCong = daoLoThuoc.capNhatTrangThaiLo(maLo, trangThaiMoi);
        if (thanhCong) {
            String msg = trangThaiMoi == 0 ? "Đã khóa lô thành công!" : "Đã mở khóa lô thành công!";
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Thành công", msg);
            loadData(); 
        } else {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Thao tác thất bại!");
        }
    }
}