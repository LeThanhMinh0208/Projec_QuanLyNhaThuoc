package gui.main;

import dao.DAO_LoThuoc;
import entity.LoThuoc;
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

import java.net.URL;
import java.util.ResourceBundle;

import utils.AlertUtils;
import utils.SceneUtils;

public class GUI_DanhMucKhoController implements Initializable {

    @FXML private ComboBox<String> cmbViTriKho;
    @FXML private TextField txtTimKiem;
    @FXML private TableView<LoThuoc> tableKho;
    
    @FXML private TableColumn<LoThuoc, String> colMaThuoc, colHinhAnh, colTenThuoc, colViTriKho, colMaLo;
    @FXML private TableColumn<LoThuoc, Integer> colSoLuong, colTrangThaiTon;

    private ObservableList<LoThuoc> masterData = FXCollections.observableArrayList();
    private FilteredList<LoThuoc> filteredData;

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        setupComboBox();
        setupTableColumns();
        loadDataFromDatabase();
        setupSearchAndFilter();
    }

    private void setupComboBox() {
        cmbViTriKho.setItems(FXCollections.observableArrayList("Tất cả vị trí kho", "Kho Bán Hàng", "Kho Dự Trữ"));
        cmbViTriKho.getSelectionModel().selectFirst();
    }

    private Image loadImage(String tenFile) {
        if (tenFile == null || tenFile.trim().isEmpty()) return null;
        try {
            URL url = getClass().getResource("/resources/images/images_thuoc/" + tenFile.trim());
            if (url != null) return new Image(url.toExternalForm(), 45, 45, true, true);
        } catch (Exception e) {}
        return null;
    }

    private void setupTableColumns() {
        colMaLo.setCellValueFactory(new PropertyValueFactory<>("maLoThuoc"));
        colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuongTon"));
        colMaThuoc.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getThuoc().getMaThuoc()));
        colTenThuoc.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getThuoc().getTenThuoc()));

        colViTriKho.setCellValueFactory(cell -> {
            String dbCode = cell.getValue().getViTriKho();
            return new SimpleStringProperty("KHO_BAN_HANG".equals(dbCode) ? "Kho Bán Hàng" : "Kho Dự Trữ");
        });

        colHinhAnh.setCellFactory(param -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) { 
                    setGraphic(null); 
                } else {
                    LoThuoc lo = getTableRow().getItem();
                    Image img = loadImage(lo.getThuoc().getHinhAnh());
                    if (img != null) {
                        imageView.setImage(img);
                        setGraphic(imageView);
                    } else {
                        Label lblError = new Label("Trống");
                        lblError.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
                        setGraphic(lblError);
                    }
                    setAlignment(Pos.CENTER);
                }
            }
        });

        // 🚨 FIX LỖI MÀU TRẠNG THÁI Ở ĐÂY
        colTrangThaiTon.setCellValueFactory(new PropertyValueFactory<>("soLuongTon"));
        colTrangThaiTon.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(Integer soLuong, boolean empty) {
                super.updateItem(soLuong, empty);
                setAlignment(Pos.CENTER);
                
                // Xóa sạch các class màu cũ để tránh xung đột
                getStyleClass().removeAll("status-con-hang", "status-sap-het", "status-het-hang");
                setStyle(""); // Reset luôn style cứng cho chắc ăn
                
                if (empty || soLuong == null) {
                    setText(null); 
                } else {
                    if (soLuong == 0) {
                        setText("Cạn Kho");
                        // Ép trực tiếp màu bằng setStyle nếu CSS không ăn
                        setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;"); 
                        getStyleClass().add("status-het-hang"); 
                    } else if (soLuong <= 100) {
                        setText("Sắp Hết Hàng");
                        setStyle("-fx-text-fill: #ea580c; -fx-font-weight: bold;");
                        getStyleClass().add("status-sap-het");  
                    } else {
                        setText("Còn Hàng");
                        setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold;");
                        getStyleClass().add("status-con-hang"); 
                    }
                }
            }
        });
    }

    private void loadDataFromDatabase() {
        masterData.clear();
        masterData.addAll(new DAO_LoThuoc().getAllLoThuoc());
        tableKho.refresh();
    }

    private void setupSearchAndFilter() {
        filteredData = new FilteredList<>(masterData, b -> true);
        cmbViTriKho.valueProperty().addListener((obs, oldV, newV) -> filterData());
        txtTimKiem.textProperty().addListener((obs, oldV, newV) -> filterData());
        SortedList<LoThuoc> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableKho.comparatorProperty());
        tableKho.setItems(sortedData);
    }

    private void filterData() {
        String viTriSelection = cmbViTriKho.getValue();
        String tuKhoa = txtTimKiem.getText().toLowerCase().trim();

        filteredData.setPredicate(lo -> {
            boolean matchKho = viTriSelection.equals("Tất cả vị trí kho") || 
                              (viTriSelection.equals("Kho Bán Hàng") && "KHO_BAN_HANG".equals(lo.getViTriKho())) ||
                              (viTriSelection.equals("Kho Dự Trữ") && "KHO_DU_TRU".equals(lo.getViTriKho()));

            // 💡 Cập nhật lại chuỗi trạng thái để tìm kiếm cho chuẩn
            String trangThai;
            if (lo.getSoLuongTon() == 0) {
                trangThai = "cạn kho";
            } else if (lo.getSoLuongTon() <= 100) {
                trangThai = "sắp hết hàng";
            } else {
                trangThai = "còn hàng";
            }

            boolean matchSearch = tuKhoa.isEmpty() || 
                                 lo.getMaLoThuoc().toLowerCase().contains(tuKhoa) ||
                                 lo.getThuoc().getMaThuoc().toLowerCase().contains(tuKhoa) ||
                                 lo.getThuoc().getTenThuoc().toLowerCase().contains(tuKhoa) ||
                                 trangThai.contains(tuKhoa);

            return matchKho && matchSearch;
        });
    }

    // =========================================================================
    // HÀM CHUYỂN TRANG: Gọi đại ca Trang Chủ và truyền chữ "Nhập Kho" / "Xuất Kho"
    // =========================================================================
    @FXML 
    void moTrangNhapKho(ActionEvent event) { 
        if (GUI_TrangChuController.getInstance() != null) {
            GUI_TrangChuController.getInstance().chuyenTrangVaHighlight("/gui/main/GUI_NhapKho.fxml", "Nhập Kho");
        }
    }

    @FXML 
    void handleChuyenTrangXuatKho(ActionEvent event) { 
        if (GUI_TrangChuController.getInstance() != null) {
            GUI_TrangChuController.getInstance().chuyenTrangVaHighlight("/gui/main/GUI_XuatKho.fxml", "Xuất Kho");
        }
    }

    @FXML void handleChuyenTrangKiemKe(ActionEvent event) { 
        AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Tính năng đang phát triển", "Tính năng kiểm kê kho sẽ được cập nhật trong phiên bản tiếp theo.");
    }
}