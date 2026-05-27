package gui.dialogs;

import dao.*;
import dao.DAO_NhatKyHoatDong;
import entity.*;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.*;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import utils.*;
import java.io.*;
import java.net.URL;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
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

        loadTatCaThuocCoHang();
        FilteredList<Thuoc> filter = new FilteredList<>(masterListThuoc, p -> true);
        cbChonThuoc.setItems(filter);

        txtTimNhanhThuoc.textProperty().addListener((o, old, newV) -> {
            filter.setPredicate(t -> newV == null || newV.isEmpty() || t.getTenThuoc().toLowerCase().contains(newV.toLowerCase()));
            if (!newV.isEmpty()) cbChonThuoc.show();
        });

        cbChonThuoc.valueProperty().addListener((o, old, t) -> {
            if (t != null) {
                cbChonLo.setItems(FXCollections.observableArrayList(daoLo.getTatCaLoThuocChoXuatHuy(t.getMaThuoc())));
            }
        });

        cbChonLo.setConverter(new StringConverter<LoThuoc>() {
            @Override public String toString(LoThuoc l) {
                if (l == null) return "";
                String k = "KHO_BAN_HANG".equals(l.getViTriKho()) ? "Bán hàng" : "Dự trữ";
                String hetHan = l.getTrangThai() == 0 ? " [Hết hạn]" : "";
                return "Lô: " + l.getMaLoThuoc() + " (Tồn: " + l.getSoLuongTon() + " | Kho: " + k + ")" + hetHan;
            }
            @Override public LoThuoc fromString(String s) { return null; }
        });
    }

    private void loadTatCaThuocCoHang() {
        String sql = "SELECT DISTINCT t.* FROM Thuoc t JOIN LoThuoc l ON t.maThuoc = l.maThuoc WHERE l.soLuongTon > 0";
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
    private void handleTaiFileMau(ActionEvent event) {
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Lưu File Mẫu Xuất Hủy");
        fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        fc.setInitialFileName("MauXuatHuyThuoc.xlsx");
        File file = fc.showSaveDialog(txtNguoiLap.getScene().getWindow());
        if (file == null) return;

        String sql = "SELECT t.tenThuoc, l.maLoThuoc, l.viTriKho, l.soLuongTon " +
                     "FROM LoThuoc l JOIN Thuoc t ON l.maThuoc = t.maThuoc " +
                     "WHERE l.soLuongTon > 0 AND l.trangThai = 1 " +
                     "ORDER BY t.tenThuoc, l.maLoThuoc";
        List<XuatHuyExcelExporter.LoRow> dsLo = new ArrayList<>();
        try (Connection con = connectDB.ConnectDB.getInstance().getConnection();
             Statement st = con.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                dsLo.add(new XuatHuyExcelExporter.LoRow(
                    rs.getString("tenThuoc"),
                    rs.getString("maLoThuoc"),
                    rs.getString("viTriKho"),
                    rs.getInt("soLuongTon")
                ));
            }
        } catch (Exception e) {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi truy vấn: " + e.getMessage());
            return;
        }

        try {
            XuatHuyExcelExporter.xuatFileMau(txtGhiChu.getText(), dsLo, file);
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã tạo file mẫu:\n" + file.getName());
        } catch (Exception e) {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể tạo file: " + e.getMessage());
        }
    }

    @FXML
    private void handleImportExcel(ActionEvent event) {
        javafx.stage.FileChooser fc = new javafx.stage.FileChooser();
        fc.setTitle("Chọn file Excel Xuất Hủy");
        fc.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("Excel Files", "*.xlsx"));
        File file = fc.showOpenDialog(txtNguoiLap.getScene().getWindow());
        if (file == null) return;

        try {
            XuatHuyExcelImporter.KetQua kq = XuatHuyExcelImporter.docFileExcel(file.getPath());
            if (kq.lyDo != null && !kq.lyDo.isEmpty()) txtGhiChu.setText(kq.lyDo);

            dsHuyTam.clear();
            List<String> dsLoi = new ArrayList<>();
            int count = 0;

            for (XuatHuyExcelImporter.DongDuLieu dong : kq.dsDong) {
                String sqlQ = "SELECT l.maThuoc, l.soLuongTon, l.giaNhap FROM LoThuoc l " +
                             "JOIN Thuoc t ON l.maThuoc = t.maThuoc " +
                             "WHERE t.tenThuoc = ? AND l.maLoThuoc = ? AND l.soLuongTon > 0";
                try (Connection con = connectDB.ConnectDB.getInstance().getConnection();
                     PreparedStatement pst = con.prepareStatement(sqlQ)) {
                    pst.setString(1, dong.tenThuoc);
                    pst.setString(2, dong.soLo);
                    ResultSet rs = pst.executeQuery();
                    if (rs.next()) {
                        if (dong.soLuongHuy == -1) continue;
                        int tonKho = rs.getInt("soLuongTon");
                        int slHuy = dong.soLuongHuy;
                        if (slHuy > tonKho) {
                            dsLoi.add(dong.tenThuoc + " / " + dong.soLo + " (SL hủy vượt tồn kho)");
                            continue;
                        }
                        double giaNhap = rs.getDouble("giaNhap");
                        dsHuyTam.add(new ChiTietPhieuXuat(null, rs.getString("maThuoc"), dong.soLo, slHuy, giaNhap, slHuy * giaNhap));
                        count++;
                    } else {
                        dsLoi.add(dong.tenThuoc + " / " + dong.soLo + " (không tìm thấy trong hệ thống)");
                    }
                }
            }

            tableThuocHuy.refresh();
            if (!dsLoi.isEmpty()) {
                AlertUtils.showAlert(Alert.AlertType.WARNING, "Hoàn tất (có lỗi)",
                    "Đã nạp " + count + " lô hợp lệ.\nCác lô không hợp lệ:\n- " + String.join("\n- ", dsLoi));
            } else {
                AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã nạp " + count + " lô thuốc cần hủy.");
            }
        } catch (Exception e) {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Lỗi đọc file: " + e.getMessage());
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
        if (dsHuyTam.isEmpty() || txtGhiChu.getText().isEmpty()) { 
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập đầy đủ thuốc và lý do!"); 
            return; 
        }
        double tongTien = 0; for (ChiTietPhieuXuat ct : dsHuyTam) tongTien += ct.getThanhTien();
        String ma = daoPX.getMaPhieuXuatMoi("XH");
        PhieuXuat px = new PhieuXuat(ma, null, UserSession.getInstance().getUser().getMaNhanVien(), 3, null, null, tongTien, txtGhiChu.getText());
        
        if (daoPX.xuatHuyThuoc(px, new ArrayList<>(dsHuyTam))) {
            // Ghi log hoạt động từ bản Incoming
            DAO_NhatKyHoatDong.ghiLog("TAO_PHIEU_XUAT", "Phiếu Xuất", ma, "Tạo phiếu xuất hủy: " + ma);
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã hủy thuốc thành công!\nMã phiếu: " + ma);
            handleHuyBo();
        } else {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Thất bại", "Lỗi khi lưu phiếu xuất hủy!");
        }
    }

    @FXML private void handleHuyBo() { ((Stage) txtNguoiLap.getScene().getWindow()).close(); }
}