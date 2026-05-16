package gui.dialogs;

import connectDB.ConnectDB;
import entity.PhieuKiemKe;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.AlertUtils;
import service.Print_PhieuKiemKe;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;

public class Dialog_ChiTietPhieuKiemKeController {
    
    @FXML private Label lblMaPhieu;
    @FXML private TableView<ChiTiet> tableChiTiet;
    @FXML private TableColumn<ChiTiet, String> colMaLo, colTenThuoc, colLyDo, colGhiChu;
    @FXML private TableColumn<ChiTiet, Integer> colKiemTra, colChenhLech;

    private PhieuKiemKe phieu;
    private ObservableList<ChiTiet> ds = FXCollections.observableArrayList();
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM/yyyy");

    @FXML public void initialize() {
        colMaLo.setCellValueFactory(new PropertyValueFactory<>("maLo"));
        colTenThuoc.setCellValueFactory(new PropertyValueFactory<>("ten"));
        colKiemTra.setCellValueFactory(new PropertyValueFactory<>("kiemTra"));
        colChenhLech.setCellValueFactory(new PropertyValueFactory<>("lech"));
        
        colChenhLech.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Integer item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); return; }
                setText(item > 0 ? "+" + item : String.valueOf(item));
                if (item < 0) setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold; -fx-alignment: CENTER;"); 
                else if (item > 0) setStyle("-fx-text-fill: #ea580c; -fx-font-weight: bold; -fx-alignment: CENTER;"); 
                else setStyle("-fx-text-fill: #059669; -fx-font-weight: bold; -fx-alignment: CENTER;"); 
            }
        });
        
        colLyDo.setCellValueFactory(new PropertyValueFactory<>("lyDo"));
        colGhiChu.setCellValueFactory(new PropertyValueFactory<>("ghiChu"));
    }

    public void setPhieuKiemKe(PhieuKiemKe pk) {
        this.phieu = pk; 
        lblMaPhieu.setText("Mã phiếu: " + pk.getMaPhieuKiemKe());
        
        // =========================================================================
        // 🚨 FIX LOGIC: TRUY VẤN BÙ ĐỂ LẤY TÊN NGƯỜI DUYỆT VÀ NGÀY DUYỆT
        // =========================================================================
        String sqlNguoiDuyet = "SELECT nv.hoTen, p.ngayDuyet FROM PhieuKiemKe p LEFT JOIN NhanVien nv ON p.maNhanVienDuyet = nv.maNhanVien WHERE p.maPhieuKiemKe = ?";
        try (Connection con = ConnectDB.getInstance().getConnection(); PreparedStatement pstNguoiDuyet = con.prepareStatement(sqlNguoiDuyet)) {
            pstNguoiDuyet.setString(1, pk.getMaPhieuKiemKe());
            ResultSet rsNguoiDuyet = pstNguoiDuyet.executeQuery();
            if (rsNguoiDuyet.next()) {
                String tenNguoiDuyet = rsNguoiDuyet.getString("hoTen");
                java.sql.Timestamp ngayDuyet = rsNguoiDuyet.getTimestamp("ngayDuyet");
                
                if (tenNguoiDuyet != null) {
                    entity.NhanVien nvDuyet = new entity.NhanVien();
                    nvDuyet.setHoTen(tenNguoiDuyet);
                    this.phieu.setNhanVienDuyet(nvDuyet);
                    this.phieu.setNgayDuyet(ngayDuyet);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // TẢI CHI TIẾT THUỐC
        String sql = "SELECT c.maLoThuoc, t.tenThuoc, c.soLuongKiemTra, c.chenhLech, c.lyDoLech, c.ghiChu " +
                     "FROM ChiTietPhieuKiemKe c JOIN LoThuoc l ON c.maLoThuoc=l.maLoThuoc " +
                     "JOIN Thuoc t ON l.maThuoc=t.maThuoc WHERE c.maPhieuKiemKe=?";
                     
        try (Connection con = ConnectDB.getInstance().getConnection(); PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, pk.getMaPhieuKiemKe()); 
            ResultSet rs = pst.executeQuery();
            
            while (rs.next()) {
                String malo = rs.getString(1);
                String ten = rs.getString(2);
                int kiemtra = rs.getInt(3);
                int lech = rs.getInt(4);
                String lydo = rs.getString(5);
                String ghichu = rs.getString(6) != null ? rs.getString(6) : ""; 
                
                ds.add(new ChiTiet(malo, ten, kiemtra, lech, lydo, ghichu));
            }
            tableChiTiet.setItems(ds);
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML void handleInPhieu(ActionEvent event) {
        if (phieu == null || ds.isEmpty()) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Thông báo", "Phiếu này không có dữ liệu để in!");
            return;
        }

        Stage previewStage = new Stage();
        previewStage.setTitle("Xem trước Phiếu Kiểm Kê - " + phieu.getMaPhieuKiemKe());
        previewStage.initModality(Modality.APPLICATION_MODAL);

        VBox paper = new VBox(10);
        paper.setStyle("-fx-background-color: white; -fx-padding: 40; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 10, 0, 0, 0);");
        paper.setMaxWidth(800);

        Label lblHeader = new Label("NHÀ THUỐC LONG NGUYÊN");
        lblHeader.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Label lblAddress = new Label("Đ/c: 12 Nguyễn Văn Bảo, P.4, Gò Vấp, TP.HCM");
        lblAddress.setStyle("-fx-font-size: 14px; -fx-font-style: italic; -fx-text-fill: #64748b;");

        Label lblTitle = new Label("PHIẾU KIỂM KÊ KHO");
        lblTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 15 0 10 0;");

        GridPane infoGrid = new GridPane();
        infoGrid.setVgap(8); infoGrid.setHgap(30);
        infoGrid.add(new Label("Mã phiếu: " + phieu.getMaPhieuKiemKe()), 0, 0);
        
        String ngayTaoStr = phieu.getNgayTao() != null ? sdf.format(phieu.getNgayTao()) : "---";
        String nguoiTaoStr = phieu.getNhanVienTao() != null ? phieu.getNhanVienTao().getHoTen() : "---";
        
        // 🚨 CHỖ NÀY ĐÃ LẤY ĐƯỢC TÊN VÀ NGÀY DUYỆT TỪ DATABASE LÊN 🚨
        String nguoiDuyetStr = phieu.getNhanVienDuyet() != null ? phieu.getNhanVienDuyet().getHoTen() : "Chưa duyệt";
        String ngayDuyetStr = phieu.getNgayDuyet() != null ? sdf.format(phieu.getNgayDuyet()) : "---";
        
        infoGrid.add(new Label("Ngày tạo: " + ngayTaoStr), 0, 1);
        infoGrid.add(new Label("Người lập: " + nguoiTaoStr), 1, 0);
        
        infoGrid.add(new Label("Ngày duyệt: " + ngayDuyetStr), 0, 2);
        infoGrid.add(new Label("Người duyệt: " + nguoiDuyetStr), 1, 1);

        TableView<ChiTiet> tablePreview = new TableView<>();
        tablePreview.setSelectionModel(null); 
        tablePreview.setFocusTraversable(false);
        tablePreview.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0;");
        tablePreview.setPrefHeight(350);

        TableColumn<ChiTiet, String> cMa = new TableColumn<>("Mã Lô");
        cMa.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getMaLo()));
        cMa.setPrefWidth(70);

        TableColumn<ChiTiet, String> cTen = new TableColumn<>("Tên Thuốc");
        cTen.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getTen()));
        cTen.setPrefWidth(180);

        TableColumn<ChiTiet, Integer> cTT = new TableColumn<>("Thực Tế");
        cTT.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getKiemTra()).asObject());
        cTT.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");
        cTT.setPrefWidth(70);

        TableColumn<ChiTiet, Integer> cLech = new TableColumn<>("Lệch");
        cLech.setCellValueFactory(c -> new SimpleIntegerProperty(c.getValue().getLech()).asObject());
        cLech.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");
        cLech.setPrefWidth(60);

        TableColumn<ChiTiet, String> cLyDo = new TableColumn<>("Lý do");
        cLyDo.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getLyDo() != null ? c.getValue().getLyDo() : ""));
        cLyDo.setPrefWidth(150);

        TableColumn<ChiTiet, String> cGhiChu = new TableColumn<>("Ghi chú");
        cGhiChu.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getGhiChu() != null ? c.getValue().getGhiChu() : ""));
        cGhiChu.setPrefWidth(150);

        tablePreview.getColumns().addAll(cMa, cTen, cTT, cLech, cLyDo, cGhiChu);
        tablePreview.setItems(ds); 

        paper.getChildren().addAll(lblHeader, lblAddress, new Separator(), lblTitle, infoGrid, tablePreview);
        paper.setAlignment(Pos.TOP_CENTER);

        Button btnCancel = new Button("Đóng");
        btnCancel.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20;");
        btnCancel.setOnAction(e -> previewStage.close());

        Button btnConfirm = new Button("✔ Xác nhận In");
        btnConfirm.setStyle("-fx-background-color: #0ea5e9; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20;");
        
        btnConfirm.setOnAction(e -> {
            try {
                java.io.File exportDir = new java.io.File("exports" + java.io.File.separator + "phieukiemke");
                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }
                
                String fileName = "PhieuKiemKe_" + phieu.getMaPhieuKiemKe() + ".pdf";
                java.io.File finalFile = new java.io.File(exportDir, fileName);
                String path = finalFile.getAbsolutePath(); 
                
                boolean success = Print_PhieuKiemKe.inPhieu(phieu, ds, path);
                
                if (success) {
                    AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã in và lưu phiếu tại:\n" + path);
                    previewStage.close();
                    
                    // 🚨 BONUS: TỰ ĐỘNG BẬT FILE PDF LÊN MÀN HÌNH NGAY LẬP TỨC 🚨
                    if (java.awt.Desktop.isDesktopSupported()) {
                        java.awt.Desktop.getDesktop().open(finalFile);
                    }
                } else {
                    AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể in phiếu. Vui lòng thử lại!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi hệ thống", "Đang có file PDF trùng tên đang được mở, vui lòng tắt file cũ trước khi in lại!");
            }
        });

        HBox actionBox = new HBox(15, btnCancel, btnConfirm);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.setPadding(new javafx.geometry.Insets(15, 0, 0, 0));

        VBox root = new VBox(15, new ScrollPane(paper), actionBox);
        root.setStyle("-fx-background-color: #f8fafc; -fx-padding: 20;");
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 850, 750);
        previewStage.setScene(scene);
        previewStage.show();
    }

    @FXML void handleDong(ActionEvent event) { 
        ((Stage) lblMaPhieu.getScene().getWindow()).close(); 
    }

    public class ChiTiet { 
        private String maLo;
        private String ten;
        private int kiemTra;
        private int lech;
        private String lyDo;
        private String ghiChu; 
        
        public ChiTiet(String maLo, String ten, int kiemTra, int lech, String lyDo, String ghiChu) {
            this.maLo = maLo;
            this.ten = ten;
            this.kiemTra = kiemTra;
            this.lech = lech;
            this.lyDo = lyDo;
            this.ghiChu = ghiChu;
        }

        public String getMaLo() { return maLo; }
        public String getTen() { return ten; }
        public int getKiemTra() { return kiemTra; }
        public int getLech() { return lech; }
        public String getLyDo() { return lyDo; }
        public String getGhiChu() { return ghiChu; } 
    }
}