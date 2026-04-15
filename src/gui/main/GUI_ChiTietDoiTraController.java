package gui.main;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import dao.DAO_BangGia;
import dao.DAO_DonViQuyDoi;
import dao.DAO_PhieuDoiTra;
import entity.ChiTietDoiTra;
import entity.ChiTietDoiTraView;
import entity.DonViQuyDoi;
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
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import utils.DoiTraSession;
import utils.SceneUtils;
import utils.UserSession;

public class GUI_ChiTietDoiTraController {

    @FXML private javafx.scene.control.Label lblMaHD;
    @FXML private javafx.scene.control.Label lblNgayLap;
    @FXML private javafx.scene.control.Label lblKhachHang;
    @FXML private javafx.scene.control.Label lblNhanVien;
    @FXML private javafx.scene.control.Label lblTongHoaDon;
    @FXML private javafx.scene.control.Label lblHinhThuc;
    @FXML private javafx.scene.control.Label lblTongDong;
    @FXML private javafx.scene.control.Label lblTongTienHoan;
    @FXML private javafx.scene.control.Label lblThuocDoi;
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
    @FXML private TableColumn<ChiTietDoiTraTam, String> colCTThanhTien;
    @FXML private TableColumn<ChiTietDoiTraTam, Void> colCTHanhDong;

    @FXML private Spinner<Integer> spSoLuongTra;
    @FXML private TextField txtPhiPhat;
    @FXML private TextArea txtLyDo;
    @FXML private ComboBox<HinhThucDoiTra> cbHinhThucXuLy;

    private final DAO_BangGia daoBangGia = new DAO_BangGia();
    private final DAO_DonViQuyDoi daoDonViQuyDoi = new DAO_DonViQuyDoi();
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
        txtPhiPhat.setEditable(false);
        txtPhiPhat.setFocusTraversable(false);

        setupSoLuongSpinner();
        setupAvailableTable();
        setupReturnTable();

