package gui.main;

import dao.DAO_KhachHang;
import entity.KhachHang;
import javafx.scene.Parent;
import gui.dialogs.Dialog_LichSuGiaoDichController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;

public class GUI_QuanLyKhachHangController {

    @FXML private TextField txtTimKiem;
    @FXML private TableView<KhachHang> tableKhachHang;
    @FXML private TableColumn<KhachHang, String> colMa;
    @FXML private TableColumn<KhachHang, String> colHoTen;
    @FXML private TableColumn<KhachHang, String> colSdt;
    @FXML private TableColumn<KhachHang, String> colDiaChi;
    @FXML private TableColumn<KhachHang, Integer> colDiem;

    private final DAO_KhachHang daoKhachHang = new DAO_KhachHang();
    private final ObservableList<KhachHang> dsKhachHang = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        setupSearch();
    }

    private void setupTable() {
        colMa.setCellValueFactory(new PropertyValueFactory<>("maKhachHang"));
        colHoTen.setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        colSdt.setCellValueFactory(new PropertyValueFactory<>("sdt"));
        colDiaChi.setCellValueFactory(new PropertyValueFactory<>("diaChi"));
        colDiem.setCellValueFactory(new PropertyValueFactory<>("diemTichLuy"));

        tableKhachHang.setRowFactory(tv -> {
            TableRow<KhachHang> row = new TableRow<>();
            row.setOnMouseClicked((MouseEvent event) -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    handleSua(null);
                }
            });
            return row;
        });
    }

    private void loadData() {
        dsKhachHang.setAll(daoKhachHang.getAllKhachHang());
        tableKhachHang.setItems(dsKhachHang);
    }

    private void setupSearch() {
        FilteredList<KhachHang> filteredData = new FilteredList<>(dsKhachHang, p -> true);
        txtTimKiem.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(kh -> {
                if (newValue == null || newValue.isEmpty()) return true;
                String lowerCaseFilter = newValue.toLowerCase();
                if (kh.getHoTen().toLowerCase().contains(lowerCaseFilter)) return true;
                if (kh.getSdt().contains(lowerCaseFilter)) return true;
                return false;
            });
        });
        SortedList<KhachHang> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableKhachHang.comparatorProperty());
        tableKhachHang.setItems(sortedData);
    }

    private String phatSinhMaKH() {
        int max = 0;
        for (KhachHang kh : dsKhachHang) {
            String ma = kh.getMaKhachHang();
            if (ma.startsWith("KH")) {
                try {
                    int num = Integer.parseInt(ma.substring(2));
                    if (num > max) max = num;
                } catch (Exception e) {}
            }
        }
        return String.format("KH%03d", max + 1);
    }

    @FXML
    void handleLamMoiTK(ActionEvent event) {
        txtTimKiem.clear();
        tableKhachHang.getSelectionModel().clearSelection();
    }

    @FXML
    void handleThem(ActionEvent event) {
        openDialog("/gui/dialogs/Dialog_ThemKhachHang.fxml", "Thêm Khách Hàng Mới", null);
    }
    @FXML
    void handleXemLichSu(ActionEvent event) {
        KhachHang selected = tableKhachHang.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Thông báo", "Vui lòng chọn một khách hàng!");
            return;
        }
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource("/gui/dialogs/Dialog_LichSuGiaoDich.fxml"));
            Parent root = loader.load();
            Dialog_LichSuGiaoDichController ctrl = loader.getController();
            ctrl.setKhachHang(selected);

            Stage stage = new Stage();
            stage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
            stage.setTitle("Lịch Sử Giao Dịch — " + selected.getHoTen());
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @FXML
    void handleSua(ActionEvent event) {
        checkAndOpenDialog("/gui/dialogs/Dialog_SuaKhachHang.fxml", "Cập Nhật Thông Tin Khách Hàng");
    }

    @FXML
    void handleXoa(ActionEvent event) {
        checkAndOpenDialog("/gui/dialogs/Dialog_XoaKhachHang.fxml", "Xác Nhận Xóa Khách Hàng");
    }

    private void checkAndOpenDialog(String path, String title) {
        KhachHang selected = tableKhachHang.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Thông báo", "Vui lòng chọn một khách hàng trong bảng!");
            return;
        }
        openDialog(path, title, selected);
    }

    private void openDialog(String path, String title, KhachHang data) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
            javafx.scene.Parent root = loader.load();

            Object ctrl = loader.getController();

            if (ctrl instanceof gui.dialogs.Dialog_ThemKhachHangController) {
                ((gui.dialogs.Dialog_ThemKhachHangController) ctrl).setMaKhachHang(phatSinhMaKH());
            } else if (ctrl instanceof gui.dialogs.Dialog_SuaKhachHangController && data != null) {
                ((gui.dialogs.Dialog_SuaKhachHangController) ctrl).setKhachHangData(data);
            } else if (ctrl instanceof gui.dialogs.Dialog_XoaKhachHangController && data != null) {
                ((gui.dialogs.Dialog_XoaKhachHangController) ctrl).setKhachHangData(data);
            }

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle(title);
            stage.setResizable(false);
            stage.showAndWait();

            loadData();
            setupSearch();
        } catch (java.io.IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Giao Diện", "Không thể mở hộp thoại: " + e.getMessage());
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type, content, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
