package gui.dialogs;

import java.sql.Date;
import java.time.format.DateTimeFormatter;
import java.util.List;

import dao.DAO_HoaDon;
import dao.DAO_PhieuDoiTra;
import entity.HoaDonView;
import entity.PhieuDoiTraView;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import utils.SceneUtils;

public class Dialog_ChiTietPhieuDoiTraController {

    @FXML private javafx.scene.control.ScrollPane scrollPane;
    @FXML private Label lblMaPhieu;
    @FXML private Label lblNgayDoiTra;
    @FXML private Label lblMaHoaDon;
    @FXML private Label lblNhanVien;
    @FXML private Label lblKhachHang;
    @FXML private Label lblHinhThuc;
    @FXML private Label lblLyDo;
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

    @FXML private VBox boxKhachNhan;
    @FXML private TableView<Object[]> tableThuocDoi;
    @FXML private TableColumn<Object[], String> colDoiSTT;
    @FXML private TableColumn<Object[], String> colDoiTen;
    @FXML private TableColumn<Object[], String> colDoiDonVi;
    @FXML private TableColumn<Object[], String> colDoiSoLuong;
    @FXML private TableColumn<Object[], String> colDoiDonGia;
    @FXML private TableColumn<Object[], String> colDoiThanhTien;

    private final DAO_PhieuDoiTra dao = new DAO_PhieuDoiTra();
    private final DAO_HoaDon daoHoaDon = new DAO_HoaDon();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private static final DateTimeFormatter DFMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    private PhieuDoiTraView currentPdt;
    private List<Object[]> listChiTiet;
    private List<Object[]> listThuocDoi;
    private double thueVAT = 0; // Lưu tỷ lệ thuế để tính toán hiển thị

    public void setPhieuDoiTra(PhieuDoiTraView pdt) {
        this.currentPdt = pdt;

        // Truy vấn lấy thueVAT từ HoaDon gốc (Xử lý null-safe)
        HoaDonView hd = daoHoaDon.getHoaDonViewByMa(pdt.getMaHoaDon());
        this.thueVAT = (hd != null) ? hd.getThueVAT() : 0;

        lblMaPhieu.setText(pdt.getMaPhieuDoiTra());
        lblNgayDoiTra.setText(pdt.getNgayDoiTra() != null ? pdt.getNgayDoiTra().toLocalDateTime().format(FMT) : "--");
        lblMaHoaDon.setText(pdt.getMaHoaDon());
        lblNhanVien.setText(pdt.getTenNhanVien());
        lblKhachHang.setText(pdt.getTenKhachHang());
        lblHinhThuc.setText(pdt.getHinhThucXuLyLabel());
        lblLyDo.setText(pdt.getLyDoHienThi() != null && !pdt.getLyDoHienThi().isEmpty() ? pdt.getLyDoHienThi() : "--");
        lblPhiPhat.setText(pdt.isDoiSanPham() ? pdt.getMoTaChenhLechDoiSanPham() : String.format("%,.0f VND", pdt.getPhiPhat()));
        colThanhTien.setText(pdt.isDoiSanPham() ? "Giá trị đổi" : "Tiền hoàn");

        setupTable();
        loadChiTiet(pdt);
    }

