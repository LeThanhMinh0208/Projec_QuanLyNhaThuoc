package gui.dialogs;

import dao.*;
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

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

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
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã lập phiếu Trả hàng NCC: " + maPhieuMoi); 
            ((Stage) txtGhiChu.getScene().getWindow()).close();
        } else {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Thất bại", "Lỗi khi lưu phiếu trả hàng!");
        }
    }

    @FXML
    private void handleImportCSV(javafx.event.ActionEvent event) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Chọn file CSV Trả hàng NCC");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        java.io.File file = fileChooser.showOpenDialog(lblTongTien.getScene().getWindow());

        if (file != null) {
            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(file), "UTF-8"))) {
                String line;
                int lineNumber = 0;
                String tenNCCFile = "";
                int count = 0;
                
                dsTraTam.clear();
                cbNhaCungCap.setDisable(false);

                while ((line = br.readLine()) != null) {
                    lineNumber++;
                    if (lineNumber == 1 && line.startsWith("\uFEFF")) line = line.substring(1);
                    if (line.trim().isEmpty()) continue;

                    String[] cols = line.split(line.contains(";") ? ";" : ",");

                    if (lineNumber == 1) {
                        if (cols.length >= 2) {
                            tenNCCFile = cols[1].trim().replaceAll("^\"|\"$", "");
                            for (NhaCungCap ncc : cbNhaCungCap.getItems()) {
                                if (ncc.getTenNhaCungCap().equalsIgnoreCase(tenNCCFile)) {
                                    cbNhaCungCap.getSelectionModel().select(ncc);
                                    break;
                                }
                            }
                        }
                        continue;
                    }

                    if (lineNumber == 2) continue;

                    if (cols.length < 3) continue;
                    String tenThuocFile = cols[0].trim().replaceAll("^\"|\"$", "");
                    String soLoFile = cols[1].trim().replaceAll("^\"|\"$", "");
                    int slTra = Integer.parseInt(cols[2].replaceAll("[^\\d]", ""));

                    NhaCungCap selectedNCC = cbNhaCungCap.getValue();
                    if (selectedNCC == null) break;

                    String sqlFindLo = "SELECT l.*, t.tenThuoc FROM LoThuoc l " +
                                       "JOIN Thuoc t ON l.maThuoc = t.maThuoc " +
                                       "WHERE t.tenThuoc = ? AND l.maLoThuoc = ? AND l.maNhaCungCap = ?";
                    
                    try (Connection con = connectDB.ConnectDB.getInstance().getConnection();
                         PreparedStatement pst = con.prepareStatement(sqlFindLo)) {
                        pst.setString(1, tenThuocFile);
                        pst.setString(2, soLoFile);
                        pst.setString(3, selectedNCC.getMaNhaCungCap());
                        ResultSet rs = pst.executeQuery();
                        
                        if (rs.next()) {
                            double giaNhap = rs.getDouble("giaNhap");
                            int tonHT = rs.getInt("soLuongTon");
                            String maThuoc = rs.getString("maThuoc");
                            
                            if (slTra <= tonHT) {
                                dsTraTam.add(new ChiTietPhieuXuat(null, maThuoc, soLoFile, slTra, giaNhap, slTra * giaNhap));
                                count++;
                            }
                        }
                    }
                }

                if (count > 0) {
                    cbNhaCungCap.setDisable(true);
                    tableThuocTra.refresh();
                    tinhTongTien();
                    AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã nạp xong " + count + " mặt hàng trả cho " + tenNCCFile);
                } else {
                    AlertUtils.showAlert(Alert.AlertType.WARNING, "Thông báo", "Không tìm thấy dữ liệu khớp hoặc NCC không chính xác!");
                }

            } catch (Exception e) {
                e.printStackTrace();
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi đọc file: " + e.getMessage());
            }
        }
    }
    
    @FXML private void handleHuyBo() { ((Stage) txtGhiChu.getScene().getWindow()).close(); }
}