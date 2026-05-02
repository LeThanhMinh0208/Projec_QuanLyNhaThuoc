package gui.main;

import java.util.Optional;

import dao.DAO_NhanVien;
import entity.NhanVien;
import gui.dialogs.Dialog_SuaNhanVienController;
import gui.dialogs.Dialog_ThemNhanVienController;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GUI_QuanLyNguoiDungController {

    @FXML private TextField txtTimKiem;
    @FXML private TableView<NhanVien> tableNhanVien;
    @FXML private TableColumn<NhanVien, String> colMa, colHoTen, colChucVu, colSdt, colTaiKhoan;
    @FXML private TableColumn<NhanVien, NhanVien> colThaoTac;

    private DAO_NhanVien daoNhanVien = new DAO_NhanVien();
    private ObservableList<NhanVien> dsNhanVien = FXCollections.observableArrayList();

    @FXML
    public void initialize() {
        setupTable();
        loadData();
        setupSearch();
    }

    private void setupTable() {
        colMa.setCellValueFactory(new PropertyValueFactory<>("maNhanVien"));
        colHoTen.setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        colChucVu.setCellValueFactory(new PropertyValueFactory<>("chucVu"));
        colSdt.setCellValueFactory(new PropertyValueFactory<>("sdt"));
        colTaiKhoan.setCellValueFactory(new PropertyValueFactory<>("tenDangNhap"));

        // NÚT KHÓA / MỞ KHÓA TÀI KHOẢN TRỰC TIẾP LÊN DB
        colThaoTac.setCellValueFactory(c -> new SimpleObjectProperty<>(c.getValue()));
        colThaoTac.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button();
            {
                btn.setOnAction(e -> {
                    NhanVien nv = getTableView().getItems().get(getIndex());
                    int trangThaiMoi = nv.getTrangThai() == 1 ? 2 : 1; // 1: Mở, 2: Khóa
                    if (daoNhanVien.capNhatTrangThai(nv.getMaNhanVien(), trangThaiMoi)) {
                        nv.setTrangThai(trangThaiMoi);
                        updateItem(nv, false); // Cập nhật lại UI nút ngay lập tức
                    }
                });
            }
            @Override protected void updateItem(NhanVien nv, boolean empty) {
                super.updateItem(nv, empty);
                if (empty || nv == null) {
                    setGraphic(null);
                } else {
                    if (nv.getTrangThai() == 1) { // Đang Mở -> Hiện nút Khóa Đỏ
                        btn.setText("Khóa");
                        btn.getStyleClass().setAll("button", "bh-btn-danger");
                    } else if (nv.getTrangThai() == 2) { // Bị Khóa -> Hiện nút Mở Xanh
                        btn.setText("Mở Khóa");
                        btn.getStyleClass().setAll("button", "bh-btn-success");
                    }
                    setGraphic(btn);
                }
            }
        });

        // XỬ LÝ DOUBLE CLICK ĐỂ MỞ FORM SỬA
        tableNhanVien.setRowFactory(tv -> {
            TableRow<NhanVien> row = new TableRow<>();
            row.setOnMouseClicked((MouseEvent event) -> {
                if (event.getClickCount() == 2 && !row.isEmpty()) {
                    moFormCapNhat(row.getItem());
                }
            });
            return row;
        });
    }

    public void loadData() {
        dsNhanVien.clear();
        dsNhanVien.addAll(daoNhanVien.getChiNhanVien());
        tableNhanVien.setItems(dsNhanVien);
    }

    private void setupSearch() {
        FilteredList<NhanVien> filteredData = new FilteredList<>(dsNhanVien, p -> true);
        txtTimKiem.textProperty().addListener((obs, oldV, newV) -> {
            filteredData.setPredicate(nv -> {
                if (newV == null || newV.isEmpty()) {
					return true;
				}
                String key = newV.toLowerCase();
                return nv.getHoTen().toLowerCase().contains(key) ||
                       nv.getMaNhanVien().toLowerCase().contains(key) ||
                       nv.getSdt().contains(key) ||
                       nv.getTenDangNhap().toLowerCase().contains(key);
            });
        });
        SortedList<NhanVien> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableNhanVien.comparatorProperty());
        tableNhanVien.setItems(sortedData);
    }

    @FXML void handleLamMoiTK(ActionEvent event) { txtTimKiem.clear(); }

    @FXML void handleThem(ActionEvent event) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_ThemNhanVien.fxml"));
            Parent root = loader.load();
            Dialog_ThemNhanVienController controller = loader.getController();
            controller.setParentController(this);
            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Thêm Người Dùng Mới");
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML void handleSua(ActionEvent event) {
        NhanVien selected = tableNhanVien.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng chọn nhân viên cần cập nhật!").show();
            return;
        }
        moFormCapNhat(selected);
    }

    private void moFormCapNhat(NhanVien nvCanSua) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_SuaNhanVien.fxml"));
            Parent root = loader.load();
            Dialog_SuaNhanVienController controller = loader.getController();
            controller.setParentController(this);
            controller.setNhanVien(nvCanSua);

            Stage stage = new Stage();
            stage.setScene(new Scene(root));
            stage.setTitle("Cập Nhật Người Dùng: " + nvCanSua.getHoTen());
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.showAndWait();
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML void handleXoa(ActionEvent event) {
        NhanVien selected = tableNhanVien.getSelectionModel().getSelectedItem();
        if (selected == null) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng chọn nhân viên cần xóa!").show();
            return;
        }

        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận xóa mềm");
        alert.setHeaderText("Bạn có chắc chắn muốn xóa nhân viên: " + selected.getHoTen() + " ?");
        alert.setContentText("Nhân viên này sẽ bị ẩn khỏi danh sách. Bạn có thể khôi phục lại khi thêm mới cùng Tên và SĐT.");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            if (daoNhanVien.xoaMemNhanVien(selected.getMaNhanVien())) {
                new Alert(Alert.AlertType.INFORMATION, "Đã đưa nhân viên vào danh sách Xóa!").show();
                loadData();
            } else {
                new Alert(Alert.AlertType.ERROR, "Xóa thất bại!").show();
            }
        }
    }
}