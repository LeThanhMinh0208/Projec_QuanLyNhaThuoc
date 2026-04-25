package gui.main;

import gui.main.GUI_QuanLyBangGiaController;
import dao.DAO_Thuoc;
import entity.NhanVien;
import entity.Thuoc;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

import java.io.InputStream;

public class GUI_TrangChuController {

    @FXML
    private TableView<Thuoc> tableThuoc;
    @FXML
    private TableColumn<Thuoc, String> colMaThuoc, colHinhAnh, colTenThuoc, colTrieuChung, colDVT, colTrangThai;
    @FXML
    private TableColumn<Thuoc, Boolean> colKeDon;
    @FXML
    private TextField txtTimKiem;
    @FXML
    private BorderPane mainBorderPane;

    private final DAO_Thuoc daoThuoc = new DAO_Thuoc();
    private final ObservableList<Thuoc> masterData = FXCollections.observableArrayList();
    private static NhanVien nhanVienDangNhap;
    private Node noiDungTrangChuGoc;

    // --- BIẾN STATIC ĐỂ CÁC TRANG CON GỌI ---
    private static GUI_TrangChuController instance;
    public static GUI_TrangChuController getInstance() { return instance; }

    public static void setNhanVienDangNhap(NhanVien nv) { nhanVienDangNhap = nv; }
    public static NhanVien getNhanVienDangNhap() { return nhanVienDangNhap; }

    @FXML
    public void initialize() {
        instance = this; // Lấy instance
        setupTable();
        loadDataFromServer();
        setupSearchLogic();
        utils.SceneUtils.init(mainBorderPane);
        if (mainBorderPane != null) {
            noiDungTrangChuGoc = mainBorderPane.getCenter();
        }
    }

    // =========================================================================
    // HÀM BÁ BẠO: CHUYỂN TRANG VÀ TÌM NÚT BẰNG TEXT (KHÔNG CẦN FX:ID)
    // =========================================================================
    public void chuyenTrangVaHighlight(String fxmlPath, String buttonTextToMatch) {
        // 1. Chuyển trang
        switchPage(fxmlPath);

        // 2. Dùng Platform.runLater để đảm bảo giao diện đã load xong
        javafx.application.Platform.runLater(() -> {
            if (mainBorderPane.getScene() != null) {
                Parent root = mainBorderPane.getScene().getRoot();
                
                // Tắt tất cả màu xanh của nút cũ
                root.lookupAll(".sub-btn").forEach(n -> n.getStyleClass().remove("sub-btn-active"));
                root.lookupAll(".btn-home-special").forEach(n -> n.getStyleClass().remove("btn-home-special-active"));

                // Quét tìm nút có chữ trùng khớp (VD: "Nhập Kho") và bật sáng
                for (Node node : root.lookupAll(".sub-btn")) {
                    if (node instanceof Button) {
                        Button btn = (Button) node;
                        if (btn.getText() != null && btn.getText().toLowerCase().contains(buttonTextToMatch.toLowerCase())) {
                            btn.getStyleClass().add("sub-btn-active");
                            return;
                        }
                    }
                }
                
                // Quét nốt các nút dạng special
                for (Node node : root.lookupAll(".btn-home-special")) {
                    if (node instanceof Button) {
                        Button btn = (Button) node;
                        if (btn.getText() != null && btn.getText().toLowerCase().contains(buttonTextToMatch.toLowerCase())) {
                            btn.getStyleClass().add("btn-home-special-active");
                            return;
                        }
                    }
                }
            }
        });
    }

