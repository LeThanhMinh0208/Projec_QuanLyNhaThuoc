package gui.dialogs;

import entity.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import java.text.SimpleDateFormat;

public class Dialog_ChiTietPhieuXuatController {
    @FXML private Label lblTieuDe, lblLoaiPhieu, lblNgayLap, lblDacThu, lblGiaTriDacThu, lblGhiChu;
    @FXML private TableView<ChiTietPhieuXuat> tableChiTiet;
    @FXML private TableColumn<ChiTietPhieuXuat, String> colTenThuoc, colMaLo;
    @FXML private TableColumn<ChiTietPhieuXuat, Integer> colSLXuat;

    public void setDuLieu(PhieuXuat px) {
        lblTieuDe.setText("CHI TIẾT PHIẾU: " + px.getMaPhieuXuat());
        lblNgayLap.setText(new SimpleDateFormat("dd/MM/yyyy").format(px.getNgayXuat()));
        lblGhiChu.setText(px.getGhiChu() != null ? px.getGhiChu() : "Không có");
        
        lblGiaTriDacThu.setText(px.getThongTinDacThu());

        if ("CHUYEN_KHO".equals(px.getLoaiXuat())) {
            lblLoaiPhieu.setText("CHUYỂN KHO");
            lblLoaiPhieu.setStyle("-fx-background-color: #3b82f6;");
            lblDacThu.setText("Kho Nhận:");
        } else if ("TRA_NCC".equals(px.getLoaiXuat())) {
            lblLoaiPhieu.setText("TRẢ NCC");
            lblLoaiPhieu.setStyle("-fx-background-color: #f59e0b;");
            lblDacThu.setText("Nhà Cung Cấp:");
        } else {
            lblLoaiPhieu.setText("XUẤT HỦY");
            lblLoaiPhieu.setStyle("-fx-background-color: #ef4444;");
            lblDacThu.setText("Lý Do Hủy:");
        }

        setupTable();

    }

    private void setupTable() {
        colTenThuoc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLoThuoc().getThuoc().getTenThuoc()));
        colMaLo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLoThuoc().getMaLoThuoc()));
        colSLXuat.setCellValueFactory(new PropertyValueFactory<>("soLuongXuat"));
    }

    @FXML void handleInPhieu(ActionEvent event) { System.out.println("Đang in..."); }
    @FXML void handleDong(ActionEvent event) { ((Stage) ((Node) event.getSource()).getScene().getWindow()).close(); }
}