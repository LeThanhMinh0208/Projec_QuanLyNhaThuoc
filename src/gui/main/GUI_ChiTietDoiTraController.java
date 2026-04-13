package gui.main;

import java.sql.Date;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import dao.DAO_PhieuDoiTra;
import entity.ChiTietDoiTra;
import entity.ChiTietDoiTraView;
import entity.HinhThucDoiTra;
import entity.HoaDon;
import entity.HoaDonView;
import entity.NhanVien;
import entity.PhieuDoiTra;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import utils.DoiTraSession;
import utils.SceneUtils;
import utils.UserSession;

public class GUI_ChiTietDoiTraController {

    @FXML private Label lblMaHD;
    @FXML private Label lblNgayLap;
    @FXML private Label lblKhachHang;
    @FXML private Label lblNhanVien;
    @FXML private Label lblTongHoaDon;
    @FXML private Label lblHinhThuc;
    @FXML private Label lblTongDong;
    @FXML private Label lblTongTienHoan;

    @FXML private TableView<ChiTietDoiTraView> tableCoTheDoiTra;
    @FXML private TableColumn<ChiTietDoiTraView, String> colHangTenThuoc;
    @FXML private TableColumn<ChiTietDoiTraView, String> colHangDonVi;
    @FXML private TableColumn<ChiTietDoiTraView, String> colHangLo;
    @FXML private TableColumn<ChiTietDoiTraView, String> colHangHSD;
    @FXML private TableColumn<ChiTietDoiTraView, String> colHangDaMua;
    @FXML private TableColumn<ChiTietDoiTraView, String> colHangDaTra;
    @FXML private TableColumn<ChiTietDoiTraView, String> colHangConLai;
    @FXML private TableColumn<ChiTietDoiTraView, String> colHangDonGia;
    @FXML private TableColumn<ChiTietDoiTraView, String> colHangTrangThai;

    @FXML private TableView<ChiTietDoiTraTam> tableChiTietDoiTra;
    @FXML private TableColumn<ChiTietDoiTraTam, String> colCTTenThuoc;
    @FXML private TableColumn<ChiTietDoiTraTam, String> colCTDonVi;
    @FXML private TableColumn<ChiTietDoiTraTam, String> colCTLo;
    @FXML private TableColumn<ChiTietDoiTraTam, String> colCTSoLuong;
    @FXML private TableColumn<ChiTietDoiTraTam, String> colCTTinhTrang;
    @FXML private TableColumn<ChiTietDoiTraTam, String> colCTThanhTien;
    @FXML private TableColumn<ChiTietDoiTraTam, Void> colCTHanhDong;

    @FXML private TextField txtSoLuongTra;
    @FXML private TextField txtPhiPhat;
    @FXML private TextField txtTinhTrang;
    @FXML private TextArea txtLyDo;
    @FXML private ComboBox<HinhThucDoiTra> cbHinhThucXuLy;

    private final DAO_PhieuDoiTra daoPhieuDoiTra = new DAO_PhieuDoiTra();
    private final ObservableList<ChiTietDoiTraView> dsChiTietHoaDon = FXCollections.observableArrayList();
    private final ObservableList<ChiTietDoiTraTam> dsTam = FXCollections.observableArrayList();
    private final DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private HoaDonView hoaDon;

    @FXML
    public void initialize() {
        cbHinhThucXuLy.setItems(FXCollections.observableArrayList(HinhThucDoiTra.values()));
        cbHinhThucXuLy.setValue(HinhThucDoiTra.HOAN_TIEN);
        txtPhiPhat.setText("0");
        setupAvailableTable();
        setupReturnTable();
        hoaDon = DoiTraSession.getHoaDonDangXuLy();
        renderHoaDon();
    }

    private void renderHoaDon() {
        if (hoaDon == null) {
            showAlert(Alert.AlertType.WARNING, "Chưa chọn hóa đơn đổi trả.");
            return;
        }

        lblMaHD.setText(hoaDon.getMaHoaDon());
        lblNgayLap.setText(hoaDon.getNgayLap() != null ? hoaDon.getNgayLap().toLocalDateTime().format(dateTimeFmt) : "--");
        lblKhachHang.setText(hoaDon.getTenKhachHang() + " | " + hoaDon.getSdt());
        lblNhanVien.setText(hoaDon.getTenNhanVien());
        lblTongHoaDon.setText(String.format("%,.0f VND", hoaDon.getTongSauVAT()));
        lblHinhThuc.setText(hoaDon.getHinhThucLabel());

        dsChiTietHoaDon.setAll(daoPhieuDoiTra.getChiTietCoTheDoiTra(hoaDon.getMaHoaDon()));
        tableCoTheDoiTra.setItems(dsChiTietHoaDon);
        tableCoTheDoiTra.refresh();
        capNhatTong();
    }

