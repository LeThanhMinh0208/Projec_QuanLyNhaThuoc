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

    @FXML private TableView<Thuoc> tableThuoc;
    @FXML private TableColumn<Thuoc, String> colMaThuoc, colHinhAnh, colTenThuoc, colTrieuChung, colDVT, colTrangThai;
    @FXML private TableColumn<Thuoc, Boolean> colKeDon;
    @FXML private TextField txtTimKiem;
    @FXML private BorderPane mainBorderPane;

    private DAO_Thuoc daoThuoc = new DAO_Thuoc();
    private ObservableList<Thuoc> masterData = FXCollections.observableArrayList();
    private static NhanVien nhanVienDangNhap;
    
    // --- [1] CẬP NHẬT 1: THÊM BIẾN ĐỂ LƯU GIAO DIỆN GỐC ---
    private Node noiDungTrangChuGoc; 

    public static void setNhanVienDangNhap(NhanVien nv) {
        nhanVienDangNhap = nv;
    }

    @FXML
    public void initialize() {
        setupTable();
        loadDataFromServer();
        setupSearchLogic();
        utils.SceneUtils.init(mainBorderPane);
        
        // --- [2] CẬP NHẬT 2: LƯU GIAO DIỆN VÀO BIẾN KHI VỪA MỞ PHẦN MỀM ---
        if (mainBorderPane != null) {
            noiDungTrangChuGoc = mainBorderPane.getCenter();
        }
    }

    private void setupTable() {
        // Cấu hình các cột hiển thị cơ bản
        colMaThuoc.setCellValueFactory(new PropertyValueFactory<>("maThuoc"));
        colTenThuoc.setCellValueFactory(new PropertyValueFactory<>("tenThuoc"));
        colTrieuChung.setCellValueFactory(new PropertyValueFactory<>("trieuChung"));
        colDVT.setCellValueFactory(new PropertyValueFactory<>("donViCoBan"));
<<<<<<< HEAD
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));

        // Cấu hình cột Kê Đơn (Có/Không)
=======
     // --- 1. CỘT KÊ ĐƠN (Gán tên Class màu sắc) ---
>>>>>>> main
        colKeDon.setCellValueFactory(new PropertyValueFactory<>("canKeDon"));
        colKeDon.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
<<<<<<< HEAD
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "Có" : "Không");
                    setStyle(item ? "-fx-text-fill: #e74c3c; -fx-font-weight: bold;" : "-fx-text-fill: #27ae60; -fx-font-weight: bold;");
=======
                // Quét sạch class màu cũ trước khi gán màu mới (tránh bị dính màu)
                getStyleClass().removeAll("text-do", "text-xanh-la");
                
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Có" : "Không");
                    // Gán tên class màu
                    getStyleClass().add(item ? "text-do" : "text-xanh-la");
                }
            }
        });

        // --- 2. CỘT TRẠNG THÁI (Dịch ngôn ngữ & Gán tên Class) ---
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        colTrangThai.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                // Quét sạch class màu cũ 
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
>>>>>>> main
                }
            }
        });

        // Cấu hình cột Hình Ảnh
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
                            iv.setFitWidth(80); iv.setFitHeight(60);
                            iv.setPreserveRatio(true);
                            setGraphic(iv); setAlignment(Pos.CENTER);
                        } else {
                            setGraphic(new Label("No image"));
                        }
                    } catch (Exception e) {
                        setGraphic(new Label("Error"));
                    }
                }
            }
        });
<<<<<<< HEAD
=======
     // --- LOGIC CLICK CHUỘT THÔNG MINH (TOGGLE SELECTION & XÓA FOCUS) ---
        tableThuoc.setRowFactory(tv -> {
            TableRow<Thuoc> row = new TableRow<>();
            row.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
                // Nếu click 1 lần vào dòng có dữ liệu và dòng đó đang được chọn
                if (event.getClickCount() == 1 && (!row.isEmpty()) && row.isSelected()) {
                    // 1. Nhả chọn
                    tv.getSelectionModel().clearSelection();
                    // 2. Xóa bóng ma Focus màu xám
                    tv.getFocusModel().focus(-1); 
                    tableThuoc.getParent().requestFocus(); 
                    // 3. Hủy sự kiện để JavaFX không tự động chọn lại
                    event.consume(); 
                }
            });
            return row;
        });
