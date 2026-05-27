package gui.dialogs;

import dao.*;
import dao.DAO_NhatKyHoatDong;
import entity.*;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.*;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import utils.*;

import java.io.File;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import javafx.event.ActionEvent;
import javafx.stage.FileChooser;

public class Dialog_XuatTraNCCController {
    
    @FXML private ComboBox<NhaCungCap> cbNhaCungCap;
    @FXML private TextField txtNguoiLap, txtGhiChu, txtSoLuongTra;
    
    @FXML private TextField txtTimNhanhThuoc;
    @FXML private ComboBox<Thuoc> cbChonThuoc;
    @FXML private ComboBox<LoThuoc> cbChonLo;
    
    @FXML private TableView<ChiTietPhieuXuat> tableThuocTra;
    @FXML private TableColumn<ChiTietPhieuXuat, Integer> colSTT, colSoLuong;
    @FXML private TableColumn<ChiTietPhieuXuat, String> colTenThuoc, colSoLo;
    @FXML private TableColumn<ChiTietPhieuXuat, Double> colThanhTien;
    @FXML private TableColumn<ChiTietPhieuXuat, Void> colXoa;
    @FXML private Label lblTongTien;

    private ObservableList<ChiTietPhieuXuat> dsTraTam = FXCollections.observableArrayList();
    private ObservableList<Thuoc> masterListThuoc = FXCollections.observableArrayList();
    private FilteredList<Thuoc> filterThuoc;
    
    private DAO_LoThuoc daoLo = new DAO_LoThuoc();
    private DAO_PhieuXuat daoPX = new DAO_PhieuXuat();
    private DecimalFormat df = new DecimalFormat("#,##0");
    private double tongTienHoan = 0;

    @FXML 
    public void initialize() {
        setupTable();
        
        if (UserSession.getInstance().getUser() != null) {
            txtNguoiLap.setText(UserSession.getInstance().getUser().getHoTen());
        }

        // 1. SETUP NHÀ CUNG CẤP
        cbNhaCungCap.setItems(FXCollections.observableArrayList(new DAO_NhaCungCap().getAllNhaCungCap()));
        cbNhaCungCap.setConverter(new StringConverter<NhaCungCap>() {
            @Override public String toString(NhaCungCap n) { return n == null ? "" : n.getTenNhaCungCap(); }
            @Override public NhaCungCap fromString(String s) { return null; }
        });
        
        cbNhaCungCap.valueProperty().addListener((obs, oldV, newV) -> loadDanhSachThuocTheoNCC());

        // 2. SETUP COMBOBOX THUỐC
        filterThuoc = new FilteredList<>(masterListThuoc, p -> true);
        cbChonThuoc.setItems(filterThuoc);
        setupComboThuoc();

        txtTimNhanhThuoc.textProperty().addListener((o, oldV, newV) -> {
            filterThuoc.setPredicate(t -> {
                if (newV == null || newV.isEmpty()) return true;
                String lowerCaseFilter = newV.toLowerCase();
                return t.getTenThuoc().toLowerCase().contains(lowerCaseFilter) || 
                       t.getMaThuoc().toLowerCase().contains(lowerCaseFilter);
            });
            if (!newV.isEmpty()) {
                javafx.application.Platform.runLater(() -> {
                    cbChonThuoc.hide();
                    cbChonThuoc.setItems(filterThuoc);
                    if(!filterThuoc.isEmpty()) cbChonThuoc.show();
                    txtTimNhanhThuoc.requestFocus();
                    txtTimNhanhThuoc.positionCaret(txtTimNhanhThuoc.getText().length());
                });
            }
        });

        cbChonThuoc.valueProperty().addListener((o, oldV, t) -> loadDanhSachLoTheoThuocVaNCC(t));

        // 3. SETUP COMBOBOX LÔ THUỐC
        cbChonLo.setConverter(new StringConverter<LoThuoc>() {
            @Override public String toString(LoThuoc l) { 
                if (l == null) return "";
                String tenKho = "KHO_BAN_HANG".equals(l.getViTriKho()) ? "Bán hàng" : "Dự trữ";
                return "Lô: " + l.getMaLoThuoc() + " (Tồn: " + l.getSoLuongTon() + " | Kho: " + tenKho + ")"; 
            }
            @Override public LoThuoc fromString(String s) { return null; }
        });
    }

