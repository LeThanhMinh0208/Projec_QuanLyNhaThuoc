package gui.main;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import dao.DAO_DonThuoc;
import dao.DAO_HoaDon;
import dao.DAO_KhachHang;
import dao.DAO_LoThuoc;
import dao.DAO_NhatKyHoatDong;
import dao.DAO_Thuoc;
import entity.ChiTietHoaDon;
import entity.DonThuoc;
import entity.DonViQuyDoi;
import entity.HoaDon;
import entity.HoaDonView;
import entity.KhachHang;
import entity.NhanVien;
import entity.Thuoc;
import gui.dialogs.Dialog_ChonSoLuongDonViController;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.HoaDonPdfExporter;

public class GUI_QuanLyBanHangController {

    @FXML private TabPane tabPaneBanHang;
    @FXML private javafx.scene.control.ToggleButton btnBanLe;
    @FXML private javafx.scene.control.ToggleButton btnDonThuoc;

    @FXML
    private void onTabBanLe() {
        btnBanLe.setSelected(true);
        btnDonThuoc.setSelected(false);
        if (tabPaneBanHang != null) {
			tabPaneBanHang.getSelectionModel().select(0);
		}
    }

    @FXML
    private void onTabDonThuoc() {
        btnDonThuoc.setSelected(true);
        btnBanLe.setSelected(false);
        if (tabPaneBanHang != null) {
			tabPaneBanHang.getSelectionModel().select(1);
		}
    }

    // --- TAB 1: BÁN LẺ ---
    @FXML private TextField txtTimKiemThuocLe;
    @FXML private TableView<Thuoc> tblThuocLe;
    @FXML private TableColumn<Thuoc, String> colThuocMa;
    @FXML private TableColumn<Thuoc, String> colThuocAnh;
    @FXML private TableColumn<Thuoc, String> colThuocTen;
    @FXML private TableColumn<Thuoc, String> colThuocTrieuChung;
    @FXML private TableColumn<Thuoc, Boolean> colThuocKeDon;
    @FXML private Button btnThemVaoGioLe;

    // VĐ4: Thông tin khách hàng - đã chuyển lên header
    @FXML private TextField txtSdtKhachLe;
    @FXML private Label lblTenKhachLe;
    @FXML private Button btnXoaKH;

    @FXML private TableView<CartItem> tblGioHangLe;
    @FXML private TableColumn<CartItem, String> colCartTenThuoc;
    @FXML private TableColumn<CartItem, String> colCartDonVi;
    @FXML private TableColumn<CartItem, Integer> colCartSoLuong;
    @FXML private TableColumn<CartItem, String> colCartDonGia;
    @FXML private TableColumn<CartItem, String> colCartThanhTien;
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

 // 🚨 BIẾN STATIC ĐỂ NHẬN DỮ LIỆU TÁI LẬP TỪ MÀN HÌNH KHÁC
    private static GUI_QuanLyBanHangController instance;
    private static DonThuoc donThuocTaiLapPending = null;
    private static Image hinhAnhTaiLapPending = null;
    public static void setHinhAnhTaiLap(Image img) { hinhAnhTaiLapPending = img; }

    public static GUI_QuanLyBanHangController getInstance() { return instance; }
    public static void setTaiLapData(DonThuoc dt) { donThuocTaiLapPending = dt; }

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
    	instance = this;
        setupTableThuocLe();
        setupTableCartLe();
        loadDataLeAsync(); // Thay đổi ở đây
        setupSearchLogic();

        cbHinhThucThanhToanLe.getItems().addAll("Tiền mặt", "Chuyển khoản", "Thẻ tín dụng");
        cbHinhThucThanhToanLe.getSelectionModel().selectFirst();

        // TAB 2: Bán theo đơn
        setupTableThuocDon();
        setupTableCartDon();
        loadDataDonAsync(); // Thay đổi ở đây
        setupSearchLogicDon();
        cbHinhThucThanhToanDon.getItems().addAll("Tiền mặt", "Chuyển khoản", "Thẻ tín dụng");
        cbHinhThucThanhToanDon.getSelectionModel().selectFirst();

        // VĐ1: Disable nút "Thêm vào giỏ" tab đơn khi chưa có đơn thuốc
        if (btnThemVaoGioDon != null) {
			btnThemVaoGioDon.setDisable(true);
		}
        // VĐ1-card: Ẩn paneThongTinDon khi khởi tạo
        if (paneThongTinDon != null) { paneThongTinDon.setVisible(false); paneThongTinDon.setManaged(false); }

