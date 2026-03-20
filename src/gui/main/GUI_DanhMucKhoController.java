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
    private void setupTableColumns() {
        colMaLo.setCellValueFactory(new PropertyValueFactory<>("maLoThuoc"));
  
      

        // 2. Cấu hình hiển thị cho cột Ngày Hết Hạn
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
        // Lấy dữ liệu từ đối tượng Thuoc
        colMaThuoc.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getThuoc().getMaThuoc()));
        colTenThuoc.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getThuoc().getTenThuoc()));

        // Xử lý Hình Ảnh
        colHinhAnh.setCellFactory(param -> new TableCell<>() {
            private final ImageView imageView = new ImageView();
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); } 
                else {
                    LoThuoc lo = getTableView().getItems().get(getIndex());
                    String tenFileAnh = lo.getThuoc().getHinhAnh();
                    try {
                       
                        String path = "/resources/images/images_thuoc/" + tenFileAnh;
                        Image img = new Image(getClass().getResourceAsStream(path));
                        imageView.setImage(img);
                        imageView.setFitWidth(45);
                        imageView.setFitHeight(45);
                        setGraphic(imageView);
                        setAlignment(Pos.CENTER);
                    } catch (Exception e) {
                        setText("Lỗi Ảnh"); // Nếu không tìm thấy file ảnh
                    }
                }
            }
        });

        // Xử lý Cảnh Báo Hạn Sử Dụng
        colCanhBao.setCellValueFactory(new PropertyValueFactory<>("hanSuDung"));
        colCanhBao.setCellFactory(param -> new TableCell<>() {
            @Override
            protected void updateItem(Date hanSuDung, boolean empty) {
                super.updateItem(hanSuDung, empty);
                if (empty || hanSuDung == null) { setGraphic(null); } 
                else {
                    LocalDate expiredDate = hanSuDung.toLocalDate();
                    long months = ChronoUnit.MONTHS.between(LocalDate.now(), expiredDate);
                    
                    Label lblStatus = new Label();
                    lblStatus.getStyleClass().add("status-label");

                    if (months > 12) {
                        lblStatus.setText("An Toàn");
                        lblStatus.getStyleClass().add("status-safe");
                    } else if (months >= 6) {
                        lblStatus.setText("Ưu Tiên Bán");
                        lblStatus.getStyleClass().add("status-priority");
                    } else if (months >= 3) {
                        lblStatus.setText("Khuyến Mãi");
                        lblStatus.getStyleClass().add("status-promo");
                    } else {
                        lblStatus.setText("Ngưng Bán");
                        lblStatus.getStyleClass().add("status-stop");
                    }
                    HBox box = new HBox(lblStatus);
                    box.setAlignment(Pos.CENTER);
                    setGraphic(box);
                }
            }
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
                                  trangThaiHan.contains(tuKhoa); // <--- Đã bổ sung tính năng sếp yêu cầu

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