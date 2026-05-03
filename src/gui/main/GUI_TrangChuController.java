package gui.main;
import java.io.InputStream;

import dao.DAO_NhatKyHoatDong;
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
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TitledPane;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.UserSession;

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

    // === SIDEBAR TitledPane (9 nhóm) ===
    @FXML private TitledPane tpQLBH, tpQLBG, tpQLDT, tpQLT, tpQLK, tpQLKH, tpQLNCC, tpQLND, tpBCTK;

    // === SIDEBAR Button con ===
    @FXML private Button btnLapHoaDon, btnDanhSachHoaDon, btnXuLyDoiTra;
    @FXML private Button btnDanhSachBangGia, btnTaoBangGiaMoi;
    @FXML private Button btnDanhMucDonThuoc;
    @FXML private Button btnDanhMucThuoc, btnDonViQuyDoi, btnLoThuoc;
    @FXML private Button btnDanhMucKho, btnDonDatHang, btnNhapKho, btnXuatKho;
    @FXML private Button btnDanhMucKhachHang, btnLichSuGiaoDich;
    @FXML private Button btnDanhMucNCC, btnCongNo;
    @FXML private Button btnDanhMucNguoiDung, btnPhanQuyen, btnNhatKy;
    @FXML private Button btnDoanhThu, btnHangHoa, btnTonKho;

    // 3 card ở Trang Chủ
    @FXML private javafx.scene.layout.HBox cardBanThuoc, cardDoiTra, cardBaoCao;

    private final DAO_Thuoc daoThuoc = new DAO_Thuoc();
    private final ObservableList<Thuoc> masterData = FXCollections.observableArrayList();
    private static NhanVien nhanVienDangNhap;
    private Node noiDungTrangChuGoc;

    public static void setNhanVienDangNhap(NhanVien nv) {
        nhanVienDangNhap = nv;
    }

    public static NhanVien getNhanVienDangNhap() {
        return nhanVienDangNhap;
    }

    @FXML
    public void initialize() {
        setupTable();
        loadDataFromServer();
        setupSearchLogic();
        utils.SceneUtils.init(mainBorderPane);
        if (mainBorderPane != null) {
            noiDungTrangChuGoc = mainBorderPane.getCenter();
        }
        // Áp dụng phân quyền sidebar
        apDungPhanQuyen();
    }

    /**
     * Ẩn/hiện các mục sidebar dựa trên quyền của nhân viên đang đăng nhập.
     * Trang Chủ + Đổi Mật Khẩu + Đăng Xuất LUÔN HIỆN.
     * Admin (Quản Lý) bỏ qua, hiện hết.
     */
    private void apDungPhanQuyen() {
        UserSession session = UserSession.getInstance();
        if (session.getUser() == null) return;

        // Admin luôn có toàn quyền -> không cần ẩn gì
        if ("Quản Lý".equals(session.getUser().getChucVu())) return;

        // Map: TitledPane -> mã quyền cha
        Object[][] tpMap = {
            {tpQLBH,  "QLBH"},  {tpQLBG,  "QLBG"},  {tpQLDT,  "QLDT"},
            {tpQLT,   "QLT"},   {tpQLK,   "QLK"},   {tpQLKH,  "QLKH"},
            {tpQLNCC, "QLNCC"}, {tpQLND,  "QLND"},  {tpBCTK,  "BCTK"}
        };

        // Map: Button con -> mã quyền con
        Object[][] btnMap = {
            {btnLapHoaDon,       "QLBH.LAP_HOA_DON"},
            {btnDanhSachHoaDon,  "QLBH.DANH_SACH_HOA_DON"},
            {btnXuLyDoiTra,      "QLBH.XU_LY_DOI_TRA"},
            {btnDanhSachBangGia, "QLBG.DANH_SACH_BANG_GIA"},
            {btnTaoBangGiaMoi,   "QLBG.TAO_BANG_GIA_MOI"},
            {btnDanhMucDonThuoc, "QLDT.DANH_MUC_DON_THUOC"},
            {btnDanhMucThuoc,    "QLT.DANH_MUC_THUOC"},
            {btnDonViQuyDoi,     "QLT.DON_VI_QUY_DOI"},
            {btnLoThuoc,         "QLT.LO_THUOC"},
            {btnDanhMucKho,      "QLK.DANH_MUC_KHO"},
            {btnDonDatHang,      "QLK.DON_DAT_HANG"},
            {btnNhapKho,         "QLK.NHAP_KHO"},
            {btnXuatKho,         "QLK.XUAT_KHO"},
            {btnDanhMucKhachHang,"QLKH.DANH_MUC_KHACH_HANG"},
            {btnLichSuGiaoDich,  "QLKH.LICH_SU_GIAO_DICH"},
            {btnDanhMucNCC,      "QLNCC.DANH_MUC_NHA_CUNG_CAP"},
            {btnCongNo,          "QLNCC.CONG_NO"},
            {btnDanhMucNguoiDung,"QLND.DANH_MUC_NGUOI_DUNG"},
            {btnPhanQuyen,       "QLND.PHAN_QUYEN"},
            {btnNhatKy,          "QLND.NHAT_KY"},
            {btnDoanhThu,        "BCTK.DOANH_THU"},
            {btnHangHoa,         "BCTK.HANG_HOA"},
            {btnTonKho,          "BCTK.TON_KHO"}
        };

        // Ẩn button con không có quyền
        for (Object[] pair : btnMap) {
            Button btn = (Button) pair[0];
            String maQuyen = (String) pair[1];
            if (btn != null && !session.hasPermission(maQuyen)) {
                btn.setVisible(false);
                btn.setManaged(false);
            }
        }

        // Ẩn TitledPane cha nếu KHÔNG có bất kỳ quyền con nào
        for (Object[] pair : tpMap) {
            TitledPane tp = (TitledPane) pair[0];
            String maCha = (String) pair[1];
            if (tp != null && !session.hasPermission(maCha)) {
                boolean coQuyenCon = false;
                for (Object[] btnPair : btnMap) {
                    String maConQuyen = (String) btnPair[1];
                    if (maConQuyen.startsWith(maCha + ".") && session.hasPermission(maConQuyen)) {
                        coQuyenCon = true;
                        break;
                    }
                }
                if (!coQuyenCon) {
                    tp.setVisible(false);
                    tp.setManaged(false);
                }
            }
        }

        // Ẩn 3 card trên Trang Chủ nếu không có quyền
        if (cardBanThuoc != null && !session.hasPermission("QLBH.LAP_HOA_DON")) {
            cardBanThuoc.setVisible(false);
            cardBanThuoc.setManaged(false);
        }
        if (cardDoiTra != null && !session.hasPermission("QLBH.XU_LY_DOI_TRA")) {
            cardDoiTra.setVisible(false);
            cardDoiTra.setManaged(false);
        }
        if (cardBaoCao != null) {
            boolean coBCTK = session.hasPermission("BCTK.DOANH_THU") || 
                             session.hasPermission("BCTK.HANG_HOA") || 
                             session.hasPermission("BCTK.TON_KHO");
            if (!coBCTK) {
                cardBaoCao.setVisible(false);
                cardBaoCao.setManaged(false);
            }
        }
    }

    private void setupTable() {
        colMaThuoc.setCellValueFactory(new PropertyValueFactory<>("maThuoc"));
        colTenThuoc.setCellValueFactory(new PropertyValueFactory<>("tenThuoc"));
        colTrieuChung.setCellValueFactory(new PropertyValueFactory<>("trieuChung"));
        colDVT.setCellValueFactory(new PropertyValueFactory<>("donViCoBan"));

        colKeDon.setCellValueFactory(new PropertyValueFactory<>("canKeDon"));
        colKeDon.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
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
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                getStyleClass().removeAll("text-xanh-bien", "text-vang-cam", "text-do");
                if (empty || item == null) {
                    setText(null);
                } else {
                    if ("DANG_BAN".equals(item)) {
                        setText("Đang Bán");
                        getStyleClass().add("text-xanh-bien");
                    } else if ("HET_HANG".equals(item)) {
                        setText("Hết Hàng");
                        getStyleClass().add("text-vang-cam");
                    } else if ("NGUNG_BAN".equals(item)) {
                        setText("Ngừng Bán");
                        getStyleClass().add("text-do");
                    } else {
                        setText(item);
                    }
                }
            }
        });

        colHinhAnh.setCellValueFactory(new PropertyValueFactory<>("hinhAnh"));
        colHinhAnh.setCellFactory(column -> new TableCell<>() {
            private final ImageView iv = new ImageView();

            @Override
            protected void updateItem(String file, boolean empty) {
                super.updateItem(file, empty);
                if (empty || file == null || file.trim().isEmpty()) {
                    setGraphic(null);
                } else {
                    try {
                        InputStream is = getClass().getResourceAsStream("/resources/images/images_thuoc/" + file.trim());
                        if (is != null) {
                            iv.setImage(new Image(is));
                            iv.setFitWidth(80);
                            iv.setFitHeight(60);
                            iv.setPreserveRatio(true);
                            setGraphic(iv);
                            setAlignment(Pos.CENTER);
                        } else {
                            setGraphic(new Label("No image"));
                        }
                    } catch (Exception e) {
                        setGraphic(new Label("Error"));
                    }
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

    private void loadDataFromServer() {
        masterData.setAll(daoThuoc.getAllThuoc());
    }

    private void setupSearchLogic() {
        FilteredList<Thuoc> filteredData = new FilteredList<>(masterData, p -> true);
        txtTimKiem.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(thuoc -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String filter = newValue.toLowerCase();

                if ((thuoc.getTrieuChung() != null && thuoc.getTrieuChung().toLowerCase().contains(filter)) || thuoc.getMaThuoc().toLowerCase().contains(filter) || thuoc.getTenThuoc().toLowerCase().contains(filter)) {
					return true;
				}
                if (thuoc.getCongDung() != null && thuoc.getCongDung().toLowerCase().contains(filter)) {
					return true;
				}
                if (thuoc.getHoatChat() != null && thuoc.getHoatChat().toLowerCase().contains(filter)) {
					return true;
				}
                if (thuoc.getHangSanXuat() != null && thuoc.getHangSanXuat().toLowerCase().contains(filter)) {
					return true;
				}
                if (thuoc.getNuocSanXuat() != null && thuoc.getNuocSanXuat().toLowerCase().contains(filter)) {
					return true;
				}

                String keDonString = thuoc.isCanKeDon() ? "co ke don" : "khong ke don";
                return keDonString.contains(filter);
            });
        });
        SortedList<Thuoc> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableThuoc.comparatorProperty());
        tableThuoc.setItems(sortedData);
    }

    @FXML
    void handleVeTrangChu(ActionEvent event) {
        loadDataTrangChu();
        if (noiDungTrangChuGoc != null) {
            mainBorderPane.setCenter(noiDungTrangChuGoc);
        }
    }

    @FXML
    void handleDangXuat(ActionEvent event) {
        try {
            // Ghi nhật ký đăng xuất
            DAO_NhatKyHoatDong.ghiLog("DANG_XUAT", "Hệ thống", 
                UserSession.getInstance().getUser().getMaNhanVien(),
                UserSession.getInstance().getUser().getHoTen() + " đã đăng xuất hệ thống");

            nhanVienDangNhap = null;
            UserSession.getInstance().clear();
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
            Parent root = FXMLLoader.load(getClass().getResource("GUI_DangNhap.fxml"));
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(root));
            loginStage.setTitle("Dang nhap");
            loginStage.show();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    void handleMoQuanLyDanhMucThuoc(ActionEvent event) {
        utils.SceneUtils.switchPage("/gui/main/GUI_DanhMucThuoc.fxml");
    }

    @FXML
    void handleMoQuanLyDonThuoc(ActionEvent event) {
        utils.SceneUtils.switchPage("/gui/main/GUI_DanhMucDonThuoc.fxml");
    }

    @FXML
    void handleMoQuanLyDonViQuyDoi(ActionEvent event) {
        utils.SceneUtils.switchPage("/gui/main/GUI_DonViQuyDoi.fxml");
    }

    @FXML
    void handleMoQuanLyBanHangLapHoaDon(ActionEvent event) {
        switchPage("/gui/main/GUI_QuanLyBanHang.fxml");
    }

    @FXML
    void handleMoBanThuoc(javafx.scene.input.MouseEvent event) {
        if (!UserSession.getInstance().hasPermission("QLBH.LAP_HOA_DON")) {
            new Alert(Alert.AlertType.WARNING, "Bạn không có quyền truy cập trang Bán Hàng.").show();
            return;
        }
        switchPage("/gui/main/GUI_QuanLyBanHang.fxml");
    }

    @FXML
    void handleMoQuanLyKhachHang(ActionEvent event) {
        switchPage("/gui/main/GUI_QuanLyKhachHang.fxml");
    }

    @FXML
    void moTrangDanhMucKho(ActionEvent event) {
        utils.SceneUtils.switchPage("/gui/main/GUI_DanhMucKho.fxml");
    }

    @FXML
    void moTrangNhapKho(ActionEvent event) {
        utils.SceneUtils.switchPage("/gui/main/GUI_NhapKho.fxml");
    }

    @FXML
    void moTrangXuLyDoiTra(ActionEvent event) {
        utils.SceneUtils.switchPage("/gui/main/GUI_XuLyDoiTra.fxml");
    }

    @FXML
    void handleMoXuLyDoiTra(ActionEvent event) {
        switchPage("/gui/main/GUI_XuLyDoiTra.fxml");
    }

    @FXML
    void handleMoXuLyDoiTraCard(javafx.scene.input.MouseEvent event) {
        if (!UserSession.getInstance().hasPermission("QLBH.XU_LY_DOI_TRA")) {
            new Alert(Alert.AlertType.WARNING, "Bạn không có quyền truy cập trang Xử Lý Đổi Trả.").show();
            return;
        }
        switchPage("/gui/main/GUI_XuLyDoiTra.fxml");
    }

    @FXML
    void handleMoBaoCaoCard(javafx.scene.input.MouseEvent event) {
        UserSession session = UserSession.getInstance();
        if (!session.hasPermission("BCTK.DOANH_THU") && !session.hasPermission("BCTK.HANG_HOA") && !session.hasPermission("BCTK.TON_KHO")) {
            new Alert(Alert.AlertType.WARNING, "Bạn không có quyền truy cập Báo Cáo Thống Kê.").show();
            return;
        }
        // Mở báo cáo doanh thu mặc định
        new Alert(Alert.AlertType.INFORMATION, "Tính năng Báo Cáo Thống Kê sẽ được cập nhật trong phiên bản tiếp theo.").show();
    }

    @FXML
    void moTrangXuatKho(ActionEvent event) {
        utils.SceneUtils.switchPage("/gui/main/GUI_XuatKho.fxml");
    }

    @FXML
    void moQuanLyDonDatHang(ActionEvent event) {
        utils.SceneUtils.switchPage("/gui/main/GUI_QuanLyDonDatHang.fxml");
    }

    @FXML
    void handleMoQuanLyDanhMucNhaCungCap(ActionEvent event) {
        utils.SceneUtils.switchPage("/gui/main/GUI_DanhMucNhaCungCap.fxml");
    }

    @FXML
    void handleMoQuanLyCongNo(ActionEvent event) {
        utils.SceneUtils.switchPage("/gui/main/GUI_QuanLyCongNo.fxml");
    }

    @FXML
    void handleMoQuanLyBangGia(ActionEvent event) {
        utils.SceneUtils.switchPage("/gui/main/GUI_QuanLyBangGia.fxml");
    }

    @FXML
    void handleMoDanhSachHoaDon(ActionEvent event) {
        utils.SceneUtils.switchPage("/gui/main/GUI_DanhSachHoaDon.fxml");
    }

    @FXML
    void handleMoQuanLyLoThuoc(ActionEvent event) {
        utils.SceneUtils.switchPage("/gui/main/GUI_QuanLyLoThuoc.fxml");
    }

    @FXML
    void handleMoQuanLyNguoiDung(ActionEvent event) {
        utils.SceneUtils.switchPage("/gui/main/GUI_QuanLyNguoiDung.fxml");
    }

    @FXML
    void handleMoLichSuGiaoDich(ActionEvent event) {
        utils.SceneUtils.switchPage("/gui/main/GUI_LichSuGiaoDich.fxml");
    }
    @FXML
    void handleMoDoiMatKhau(ActionEvent event) {
        utils.SceneUtils.switchPage("/gui/main/GUI_DoiMatKhau.fxml");
    }

    @FXML
    void handleMoThongKeDoanhThu(ActionEvent event) {
        utils.SceneUtils.switchPage("/gui/main/GUI_ThongKeDoanhThu.fxml");
    }

    @FXML
    void handleMoPhanQuyen(ActionEvent event) {
        utils.SceneUtils.switchPage("/gui/main/GUI_PhanQuyen.fxml");
    }

    @FXML
    void handleMoNhatKy(ActionEvent event) {
        utils.SceneUtils.switchPage("/gui/main/GUI_NhatKyHoatDong.fxml");
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
        } catch (Exception e) {
            System.err.println("Loi mo Tao Bang Gia: " + e.getMessage());
            e.printStackTrace();
        }
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
        } catch (Exception e) {
            System.err.println("Loi nap file FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public void loadDataTrangChu() {
        masterData.setAll(daoThuoc.getAllThuoc());
    }
}