    private void setupAvailableTable() {
        colHangTenThuoc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenThuoc()));
        colHangDonVi.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenDonVi()));
        colHangLo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaLoThuoc()));
        colHangHSD.setCellValueFactory(d -> new SimpleStringProperty(formatDate(d.getValue().getHanSuDung())));
        colHangDaMua.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getSoLuongDaMua())));
        colHangDaTra.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getSoLuongDaTra())));
        colHangConLai.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getSoLuongConLai())));
        colHangDonGia.setCellValueFactory(d -> new SimpleStringProperty(String.format("%,.0f VND", d.getValue().getDonGia())));
        colHangTrangThai.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getSoLuongConLai() > 0 ? "Có thể đổi trả" : "Đã trả hết"));
    }

    private void setupReturnTable() {
        colCTTenThuoc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().tenThuoc));
        colCTDonVi.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().tenDonVi));
        colCTLo.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().maLoThuoc));
        colCTSoLuong.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().soLuongTra)));
        colCTTinhTrang.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().tinhTrang));
        colCTThanhTien.setCellValueFactory(d -> new SimpleStringProperty(String.format("%,.0f VND", d.getValue().thanhTienHoan)));
        colCTHanhDong.setCellFactory(col -> new TableCell<>() {
            private final Button btnXoa = new Button("Xóa");
            {
                btnXoa.setStyle("-fx-background-color:#dc2626;-fx-text-fill:white;-fx-font-size:12px;-fx-padding:4 10;-fx-cursor:hand;");
                btnXoa.setOnAction(e -> xoaDongTam(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnXoa);
            }
        });
        tableChiTietDoiTra.setItems(dsTam);
    }

    @FXML
    void handleThemChiTiet(ActionEvent event) {
        ChiTietDoiTraView selected = tableCoTheDoiTra.getSelectionModel().getSelectedItem();
        if (selected == null) {
            showAlert(Alert.AlertType.WARNING, "Chọn một mặt hàng trong bảng chi tiết hóa đơn.");
            return;
        }
        if (selected.getSoLuongConLai() <= 0) {
            showAlert(Alert.AlertType.WARNING, "Mặt hàng này đã được đổi trả hết.");
            return;
        }

        int soLuongTra;
        try {
            soLuongTra = Integer.parseInt(txtSoLuongTra.getText().trim());
        } catch (Exception e) {
            showAlert(Alert.AlertType.WARNING, "Số lượng trả không hợp lệ.");
            return;
        }

        if (soLuongTra <= 0 || soLuongTra > selected.getSoLuongConLai()) {
            showAlert(Alert.AlertType.WARNING, "Số lượng trả phải > 0 va <= số lượng còn lại.");
            return;
        }

        String tinhTrang = txtTinhTrang.getText() != null ? txtTinhTrang.getText().trim() : "";
        if (tinhTrang.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Nhập tình trạng thuốc.");
            return;
        }

        ChiTietDoiTraTam item = new ChiTietDoiTraTam(selected, soLuongTra, tinhTrang);
        dsTam.add(item);
        selected.setSoLuongDaTra(selected.getSoLuongDaTra() + soLuongTra);
        tableCoTheDoiTra.refresh();
        capNhatTong();
        txtSoLuongTra.clear();
        txtTinhTrang.clear();
    }

    @FXML
    void handleLuu(ActionEvent event) {
        if (hoaDon == null) {
            showAlert(Alert.AlertType.ERROR, "Không có hóa đơn để lập phiếu đổi trả.");
            return;
        }
        if (dsTam.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Chưa có phiếu đổi trả nào.");
            return;
        }
        String lyDo = txtLyDo.getText() != null ? txtLyDo.getText().trim() : "";
        if (lyDo.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Nhập lý do đổi trả.");
            return;
        }

        double phiPhat;
        try {
            phiPhat = Double.parseDouble(txtPhiPhat.getText().trim());
        } catch (Exception e) {
            showAlert(Alert.AlertType.WARNING, "Phí phạt không hợp lệ.");
            return;
        }
        if (phiPhat < 0) {
            showAlert(Alert.AlertType.WARNING, "Phí phạt không được âm.");
            return;
        }

        NhanVien nhanVien = UserSession.getInstance().getUser();
        if (nhanVien == null) {
            nhanVien = GUI_TrangChuController.getNhanVienDangNhap();
        }
        if (nhanVien == null) {
            showAlert(Alert.AlertType.ERROR, "Không xác định được nhân viên.");
            return;
        }

        HoaDon hd = new HoaDon();
        hd.setMaHoaDon(hoaDon.getMaHoaDon());

        PhieuDoiTra pdt = new PhieuDoiTra();
        pdt.setMaPhieuDoiTra(daoPhieuDoiTra.generateMaPhieuDoiTra());
        pdt.setHoaDon(hd);
        pdt.setNhanVien(nhanVien);
        pdt.setNgayDoiTra(new java.util.Date());
        pdt.setLyDo(lyDo);
        pdt.setHinhThucXuLy(cbHinhThucXuLy.getValue());
        pdt.setPhiPhat(phiPhat);

        List<ChiTietDoiTra> chiTiet = new ArrayList<>();
        for (ChiTietDoiTraTam item : dsTam) {
            ChiTietDoiTra ct = new ChiTietDoiTra();
            ct.setMaPhieuDoiTra(pdt.getMaPhieuDoiTra());
            ct.setMaQuyDoi(item.maQuyDoi);
            ct.setMaLoThuoc(item.maLoThuoc);
            ct.setSoLuong(item.soLuongTra);
            ct.setTinhTrang(item.tinhTrang);
            chiTiet.add(ct);
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Xác nhận lưu phiếu đổi trả " + pdt.getMaPhieuDoiTra() + "?",
                ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> rs = confirm.showAndWait();
        if (rs.isEmpty() || rs.get() != ButtonType.YES) {
            return;
        }

        boolean ok = daoPhieuDoiTra.lapPhieuDoiTra(pdt, chiTiet);
        if (ok) {
            showAlert(Alert.AlertType.INFORMATION, "Lập phiếu đổi trả thành công. Mã phiếu: " + pdt.getMaPhieuDoiTra());
            DoiTraSession.clear();
            SceneUtils.switchPage("/gui/main/GUI_XuLyDoiTra.fxml");
        } else {
            showAlert(Alert.AlertType.ERROR, "Lập phiếu đổi trả thất bại.");
        }
    }

    @FXML
    void handleQuayLai(ActionEvent event) {
        SceneUtils.switchPage("/gui/main/GUI_XuLyDoiTra.fxml");
    }

    private void xoaDongTam(ChiTietDoiTraTam item) {
        dsTam.remove(item);
        for (ChiTietDoiTraView row : dsChiTietHoaDon) {
            if (row.getMaQuyDoi().equals(item.maQuyDoi) && row.getMaLoThuoc().equals(item.maLoThuoc)) {
                row.setSoLuongDaTra(Math.max(0, row.getSoLuongDaTra() - item.soLuongTra));
                break;
            }
        }
        tableCoTheDoiTra.refresh();
        capNhatTong();
    }

    private void capNhatTong() {
        lblTongDong.setText(String.valueOf(dsTam.size()));
        double tong = dsTam.stream().mapToDouble(item -> item.thanhTienHoan).sum();
        lblTongTienHoan.setText(String.format("%,.0f VND", tong));
    }

    private String formatDate(Date date) {
        return date != null ? date.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "--";
    }

    private void showAlert(Alert.AlertType type, String message) {
        Alert alert = new Alert(type, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    public static class ChiTietDoiTraTam {
        private final String maQuyDoi;
        private final String maLoThuoc;
        private final String tenThuoc;
        private final String tenDonVi;
        private final int soLuongTra;
        private final String tinhTrang;
        private final double thanhTienHoan;

        public ChiTietDoiTraTam(ChiTietDoiTraView source, int soLuongTra, String tinhTrang) {
            this.maQuyDoi = source.getMaQuyDoi();
            this.maLoThuoc = source.getMaLoThuoc();
            this.tenThuoc = source.getTenThuoc();
            this.tenDonVi = source.getTenDonVi();
            this.soLuongTra = soLuongTra;
            this.tinhTrang = tinhTrang;
            this.thanhTienHoan = source.getDonGia() * soLuongTra;
        }
    }
}
