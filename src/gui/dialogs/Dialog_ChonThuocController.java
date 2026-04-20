package gui.dialogs;

import dao.DAO_BangGia;
import dao.DAO_DonViQuyDoi;
import dao.DAO_LoThuoc;
import dao.DAO_Thuoc;
import entity.DonViQuyDoi;
import entity.LoThuoc;
import entity.Thuoc;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.math.BigDecimal;
import java.util.List;

public class Dialog_ChonThuocController {

    @FXML private TextField txtTim;
    @FXML private ListView<Thuoc> listThuoc;
    @FXML private Label lblBadgeKeDon;
    @FXML private Label lblTonInfo;
    @FXML private Label lblGiaInfo;
    @FXML private Button btnThemVaoGio;

    private final DAO_Thuoc daoThuoc = new DAO_Thuoc();
    private final DAO_LoThuoc daoLoThuoc = new DAO_LoThuoc();
    private final DAO_DonViQuyDoi daoDonViQuyDoi = new DAO_DonViQuyDoi();
    private final DAO_BangGia daoBangGia = new DAO_BangGia();
    private final ObservableList<Thuoc> dsThuoc = FXCollections.observableArrayList();
    private Thuoc thuocChon;

    private String loaiBan = "BAN_LE";

    public void setLoaiBan(String loaiBan) {
        this.loaiBan = loaiBan;
        if ("BAN_THEO_DON".equals(loaiBan)) {
            dsThuoc.setAll(daoThuoc.getAllThuocCoLoKhoBanHang());
        } else {
            dsThuoc.setAll(daoThuoc.getAllThuocKhongKeDonKhoBanHang());
        }
    }

    // Thông tin đơn vị mặc định và giá
    private DonViQuyDoi donViMacDinh;
    private double donGiaMacDinh;
    private String maBangGiaMacDinh;
    private int tongTonMacDinh;