    private void setupTable() {
        colMaThuoc.setCellValueFactory(new PropertyValueFactory<>("maThuoc"));
        colTenThuoc.setCellValueFactory(new PropertyValueFactory<>("tenThuoc"));
        colTrieuChung.setCellValueFactory(new PropertyValueFactory<>("trieuChung"));
        colDVT.setCellValueFactory(new PropertyValueFactory<>("donViCoBan"));

        colKeDon.setCellValueFactory(new PropertyValueFactory<>("canKeDon"));
        colKeDon.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("text-do", "text-xanh-la");
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Có" : "Không");
                    getStyleClass().add(item ? "text-do" : "text-xanh-la");
                }
            }
        });

        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        colTrangThai.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("text-xanh-bien", "text-vang-cam", "text-do");
                if (empty || item == null) {
                    setText(null);
                } else {
                    if ("DANG_BAN".equals(item)) { setText("Đang Bán"); getStyleClass().add("text-xanh-bien"); } 
                    else if ("HET_HANG".equals(item)) { setText("Hết Hàng"); getStyleClass().add("text-vang-cam"); } 
                    else if ("NGUNG_BAN".equals(item)) { setText("Ngừng Bán"); getStyleClass().add("text-do"); } 
                    else { setText(item); }
                }
            }
        });

        colHinhAnh.setCellValueFactory(new PropertyValueFactory<>("hinhAnh"));
        colHinhAnh.setCellFactory(column -> new TableCell<>() {
            private final ImageView iv = new ImageView();
            @Override protected void updateItem(String file, boolean empty) {
                super.updateItem(file, empty);
                if (empty || file == null || file.trim().isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        InputStream is = getClass().getResourceAsStream("/resources/images/images_thuoc/" + file.trim());
                        if (is != null) {
                            iv.setImage(new Image(is)); iv.setFitWidth(80); iv.setFitHeight(60);
                            iv.setPreserveRatio(true); setGraphic(iv); setAlignment(Pos.CENTER);
                        } else { setGraphic(new Label("No image")); }
                    } catch (Exception e) { setGraphic(new Label("Error")); }
                }
            }
        });

        tableThuoc.setRowFactory(tv -> {
            TableRow<Thuoc> row = new TableRow<>();
            row.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
                if (event.getClickCount() == 1 && !row.isEmpty() && row.isSelected()) {
                    tv.getSelectionModel().clearSelection();
                    tv.getFocusModel().focus(-1);
                    tableThuoc.getParent().requestFocus();
                    event.consume();
                }
            });
            return row;
        });
    }

    private void loadDataFromServer() { masterData.setAll(daoThuoc.getAllThuoc()); }

    private void setupSearchLogic() {
        FilteredList<Thuoc> filteredData = new FilteredList<>(masterData, p -> true);
        txtTimKiem.textProperty().addListener((obs, old, newValue) -> {
            filteredData.setPredicate(thuoc -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String filter = newValue.toLowerCase();
                if (thuoc.getTrieuChung() != null && thuoc.getTrieuChung().toLowerCase().contains(filter)) return true;
                if (thuoc.getMaThuoc().toLowerCase().contains(filter)) return true;
                if (thuoc.getTenThuoc().toLowerCase().contains(filter)) return true;
                if (thuoc.getCongDung() != null && thuoc.getCongDung().toLowerCase().contains(filter)) return true;
                if (thuoc.getHoatChat() != null && thuoc.getHoatChat().toLowerCase().contains(filter)) return true;
                if (thuoc.getHangSanXuat() != null && thuoc.getHangSanXuat().toLowerCase().contains(filter)) return true;
                if (thuoc.getNuocSanXuat() != null && thuoc.getNuocSanXuat().toLowerCase().contains(filter)) return true;
                String keDonString = thuoc.isCanKeDon() ? "co ke don" : "khong ke don";
                return keDonString.contains(filter);
            });
        });
        SortedList<Thuoc> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableThuoc.comparatorProperty());
        tableThuoc.setItems(sortedData);
    }

    private void setMenuButtonActive(ActionEvent event) {
        if (event == null || !(event.getSource() instanceof Button)) return;
        Button clickedButton = (Button) event.getSource();
        if (mainBorderPane.getScene() != null) {
            mainBorderPane.getScene().getRoot().lookupAll(".sub-btn").forEach(n -> n.getStyleClass().remove("sub-btn-active"));
            mainBorderPane.getScene().getRoot().lookupAll(".btn-home-special").forEach(n -> n.getStyleClass().remove("btn-home-special-active"));
        }
        if (clickedButton.getStyleClass().contains("btn-home-special")) {
            clickedButton.getStyleClass().add("btn-home-special-active");
        } else {
            clickedButton.getStyleClass().add("sub-btn-active");
        }
    }

    @FXML
    void handleVeTrangChu(ActionEvent event) {
        loadDataTrangChu();
        if (noiDungTrangChuGoc != null) mainBorderPane.setCenter(noiDungTrangChuGoc);
        setMenuButtonActive(event);
    }

    @FXML void handleDangXuat(ActionEvent event) {
        try {
            nhanVienDangNhap = null;
            ((Stage) ((Node) event.getSource()).getScene().getWindow()).close();
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(FXMLLoader.load(getClass().getResource("GUI_DangNhap.fxml"))));
            loginStage.setTitle("Dang nhap");
            loginStage.show();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML void handleMoQuanLyDanhMucThuoc(ActionEvent event) { setMenuButtonActive(event); utils.SceneUtils.switchPage("/gui/main/GUI_DanhMucThuoc.fxml"); }
    @FXML void handleMoQuanLyDonThuoc(ActionEvent event) { setMenuButtonActive(event); utils.SceneUtils.switchPage("/gui/main/GUI_DanhMucDonThuoc.fxml"); }
    @FXML void handleMoQuanLyDonViQuyDoi(ActionEvent event) { setMenuButtonActive(event); utils.SceneUtils.switchPage("/gui/main/GUI_DonViQuyDoi.fxml"); }
    @FXML void handleMoQuanLyBanHangLapHoaDon(ActionEvent event) { setMenuButtonActive(event); switchPage("/gui/main/GUI_QuanLyBanHang.fxml"); }
    @FXML void handleMoQuanLyKhachHang(ActionEvent event) { setMenuButtonActive(event); switchPage("/gui/main/GUI_QuanLyKhachHang.fxml"); }
    @FXML void moTrangDanhMucKho(ActionEvent event) { setMenuButtonActive(event); utils.SceneUtils.switchPage("/gui/main/GUI_DanhMucKho.fxml"); }
    @FXML void moTrangNhapKho(ActionEvent event) { setMenuButtonActive(event); utils.SceneUtils.switchPage("/gui/main/GUI_NhapKho.fxml"); }
    @FXML void moTrangXuLyDoiTra(ActionEvent event) { setMenuButtonActive(event); utils.SceneUtils.switchPage("/gui/main/GUI_XuLyDoiTra.fxml"); }
    @FXML void handleMoXuLyDoiTra(ActionEvent event) { setMenuButtonActive(event); switchPage("/gui/main/GUI_XuLyDoiTra.fxml"); }
    @FXML void moTrangXuatKho(ActionEvent event) { setMenuButtonActive(event); utils.SceneUtils.switchPage("/gui/main/GUI_XuatKho.fxml"); }
    @FXML void moQuanLyDonDatHang(ActionEvent event) { setMenuButtonActive(event); utils.SceneUtils.switchPage("/gui/main/GUI_QuanLyDonDatHang.fxml"); }
    @FXML void handleMoQuanLyDanhMucNhaCungCap(ActionEvent event) { setMenuButtonActive(event); utils.SceneUtils.switchPage("/gui/main/GUI_DanhMucNhaCungCap.fxml"); }
    @FXML void handleMoQuanLyCongNo(ActionEvent event) { setMenuButtonActive(event); utils.SceneUtils.switchPage("/gui/main/GUI_QuanLyCongNo.fxml"); }
    @FXML void handleMoQuanLyBangGia(ActionEvent event) { setMenuButtonActive(event); utils.SceneUtils.switchPage("/gui/main/GUI_QuanLyBangGia.fxml"); }
    @FXML void handleMoDanhSachHoaDon(ActionEvent event) { setMenuButtonActive(event); utils.SceneUtils.switchPage("/gui/main/GUI_DanhSachHoaDon.fxml"); }
    @FXML void handleMoQuanLyLoThuoc(ActionEvent event) { setMenuButtonActive(event); utils.SceneUtils.switchPage("/gui/main/GUI_QuanLyLoThuoc.fxml"); }
    @FXML void handleMoQuanLyNguoiDung(ActionEvent event) { setMenuButtonActive(event); utils.SceneUtils.switchPage("/gui/main/GUI_QuanLyNguoiDung.fxml"); }
    @FXML void handleMoLichSuGiaoDich(ActionEvent event) { setMenuButtonActive(event); utils.SceneUtils.switchPage("/gui/main/GUI_LichSuGiaoDich.fxml"); }
    @FXML void handleMoDoiMatKhau(ActionEvent event) { setMenuButtonActive(event); utils.SceneUtils.switchPage("/gui/main/GUI_DoiMatKhau.fxml"); }

    // --- SỬA CARD DASHBOARD TÌM MÀU XANH BẰNG CHỮ ---
    @FXML void handleMoBanThuoc(javafx.scene.input.MouseEvent event) { 
        chuyenTrangVaHighlight("/gui/main/GUI_QuanLyBanHang.fxml", "Lập Hóa Đơn"); 
    }
    @FXML void handleMoXuLyDoiTraCard(javafx.scene.input.MouseEvent event) { 
        chuyenTrangVaHighlight("/gui/main/GUI_XuLyDoiTra.fxml", "Đổi Trả"); 
    }

    @FXML
    void handleMoTaoBangGia(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/main/GUI_QuanLyBangGia.fxml"));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof GUI_QuanLyBangGiaController) {
                ((GUI_QuanLyBangGiaController) controller).handleThemBangGiaMoi();
            }
            mainBorderPane.setCenter(root);
            setMenuButtonActive(event);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void switchPage(String fxmlPath) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlPath));
            Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof GUI_TrangChuController) {
                ((GUI_TrangChuController) controller).loadDataTrangChu();
            } else if (controller instanceof GUI_QuanLyBanHangController) {
                ((GUI_QuanLyBanHangController) controller).chonTabBanLe();
            }
            mainBorderPane.setCenter(root);
        } catch (Exception e) { e.printStackTrace(); }
    }

    public void loadDataTrangChu() { masterData.setAll(daoThuoc.getAllThuoc()); }
}