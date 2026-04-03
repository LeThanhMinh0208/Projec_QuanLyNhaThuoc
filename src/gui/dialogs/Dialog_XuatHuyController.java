package gui.dialogs;

import dao.*;
import entity.*;
import javafx.beans.property.ReadOnlyObjectWrapper;
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
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Dialog_XuatHuyController implements Initializable {
    @FXML private ComboBox<String> cbKhoXuat;
    @FXML private TextField txtNguoiLap, txtGhiChu, txtSoLuongHuy, txtTimNhanhThuoc;
    @FXML private ComboBox<Thuoc> cbChonThuoc;
    @FXML private ComboBox<LoThuoc> cbChonLo;
    
    @FXML private TableView<ChiTietPhieuXuat> tableThuocHuy;
    @FXML private TableColumn<ChiTietPhieuXuat, Integer> colSTT, colSoLuong;
    @FXML private TableColumn<ChiTietPhieuXuat, String> colTenThuoc, colSoLo;
    @FXML private TableColumn<ChiTietPhieuXuat, Void> colXoa;

    private ObservableList<ChiTietPhieuXuat> dsHuyTam = FXCollections.observableArrayList();
    private DAO_LoThuoc daoLo = new DAO_LoThuoc();
    private DAO_PhieuXuat daoPX = new DAO_PhieuXuat();
    private double tongGiaTriThietHai = 0; // Để log lại thiệt hại nếu cần

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        if (UserSession.getInstance().getUser() != null) {
            txtNguoiLap.setText(UserSession.getInstance().getUser().getHoTen());
        }

        // Setup Kho Xuất
        cbKhoXuat.getItems().addAll("Kho dự trữ", "Kho bán hàng");
        cbKhoXuat.getSelectionModel().selectFirst();
        cbKhoXuat.valueProperty().addListener((o, oldV, newV) -> cbChonLo.getItems().clear());

        // Setup Chọn Thuốc (Có tìm kiếm + Hình ảnh)
        ObservableList<Thuoc> allThuoc = FXCollections.observableArrayList(new DAO_Thuoc().getAllThuoc());
        FilteredList<Thuoc> filter = new FilteredList<>(allThuoc, p -> true);
        cbChonThuoc.setItems(filter);
        setupComboThuoc();

        txtTimNhanhThuoc.textProperty().addListener((o, oldV, newV) -> {
            filter.setPredicate(t -> {
                if (newV == null || newV.isEmpty()) return true;
                String lower = newV.toLowerCase();
                return t.getTenThuoc().toLowerCase().contains(lower) || t.getMaThuoc().toLowerCase().contains(lower);
            });
            if (!newV.isEmpty()) cbChonThuoc.show();
        });

        cbChonThuoc.valueProperty().addListener((o, oldV, s) -> {
            if (s != null && cbKhoXuat.getValue() != null) {
                String k = cbKhoXuat.getValue().equals("Kho dự trữ") ? "KHO_DU_TRU" : "KHO_BAN_HANG";
                cbChonLo.setItems(FXCollections.observableArrayList(daoLo.getLoThuocTheoFEFO(s.getMaThuoc(), k)));
            }
        });

        cbChonLo.setConverter(new StringConverter<LoThuoc>() {
            @Override public String toString(LoThuoc l) { return l==null ? "" : "Lô: " + l.getMaLoThuoc() + " (Tồn: " + l.getSoLuongTon() + ")"; }
            @Override public LoThuoc fromString(String s) { return null; }
        });
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
                        else setGraphic(null);
                    } catch (Exception ex) { setGraphic(null); }
                }
            }
        });
        cbChonThuoc.setButtonCell(cbChonThuoc.getCellFactory().call(null));
    }

    private void setupTable() {
        colSTT.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(dsHuyTam.indexOf(c.getValue()) + 1));
        colTenThuoc.setCellValueFactory(c -> {
            String ma = c.getValue().getMaThuoc();
            for(Thuoc t : cbChonThuoc.getItems()) {
                if(t.getMaThuoc().equals(ma)) return new javafx.beans.property.SimpleStringProperty(t.getTenThuoc());
            }
            return new javafx.beans.property.SimpleStringProperty(ma);
        });
        colSoLo.setCellValueFactory(new PropertyValueFactory<>("soLo"));
        colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        
        colXoa.setCellFactory(c -> new TableCell<>() {
            private final Button b = new Button("🗑");
            { 
                b.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand;"); 
                b.setOnAction(e -> { 
                    dsHuyTam.remove(getTableView().getItems().get(getIndex())); 
                    tableThuocHuy.refresh(); 
                }); 
            }
            @Override protected void updateItem(Void i, boolean e) {
                super.updateItem(i, e); setGraphic(e ? null : b); setAlignment(Pos.CENTER);
            }
        });
        tableThuocHuy.setItems(dsHuyTam);
    }

    @FXML private void handleThemThuoc() {
        Thuoc t = cbChonThuoc.getValue(); LoThuoc l = cbChonLo.getValue();
        if (t == null || l == null || txtSoLuongHuy.getText().isEmpty()) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn thuốc, lô và nhập số lượng!");
            return;
        }
        
        for(ChiTietPhieuXuat ct : dsHuyTam) {
            if(ct.getSoLo().equals(l.getMaLoThuoc())) {
                AlertUtils.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Lô này đã có trong danh sách hủy!");
                return;
            }
        }
        
        try {
            int sl = Integer.parseInt(txtSoLuongHuy.getText());
            if (sl <= 0) return;
            if (sl > l.getSoLuongTon()) { 
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Vượt tồn! Kho chỉ còn: " + l.getSoLuongTon()); 
                return; 
            }
            
            double gia = l.getGiaNhap();
            dsHuyTam.add(new ChiTietPhieuXuat(null, t.getMaThuoc(), l.getMaLoThuoc(), sl, gia, sl * gia));
            
            txtSoLuongHuy.clear();
            cbChonLo.getSelectionModel().clearSelection();
            
        } catch (NumberFormatException e) {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Vui lòng nhập số lượng hợp lệ!");
        }
    }

    @FXML private void handleXacNhanHuy() {
        if (dsHuyTam.isEmpty()) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Chưa có thuốc nào trong danh sách!");
            return;
        }
        if (txtGhiChu.getText().isEmpty()) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Bắt buộc phải nhập Lý do xuất hủy!");
            return;
        }

        tongGiaTriThietHai = 0;
        for (ChiTietPhieuXuat ct : dsHuyTam) {
            tongGiaTriThietHai += ct.getThanhTien();
        }

        String maPhieuMoi = daoPX.getMaPhieuXuatMoi("XH"); // XH = Xuất Hủy
        
        PhieuXuat px = new PhieuXuat(
            maPhieuMoi, null, 
            UserSession.getInstance().getUser().getMaNhanVien(), 
            3, null, null, 
            tongGiaTriThietHai, 
            txtGhiChu.getText()
        );
        
        if (daoPX.xuatHuyThuoc(px, new ArrayList<>(dsHuyTam))) {
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã hủy thuốc thành công!\nMã phiếu: " + maPhieuMoi); 
            ((Stage) txtGhiChu.getScene().getWindow()).close();
        } else {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Thất bại", "Lỗi khi lưu phiếu xuất hủy!");
        }
    }
    
    @FXML private void handleHuyBo() { ((Stage) txtGhiChu.getScene().getWindow()).close(); }
}