        tableCoTheDoiTra.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, newVal) -> {
                    capNhatSpinnerSoLuong(newVal);
                    capNhatTong();
                });

        spSoLuongTra.valueProperty().addListener((obs, oldVal, newVal) -> capNhatTong());

        hoaDon = DoiTraSession.getHoaDonDangXuLy();
        renderHoaDon();
        khoiPhucDuLieuTam();
        cbHinhThucXuLy.valueProperty().addListener((obs, oldVal, newVal) -> xuLyThayDoiHinhThuc(oldVal, newVal));
        colCTThanhTien.setText(cbHinhThucXuLy.getValue() == HinhThucDoiTra.DOI_SAN_PHAM ? "Giá trị trả" : "Tiền hoàn");
        capNhatTong();
    }

    private void setupSoLuongSpinner() {
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
                new SpinnerValueFactory.IntegerSpinnerValueFactory(0, 0, 0);
        spSoLuongTra.setValueFactory(valueFactory);
        spSoLuongTra.setEditable(true);
        spSoLuongTra.setDisable(true);

        UnaryOperator<TextFormatter.Change> integerFilter = change ->
                change.getControlNewText().matches("\\d*") ? change : null;
        spSoLuongTra.getEditor().setTextFormatter(new TextFormatter<>(integerFilter));

        spSoLuongTra.focusedProperty().addListener((obs, oldVal, focused) -> {
            if (!focused) {
                commitSpinnerEditorText();
            }
        });
        spSoLuongTra.getEditor().setOnAction(event -> commitSpinnerEditorText());
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
        capNhatSpinnerSoLuong(tableCoTheDoiTra.getSelectionModel().getSelectedItem());
        capNhatTong();
    }

    private void khoiPhucDuLieuTam() {
        txtLyDo.setText(DoiTraSession.getLyDoTam());
        txtPhiPhat.setText(DoiTraSession.getPhiPhatTam());
        cbHinhThucXuLy.setValue(DoiTraSession.getHinhThucXuLyTam());

        dsTam.clear();
        for (DoiTraSession.ChiTietDoiTraTamData item : DoiTraSession.getDsChiTietTam()) {
            dsTam.add(new ChiTietDoiTraTam(item));
            for (ChiTietDoiTraView row : dsChiTietHoaDon) {
                if (row.getMaQuyDoi().equals(item.getMaQuyDoi()) && row.getMaLoThuoc().equals(item.getMaLoThuoc())) {
                    row.setSoLuongDaTra(row.getSoLuongDaTra() + item.getSoLuongTra());
                    break;
                }
            }
        }

        tableCoTheDoiTra.refresh();
        tableChiTietDoiTra.refresh();
        capNhatTong();
        capNhatThongTinThuocDoi();
    }

    private void luuDuLieuTamVaoSession() {
        DoiTraSession.setLyDoTam(txtLyDo.getText());
        DoiTraSession.setPhiPhatTam(txtPhiPhat.getText());
        DoiTraSession.setHinhThucXuLyTam(cbHinhThucXuLy.getValue());

        List<DoiTraSession.ChiTietDoiTraTamData> data = new ArrayList<>();
        for (ChiTietDoiTraTam item : dsTam) {
            data.add(item.toSessionData());
        }
        DoiTraSession.setDsChiTietTam(data);
    }

    private void xuLyThayDoiHinhThuc(HinhThucDoiTra oldVal, HinhThucDoiTra newVal) {
        if (newVal == null || newVal == oldVal) {
            return;
        }

        colCTThanhTien.setText(newVal == HinhThucDoiTra.DOI_SAN_PHAM ? "Giá trị đổi" : "Tiền hoàn");
        capNhatTong();
        capNhatThongTinThuocDoi();

        if (newVal == HinhThucDoiTra.DOI_SAN_PHAM) {
            if (DoiTraSession.getThuocDoiDaChon() != null) {
                DoiTraSession.setDangChonThuocDoi(false);
                return;
            }
            luuDuLieuTamVaoSession();
            DoiTraSession.setDangChonThuocDoi(true);
            SceneUtils.switchPage("/gui/main/GUI_DanhMucThuoc.fxml");
            return;
        }
    }

    private void capNhatThongTinThuocDoi() {
        if (lblThuocDoi == null) {
            return;
        }

        if (cbHinhThucXuLy.getValue() != HinhThucDoiTra.DOI_SAN_PHAM) {
            lblThuocDoi.setText("Không áp dụng");
            return;
        }

        if (DoiTraSession.getThuocDoiDaChon() == null) {
            lblThuocDoi.setText("Chưa chọn thuốc đổi");
            return;
        }

        double giaTriThuocDoi = tinhGiaTriThuocDoi();
        lblThuocDoi.setText(DoiTraSession.getThuocDoiDaChon().getTenThuoc() + " (" +
                DoiTraSession.getThuocDoiDaChon().getMaThuoc() + ") - " +
                String.format("%,.0f VND", giaTriThuocDoi));
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
            capNhatSpinnerSoLuong(selected);
            return;
        }

        int soLuongTra;
        try {
            soLuongTra = parseSoLuongDangChon();
        } catch (Exception e) {
            showAlert(Alert.AlertType.WARNING, "Số lượng trả không hợp lệ.");
            capNhatSpinnerSoLuong(selected);
            return;
        }

        if (soLuongTra <= 0 || soLuongTra > selected.getSoLuongConLai()) {
            showAlert(Alert.AlertType.WARNING, "Số lượng trả phải > 0 và <= số lượng còn lại.");
            capNhatSpinnerSoLuong(selected);
            return;
        }

        ChiTietDoiTraTam item = new ChiTietDoiTraTam(selected, soLuongTra);
        dsTam.add(item);
        selected.setSoLuongDaTra(selected.getSoLuongDaTra() + soLuongTra);
        tableCoTheDoiTra.refresh();
        capNhatTong();
        capNhatSpinnerSoLuong(selected);
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

        if (cbHinhThucXuLy.getValue() == HinhThucDoiTra.DOI_SAN_PHAM) {
            if (DoiTraSession.getThuocDoiDaChon() == null) {
                showAlert(Alert.AlertType.WARNING, "Vui lòng chọn thuốc đổi trước khi lưu phiếu");
                return;
            }
            phiPhat = tinhChenhLechDoiSanPham();
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
        pdt.setLyDo(buildLyDoLuu(lyDo));
        pdt.setHinhThucXuLy(cbHinhThucXuLy.getValue());
        pdt.setPhiPhat(phiPhat);

        List<ChiTietDoiTra> chiTiet = new ArrayList<>();
        for (ChiTietDoiTraTam item : dsTam) {
            ChiTietDoiTra ct = new ChiTietDoiTra();
            ct.setMaPhieuDoiTra(pdt.getMaPhieuDoiTra());
            ct.setMaQuyDoi(item.maQuyDoi);
            ct.setMaLoThuoc(item.maLoThuoc);
            ct.setSoLuong(item.soLuongTra);
            ct.setTinhTrang("");
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
        DoiTraSession.clear();
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
        capNhatSpinnerSoLuong(tableCoTheDoiTra.getSelectionModel().getSelectedItem());
    }

    private void capNhatSpinnerSoLuong(ChiTietDoiTraView selected) {
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
                (SpinnerValueFactory.IntegerSpinnerValueFactory) spSoLuongTra.getValueFactory();

        if (selected == null) {
            valueFactory.setMin(0);
            valueFactory.setMax(0);
            valueFactory.setValue(0);
            spSoLuongTra.setDisable(true);
            spSoLuongTra.getEditor().setText("0");
            return;
        }

        int max = Math.max(0, selected.getSoLuongConLai());
        int min = max > 0 ? 1 : 0;
        int value = max > 0 ? 1 : 0;

        valueFactory.setMin(min);
        valueFactory.setMax(max);
        valueFactory.setValue(value);
        spSoLuongTra.setDisable(max <= 0);
        spSoLuongTra.getEditor().setText(String.valueOf(value));
    }

    private int parseSoLuongDangChon() {
        commitSpinnerEditorText();
        Integer value = spSoLuongTra.getValue();
        return value != null ? value : 0;
    }

    private void commitSpinnerEditorText() {
        SpinnerValueFactory.IntegerSpinnerValueFactory valueFactory =
                (SpinnerValueFactory.IntegerSpinnerValueFactory) spSoLuongTra.getValueFactory();
        if (valueFactory == null) {
            return;
        }

        String text = spSoLuongTra.getEditor().getText();
        if (text == null || text.trim().isEmpty()) {
            spSoLuongTra.getEditor().setText(String.valueOf(valueFactory.getValue()));
            return;
        }

        try {
            int parsedValue = Integer.parseInt(text.trim());
            int clampedValue = Math.max(valueFactory.getMin(), Math.min(valueFactory.getMax(), parsedValue));
            valueFactory.setValue(clampedValue);
            spSoLuongTra.getEditor().setText(String.valueOf(clampedValue));
        } catch (NumberFormatException e) {
            spSoLuongTra.getEditor().setText(String.valueOf(valueFactory.getValue()));
        }
    }

    private void capNhatTong() {
        lblTongDong.setText(String.valueOf(dsTam.size()));
        double tong = tinhTongGiaTriHienThi();
        if (cbHinhThucXuLy.getValue() == HinhThucDoiTra.DOI_SAN_PHAM) {
            double chenhLech = tinhChenhLechDoiSanPham();
            txtPhiPhat.setText(String.format("%.0f", Math.abs(chenhLech)));
            lblTongTienHoan.setText(formatKetQuaDoiSanPham(chenhLech));
            return;
        }

        double phiPhat = tong;
        txtPhiPhat.setText(String.format("%.0f", phiPhat));
        lblTongTienHoan.setText(String.format("%,.0f VND", Math.max(0, tong - phiPhat)));
    }

    private double parsePhiPhatHienTai() {
        try {
            return Double.parseDouble(txtPhiPhat.getText().trim());
        } catch (Exception e) {
            return 0;
        }
    }

    private double tinhTongGiaTriThuocTra() {
        return dsTam.stream().mapToDouble(item -> item.thanhTienHoan).sum();
    }

    private double tinhTongGiaTriHienThi() {
        double tongDaThem = tinhTongGiaTriThuocTra();
        if (tongDaThem > 0) {
            return tongDaThem;
        }
        return tinhGiaTriThuocDangChon();
    }

    private double tinhGiaTriThuocDangChon() {
        ChiTietDoiTraView selected = tableCoTheDoiTra != null
                ? tableCoTheDoiTra.getSelectionModel().getSelectedItem()
                : null;
        if (selected == null || spSoLuongTra == null || spSoLuongTra.isDisable()) {
            return 0;
        }

        Integer soLuong = spSoLuongTra.getValue();
        if (soLuong == null || soLuong <= 0) {
            return 0;
        }

        return selected.getDonGia() * soLuong;
    }

    private double tinhGiaTriThuocDoi() {
        if (DoiTraSession.getThuocDoiDaChon() == null) {
            return 0;
        }

        if (DoiTraSession.getDonViDoiDaChon() != null) {
            return DoiTraSession.getDonViDoiDaChon().getThanhTien();
        }

        List<DonViQuyDoi> dsDonVi = daoDonViQuyDoi.getDonViByMaThuoc(DoiTraSession.getThuocDoiDaChon().getMaThuoc());
        if (dsDonVi == null || dsDonVi.isEmpty()) {
            return 0;
        }

        DonViQuyDoi donViMacDinh = dsDonVi.stream()
                .filter(dv -> dv.getTyLeQuyDoi() == 1)
                .findFirst()
                .orElse(dsDonVi.get(0));

        Object[] giaInfo = daoBangGia.getGiaVaMaBangGia(donViMacDinh.getMaQuyDoi());
        if (giaInfo == null || giaInfo[0] == null) {
            return 0;
        }
        return ((BigDecimal) giaInfo[0]).doubleValue();
    }

    private double tinhChenhLechDoiSanPham() {
        return tinhGiaTriThuocDoi() - tinhTongGiaTriThuocTra();
    }

    private String formatKetQuaDoiSanPham(double chenhLech) {
        if (chenhLech > 0) {
            return "Bù: " + String.format("%,.0f VND", chenhLech);
        }
        if (chenhLech < 0) {
            return "Hoàn: " + String.format("%,.0f VND", Math.abs(chenhLech));
        }
        return "Không";
    }

    private String formatDate(Date date) {
        return date != null ? date.toLocalDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")) : "--";
    }

    private String buildLyDoLuu(String lyDoGoc) {
        if (cbHinhThucXuLy.getValue() != HinhThucDoiTra.DOI_SAN_PHAM || DoiTraSession.getThuocDoiDaChon() == null) {
            return lyDoGoc;
        }

        String ketQuaDoi = xacDinhKetQuaDoiSanPham();
        return lyDoGoc +
                " | Thuốc trả: " + buildThongTinThuocTra() +
                " | Thuốc đổi: " + DoiTraSession.getThuocDoiDaChon().getTenThuoc() +
                " (" + DoiTraSession.getThuocDoiDaChon().getMaThuoc() + ")" +
                " | Kết quả đổi: " + ketQuaDoi;
    }

    private String xacDinhKetQuaDoiSanPham() {
        double chenhLech = tinhChenhLechDoiSanPham();
        if (chenhLech > 0) {
            return "BU_TIEN";
        }
        if (chenhLech < 0) {
            return "HOAN_TIEN";
        }
        return "KHONG_CHENH_LECH";
    }

    private String buildThongTinThuocTra() {
        if (dsTam.isEmpty()) {
            return "";
        }

        List<String> thongTin = new ArrayList<>();
        for (ChiTietDoiTraTam item : dsTam) {
            thongTin.add(item.tenThuoc + " x" + item.soLuongTra + " " + item.tenDonVi);
        }
        return String.join(", ", thongTin);
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
        private final double thanhTienHoan;

        public ChiTietDoiTraTam(ChiTietDoiTraView source, int soLuongTra) {
            this.maQuyDoi = source.getMaQuyDoi();
            this.maLoThuoc = source.getMaLoThuoc();
            this.tenThuoc = source.getTenThuoc();
            this.tenDonVi = source.getTenDonVi();
            this.soLuongTra = soLuongTra;
            this.thanhTienHoan = source.getDonGia() * soLuongTra;
        }

        public ChiTietDoiTraTam(DoiTraSession.ChiTietDoiTraTamData data) {
            this.maQuyDoi = data.getMaQuyDoi();
            this.maLoThuoc = data.getMaLoThuoc();
            this.tenThuoc = data.getTenThuoc();
            this.tenDonVi = data.getTenDonVi();
            this.soLuongTra = data.getSoLuongTra();
            this.thanhTienHoan = data.getThanhTienHoan();
        }

        public DoiTraSession.ChiTietDoiTraTamData toSessionData() {
            return new DoiTraSession.ChiTietDoiTraTamData(
                    maQuyDoi, maLoThuoc, tenThuoc, tenDonVi, soLuongTra, thanhTienHoan);
        }
    }
}
