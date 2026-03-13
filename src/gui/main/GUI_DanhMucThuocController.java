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
    @FXML private ComboBox<String> cbLocDanhMuc;
    
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

        // 1. Logic Render Trạng Thái (Giữ nguyên logic của bạn)
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        colTrangThai.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setStyle("");
                } else {
                    switch (item) {
                        case "DANG_BAN":
                            setText("Đang kinh doanh");
                            setStyle("-fx-text-fill: #10b981; -fx-font-weight: bold;");
                            break;
                        case "HET_HANG":
                            setText("Hết hàng");
                            setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                            break;
                        case "NGUNG_BAN":
                            setText("Ngừng bán");
                            setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                            break;
                        default:
                            setText(item); setStyle("");
                    }
                }
            }
        });

        // 2. Logic Render Cột Kê Đơn
        colKeDon.setCellValueFactory(new PropertyValueFactory<>("canKeDon"));
        colKeDon.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null); setStyle("");
                } else {
                    setText(item ? "Có" : "Không");
                    setStyle(item ? "-fx-text-fill: #ef4444; -fx-font-weight: bold;" : "-fx-text-fill: #10b981; -fx-font-weight: bold;");
                }
            }
        });

        // 3. Logic Render Hình Ảnh
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
    }

    private void loadData() {
        masterData.setAll(daoThuoc.getAllThuoc());
        
        ArrayList<String> dsTenDanhMuc = new ArrayList<>();
        dsTenDanhMuc.add("Tất cả danh mục"); 
        for (entity.DanhMucThuoc dm : daoDanhMuc.getAllDanhMuc()) {
            dsTenDanhMuc.add(dm.getTenDanhMuc());
        }
        
        cbLocDanhMuc.getItems().setAll(dsTenDanhMuc);
        cbLocDanhMuc.getSelectionModel().selectFirst(); 

        filteredData = new FilteredList<>(masterData, p -> true);
        txtTimKiem.textProperty().addListener((o, oldV, newV) -> filterData());
        cbLocDanhMuc.valueProperty().addListener((o, oldV, newV) -> filterData());
        
        SortedList<Thuoc> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableThuoc.comparatorProperty());
        tableThuoc.setItems(sortedData);
    }

    private void filterData() {
        String keyword = txtTimKiem.getText() == null ? "" : txtTimKiem.getText().toLowerCase();
        String danhMucFilter = cbLocDanhMuc.getValue();

        filteredData.setPredicate(t -> {
            if (danhMucFilter != null && !danhMucFilter.equals("Tất cả danh mục")) {
                if (t.getTenDanhMuc() == null || !t.getTenDanhMuc().equalsIgnoreCase(danhMucFilter)) return false;
            }

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