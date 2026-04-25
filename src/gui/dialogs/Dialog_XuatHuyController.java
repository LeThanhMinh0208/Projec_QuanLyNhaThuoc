package gui.dialogs;

import dao.*;
import entity.*;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.*;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory; // Dòng này cực kỳ quan trọng!
import javafx.stage.Stage;
import javafx.util.StringConverter;
import utils.*;
import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.event.ActionEvent;

public class Dialog_XuatHuyController implements Initializable {
    @FXML private TextField txtNguoiLap, txtGhiChu, txtSoLuongHuy, txtTimNhanhThuoc;
    @FXML private ComboBox<Thuoc> cbChonThuoc;
    @FXML private ComboBox<LoThuoc> cbChonLo;
    @FXML private TableView<ChiTietPhieuXuat> tableThuocHuy;
    @FXML private TableColumn<ChiTietPhieuXuat, Integer> colSTT, colSoLuong;
    @FXML private TableColumn<ChiTietPhieuXuat, String> colTenThuoc, colSoLo;
    @FXML private TableColumn<ChiTietPhieuXuat, Void> colXoa;
    @FXML private TextField txtNoiHuy;

    private ObservableList<ChiTietPhieuXuat> dsHuyTam = FXCollections.observableArrayList();
    private ObservableList<Thuoc> masterListThuoc = FXCollections.observableArrayList();
    private DAO_LoThuoc daoLo = new DAO_LoThuoc();
    private DAO_PhieuXuat daoPX = new DAO_PhieuXuat();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        txtNoiHuy.setText("Hủy Rác Y Tế");
        if (UserSession.getInstance().getUser() != null) {
            txtNguoiLap.setText(UserSession.getInstance().getUser().getHoTen());
        }

        // 1. TẢI TẤT CẢ THUỐC CÓ HÀNG TRONG HỆ THỐNG (KHÔNG PHÂN BIỆT KHO)
        loadTatCaThuocCoHang();
        FilteredList<Thuoc> filter = new FilteredList<>(masterListThuoc, p -> true);
        cbChonThuoc.setItems(filter);

        txtTimNhanhThuoc.textProperty().addListener((o, old, newV) -> {
            filter.setPredicate(t -> newV == null || newV.isEmpty() || t.getTenThuoc().toLowerCase().contains(newV.toLowerCase()));
            if (!newV.isEmpty()) cbChonThuoc.show();
        });

        cbChonThuoc.valueProperty().addListener((o, old, t) -> {
            if (t != null) {
                // Tải tất cả các lô của thuốc này ở mọi kho
                cbChonLo.setItems(FXCollections.observableArrayList(daoLo.getTatCaLoThuocTraNCC(t.getMaThuoc())));
            }
        });

