package gui.main;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

import dao.DAO_BangGia;
import entity.BangGia;
import entity.ChiTietBangGia;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.TextFieldTableCell;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

public class GUI_QuanLyBangGiaController {

    // ======= Views (StackPane toggle) =======
    @FXML private VBox viewDanhSach;
    @FXML private VBox viewTaoBangGia;

    // ======= View 1: Danh sách =======
    @FXML private TextField txtTimKiemTab1;
    @FXML private TableView<BangGia> tableBangGia;
    @FXML private TableColumn<BangGia, String> colMaBangGia;
    @FXML private TableColumn<BangGia, String> colTenBangGia;
    @FXML private TableColumn<BangGia, String> colLoai;
    @FXML private TableColumn<BangGia, String> colNgayBatDau;
    @FXML private TableColumn<BangGia, String> colNgayKetThuc;
    @FXML private TableColumn<BangGia, String> colTrangThai;
    @FXML private TableColumn<BangGia, String> colSoLuong;
    @FXML private TableColumn<BangGia, Void>   colHanhDong;

    // ======= View 2: Tạo bảng giá mới =======
    @FXML private TextField   txtTenBangGia;
    @FXML private ComboBox<String> cbLoaiBangGia;
    @FXML private Label       lblDefaultHienTai;
    @FXML private DatePicker  dpNgayBatDau;
    @FXML private DatePicker  dpNgayKetThuc;
    @FXML private Label       lblNgayKetThuc;
    @FXML private Label       lblNgayKetThucHint;
    @FXML private TextArea    txtMoTa;
    @FXML private TextField   txtTimKiemTab2;
    @FXML private TableView<ChiTietBangGia> tableThuocNhapGia;
    @FXML private TableColumn<ChiTietBangGia, String> colNhapTenThuoc;
    @FXML private TableColumn<ChiTietBangGia, String> colNhapDonVi;
    @FXML private TableColumn<ChiTietBangGia, String> colNhapGiaBan;

    // ======= DAO & Data =======
    private final DAO_BangGia dao = new DAO_BangGia();
    private ObservableList<BangGia> masterBangGia = FXCollections.observableArrayList();
    private ObservableList<ChiTietBangGia> masterThuocNhapGia = FXCollections.observableArrayList();
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {
        setupTableDanhSach();
        setupTableNhapGia();
        setupSearch();
        setupLoaiComboBox();
        loadDanhSach();
        hienViewDanhSach();
    }

