package gui.main;

import java.sql.Date;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

import dao.DAO_PhieuDoiTra;
import entity.ChiTietDoiTra;
import entity.ChiTietDoiTraView;
import entity.DonViQuyDoi;
import entity.HinhThucDoiTra;
import entity.HoaDon;
import entity.HoaDonView;
import entity.NhanVien;
import entity.PhieuDoiTra;
import entity.Thuoc;
import gui.dialogs.Dialog_ChonSoLuongDonViController;
import gui.dialogs.Dialog_ChonThuocController;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import dao.DAO_NhatKyHoatDong;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Spinner;
import javafx.scene.control.SpinnerValueFactory;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TextFormatter;
import javafx.stage.Modality;
import javafx.stage.Stage;
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

    // Bảng thuốc nhận đổi
    @FXML private TableView<DoiTraSession.DonViDoiData> tableThuocNhan;
    @FXML private TableColumn<DoiTraSession.DonViDoiData, String> colNhanTenThuoc;
    @FXML private TableColumn<DoiTraSession.DonViDoiData, String> colNhanDonVi;
    @FXML private TableColumn<DoiTraSession.DonViDoiData, String> colNhanSoLuong;
    @FXML private TableColumn<DoiTraSession.DonViDoiData, String> colNhanDonGia;
    @FXML private TableColumn<DoiTraSession.DonViDoiData, String> colNhanThanhTien;
    @FXML private TableColumn<DoiTraSession.DonViDoiData, Void> colNhanXoa;

    @FXML private Spinner<Integer> spSoLuongTra;
    @FXML private ComboBox<String> cbLyDo;
    @FXML private TextField txtLyDo;
    @FXML private ComboBox<HinhThucDoiTra> cbHinhThucXuLy;
    @FXML private javafx.scene.control.Label lblPhiPhat;

    private final DAO_PhieuDoiTra daoPhieuDoiTra = new DAO_PhieuDoiTra();
    private final ObservableList<ChiTietDoiTraView> dsChiTietHoaDon = FXCollections.observableArrayList();
    private final ObservableList<ChiTietDoiTraTam> dsTam = FXCollections.observableArrayList();
    private final ObservableList<DoiTraSession.DonViDoiData> dsThuocNhan = FXCollections.observableArrayList();
    private final DateTimeFormatter dateTimeFmt = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    private HoaDonView hoaDon;

    @FXML
    public void initialize() {
        cbHinhThucXuLy.setItems(FXCollections.observableArrayList(HinhThucDoiTra.values()));
        cbHinhThucXuLy.setValue(HinhThucDoiTra.HOAN_TIEN);
        cbHinhThucXuLy.valueProperty().addListener((obs, oldVal, newVal) -> xuLyThayDoiHinhThuc(oldVal, newVal));

        cbLyDo.setItems(FXCollections.observableArrayList(
                "Khách hàng muốn hoàn", "Lỗi từ nhà sản xuất"));
        cbLyDo.setValue("Khách hàng muốn hoàn");
        cbLyDo.valueProperty().addListener((obs, oldVal, newVal) -> capNhatTong());

        setupSoLuongSpinner();
        setupAvailableTable();
        setupReturnTable();
        setupThuocNhanTable();

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
        xuLyThayDoiHinhThuc(null, cbHinhThucXuLy.getValue());
        capNhatTong();
    }

    private void khoiPhucDuLieuTam() {
        String lyDoTam = DoiTraSession.getLyDoTam();
        if (DoiTraSession.getHinhThucXuLyTam() == HinhThucDoiTra.DOI_SAN_PHAM) {
             txtLyDo.setText(lyDoTam);
        } else {
             cbLyDo.setValue(lyDoTam != null && !lyDoTam.isEmpty() ? lyDoTam : "Khách hàng muốn hoàn");
        }
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

        // Khôi phục danh sách thuốc nhận đổi
        dsThuocNhan.setAll(DoiTraSession.getDsThuocDoi());

        tableCoTheDoiTra.refresh();
        tableChiTietDoiTra.refresh();
        capNhatTong();
    }

    private void luuDuLieuTamVaoSession() {
        String lyDo = cbHinhThucXuLy.getValue() == HinhThucDoiTra.DOI_SAN_PHAM ? txtLyDo.getText() : cbLyDo.getValue();
        DoiTraSession.setLyDoTam(lyDo);
        DoiTraSession.setHinhThucXuLyTam(cbHinhThucXuLy.getValue());

        List<DoiTraSession.ChiTietDoiTraTamData> data = new ArrayList<>();
        for (ChiTietDoiTraTam item : dsTam) {
            data.add(item.toSessionData());
        }
        DoiTraSession.setDsChiTietTam(data);

        // Lưu danh sách thuốc nhận đổi
        DoiTraSession.clearThuocDoi();
        for (DoiTraSession.DonViDoiData item : dsThuocNhan) {
            DoiTraSession.addThuocDoi(item);
        }
    }

    private void xuLyThayDoiHinhThuc(HinhThucDoiTra oldVal, HinhThucDoiTra newVal) {
        if (newVal == null) {
            return;
        }

        boolean isDoi = (newVal == HinhThucDoiTra.DOI_SAN_PHAM);
        cbLyDo.setVisible(!isDoi);
        cbLyDo.setManaged(!isDoi);
        txtLyDo.setVisible(isDoi);
        txtLyDo.setManaged(isDoi);

        // Nếu chuyển sang hoàn tiền -> Xóa danh sách thuốc đổi đã chọn
        if (newVal == HinhThucDoiTra.HOAN_TIEN) {
            dsThuocNhan.clear();
            DoiTraSession.clearThuocDoi();
        }

        colCTThanhTien.setText(isDoi ? "Giá trị đổi" : "Tiền hoàn");
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

        // [10] Row factory: hiển thị tất cả, disable hàng đã trả hết
        tableCoTheDoiTra.setRowFactory(tv -> new TableRow<ChiTietDoiTraView>() {
            @Override
            protected void updateItem(ChiTietDoiTraView item, boolean empty) {
                super.updateItem(item, empty);
                if (item != null && item.getSoLuongConLai() <= 0) {
                    setDisable(true);
                    setStyle("-fx-opacity: 0.4;");
                } else {
                    setDisable(false);
                    setStyle("");
                }
            }
        });
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

    private void setupThuocNhanTable() {
        colNhanTenThuoc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenThuoc()));
        colNhanDonVi.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenDonVi()));
        colNhanSoLuong.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getSoLuong())));
        colNhanDonGia.setCellValueFactory(d -> new SimpleStringProperty(String.format("%,.0f VND", d.getValue().getDonGia())));
        colNhanThanhTien.setCellValueFactory(d -> new SimpleStringProperty(String.format("%,.0f VND", d.getValue().getThanhTien())));

        colNhanXoa.setCellFactory(col -> new TableCell<>() {
            private final Button btnXoa = new Button("Xóa");

            {
                btnXoa.setStyle("-fx-background-color:#dc2626;-fx-text-fill:white;-fx-font-size:12px;-fx-padding:4 10;-fx-cursor:hand;");
                btnXoa.setOnAction(e -> {
                    DoiTraSession.DonViDoiData item = getTableView().getItems().get(getIndex());
                    dsThuocNhan.remove(item);
                    capNhatTong();
                });
            }

            @Override
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btnXoa);
            }
        });

        tableThuocNhan.setItems(dsThuocNhan);
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

        // Bỏ kiểm tra tình trạng hư hỏng vì không còn combobox tình trạng

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

        String tinhTrang = null;

        ChiTietDoiTraTam item = timChiTietTam(selected.getMaQuyDoi(), selected.getMaLoThuoc());
        if (item == null) {
            dsTam.add(new ChiTietDoiTraTam(selected, soLuongTra, tinhTrang));
        } else {
            item.addSoLuong(soLuongTra, selected.getDonGia());
            tableChiTietDoiTra.refresh();
        }
        selected.setSoLuongDaTra(selected.getSoLuongDaTra() + soLuongTra);
        tableCoTheDoiTra.refresh();
        capNhatTong();
        capNhatSpinnerSoLuong(selected);
    }

    @FXML
    void handleChonThuocDoi(ActionEvent event) {
        if (cbHinhThucXuLy.getValue() != HinhThucDoiTra.DOI_SAN_PHAM) {
            showAlert(Alert.AlertType.INFORMATION, "Chỉ chọn thuốc đổi khi phiếu đang ở hình thức đổi sản phẩm.");
            return;
        }

        Thuoc thuoc = moDialogChonThuoc();
        if (thuoc == null) {
            return;
        }

        DoiTraSession.DonViDoiData donViDoi = moDialogChonDonViThuocDoi(thuoc);
        if (donViDoi == null) {
            return;
        }

        dsThuocNhan.add(donViDoi);
        DoiTraSession.addThuocDoi(donViDoi);
        capNhatTong();
    }

    @FXML
    void handleBoThuocDoi(ActionEvent event) {
        DoiTraSession.DonViDoiData selected = tableThuocNhan.getSelectionModel().getSelectedItem();
        if (selected == null) {
            // Nếu không chọn dòng cụ thể, xóa hết
            dsThuocNhan.clear();
            DoiTraSession.clearThuocDoi();
        } else {
            dsThuocNhan.remove(selected);
            DoiTraSession.removeThuocDoi(selected);
        }
        capNhatTong();
    }

    @FXML
    void handleLuu(ActionEvent event) {
        if (hoaDon == null) {
            showAlert(Alert.AlertType.ERROR, "Không có hóa đơn để lập phiếu đổi trả.");
            return;
        }

        // [4] Kiểm tra thời hạn đổi trả 30 ngày
        long millisPerDay = 86_400_000L;
        long soNgay = (System.currentTimeMillis() - hoaDon.getNgayLap().getTime()) / millisPerDay;
        if (soNgay > 30) {
            showAlert(Alert.AlertType.WARNING, "Hóa đơn đã quá 30 ngày, không thể lập phiếu đổi trả.");
            return;
        }

        if (dsTam.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Chưa có thuốc nào trong danh sách trả.");
            return;
        }

        boolean isDoi = (cbHinhThucXuLy.getValue() == HinhThucDoiTra.DOI_SAN_PHAM);
        String lyDo = isDoi ? txtLyDo.getText().trim() : (cbLyDo.getValue() != null ? cbLyDo.getValue().trim() : "");
        if (lyDo.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Vui lòng nhập lý do đổi trả.");
            if (isDoi) {
				txtLyDo.requestFocus();
			}
            return;
        }

        if (isDoi && dsThuocNhan.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Chưa có thông tin thuốc đổi cho phiếu đổi sản phẩm.");
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

        // [2] Tính phiPhat và ketQuaDoiSanPham
        if (isDoi) {
            double chenhLech = tinhChenhLechDoiSanPham();
            pdt.setPhiPhat(Math.abs(chenhLech));
            pdt.setKetQuaDoiSanPham(chenhLech > 0 ? "BU_TIEN" : chenhLech < 0 ? "HOAN_TIEN" : "KHONG_CHENH_LECH");
        } else {
            if ("Khách hàng muốn hoàn".equals(lyDo)) {
                 double tongTienHoan = 0;
                 for (ChiTietDoiTraTam t : dsTam) {
					tongTienHoan += t.thanhTienHoan;
				 }
                 pdt.setPhiPhat(tongTienHoan * 0.3); // Phạt 30% nếu khách tự ý trả
            } else {
                 pdt.setPhiPhat(0);
            }
            pdt.setKetQuaDoiSanPham(null);
        }

        // Tạo danh sách chi tiết thuốc trả
        List<ChiTietDoiTra> chiTiet = new ArrayList<>();
        for (ChiTietDoiTraTam item : dsTam) {
            ChiTietDoiTra ct = new ChiTietDoiTra();
            ct.setMaPhieuDoiTra(pdt.getMaPhieuDoiTra());
            ct.setMaQuyDoi(item.maQuyDoi);
            ct.setMaLoThuoc(item.maLoThuoc);
            ct.setSoLuong(item.soLuongTra);
            ct.setTinhTrang(item.tinhTrang); // Ở đây sếp đang truyền null vì bỏ ô nhập tình trạng
            chiTiet.add(ct);
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Xác nhận lưu phiếu đổi trả " + pdt.getMaPhieuDoiTra() + "?",
                ButtonType.YES, ButtonType.NO);
        Optional<ButtonType> rs = confirm.showAndWait();
        if (rs.isEmpty() || rs.get() != ButtonType.YES) {
            return;
        }

        // =====================================================================
        // GIAO DỊCH 1: LƯU PHIẾU ĐỔI TRẢ (KHÁCH TRẢ VỀ)
        // =====================================================================
        List<DoiTraSession.DonViDoiData> dsThuocDoiParam = isDoi ? new ArrayList<>(dsThuocNhan) : null;
        boolean okPhieuTra = daoPhieuDoiTra.lapPhieuDoiTra(pdt, chiTiet, dsThuocDoiParam);

        if (okPhieuTra) {
            DAO_NhatKyHoatDong.ghiLog("TAO_PHIEU_DOI_TRA", "Phiếu Đổi Trả", pdt.getMaPhieuDoiTra(), "Lập phiếu đổi trả từ hóa đơn: " + hoaDon.getMaHoaDon());
            // =====================================================================
            // GIAO DỊCH 2: TẠO HÓA ĐƠN CHO THUỐC ĐỔI (KHÁCH LẤY ĐI)
            // Mục đích: Ép trừ kho FEFO và tăng Số Lượng Bán trong thống kê
            // =====================================================================
            if (isDoi && !dsThuocNhan.isEmpty()) {
                try {
                    dao.DAO_HoaDon daoHoaDon = new dao.DAO_HoaDon();
                    String maHDMoi = daoHoaDon.generateMaHoaDon();

                    HoaDon hdMoi = new HoaDon();
                    hdMoi.setMaHoaDon(maHDMoi);
                    hdMoi.setNgayLap(new java.sql.Timestamp(System.currentTimeMillis()));
                    hdMoi.setThueVAT(8.0); // Mặc định VAT
                    hdMoi.setHinhThucThanhToan("TIEN_MAT");
                    hdMoi.setLoaiBan("BAN_LE"); // Từ khóa để sau này phân biệt
                    hdMoi.setNhanVien(nhanVien);

                    // Lấy Khách Hàng từ hóa đơn gốc (Cần tạo đối tượng hoặc truy vấn DAO)
                    entity.KhachHang kh = new dao.DAO_KhachHang().getBySdt(hoaDon.getSdt());
                    hdMoi.setKhachHang(kh);
                    hdMoi.setGhiChu("HĐ xuất bù cho PDT: " + pdt.getMaPhieuDoiTra());

                    // Chuyển dsThuocNhan thành ChiTietHoaDon
                    List<entity.ChiTietHoaDon> dsCTHD = new ArrayList<>();
                    for (DoiTraSession.DonViDoiData item : dsThuocNhan) {
                        entity.ChiTietHoaDon ct = new entity.ChiTietHoaDon();
                        ct.setHoaDon(hdMoi);
                        ct.setMaQuyDoi(item.getMaQuyDoi());
                        ct.setSoLuong(item.getSoLuong());
                        ct.setDonGia(item.getDonGia() / (1 + hdMoi.getThueVAT() / 100.0));

                        // Phải tìm một bảng giá mặc định để chèn vào (hoặc null nếu DB cho phép)
                        ct.setMaBangGia("BG0001"); // Sếp tự điều chỉnh lại mã bảng giá cho đúng logic nhà thuốc nhé

                        dsCTHD.add(ct);
                    }

                    // Gọi hàm thanh toán để chốt kho
                    boolean okHoaDon = daoHoaDon.thanhToan(hdMoi, dsCTHD);
                    if(!okHoaDon) {
                        System.err.println("Cảnh báo: Tạo Phiếu Đổi Trả thành công nhưng lỗi khi tạo Hóa Đơn trừ kho bù!");
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }

            showAlert(Alert.AlertType.INFORMATION, "Lập phiếu đổi trả thành công. Mã phiếu: " + pdt.getMaPhieuDoiTra());
            DoiTraSession.clear();
            SceneUtils.switchPage("/gui/main/GUI_XuLyDoiTra.fxml");
        } else {
            showAlert(Alert.AlertType.ERROR, "Lập phiếu đổi trả thất bại. Có thể tồn kho thuốc đổi không đủ.");
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

    private ChiTietDoiTraTam timChiTietTam(String maQuyDoi, String maLoThuoc) {
        for (ChiTietDoiTraTam item : dsTam) {
            if (item.maQuyDoi.equals(maQuyDoi) && item.maLoThuoc.equals(maLoThuoc)) {
                return item;
            }
        }
        return null;
    }

    private Thuoc moDialogChonThuoc() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_ChonThuoc.fxml"));
            Parent root = loader.load();
            Dialog_ChonThuocController controller = loader.getController();

            String loaiBan = hoaDon != null ? hoaDon.getLoaiBan() : "BAN_LE";
            controller.setLoaiBan(loaiBan);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Chọn thuốc đổi");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            return controller.getThuocChon();
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Không thể mở danh sách chọn thuốc đổi.");
            return null;
        }
    }

    private DoiTraSession.DonViDoiData moDialogChonDonViThuocDoi(Thuoc thuoc) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_ChonSoLuongDonVi.fxml"));
            Parent root = loader.load();
            Dialog_ChonSoLuongDonViController controller = loader.getController();
            controller.setThuoc(thuoc);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Chọn đơn vị thuốc đổi");
            stage.setScene(new Scene(root));
            stage.showAndWait();

            DonViQuyDoi donVi = controller.getDonViChon();
            if (donVi == null || controller.getSoLuongChon() <= 0) {
                return null;
            }

            double donGiaVAT = controller.getDonGiaChon() * (1 + hoaDon.getThueVAT() / 100.0);
            return new DoiTraSession.DonViDoiData(
                    donVi.getMaQuyDoi(),
                    donVi.getTenDonVi(),
                    controller.getSoLuongChon(),
                    donGiaVAT,
                    thuoc.getTenThuoc());
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Không thể chọn đơn vị thuốc đổi.");
            return null;
        }
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

    // [7] capNhatTong với 2 label thay vì txtPhiPhat
    private void capNhatTong() {
        lblTongDong.setText(String.valueOf(dsTam.size()));
        double tongTra = tinhTongGiaTriThuocTra();

        if (cbHinhThucXuLy.getValue() == HinhThucDoiTra.DOI_SAN_PHAM) {
            if (dsThuocNhan.isEmpty()) {
                lblTongTienHoan.setText("Chưa chọn thuốc nhận");
                lblTongTienHoan.setStyle("-fx-text-fill:#64748b; -fx-font-size:18px; -fx-font-weight:bold;");
            } else {
                double chenhLech = tinhChenhLechDoiSanPham();
                if (chenhLech > 0) {
                    lblTongTienHoan.setText("Khách cần bù: " + String.format("%,.0f VND", chenhLech));
                    lblTongTienHoan.setStyle("-fx-text-fill:#e65100; -fx-font-size:18px; -fx-font-weight:bold;");
                } else if (chenhLech < 0) {
                    lblTongTienHoan.setText("Nhà thuốc hoàn: " + String.format("%,.0f VND", Math.abs(chenhLech)));
                    lblTongTienHoan.setStyle("-fx-text-fill:#2e7d32; -fx-font-size:18px; -fx-font-weight:bold;");
                } else {
                    lblTongTienHoan.setText("Không chênh lệch(0 VND)");
                    lblTongTienHoan.setStyle("-fx-text-fill:#16a34a; -fx-font-size:18px; -fx-font-weight:bold;");
                }
            }
            if (lblPhiPhat != null) {
                lblPhiPhat.setVisible(false);
                lblPhiPhat.setManaged(false);
            }
            return;
        }

        // HOAN_TIEN
        double tong = tinhTongGiaTriHienThi();
        double phiPhat = 0;

        if ("Khách hàng muốn hoàn".equals(cbLyDo.getValue())) {
            phiPhat = tong * 0.3;
        }

        double tongTienHoan = tong - phiPhat;

        lblTongTienHoan.setText("Nhà thuốc hoàn: " + String.format("%,.0f VND", tongTienHoan));
        lblTongTienHoan.setStyle("-fx-text-fill:#16a34a; -fx-font-size:18px; -fx-font-weight:bold;");

        if (phiPhat > 0 && lblPhiPhat != null) {
            lblPhiPhat.setText("Phí phạt (30%): " + String.format("-%,.0f VND", phiPhat));
            lblPhiPhat.setVisible(true);
            lblPhiPhat.setManaged(true);
        } else if (lblPhiPhat != null) {
            lblPhiPhat.setVisible(false);
            lblPhiPhat.setManaged(false);
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
        return dsThuocNhan.stream().mapToDouble(DoiTraSession.DonViDoiData::getThanhTien).sum();
    }

    private double tinhChenhLechDoiSanPham() {
        return tinhGiaTriThuocDoi() - tinhTongGiaTriThuocTra();
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
        private int soLuongTra;
        private double thanhTienHoan;
        private final String tinhTrang;

        public ChiTietDoiTraTam(ChiTietDoiTraView source, int soLuongTra, String tinhTrang) {
            this.maQuyDoi = source.getMaQuyDoi();
            this.maLoThuoc = source.getMaLoThuoc();
            this.tenThuoc = source.getTenThuoc();
            this.tenDonVi = source.getTenDonVi();
            this.soLuongTra = soLuongTra;
            this.thanhTienHoan = source.getDonGia() * soLuongTra;
            this.tinhTrang = tinhTrang;
        }

        public ChiTietDoiTraTam(DoiTraSession.ChiTietDoiTraTamData data) {
            this.maQuyDoi = data.getMaQuyDoi();
            this.maLoThuoc = data.getMaLoThuoc();
            this.tenThuoc = data.getTenThuoc();
            this.tenDonVi = data.getTenDonVi();
            this.soLuongTra = data.getSoLuongTra();
            this.thanhTienHoan = data.getThanhTienHoan();
            this.tinhTrang = data.getTinhTrang();
        }

        public DoiTraSession.ChiTietDoiTraTamData toSessionData() {
            return new DoiTraSession.ChiTietDoiTraTamData(
                    maQuyDoi, maLoThuoc, tenThuoc, tenDonVi, soLuongTra, thanhTienHoan, tinhTrang);
        }

        public void addSoLuong(int soLuongThem, double donGia) {
            soLuongTra += soLuongThem;
            thanhTienHoan += soLuongThem * donGia;
        }
    }
}