    @FXML
    public void initialize() {
        // Mặc định load bán lẻ, dùng khi dialog hiện lên chưa setLoaiBan
        dsThuoc.setAll(daoThuoc.getAllThuocKhongKeDonKhoBanHang());

        // Custom cell factory cho ListView
        listThuoc.setCellFactory(lv -> new ThuocListCell());
        listThuoc.setFixedCellSize(80);

        // Filtered list cho tìm kiếm
        FilteredList<Thuoc> filtered = new FilteredList<>(dsThuoc, p -> true);
        txtTim.textProperty().addListener((obs, ov, nv) -> {
            filtered.setPredicate(t -> {
                if (nv == null || nv.trim().isEmpty()) return true;
                String f = nv.toLowerCase();
                if (t.getMaThuoc() != null && t.getMaThuoc().toLowerCase().contains(f)) return true;
                if (t.getTenThuoc() != null && t.getTenThuoc().toLowerCase().contains(f)) return true;
                if (t.getHoatChat() != null && t.getHoatChat().toLowerCase().contains(f)) return true;
                if (t.getTrieuChung() != null && t.getTrieuChung().toLowerCase().contains(f)) return true;
                if (t.getCongDung() != null && t.getCongDung().toLowerCase().contains(f)) return true;
                return false;
            });
        });
        listThuoc.setItems(filtered);

        // Khi chọn thuốc trong list → cập nhật tồn kho & giá
        listThuoc.getSelectionModel().selectedItemProperty().addListener((obs, oldSel, newSel) -> {
            if (newSel != null) {
                capNhatThongTinThuocChon(newSel);
            } else {
                lblTonInfo.setText("Tồn: —");
                lblGiaInfo.setText("Giá: —");
            }
        });

        // Double-click → chọn luôn
        listThuoc.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && e.isPrimaryButtonDown()) {
                Thuoc t = listThuoc.getSelectionModel().getSelectedItem();
                if (t != null) {
                    thuocChon = t;
                    close();
                }
            }
        });
    }

    private void capNhatThongTinThuocChon(Thuoc thuoc) {
        // Lấy đơn vị quy đổi mặc định (đầu tiên)
        List<DonViQuyDoi> donVis = daoDonViQuyDoi.getDonViByMaThuoc(thuoc.getMaThuoc());
        if (!donVis.isEmpty()) {
            donViMacDinh = donVis.get(0);
            // Tồn kho
            int tongTonCoBan = daoLoThuoc.getLoThuocBanDuocByMaThuoc(thuoc.getMaThuoc())
                    .stream().mapToInt(LoThuoc::getSoLuongTon).sum();
            tongTonMacDinh = (donViMacDinh.getTyLeQuyDoi() <= 0) ? 0
                    : tongTonCoBan / donViMacDinh.getTyLeQuyDoi();
            lblTonInfo.setText("Tồn: " + tongTonMacDinh + " " + donViMacDinh.getTenDonVi());

            // Giá
            Object[] giaInfo = daoBangGia.getGiaVaMaBangGia(donViMacDinh.getMaQuyDoi());
            if (giaInfo != null) {
                donGiaMacDinh = ((BigDecimal) giaInfo[0]).doubleValue();
                maBangGiaMacDinh = (String) giaInfo[1];
                lblGiaInfo.setText(String.format("Giá: %,.0f ₫", donGiaMacDinh));
                lblGiaInfo.setStyle("-fx-text-fill: #16a34a; -fx-font-weight: bold; -fx-font-size: 13px;");
            } else {
                donGiaMacDinh = 0;
                maBangGiaMacDinh = null;
                lblGiaInfo.setText("Giá: Chưa có bảng giá");
                lblGiaInfo.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-font-size: 13px;");
            }
        } else {
            lblTonInfo.setText("Tồn: 0");
            lblGiaInfo.setText("Giá: —");
        }
    }

    @FXML
    private void handleChon() {
        thuocChon = listThuoc.getSelectionModel().getSelectedItem();
        if (thuocChon == null) {
            new Alert(Alert.AlertType.WARNING, "Vui lòng chọn một thuốc trong danh sách!").showAndWait();
            return;
        }
        close();
    }

    @FXML
    private void handleHuy() {
        thuocChon = null;
        close();
    }

    private void close() {
        Stage stage = (Stage) listThuoc.getScene().getWindow();
        stage.close();
    }

    public Thuoc getThuocChon() {
        return thuocChon;
    }

    // ═══════════════════════════════════════════════════════════════
    // Custom ListCell cho thuốc — hiện ảnh + badge kê đơn
    // ═══════════════════════════════════════════════════════════════
    private class ThuocListCell extends ListCell<Thuoc> {
        private final ImageView imgView = new ImageView();
        private final Label lblTen = new Label();
        private final Label lblHoatChat = new Label();
        private final Label lblDonVi = new Label();
        private final Label lblBadge = new Label("Kê đơn");
        private final HBox root = new HBox(12);
        private final VBox info = new VBox(3);

        ThuocListCell() {
            imgView.setFitWidth(60);
            imgView.setFitHeight(60);
            imgView.setPreserveRatio(true);
            imgView.setSmooth(true);
            // Rounded clip for image
            javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle(60, 60);
            clip.setArcWidth(10);
            clip.setArcHeight(10);
            imgView.setClip(clip);

            lblTen.setStyle("-fx-font-weight: bold; -fx-font-size: 13px; -fx-text-fill: #1e293b;");
            lblHoatChat.setStyle("-fx-font-size: 11px; -fx-text-fill: #64748b;");
            lblDonVi.setStyle("-fx-font-size: 11px; -fx-text-fill: #94a3b8;");
            
            lblBadge.setStyle("-fx-background-color: #1565C0; -fx-text-fill: white; " +
                             "-fx-background-radius: 4; -fx-padding: 2 8; -fx-font-size: 10px; -fx-font-weight: bold;");
            lblBadge.setVisible(false);

            HBox tenRow = new HBox(8, lblTen, lblBadge);
            tenRow.setAlignment(Pos.CENTER_LEFT);

            info.getChildren().addAll(tenRow, lblHoatChat, lblDonVi);
            info.setAlignment(Pos.CENTER_LEFT);

            root.getChildren().addAll(imgView, info);
            root.setAlignment(Pos.CENTER_LEFT);
            root.setPadding(new Insets(6, 10, 6, 10));
        }

        @Override
        protected void updateItem(Thuoc item, boolean empty) {
            super.updateItem(item, empty);
            if (empty || item == null) {
                setGraphic(null);
                setText(null);
                setStyle("");
            } else {
                lblTen.setText(item.getTenThuoc());
                String hoatChatStr = (item.getHoatChat() != null ? item.getHoatChat() : "")
                    + (item.getHamLuong() != null && !item.getHamLuong().isEmpty()
                       ? " - " + item.getHamLuong() : "");
                lblHoatChat.setText(hoatChatStr);
                lblDonVi.setText("ĐV: " + (item.getDonViCoBan() != null ? item.getDonViCoBan() : "—"));

                // Badge kê đơn
                lblBadge.setVisible(item.isCanKeDon());
                lblBadge.setManaged(item.isCanKeDon());

                // Highlight dòng thuốc kê đơn
                if (item.isCanKeDon()) {
                    root.setStyle("-fx-background-color: #E3F2FD; -fx-background-radius: 8;");
                } else {
                    root.setStyle("-fx-background-color: transparent;");
                }

                // Load ảnh thuốc
                try {
                    if (item.getHinhAnh() != null && !item.getHinhAnh().trim().isEmpty()) {
                        java.io.InputStream is = getClass().getResourceAsStream(
                            "/resources/images/images_thuoc/" + item.getHinhAnh().trim());
                        if (is != null) {
                            imgView.setImage(new Image(is, 60, 60, true, true));
                        } else {
                            imgView.setImage(null);
                        }
                    } else {
                        imgView.setImage(null);
                    }
                } catch (Exception e) {
                    imgView.setImage(null);
                }

                setGraphic(root);
            }
        }
    }
}
