package gui.main;

import dao.DAO_DanhMucDonThuoc;
import entity.DonThuoc;
import gui.dialogs.Dialog_DonThuocController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URL;
import java.util.ResourceBundle;

public class GUI_DanhMucDonThuocController implements Initializable {

    @FXML private TableView<DonThuoc>           tableThuoc;
    @FXML private TableColumn<DonThuoc, String> colMaDon;
    @FXML private TableColumn<DonThuoc, String> colMaHoaDon;
    @FXML private TableColumn<DonThuoc, String> colBacSi;
    @FXML private TableColumn<DonThuoc, String> colChanDoan;
    @FXML private TableColumn<DonThuoc, String> colBenhNhan;
    @FXML private TableColumn<DonThuoc, String> colHinhAnh;
    @FXML private TextField                      txtTimKiem;
    @FXML private ComboBox<String>               cbLocDanhMuc;

    private final DAO_DanhMucDonThuoc dao = new DAO_DanhMucDonThuoc();
    private final ObservableList<DonThuoc> danhSach = FXCollections.observableArrayList();

    @Override
    public void initialize(URL url, ResourceBundle rb) {
        colMaDon.setCellValueFactory(new PropertyValueFactory<>("maDonThuoc"));       
        colBacSi.setCellValueFactory(new PropertyValueFactory<>("tenBacSi"));
        colChanDoan.setCellValueFactory(new PropertyValueFactory<>("chanDoan"));
        colBenhNhan.setCellValueFactory(new PropertyValueFactory<>("thongTinBenhNhan"));
        colHinhAnh.setCellValueFactory(new PropertyValueFactory<>("hinhAnhDon"));
        colMaHoaDon.setCellValueFactory(new PropertyValueFactory<>("maHoaDon"));
        cbLocDanhMuc.setItems(FXCollections.observableArrayList(dao.getDanhSachBacSi()));
        cbLocDanhMuc.getSelectionModel().selectFirst();
        cbLocDanhMuc.setOnAction(e -> locTheoBacSi());

        txtTimKiem.textProperty().addListener((obs, oldVal, newVal) -> {
            if (newVal.trim().isEmpty()) locTheoBacSi();
            else {
                danhSach.setAll(dao.timKiem(newVal.trim()));
                tableThuoc.setItems(danhSach);
            }
        });

        taiDuLieu();
    }

    private void taiDuLieu() {
        danhSach.setAll(dao.getAll());
        tableThuoc.setItems(danhSach);
    }

    private void locTheoBacSi() {
        String selected = cbLocDanhMuc.getValue();
        if (selected == null || selected.equals("Tất cả bác sĩ")) taiDuLieu();
        else {
            danhSach.setAll(dao.timKiem(selected));
            tableThuoc.setItems(danhSach);
        }
    }

    @FXML
    private void handleThem() {
        openDialog(null);
    }

    @FXML
    private void handleSua() {
        DonThuoc selected = tableThuoc.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarn("Vui lòng chọn đơn thuốc cần cập nhật!");
            return;
        }
        openDialog(selected);
    }

    @FXML
    private void handleXoa() {
        DonThuoc selected = tableThuoc.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showWarn("Vui lòng chọn đơn thuốc cần xóa!");
            return;
        }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
            "Bạn có chắc muốn xóa đơn thuốc: " + selected.getMaDonThuoc() + "?",
            ButtonType.YES, ButtonType.NO);
        confirm.setTitle("Xác nhận xóa");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.YES) {
                if (dao.xoa(selected.getMaDonThuoc())) {
                    danhSach.remove(selected);
                    new Alert(Alert.AlertType.INFORMATION, "Xóa thành công!", ButtonType.OK).showAndWait();
                } else {
                    showWarn("Xóa thất bại!");
                }
            }
        });
    }

    private void openDialog(DonThuoc donThuocSua) {
        try {
            FXMLLoader loader = new FXMLLoader(
                getClass().getResource("/gui/dialogs/Dialog_DonThuoc.fxml"));
            Parent root = loader.load();
            Dialog_DonThuocController ctrl = loader.getController();
            ctrl.setOnSuccess(this::taiDuLieu);
            if (donThuocSua != null) ctrl.setDonThuocSua(donThuocSua);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle(donThuocSua == null ? "Thêm Đơn Thuốc" : "Cập Nhật Đơn Thuốc");
            stage.setScene(new Scene(root));
            stage.showAndWait();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void showWarn(String msg) {
        new Alert(Alert.AlertType.WARNING, msg, ButtonType.OK).showAndWait();
    }
}