        cbChonLo.setConverter(new StringConverter<LoThuoc>() {
            @Override public String toString(LoThuoc l) { 
                if (l == null) return "";
                String k = "KHO_BAN_HANG".equals(l.getViTriKho()) ? "Bán hàng" : "Dự trữ";
                return "Lô: " + l.getMaLoThuoc() + " (Tồn: " + l.getSoLuongTon() + " | Kho: " + k + ")";
            }
            @Override public LoThuoc fromString(String s) { return null; }
        });
    }

    private void loadTatCaThuocCoHang() {
        String sql = "SELECT DISTINCT t.* FROM Thuoc t JOIN LoThuoc l ON t.maThuoc = l.maThuoc WHERE l.soLuongTon > 0 AND l.trangThai = 1";
        try (Connection con = connectDB.ConnectDB.getInstance().getConnection();
             Statement st = con.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                Thuoc t = new Thuoc();
                t.setMaThuoc(rs.getString("maThuoc"));
                t.setTenThuoc(rs.getString("tenThuoc"));
                masterListThuoc.add(t);
            }
        } catch (Exception e) { e.printStackTrace(); }
    }

    @FXML
    private void handleImportCSV(javafx.event.ActionEvent event) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Chọn file CSV Xuất Hủy");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        File file = fileChooser.showOpenDialog(txtNguoiLap.getScene().getWindow());

        if (file != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream(file), "UTF-8"))) {
                String line; int lineNumber = 0; int count = 0;
                dsHuyTam.clear();
                while ((line = br.readLine()) != null) {
                    lineNumber++;
                    if (lineNumber == 1 && line.startsWith("\uFEFF")) line = line.substring(1);
                    if (lineNumber == 1 || line.trim().isEmpty()) continue; // Bỏ qua tiêu đề

                    String[] cols = line.split(line.contains(";") ? ";" : ",");
                    if (cols.length < 3) continue;

                    String tenThuoc = cols[0].trim().replaceAll("^\"|\"$", "");
                    String soLo = cols[1].trim().replaceAll("^\"|\"$", "");
                    int slHuy = Integer.parseInt(cols[2].replaceAll("[^\\d]", ""));

                    // Tìm lô thuốc bất kể kho
                    String sql = "SELECT l.* FROM LoThuoc l JOIN Thuoc t ON l.maThuoc = t.maThuoc " +
                                 "WHERE t.tenThuoc = ? AND l.maLoThuoc = ?";
                    try (Connection con = connectDB.ConnectDB.getInstance().getConnection();
                         PreparedStatement pst = con.prepareStatement(sql)) {
                        pst.setString(1, tenThuoc); pst.setString(2, soLo);
                        ResultSet rs = pst.executeQuery();
                        if (rs.next()) {
                            if (slHuy <= rs.getInt("soLuongTon")) {
                                dsHuyTam.add(new ChiTietPhieuXuat(null, rs.getString("maThuoc"), soLo, slHuy, rs.getDouble("giaNhap"), slHuy * rs.getDouble("giaNhap")));
                                count++;
                            }
                        }
                    }
                }
                tableThuocHuy.refresh();
                AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã nạp xong " + count + " lô thuốc cần hủy.");
            } catch (Exception e) { AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi đọc file: " + e.getMessage()); }
        }
    }

    private void setupTable() {
        colSTT.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(dsHuyTam.indexOf(c.getValue()) + 1));
        colTenThuoc.setCellValueFactory(c -> {
            String ma = c.getValue().getMaThuoc();
            for(Thuoc t : masterListThuoc) if(t.getMaThuoc().equals(ma)) return new javafx.beans.property.SimpleStringProperty(t.getTenThuoc());
            return new javafx.beans.property.SimpleStringProperty(ma);
        });
        colSoLo.setCellValueFactory(new PropertyValueFactory<>("soLo"));
        colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        colXoa.setCellFactory(c -> new TableCell<>() {
            private final Button b = new Button("✕");
            { b.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-font-weight: bold;");
              b.setOnAction(e -> { dsHuyTam.remove(getTableView().getItems().get(getIndex())); tableThuocHuy.refresh(); }); }
            @Override protected void updateItem(Void i, boolean e) { super.updateItem(i, e); setGraphic(e ? null : b); setAlignment(Pos.CENTER); }
        });
        tableThuocHuy.setItems(dsHuyTam);
    }

    @FXML private void handleThemThuoc() {
        Thuoc t = cbChonThuoc.getValue(); LoThuoc l = cbChonLo.getValue();
        if (t == null || l == null || txtSoLuongHuy.getText().isEmpty()) return;
        int sl = Integer.parseInt(txtSoLuongHuy.getText().replaceAll("[^\\d]", ""));
        if (sl > l.getSoLuongTon()) { AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Vượt tồn kho!"); return; }
        dsHuyTam.add(new ChiTietPhieuXuat(null, t.getMaThuoc(), l.getMaLoThuoc(), sl, l.getGiaNhap(), sl * l.getGiaNhap()));
        txtSoLuongHuy.clear();
    }

    @FXML private void handleXacNhanHuy() {
        if (dsHuyTam.isEmpty() || txtGhiChu.getText().isEmpty()) { AlertUtils.showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập đầy đủ thuốc và lý do!"); return; }
        double tongTien = 0; for (ChiTietPhieuXuat ct : dsHuyTam) tongTien += ct.getThanhTien();
        String ma = daoPX.getMaPhieuXuatMoi("XH");
        PhieuXuat px = new PhieuXuat(ma, null, UserSession.getInstance().getUser().getMaNhanVien(), 3, null, null, tongTien, txtGhiChu.getText());
        if (daoPX.xuatHuyThuoc(px, new ArrayList<>(dsHuyTam))) {
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã lưu phiếu xuất hủy: " + ma);
            handleHuyBo();
        }
    }

    @FXML private void handleHuyBo() { ((Stage) txtNguoiLap.getScene().getWindow()).close(); }
}