package gui.main;

import dao.DAO_NhaCungCap;  // Giả sử bạn đã tạo DAO này
import entity.NhaCungCap;
import gui.dialogs.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import utils.AlertUtils;
import utils.WindowUtils;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;

public class GUI_DanhMucNhaCungCapController {

    @FXML private TableView<NhaCungCap> tableNhaCungCap;
    @FXML private ComboBox<String> cbLocDanhMuc;  

    @FXML private TableColumn<NhaCungCap, String> colMa, colTen, colSdt, colDiaChi;
    @FXML private TableColumn<NhaCungCap, Double> colCongNo;

    @FXML private TextField txtTimKiem;

    private DAO_NhaCungCap daoNhaCungCap = new DAO_NhaCungCap();
    private ObservableList<NhaCungCap> masterData = FXCollections.observableArrayList();
    private FilteredList<NhaCungCap> filteredData;

    private final DecimalFormat df = new DecimalFormat("#,### VNĐ");  // Format công nợ

    @FXML
    public void initialize() {
        setupTable();
        loadData();
    }

    private void setupTable() {
        colMa.setCellValueFactory(new PropertyValueFactory<>("maNhaCungCap"));
        colTen.setCellValueFactory(new PropertyValueFactory<>("tenNhaCungCap"));
        colSdt.setCellValueFactory(new PropertyValueFactory<>("sdt"));
        colDiaChi.setCellValueFactory(new PropertyValueFactory<>("diaChi"));

        // Format cột Công nợ
        colCongNo.setCellValueFactory(new PropertyValueFactory<>("congNo"));
        colCongNo.setCellFactory(column -> new TableCell<>() {
            @Override
            protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(df.format(item));
                    // Màu sắc tùy theo công nợ (ví dụ: âm = đỏ, dương = cam)
                    if (item < 0) {
                        setStyle("-fx-text-fill: #ef4444; -fx-font-weight: bold;");
                    } else if (item > 0) {
                        setStyle("-fx-text-fill: #f59e0b; -fx-font-weight: bold;");
                    } else {
                        setStyle("-fx-text-fill: #10b981;");
                    }
                }
            }
        });
    }

    private void loadData() {
        masterData.setAll(daoNhaCungCap.getAllNhaCungCap());  // Giả sử DAO có method này

        // ComboBox lọc (hiện tại chỉ "Tất cả", có thể mở rộng sau)
        ArrayList<String> dsLoc = new ArrayList<>();
        dsLoc.add("Tất cả nhà cung cấp");
        cbLocDanhMuc.getItems().setAll(dsLoc);
        cbLocDanhMuc.getSelectionModel().selectFirst();

        filteredData = new FilteredList<>(masterData, p -> true);

        txtTimKiem.textProperty().addListener((obs, oldVal, newVal) -> filterData());
        cbLocDanhMuc.valueProperty().addListener((obs, oldVal, newVal) -> filterData());

        SortedList<NhaCungCap> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableNhaCungCap.comparatorProperty());
        tableNhaCungCap.setItems(sortedData);
    }

    private void filterData() {
        String keyword = txtTimKiem.getText() == null ? "" : txtTimKiem.getText().toLowerCase().trim();
        String locValue = cbLocDanhMuc.getValue();

        filteredData.setPredicate(ncc -> {
            // Nếu có lọc theo nhóm/khu vực sau này thì thêm logic ở đây
            if (locValue != null && !locValue.equals("Tất cả nhà cung cấp")) {
                // Ví dụ: return ncc.getKhuVuc().equalsIgnoreCase(locValue);
            }

            if (keyword.isEmpty()) return true;

            if (ncc.getMaNhaCungCap() != null && ncc.getMaNhaCungCap().toLowerCase().contains(keyword)) return true;
            if (ncc.getTenNhaCungCap() != null && ncc.getTenNhaCungCap().toLowerCase().contains(keyword)) return true;
            if (ncc.getSdt() != null && ncc.getSdt().toLowerCase().contains(keyword)) return true;
            if (ncc.getDiaChi() != null && ncc.getDiaChi().toLowerCase().contains(keyword)) return true;

            // Tìm theo công nợ (chuyển số thành chuỗi)
            String congNoStr = df.format(ncc.getCongNo()).toLowerCase();
            return congNoStr.contains(keyword);
        });
    }

    @FXML
    void handleThem() {
        openDialog("/gui/dialogs/Dialog_ThemNhaCungCap.fxml", "Thêm Nhà Cung Cấp Mới", null);
    }

    @FXML
    void handleSua() {
        checkAndOpenDialog("/gui/dialogs/Dialog_SuaNhaCungCap.fxml", "Cập Nhật Nhà Cung Cấp");
    }

    @FXML
    void handleXoa() {
        checkAndOpenDialog("/gui/dialogs/Dialog_XoaNhaCungCap.fxml", "Xóa Nhà Cung Cấp");
    }

    private void checkAndOpenDialog(String path, String title) {
        NhaCungCap selected = tableNhaCungCap.getSelectionModel().getSelectedItem();
        if (selected == null) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Thông báo", "Vui lòng chọn một nhà cung cấp trong bảng!");
            return;
        }
        openDialog(path, title, selected);
    }

    private void openDialog(String path, String title, NhaCungCap data) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            Parent root = loader.load();  // load trả về Parent

            Object ctrl = loader.getController();

            // Truyền dữ liệu nếu cần (sửa/xóa)
            if (ctrl instanceof Dialog_SuaNhaCungCapController && data != null) {
                ((Dialog_SuaNhaCungCapController) ctrl).setNhaCungCapData(data);
            } else if (ctrl instanceof Dialog_XoaNhaCungCapController && data != null) {
                ((Dialog_XoaNhaCungCapController) ctrl).setNhaCungCapData(data);
            }

            // Tạo Stage mới
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.setResizable(false);  // Tùy chọn
            stage.showAndWait();

            loadData();  // Refresh bảng sau dialog
        } catch (IOException e) {
            e.printStackTrace();
            new Alert(Alert.AlertType.ERROR, "Không mở được dialog: " + e.getMessage()).show();
        }
    }
}