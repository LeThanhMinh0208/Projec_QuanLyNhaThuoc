package gui.main;

import dao.DAO_KhachHang;
import entity.KhachHang;
import gui.dialogs.Dialog_ThemKhachHangController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import java.io.IOException;

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

        tableKhachHang.setOnMouseClicked((MouseEvent event) -> {
            if (event.getClickCount() == 2) {
                KhachHang selected = tableKhachHang.getSelectionModel().getSelectedItem();
                if (selected != null) {
                    handleSua(null);
                }
            }
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
        openKhachHangDialog(null);
    }

    @FXML
    void handleSua(ActionEvent event) {
        KhachHang selected = tableKhachHang.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa Chọn", "Vui lòng chọn khách hàng cần sửa trên bảng!");
            return;
        }
        openKhachHangDialog(selected);
    }

    @FXML
    void handleXoa(ActionEvent event) {
        KhachHang selected = tableKhachHang.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa Chọn", "Vui lòng chọn khách hàng cần xóa trên bảng!");
            return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, "Bạn có chắc chắn muốn xóa khách hàng " + selected.getHoTen() + "?", ButtonType.YES, ButtonType.NO);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            if (daoKhachHang.xoaKhachHang(selected.getMaKhachHang())) {
                loadData();
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Xóa khách hàng thành công!");
            } else {
                showAlert(Alert.AlertType.ERROR, "Thất bại", "Khách hàng này có dữ liệu hóa đơn giao dịch, không thể xóa!");
            }
        }
    }

    private void openKhachHangDialog(KhachHang khachHang) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_ThemKhachHang.fxml"));
            Scene scene = new Scene(loader.load());
            
            Dialog_ThemKhachHangController controller = loader.getController();
            controller.setKhachHang(khachHang, (khachHang == null) ? phatSinhMaKH() : null);

            Stage dialogStage = new Stage();
            dialogStage.initModality(Modality.APPLICATION_MODAL);
            dialogStage.initStyle(StageStyle.UTILITY);
            dialogStage.setTitle(khachHang == null ? "Thêm Khách Hàng Mới" : "Sửa Thông Tin Khách Hàng");
            dialogStage.setScene(scene);
            dialogStage.showAndWait();

            KhachHang result = controller.getResultKhachHang();
            if (result != null) {
                if (khachHang == null) {
                    if (daoKhachHang.themKhachHang(result)) {
                        loadData();
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Thêm khách hàng mới thành công!");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Thất bại", "Lỗi CSDL khi thêm khách hàng!");
                    }
                } else {
                    if (daoKhachHang.capNhatKhachHang(result)) {
                        // tableKhachHang.refresh() is handled smoothly by replacing list
                        loadData(); 
                        showAlert(Alert.AlertType.INFORMATION, "Thành công", "Cập nhật thông tin thành công!");
                    } else {
                        showAlert(Alert.AlertType.ERROR, "Thất bại", "Lỗi CSDL khi cập nhật!");
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi Giao Diện", "Không thể mở hộp thoại nhập liệu!");
        }
    }

    private void showAlert(Alert.AlertType type, String title, String content) {
        Alert a = new Alert(type, content, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
