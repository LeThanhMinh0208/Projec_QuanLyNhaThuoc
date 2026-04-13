package gui.main;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import dao.DAO_HoaDon;
import dao.DAO_KhachHang;
import dao.DAO_LoThuoc;
import dao.DAO_Thuoc;
import entity.ChiTietHoaDon;
import entity.DonThuoc;
import entity.DonViQuyDoi;
import entity.HoaDon;
import entity.KhachHang;
import entity.LoThuoc;
import entity.NhanVien;
import entity.Thuoc;
import gui.dialogs.Dialog_ChonSoLuongDonViController;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TabPane;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GUI_QuanLyBanHangController {

    @FXML private TabPane tabPaneBanHang;

    // --- TAB 1: BÁN LẺ ---
    @FXML private TextField txtTimKiemThuocLe;
    @FXML private TableView<Thuoc> tblThuocLe;
    @FXML private TableColumn<Thuoc, String> colThuocMa;
    @FXML private TableColumn<Thuoc, String> colThuocAnh;
    @FXML private TableColumn<Thuoc, String> colThuocTen;
    @FXML private TableColumn<Thuoc, String> colThuocTrieuChung;
    @FXML private TableColumn<Thuoc, Boolean> colThuocKeDon;
    @FXML private Button btnThemVaoGioLe;

    @FXML private TextField txtSdtKhachLe;
    @FXML private Label lblTenKhachLe;

    @FXML private TableView<CartItem> tblGioHangLe;
    @FXML private TableColumn<CartItem, String> colCartTenThuoc;
    @FXML private TableColumn<CartItem, String> colCartDonVi;
    @FXML private TableColumn<CartItem, Integer> colCartSoLuong;
    @FXML private TableColumn<CartItem, String> colCartDonGia;
    @FXML private TableColumn<CartItem, String> colCartThanhTien;
    @FXML private TableColumn<CartItem, String> colCartHanSuDung;
    @FXML private TableColumn<CartItem, Void> colCartXoa;

    @FXML private ComboBox<String> cbHinhThucThanhToanLe;
    @FXML private Label lblTongTienLe;
    @FXML private Label lblVatLe;
    @FXML private Label lblThanhTienLe;
    @FXML private Button btnThanhToanLe;

    // --- DAOs & Data ---
    private final DAO_Thuoc daoThuoc = new DAO_Thuoc();
    private final DAO_LoThuoc daoLoThuoc = new DAO_LoThuoc();
    private final DAO_KhachHang daoKhachHang = new DAO_KhachHang();
    private final DAO_HoaDon daoHoaDon = new DAO_HoaDon();

    private ObservableList<Thuoc> masterDataLe = FXCollections.observableArrayList();
    private ObservableList<CartItem> cartDataLe = FXCollections.observableArrayList();
    
    private KhachHang currentKhachHang = null;
    private double tongTienLe = 0;
    private double thueVatLe = 0;

    public class CartItem {
        public Thuoc     thuoc;
        public DonViQuyDoi donVi;
        public int       soLuong;
        public double    donGia;
        public double    thanhTien;
        public String    maBangGia;   // mã bảng giá đang áp dụng
        public String    maLoThuoc;   // lô FEFO được chọn khi thêm
        public java.sql.Date hanSuDung; // HSD của lô FEFO

        public CartItem(Thuoc thuoc, DonViQuyDoi donVi, int soLuong, double donGia, String maBangGia) {
            this.thuoc      = thuoc;
            this.donVi      = donVi;
            this.soLuong    = soLuong;
            this.donGia     = donGia;
            this.maBangGia  = maBangGia;
            this.thanhTien  = soLuong * donGia;
        }
    }

    @FXML
    public void initialize() {
        setupTableThuocLe();
        setupTableCartLe();
        loadDataThuocLe();
        setupSearchLogic();

        cbHinhThucThanhToanLe.getItems().addAll("Tiền mặt", "Chuyển khoản", "Thẻ tín dụng");
        cbHinhThucThanhToanLe.getSelectionModel().selectFirst();
        
        setupTableDonThuoc();
        setupTableCartDon();
        cbHinhThucThanhToanDon.getItems().addAll("Tiền mặt", "Chuyển khoản", "Thẻ tín dụng");
        cbHinhThucThanhToanDon.getSelectionModel().selectFirst();
        
        // Keyboard navigation
        setupKeyboardNavigation();
    }

    public void chonTabBanLe() {
        if (tabPaneBanHang != null) {
            tabPaneBanHang.getSelectionModel().select(0);
        }
    }

    private void setupTableThuocLe() {
        colThuocMa.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("maThuoc"));
        
        colThuocAnh.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("hinhAnh"));
        colThuocAnh.setCellFactory(column -> new TableCell<Thuoc, String>() {
            private final javafx.scene.image.ImageView imageView = new javafx.scene.image.ImageView();
            private final javafx.scene.layout.HBox box = new javafx.scene.layout.HBox(imageView);
            {
                imageView.setFitWidth(60);
                imageView.setFitHeight(60);
                imageView.setPreserveRatio(true);
                imageView.setSmooth(true);
                box.setAlignment(javafx.geometry.Pos.CENTER);
                box.setPrefHeight(72);
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null || item.trim().isEmpty()) {
                    imageView.setImage(null);
                    setGraphic(empty ? null : box);
                } else {
                    try {
                        java.io.InputStream is = getClass().getResourceAsStream("/resources/images/images_thuoc/" + item.trim());
                        if (is != null) {
                            imageView.setImage(new javafx.scene.image.Image(is, 60, 60, true, true));
                        } else {
                            imageView.setImage(null);
                        }
                    } catch (Exception e) {
                        imageView.setImage(null);
                    }
                    setGraphic(box);
                }
            }
        });
        
        colThuocTen.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("tenThuoc"));
        colThuocTrieuChung.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("trieuChung"));
        // colThuocTrangThai removed - no longer in FXML
        
        colThuocKeDon.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("canKeDon"));
        colThuocKeDon.setCellFactory(column -> new TableCell<Thuoc, Boolean>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "Có" : "Không");
                    setStyle(item ? "-fx-text-fill: #e74c3c; -fx-font-weight: bold;" : "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                }
            }
        });

        tblThuocLe.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tblThuocLe.getSelectionModel().getSelectedItem() != null) {
                handleThemVaoGioLe(null);
            }
        });
    }

    private void setupTableCartLe() {
        tblGioHangLe.setItems(cartDataLe);
        colCartTenThuoc.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().thuoc.getTenThuoc()));
        colCartDonVi.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().donVi.getTenDonVi()));
        colCartSoLuong.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().soLuong).asObject());
        colCartDonGia.setCellValueFactory(cd -> new SimpleStringProperty(String.format("%,.0f ₫", cd.getValue().donGia)));
        colCartThanhTien.setCellValueFactory(cd -> new SimpleStringProperty(String.format("%,.0f ₫", cd.getValue().thanhTien)));
        colCartHanSuDung.setCellValueFactory(cd -> {
            CartItem it = cd.getValue();
            if (it.hanSuDung != null) return new SimpleStringProperty(it.hanSuDung.toLocalDate().format(java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            return new SimpleStringProperty("FEFO");
        });
        
        colCartXoa.setCellFactory(column -> new TableCell<CartItem, Void>() {
            private final Button btn = new Button("✕");
            {
                btn.getStyleClass().add("bh-btn-remove");
                btn.setOnAction(e -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    cartDataLe.remove(item);
                    tinhTongTienLe();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });
    }

    private void loadDataThuocLe() {
        List<Thuoc> allThuoc = daoThuoc.getAllThuoc();
        List<Thuoc> filtered = new ArrayList<>();
        for (Thuoc t : allThuoc) {
            if ("DANG_BAN".equals(t.getTrangThai())) {
                filtered.add(t);
            }
        }
        masterDataLe.setAll(filtered);
        tblThuocLe.setItems(masterDataLe);
    }

    private void setupSearchLogic() {
        FilteredList<Thuoc> filteredData = new FilteredList<>(masterDataLe, p -> true);
        txtTimKiemThuocLe.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(thuoc -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (thuoc.getTenThuoc().toLowerCase().contains(lowerCaseFilter)) return true;
                if (thuoc.getMaThuoc().toLowerCase().contains(lowerCaseFilter)) return true;
                if (thuoc.getCongDung() != null && thuoc.getCongDung().toLowerCase().contains(lowerCaseFilter)) return true;
                if (thuoc.getTrieuChung() != null && thuoc.getTrieuChung().toLowerCase().contains(lowerCaseFilter)) return true;
                return false;
            });
        });
        SortedList<Thuoc> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tblThuocLe.comparatorProperty());
        tblThuocLe.setItems(sortedData);
    }

    @FXML void handleLamMoiLe(ActionEvent event) {
        txtTimKiemThuocLe.clear();
        loadDataThuocLe();
    }

    @FXML void handleThemVaoGioLe(ActionEvent event) {
        Thuoc selectedThuoc = tblThuocLe.getSelectionModel().getSelectedItem();
        if (selectedThuoc == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Vui lòng chọn thuốc để thêm vào giỏ!");
            return;
        }

        if (selectedThuoc.isCanKeDon()) {
            showAlert(AlertType.ERROR, "Lỗi Kê Đơn", "Thuốc này CẦN KÊ ĐƠN!\nVui lòng chuyển sang Tab 'Bán Theo Đơn Thuốc'.");
            return;
        }

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_ChonSoLuongDonVi.fxml"));
            Parent root = loader.load();
            Dialog_ChonSoLuongDonViController controller = loader.getController();
            controller.setThuoc(selectedThuoc);
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setScene(new Scene(root));
            stage.setTitle("Chọn Số Lượng");
            stage.showAndWait();

            DonViQuyDoi dv  = controller.getDonViChon();
            int  sl         = controller.getSoLuongChon();
            double donGia   = controller.getDonGiaChon();
            String maBangGia = controller.getMaBangGiaChon();

            if (dv != null && sl > 0 && maBangGia != null) {
                // Kiểm tra thuốc + đơn vị đã có trong giỏ → cộng dồn
                boolean found = false;
                for (CartItem item : cartDataLe) {
                    if (item.thuoc.getMaThuoc().equals(selectedThuoc.getMaThuoc())
                            && item.donVi.getMaQuyDoi().equals(dv.getMaQuyDoi())) {
                        item.soLuong   += sl;
                        item.thanhTien  = item.soLuong * item.donGia;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    // Lấy lô FEFO ngay khi thêm để hiển thị HSD
                    CartItem ci = new CartItem(selectedThuoc, dv, sl, donGia, maBangGia);
                    String maLo = daoLoThuoc.getLoFEFO(selectedThuoc.getMaThuoc());
                    if (maLo != null) {
                        ci.maLoThuoc = maLo;
                        // Lấy HSD để hiển thị
                        daoLoThuoc.getLoThuocBanDuocByMaThuoc(selectedThuoc.getMaThuoc()).stream()
                            .filter(l -> l.getMaLoThuoc().equals(maLo)).findFirst()
                            .ifPresent(l -> ci.hanSuDung = l.getHanSuDung());
                    }
                    cartDataLe.add(ci);
                }
                tblGioHangLe.refresh();
                tinhTongTienLe();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML void handleTimKhachLe(ActionEvent event) {
        String sdt = txtSdtKhachLe.getText().trim();
        if (sdt.isEmpty()) {
            currentKhachHang = null;
            lblTenKhachLe.setText("👤 Khách lẻ (vãng lai)");
            return;
        }
        KhachHang kh = daoKhachHang.getBySdt(sdt);
        if (kh != null) {
            currentKhachHang = kh;
            lblTenKhachLe.setText("👤 " + kh.getHoTen() + " (Điểm: " + kh.getDiemTichLuy() + ")");
        } else {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Không tìm thấy khách hàng với SĐT: " + sdt + ".\nBạn có muốn thêm khách hàng mới không?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait();
            if (confirm.getResult() == ButtonType.YES) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_ThemKhachHang.fxml"));
                    Scene scene = new Scene(loader.load());
                    gui.dialogs.Dialog_ThemKhachHangController controller = loader.getController();
                    
                    // Sinh mã khách hàng tiếp theo
                    int max = 0;
                    for (KhachHang khach : daoKhachHang.getAllKhachHang()) {
                        String ma = khach.getMaKhachHang();
                        if (ma != null && ma.startsWith("KH")) {
                            try {
                                int num = Integer.parseInt(ma.substring(2));
                                if (num > max) max = num;
                            } catch (Exception e) {}
                        }
                    }
                    String nextIdSeq = String.format("KH%03d", max + 1);
                    controller.setMaKhachHang(nextIdSeq);
                    controller.setSdt(sdt);
                    
                    Stage dialogStage = new Stage();
                    dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
                    dialogStage.initStyle(javafx.stage.StageStyle.UTILITY);
                    dialogStage.setTitle("Thêm Khách Hàng Mới");
                    dialogStage.setScene(scene);
                    dialogStage.showAndWait();

                    KhachHang result = controller.getResultKhachHang();
                    if (result != null) {
                        // User đã nhấp Lưu trong dialog → thêm vào DB
                        boolean saved = daoKhachHang.themKhachHang(result);
                        if (saved) {
                            currentKhachHang = result;
                            lblTenKhachLe.setText("👤 " + result.getHoTen() + " (Điểm: 0)");
                            txtSdtKhachLe.setText(result.getSdt());
                            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm khách hàng thành công! Mã: " + result.getMaKhachHang());
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Thất bại", "Lỗi cơ sở dữ liệu khi lưu khách hàng. Vui lòng thử lại.");
                            currentKhachHang = null;
                            lblTenKhachLe.setText("👤 Khách lẻ (vãng lai)");
                        }
                    } else {
                        // User hủy dialog → không làm gì, giữ Khách lẻ
                        currentKhachHang = null;
                        lblTenKhachLe.setText("👤 Khách lẻ (vãng lai)");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở form thêm khách hàng: " + e.getMessage());
                }
            } else {
                currentKhachHang = null;
                lblTenKhachLe.setText("👤 Không tìm thấy khách hàng: " + sdt);
            }
        }
    }

    @FXML void handleHuyGioLe(ActionEvent event) {
        cartDataLe.clear();
        tinhTongTienLe();
        currentKhachHang = null;
        txtSdtKhachLe.clear();
        lblTenKhachLe.setText("👤 Khách lẻ (vãng lai)");
    }

    @FXML void handleThanhToanLe(ActionEvent event) {
        if (cartDataLe.isEmpty()) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Giỏ hàng trống!");
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION, "Xác nhận thanh toán hóa đơn với tổng số tiền: " + String.format("%,.0f ₫", tongTienLe + thueVatLe) + " ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() != ButtonType.YES) return;
        
        // Kiem tra thuoc ke don
        for (CartItem item : cartDataLe) {
            if (item.thuoc.isCanKeDon()) {
                showAlert(AlertType.ERROR, "Lỗi Kê Đơn", "Đơn hàng có thuốc kê đơn (" + item.thuoc.getTenThuoc() + "). Vui lòng chuyển sang Tab 'Bán Theo Đơn Thuốc'.");
                return;
            }
        }
        
        NhanVien user = GUI_TrangChuController.getNhanVienDangNhap();
        if (user == null) {
            user = new NhanVien("NV001", "testuser", "123", "Nhân Viên Test", "Nhân viên", "Ca 1", "0123",1); // Fallback for debugging
        }
        
        // Tao Hoa don
        String maHD = daoHoaDon.generateMaHoaDon();
        String pMethod = cbHinhThucThanhToanLe.getValue() != null ? cbHinhThucThanhToanLe.getValue().toString() : "";
        String dbHinhThuc = pMethod.equals("Chuyển khoản") ? "CHUYEN_KHOAN" : 
                            pMethod.equals("Thẻ tín dụng") ? "THE" : "TIEN_MAT";

        // --- FIX thueVAT: DB lưu 8.0 = 8%, KHÔNG phải 0.08 ---
        HoaDon hd = new HoaDon(maHD, java.sql.Date.valueOf(LocalDate.now()), 8.0, 
                dbHinhThuc, "", user, currentKhachHang);
        
        // Tạo danh sách ChiTietHoaDon – mỗi sản phẩm 1 lô FEFO (strict)
        List<ChiTietHoaDon> dsCT = new ArrayList<>();
        for (CartItem item : cartDataLe) {
            String maLo = item.maLoThuoc != null ? item.maLoThuoc
                        : daoLoThuoc.getLoFEFO(item.thuoc.getMaThuoc());
            if (maLo == null) {
                showAlert(AlertType.ERROR, "Lỗi tồn kho",
                    "Không tìm thấy lô hàng hợp lệ (còn hàng, chưa hết hạn) cho: " + item.thuoc.getTenThuoc());
                return;
            }
            int soLuongCoBan = item.soLuong * item.donVi.getTyLeQuyDoi();
            ChiTietHoaDon ct = new ChiTietHoaDon();
            ct.setMaBangGia(item.maBangGia);
            ct.setMaQuyDoi(item.donVi.getMaQuyDoi());
            ct.setMaLoThuoc(maLo);
            ct.setSoLuong(item.soLuong);
            ct.setDonGia(item.donGia);
            ct.setSoLuongTruKho(soLuongCoBan);
            dsCT.add(ct);
        }
        
        // Lưu Hóa Đơn (Phần DAO_HoaDon hiện đang deduct bằng soLuong, nên sẽ lỗi logic tỷ lệ quy đổi nếu ta không sửa DAO_HoaDon)
        // Note: For now we proceed...
        boolean ok = daoHoaDon.thanhToan(hd, dsCT);
        if (ok) {
            // Tích điểm
            if (currentKhachHang != null) {
                int diemCong = (int) ((tongTienLe + thueVatLe) / 1000);
                daoKhachHang.capNhatDiemTichLuy(currentKhachHang.getMaKhachHang(), currentKhachHang.getDiemTichLuy() + diemCong);
            }
            showAlert(AlertType.INFORMATION, "Thành công", "Thanh toán thành công! Mã HĐ: " + maHD);
            handleHuyGioLe(null);
        } else {
            showAlert(AlertType.ERROR, "Lỗi", "Thanh toán thất bại. Vui lòng thử lại!");
        }
    }

    private void tinhTongTienLe() {
        tongTienLe = cartDataLe.stream().mapToDouble(item -> item.thanhTien).sum();
        thueVatLe = tongTienLe * 0.08;
        double th = tongTienLe + thueVatLe;

        lblTongTienLe.setText(String.format("%,.0f ₫", tongTienLe));
        lblVatLe.setText(String.format("%,.0f ₫", thueVatLe));
        lblThanhTienLe.setText(String.format("%,.0f ₫", th));
    }

    private void showAlert(AlertType type, String title, String msg) {
        Alert a = new Alert(type, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }

    // --- TAB 2 UI Elements ---
    @FXML private TableView<DonThuoc> tblDonThuoc;
    @FXML private TableColumn<DonThuoc, String> colDTMa, colDTBacSi, colDTBenhNhan;
    @FXML private Label lblDonThuocChiTiet;
    @FXML private TextField txtSdtKhachDon;
    @FXML private Label lblTenKhachDon;
    @FXML private TableView<CartItem> tblGioHangDon;
    @FXML private TableColumn<CartItem, String> colCartDonTenThuoc, colCartDonDonVi;
    @FXML private TableColumn<CartItem, Integer> colCartDonSoLuong;
    @FXML private TableColumn<CartItem, String> colCartDonDonGia, colCartDonThanhTien, colCartDonHanSuDung;
    @FXML private TableColumn<CartItem, Void> colCartDonXoa;
    @FXML private ComboBox<String> cbHinhThucThanhToanDon;
    @FXML private Label lblTongTienDon, lblVatDon, lblThanhTienDon;
    @FXML private Button btnThanhToanDon;

    private ObservableList<DonThuoc> dsDonThuocPendings = FXCollections.observableArrayList();
    private ObservableList<CartItem> cartDataDon = FXCollections.observableArrayList();
    private KhachHang currentKhachHangDon = null;
    private DonThuoc currentDonThuoc = null;
    private double tongTienDon = 0;
    private double thueVatDon = 0;
    private final dao.DAO_DonThuoc daoDonThuoc = new dao.DAO_DonThuoc();

    private void setupTableDonThuoc() {
        colDTMa.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("maDonThuoc"));
        colDTBacSi.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("tenBacSi"));
        colDTBenhNhan.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("thongTinBenhNhan"));
        tblDonThuoc.setItems(dsDonThuocPendings);
        tblDonThuoc.getSelectionModel().selectedItemProperty().addListener((obs, oldSelection, newSelection) -> {
            if (newSelection != null) {
                lblDonThuocChiTiet.setText("Bác sĩ: " + newSelection.getTenBacSi() + "\nBệnh nhân: " + newSelection.getThongTinBenhNhan() + "\nChẩn đoán: " + newSelection.getChanDoan());
            } else {
                lblDonThuocChiTiet.setText("Chọn 1 đơn thuốc để xem chi tiết.");
            }
        });
    }

    private void setupTableCartDon() {
        colCartDonTenThuoc.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().thuoc.getTenThuoc()));
        colCartDonDonVi.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().donVi.getTenDonVi()));
        colCartDonSoLuong.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("soLuong"));
        colCartDonDonGia.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%,.0f", cellData.getValue().donGia)));
        colCartDonThanhTien.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%,.0f", cellData.getValue().thanhTien)));
        
        colCartDonXoa.setCellFactory(param -> new TableCell<>() {
            private final Button btnXoa = new Button("🗑");
            {
                btnXoa.setStyle("-fx-text-fill: red; -fx-background-color: transparent; -fx-cursor: hand;");
                btnXoa.setOnAction(event -> {
                    CartItem item = getTableView().getItems().get(getIndex());
                    cartDataDon.remove(item);
                    tinhTongTienDon();
                });
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnXoa);
            }
        });
        tblGioHangDon.setItems(cartDataDon);
    }

    private void tinhTongTienDon() {
        tongTienDon = cartDataDon.stream().mapToDouble(item -> item.thanhTien).sum();
        thueVatDon = tongTienDon * 0.08;
        double th = tongTienDon + thueVatDon;
        lblTongTienDon.setText(String.format("%,.0f ₫", tongTienDon));
        lblVatDon.setText(String.format("%,.0f ₫", thueVatDon));
        lblThanhTienDon.setText(String.format("%,.0f ₫", th));
    }

    @FXML void handleThemDonThuoc(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_ThemDonThuoc.fxml"));
            Scene scene = new Scene(loader.load());
            gui.dialogs.Dialog_ThemDonThuocController controller = loader.getController();
            
            int maxDB = daoDonThuoc.getMaxMaDonThuoc();
            int maxPending = 0;
            for (DonThuoc p : dsDonThuocPendings) {
                try {
                    if (p.getMaDonThuoc() != null && p.getMaDonThuoc().startsWith("DT")) {
                        int pNum = Integer.parseInt(p.getMaDonThuoc().substring(2));
                        if (pNum > maxPending) maxPending = pNum;
                    }
                } catch(Exception ex) {}
            }
            int nextNum = Math.max(maxDB, maxPending) + 1;
            String nextMa = String.format("DT%04d", nextNum);
            
            controller.setDonThuoc(null, nextMa);
            
            Stage stg = new Stage();
            stg.initModality(Modality.APPLICATION_MODAL);
            stg.setTitle("Thêm Đơn Thuốc");
            stg.setScene(scene);
            stg.showAndWait();
            
            DonThuoc res = controller.getResultDonThuoc();
            if (res != null) {
                dsDonThuocPendings.add(res);
                tblDonThuoc.getSelectionModel().select(res);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML void handleSuaDonThuoc(ActionEvent event) {
        DonThuoc dt = tblDonThuoc.getSelectionModel().getSelectedItem();
        if (dt == null) return;
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_ThemDonThuoc.fxml"));
            Scene scene = new Scene(loader.load());
            gui.dialogs.Dialog_ThemDonThuocController controller = loader.getController();
            
            controller.setDonThuoc(dt, null);
            
            Stage stg = new Stage();
            stg.initModality(Modality.APPLICATION_MODAL);
            stg.setTitle("Sửa Đơn Thuốc");
            stg.setScene(scene);
            stg.showAndWait();
            
            DonThuoc res = controller.getResultDonThuoc();
            if (res != null) {
                int idx = dsDonThuocPendings.indexOf(dt);
                dsDonThuocPendings.set(idx, res);
                tblDonThuoc.getSelectionModel().select(res);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML void handleXoaDonThuoc(ActionEvent event) {
        DonThuoc dt = tblDonThuoc.getSelectionModel().getSelectedItem();
        if (dt == null) {
            showAlert(AlertType.WARNING, "Chưa Chọn", "Vui lòng chọn đơn thuốc cần xóa!");
            return;
        }
        
        Alert confirm = new Alert(AlertType.CONFIRMATION, "Bạn có chắn chắn muốn xóa bỏ đơn thuốc đang chờ này?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            dsDonThuocPendings.remove(dt);
            if(currentDonThuoc == dt) {
                currentDonThuoc = null;
                cartDataDon.clear();
                tinhTongTienDon();
            }
        }
    }

    @FXML void handleChuyenDonSangGio(ActionEvent event) {
        DonThuoc dt = tblDonThuoc.getSelectionModel().getSelectedItem();
        if (dt == null) {
            showAlert(AlertType.WARNING, "Lỗi", "Hãy chọn 1 đơn thuốc bên trái!");
            return;
        }
        currentDonThuoc = dt;
        cartDataDon.clear();
        tinhTongTienDon();
        showAlert(AlertType.INFORMATION, "Chuyển Đơn", "Đã chọn đơn thuốc " + dt.getMaDonThuoc() + ". Vui lòng thêm thuốc vào giỏ!");
    }

    @FXML void handleTimKhachDon(ActionEvent event) {
        String sdt = txtSdtKhachDon.getText().trim();
        if (sdt.isEmpty()) {
            currentKhachHangDon = null;
            lblTenKhachDon.setText("👤 Khách lẻ (vãng lai)");
            return;
        }
        KhachHang kh = daoKhachHang.getBySdt(sdt);
        if (kh != null) {
            currentKhachHangDon = kh;
            lblTenKhachDon.setText("👤 " + kh.getHoTen() + " (Điểm: " + kh.getDiemTichLuy() + ")");
        } else {
            Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Phát hiện khách mới, bạn có muốn thêm KH?", ButtonType.YES, ButtonType.NO);
            confirm.showAndWait();
            if (confirm.getResult() == ButtonType.YES) {
                try {
                    FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_ThemKhachHang.fxml"));
                    Scene scene = new Scene(loader.load());
                    gui.dialogs.Dialog_ThemKhachHangController controller = loader.getController();
                    int max = 0;
                    for (KhachHang khach : daoKhachHang.getAllKhachHang()) {
                        if (khach.getMaKhachHang().startsWith("KH")) {
                            try {
                                int num = Integer.parseInt(khach.getMaKhachHang().substring(2));
                                if (num > max) max = num;
                            } catch (Exception e) {}
                        }
                    }
                    controller.setMaKhachHang(String.format("KH%03d", max + 1));
                    controller.setSdt(sdt);
                    Stage stg = new Stage();
                    stg.initModality(Modality.APPLICATION_MODAL);
                    stg.setScene(scene);
                    stg.showAndWait();

                    KhachHang res = controller.getResultKhachHang();
                    if (res != null && daoKhachHang.themKhachHang(res)) {
                        currentKhachHangDon = res;
                        lblTenKhachDon.setText("👤 " + res.getHoTen() + " (Điểm: 0)");
                        txtSdtKhachDon.setText(res.getSdt());
                        showAlert(AlertType.INFORMATION, "Thành công", "Thêm khách hàng thành công!");
                    } else {
                        currentKhachHangDon = null;
                        lblTenKhachDon.setText("👤 Không tìm thấy khách hàng: " + sdt);
                    }
                } catch (Exception e) { e.printStackTrace(); }
            } else {
                currentKhachHangDon = null;
                lblTenKhachDon.setText("👤 Không tìm thấy khách hàng: " + sdt);
            }
        }
    }

    @FXML void handleThemThuocDon(ActionEvent event) {
        if (currentDonThuoc == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Hãy chọn 1 đơn thuốc và bấm Chuyển đơn vào giỏ trước khi kê thuốc!");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_ChonThuoc.fxml"));
            Scene scene = new Scene(loader.load());
            gui.dialogs.Dialog_ChonThuocController controller = loader.getController();
            
            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Chọn Thuốc Kê Đơn");
            stage.setScene(scene);
            stage.showAndWait();
            
            Thuoc selectedThuoc = controller.getThuocChon();
            if (selectedThuoc != null) {
                FXMLLoader loader2 = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_ChonSoLuongDonVi.fxml"));
                Scene scene2 = new Scene(loader2.load());
                Dialog_ChonSoLuongDonViController controller2 = loader2.getController();
                controller2.setThuoc(selectedThuoc);
                
                Stage stage2 = new Stage();
                stage2.initModality(Modality.APPLICATION_MODAL);
                stage2.setTitle("Nhập Số Lượng");
                stage2.setScene(scene2);
                stage2.showAndWait();
                
                int sl = controller2.getSoLuongChon();
                DonViQuyDoi dv = controller2.getDonViChon();
                
                if (sl > 0 && dv != null && controller2.getMaBangGiaChon() != null) {
                    CartItem existing = cartDataDon.stream()
                        .filter(i -> i.thuoc.getMaThuoc().equals(selectedThuoc.getMaThuoc())
                                  && i.donVi.getMaQuyDoi().equals(dv.getMaQuyDoi()))
                        .findFirst().orElse(null);
                    if (existing != null) {
                        existing.soLuong   += sl;
                        existing.thanhTien  = existing.soLuong * existing.donGia;
                    } else {
                        CartItem ci = new CartItem(selectedThuoc, dv, sl,
                                controller2.getDonGiaChon(), controller2.getMaBangGiaChon());
                        String maLo = daoLoThuoc.getLoFEFO(selectedThuoc.getMaThuoc());
                        if (maLo != null) {
                            ci.maLoThuoc = maLo;
                            daoLoThuoc.getLoThuocBanDuocByMaThuoc(selectedThuoc.getMaThuoc()).stream()
                                .filter(l -> l.getMaLoThuoc().equals(maLo)).findFirst()
                                .ifPresent(l -> ci.hanSuDung = l.getHanSuDung());
                        }
                        cartDataDon.add(ci);
                    }
                    tblGioHangDon.refresh();
                    tinhTongTienDon();
                }
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML void handleHuyGioDon(ActionEvent event) {
        cartDataDon.clear();
        tinhTongTienDon();
        currentDonThuoc = null;
        currentKhachHangDon = null;
        txtSdtKhachDon.clear();
        lblTenKhachDon.setText("👤 Khách lẻ (vãng lai)");
    }

    @FXML void handleThanhToanDon(ActionEvent event) {
        if (cartDataDon.isEmpty() || currentDonThuoc == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Giỏ hàng theo đơn hiện đang rỗng hoặc chưa chọn Đơn thuốc!");
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION, "Xác nhận thanh toán hóa đơn với tổng số tiền: " + String.format("%,.0f ₫", tongTienDon + thueVatDon) + " ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() != ButtonType.YES) return;
        
        NhanVien user = GUI_TrangChuController.getNhanVienDangNhap();
        if (user == null) {
            user = new NhanVien("NV001", "testuser", "123", "Nhân Viên Test", "Nhân viên", "Ca 1", "0123",1);
        }
        
        String maHD = daoHoaDon.generateMaHoaDon();
        String pMethod = cbHinhThucThanhToanDon.getValue() != null ? cbHinhThucThanhToanDon.getValue().toString() : "";
        String dbHinhThuc = pMethod.equals("Chuyển khoản") ? "CHUYEN_KHOAN" : 
                            pMethod.equals("Thẻ tín dụng") ? "THE" : "TIEN_MAT";

        // --- FIX thueVAT: DB lưu 8.0 = 8%, KHÔNG phải 0.08 ---
        HoaDon hd = new HoaDon(maHD, java.sql.Date.valueOf(LocalDate.now()), 8.0, dbHinhThuc, "Bán hóa đơn theo đơn thuốc", user, currentKhachHangDon);
        
        List<ChiTietHoaDon> dsCT = new ArrayList<>();
        for (CartItem item : cartDataDon) {
            String maLo = item.maLoThuoc != null ? item.maLoThuoc
                        : daoLoThuoc.getLoFEFO(item.thuoc.getMaThuoc());
            if (maLo == null) {
                showAlert(AlertType.ERROR, "Lỗi tồn kho",
                    "Không tìm thấy lô hàng hợp lệ cho: " + item.thuoc.getTenThuoc());
                return;
            }
            int soLuongCoBan = item.soLuong * item.donVi.getTyLeQuyDoi();
            ChiTietHoaDon ct = new ChiTietHoaDon();
            ct.setMaBangGia(item.maBangGia);
            ct.setMaQuyDoi(item.donVi.getMaQuyDoi());
            ct.setMaLoThuoc(maLo);
            ct.setSoLuong(item.soLuong);
            ct.setDonGia(item.donGia);
            ct.setSoLuongTruKho(soLuongCoBan);
            dsCT.add(ct);
        }
        
        boolean ok = daoHoaDon.thanhToan(hd, dsCT);
        if (ok) {
            // Chèn đơn thuốc
            String oldMa = currentDonThuoc.getMaDonThuoc();
            currentDonThuoc.setMaHoaDon(maHD);
            daoDonThuoc.themDonThuoc(currentDonThuoc);
            
            // Xóa Đơn Thuốc Peding
            dsDonThuocPendings.removeIf(d -> d.getMaDonThuoc().equals(oldMa));
            
            if (currentKhachHangDon != null) {
                int diemCong = (int) ((tongTienDon + thueVatDon) / 1000);
                daoKhachHang.capNhatDiemTichLuy(currentKhachHangDon.getMaKhachHang(), currentKhachHangDon.getDiemTichLuy() + diemCong);
            }
            showAlert(AlertType.INFORMATION, "Thành công", "Thanh toán thành công! Mã Hóa Đơn: " + maHD);
            handleHuyGioDon(null);
        } else {
            showAlert(AlertType.ERROR, "Lỗi CSDL", "Thanh toán theo đơn thất bại!");
        }
    }

    // =================================================================
    // KEYBOARD NAVIGATION (FIX 5)
    // =================================================================
    private void setupKeyboardNavigation() {
        // Tab lẻ - Ô tìm kiếm thuốc: ENTER → focus xuống TableView
        txtTimKiemThuocLe.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                tblThuocLe.requestFocus();
                if (!tblThuocLe.getItems().isEmpty()) {
                    tblThuocLe.getSelectionModel().selectFirst();
                }
                event.consume();
            }
        });

        // Tab lẻ - TableView thuốc: ENTER hoặc Space → thêm vào giỏ
        tblThuocLe.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER ||
                event.getCode() == javafx.scene.input.KeyCode.SPACE) {
                if (!tblThuocLe.getSelectionModel().isEmpty()) {
                    handleThemVaoGioLe(null);
                    event.consume();
                }
            }
        });

        // Tab lẻ - Ô tìm SĐT khách: ENTER → tìm kiếm
        txtSdtKhachLe.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                handleTimKhachLe(null);
                event.consume();
            }
        });

        // Tab đơn - Ô tìm SĐT khách: ENTER → tìm kiếm
        txtSdtKhachDon.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                handleTimKhachDon(null);
                event.consume();
            }
        });
    }
}