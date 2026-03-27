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
import javafx.scene.layout.HBox;

import java.net.URL;
import java.util.ResourceBundle;

public class GUI_DanhMucKhoController implements Initializable {

    @FXML private ComboBox<String> cmbViTriKho;
    @FXML private TextField txtTimKiem;
    @FXML private TableView<LoThuoc> tableKho;
    
    @FXML private TableColumn<LoThuoc, String> colMaThuoc;
    @FXML private TableColumn<LoThuoc, String> colHinhAnh;
    @FXML private TableColumn<LoThuoc, String> colTenThuoc;
    @FXML private TableColumn<LoThuoc, String> colViTriKho;
    @FXML private TableColumn<LoThuoc, String> colMaLo;
    @FXML private TableColumn<LoThuoc, Integer> colSoLuong;
    @FXML private TableColumn<LoThuoc, Integer> colTrangThaiTon; // Cột mới

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
        cmbViTriKho.setItems(FXCollections.observableArrayList(
            "Tất cả vị trí kho", 
            "Kho Bán Hàng", 
            "Kho Dự Trữ"
        ));
        cmbViTriKho.getSelectionModel().selectFirst();
    }

    private Image loadImage(String tenFile) {
        if (tenFile == null || tenFile.trim().isEmpty()) return null;
        try {
            String path = "/resources/images/images_thuoc/" + tenFile.trim();
            URL url = getClass().getResource(path);
            if (url != null) return new Image(url.toExternalForm(), 45, 45, true, true);
        } catch (Exception e) {}
        return null;
    }

    private void setupTableColumns() {
        colMaLo.setCellValueFactory(new PropertyValueFactory<>("maLoThuoc"));
        
        colViTriKho.setCellValueFactory(cell -> {
            String maDB = cell.getValue().getViTriKho();
            String tenHienThi = maDB; 
            if ("KHO_BAN_HANG".equals(maDB)) tenHienThi = "Kho Bán Hàng";
            else if ("KHO_DU_TRU".equals(maDB)) tenHienThi = "Kho Dự Trữ";
            return new SimpleStringProperty(tenHienThi);
        });
        
        colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuongTon"));
        
        colMaThuoc.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getThuoc().getMaThuoc()));
        colTenThuoc.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getThuoc().getTenThuoc()));

        colHinhAnh.setCellFactory(param -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { 
                    setGraphic(null); 
                } else {
                    LoThuoc lo = getTableView().getItems().get(getIndex());
                    String tenFileAnh = lo.getThuoc().getHinhAnh();
                    Image img = loadImage(tenFileAnh);
                    if (img != null) {
                        imageView.setImage(img);
                        setGraphic(imageView);
                        setAlignment(Pos.CENTER);
                    } else {
                        Label lblError = new Label("Trống");
                        lblError.setStyle("-fx-text-fill: #94a3b8; -fx-font-size: 11px;");
                        setGraphic(lblError);
                        setAlignment(Pos.CENTER);
                    }
                }
            }
        });

        // ========================================================
        // CỘT MỚI: TRẠNG THÁI TỒN (Dưới 100 Đỏ, Trên 100 Xanh)
        // ========================================================
        colTrangThaiTon.setCellValueFactory(new PropertyValueFactory<>("soLuongTon"));
        colTrangThaiTon.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Integer soLuong, boolean empty) {
                super.updateItem(soLuong, empty);
                updateAppearance(soLuong, empty, isSelected());
            }

            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);
                updateAppearance(getItem(), isEmpty(), selected);
            }

            private void updateAppearance(Integer soLuong, boolean empty, boolean selected) {
                setAlignment(Pos.CENTER);
                if (empty || soLuong == null) {
                    setText(null);
                    setStyle(""); 
                } else {
                    String textHienThi = "";
                    String mauChu = "";

                    if (soLuong < 100) {
                        textHienThi = "Tồn Thấp";
                        mauChu = "-fx-text-fill: #dc2626;"; // Màu Đỏ
                    } else {
                        textHienThi = "Tồn Cao";
                        mauChu = "-fx-text-fill: #16a34a;"; // Màu Xanh lá
                    }

                    setText(textHienThi);

                    if (selected) {
                        setStyle("-fx-text-fill: white; -fx-font-weight: bold;");
                    } else {
                        setStyle(mauChu + " -fx-font-weight: bold;");
                    }
                }
            }
        });

        tableKho.setRowFactory(tv -> {
            TableRow<LoThuoc> row = new TableRow<>();
            row.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
                if (event.getClickCount() == 1 && (!row.isEmpty()) && row.isSelected()) {
                    tv.getSelectionModel().clearSelection();
                    tv.getFocusModel().focus(-1); 
                    tableKho.getParent().requestFocus(); 
                    event.consume(); 
                }
            });
            return row;
        });
    }

    private void loadDataFromDatabase() {
        DAO_LoThuoc dao = new DAO_LoThuoc();
        masterData.addAll(dao.getAllLoThuoc());
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
        String viTriHienThi = cmbViTriKho.getValue();
        String tuKhoa = txtTimKiem.getText().toLowerCase().trim();

        filteredData.setPredicate(loThuoc -> {
            // 1. Lọc theo vị trí kho
            String maViTriDB = "";
            if ("Kho Bán Hàng".equals(viTriHienThi)) {
                maViTriDB = "KHO_BAN_HANG";
            } else if ("Kho Dự Trữ".equals(viTriHienThi)) {
                maViTriDB = "KHO_DU_TRU";
            }

            boolean matchViTri = "Tất cả vị trí kho".equals(viTriHienThi) || 
                                 (loThuoc.getViTriKho() != null && loThuoc.getViTriKho().equals(maViTriDB));

            // Tính trạng thái tồn để có thể gõ "Tồn thấp" lọc ra luôn
            String trangThaiTon = (loThuoc.getSoLuongTon() < 100) ? "tồn thấp" : "tồn cao";

            // 2. Lọc theo từ khóa tìm kiếm
            boolean matchTuKhoa = tuKhoa.isEmpty() || 
                                  (loThuoc.getMaLoThuoc() != null && loThuoc.getMaLoThuoc().toLowerCase().contains(tuKhoa)) ||
                                  (loThuoc.getThuoc().getMaThuoc() != null && loThuoc.getThuoc().getMaThuoc().toLowerCase().contains(tuKhoa)) ||
                                  (loThuoc.getThuoc().getTenThuoc() != null && loThuoc.getThuoc().getTenThuoc().toLowerCase().contains(tuKhoa)) ||
                                  trangThaiTon.contains(tuKhoa);

            return matchViTri && matchTuKhoa;
        });
    }

    @FXML
    void moTrangNhapKho(ActionEvent event) {
        utils.SceneUtils.switchPage("/gui/main/GUI_NhapKho.fxml");
    }
    
    @FXML 
    void handleChuyenTrangXuatKho(ActionEvent event) { 
        utils.SceneUtils.switchPage("/gui/main/GUI_XuatKho.fxml"); 
    }

    @FXML 
    void handleChuyenTrangKiemKe(ActionEvent event) { 
        utils.SceneUtils.switchPage("/gui/main/GUI_KiemKe.fxml"); 
    }
}