>>>>>>> main
    }

    private void loadDataFromServer() {
        // Lấy dữ liệu gốc từ database
        masterData.setAll(daoThuoc.getAllThuoc());
    }

    private void setupSearchLogic() {
        // 1. Tạo FilteredList bao quanh masterData
        FilteredList<Thuoc> filteredData = new FilteredList<>(masterData, p -> true);

        // 2. Lắng nghe thay đổi trên ô TextField Tìm kiếm
        txtTimKiem.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(thuoc -> {
                // Nếu không nhập gì, hiện tất cả
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }

                String filter = newValue.toLowerCase();

                // LỌC THEO TRIỆU CHỨNG (Cột mới thêm)
                if (thuoc.getTrieuChung() != null && thuoc.getTrieuChung().toLowerCase().contains(filter)) return true;

                // Lọc theo Mã Thuốc
                if (thuoc.getMaThuoc().toLowerCase().contains(filter)) return true;

                // Lọc theo Tên Thuốc
                if (thuoc.getTenThuoc().toLowerCase().contains(filter)) return true;

                // Lọc theo Công Dụng
                if (thuoc.getCongDung() != null && thuoc.getCongDung().toLowerCase().contains(filter)) return true;

                // Lọc theo Hoạt Chất, Hãng, Nước SX (Dù không hiện trên bảng vẫn lọc được)
                if (thuoc.getHoatChat() != null && thuoc.getHoatChat().toLowerCase().contains(filter)) return true;
                if (thuoc.getHangSanXuat() != null && thuoc.getHangSanXuat().toLowerCase().contains(filter)) return true;
                if (thuoc.getNuocSanXuat() != null && thuoc.getNuocSanXuat().toLowerCase().contains(filter)) return true;

                // Lọc theo Kê đơn (Nhập "có" hoặc "không")
                String keDonString = thuoc.isCanKeDon() ? "có kê đơn" : "không kê đơn";
                if (keDonString.contains(filter)) return true;

                return false; // Không khớp tiêu chí nào
            });
        });

        // 3. Cho phép bảng vẫn có thể sắp xếp (Sort) dữ liệu sau khi lọc
        SortedList<Thuoc> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableThuoc.comparatorProperty());

        // 4. Đổ dữ liệu đã lọc vào bảng
        tableThuoc.setItems(sortedData);
    }

    // --- [3] CẬP NHẬT 3: THÊM HÀM XỬ LÝ NÚT BẤM "TRANG CHỦ" ---
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
<<<<<<< HEAD
    @FXML
    void handleMoQuanLyDonThuoc(ActionEvent event) {
        utils.SceneUtils.switchPage("/gui/main/GUI_DanhMucDonThuoc.fxml");
    }
=======

>>>>>>> main
    @FXML
    void moTrangDanhMucKho(ActionEvent event) {
        utils.SceneUtils.switchPage("/gui/main/GUI_DanhMucKho.fxml");
    }

    @FXML
    void moTrangNhapKho(ActionEvent event) {
        utils.SceneUtils.switchPage("/gui/main/GUI_NhapKho.fxml");
    }
    @FXML
<<<<<<< HEAD
=======
    void moTrangXuatKho(ActionEvent event) {
        utils.SceneUtils.switchPage("/gui/main/GUI_XuatKho.fxml");
    }
    @FXML
>>>>>>> main
    void moQuanLyDonNhapHang(ActionEvent event) {
        utils.SceneUtils.switchPage("/gui/main/GUI_QuanLyDonNhapHang.fxml");
    }
    public void loadDataTrangChu() {
        masterData.setAll(daoThuoc.getAllThuoc()); 
    }
}