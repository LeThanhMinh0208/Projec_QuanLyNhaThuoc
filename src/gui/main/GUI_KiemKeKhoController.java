package gui.main;

import connectDB.ConnectDB;
import dao.DAO_KiemKeKho;
import entity.LoThuoc;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import utils.AlertUtils;
import utils.UserSession;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class GUI_KiemKeKhoController {

    @FXML private VBox viewLapPhieu, viewDangDem;
    @FXML private MenuButton mbNhomThuoc;
    @FXML private TableView<LoThuocUI> tableChonThuoc;
    @FXML private TableColumn<LoThuocUI, Boolean> colChon;
    @FXML private TableColumn<LoThuocUI, String> colMaLoLap, colTenThuocLap;
    @FXML private TableColumn<LoThuocUI, Integer> colTonKhoLap;
    @FXML private Label lblDaChon, lblMaPhieuDem;
    @FXML private TextField txtTimKiemThuoc;

    @FXML private TableView<ChiTietKiemKeUI> tableChiTietDem;
    @FXML private TableColumn<ChiTietKiemKeUI, String> colMaLoDem, colTenThuocDem;
    @FXML private TableColumn<ChiTietKiemKeUI, Integer> colTonSnapshotDem, colThucTeDem;

    private DAO_KiemKeKho dao = new DAO_KiemKeKho();
    private ObservableList<LoThuocUI> dsLoThuoc = FXCollections.observableArrayList();
    private ObservableList<ChiTietKiemKeUI> dsChiTiet = FXCollections.observableArrayList();

    private static String currentMaPhieu = null;
    private static boolean isDangKiemKe = false;

    @FXML
    public void initialize() {
        setupMenuNhomThuoc();
        setupTableLapPhieu();
        setupTableDem();
        if (isDangKiemKe && currentMaPhieu != null) {
            phucHoiManHinhDangDem();
        } else {
            loadDuLieuKhaDung();
            viewLapPhieu.setVisible(true); viewLapPhieu.setManaged(true);
            viewDangDem.setVisible(false); viewDangDem.setManaged(false);
        }
    }

    private void phucHoiManHinhDangDem() {
        viewLapPhieu.setVisible(false); viewLapPhieu.setManaged(false);
        viewDangDem.setVisible(true); viewDangDem.setManaged(true);
        lblMaPhieuDem.setText(currentMaPhieu);
        loadDuLieuDemKho();
    }

    private void setupMenuNhomThuoc() {
        mbNhomThuoc.getItems().clear();
        String[] danhMucs = {"Kháng sinh", "Giảm đau hạ sốt", "Vitamin - Khoáng chất", "Dạ dày",
                "Thuốc bôi ngoài da", "Thực phẩm chức năng", "Hô hấp", "Tim mạch", "Khác"};
        for (String dm : danhMucs) {
            CheckBox cb = new CheckBox(dm);
            cb.setStyle("-fx-text-fill: #334155; -fx-font-weight: bold; -fx-padding: 5 10; -fx-cursor: hand;");
            cb.selectedProperty().addListener((obs, o, n) -> filterVaTickNhom());
            CustomMenuItem item = new CustomMenuItem(cb);
            item.setHideOnClick(false);
            mbNhomThuoc.getItems().add(item);
        }
    }

    private void filterVaTickNhom() {
        List<String> nhomChon = new ArrayList<>();
        for (MenuItem item : mbNhomThuoc.getItems())
            if (((CheckBox) ((CustomMenuItem) item).getContent()).isSelected())
                nhomChon.add(((CheckBox) ((CustomMenuItem) item).getContent()).getText());
        for (LoThuocUI lo : dsLoThuoc)
            lo.setSelected(!nhomChon.isEmpty() && nhomChon.contains(lo.tenDanhMuc));
        tableChonThuoc.refresh();
        capNhatSoLuongChon();
    }

    @FXML
    void handleKiemKeThongMinh(ActionEvent event) {
        List<String> dsBienDong = dao.getDanhSachMaLoCoBienDong();
        for (LoThuocUI lo : dsLoThuoc) lo.setSelected(dsBienDong.contains(lo.getMaLoThuoc()));
        tableChonThuoc.refresh();
        capNhatSoLuongChon();
    }

    @FXML
    void handleKiemTatCa(ActionEvent event) {
        for (LoThuocUI lo : dsLoThuoc) lo.setSelected(true);
        tableChonThuoc.refresh();
        capNhatSoLuongChon();
    }

    private void setupTableLapPhieu() {
        colChon.setCellValueFactory(c -> c.getValue().selectedProperty());
        colChon.setCellFactory(CheckBoxTableCell.forTableColumn(colChon));
        colMaLoLap.setCellValueFactory(new PropertyValueFactory<>("maLoThuoc"));
        colTenThuocLap.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getThuoc().getTenThuoc()));
        colTonKhoLap.setCellValueFactory(new PropertyValueFactory<>("soLuongTon"));
        tableChonThuoc.setEditable(true);

        tableChonThuoc.setRowFactory(tv -> {
            TableRow<LoThuocUI> row = new TableRow<>();
            row.setOnMouseClicked(event -> {
                if (!row.isEmpty() && event.getButton() == javafx.scene.input.MouseButton.PRIMARY) {
                    LoThuocUI item = row.getItem();
                    item.setSelected(!item.isSelected());
                    capNhatSoLuongChon();
                    tableChonThuoc.refresh();
                }
            });
            return row;
        });
    }

    private void setupTableDem() {
        colMaLoDem.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().maLoThuoc));
        colTenThuocDem.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().tenThuoc));
        colTonSnapshotDem.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().tonKhoSnapshot).asObject());

        colThucTeDem.setCellValueFactory(c -> c.getValue().soLuongKiemTraProperty().asObject());

        // CHỐNG NUỐT CLICK
        viewDangDem.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_PRESSED, event -> {
            javafx.scene.Node target = (javafx.scene.Node) event.getTarget();
            while (target != null) {
                if (target instanceof TextField) return; 
                if (target instanceof Button) {
                    tableChiTietDem.requestFocus(); 
                    return;
                }
                target = target.getParent();
            }
        });

        // ===== CỘT NHẬP SỐ ĐẾM =====
        colThucTeDem.setCellFactory(column -> new TableCell<ChiTietKiemKeUI, Integer>() {
            private TextField textField;

            @Override
            public void startEdit() {
                super.startEdit();
                if (textField == null) {
                    textField = new TextField(getString());
                    textField.setStyle("-fx-text-fill: black; -fx-background-color: white; -fx-border-color: #0ea5e9; -fx-alignment: center;");
                    
                    textField.setOnAction(e -> xuLyNhapLieu(textField.getText()));
                    
                    textField.focusedProperty().addListener((obs, oldVal, newVal) -> {
                        if (!newVal) {
                            xuLyNhapLieu(textField.getText());
                        }
                    });
                }
                textField.setText(getString());
                setGraphic(textField);
                setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                javafx.application.Platform.runLater(() -> {
                    textField.requestFocus();
                    textField.selectAll();
                });
            }
            
            // 🚨 ĐÃ FIX: LẮP THÊM THÔNG BÁO NHỎ KHI GÕ LỐ 🚨
            private void xuLyNhapLieu(String text) {
                try {
                    int slNhap = Integer.parseInt(text.trim());
                    if (slNhap < 0) slNhap = 0; // Chặn số âm
                    
                    ChiTietKiemKeUI rowData = getTableRow().getItem();
                    if (rowData != null) {
                        int maxChoPhep = Math.max(rowData.tongNhapBanDau, rowData.tonKhoSnapshot);
                        
                        // Ép về Max nếu gõ lố và bật thông báo
                        if (maxChoPhep > 0 && slNhap > maxChoPhep) {
                            slNhap = maxChoPhep;
                            final int slThuc = slNhap; // Biến hằng để ném vào thread
                            
                            // Bọc trong Platform.runLater để không bị lỗi giật bảng của JavaFX
                            javafx.application.Platform.runLater(() -> {
                                AlertUtils.showAlert(Alert.AlertType.WARNING, "Vượt quá giới hạn", 
                                        "Số lượng đếm không được vượt quá số lượng đã nhập ban đầu (" + slThuc + ")!");
                            });
                        }
                    }
                    commitEdit(slNhap);
                } catch (NumberFormatException ex) { 
                    commitEdit(getItem() != null ? getItem() : 0); 
                }
            }

            @Override
            public void cancelEdit() {
                super.cancelEdit();
                setText(getString());
                setGraphic(null);
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }

            @Override
            public void commitEdit(Integer newValue) {
                super.commitEdit(newValue);
                setText(newValue == null ? "0" : newValue.toString());
                setGraphic(null);
                setContentDisplay(ContentDisplay.TEXT_ONLY);
            }

            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setText(null);
                    setGraphic(null);
                    return;
                }
                if (isEditing()) {
                    if (textField != null) textField.setText(getString());
                    setGraphic(textField);
                    setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
                } else {
                    setText(getString());
                    setGraphic(null);
                    setContentDisplay(ContentDisplay.TEXT_ONLY);
                    setStyle("-fx-text-fill: #0ea5e9; -fx-font-weight: bold; -fx-alignment: center;");
                }
            }

            private String getString() {
                return getItem() == null ? "0" : getItem().toString();
            }
        });

        colThucTeDem.setOnEditCommit(e -> {
            ChiTietKiemKeUI row = e.getRowValue();
            row.setSoLuongKiemTra(e.getNewValue() == null ? 0 : e.getNewValue());
            row.thoiDiemDem = new Timestamp(System.currentTimeMillis());
            tuDongLuuTungDong(row);
        });

        tableChiTietDem.setEditable(true);
        colThucTeDem.setEditable(true);
    }

    private void tuDongLuuTungDong(ChiTietKiemKeUI row) {
        String sql = "UPDATE ChiTietPhieuKiemKe SET soLuongKiemTra = ?, thoiDiemDem = ? WHERE maPhieuKiemKe = ? AND maLoThuoc = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setInt(1, row.getSoLuongKiemTra());
            pst.setTimestamp(2, row.thoiDiemDem);
            pst.setString(3, currentMaPhieu);
            pst.setString(4, row.maLoThuoc);
            pst.executeUpdate();
        } catch (Exception ex) { ex.printStackTrace(); }
    }

    private void loadDuLieuKhaDung() {
        dsLoThuoc.clear();
        for (Object[] obj : dao.getLoThuocKhaDungKemDanhMuc())
            dsLoThuoc.add(new LoThuocUI((LoThuoc) obj[0], (String) obj[1]));
        FilteredList<LoThuocUI> filteredData = new FilteredList<>(dsLoThuoc, b -> true);
        if (txtTimKiemThuoc != null) {
            txtTimKiemThuoc.textProperty().addListener((o, oldV, newV) -> {
                filteredData.setPredicate(lo -> {
                    if (newV == null || newV.isEmpty()) return true;
                    String lowerCaseFilter = newV.toLowerCase();
                    return lo.getMaLoThuoc().toLowerCase().contains(lowerCaseFilter)
                            || lo.getThuoc().getTenThuoc().toLowerCase().contains(lowerCaseFilter);
                });
            });
        }
        SortedList<LoThuocUI> sortedData = new SortedList<>(filteredData);
        sortedData.comparatorProperty().bind(tableChonThuoc.comparatorProperty());
        tableChonThuoc.setItems(sortedData);
        capNhatSoLuongChon();
    }

    private void capNhatSoLuongChon() {
        lblDaChon.setText("Đã chọn: " + dsLoThuoc.stream().filter(LoThuocUI::isSelected).count() + " mặt hàng");
    }

    @FXML
    void handleBatDauKiemKe(ActionEvent event) {
        List<LoThuoc> listChon = new ArrayList<>();
        for (LoThuocUI ui : dsLoThuoc) if (ui.isSelected()) listChon.add(ui);
        if (listChon.isEmpty()) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn thuốc!");
            return;
        }
        currentMaPhieu = "KK" + (new java.util.Random().nextInt(90000) + 10000);
        String maNV = UserSession.getInstance().getUser() != null
                ? UserSession.getInstance().getUser().getMaNhanVien() : "NV001";
        if (dao.taoPhieuKiemKe(currentMaPhieu, maNV, listChon)) {
            isDangKiemKe = true;
            viewLapPhieu.setVisible(false); viewLapPhieu.setManaged(false);
            viewDangDem.setVisible(true); viewDangDem.setManaged(true);
            lblMaPhieuDem.setText(currentMaPhieu);
            loadDuLieuDemKho();
        }
    }

    private void loadDuLieuDemKho() {
        dsChiTiet.clear();
        String sql = "SELECT c.*, t.tenThuoc, " +
                     "ISNULL((SELECT SUM(ctpn.soLuong * dq.tyLeQuyDoi) FROM ChiTietPhieuNhap ctpn JOIN DonViQuyDoi dq ON ctpn.maQuyDoi = dq.maQuyDoi WHERE ctpn.maLoThuoc = l.maLoThuoc), 0) as tongNhap " +
                     "FROM ChiTietPhieuKiemKe c " +
                     "JOIN LoThuoc l ON c.maLoThuoc = l.maLoThuoc " +
                     "JOIN Thuoc t ON l.maThuoc = t.maThuoc " +
                     "WHERE c.maPhieuKiemKe = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, currentMaPhieu);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                ChiTietKiemKeUI ui = new ChiTietKiemKeUI();
                ui.maLoThuoc = rs.getString("maLoThuoc");
                ui.tenThuoc = rs.getString("tenThuoc");
                ui.tonKhoSnapshot = rs.getInt("tonKhoSnapshot");
                ui.setSoLuongKiemTra(rs.getObject("soLuongKiemTra") != null
                        ? rs.getInt("soLuongKiemTra") : ui.tonKhoSnapshot);
                ui.thoiDiemDem = rs.getTimestamp("thoiDiemDem");
                ui.tongNhapBanDau = rs.getInt("tongNhap");
                dsChiTiet.add(ui);
            }
            tableChiTietDem.setItems(dsChiTiet);
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    void handleImportCSV(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Chọn file CSV nhập số lượng đếm");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fc.showOpenDialog(null);
        if (file != null) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line; int count = 0; boolean isFirstLine = true;
                List<String> canhBaoList = new ArrayList<>(); // Lưu lại các mã lô bị lố giới hạn
                
                while ((line = br.readLine()) != null) {
                    if (isFirstLine) {
                        line = line.replace("\uFEFF", "");
                        isFirstLine = false;
                        if (!line.toUpperCase().startsWith("LO")) continue;
                    }
                    String[] parts = line.split(",");
                    if (parts.length >= 2) {
                        String maLo = parts[0].trim().toUpperCase();
                        try {
                            int slNhap = Integer.parseInt(parts[1].trim());
                            if (slNhap < 0) slNhap = 0; 
                            for (ChiTietKiemKeUI ui : dsChiTiet) {
                                if (ui.maLoThuoc.toUpperCase().equals(maLo)) {
                                    
                                    int maxChoPhep = Math.max(ui.tongNhapBanDau, ui.tonKhoSnapshot);
                                    if(maxChoPhep > 0 && slNhap > maxChoPhep) {
                                        slNhap = maxChoPhep;
                                        canhBaoList.add(maLo); // Ghi nhận mã lô bị ép số
                                    }
                                    
                                    ui.setSoLuongKiemTra(slNhap);
                                    ui.thoiDiemDem = new Timestamp(System.currentTimeMillis());
                                    tuDongLuuTungDong(ui);
                                    count++;
                                }
                            }
                        } catch (NumberFormatException ignored) {}
                    }
                }
                tableChiTietDem.refresh();
                
                String tb = "Đã cập nhật thành công " + count + " lô thuốc từ file!";
                if (!canhBaoList.isEmpty()) {
                    tb += "\n⚠️ Có " + canhBaoList.size() + " lô thuốc vượt quá số lượng nhập ban đầu đã được tự động điều chỉnh về mức tối đa.";
                    AlertUtils.showAlert(Alert.AlertType.WARNING, "Import CSV Thành Công (Có Cảnh Báo)", tb);
                } else {
                    AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Import CSV Thành Công", tb);
                }
                
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi đọc file", "Định dạng file không hợp lệ.");
            }
        }
    }

    @FXML
    void handleNopPhieuKiemKe(ActionEvent event) {
         tableChiTietDem.edit(-1, null);
        if (luuDataChiTietTruocKhiNop()) {
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Hoàn tất đếm", "Đã nộp phiếu chờ duyệt.");
            isDangKiemKe = false;
            currentMaPhieu = null;
            viewDangDem.setVisible(false); viewDangDem.setManaged(false);
            viewLapPhieu.setVisible(true); viewLapPhieu.setManaged(true);
            loadDuLieuKhaDung();
        }
    }

    private boolean luuDataChiTietTruocKhiNop() {
        for (ChiTietKiemKeUI ui : dsChiTiet) {
            if (ui.thoiDiemDem == null) {
                ui.thoiDiemDem = new Timestamp(System.currentTimeMillis());
            }
            tuDongLuuTungDong(ui);
        }

        String sqlChotPhieu = "UPDATE PhieuKiemKe SET trangThai = 'CHO_DUYET' WHERE maPhieuKiemKe = ?";
        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pstPhieu = con.prepareStatement(sqlChotPhieu)) {
            pstPhieu.setString(1, currentMaPhieu);
            pstPhieu.executeUpdate();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @FXML
    void handleImportCSVChonThuoc(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Chọn file CSV danh sách Mã Lô cần kiểm kê");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fc.showOpenDialog(null);
        if (file != null) {
            try (BufferedReader br = new BufferedReader(new FileReader(file))) {
                String line; int count = 0;
                for (LoThuocUI lo : dsLoThuoc) lo.setSelected(false);
                List<String> listMaLoImport = new ArrayList<>();
                boolean isFirstLine = true;
                while ((line = br.readLine()) != null) {
                    if (isFirstLine) {
                        line = line.replace("\uFEFF", "");
                        isFirstLine = false;
                        if (!line.toUpperCase().startsWith("LO")) continue;
                    }
                    String[] parts = line.split(",");
                    if (parts.length >= 1) listMaLoImport.add(parts[0].trim().toUpperCase());
                }
                for (LoThuocUI lo : dsLoThuoc) {
                    if (listMaLoImport.contains(lo.getMaLoThuoc().toUpperCase())) {
                        lo.setSelected(true);
                        count++;
                    }
                }
                tableChonThuoc.refresh();
                capNhatSoLuongChon();
                if (count > 0)
                    AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Thành công",
                            "Đã quét và tick chọn tự động " + count + " lô thuốc từ file!");
                else
                    AlertUtils.showAlert(Alert.AlertType.WARNING, "Cảnh báo",
                            "Không tìm thấy Mã Lô nào trong file khớp với kho hiện tại!");
            } catch (Exception e) {
                e.printStackTrace();
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi đọc file",
                        "Định dạng file không hợp lệ! Vui lòng kiểm tra lại file CSV.");
            }
        }
    }

    public class LoThuocUI extends LoThuoc {
        private BooleanProperty selected = new SimpleBooleanProperty(false);
        public String tenDanhMuc;

        public LoThuocUI(LoThuoc lo, String dm) {
            this.setMaLoThuoc(lo.getMaLoThuoc());
            this.setSoLuongTon(lo.getSoLuongTon());
            this.setThuoc(lo.getThuoc());
            this.tenDanhMuc = dm;
        }

        public BooleanProperty selectedProperty() { return selected; }
        public boolean isSelected() { return selected.get(); }
        public void setSelected(boolean sel) { this.selected.set(sel); }
    }

    public class ChiTietKiemKeUI {
        public String maLoThuoc, tenThuoc;
        public int tonKhoSnapshot;
        public int tongNhapBanDau; 
        private SimpleIntegerProperty soLuongKiemTra = new SimpleIntegerProperty(0);
        public Timestamp thoiDiemDem;

        public IntegerProperty soLuongKiemTraProperty() { return soLuongKiemTra; }
        public int getSoLuongKiemTra() { return soLuongKiemTra.get(); }
        public void setSoLuongKiemTra(int val) { soLuongKiemTra.set(val); }
    }
}