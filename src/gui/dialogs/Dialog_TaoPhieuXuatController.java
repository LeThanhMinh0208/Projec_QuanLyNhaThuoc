package gui.dialogs;

import dao.*;
import entity.*;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Popup;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class Dialog_TaoPhieuXuatController {
    @FXML private Label lblTieuDe, lblLoaiPhieu, lblDacThu, lblKhoXuat;
    @FXML private TextField txtMaPhieu, txtGhiChu;
    @FXML private ComboBox<String> cmbKhoXuat, cmbKhoNhan;
    @FXML private ComboBox<NhaCungCap> cmbNhaCungCap;
    
    public static class ThuocKhoDTO {
        private Thuoc thuoc;
        private int tongTon;
        private String thongTinLo;

        public ThuocKhoDTO(Thuoc thuoc, int tongTon, String thongTinLo) {
            this.thuoc = thuoc;
            this.tongTon = tongTon;
            this.thongTinLo = thongTinLo;
        }
        public Thuoc getThuoc() { return thuoc; }
        public int getTongTon() { return tongTon; }
        public String getThongTinLo() { return thongTinLo; }
    }

    public static class ThuocYeuCauRow {
        private ThuocKhoDTO thuocDuocChon;
        private int slYeuCau = 0;
        private SimpleStringProperty trangThaiPhanBo = new SimpleStringProperty("Chưa nhập số lượng");
        private List<ChiTietPhieuXuat> chiTietThucTe = new ArrayList<>();

        public ThuocKhoDTO getThuocDuocChon() { return thuocDuocChon; }
        public void setThuocDuocChon(ThuocKhoDTO thuocDuocChon) { this.thuocDuocChon = thuocDuocChon; }
        public int getSlYeuCau() { return slYeuCau; }
        public void setSlYeuCau(int slYeuCau) { this.slYeuCau = slYeuCau; }
        public String getTrangThaiPhanBo() { return trangThaiPhanBo.get(); }
        public void setTrangThaiPhanBo(String t) { this.trangThaiPhanBo.set(t); }
        public SimpleStringProperty trangThaiPhanBoProperty() { return trangThaiPhanBo; }
        public List<ChiTietPhieuXuat> getChiTietThucTe() { return chiTietThucTe; }
    }

    @FXML private TableView<ThuocYeuCauRow> tableChiTiet;
    @FXML private TableColumn<ThuocYeuCauRow, String> colTenThuoc, colTrangThaiFEFO;
    @FXML private TableColumn<ThuocYeuCauRow, Integer> colSLYeuCau;
    @FXML private TableColumn<ThuocYeuCauRow, Void> colXoa;

    private String loaiPhieu;
    private DAO_PhieuXuat daoPX = new DAO_PhieuXuat();
    private DAO_LoThuoc daoLo = new DAO_LoThuoc();
    private DAO_Thuoc daoThuoc = new DAO_Thuoc();
    
    private ObservableList<ThuocYeuCauRow> listYeuCau = FXCollections.observableArrayList();
    private ObservableList<ThuocKhoDTO> listDanhSachThuocKho = FXCollections.observableArrayList();

    public void setLoaiPhieu(String loai) {
        this.loaiPhieu = loai;
        txtMaPhieu.setText(daoPX.getMaPhieuMoi());

        cmbKhoXuat.setItems(FXCollections.observableArrayList("Kho Dự Trữ", "Kho Bán Hàng"));
        cmbKhoXuat.getSelectionModel().selectFirst();
        
        taiDanhSachThuocHienCoTheoKho(getMaKho(cmbKhoXuat.getValue()));
        setupTable();
        
        cmbKhoXuat.setOnAction(e -> {
            String khoXuatChon = cmbKhoXuat.getValue();
            if ("Kho Dự Trữ".equals(khoXuatChon)) cmbKhoNhan.getSelectionModel().select("Kho Bán Hàng");
            else cmbKhoNhan.getSelectionModel().select("Kho Dự Trữ");
            
            taiDanhSachThuocHienCoTheoKho(getMaKho(khoXuatChon));
            listYeuCau.clear(); 
        });

        cmbKhoNhan.setVisible(false);
        cmbNhaCungCap.setVisible(false);

        if ("CHUYEN_KHO".equals(loai)) {
            lblTieuDe.setText("LẬP PHIẾU CHUYỂN KHO NỘI BỘ");
            lblLoaiPhieu.setText("CHUYỂN KHO");
            lblLoaiPhieu.getStyleClass().add("badge-chuyen");
            lblDacThu.setText("Đến Kho:");
            cmbKhoNhan.setVisible(true);
            cmbKhoNhan.setItems(FXCollections.observableArrayList("Kho Bán Hàng", "Kho Dự Trữ"));
            cmbKhoNhan.getSelectionModel().select("Kho Bán Hàng");
            cmbKhoNhan.setDisable(true); 
            cmbKhoNhan.setStyle("-fx-opacity: 1; -fx-font-weight: bold;"); 
        } else if ("TRA_NCC".equals(loai)) {
            lblTieuDe.setText("LẬP PHIẾU TRẢ NHÀ CUNG CẤP");
            lblLoaiPhieu.setText("TRẢ NCC");
            lblLoaiPhieu.getStyleClass().add("badge-tra");
            lblDacThu.setText("Nhà Cung Cấp:");
            cmbNhaCungCap.setVisible(true);
        } else {
            lblTieuDe.setText("LẬP PHIẾU XUẤT HỦY");
            lblLoaiPhieu.setText("XUẤT HỦY");
            lblLoaiPhieu.getStyleClass().add("badge-huy");
            lblDacThu.setVisible(false);
        }
    }

    // --- FIX HÌNH ẢNH: Hàm bọc thép để load ảnh từ mọi loại đường dẫn ---
    private Image loadImage(String path) {
        if (path == null || path.trim().isEmpty()) return null;
        try {
            File file = new File(path);
            if (file.exists()) return new Image(file.toURI().toString(), 45, 45, true, true);
            java.net.URL url = getClass().getResource(path);
            if (url != null) return new Image(url.toExternalForm(), 45, 45, true, true);
            if (path.startsWith("http")) return new Image(path, 45, 45, true, true);
        } catch (Exception e) {}
        return null;
    }

    private void taiDanhSachThuocHienCoTheoKho(String maKho) {
        listDanhSachThuocKho.clear();
        List<Thuoc> allThuoc = daoThuoc.getAllThuoc();
        for (Thuoc t : allThuoc) {
            List<LoThuoc> dsLo = daoLo.getLoThuocTheoFEFO(t.getMaThuoc(), maKho);
            if (dsLo != null && !dsLo.isEmpty()) { 
                int tongTon = 0;
                StringBuilder chiTiet = new StringBuilder();
                for (LoThuoc lo : dsLo) {
                    tongTon += lo.getSoLuongTon();
                    chiTiet.append("[Lô ").append(lo.getMaLoThuoc()).append(": ").append(lo.getSoLuongTon()).append("] ");
                }
                listDanhSachThuocKho.add(new ThuocKhoDTO(t, tongTon, chiTiet.toString().trim()));
            }
        }
    }

    private String getMaKho(String tenKhoHienThi) {
        if ("Kho Dự Trữ".equals(tenKhoHienThi)) return "KHO_DU_TRU";
        if ("Kho Bán Hàng".equals(tenKhoHienThi)) return "KHO_BAN_HANG";
        return tenKhoHienThi;
    }

    private void setupTable() {
        // --- FIX TÌM KIẾM: Biến Ô chọn thuốc thành Thanh Tìm Kiếm Đa Năng ---
        colTenThuoc.setCellFactory(param -> new TableCell<>() {
            private final TextField txtSearch = new TextField();
            private final Popup popup = new Popup();
            private final ListView<ThuocKhoDTO> listView = new ListView<>();
            private final FilteredList<ThuocKhoDTO> filteredData = new FilteredList<>(listDanhSachThuocKho, p -> true);

            {
                txtSearch.setPromptText("🔍 Tìm Tên, Mã thuốc, Lô...");
                txtSearch.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-border-radius: 4; -fx-padding: 6;");

                listView.setItems(filteredData);
                listView.setPrefSize(420, 250); // Cửa sổ danh sách to, dễ nhìn
                listView.setStyle("-fx-background-color: white; -fx-border-color: #cbd5e1; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.2), 10, 0, 0, 5);");

                // Render giao diện từng dòng trong danh sách xổ xuống
                listView.setCellFactory(lv -> new ListCell<>() {
                    @Override protected void updateItem(ThuocKhoDTO item, boolean empty) {
                        super.updateItem(item, empty);
                        if (empty || item == null) { setGraphic(null); setText(null); }
                        else {
                            HBox box = new HBox(12);
                            box.setAlignment(Pos.CENTER_LEFT);
                            box.setStyle("-fx-padding: 8;");
                            
                            ImageView imgView = new ImageView();
                            imgView.setFitWidth(45); imgView.setFitHeight(45);
                            Image img = loadImage(item.getThuoc().getHinhAnh()); // Gọi hàm fix ảnh
                            if (img != null) imgView.setImage(img);

                            VBox vbox = new VBox(4);
                            Label lblTen = new Label(item.getThuoc().getMaThuoc() + " - " + item.getThuoc().getTenThuoc());
                            lblTen.setStyle("-fx-font-weight: bold; -fx-font-size: 14px; -fx-text-fill: #1e293b;");
                            Label lblChiTiet = new Label("Tồn: " + item.getTongTon() + " | " + item.getThongTinLo());
                            lblChiTiet.setStyle("-fx-font-size: 12px; -fx-text-fill: #059669; -fx-font-weight: bold;");
                            
                            vbox.getChildren().addAll(lblTen, lblChiTiet);
                            box.getChildren().addAll(imgView, vbox);
                            setGraphic(box);
                        }
                    }
                });

                popup.getContent().add(listView);
                popup.setAutoHide(true); // Click ra ngoài tự ẩn

                // Sự kiện khi gõ vào ô tìm kiếm -> Lọc danh sách ngay lập tức
                txtSearch.textProperty().addListener((obs, oldV, newV) -> {
                    if (newV == null || newV.isEmpty()) {
                        filteredData.setPredicate(p -> true);
                    } else {
                        String keyword = newV.toLowerCase();
                        filteredData.setPredicate(dto -> 
                            dto.getThuoc().getTenThuoc().toLowerCase().contains(keyword) ||
                            dto.getThuoc().getMaThuoc().toLowerCase().contains(keyword) ||
                            dto.getThongTinLo().toLowerCase().contains(keyword)
                        );
                    }
                    // Bật Popup hiển thị danh sách
                    if (!popup.isShowing() && getScene() != null && txtSearch.isFocused()) {
                        javafx.geometry.Bounds bounds = txtSearch.localToScreen(txtSearch.getBoundsInLocal());
                        if (bounds != null) popup.show(txtSearch, bounds.getMinX(), bounds.getMaxY());
                    }
                });

                // Sự kiện click chuột vào ô -> Xổ danh sách
                txtSearch.setOnMouseClicked(e -> {
                    if (!popup.isShowing() && getScene() != null) {
                        txtSearch.selectAll();
                        javafx.geometry.Bounds bounds = txtSearch.localToScreen(txtSearch.getBoundsInLocal());
                        if (bounds != null) popup.show(txtSearch, bounds.getMinX(), bounds.getMaxY());
                    }
                });

                // Sự kiện click chọn 1 loại thuốc trong danh sách
                listView.setOnMouseClicked(e -> {
                    ThuocKhoDTO selected = listView.getSelectionModel().getSelectedItem();
                    if (selected != null) {
                        ThuocYeuCauRow row = getTableRow().getItem();
                        if (row != null) {
                            row.setThuocDuocChon(selected);
                            txtSearch.setText(selected.getThuoc().getMaThuoc() + " - " + selected.getThuoc().getTenThuoc());
                            updateFEFO(row);
                            getTableView().refresh();
                        }
                        popup.hide();
                    }
                });
            }

            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setGraphic(null);
                } else {
                    ThuocKhoDTO dto = getTableRow().getItem().getThuocDuocChon();
                    if (dto != null && !txtSearch.isFocused()) {
                        txtSearch.setText(dto.getThuoc().getMaThuoc() + " - " + dto.getThuoc().getTenThuoc());
                    } else if (dto == null && !txtSearch.isFocused()) {
                        txtSearch.setText("");
                    }
                    setGraphic(txtSearch);
                }
            }
        });

        colSLYeuCau.setCellFactory(param -> new TableCell<>() {
            private final TextField txt = new TextField();
            {
                txt.setStyle("-fx-alignment: center;");
                txt.textProperty().addListener((obs, oldV, newV) -> {
                    if (getTableRow() != null && getTableRow().getItem() != null) {
                        try {
                            int qty = newV.isEmpty() ? 0 : Integer.parseInt(newV);
                            ThuocYeuCauRow row = getTableRow().getItem();
                            row.setSlYeuCau(qty);
                            updateFEFO(row); 
                        } catch (Exception e) {}
                    }
                });
            }
            @Override protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) setGraphic(null);
                else {
                    String val = String.valueOf(getTableRow().getItem().getSlYeuCau());
                    if (!txt.getText().equals(val)) txt.setText(val);
                    setGraphic(txt);
                }
            }
        });

        colTrangThaiFEFO.setCellValueFactory(cellData -> cellData.getValue().trangThaiPhanBoProperty());
        colTrangThaiFEFO.setCellFactory(column -> {
            return new TableCell<ThuocYeuCauRow, String>() {
                private final javafx.scene.text.Text text = new javafx.scene.text.Text();
                { text.wrappingWidthProperty().bind(column.widthProperty().subtract(10)); }
                @Override protected void updateItem(String item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) setGraphic(null);
                    else {
                        text.setText(item);
                        if (item.contains("❌ LỖI") || item.contains("Chưa")) text.setStyle("-fx-fill: #ef4444; -fx-font-weight: bold;");
                        else text.setStyle("-fx-fill: #16a34a;");
                        setGraphic(text);
                    }
                }
            };
        });
        
        colXoa.setCellFactory(param -> new TableCell<>() {
            private final Button btn = new Button("Xóa");
            { btn.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand;"); btn.setOnAction(e -> listYeuCau.remove(getTableRow().getItem())); }
            @Override protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : btn);
            }
        });

        tableChiTiet.setItems(listYeuCau);
    }

    private void updateFEFO(ThuocYeuCauRow row) {
        if (row.getThuocDuocChon() == null || row.getSlYeuCau() <= 0) {
            row.setTrangThaiPhanBo("Chưa nhập đủ thông tin (Thuốc hoặc Số lượng)");
            row.getChiTietThucTe().clear();
            return;
        }

        Thuoc thuoc = row.getThuocDuocChon().getThuoc();
        List<LoThuoc> dsLo = daoLo.getLoThuocTheoFEFO(thuoc.getMaThuoc(), getMaKho(cmbKhoXuat.getValue()));
        int tongTon = dsLo.stream().mapToInt(LoThuoc::getSoLuongTon).sum();
        
        if (row.getSlYeuCau() > tongTon) {
            row.setTrangThaiPhanBo("❌ LỖI: Kho xuất chỉ còn " + tongTon + " đơn vị. Đề xuất nhập thêm!");
            row.getChiTietThucTe().clear();
        } else {
            int canLay = row.getSlYeuCau();
            String kq = "✅ Đã phân bổ: ";
            row.getChiTietThucTe().clear();
            
            for (LoThuoc lo : dsLo) {
                if (canLay <= 0) break;
                int lay = Math.min(lo.getSoLuongTon(), canLay);
                kq += "[Lô " + lo.getMaLoThuoc() + ": " + lay + "] ";
                
                ChiTietPhieuXuat ct = new ChiTietPhieuXuat();
                ct.setLoThuoc(lo);
                ct.setSoLuongXuat(lay);
                row.getChiTietThucTe().add(ct);
                
                canLay -= lay;
            }
            row.setTrangThaiPhanBo(kq);
        }
    }

    @FXML void handleThemDong() { listYeuCau.add(new ThuocYeuCauRow()); }

    @FXML void handleLuuPhieu(ActionEvent event) {
        if (listYeuCau.isEmpty()) {
            new Alert(Alert.AlertType.WARNING, "Chưa có thuốc nào để xuất!").show();
            return;
        }

        for (ThuocYeuCauRow row : listYeuCau) {
            if (row.getChiTietThucTe().isEmpty() || row.getTrangThaiPhanBo().contains("❌ LỖI")) {
                new Alert(Alert.AlertType.ERROR, "Tồn tại dòng thuốc không hợp lệ. Vui lòng kiểm tra lại!").show();
                return;
            }
        }

        PhieuXuat px = new PhieuXuat();
        px.setMaPhieuXuat(txtMaPhieu.getText());
        px.setNgayXuat(new java.sql.Date(System.currentTimeMillis()));
        px.setLoaiXuat(loaiPhieu);
        px.setGhiChu(txtGhiChu.getText());
        
        if ("CHUYEN_KHO".equals(loaiPhieu)) px.setKhoNhan(getMaKho(cmbKhoNhan.getValue()));

        List<ChiTietPhieuXuat> listCT = new ArrayList<>();
        for (ThuocYeuCauRow row : listYeuCau) listCT.addAll(row.getChiTietThucTe());

        if (daoPX.luuPhieuXuatVaCapNhatKho(px, listCT)) {
            new Alert(Alert.AlertType.INFORMATION, "Lưu Phiếu Thành Công! Tồn kho đã được cập nhật.").showAndWait();
            handleDong(event);
        } else {
            new Alert(Alert.AlertType.ERROR, "Lưu thất bại!").show();
        }
    }

    @FXML void handleDong(ActionEvent event) { ((Stage) ((Node) event.getSource()).getScene().getWindow()).close(); }
}