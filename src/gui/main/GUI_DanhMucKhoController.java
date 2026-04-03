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

        colTrangThaiTon.setCellValueFactory(new PropertyValueFactory<>("soLuongTon"));
        colTrangThaiTon.setCellFactory(column -> new TableCell<>() {
            @Override protected void updateItem(Integer soLuong, boolean empty) {
                super.updateItem(soLuong, empty);
                setAlignment(Pos.CENTER);
                if (empty || soLuong == null) {
                    setText(null); setStyle("");
                } else {
                    setText(soLuong < 100 ? "Tồn Thấp" : "Tồn Cao");
                    setStyle(soLuong < 100 ? "-fx-text-fill: #dc2626; -fx-font-weight: bold;" : "-fx-text-fill: #16a34a; -fx-font-weight: bold;");
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

            String trangThai = lo.getSoLuongTon() < 100 ? "tồn thấp" : "tồn cao";
            boolean matchSearch = tuKhoa.isEmpty() || 
                                 lo.getMaLoThuoc().toLowerCase().contains(tuKhoa) ||
                                 lo.getThuoc().getMaThuoc().toLowerCase().contains(tuKhoa) ||
                                 lo.getThuoc().getTenThuoc().toLowerCase().contains(tuKhoa) ||
                                 trangThai.contains(tuKhoa);

            return matchKho && matchSearch;
        });
    }

    @FXML void moTrangNhapKho(ActionEvent event) { utils.SceneUtils.switchPage("/gui/main/GUI_NhapKho.fxml"); }
    @FXML void handleChuyenTrangXuatKho(ActionEvent event) { utils.SceneUtils.switchPage("/gui/main/GUI_XuatKho.fxml"); }
    @FXML void handleChuyenTrangKiemKe(ActionEvent event) { utils.SceneUtils.switchPage("/gui/main/GUI_KiemKe.fxml"); }
}