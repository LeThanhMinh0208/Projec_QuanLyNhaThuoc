package gui.dialogs;

import java.sql.Date;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import dao.DAO_HoaDon;
import entity.HoaDonView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;

public class Dialog_ChiTietHoaDonController {

    @FXML private Label lblMaHD, lblNgayLap, lblNhanVien, lblHinhThuc;
    @FXML private Label lblKhachHang, lblVAT, lblGhiChu;
    @FXML private TableView<Object[]> tableChiTiet;
    @FXML private TableColumn<Object[], String> colSTT, colTenThuoc, colDonVi, colLoThuoc, colHSD, colSoLuong, colDonGia, colThanhTien;
    @FXML private Label lblTamTinh, lblVATTien, lblTong;

    private final DAO_HoaDon dao = new DAO_HoaDon();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DFMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void setHoaDon(HoaDonView hd) {
        lblMaHD.setText(hd.getMaHoaDon());
        lblNgayLap.setText(hd.getNgayLap() != null ? hd.getNgayLap().toLocalDateTime().format(FMT) : "—");
        lblNhanVien.setText(hd.getTenNhanVien());
        lblHinhThuc.setText(hd.getHinhThucLabel());
        lblKhachHang.setText(hd.getTenKhachHang() + "  |  " + hd.getSdt());
        lblVAT.setText(String.format("%.0f%%", hd.getThueVAT()));
        lblGhiChu.setText(hd.getGhiChu() != null ? hd.getGhiChu() : "—");

        setupTable();
        loadChiTiet(hd.getMaHoaDon(), hd.getTamTinh(), hd.getThueVAT(), hd.getTongSauVAT());
    }

    private void setupTable() {
        AtomicInteger stt = new AtomicInteger(0);
        colSTT      .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(stt.incrementAndGet())));
        colTenThuoc .setCellValueFactory(d -> new SimpleStringProperty((String)  d.getValue()[0]));
        colDonVi    .setCellValueFactory(d -> new SimpleStringProperty((String)  d.getValue()[1]));
        colLoThuoc  .setCellValueFactory(d -> new SimpleStringProperty((String)  d.getValue()[2]));
        colHSD      .setCellValueFactory(d -> {
            Date hsd = (Date) d.getValue()[3];
            return new SimpleStringProperty(hsd != null ? hsd.toLocalDate().format(DFMT) : "—");
        });
        colSoLuong  .setCellValueFactory(d -> new SimpleStringProperty(String.valueOf((int) d.getValue()[4])));
        colDonGia   .setCellValueFactory(d -> new SimpleStringProperty(String.format("%,.0f ₫", (double) d.getValue()[5])));
        colThanhTien.setCellValueFactory(d -> new SimpleStringProperty(String.format("%,.0f ₫", (double) d.getValue()[6])));
    }

    private void loadChiTiet(String maHoaDon, double tamTinh, double thueVAT, double tongSauVAT) {
        List<Object[]> list = dao.getChiTietByMaHoaDon(maHoaDon);
        ObservableList<Object[]> items = FXCollections.observableArrayList(list);
        tableChiTiet.setItems(items);

        double vatTien = tongSauVAT - tamTinh;
        lblTamTinh.setText(String.format("%,.0f ₫", tamTinh));
        lblVATTien.setText(String.format("+ %,.0f ₫  (%.0f%%)", vatTien, thueVAT));
        lblTong.setText(String.format("%,.0f ₫", tongSauVAT));
    }

    @FXML
    void handleIn(ActionEvent event) {
        Alert a = new Alert(Alert.AlertType.INFORMATION,
                "Chức năng in hóa đơn chưa được implement.\n(Placeholder)", ButtonType.OK);
        a.setTitle("In Hóa Đơn");
        a.setHeaderText(null);
        a.showAndWait();
    }

    @FXML
    void handleDong(ActionEvent event) {
        ((Stage) lblMaHD.getScene().getWindow()).close();
    }
}
