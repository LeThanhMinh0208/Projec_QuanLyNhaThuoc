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
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.ResourceBundle;

public class Dialog_XuatTraNCCController implements Initializable {
    @FXML private ComboBox<NhaCungCap> cbNhaCungCap;
    @FXML private ComboBox<String> cbKhoXuat;
    @FXML private TextField txtNguoiLap, txtGhiChu, txtSoLuongTra;
    
    // Đã thêm ô tìm nhanh
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
    private DAO_LoThuoc daoLo = new DAO_LoThuoc();
    private DAO_PhieuXuat daoPX = new DAO_PhieuXuat();
    private DecimalFormat df = new DecimalFormat("#,##0");
    private double tongTienHoan = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        
        if (UserSession.getInstance().getUser() != null) {
            txtNguoiLap.setText(UserSession.getInstance().getUser().getHoTen());
        }

        // Setup Nhà Cung Cấp
        cbNhaCungCap.setItems(FXCollections.observableArrayList(new DAO_NhaCungCap().getAllNhaCungCap()));
        cbNhaCungCap.setConverter(new StringConverter<NhaCungCap>() {
            @Override public String toString(NhaCungCap n) { return n == null ? "" : n.getTenNhaCungCap(); }
            @Override public NhaCungCap fromString(String s) { return null; }
        });

        // Setup Kho Xuất
        cbKhoXuat.getItems().addAll("Kho dự trữ", "Kho bán hàng");
        cbKhoXuat.getSelectionModel().selectFirst();
        cbKhoXuat.valueProperty().addListener((o, oldV, newV) -> cbChonLo.getItems().clear());

        // Setup Chọn Thuốc (Có tìm kiếm + Hình ảnh y như Chuyển Kho)
        ObservableList<Thuoc> allThuoc = FXCollections.observableArrayList(new DAO_Thuoc().getAllThuoc());
        FilteredList<Thuoc> filter = new FilteredList<>(allThuoc, p -> true);
        cbChonThuoc.setItems(filter);
        setupComboThuoc(); // Hàm render ảnh

        // Logic tìm kiếm nhanh
        txtTimNhanhThuoc.textProperty().addListener((o, oldV, newV) -> {
            filter.setPredicate(t -> {
                if (newV == null || newV.isEmpty()) return true;
                String lowerCaseFilter = newV.toLowerCase();
                return t.getTenThuoc().toLowerCase().contains(lowerCaseFilter) || 
                       t.getMaThuoc().toLowerCase().contains(lowerCaseFilter);
            });
            if (!newV.isEmpty()) cbChonThuoc.show();
        });

        cbChonThuoc.valueProperty().addListener((o, oldV, s) -> {
            if (s != null && cbKhoXuat.getValue() != null) {
                String k = cbKhoXuat.getValue().equals("Kho dự trữ") ? "KHO_DU_TRU" : "KHO_BAN_HANG";
                cbChonLo.setItems(FXCollections.observableArrayList(daoLo.getLoThuocTraNCC(s.getMaThuoc(), k)));
            }
        });

        cbChonLo.setConverter(new StringConverter<LoThuoc>() {
            @Override public String toString(LoThuoc l) { 
                return l==null ? "" : "Lô: " + l.getMaLoThuoc() + " (Tồn: " + l.getSoLuongTon() + " | Giá: " + df.format(l.getGiaNhap()) + ")"; 
            }
            @Override public LoThuoc fromString(String s) { return null; }
        });
    }

    // Hàm gắn hình ảnh vào ComboBox Thuốc
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
            for(Thuoc t : cbChonThuoc.getItems()) {
                if(t.getMaThuoc().equals(ma)) return new javafx.beans.property.SimpleStringProperty(t.getTenThuoc());
            }
            return new javafx.beans.property.SimpleStringProperty(ma);
        });

        colSoLo.setCellValueFactory(new PropertyValueFactory<>("soLo"));
        colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        
        colThanhTien.setCellValueFactory(new PropertyValueFactory<>("thanhTien"));
        colThanhTien.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : df.format(item));
            }
        });

        colXoa.setCellFactory(c -> new TableCell<>() {
            private final Button b = new Button("🗑");
            { 
                b.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand;"); 
                b.setOnAction(e -> { 
                    dsTraTam.remove(getTableView().getItems().get(getIndex())); 
                    tinhTongTien();
                    tableThuocTra.refresh(); 
                }); 
            }
            @Override protected void updateItem(Void i, boolean e) {
                super.updateItem(i, e); setGraphic(e ? null : b); setAlignment(Pos.CENTER);
            }
        });
        tableThuocTra.setItems(dsTraTam);
    }

    @FXML private void handleThemThuoc() {
        Thuoc t = cbChonThuoc.getValue(); 
        LoThuoc l = cbChonLo.getValue();
        if (t == null || l == null || txtSoLuongTra.getText().isEmpty()) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn thuốc, lô và nhập số lượng!");
            return;
        }
        
        for(ChiTietPhieuXuat ct : dsTraTam) {
            if(ct.getSoLo().equals(l.getMaLoThuoc())) {
                AlertUtils.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Lô thuốc này đã có trong danh sách trả!");
                return;
            }
        }
        
        try {
            int sl = Integer.parseInt(txtSoLuongTra.getText());
            if (sl <= 0) return;
            if (sl > l.getSoLuongTon()) { 
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Vượt tồn! Kho chỉ còn: " + l.getSoLuongTon()); 
                return; 
            }
            
            double gia = l.getGiaNhap();
            dsTraTam.add(new ChiTietPhieuXuat(null, t.getMaThuoc(), l.getMaLoThuoc(), sl, gia, sl * gia));
            tinhTongTien();
            
            txtSoLuongTra.clear();
            cbChonLo.getSelectionModel().clearSelection();
            
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
    
    @FXML private void handleHuyBo() { ((Stage) txtGhiChu.getScene().getWindow()).close(); }
}