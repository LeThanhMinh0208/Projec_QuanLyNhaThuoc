package gui.main;

import connectDB.ConnectDB;
import dao.DAO_KiemKeKho;
import entity.PhieuKiemKe;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.AlertUtils;
import utils.UserSession;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GUI_GiaiQuyetKiemKeController {

    @FXML private VBox viewDanhSach, viewDuyet;
    @FXML private TextField txtTimKiem;
    @FXML private TableView<PhieuKiemKe> tableDanhSach;
    @FXML private TableColumn<PhieuKiemKe, String> colMaPhieu, colNguoiLap, colNgayTao, colTrangThai;
    @FXML private TableColumn<PhieuKiemKe, Void> colThaoTac;

    @FXML private TableView<ChiTietDuyetUI> tableChiTiet;
    @FXML private TableColumn<ChiTietDuyetUI, String> colMaLoCT, colTenThuocCT, colLyDoCT, colGhiChuCT;
    @FXML private TableColumn<ChiTietDuyetUI, Integer> colTonSnapshotCT, colTonKyVongCT, colThucTeCT, colChenhLechCT;
    @FXML private Button btnXacNhan;

    private DAO_KiemKeKho dao = new DAO_KiemKeKho();
    private ObservableList<ChiTietDuyetUI> dsChiTiet = FXCollections.observableArrayList();
    private String currentMaPhieu;
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm");

    private final List<String> lyDoThieu = List.of("Thuốc hư hỏng", "Thất thoát", "Nhân viên quên lập hóa đơn", "Khác");
    private final List<String> lyDoThua = List.of("Quên lập phiếu nhập", "NCC giao dư", "Thuốc còn sót lại", "Nhân viên nhầm lẫn", "Khác");

    @FXML
    public void initialize() {
        setupTableDS();
        setupTableDuyet(); 
        loadDanhSach();
    }

    private void setupTableDS() {
        colMaPhieu.setCellValueFactory(new PropertyValueFactory<>("maPhieuKiemKe"));
        colNguoiLap.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getNhanVienTao().getHoTen()));
        colNgayTao.setCellValueFactory(c -> new SimpleStringProperty(sdf.format(c.getValue().getNgayTao())));
        colTrangThai.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        colTrangThai.setCellFactory(col -> new TableCell<>() {
            protected void updateItem(String s, boolean empty) {
                super.updateItem(s, empty);
                if (empty || s == null) { setGraphic(null); return; }
                Label lbl = new Label();
                lbl.getStyleClass().add("status-badge");
                if ("CHO_DUYET".equals(s)) { lbl.setText("Chờ Duyệt"); lbl.getStyleClass().add("status-pending"); }
                else if ("DA_HOAN_THANH".equals(s)) { lbl.setText("Hoàn Thành"); lbl.getStyleClass().add("status-resolved"); }
                else { lbl.setText("Đã Hủy"); lbl.getStyleClass().add("status-canceled"); }
                setGraphic(lbl);
                setAlignment(Pos.CENTER);
            }
        });
        colThaoTac.setCellFactory(p -> new TableCell<>() {
            private final Button btn = new Button();
            {
                btn.getStyleClass().add("btn-pill");
                btn.setOnAction(e -> handleAction(getTableRow().getItem()));
            }
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow().getItem() == null) { setGraphic(null); return; }
                PhieuKiemKe pk = getTableRow().getItem();
                
                btn.getStyleClass().removeAll("btn-giai-quyet", "btn-xem-chi-tiet");
                
                if ("CHO_DUYET".equals(pk.getTrangThai())) {
                    btn.setText("Giải Quyết");
                    btn.getStyleClass().add("btn-giai-quyet");
                } else {
                    btn.setText("Xem Chi Tiết");
                    btn.getStyleClass().add("btn-xem-chi-tiet");
                }
                setGraphic(btn);
                setAlignment(Pos.CENTER);
            }
        });
        txtTimKiem.textProperty().addListener((obs, o, n) -> loadDanhSach());
    }

    private void setupTableDuyet() {
        colMaLoCT.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().maLoThuoc));
        colTenThuocCT.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().tenThuoc));
        colTonSnapshotCT.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().tonKhoSnapshot).asObject());
        colTonKyVongCT.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().tonKyVongDong).asObject());
        colThucTeCT.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().soLuongKiemTra).asObject());
        colChenhLechCT.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().chenhLech).asObject());

        colChenhLechCT.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                
                getStyleClass().removeAll("text-lech-am", "text-lech-du", "text-lech-khop");
                
                if (empty || item == null) {
                    setText(null);
                    return;
                }
                setText(item > 0 ? "+" + item : String.valueOf(item));
                
                if (item < 0) getStyleClass().add("text-lech-am");
                else if (item > 0) getStyleClass().add("text-lech-du");
                else getStyleClass().add("text-lech-khop");
            }
        });

        colLyDoCT.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().lyDoLech));
        colLyDoCT.setCellFactory(column -> new TableCell<ChiTietDuyetUI, String>() {
            private final ComboBox<String> comboBox = new ComboBox<>();

            {
                comboBox.getItems().addAll("Khớp số", "Hư hỏng", "Hết hạn", "Mất mát", "Khác");
                comboBox.getStyleClass().add("kk-combo-box");
                comboBox.setMaxWidth(Double.MAX_VALUE);

                comboBox.setButtonCell(new ListCell<String>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) {
                            setText(null);
                        } else {
                            setText(item);
                            setStyle("-fx-text-fill: #1e293b; -fx-font-weight: bold; -fx-background-color: transparent;");
                        }
                    }
                });

                comboBox.valueProperty().addListener((obs, oldVal, newVal) -> {
                    ChiTietDuyetUI row = getTableRow().getItem();
                    if (row != null && newVal != null) {
                        row.lyDoLech = newVal;
                        kiemTraNutXacNhan(); 
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    ChiTietDuyetUI row = getTableRow().getItem();
                    
                    if (row.chenhLech == 0) {
                        comboBox.setItems(FXCollections.observableArrayList("Khớp số"));
                        comboBox.setValue("Khớp số");
                        comboBox.setDisable(true);
                        row.lyDoLech = "Khớp số";
                    } else if (row.chenhLech < 0) {
                        comboBox.setItems(FXCollections.observableArrayList(lyDoThieu));
                        comboBox.setDisable(false);
                        comboBox.setValue(row.lyDoLech);
                    } else {
                        comboBox.setItems(FXCollections.observableArrayList(lyDoThua));
                        comboBox.setDisable(false);
                        comboBox.setValue(row.lyDoLech);
                    }
                    setGraphic(comboBox);
                }
            }
        });

        colGhiChuCT.setCellValueFactory(c -> c.getValue().ghiChuProperty());
        colGhiChuCT.setCellFactory(column -> new TableCell<ChiTietDuyetUI, String>() {
            private final TextField textField = new TextField();

            {
                textField.getStyleClass().add("kk-text-field");
                
                textField.textProperty().addListener((obs, oldVal, newVal) -> {
                    ChiTietDuyetUI row = getTableRow().getItem();
                    if (row != null) {
                        row.setGhiChu(newVal); 
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    ChiTietDuyetUI row = getTableRow().getItem();
                    if (!textField.getText().equals(row.getGhiChu())) {
                        textField.setText(row.getGhiChu());
                    }
                    setGraphic(textField);
                }
            }
        });
    }

    @FXML
    void loadDanhSach() {
        tableDanhSach.setItems(FXCollections.observableArrayList(dao.getAllPhieuGiaiQuyet(txtTimKiem.getText())));
    }

    private void handleAction(PhieuKiemKe pk) {
        if ("CHO_DUYET".equals(pk.getTrangThai())) {
            currentMaPhieu = pk.getMaPhieuKiemKe();
            viewDanhSach.setVisible(false); viewDanhSach.setManaged(false);
            viewDuyet.setVisible(true); viewDuyet.setManaged(true);
            loadDuLieuDuyet(pk);
            btnXacNhan.setDisable(true);
        } else {
            try {
                FXMLLoader loader = new FXMLLoader(getClass().getResource("/gui/dialogs/Dialog_ChiTietPhieuKiemKe.fxml"));
                Parent root = loader.load();
                gui.dialogs.Dialog_ChiTietPhieuKiemKeController ctrl = loader.getController();
                ctrl.setPhieuKiemKe(pk);
                Stage stage = new Stage();
                stage.setScene(new Scene(root));
                stage.initModality(Modality.APPLICATION_MODAL);
                stage.showAndWait();
            } catch (Exception e) { e.printStackTrace(); }
        }
    }

    private void loadDuLieuDuyet(PhieuKiemKe pk) {
        dsChiTiet.clear();
        String sql = "SELECT c.*, t.tenThuoc FROM ChiTietPhieuKiemKe c JOIN LoThuoc l ON c.maLoThuoc=l.maLoThuoc JOIN Thuoc t ON l.maThuoc=t.maThuoc WHERE c.maPhieuKiemKe=?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, pk.getMaPhieuKiemKe());
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                ChiTietDuyetUI ui = new ChiTietDuyetUI();
                ui.maLoThuoc = rs.getString("maLoThuoc");
                ui.tenThuoc = rs.getString("tenThuoc");
                ui.tonKhoSnapshot = rs.getInt("tonKhoSnapshot");
                ui.soLuongKiemTra = rs.getInt("soLuongKiemTra");
                ui.ngayTaoT0 = pk.getNgayTao();
                ui.thoiDiemDem = rs.getTimestamp("thoiDiemDem");
                String gc = rs.getString("ghiChu");
                ui.setGhiChu(gc != null ? gc.trim() : "");
                dsChiTiet.add(ui);
            }
            tableChiTiet.setItems(dsChiTiet);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    void handleTinhToanChenhLech(ActionEvent event) {
        String qNhap = "SELECT ISNULL(SUM(soLuong),0) FROM ChiTietPhieuNhap c JOIN PhieuNhap p ON c.maPhieuNhap=p.maPhieuNhap WHERE maLoThuoc=? AND p.ngayNhap >= ? AND p.ngayNhap <= ?";
        String qBan  = "SELECT ISNULL(SUM(soLuong),0) FROM ChiTietHoaDon c JOIN HoaDon h ON c.maHoaDon=h.maHoaDon WHERE maLoThuoc=? AND h.ngayLap >= ? AND h.ngayLap <= ?";
        String qTra  = "SELECT ISNULL(SUM(soLuong),0) FROM ChiTietPhieuXuat c JOIN PhieuXuat p ON c.maPhieuXuat=p.maPhieuXuat WHERE maLoThuoc=? AND p.ngayXuat >= ? AND p.ngayXuat <= ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pstN = con.prepareStatement(qNhap);
             PreparedStatement pstB = con.prepareStatement(qBan);
             PreparedStatement pstX = con.prepareStatement(qTra)) {
            for (ChiTietDuyetUI ui : dsChiTiet) {
                Timestamp t0 = new Timestamp(ui.ngayTaoT0.getTime());
                Timestamp countedAt = ui.thoiDiemDem != null ? ui.thoiDiemDem : new Timestamp(System.currentTimeMillis());
                pstN.setString(1, ui.maLoThuoc); pstN.setTimestamp(2, t0); pstN.setTimestamp(3, countedAt);
                ResultSet rsN = pstN.executeQuery(); int nhap = rsN.next() ? rsN.getInt(1) : 0;
                pstB.setString(1, ui.maLoThuoc); pstB.setTimestamp(2, t0); pstB.setTimestamp(3, countedAt);
                ResultSet rsB = pstB.executeQuery(); int ban = rsB.next() ? rsB.getInt(1) : 0;
                pstX.setString(1, ui.maLoThuoc); pstX.setTimestamp(2, t0); pstX.setTimestamp(3, countedAt);
                ResultSet rsX = pstX.executeQuery(); int xuat = rsX.next() ? rsX.getInt(1) : 0;
                ui.tonKyVongDong = ui.tonKhoSnapshot + nhap - (ban + xuat);
                ui.chenhLech = ui.soLuongKiemTra - ui.tonKyVongDong;
            }
            tableChiTiet.refresh();
            kiemTraNutXacNhan();
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã chốt Số lượng Kỳ vọng thời gian thực!");
        } catch (Exception e) { e.printStackTrace(); }
    }

    private void kiemTraNutXacNhan() {
        boolean allValid = true;
        for (ChiTietDuyetUI ui : dsChiTiet) {
            if (ui.chenhLech != 0 && (ui.lyDoLech == null || ui.lyDoLech.trim().isEmpty() || "Khớp số".equals(ui.lyDoLech))) {
                allValid = false;
                break;
            }
        }
        btnXacNhan.setDisable(!allValid);
    }

    @FXML
    void handleXacNhanDuyet(ActionEvent event) {
        handleTinhToanChenhLech(null);
        List<entity.ChiTietPhieuKiemKe> listToSave = new ArrayList<>();
        for (ChiTietDuyetUI ui : dsChiTiet) {
            entity.ChiTietPhieuKiemKe eCT = new entity.ChiTietPhieuKiemKe();
            eCT.setMaLoThuoc(ui.maLoThuoc);
            eCT.setSoLuongKiemTra(ui.soLuongKiemTra);
            eCT.setChenhLech(ui.chenhLech);
            eCT.setLyDoLech(ui.lyDoLech);
            eCT.setGhiChu(ui.getGhiChu() != null ? ui.getGhiChu().trim() : "");
            listToSave.add(eCT);
        }
        String maNV = UserSession.getInstance().getUser() != null
                ? UserSession.getInstance().getUser().getMaNhanVien() : "NV001";
        if (dao.chotSoKiemKeDuyet(currentMaPhieu, maNV, listToSave)) {
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "THÀNH CÔNG", "Đã chốt sổ!");
            handleQuayLai(null);
        }
    }
    
    // 🚨 HÀM XỬ LÝ HỦY KIỂM KÊ 🚨
    @FXML
    void handleHuyKiemKe(ActionEvent event) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION, 
                "Bạn có chắc chắn muốn hủy phiếu kiểm kê này?\n(Phiếu sẽ lưu trạng thái ĐÃ HỦY và không làm thay đổi tồn kho hiện tại)", 
                ButtonType.YES, ButtonType.NO);
        confirm.setHeaderText("Xác nhận Hủy Kiểm Kê");
        
        Optional<ButtonType> rs = confirm.showAndWait();
        if (rs.isPresent() && rs.get() == ButtonType.YES) {
            String sql = "UPDATE PhieuKiemKe SET trangThai = 'DA_HUY' WHERE maPhieuKiemKe = ?";
            try (Connection con = ConnectDB.getInstance().getConnection();
                 PreparedStatement pst = con.prepareStatement(sql)) {
                 
                pst.setString(1, currentMaPhieu);
                if (pst.executeUpdate() > 0) {
                    AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã hủy phiếu kiểm kê!");
                    handleQuayLai(null); // Quay lại trang danh sách
                } else {
                    AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể hủy phiếu kiểm kê!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi Database", "Lỗi trong quá trình hủy phiếu!");
            }
        }
    }

    @FXML
    void handleQuayLai(ActionEvent event) {
        viewDuyet.setVisible(false); viewDuyet.setManaged(false);
        viewDanhSach.setVisible(true); viewDanhSach.setManaged(true);
        loadDanhSach();
    }

    public static class ChiTietDuyetUI {
        public String maLoThuoc, tenThuoc, lyDoLech;
        public java.util.Date ngayTaoT0;
        public Timestamp thoiDiemDem;
        public int tonKhoSnapshot, tonKyVongDong, soLuongKiemTra, chenhLech;

        private final SimpleStringProperty ghiChu = new SimpleStringProperty("");

        public String getGhiChu() { return ghiChu.get(); }
        public void setGhiChu(String val) { this.ghiChu.set(val); }
        public SimpleStringProperty ghiChuProperty() { return ghiChu; }
    }
}