    // ============================================================
    // SETUP
    // ============================================================
    private void setupTableDanhSach() {
        colMaBangGia .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getMaBangGia()));
        colTenBangGia.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenBangGia()));
        colLoai      .setCellValueFactory(d -> new SimpleStringProperty(
                "DEFAULT".equals(d.getValue().getLoaiBangGia()) ? "Mặc Định" : "Khuyến Mãi"));
        colNgayBatDau.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getNgayBatDau() != null ? d.getValue().getNgayBatDau().format(FMT) : ""));
        colNgayKetThuc.setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getNgayKetThuc() != null ? d.getValue().getNgayKetThuc().format(FMT) : "—"));
        colSoLuong.setCellValueFactory(d -> new SimpleStringProperty(String.valueOf(d.getValue().getSoLuongThuoc())));

        colTrangThai.setCellValueFactory(d -> new SimpleStringProperty(tinhTrangThai(d.getValue())));
        colTrangThai.setCellFactory(col -> new TableCell<>() {
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : item);
                getStyleClass().removeAll("text-xanh-la", "text-vang-cam", "text-do");
                if (!empty && item != null) {
                    if (item.contains("hiệu lực") && !item.contains("Chưa")) getStyleClass().add("text-xanh-la");
                    else if (item.contains("Chưa")) getStyleClass().add("text-vang-cam");
                    else getStyleClass().add("text-do");
                }
            }
        });

        colHanhDong.setCellFactory(col -> new TableCell<>() {
            private final Button btnXem = new Button("👁 Xem chi tiết");
            private final Button btnVHH = new Button("🚫 Vô Hiệu");
            private final HBox box = new HBox(6, btnXem, btnVHH);
            {
                btnXem.setStyle("-fx-background-color:#2563eb;-fx-text-fill:white;-fx-font-size:11px;-fx-padding:4 8;-fx-cursor:hand;");
                btnVHH.setStyle("-fx-background-color:#dc3545;-fx-text-fill:white;-fx-font-size:11px;-fx-padding:4 8;-fx-cursor:hand;");
                btnXem.setOnAction(e -> moDialogChiTiet(getTableView().getItems().get(getIndex())));
                btnVHH.setOnAction(e -> handleVoHieuHoa(getTableView().getItems().get(getIndex())));
            }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                BangGia bg = getTableView().getItems().get(getIndex());
                String tt = tinhTrangThai(bg);
                btnVHH.setDisable(!tt.contains("hiệu lực") || tt.contains("Chưa"));
                setGraphic(box);
            }
        });
        tableBangGia.setItems(masterBangGia);
    }

    private void setupTableNhapGia() {
        colNhapTenThuoc.setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenThuoc()));
        colNhapDonVi   .setCellValueFactory(d -> new SimpleStringProperty(d.getValue().getTenDonVi()));
        colNhapGiaBan  .setCellValueFactory(d -> new SimpleStringProperty(
                d.getValue().getDonGiaBan().compareTo(BigDecimal.ZERO) == 0 ? "" :
                String.format("%,.0f", d.getValue().getDonGiaBan())));
        colNhapGiaBan.setCellFactory(TextFieldTableCell.forTableColumn());
        colNhapGiaBan.setEditable(true);
        colNhapGiaBan.setOnEditCommit(evt -> {
            String raw = evt.getNewValue().trim().replaceAll(",", "");
            try {
                BigDecimal gia = raw.isEmpty() ? BigDecimal.ZERO : new BigDecimal(raw);
                evt.getRowValue().setDonGiaBan(gia);
            } catch (NumberFormatException ex) {
                showAlert(Alert.AlertType.WARNING, "Lỗi nhập liệu", "Giá phải là số hợp lệ.");
                tableThuocNhapGia.refresh();
            }
        });
        tableThuocNhapGia.setEditable(true);
        tableThuocNhapGia.setItems(masterThuocNhapGia);
    }

    private void setupSearch() {
        // Search danh sách
        txtTimKiemTab1.textProperty().addListener((obs, ov, nv) -> {
            String kw = nv == null ? "" : nv.toLowerCase().trim();
            FilteredList<BangGia> fl = new FilteredList<>(
                    FXCollections.observableArrayList(dao.getAllBangGia()),
                    bg -> kw.isEmpty()
                       || bg.getMaBangGia().toLowerCase().contains(kw)
                       || bg.getTenBangGia().toLowerCase().contains(kw));
            masterBangGia.setAll(fl);
        });

        // Search thuốc nhập giá
        txtTimKiemTab2.textProperty().addListener((obs, ov, nv) -> {
            if (nv == null || nv.isBlank()) {
                tableThuocNhapGia.setItems(masterThuocNhapGia);
                return;
            }
            String kw = nv.toLowerCase();
            ObservableList<ChiTietBangGia> filtered = masterThuocNhapGia.filtered(
                    ct -> ct.getTenThuoc().toLowerCase().contains(kw) ||
                          ct.getTenDonVi().toLowerCase().contains(kw));
            tableThuocNhapGia.setItems(filtered);
        });
    }

    private void setupLoaiComboBox() {
        cbLoaiBangGia.setItems(FXCollections.observableArrayList("DEFAULT", "PROMO"));
        cbLoaiBangGia.valueProperty().addListener((obs, ov, nv) -> {
            if ("DEFAULT".equals(nv)) {
                dpNgayKetThuc.setDisable(true);
                dpNgayKetThuc.setValue(null);
                lblNgayKetThucHint.setText("(DEFAULT không có ngày kết thúc)");
                BangGia cur = dao.getBangGiaDefaultDangMo();
                if (cur != null) {
                    lblDefaultHienTai.setText("Hiện tại: " + cur.getTenBangGia()
                            + " (từ " + cur.getNgayBatDau().format(FMT) + ")");
                } else {
                    lblDefaultHienTai.setText("");
                }
            } else {
                dpNgayKetThuc.setDisable(false);
                lblNgayKetThucHint.setText("(Bắt buộc với PROMO)");
                lblDefaultHienTai.setText("");
            }
        });
    }

    // ============================================================
    // DATA LOAD
    // ============================================================
    private void loadDanhSach() {
        masterBangGia.setAll(dao.getAllBangGia());
    }

    // ============================================================
    // VIEW TOGGLE
    // ============================================================
    private void hienViewDanhSach() {
        viewDanhSach.setVisible(true);
        viewDanhSach.setManaged(true);
        viewTaoBangGia.setVisible(false);
        viewTaoBangGia.setManaged(false);
    }

    private void hienViewTaoBangGia() {
        viewTaoBangGia.setVisible(true);
        viewTaoBangGia.setManaged(true);
        viewDanhSach.setVisible(false);
        viewDanhSach.setManaged(false);
    }

    // ============================================================
    // HANDLERS — Danh sách
    // ============================================================
    @FXML
    public void handleThemBangGiaMoi() {
        // Reset form
        txtTenBangGia.clear();
        cbLoaiBangGia.setValue(null);
        dpNgayBatDau.setValue(null);
        dpNgayKetThuc.setValue(null);
        dpNgayKetThuc.setDisable(false);
        txtMoTa.clear();
        masterThuocNhapGia.setAll(dao.getAllThuocDangBanVaDonVi());
        txtTimKiemTab2.clear();
        hienViewTaoBangGia();
    }

    @FXML
    void handleLamMoiDanhSach(ActionEvent event) {
        txtTimKiemTab1.clear();
        loadDanhSach();
    }

    // ============================================================
    // HANDLERS — Tạo bảng giá
    // ============================================================
    @FXML
    void handleQuayLaiDanhSach(ActionEvent event) {
        hienViewDanhSach();
    }

    @FXML
    void handleLuuBangGia(ActionEvent event) {
        // --- Validate ---
        String ten = txtTenBangGia.getText().trim();
        String loai = cbLoaiBangGia.getValue();
        LocalDate ngayBatDau = dpNgayBatDau.getValue();
        LocalDate ngayKetThuc = dpNgayKetThuc.getValue();

        if (ten.isEmpty()) { showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập tên bảng giá."); return; }
        if (loai == null)  { showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn loại bảng giá."); return; }
        if (ngayBatDau == null) { showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng chọn ngày bắt đầu."); return; }
        if (ngayBatDau.isBefore(LocalDate.now())) {
            showAlert(Alert.AlertType.WARNING, "Lỗi ngày", "Ngày bắt đầu không được là ngày trong quá khứ."); return;
        }

        if ("PROMO".equals(loai)) {
            if (ngayKetThuc == null) {
                showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Bảng giá PROMO bắt buộc phải có ngày kết thúc."); return;
            }
            if (!ngayKetThuc.isAfter(LocalDate.now())) {
                showAlert(Alert.AlertType.WARNING, "Lỗi ngày", "Ngày kết thúc phải là ngày trong tương lai và sau ngày bắt đầu."); return;
            }
            if (!ngayKetThuc.isAfter(ngayBatDau)) {
                showAlert(Alert.AlertType.WARNING, "Lỗi ngày", "Ngày kết thúc phải sau ngày bắt đầu."); return;
            }
        }

        // Validate giá > 0 cho ít nhất 1 thuốc
        List<ChiTietBangGia> danhSachGia = masterThuocNhapGia.stream()
                .filter(ct -> ct.getDonGiaBan() != null && ct.getDonGiaBan().compareTo(BigDecimal.ZERO) > 0)
                .collect(Collectors.toList());
        if (danhSachGia.isEmpty()) {
            showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập giá cho ít nhất một đơn vị thuốc."); return;
        }

        // --- Build entity ---
        BangGia bg = new BangGia();
        bg.setMaBangGia(dao.generateNextMaBangGia());
        bg.setTenBangGia(ten);
        bg.setLoaiBangGia(loai);
        bg.setNgayBatDau(ngayBatDau);
        bg.setNgayKetThuc("DEFAULT".equals(loai) ? null : ngayKetThuc);
        bg.setMoTa(txtMoTa.getText().trim());
        bg.setTrangThai(true);

        // Set maBangGia cho mỗi chi tiết
        for (ChiTietBangGia ct : danhSachGia) {
            ct.setMaBangGia(bg.getMaBangGia());
        }

        // --- Lưu ---
        String err = dao.taoBangGiaMoi(bg, danhSachGia);
        if (err == null) {
            showAlert(Alert.AlertType.INFORMATION, "Thành công", "Tạo bảng giá thành công! Mã: " + bg.getMaBangGia());
            loadDanhSach();
            hienViewDanhSach();
        } else {
            showAlert(Alert.AlertType.ERROR, "Lỗi", err);
        }
    }

    // ============================================================
    // HANDLERS — Dialog chi tiết
    // ============================================================
    private void moDialogChiTiet(BangGia bg) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_ChiTietBangGia.fxml"));
            Parent root = loader.load();
            gui.dialogs.Dialog_ChiTietBangGiaController ctrl = loader.getController();
            ctrl.setBangGia(bg);

            Stage stage = new Stage();
            stage.initModality(Modality.APPLICATION_MODAL);
            stage.setTitle("Chi Tiết Bảng Giá — " + bg.getMaBangGia());
            stage.setScene(new Scene(root));
            stage.showAndWait();

            loadDanhSach(); // Reload sau khi đóng dialog
        } catch (Exception e) {
            e.printStackTrace();
            showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể mở dialog chi tiết: " + e.getMessage());
        }
    }

    // ============================================================
    // HANDLERS — Vô hiệu hóa
    // ============================================================
    private void handleVoHieuHoa(BangGia bg) {
        String tt = tinhTrangThai(bg);
        if (!tt.contains("hiệu lực") || tt.contains("Chưa")) {
            showAlert(Alert.AlertType.WARNING, "Không thể", "Chỉ có thể vô hiệu hóa bảng giá đang hiệu lực."); return;
        }

        // Chặn nếu là DEFAULT duy nhất
        if ("DEFAULT".equals(bg.getLoaiBangGia()) && !dao.coTonTaiBangGiaDefaultKhac(bg.getMaBangGia())) {
            showAlert(Alert.AlertType.WARNING, "Không thể vô hiệu hóa",
                    "Đây là bảng giá mặc định duy nhất đang hoạt động.\nVui lòng tạo bảng mặc định mới trước."); return;
        }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION,
                "Vô hiệu hóa bảng giá \"" + bg.getTenBangGia() + "\"?",
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText(null);
        confirm.showAndWait();
        if (confirm.getResult() == ButtonType.YES) {
            boolean ok = dao.voHieuHoa(bg.getMaBangGia());
            if (ok) {
                showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã vô hiệu hóa bảng giá.");
                loadDanhSach();
            } else {
                showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể vô hiệu hóa. Vui lòng thử lại.");
            }
        }
    }

    // ============================================================
    // HELPER — Tính trạng thái realtime
    // ============================================================
    private String tinhTrangThai(BangGia bg) {
        LocalDate today = LocalDate.now();
        LocalDate bd = bg.getNgayBatDau();
        LocalDate kt = bg.getNgayKetThuc();
        boolean active = bg.isTrangThai();

        if (!active || (kt != null && kt.isBefore(today))) return "Đã kết thúc";
        if (bd.isAfter(today)) return "Chưa hiệu lực";
        return "Đang hiệu lực";
    }

    private void showAlert(Alert.AlertType type, String title, String msg) {
        Alert a = new Alert(type, msg, ButtonType.OK);
        a.setTitle(title);
        a.setHeaderText(null);
        a.showAndWait();
    }
}
