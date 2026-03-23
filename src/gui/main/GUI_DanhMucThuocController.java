package gui.main;

import dao.DAO_DanhMucThuoc;
import dao.DAO_Thuoc;
import entity.Thuoc;
import gui.dialogs.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import utils.AlertUtils;
import utils.WindowUtils;

import java.io.InputStream;
import java.util.ArrayList;

public class GUI_DanhMucThuocController {
    @FXML private TableView<Thuoc> tableThuoc;
    // @FXML private ComboBox<String> cbLocDanhMuc; // Bỏ theo yêu cầu người dùng
    
    @FXML private TableColumn<Thuoc, String> colMa, colHinhAnh, colTen, colDanhMuc, colHoatChat, colHangSX, colNuocSX, colCongDung, colTrangThai;
    @FXML private TableColumn<Thuoc, Boolean> colKeDon;
    
    @FXML private TextField txtTimKiem;

    private DAO_Thuoc daoThuoc = new DAO_Thuoc();
    private DAO_DanhMucThuoc daoDanhMuc = new DAO_DanhMucThuoc();
    private ObservableList<Thuoc> masterData = FXCollections.observableArrayList();
    private FilteredList<Thuoc> filteredData;

    @FXML
    public void initialize() {
        setupTable();
        loadData();
    }

    private void setupTable() {
        // Cấu hình các cột Text cơ bản
        colMa.setCellValueFactory(new PropertyValueFactory<>("maThuoc"));
        colTen.setCellValueFactory(new PropertyValueFactory<>("tenThuoc"));
        colDanhMuc.setCellValueFactory(new PropertyValueFactory<>("tenDanhMuc"));
        colHoatChat.setCellValueFactory(new PropertyValueFactory<>("hoatChat"));
        colHangSX.setCellValueFactory(new PropertyValueFactory<>("hangSanXuat"));
        colNuocSX.setCellValueFactory(new PropertyValueFactory<>("nuocSanXuat"));
        colCongDung.setCellValueFactory(new PropertyValueFactory<>("congDung"));

        // 1. CỘT TRẠNG THÁI (Đã cạo trọc màu, chỉ dịch ngôn ngữ)
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        colTrangThai.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    switch (item) {
                        case "DANG_BAN": setText("Đang kinh doanh"); break;
                        case "HET_HANG": setText("Hết hàng"); break;
                        case "NGUNG_BAN": setText("Ngừng bán"); break;
                        default: setText(item);
                    }
                }
            }
        });

        // 2. CỘT KÊ ĐƠN (Đã cạo trọc màu)
        colKeDon.setCellValueFactory(new PropertyValueFactory<>("canKeDon"));
        colKeDon.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item ? "Có" : "Không");
                }
            }
        });

        // 3. CỘT HÌNH ẢNH (Giữ nguyên)
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
                            iv.setFitWidth(60); iv.setFitHeight(45);
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

        // --- 4. LOGIC CLICK CHUỘT THÔNG MINH (GIỐNG Y CHANG TRANG CHỦ) ---
        tableThuoc.setRowFactory(tv -> {
            TableRow<Thuoc> row = new TableRow<>();
            
            // Lắng nghe sự kiện click 1 lần (Toggle Selection & Xóa bóng ma Focus)
            row.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
                if (event.getClickCount() == 1 && (!row.isEmpty()) && row.isSelected()) {
                    tv.getSelectionModel().clearSelection();
                    tv.getFocusModel().focus(-1); 
                    tableThuoc.getParent().requestFocus(); 
                    event.consume(); 
                }
            });

            // Lắng nghe sự kiện Double Click (Mở form Sửa)
            row.setOnMouseClicked(event -> {
                if (event.getClickCount() == 2 && (!row.isEmpty())) {
                    checkAndOpenDialog("/gui/dialogs/Dialog_SuaThuoc.fxml", "Sửa Thuốc");
                }
            });
            
            return row;
        });
    }

    private void loadData() {
        masterData.setAll(daoThuoc.getAllThuoc());
        
        filteredData = new FilteredList<>(masterData, p -> true);
        txtTimKiem.textProperty().addListener((o, oldV, newV) -> filterData());
        
        SortedList<Thuoc> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableThuoc.comparatorProperty());
        tableThuoc.setItems(sortedData);
    }

    private void filterData() {
        String keyword = txtTimKiem.getText() == null ? "" : txtTimKiem.getText().toLowerCase();

        filteredData.setPredicate(t -> {
            if (keyword.isEmpty()) return true;

            if (t.getMaThuoc() != null && t.getMaThuoc().toLowerCase().contains(keyword)) return true;
            if (t.getTenThuoc() != null && t.getTenThuoc().toLowerCase().contains(keyword)) return true;
            if (t.getHoatChat() != null && t.getHoatChat().toLowerCase().contains(keyword)) return true;
            if (t.getHangSanXuat() != null && t.getHangSanXuat().toLowerCase().contains(keyword)) return true;
            if (t.getCongDung() != null && t.getCongDung().toLowerCase().contains(keyword)) return true;
            
            String keDonString = t.isCanKeDon() ? "có kê đơn" : "không kê đơn";
            return keDonString.contains(keyword);
        });
    }

    @FXML void handleThem() { openDialog("/gui/dialogs/Dialog_ThemThuoc.fxml", "Thêm Thuốc Mới", null); }
    @FXML void handleSua() { checkAndOpenDialog("/gui/dialogs/Dialog_SuaThuoc.fxml", "Sửa Thuốc"); }
    @FXML void handleXoa() { checkAndOpenDialog("/gui/dialogs/Dialog_XoaThuoc.fxml", "Xóa Thuốc"); }

    @FXML
    void handleRefresh() {
        txtTimKiem.clear();
        tableThuoc.getSelectionModel().clearSelection();
        loadData();
    }

    private void checkAndOpenDialog(String path, String title) {
        Thuoc selected = tableThuoc.getSelectionModel().getSelectedItem();
        if (selected == null) {
            // Dùng AlertUtils thay vì khởi tạo Alert thủ công
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Thông báo", "Vui lòng chọn một loại thuốc trong bảng!");
            return;
        }
        openDialog(path, title, selected);
    }

    private void openDialog(String path, String title, Thuoc data) {
        // Sử dụng WindowUtils để bóc tách logic mở Modal
        FXMLLoader loader = WindowUtils.openModal(path, title);
        
        if (loader != null) {
            Object ctrl = loader.getController();

            // Logic truyền dữ liệu vẫn giữ nguyên vẹn
            if (ctrl instanceof Dialog_SuaThuocController && data != null) {
                ((Dialog_SuaThuocController) ctrl).setThuocData(data);
            } else if (ctrl instanceof Dialog_XoaThuocController && data != null) {
                ((Dialog_XoaThuocController) ctrl).setThuocData(data);
            }

            Stage stage = (Stage) ((Parent) loader.getRoot()).getScene().getWindow();
            stage.showAndWait();
            
            loadData(); // Tải lại dữ liệu sau khi đóng Dialog
        }
    }
}