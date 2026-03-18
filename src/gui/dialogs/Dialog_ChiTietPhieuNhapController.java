package gui.dialogs;

import dao.DAO_DonNhapHang;
import entity.ChiTietDonNhapHang;
import entity.DonNhapHang;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class Dialog_ChiTietPhieuNhapController {
    @FXML private Label lblMaPhieu;
    @FXML private TableView<ChiTietDonNhapHang> tableChiTiet;
    @FXML private TableColumn<ChiTietDonNhapHang, String> colTenThuoc, colDonVi, colMaLo, colNgaySX, colHanDung;
    @FXML private TableColumn<ChiTietDonNhapHang, Integer> colSLNhan;
    @FXML private TableColumn<ChiTietDonNhapHang, Double> colGiaNhap;

    private DAO_DonNhapHang daoDonNhap = new DAO_DonNhapHang();

    public void setDuLieu(String maDon) {
        lblMaPhieu.setText(maDon);
        
        colTenThuoc.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getThuoc().getTenThuoc()));
        colDonVi.setCellValueFactory(cell -> new SimpleStringProperty(cell.getValue().getDonViQuyDoi().getTenDonVi()));
        colMaLo.setCellValueFactory(new PropertyValueFactory<>("maLo"));
        colSLNhan.setCellValueFactory(new PropertyValueFactory<>("soLuongDaNhan"));
        colGiaNhap.setCellValueFactory(new PropertyValueFactory<>("donGiaDuKien"));
        colNgaySX.setCellValueFactory(new PropertyValueFactory<>("ngaySanXuatTemp"));
        colHanDung.setCellValueFactory(new PropertyValueFactory<>("hanSuDung"));

        // CHÈN THÊM CỘT SL ĐẶT ĐỂ ĐỐI CHIẾU (Tự động không cần sửa FXML)
        TableColumn<ChiTietDonNhapHang, Integer> colSLDat = new TableColumn<>("SL Đặt");
        colSLDat.setCellValueFactory(new PropertyValueFactory<>("soLuongDat"));
        colSLDat.setPrefWidth(80);
        // Thêm vào vị trí thứ 3 (Trước cột SL Nhận)
        if (tableChiTiet.getColumns().size() == 7) {
            tableChiTiet.getColumns().add(3, colSLDat);
        }

        colGiaNhap.setCellFactory(tc -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText((empty || item == null) ? null : String.format("%,.0f đ", item));
            }
        });

        tableChiTiet.setItems(FXCollections.observableArrayList(daoDonNhap.getChiTietByMaDon(maDon)));
    }

    @FXML void handleInPhieuTuDialog(ActionEvent event) {
        DonNhapHang don = daoDonNhap.getDonHangByMa(lblMaPhieu.getText());
        if (don != null) service.Print_HoaDonDatHang.inHoaDon(don, tableChiTiet.getItems());
    }

    @FXML void handleDong(ActionEvent event) { ((Stage) ((Node) event.getSource()).getScene().getWindow()).close(); }
}