package gui.main;

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
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.scene.layout.BorderPane;
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

    private DAO_Thuoc daoThuoc = new DAO_Thuoc();
    private ObservableList<Thuoc> masterData = FXCollections.observableArrayList();
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
                if (event.getClickCount() == 1 && (!row.isEmpty()) && row.isSelected()) {
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
                if (newValue == null || newValue.isEmpty()) return true;
                String filter = newValue.toLowerCase();

                if (thuoc.getTrieuChung() != null && thuoc.getTrieuChung().toLowerCase().contains(filter)) return true;
                if (thuoc.getMaThuoc().toLowerCase().contains(filter)) return true;
                if (thuoc.getTenThuoc().toLowerCase().contains(filter)) return true;
                if (thuoc.getCongDung() != null && thuoc.getCongDung().toLowerCase().contains(filter)) return true;
                if (thuoc.getHoatChat() != null && thuoc.getHoatChat().toLowerCase().contains(filter)) return true;
                if (thuoc.getHangSanXuat() != null && thuoc.getHangSanXuat().toLowerCase().contains(filter)) return true;
                if (thuoc.getNuocSanXuat() != null && thuoc.getNuocSanXuat().toLowerCase().contains(filter)) return true;
                
                String keDonString = thuoc.isCanKeDon() ? "có kê đơn" : "không kê đơn";
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
            nhanVienDangNhap = null;
            Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
            currentStage.close();
            Parent root = FXMLLoader.load(getClass().getResource("GUI_DangNhap.fxml"));
            Stage loginStage = new Stage();
            loginStage.setScene(new Scene(root));
            loginStage.setTitle("Đăng nhập");
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
    void handleMoQuanLyBanHangLapHoaDon(ActionEvent event) {
        switchPage("/gui/main/GUI_QuanLyBanHang.fxml");
    }

    @FXML
    void handleMoBanThuoc(javafx.scene.input.MouseEvent event) {
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
    void handleMoTaoBangGia(ActionEvent event) {
        try {
            javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(
                    getClass().getResource("/gui/main/GUI_QuanLyBangGia.fxml"));
            javafx.scene.Parent root = loader.load();
            Object controller = loader.getController();
            if (controller instanceof GUI_QuanLyBangGiaController) {
                ((GUI_QuanLyBangGiaController) controller).handleThemBangGiaMoi();
            }
            mainBorderPane.setCenter(root);
        } catch (Exception e) {
            System.err.println("Lỗi mở Tạo Bảng Giá: " + e.getMessage());
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
            System.err.println("Lỗi nạp file FXML: " + fxmlPath);
            e.printStackTrace();
        }
    }

    public void loadDataTrangChu() {
        masterData.setAll(daoThuoc.getAllThuoc());
    }
}