package gui.dialogs;

import java.sql.Date;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import dao.DAO_PhieuDoiTra;
import entity.PhieuDoiTraView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.stage.Stage;

public class Dialog_ChiTietPhieuDoiTraController {

    @FXML private Label lblMaPhieu;
    @FXML private Label lblNgayDoiTra;
    @FXML private Label lblMaHoaDon;
    @FXML private Label lblNhanVien;
    @FXML private Label lblKhachHang;
    @FXML private Label lblHinhThuc;
    @FXML private Label lblLyDo;
    @FXML private Label lblThuocTra;
    @FXML private Label lblThuocDoi;
    @FXML private Label lblPhiPhat;
    @FXML private Label lblTongTienHoan;

    @FXML private TableView<Object[]> tableChiTiet;
    @FXML private TableColumn<Object[], String> colSTT;
    @FXML private TableColumn<Object[], String> colTenThuoc;
    @FXML private TableColumn<Object[], String> colDonVi;
    @FXML private TableColumn<Object[], String> colLoThuoc;
    @FXML private TableColumn<Object[], String> colHSD;
    @FXML private TableColumn<Object[], String> colSoLuong;
    @FXML private TableColumn<Object[], String> colDonGia;
    @FXML private TableColumn<Object[], String> colThanhTien;

    private final DAO_PhieuDoiTra dao = new DAO_PhieuDoiTra();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DFMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public void setPhieuDoiTra(PhieuDoiTraView pdt) {
        lblMaPhieu.setText(pdt.getMaPhieuDoiTra());
        lblNgayDoiTra.setText(pdt.getNgayDoiTra() != null ? pdt.getNgayDoiTra().toLocalDateTime().format(FMT) : "--");
        lblMaHoaDon.setText(pdt.getMaHoaDon());
        lblNhanVien.setText(pdt.getTenNhanVien());
        lblKhachHang.setText(pdt.getTenKhachHang());
        lblHinhThuc.setText(pdt.getHinhThucXuLyLabel());
        lblLyDo.setText(pdt.getLyDoHienThi().isEmpty() ? "--" : pdt.getLyDoHienThi());
        lblThuocTra.setText(pdt.getThongTinThuocTra().isEmpty() ? "--" : pdt.getThongTinThuocTra());
        lblThuocDoi.setText(pdt.getThongTinThuocDoi().isEmpty() ? "--" : pdt.getThongTinThuocDoi());
        lblPhiPhat.setText(pdt.isDoiSanPham() ? pdt.getMoTaChenhLechDoiSanPham() : String.format("%,.0f VND", pdt.getPhiPhat()));
        colThanhTien.setText(pdt.isDoiSanPham() ? "Giá trị đổi" : "Tiền hoàn");

        setupTable();
        loadChiTiet(pdt);
    }

    private void setupTable() {
        AtomicInteger stt = new AtomicInteger(0);
        colSTT.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(stt.incrementAndGet())));
        colTenThuoc.setCellValueFactory(d -> new SimpleStringProperty((String) d.getValue()[0]));
        colDonVi.setCellValueFactory(d -> new SimpleStringProperty((String) d.getValue()[1]));
        colLoThuoc.setCellValueFactory(d -> new SimpleStringProperty((String) d.getValue()[2]));
        colHSD.setCellValueFactory(d -> {
            Date hsd = (Date) d.getValue()[3];
            return new SimpleStringProperty(hsd != null ? hsd.toLocalDate().format(DFMT) : "--");
        });
        colSoLuong.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf((int) d.getValue()[4])));
        colDonGia.setCellValueFactory(d -> new SimpleStringProperty(String.format("%,.0f VND", (double) d.getValue()[5])));
        colThanhTien.setCellValueFactory(d -> new SimpleStringProperty(String.format("%,.0f VND", (double) d.getValue()[6])));
    }

    private void loadChiTiet(PhieuDoiTraView pdt) {
        List<Object[]> list = dao.getChiTietByMaPhieuDoiTra(pdt.getMaPhieuDoiTra());
        tableChiTiet.setItems(FXCollections.observableArrayList(list));
        double tongTien = list.stream().mapToDouble(r -> (double) r[6]).sum();
        if (pdt.isDoiSanPham()) {
            lblTongTienHoan.setText(pdt.getMoTaChenhLechDoiSanPham());
            return;
        }

        double tongTienHoan = Math.max(0, tongTien - pdt.getPhiPhat());
        lblTongTienHoan.setText(String.format("%,.0f VND", tongTienHoan));
    }

    @FXML
    void handleDong() {
        ((Stage) lblMaPhieu.getScene().getWindow()).close();
    }
}