    private void loadDanhSachThuocTheoNCC() {
        NhaCungCap ncc = cbNhaCungCap.getValue();
        masterListThuoc.clear();
        cbChonThuoc.getSelectionModel().clearSelection();
        cbChonLo.getItems().clear();

        if (ncc != null) {
            String sql = "SELECT DISTINCT t.maThuoc, t.tenThuoc, t.hinhAnh, t.donViCoBan " +
                         "FROM Thuoc t JOIN LoThuoc l ON t.maThuoc = l.maThuoc " +
                         "WHERE l.maNhaCungCap = ? AND l.soLuongTon > 0 AND l.trangThai = 1";
            try (Connection con = connectDB.ConnectDB.getInstance().getConnection();
                 PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, ncc.getMaNhaCungCap());
                ResultSet rs = pst.executeQuery();
                while(rs.next()){
                    Thuoc t = new Thuoc();
                    t.setMaThuoc(rs.getString("maThuoc"));
                    t.setTenThuoc(rs.getString("tenThuoc"));
                    t.setHinhAnh(rs.getString("hinhAnh"));
                    t.setDonViCoBan(rs.getString("donViCoBan")); 
                    masterListThuoc.add(t);
                }
            } catch (Exception e) { 
                e.printStackTrace(); 
            }
            
            if (masterListThuoc.isEmpty()) {
                cbChonThuoc.setPromptText("NCC này không có thuốc trong kho!");
            } else {
                cbChonThuoc.setPromptText("-- Chọn thuốc --");
            }
        }
    }

    private void loadDanhSachLoTheoThuocVaNCC(Thuoc t) {
        NhaCungCap ncc = cbNhaCungCap.getValue();
        if (t != null && ncc != null) {
            List<LoThuoc> tatCaCacLo = daoLo.getTatCaLoThuocTraNCC(t.getMaThuoc());
            List<LoThuoc> loCuaNCC = tatCaCacLo.stream()
                    .filter(l -> l.getNhaCungCap() != null && l.getNhaCungCap().getMaNhaCungCap().equals(ncc.getMaNhaCungCap()))
                    .collect(Collectors.toList());
            cbChonLo.setItems(FXCollections.observableArrayList(loCuaNCC));
        } else {
            cbChonLo.getItems().clear();
        }
    }

    private void setupComboThuoc() {
        cbChonThuoc.setCellFactory(lv -> new ListCell<Thuoc>() {
            private final ImageView iv = new ImageView();
            @Override protected void updateItem(Thuoc t, boolean empty) {
                super.updateItem(t, empty);
                if (empty || t == null) { setGraphic(null); setText(null); }
                else {
                    setText(t.getTenThuoc());
                    try {
                        InputStream is = getClass().getResourceAsStream("/resources/images/images_thuoc/" + t.getHinhAnh().trim());
                        if (is != null) { iv.setImage(new Image(is)); iv.setFitWidth(40); iv.setFitHeight(30); setGraphic(iv); }
                        else { setGraphic(null); }
                    } catch (Exception ex) { setGraphic(null); }
                }
            }
        });
        cbChonThuoc.setButtonCell(cbChonThuoc.getCellFactory().call(null));
    }

    private void setupTable() {
        colSTT.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(dsTraTam.indexOf(c.getValue()) + 1));
        
        colTenThuoc.setCellValueFactory(c -> {
            String ma = c.getValue().getMaThuoc();
            for(Thuoc t : masterListThuoc) {
                if(t.getMaThuoc().equals(ma)) return new javafx.beans.property.SimpleStringProperty(t.getTenThuoc());
            }
            return new javafx.beans.property.SimpleStringProperty(ma);
        });

        colSoLo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getSoLo()));
        colSoLuong.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getSoLuong()));
        
        colThanhTien.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getThanhTien()));
        colThanhTien.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : df.format(item));
            }
        });

        colXoa.setCellFactory(c -> new TableCell<>() {
            private final Button b = new Button("✕");
            { 
                b.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;"); 
                b.setOnAction(e -> { 
                    dsTraTam.remove(getTableView().getItems().get(getIndex())); 
                    tinhTongTien();
                    tableThuocTra.refresh(); 
                    if (dsTraTam.isEmpty()) {
                        cbNhaCungCap.setDisable(false);
                        cbNhaCungCap.setStyle("-fx-opacity: 1;");
                    }
                }); 
            }
            @Override protected void updateItem(Void i, boolean e) {
                super.updateItem(i, e); setGraphic(e ? null : b); setAlignment(Pos.CENTER);
            }
        });
        tableThuocTra.setItems(dsTraTam);
    }

    @FXML private void handleThemThuoc() {
        if (cbNhaCungCap.getValue() == null) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn Nhà Cung Cấp trước!");
            cbNhaCungCap.requestFocus();
            return;
        }
        
        Thuoc t = cbChonThuoc.getValue(); 
        LoThuoc l = cbChonLo.getValue();
        
        if (t == null || l == null || txtSoLuongTra.getText().isEmpty()) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn Thuốc, chọn Lô và nhập số lượng!");
            return;
        }
        
        for(ChiTietPhieuXuat ct : dsTraTam) {
            if(ct.getSoLo().equals(l.getMaLoThuoc())) {
                AlertUtils.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Lô thuốc này đã được thêm vào bảng xuất trả!");
                return;
            }
        }
        
        try {
            int sl = Integer.parseInt(txtSoLuongTra.getText().replaceAll("[^\\d]", ""));
            if (sl <= 0) return;
            
            if (sl > l.getSoLuongTon()) { 
                String donVi = (t.getDonViCoBan() != null && !t.getDonViCoBan().trim().isEmpty()) ? t.getDonViCoBan() : "sản phẩm";
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Vượt quá số lượng", 
                    "Số lượng xuất trả không được vượt quá số lượng đang tồn!\n" +
                    "Lô này trong kho chỉ còn: " + l.getSoLuongTon() + " " + donVi + "."); 
                txtSoLuongTra.requestFocus();
                return; 
            }
            
            double gia = l.getGiaNhap();
            dsTraTam.add(new ChiTietPhieuXuat(null, t.getMaThuoc(), l.getMaLoThuoc(), sl, gia, sl * gia));
            tinhTongTien();
            
            cbNhaCungCap.setDisable(true);
            cbNhaCungCap.setStyle("-fx-opacity: 1; -fx-background-color: #f1f5f9; -fx-font-weight: bold;");
            
            txtSoLuongTra.clear();
            cbChonThuoc.getSelectionModel().clearSelection();
            cbChonLo.getItems().clear();
            txtTimNhanhThuoc.clear();
            
        } catch (NumberFormatException e) {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập số lượng hợp lệ!");
        }
    }

    private void tinhTongTien() {
        tongTienHoan = 0;
        for (ChiTietPhieuXuat ct : dsTraTam) {
            tongTienHoan += ct.getThanhTien();
        }
        lblTongTien.setText(df.format(tongTienHoan) + " VNĐ");
    }

    @FXML private void handleXacNhanTra() {
        if (cbNhaCungCap.getValue() == null) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn Nhà cung cấp!");
            return;
        }
        if (dsTraTam.isEmpty()) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa có thuốc nào trong danh sách!");
            return;
        }

        String maPhieuMoi = daoPX.getMaPhieuXuatMoi("TN"); 
        
        PhieuXuat px = new PhieuXuat(
            maPhieuMoi, 
            null, 
            UserSession.getInstance().getUser().getMaNhanVien(), 
            2, 
            cbNhaCungCap.getValue().getMaNhaCungCap(), 
            null, 
            tongTienHoan, 
            txtGhiChu.getText()
        );
        
        if (daoPX.traNhaCungCap(px, new ArrayList<>(dsTraTam))) {
            DAO_NhatKyHoatDong.ghiLog("TAO_PHIEU_XUAT", "Phiếu Xuất", maPhieuMoi, "Tạo phiếu xuất trả NCC: " + maPhieuMoi);
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã trả hàng cho nhà cung cấp thành công!\nMã phiếu: " + maPhieuMoi); 
            ((Stage) txtGhiChu.getScene().getWindow()).close();
        } else {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Thất bại", "Lỗi khi lưu phiếu trả hàng!");
        }
    }

    @FXML
    private void handleTaiFileMau(ActionEvent event) {
        NhaCungCap ncc = cbNhaCungCap.getValue();
        if (ncc == null) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Chưa chọn NCC",
                "Vui lòng chọn Nhà Cung Cấp trước khi tải file mẫu.");
            cbNhaCungCap.requestFocus();
            return;
        }

        FileChooser fc = new FileChooser();
        fc.setTitle("Lưu file mẫu trả hàng NCC");
        fc.setInitialFileName("MauTraHang_" + ncc.getTenNhaCungCap().replaceAll("[^a-zA-Z0-9_]", "_") + ".xlsx");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fc.showSaveDialog(lblTongTien.getScene().getWindow());
        if (file == null) return;

        try {
            String sql = "SELECT t.tenThuoc, t.donViCoBan, l.maLoThuoc, l.viTriKho, l.soLuongTon " +
                         "FROM LoThuoc l JOIN Thuoc t ON l.maThuoc = t.maThuoc " +
                         "WHERE l.maNhaCungCap = ? AND l.soLuongTon > 0 AND l.trangThai = 1 " +
                         "ORDER BY t.tenThuoc, l.maLoThuoc";

            List<TraHangNCCExcelExporter.LoRow> dsLo = new ArrayList<>();
            try (Connection con = connectDB.ConnectDB.getInstance().getConnection();
                 PreparedStatement pst = con.prepareStatement(sql)) {
                pst.setString(1, ncc.getMaNhaCungCap());
                ResultSet rs = pst.executeQuery();
                while (rs.next()) {
                    dsLo.add(new TraHangNCCExcelExporter.LoRow(
                        rs.getString("tenThuoc"),
                        rs.getString("donViCoBan"),
                        rs.getString("maLoThuoc"),
                        rs.getString("viTriKho"),
                        rs.getInt("soLuongTon")
                    ));
                }
            }

            if (dsLo.isEmpty()) {
                AlertUtils.showAlert(Alert.AlertType.WARNING, "Không có dữ liệu",
                    "Nhà cung cấp này không có lô thuốc nào đang tồn kho.");
                return;
            }

            TraHangNCCExcelExporter.xuatFileMau(ncc.getTenNhaCungCap(), "", dsLo, file);
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Thành công",
                "Đã tạo file mẫu:\n" + file.getAbsolutePath());
            try { java.awt.Desktop.getDesktop().open(file); } catch (Exception ignored) {}
        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tạo file mẫu: " + e.getMessage());
        }
    }

    @FXML
    private void handleImportExcel(ActionEvent event) {
        FileChooser fc = new FileChooser();
        fc.setTitle("Chọn file Excel Trả Hàng NCC");
        fc.getExtensionFilters().add(new FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fc.showOpenDialog(lblTongTien.getScene().getWindow());
        if (file == null) return;

        try {
            TraHangNCCExcelImporter.KetQua kq = TraHangNCCExcelImporter.docFileExcel(file.getAbsolutePath());

            if (kq.tenNCC.isEmpty()) {
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Không đọc được Nhà Cung Cấp từ file.");
                return;
            }

            // Tìm và chọn NCC
            NhaCungCap nccFound = null;
            for (NhaCungCap ncc : cbNhaCungCap.getItems()) {
                if (ncc.getTenNhaCungCap().equalsIgnoreCase(kq.tenNCC)) {
                    nccFound = ncc;
                    break;
                }
            }
            if (nccFound == null) {
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi file",
                    "Nhà cung cấp \"" + kq.tenNCC + "\" không tồn tại trong hệ thống!");
                return;
            }

            cbNhaCungCap.setDisable(false);
            cbNhaCungCap.getSelectionModel().select(nccFound);

            dsTraTam.clear();
            List<String> dsLoi = new ArrayList<>();
            int successCount = 0;

            for (TraHangNCCExcelImporter.DongDuLieu dong : kq.dsDong) {
                if (dong.soLo.isEmpty()) {
                    dsLoi.add("\"" + dong.tenThuoc + "\" — thiếu số lô");
                    continue;
                }

                String sqlFindLo = "SELECT l.*, t.tenThuoc, t.maThuoc FROM LoThuoc l " +
                                   "JOIN Thuoc t ON l.maThuoc = t.maThuoc " +
                                   "WHERE t.tenThuoc = ? AND l.maLoThuoc = ? AND l.maNhaCungCap = ? " +
                                   "AND l.soLuongTon > 0 AND l.trangThai = 1";

                try (Connection con = connectDB.ConnectDB.getInstance().getConnection();
                     PreparedStatement pst = con.prepareStatement(sqlFindLo)) {
                    pst.setString(1, dong.tenThuoc);
                    pst.setString(2, dong.soLo);
                    pst.setString(3, nccFound.getMaNhaCungCap());
                    ResultSet rs = pst.executeQuery();

                    if (!rs.next()) {
                        dsLoi.add("\"" + dong.tenThuoc + "\" lô \"" + dong.soLo +
                                  "\" không có trong kho của " + kq.tenNCC);
                        continue;
                    }

                    double giaNhap   = rs.getDouble("giaNhap");
                    int    tonKho    = rs.getInt("soLuongTon");
                    String maThuoc   = rs.getString("maThuoc");
                    String maLo      = rs.getString("maLoThuoc");

                    int slTra = (dong.soLuongTra == -1) ? tonKho : dong.soLuongTra;

                    if (slTra <= 0) {
                        dsLoi.add("\"" + dong.tenThuoc + "\" lô \"" + dong.soLo +
                                  "\" — số lượng trả phải lớn hơn 0");
                        continue;
                    }
                    if (slTra > tonKho) {
                        dsLoi.add("\"" + dong.tenThuoc + "\" lô \"" + dong.soLo +
                                  "\" — SL trả (" + slTra + ") vượt quá tồn kho (" + tonKho + ")");
                        continue;
                    }

                    // Bỏ qua nếu lô đã có trong danh sách
                    boolean duplicate = false;
                    for (ChiTietPhieuXuat ct : dsTraTam) {
                        if (ct.getSoLo().equals(maLo)) { duplicate = true; break; }
                    }
                    if (duplicate) continue;

                    dsTraTam.add(new ChiTietPhieuXuat(null, maThuoc, maLo, slTra, giaNhap, slTra * giaNhap));
                    successCount++;
                }
            }

            if (successCount > 0) {
                cbNhaCungCap.setDisable(true);
                cbNhaCungCap.setStyle("-fx-opacity: 1; -fx-background-color: #f1f5f9; -fx-font-weight: bold;");
            }

            if (!kq.lyDo.isEmpty()) txtGhiChu.setText(kq.lyDo);
            tableThuocTra.refresh();
            tinhTongTien();

            StringBuilder msg = new StringBuilder(
                "Đã nạp " + successCount + "/" + kq.dsDong.size() + " lô thuốc của [" + kq.tenNCC + "].");
            if (!dsLoi.isEmpty()) {
                msg.append("\n\nCác mục không hợp lệ:");
                for (String err : dsLoi) msg.append("\n• ").append(err);
            }
            Alert.AlertType type = dsLoi.isEmpty() ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING;
            AlertUtils.showAlert(type, dsLoi.isEmpty() ? "Thành công" : "Hoàn thành (có cảnh báo)", msg.toString());

        } catch (Exception e) {
            e.printStackTrace();
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể đọc file: " + e.getMessage());
        }
    }

    @FXML private void handleHuyBo() { ((Stage) txtGhiChu.getScene().getWindow()).close(); }
}