        // Keyboard navigation
        setupKeyboardNavigation();
        javafx.application.Platform.runLater(() -> {
            if (donThuocTaiLapPending != null) {
                xuLyTaiLapDonThuoc(donThuocTaiLapPending);
                donThuocTaiLapPending = null;
            }
        });
    }

    public void chonTabBanLe() {
        if (tabPaneBanHang != null) {
            tabPaneBanHang.getSelectionModel().select(0);
        }
        if (btnBanLe != null) {
            btnBanLe.setSelected(true);
        }
    }

    private void setupTableThuocLe() {
        colThuocMa.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("maThuoc"));
        colThuocMa.setCellFactory(col -> {
            TableCell<Thuoc, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item);
                }
            };
            cell.setAlignment(javafx.geometry.Pos.CENTER);
            return cell;
        });

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
        colThuocTen.setCellFactory(col -> {
            TableCell<Thuoc, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item);
                }
            };
            cell.setAlignment(javafx.geometry.Pos.CENTER);
            return cell;
        });
        colThuocTrieuChung.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("trieuChung"));

        colThuocKeDon.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("canKeDon"));
        colThuocKeDon.setCellFactory(column -> new TableCell<Thuoc, Boolean>() {
            {
                setAlignment(javafx.geometry.Pos.CENTER);
            }
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
        tblGioHangLe.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        colCartTenThuoc.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().thuoc.getTenThuoc()));
        colCartDonVi.setCellValueFactory(cd -> new SimpleStringProperty(cd.getValue().donVi.getTenDonVi()));
        colCartSoLuong.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().soLuong).asObject());
        colCartDonGia.setCellValueFactory(cd -> new SimpleStringProperty(String.format("%,.0f ₫", cd.getValue().donGia)));
        colCartThanhTien.setCellValueFactory(cd -> new SimpleStringProperty(String.format("%,.0f ₫", cd.getValue().thanhTien)));

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

    // VĐ3: Chỉ hiện thuốc KHÔNG kê đơn cho tab bán lẻ
    private void loadDataLeAsync() {
        Task<List<Thuoc>> task = new Task<>() {
            @Override
            protected List<Thuoc> call() throws Exception {
                return daoThuoc.getAllThuocKhongKeDonKhoBanHang();
            }
        };
        task.setOnSucceeded(e -> {
            masterDataLe.setAll(task.getValue());
        });
        new Thread(task).start();
    }

    private void setupSearchLogic() {
        FilteredList<Thuoc> filteredData = new FilteredList<>(masterDataLe, p -> true);
        txtTimKiemThuocLe.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(thuoc -> {
                if (newValue == null || newValue.isEmpty()) {
					return true;
				}
                String lowerCaseFilter = newValue.toLowerCase();
                if (thuoc.getTenThuoc().toLowerCase().contains(lowerCaseFilter) || thuoc.getMaThuoc().toLowerCase().contains(lowerCaseFilter) || (thuoc.getCongDung() != null && thuoc.getCongDung().toLowerCase().contains(lowerCaseFilter))) {
					return true;
				}
                if (thuoc.getTrieuChung() != null && thuoc.getTrieuChung().toLowerCase().contains(lowerCaseFilter)) {
					return true;
				}
                return false;
            });
        });
        SortedList<Thuoc> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tblThuocLe.comparatorProperty());
        tblThuocLe.setItems(sortedData);
    }

    @FXML void handleLamMoiLe(ActionEvent event) {
        txtTimKiemThuocLe.clear();
        loadDataLeAsync();
    }

    @FXML void handleThemVaoGioLe(ActionEvent event) {
        Thuoc selectedThuoc = tblThuocLe.getSelectionModel().getSelectedItem();
        if (selectedThuoc == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Vui lòng chọn thuốc để thêm vào giỏ!");
            return;
        }

        // VĐ3: Tab bán lẻ đã lọc chỉ thuốc không kê đơn, nhưng vẫn double-check
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
            lblTenKhachLe.setText("Khách lẻ (vãng lai)");
            if (btnXoaKH != null) {
				btnXoaKH.setVisible(false);
			}
            return;
        }
        KhachHang kh = daoKhachHang.getBySdt(sdt);
        if (kh != null) {
            currentKhachHang = kh;
            lblTenKhachLe.setText("👤 " + kh.getHoTen() + " (Điểm: " + kh.getDiemTichLuy() + ")");
            if (btnXoaKH != null) {
				btnXoaKH.setVisible(true);
			}
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
                                if (num > max) {
									max = num;
								}
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
                            DAO_NhatKyHoatDong.ghiLog("THEM", "Khách Hàng", result.getMaKhachHang(), "Thêm khách hàng mới từ màn hình bán hàng: " + result.getHoTen());
                            new Alert(Alert.AlertType.INFORMATION, "Thêm khách hàng thành công!").show();
                            lblTenKhachLe.setText("👤 " + result.getHoTen() + " (Điểm: 0)");
                            txtSdtKhachLe.setText(result.getSdt());
                            if (btnXoaKH != null) {
								btnXoaKH.setVisible(true);
							}
                            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm khách hàng thành công! Mã: " + result.getMaKhachHang());
                        } else {
                            showAlert(Alert.AlertType.ERROR, "Thất bại", "Lỗi cơ sở dữ liệu khi lưu khách hàng. Vui lòng thử lại.");
                            currentKhachHang = null;
                            lblTenKhachLe.setText("Khách lẻ (vãng lai)");
                        }
                    } else {
                        // User hủy dialog → không làm gì, giữ Khách lẻ
                        currentKhachHang = null;
                        lblTenKhachLe.setText("Khách lẻ (vãng lai)");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở form thêm khách hàng: " + e.getMessage());
                }
            } else {
                currentKhachHang = null;
                lblTenKhachLe.setText("Không tìm thấy: " + sdt);
            }
        }
    }

    // VĐ4: Xóa khách hàng đã chọn
    @FXML void onXoaKhachHang(ActionEvent event) {
        currentKhachHang = null;
        txtSdtKhachLe.clear();
        lblTenKhachLe.setText("Khách lẻ (vãng lai)");
        if (btnXoaKH != null) {
			btnXoaKH.setVisible(false);
		}
    }

    @FXML void handleHuyGioLe(ActionEvent event) {
        cartDataLe.clear();
        tinhTongTienLe();
        // VĐ3: CHỈ xóa giỏ, KHÔNG reset khách hàng
    }

    @FXML void handleThanhToanLe(ActionEvent event) {
        if (cartDataLe.isEmpty()) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Giỏ hàng trống!");
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION, "Xác nhận thanh toán hóa đơn với tổng số tiền: " + String.format("%,.0f ₫", tongTienLe + thueVatLe) + " ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() != ButtonType.YES) {
			return;
		}

        // Kiem tra thuoc ke don
        for (CartItem item : cartDataLe) {
            if (item.thuoc.isCanKeDon()) {
                showAlert(AlertType.ERROR, "Lỗi Kê Đơn", "Đơn hàng có thuốc kê đơn (" + item.thuoc.getTenThuoc() + "). Vui lòng chuyển sang Tab 'Bán Theo Đơn Thuốc'.");
                return;
            }
        }

        NhanVien user = GUI_TrangChuController.getNhanVienDangNhap();
        if (user == null) {
            user = new NhanVien("NV001", "testuser", "123", "Nhân Viên Test", "Nhân viên", "Ca 1", "0123",1);
        }

        // Tao Hoa don
        String maHD = daoHoaDon.generateMaHoaDon();
        String pMethod = cbHinhThucThanhToanLe.getValue() != null ? cbHinhThucThanhToanLe.getValue().toString() : "";
        String dbHinhThuc = pMethod.equals("Chuyển khoản") ? "CHUYEN_KHOAN" :
                            pMethod.equals("Thẻ tín dụng") ? "THE" : "TIEN_MAT";

        // --- FIX thueVAT: DB lưu 8.0 = 8%, KHÔNG phải 0.08 ---
        HoaDon hd = new HoaDon(maHD, Timestamp.valueOf(LocalDateTime.now()), 8.0,
                dbHinhThuc, "", user, currentKhachHang);
        hd.setLoaiBan("BAN_LE"); // Tường minh set BAN_LE

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

        boolean ok = daoHoaDon.thanhToan(hd, dsCT);
        if (ok) {
            DAO_NhatKyHoatDong.ghiLog("TAO_HOA_DON", "Hóa Đơn", hd.getMaHoaDon(), "Tạo hóa đơn bán lẻ: " + hd.getMaHoaDon());
            // Tích điểm
            if (currentKhachHang != null) {
                int diemCong = (int) ((tongTienLe + thueVatLe) / 1000);
                daoKhachHang.capNhatDiemTichLuy(currentKhachHang.getMaKhachHang(), currentKhachHang.getDiemTichLuy() + diemCong);
            }
            // ── Hỏi in hóa đơn sau khi thanh toán thành công ──
            Alert confirmPrint = new Alert(AlertType.CONFIRMATION);
            confirmPrint.setTitle("Thanh toán thành công");
            confirmPrint.setHeaderText("✅ Hóa đơn " + maHD + " đã được lưu!");
            confirmPrint.setContentText("Bạn có muốn in hóa đơn ngay bây giờ không?");

            ButtonType btnIn = new ButtonType("🖨 In hóa đơn");
            ButtonType btnBo = new ButtonType("Bỏ qua", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirmPrint.getButtonTypes().setAll(btnIn, btnBo);

            Optional<ButtonType> resultPrint = confirmPrint.showAndWait();
            if (resultPrint.isPresent() && resultPrint.get() == btnIn) {
                try {
                    List<Object[]> chiTiet = daoHoaDon.getChiTietByMaHoaDon(maHD);

                    HoaDonView hdView = daoHoaDon.getHoaDonViewByMa(maHD);
                    if (hdView == null) {
                        throw new Exception("Lỗi: không reload được hóa đơn từ DB để in PDF.");
                    }

                    String filePdf = HoaDonPdfExporter.xuatPDF(hdView, chiTiet);
                    Alert a = new Alert(Alert.AlertType.INFORMATION,
                        "Xuất hóa đơn thành công!\nFile: " + filePdf, ButtonType.OK);
                    a.setTitle("In Hóa Đơn");
                    a.setHeaderText(null);
                    a.showAndWait();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert(AlertType.ERROR, "Lỗi in PDF", "Lỗi khi xuất PDF: " + ex.getMessage());
                }
            }

            handleHuyGioLe(null);
            onXoaKhachHang(null);
            // Reload lại danh sách thuốc (tồn kho đã thay đổi)
            loadDataLeAsync();
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

    // ═══════════════════════════════════════════════════════════════════
    // TAB 2: BÁN THEO ĐƠN — Bảng thuốc + Card đơn thuốc + Giỏ hàng
    // ═══════════════════════════════════════════════════════════════════

    // Bảng thuốc tab đơn
    @FXML private TextField txtTimKiemThuocDon;
    @FXML private TableView<Thuoc> tblThuocDon;
    @FXML private TableColumn<Thuoc, String> colThuocDonMa;
    @FXML private TableColumn<Thuoc, String> colThuocDonAnh;
    @FXML private TableColumn<Thuoc, String> colThuocDonTen;
    @FXML private TableColumn<Thuoc, String> colThuocDonTrieuChung;
    @FXML private TableColumn<Thuoc, Boolean> colThuocDonKeDon;
    @FXML private Button btnThemVaoGioDon;

    // Card đơn thuốc
    @FXML private Label lblTrangThaiDon;
    @FXML private Button btnThemDon;
    @FXML private Button btnXemSuaDon;
    @FXML private Button btnDatLaiDon;

    // Pane chứa nội dung đơn thuốc (visible/managed control)
    @FXML private javafx.scene.layout.VBox paneThongTinDon;

    // Giỏ hàng + thanh toán tab đơn
    @FXML private TableView<CartItem> tblGioHangDon;
    @FXML private TableColumn<CartItem, String> colCartDonTenThuoc, colCartDonDonVi;
    @FXML private TableColumn<CartItem, Integer> colCartDonSoLuong;
    @FXML private TableColumn<CartItem, String> colCartDonDonGia, colCartDonThanhTien;
    @FXML private TableColumn<CartItem, Void> colCartDonXoa;
    @FXML private ComboBox<String> cbHinhThucThanhToanDon;
    @FXML private Label lblTongTienDon, lblVatDon, lblThanhTienDon;
    @FXML private Button btnThanhToanDon;

    private ObservableList<Thuoc> masterDataDon = FXCollections.observableArrayList();
    private ObservableList<CartItem> cartDataDon = FXCollections.observableArrayList();
    private double tongTienDon = 0;
    private double thueVatDon = 0;
    private final DAO_DonThuoc daoDonThuoc = new DAO_DonThuoc();

    // Object tạm cho đơn thuốc — chỉ INSERT vào DB khi thanh toán
    private DonThuoc donThuocTemp = null;

    // --- BẢNG THUỐC TAB ĐƠN ---

    private void setupTableThuocDon() {
        colThuocDonMa.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("maThuoc"));
        colThuocDonMa.setCellFactory(col -> {
            TableCell<Thuoc, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item);
                }
            };
            cell.setAlignment(javafx.geometry.Pos.CENTER);
            return cell;
        });

        // Ảnh thuốc (giống tab lẻ)
        colThuocDonAnh.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("hinhAnh"));
        colThuocDonAnh.setCellFactory(column -> new TableCell<Thuoc, String>() {
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

        colThuocDonTen.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("tenThuoc"));
        colThuocDonTen.setCellFactory(col -> {
            TableCell<Thuoc, String> cell = new TableCell<>() {
                @Override
                protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    setText(empty || item == null ? null : item);
                }
            };
            cell.setAlignment(javafx.geometry.Pos.CENTER);
            return cell;
        });
        colThuocDonTrieuChung.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("trieuChung"));

        // Cột kê đơn — highlight thuốc kê đơn
        colThuocDonKeDon.setCellValueFactory(new javafx.scene.control.cell.PropertyValueFactory<>("canKeDon"));
        colThuocDonKeDon.setCellFactory(column -> new TableCell<Thuoc, Boolean>() {
            {
                setAlignment(javafx.geometry.Pos.CENTER);
            }
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "Kê đơn" : "Không");
                    setStyle(item ? "-fx-text-fill: #1565C0; -fx-font-weight: bold;" : "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
                }
            }
        });

        // VĐ2: Row factory — highlight dòng thuốc kê đơn bằng nền #E3F2FD nhưng tôn trọng style khi selected
        tblThuocDon.setRowFactory(tv -> {
            javafx.scene.control.TableRow<Thuoc> row = new javafx.scene.control.TableRow<>();

            row.itemProperty().addListener((obs, oldItem, newItem) -> {
                if (newItem == null) {
                    row.setStyle("");
                } else if (newItem.isCanKeDon() && !row.isSelected()) {
                    row.setStyle("-fx-background-color: #E3F2FD;");
                } else if (!row.isSelected()) {
                    row.setStyle("");
                }
            });

            row.selectedProperty().addListener((obs, wasSelected, isNowSelected) -> {
                if (isNowSelected) {
                    row.setStyle("");
                } else {
                    Thuoc item = row.getItem();
                    if (item != null && item.isCanKeDon()) {
                        row.setStyle("-fx-background-color: #E3F2FD;");
                    } else {
                        row.setStyle("");
                    }
                }
            });
            return row;
        });

        // Double-click → thêm vào giỏ
        tblThuocDon.setOnMouseClicked(event -> {
            if (event.getClickCount() == 2 && tblThuocDon.getSelectionModel().getSelectedItem() != null) {
                handleThemVaoGioDon(null);
            }
        });
    }

    // VĐ2: Load TẤT CẢ thuốc DANG_BAN ở KHO_BAN_HANG (không lọc canKeDon)
    private void loadDataDonAsync() {
        Task<List<Thuoc>> task = new Task<>() {
            @Override
            protected List<Thuoc> call() throws Exception {
                return daoThuoc.getAllThuocCoLoKhoBanHang();
            }
        };
        task.setOnSucceeded(e -> {
            masterDataDon.setAll(task.getValue());
        });
        new Thread(task).start();
    }

    private void setupSearchLogicDon() {
        FilteredList<Thuoc> filteredData = new FilteredList<>(masterDataDon, p -> true);
        txtTimKiemThuocDon.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(thuoc -> {
                if (newValue == null || newValue.isEmpty()) {
					return true;
				}
                String lowerCaseFilter = newValue.toLowerCase();
                if (thuoc.getTenThuoc().toLowerCase().contains(lowerCaseFilter) || thuoc.getMaThuoc().toLowerCase().contains(lowerCaseFilter) || (thuoc.getHoatChat() != null && thuoc.getHoatChat().toLowerCase().contains(lowerCaseFilter))) {
					return true;
				}
                if (thuoc.getCongDung() != null && thuoc.getCongDung().toLowerCase().contains(lowerCaseFilter)) {
					return true;
				}
                if (thuoc.getTrieuChung() != null && thuoc.getTrieuChung().toLowerCase().contains(lowerCaseFilter)) {
					return true;
				}
                return false;
            });
        });
        SortedList<Thuoc> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tblThuocDon.comparatorProperty());
        tblThuocDon.setItems(sortedData);
    }

    @FXML void handleLamMoiDon(ActionEvent event) {
        txtTimKiemThuocDon.clear();
        loadDataDonAsync();
    }

    // Thêm thuốc từ bảng vào giỏ hàng đơn (giống tab lẻ, nhưng KHÔNG chặn kê đơn)
    @FXML void handleThemVaoGioDon(ActionEvent event) {
        // VĐ1: Bắt buộc nhập đơn thuốc trước khi thêm sản phẩm
        if (donThuocTemp == null) {
            showAlert(AlertType.WARNING, "Thông báo",
                "Vui lòng nhập thông tin đơn thuốc trước khi thêm sản phẩm.");
            return;
        }

        Thuoc selectedThuoc = tblThuocDon.getSelectionModel().getSelectedItem();
        if (selectedThuoc == null) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Vui lòng chọn thuốc để thêm vào giỏ!");
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
                boolean found = false;
                for (CartItem item : cartDataDon) {
                    if (item.thuoc.getMaThuoc().equals(selectedThuoc.getMaThuoc())
                            && item.donVi.getMaQuyDoi().equals(dv.getMaQuyDoi())) {
                        item.soLuong   += sl;
                        item.thanhTien  = item.soLuong * item.donGia;
                        found = true;
                        break;
                    }
                }
                if (!found) {
                    CartItem ci = new CartItem(selectedThuoc, dv, sl, donGia, maBangGia);
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
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // --- CARD ĐƠN THUỐC: 3 NÚT ---

    @FXML void onThemDonThuoc(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_ThemDonThuoc.fxml"));
            Scene scene = new Scene(loader.load());
            gui.dialogs.Dialog_ThemDonThuocController controller = loader.getController();

            // Sinh mã đơn thuốc tạm
            int maxMaDT = daoDonThuoc.getMaxMaDonThuoc();
            String nextMa = String.format("DT%04d", maxMaDT + 1);
            controller.setDonThuoc(null, nextMa);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Thêm Thông Tin Đơn Thuốc");
            stage.setScene(scene);
            stage.showAndWait();

            DonThuoc result = controller.getResultDonThuoc();
            if (result != null) {
                donThuocTemp = result;
                updateCardDonThuoc();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Lỗi", "Không thể mở dialog đơn thuốc: " + e.getMessage());
        }
    }

    @FXML void onXemSuaDonThuoc(ActionEvent event) {
        if (donThuocTemp == null) {
			return;
		}
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_ThemDonThuoc.fxml"));
            Scene scene = new Scene(loader.load());
            gui.dialogs.Dialog_ThemDonThuocController controller = loader.getController();

            // Điền sẵn dữ liệu đã nhập
            controller.setDonThuoc(donThuocTemp, donThuocTemp.getMaDonThuoc());

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Xem / Sửa Thông Tin Đơn Thuốc");
            stage.setScene(scene);
            stage.showAndWait();

            DonThuoc result = controller.getResultDonThuoc();
            if (result != null) {
                donThuocTemp = result;
                updateCardDonThuoc();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(AlertType.ERROR, "Lỗi", "Không thể mở dialog đơn thuốc: " + e.getMessage());
        }
    }

    @FXML void onDatLaiDonThuoc(ActionEvent event) {
        if (cartDataDon != null && !cartDataDon.isEmpty()) {
            Alert confirm = new Alert(AlertType.CONFIRMATION,
                "Đặt lại thông tin đơn thuốc sẽ xóa toàn bộ thuốc trong giỏ hàng. Bạn có đồng ý không?",
                ButtonType.YES, ButtonType.NO);
            confirm.setTitle("Xác nhận");
            if (confirm.showAndWait().orElse(ButtonType.NO) != ButtonType.YES) {
                return;
            }
            cartDataDon.clear();
            tinhTongTienDon();
        }

        donThuocTemp = null;
        resetCardDonThuoc();
    }

    /** Cập nhật card đơn thuốc sau khi thêm/sửa */
    private void updateCardDonThuoc() {
        if (donThuocTemp == null) {
            resetCardDonThuoc();
            return;
        }
        // VĐ1: Hiển thị nhiều dòng KHÔNG có emoji
        String bacSi = donThuocTemp.getTenBacSi();
        String benhNhan = donThuocTemp.getThongTinBenhNhan();
        String chanDoan = donThuocTemp.getChanDoan();

        List<String> list = new ArrayList<>();
        if (bacSi != null && !bacSi.isBlank()) {
			list.add("BS: " + bacSi);
		}
        if (benhNhan != null && !benhNhan.isBlank()) {
			list.add("BN: " + benhNhan);
		}
        if (chanDoan != null && !chanDoan.isBlank()) {
			list.add("Chẩn đoán: " + chanDoan);
		}

        lblTrangThaiDon.setText(String.join(" | ", list));
        // Hiện paneThongTinDon và cho nó chiếm không gian
        if (paneThongTinDon != null) {
            paneThongTinDon.setVisible(true);
            paneThongTinDon.setManaged(true);
        }
        lblTrangThaiDon.setStyle("-fx-text-fill: #1565C0; -fx-font-weight: bold; -fx-font-size: 11px; -fx-line-spacing: 2;");
        btnThemDon.setVisible(false);
        btnXemSuaDon.setVisible(true);
        btnDatLaiDon.setVisible(true);
        // VĐ1: Enable nút "Thêm vào giỏ" khi đã có đơn
        if (btnThemVaoGioDon != null) {
			btnThemVaoGioDon.setDisable(false);
		}
    }

    /** Reset card đơn thuốc về trạng thái "Chưa có đơn" */
    private void resetCardDonThuoc() {
        donThuocTemp = null;
        lblTrangThaiDon.setText("");
        // Ẩn paneThongTinDon và KHÔNG chiếm không gian
        if (paneThongTinDon != null) {
            paneThongTinDon.setVisible(false);
            paneThongTinDon.setManaged(false);
        }
        btnThemDon.setVisible(true);
        btnXemSuaDon.setVisible(false);
        btnDatLaiDon.setVisible(false);
        // VĐ1: Disable nút "Thêm vào giỏ" khi không có đơn
        if (btnThemVaoGioDon != null) {
			btnThemVaoGioDon.setDisable(true);
		}
    }

    // --- GIỎ HÀNG + THANH TOÁN TAB ĐƠN ---

    private void setupTableCartDon() {
        colCartDonTenThuoc.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().thuoc.getTenThuoc()));
        colCartDonDonVi.setCellValueFactory(cellData -> new SimpleStringProperty(cellData.getValue().donVi.getTenDonVi()));
        colCartDonSoLuong.setCellValueFactory(cd -> new SimpleIntegerProperty(cd.getValue().soLuong).asObject());
        colCartDonDonGia.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%,.0f", cellData.getValue().donGia)));
        colCartDonThanhTien.setCellValueFactory(cellData -> new SimpleStringProperty(String.format("%,.0f", cellData.getValue().thanhTien)));

        tblGioHangDon.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        colCartDonXoa.setCellFactory(param -> new TableCell<>() {
            private final Button btnXoa = new Button("✕");
            {
                btnXoa.getStyleClass().add("bh-btn-remove");
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

    @FXML void handleHuyGioDon(ActionEvent event) {
        // VĐ3: CHỈ xóa giỏ hàng, KHÔNG reset đơn thuốc
        cartDataDon.clear();
        tinhTongTienDon();
    }

    // Thanh toán theo đơn — INSERT DonThuoc nếu donThuocTemp != null
    @FXML void handleThanhToanDon(ActionEvent event) {
        if (donThuocTemp == null) {
            showAlert(AlertType.ERROR, "Lỗi", "Chưa có thông tin đơn thuốc. Vui lòng nhập thông tin đơn trước khi thanh toán.");
            return;
        }

        if (cartDataDon.isEmpty()) {
            showAlert(AlertType.WARNING, "Cảnh báo", "Giỏ hàng theo đơn hiện đang rỗng!");
            return;
        }

        Alert confirm = new Alert(AlertType.CONFIRMATION, "Xác nhận thanh toán hóa đơn với tổng số tiền: " + String.format("%,.0f ₫", tongTienDon + thueVatDon) + " ?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() != ButtonType.YES) {
			return;
		}

        NhanVien user = GUI_TrangChuController.getNhanVienDangNhap();
        if (user == null) {
            user = new NhanVien("NV001", "testuser", "123", "Nhân Viên Test", "Nhân viên", "Ca 1", "0123",1);
        }

        String maHD = daoHoaDon.generateMaHoaDon();
        String pMethod = cbHinhThucThanhToanDon.getValue() != null ? cbHinhThucThanhToanDon.getValue().toString() : "";
        String dbHinhThuc = pMethod.equals("Chuyển khoản") ? "CHUYEN_KHOAN" :
                            pMethod.equals("Thẻ tín dụng") ? "THE" : "TIEN_MAT";

        // --- FIX thueVAT: DB lưu 8.0 = 8%, KHÔNG phải 0.08 ---
        HoaDon hd = new HoaDon(maHD, Timestamp.valueOf(LocalDateTime.now()), 8.0, dbHinhThuc, "Bán hóa đơn theo đơn thuốc", user, currentKhachHang);
        hd.setLoaiBan("BAN_THEO_DON"); // Tường minh set BAN_THEO_DON

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
            DAO_NhatKyHoatDong.ghiLog("TAO_HOA_DON", "Hóa Đơn", hd.getMaHoaDon(), "Tạo hóa đơn bán theo đơn: " + hd.getMaHoaDon());
            // INSERT DonThuoc nếu donThuocTemp != null
            if (donThuocTemp != null) {
                donThuocTemp.setMaHoaDon(maHD);
                daoDonThuoc.themDonThuoc(donThuocTemp);
            }

            if (currentKhachHang != null) {
                int diemCong = (int) ((tongTienDon + thueVatDon) / 1000);
                daoKhachHang.capNhatDiemTichLuy(currentKhachHang.getMaKhachHang(), currentKhachHang.getDiemTichLuy() + diemCong);
            }
            // ── Hỏi in hóa đơn sau thanh toán đơn thuốc ──
            Alert confirmPrint = new Alert(AlertType.CONFIRMATION);
            confirmPrint.setTitle("Thanh toán thành công");
            confirmPrint.setHeaderText("✅ Hóa đơn " + maHD + " đã được lưu!");
            confirmPrint.setContentText("Bạn có muốn in hóa đơn ngay bây giờ không?");

            ButtonType btnIn = new ButtonType("🖨 In hóa đơn");
            ButtonType btnBo = new ButtonType("Bỏ qua", ButtonBar.ButtonData.CANCEL_CLOSE);
            confirmPrint.getButtonTypes().setAll(btnIn, btnBo);

            Optional<ButtonType> resultPrint = confirmPrint.showAndWait();
            if (resultPrint.isPresent() && resultPrint.get() == btnIn) {
                try {
                    List<Object[]> chiTiet = daoHoaDon.getChiTietByMaHoaDon(maHD);

                    HoaDonView hdView = daoHoaDon.getHoaDonViewByMa(maHD);
                    if (hdView == null) {
                        throw new Exception("Lỗi: không reload được hóa đơn từ DB để in PDF.");
                    }

                    String filePdf = HoaDonPdfExporter.xuatPDF(hdView, chiTiet);
                    Alert a = new Alert(Alert.AlertType.INFORMATION,
                        "Xuất hóa đơn thành công!\nFile: " + filePdf, ButtonType.OK);
                    a.setTitle("In Hóa Đơn");
                    a.setHeaderText(null);
                    a.showAndWait();
                } catch (Exception ex) {
                    ex.printStackTrace();
                    showAlert(AlertType.ERROR, "Lỗi in PDF", "Lỗi khi xuất PDF: " + ex.getMessage());
                }
            }

            // Reset toàn bộ giỏ hàng + card đơn thuốc
            cartDataDon.clear();
            tinhTongTienDon();
            onXoaKhachHang(null);
            resetCardDonThuoc();

            // Reload lại danh sách thuốc (tồn kho đã thay đổi)
            loadDataDonAsync();
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

        // Tab đơn - Ô tìm kiếm thuốc: ENTER → focus xuống TableView
        txtTimKiemThuocDon.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER) {
                tblThuocDon.requestFocus();
                if (!tblThuocDon.getItems().isEmpty()) {
                    tblThuocDon.getSelectionModel().selectFirst();
                }
                event.consume();
            }
        });

        // Tab đơn - TableView thuốc: ENTER hoặc Space → thêm vào giỏ
        tblThuocDon.setOnKeyPressed(event -> {
            if (event.getCode() == javafx.scene.input.KeyCode.ENTER ||
                event.getCode() == javafx.scene.input.KeyCode.SPACE) {
                if (!tblThuocDon.getSelectionModel().isEmpty()) {
                    handleThemVaoGioDon(null);
                    event.consume();
                }
            }
        });
    }
 // =======================================================
    // 🚨 HÀM XỬ LÝ NGHIỆP VỤ TÁI LẬP ĐƠN THUỐC (FULL LOGIC)
    // =======================================================
    public void xuLyTaiLapDonThuoc(DonThuoc dtCu) {
        javafx.application.Platform.runLater(() -> {
            onTabDonThuoc(); // Chuyển sang tab Đơn thuốc

            // 1. Gán lại thông tin thẻ Đơn Thuốc
            this.donThuocTemp = new DonThuoc();
            this.donThuocTemp.setTenBacSi(dtCu.getTenBacSi());
            this.donThuocTemp.setThongTinBenhNhan(dtCu.getThongTinBenhNhan());
            this.donThuocTemp.setChanDoan(dtCu.getChanDoan());
            this.donThuocTemp.setHinhAnhDon(dtCu.getHinhAnhDon());


            if (hinhAnhTaiLapPending != null) {

                this.donThuocTemp.setHinhAnhDon(dtCu.getHinhAnhDon());


            }

            try {
                int maxMaDT = daoDonThuoc.getMaxMaDonThuoc();
                this.donThuocTemp.setMaDonThuoc(String.format("DT%04d", maxMaDT + 1));
            } catch(Exception ex) {
                this.donThuocTemp.setMaDonThuoc("DT_NEW");
            }
            updateCardDonThuoc();

            // 2. Xóa sạch giỏ hàng cũ trước khi nạp đồ mới
            cartDataDon.clear();

            // 3. Nạp thuốc vào giỏ (Tự động tìm lô FEFO mới nhất)
            try {
                List<Object[]> listCT = daoHoaDon.getChiTietRebuildCart(dtCu.getMaHoaDon());
                for (Object[] row : listCT) {
                    String maThuoc = (String) row[0];
                    String maQuyDoi = (String) row[1];
                    int soLuong = (int) row[2];
                    double donGia = (double) row[3];
                    String maBangGia = (String) row[4];
                    String tenDonVi = (String) row[5];

                    Thuoc thuoc = daoThuoc.getThuocByMa(maThuoc);
                    if (thuoc != null) {
                        DonViQuyDoi dv = new DonViQuyDoi();
                        dv.setMaQuyDoi(maQuyDoi);
                        dv.setMaThuoc(maThuoc);
                        dv.setTenDonVi(tenDonVi);

                        CartItem item = new CartItem(thuoc, dv, soLuong, donGia, maBangGia);

                        // Cực quan trọng: Chọn lại lô FEFO hiện tại, bỏ qua lô của đơn cũ
                        String maLo = daoLoThuoc.getLoFEFO(maThuoc);
                        if (maLo != null) {
                            item.maLoThuoc = maLo;
                            daoLoThuoc.getLoThuocBanDuocByMaThuoc(maThuoc).stream()
                                .filter(l -> l.getMaLoThuoc().equals(maLo)).findFirst()
                                .ifPresent(l -> item.hanSuDung = l.getHanSuDung());
                        }
                        cartDataDon.add(item);
                    }
                }
                tblGioHangDon.refresh();
                tinhTongTienDon();
                showAlert(AlertType.INFORMATION, "Tái lập thành công", "Đã nạp toàn bộ toa thuốc cũ vào giỏ hàng. Bạn có thể thay đổi số lượng hoặc thanh toán ngay!");
            } catch (Exception e) {
                e.printStackTrace();
                showAlert(AlertType.WARNING, "Cảnh báo", "Đã tái lập thông tin đơn, nhưng gặp lỗi khi lấy thuốc vào giỏ!");
            }
        });
    }
}