    private void setupTable() {
        colSTT.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });
        colTenThuoc.setCellValueFactory(d -> new SimpleStringProperty((String) d.getValue()[0]));
        colDonVi.setCellValueFactory(d -> new SimpleStringProperty((String) d.getValue()[1]));
        colLoThuoc.setCellValueFactory(d -> new SimpleStringProperty((String) d.getValue()[2]));
        colHSD.setCellValueFactory(d -> {
            Date hsd = (Date) d.getValue()[3];
            return new SimpleStringProperty(hsd != null ? hsd.toLocalDate().format(DFMT) : "--");
        });
        colSoLuong.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf((int) d.getValue()[4])));

        // Bảng 1 - Cột Đơn giá: Hiển thị giá đã bao gồm VAT
        colDonGia.setCellValueFactory(d -> {
            double donGiaGoc = (double) d.getValue()[5];
            double donGiaVAT = donGiaGoc * (1 + thueVAT / 100.0);
            return new SimpleStringProperty(String.format("%,.0f VND", donGiaVAT));
        });

        // Bảng 1 - Cột Thành tiền: Hiển thị giá trị đã bao gồm VAT
        colThanhTien.setCellValueFactory(d -> {
            double thanhTienGoc = (double) d.getValue()[6];
            double thanhTienVAT = thanhTienGoc * (1 + thueVAT / 100.0);
            return new SimpleStringProperty(String.format("%,.0f VND", thanhTienVAT));
        });

        colDoiSTT.setCellFactory(col -> new javafx.scene.control.TableCell<>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty ? null : String.valueOf(getIndex() + 1));
            }
        });
        colDoiTen.setCellValueFactory(d -> new SimpleStringProperty((String) d.getValue()[1]));
        colDoiDonVi.setCellValueFactory(d -> new SimpleStringProperty((String) d.getValue()[2]));
        colDoiSoLuong.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue()[3])));
        colDoiDonGia.setCellValueFactory(d -> new SimpleStringProperty(String.format("%,.0f VND", Double.valueOf(d.getValue()[4].toString()))));
        colDoiThanhTien.setCellValueFactory(d -> {
            double donGia = Double.valueOf(d.getValue()[4].toString());
            int sl = Integer.valueOf(d.getValue()[3].toString());
            return new SimpleStringProperty(String.format("%,.0f VND", donGia * sl));
        });
    }

    private void loadChiTiet(PhieuDoiTraView pdt) {
        listChiTiet = dao.getChiTietByMaPhieuDoiTra(pdt.getMaPhieuDoiTra());
        tableChiTiet.setItems(FXCollections.observableArrayList(listChiTiet));

        // Tính tổng tiền bảng 1 (đã bao gồm VAT) để hiển thị ở summary nếu cần
        double tongTienB1 = listChiTiet.stream().mapToDouble(r -> (double) r[6]).sum() * (1 + thueVAT / 100.0);

        String dsThuocDoi = pdt.getDanhSachThuocDoi();
        listThuocDoi = new java.util.ArrayList<>();
        if (dsThuocDoi != null && !dsThuocDoi.isEmpty()) {
            for (String row : dsThuocDoi.split(";")) {
                if (!row.isBlank()) {
                    String[] parts = row.split("\\|");
                    if (parts.length >= 5) {
                        listThuocDoi.add(parts); // maQuyDoi, tenThuoc, tenDonVi, soLuong, donGia
                    }
                }
            }
        }

        if (!listThuocDoi.isEmpty()) {
            boxKhachNhan.setManaged(true);
            boxKhachNhan.setVisible(true);
            tableThuocDoi.setItems(FXCollections.observableArrayList(listThuocDoi));
        }

        if (pdt.isDoiSanPham()) {
            lblTongTienHoan.setText(pdt.getMoTaChenhLechDoiSanPham());
        } else {
            // Trường hợp HOAN_TIEN: trừ phi phạt từ tổng tiền đã VAT
            double tongTienHoan = Math.max(0, tongTienB1 - pdt.getPhiPhat());
            lblTongTienHoan.setText(String.format("%,.0f VND", tongTienHoan));
        }

        javafx.application.Platform.runLater(() -> {
            if (scrollPane != null) {
                scrollPane.setVvalue(0.0);
                scrollPane.setHvalue(0.0);
            }
        });
    }

    @FXML
    void handleInPhieu() {
        try {
            utils.PhieuDoiTraPdfExporter.xuatPDF(currentPdt, listChiTiet, listThuocDoi);
            SceneUtils.showAlert(Alert.AlertType.INFORMATION, "Đã xuất phiếu đổi trả thành công!");
        } catch (Exception e) {
            e.printStackTrace();
            SceneUtils.showAlert(Alert.AlertType.ERROR, "Lỗi khi xuất PDF: " + e.getMessage());
        }
    }

    @FXML
    void handleDong() {
        ((Stage) lblMaPhieu.getScene().getWindow()).close();
    }
}
