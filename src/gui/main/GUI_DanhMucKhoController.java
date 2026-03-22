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
import utils.DateFormatter;

import java.io.File;
import java.net.URL;
import java.sql.Date;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
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
    @FXML private TableColumn<LoThuoc, Date> colNgayHetHan;
    @FXML private TableColumn<LoThuoc, Date> colCanhBao;
    @FXML private TableColumn<LoThuoc, Integer> colSoLuong;

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

    // --- HÀM LOAD ẢNH BỌC THÉP CHỐNG SẬP APP ---
    private Image loadImage(String tenFile) {
        if (tenFile == null || tenFile.trim().isEmpty()) return null;
        try {
            // Thử load từ thư mục resources
            String path = "/resources/images/images_thuoc/" + tenFile.trim();
            URL url = getClass().getResource(path);
            if (url != null) return new Image(url.toExternalForm(), 45, 45, true, true);
        } catch (Exception e) {}
        return null;
    }

    private void setupTableColumns() {
        colMaLo.setCellValueFactory(new PropertyValueFactory<>("maLoThuoc"));

        colNgayHetHan.setCellValueFactory(new PropertyValueFactory<>("hanSuDung"));
        colNgayHetHan.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Date item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(DateFormatter.format(item));
                }
            }
        });
        
        colViTriKho.setCellValueFactory(cell -> {
            String maDB = cell.getValue().getViTriKho();
            String tenHienThi = maDB; 
            
            if ("KHO_BAN_HANG".equals(maDB)) {
                tenHienThi = "Kho Bán Hàng";
            } else if ("KHO_DU_TRU".equals(maDB)) {
                tenHienThi = "Kho Dự Trữ";
            }
            
            return new SimpleStringProperty(tenHienThi);
        });
        
        colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuongTon"));
        
        colMaThuoc.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getThuoc().getMaThuoc()));
        colTenThuoc.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getThuoc().getTenThuoc()));

        // --- ĐÃ FIX CỘT HÌNH ẢNH DÙNG HÀM BỌC THÉP ---
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

     // --- VŨ KHÍ TỐI THƯỢNG: FIX DỨT ĐIỂM MẤT MÀU CỘT CẢNH BÁO ---
        colCanhBao.setCellValueFactory(new PropertyValueFactory<>("hanSuDung"));
        colCanhBao.setCellFactory(column -> new TableCell<>() {
            
            @Override
            protected void updateItem(Date hanSuDung, boolean empty) {
                super.updateItem(hanSuDung, empty);
                updateAppearance(hanSuDung, empty, isSelected());
            }

            // Bắt sự kiện ngay khoảnh khắc sếp Click hoặc Bỏ Click
            @Override
            public void updateSelected(boolean selected) {
                super.updateSelected(selected);
                updateAppearance(getItem(), isEmpty(), selected);
            }

            // Hàm bơm màu trực tiếp, JavaFX không thể cãi được
            private void updateAppearance(Date hanSuDung, boolean empty, boolean selected) {
                setAlignment(Pos.CENTER);

                if (empty || hanSuDung == null) {
                    setText(null);
                    setStyle(""); 
                } else {
                    LocalDate expiredDate = hanSuDung.toLocalDate();
                    long months = ChronoUnit.MONTHS.between(LocalDate.now(), expiredDate);

                    String textHienThi = "";
                    String mauChu = "";

                    // Định vị ngôn ngữ và màu sắc
                    if (months > 12) {
                        textHienThi = "An Toàn";
                        mauChu = "-fx-text-fill: #16a34a;"; // Xanh lá
                    } else if (months >= 6) {
                        textHienThi = "Ưu Tiên Bán";
                        mauChu = "-fx-text-fill: #ca8a04;"; // Vàng
                    } else if (months >= 3) {
                        textHienThi = "Khuyến Mãi";
                        mauChu = "-fx-text-fill: #ea580c;"; // Cam
                    } else {
                        textHienThi = "Ngưng Bán";
                        mauChu = "-fx-text-fill: #dc2626;"; // Đỏ
                    }

                    setText(textHienThi);

                    // Bơm màu bằng vũ lực
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

            // --- TÍNH TOÁN LẠI TRẠNG THÁI HẠN ĐỂ LỌC ---
            String trangThaiHan = "";
            if (loThuoc.getHanSuDung() != null) {
                long months = ChronoUnit.MONTHS.between(LocalDate.now(), loThuoc.getHanSuDung().toLocalDate());
                if (months > 12) trangThaiHan = "an toàn";
                else if (months >= 6) trangThaiHan = "ưu tiên bán";
                else if (months >= 3) trangThaiHan = "khuyến mãi";
                else trangThaiHan = "ngưng bán";
            }

            // 2. Lọc theo từ khóa tìm kiếm (Gộp cả Trạng Thái Hạn vào đây)
            boolean matchTuKhoa = tuKhoa.isEmpty() || 
                                  (loThuoc.getMaLoThuoc() != null && loThuoc.getMaLoThuoc().toLowerCase().contains(tuKhoa)) ||
                                  (loThuoc.getThuoc().getMaThuoc() != null && loThuoc.getThuoc().getMaThuoc().toLowerCase().contains(tuKhoa)) ||
                                  (loThuoc.getThuoc().getTenThuoc() != null && loThuoc.getThuoc().getTenThuoc().toLowerCase().contains(tuKhoa)) ||
                                  trangThaiHan.contains(tuKhoa);

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