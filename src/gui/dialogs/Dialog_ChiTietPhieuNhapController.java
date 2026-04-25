package gui.dialogs;

import connectDB.ConnectDB;
import entity.PhieuNhap;
import service.Print_PhieuNhap; 
import utils.AlertUtils; 
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

public class Dialog_ChiTietPhieuNhapController {

    @FXML private Label lblMaPhieu, lblNhaCungCap, lblNgayNhap, lblNguoiLap, lblTongTien;

    @FXML private TableView<ChiTietUI> tableChiTiet;
    @FXML private TableColumn<ChiTietUI, String> colTenThuoc, colDonVi, colMaLo;
    @FXML private TableColumn<ChiTietUI, Integer> colSoLuong;
    @FXML private TableColumn<ChiTietUI, String> colNgaySX, colHanDung, colGiaNhap, colThanhTien;

    private DecimalFormat df = new DecimalFormat("#,### VNĐ");
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
    
    private PhieuNhap phieuHienTai;

    @FXML public void initialize() {
        colTenThuoc.setCellValueFactory(new PropertyValueFactory<>("tenThuoc"));
        colDonVi.setCellValueFactory(new PropertyValueFactory<>("donVi"));
        colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        colSoLuong.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: #059669;");
        
        colMaLo.setCellValueFactory(new PropertyValueFactory<>("maLo"));
        colMaLo.setStyle("-fx-alignment: CENTER;");
        
        colNgaySX.setCellValueFactory(new PropertyValueFactory<>("ngaySXStr"));
        colHanDung.setCellValueFactory(new PropertyValueFactory<>("hanDungStr"));
        
        colGiaNhap.setCellValueFactory(new PropertyValueFactory<>("giaNhapStr"));
        colGiaNhap.setStyle("-fx-alignment: CENTER-RIGHT;");
        
        colThanhTien.setCellValueFactory(new PropertyValueFactory<>("thanhTienStr"));
        colThanhTien.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: #e11d48;");
    }

    public void setPhieuNhap(PhieuNhap pn) {
        this.phieuHienTai = pn; 
        
        lblMaPhieu.setText("Mã phiếu: " + pn.getMaPhieuNhap());
        lblNhaCungCap.setText(pn.getNhaCungCap().getTenNhaCungCap());
        lblNguoiLap.setText(pn.getNhanVien().getHoTen());
        lblNgayNhap.setText(sdf.format(pn.getNgayNhap()));
        lblTongTien.setText(df.format(pn.getTongTien()));

        loadChiTietTuDatabase(pn.getMaPhieuNhap());
    }

    private void loadChiTietTuDatabase(String maPhieu) {
        ObservableList<ChiTietUI> list = FXCollections.observableArrayList();
        
        String sql = "SELECT t.tenThuoc, dv.tenDonVi, ctpn.soLuong, ctpn.donGiaNhap, " +
                     "lt.maLoThuoc, lt.ngaySanXuat, lt.hanSuDung " +
                     "FROM ChiTietPhieuNhap ctpn " +
                     "JOIN LoThuoc lt ON ctpn.maLoThuoc = lt.maLoThuoc " +
                     "JOIN Thuoc t ON lt.maThuoc = t.maThuoc " +
                     "JOIN DonViQuyDoi dv ON ctpn.maQuyDoi = dv.maQuyDoi " +
                     "WHERE ctpn.maPhieuNhap = ?";

        try (Connection con = ConnectDB.getInstance().getConnection();
             PreparedStatement pst = con.prepareStatement(sql)) {
            pst.setString(1, maPhieu);
            ResultSet rs = pst.executeQuery();
            while (rs.next()) {
                ChiTietUI ct = new ChiTietUI();
                ct.setTenThuoc(rs.getString("tenThuoc"));
                ct.setDonVi(rs.getString("tenDonVi")); 
                ct.setSoLuong(rs.getInt("soLuong"));
                ct.setGiaNhap(rs.getDouble("donGiaNhap"));
                ct.setMaLo(rs.getString("maLoThuoc"));
                ct.setNgaySX(rs.getDate("ngaySanXuat"));
                ct.setHanDung(rs.getDate("hanSuDung"));
                list.add(ct);
            }
            tableChiTiet.setItems(list);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML 
    void handleInPhieu(ActionEvent event) {
        if (phieuHienTai == null || tableChiTiet.getItems() == null || tableChiTiet.getItems().isEmpty()) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Thông báo", "Phiếu này chưa có chi tiết hàng hóa, không thể in!");
            return;
        }
        
        Stage previewStage = new Stage();
        previewStage.setTitle("Xem trước Phiếu Nhập Kho - " + phieuHienTai.getMaPhieuNhap());
        previewStage.initModality(Modality.APPLICATION_MODAL);

        VBox paper = new VBox(10);
        paper.setStyle("-fx-background-color: white; -fx-padding: 40; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 10, 0, 0, 0);");
        paper.setMaxWidth(750);

        Label lblHeader = new Label("NHÀ THUỐC LONG NGUYÊN");
        lblHeader.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Label lblAddress = new Label("Đ/c: 12 Nguyễn Văn Bảo, P.4, Gò Vấp, TP.HCM");
        lblAddress.setStyle("-fx-font-size: 14px; -fx-font-style: italic; -fx-text-fill: #64748b;");
        
        Label lblTitle = new Label("PHIẾU NHẬP KHO");
        lblTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 15 0 10 0;");

        GridPane infoGrid = new GridPane();
        infoGrid.setVgap(8); infoGrid.setHgap(30);
        infoGrid.add(new Label("Mã phiếu: " + phieuHienTai.getMaPhieuNhap()), 0, 0);
        infoGrid.add(new Label("Ngày nhập: " + sdf.format(phieuHienTai.getNgayNhap())), 0, 1);
        infoGrid.add(new Label("Nhà cung cấp: " + phieuHienTai.getNhaCungCap().getTenNhaCungCap()), 1, 0);
        infoGrid.add(new Label("Người lập: " + phieuHienTai.getNhanVien().getHoTen()), 1, 1);

        TableView<ChiTietUI> tablePreview = new TableView<>();
        tablePreview.setSelectionModel(null);
        tablePreview.setFocusTraversable(false);
        tablePreview.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0;");
        tablePreview.setPrefHeight(300);

        TableColumn<ChiTietUI, String> cTen = new TableColumn<>("Tên thuốc");
        cTen.setCellValueFactory(new PropertyValueFactory<>("tenThuoc"));
        cTen.setPrefWidth(200);

        TableColumn<ChiTietUI, String> cDVT = new TableColumn<>("ĐVT");
        cDVT.setCellValueFactory(new PropertyValueFactory<>("donVi"));
        cDVT.setPrefWidth(50);
        
        TableColumn<ChiTietUI, String> cLo = new TableColumn<>("Số lô");
        cLo.setCellValueFactory(new PropertyValueFactory<>("maLo"));
        cLo.setPrefWidth(80);

        TableColumn<ChiTietUI, String> cGia = new TableColumn<>("Giá nhập");
        cGia.setCellValueFactory(new PropertyValueFactory<>("giaNhapStr"));
        cGia.setPrefWidth(90);

        TableColumn<ChiTietUI, Integer> cSL = new TableColumn<>("SL");
        cSL.setCellValueFactory(new PropertyValueFactory<>("soLuong"));
        cSL.setPrefWidth(50);

        TableColumn<ChiTietUI, String> cTT = new TableColumn<>("Thành tiền");
        cTT.setCellValueFactory(new PropertyValueFactory<>("thanhTienStr"));
        cTT.setPrefWidth(120);

        tablePreview.getColumns().addAll(cTen, cDVT, cLo, cGia, cSL, cTT);
        tablePreview.setItems(tableChiTiet.getItems());

        Label lblTotal = new Label("TỔNG TIỀN NHẬP: " + df.format(phieuHienTai.getTongTien()));
        lblTotal.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #dc2626; -fx-padding: 10 0 0 0;");

        paper.getChildren().addAll(lblHeader, lblAddress, new Separator(), lblTitle, infoGrid, tablePreview, lblTotal);
        paper.setAlignment(Pos.TOP_CENTER);

        Button btnCancel = new Button("Đóng");
        btnCancel.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20;");
        btnCancel.setOnAction(e -> previewStage.close());

        Button btnConfirm = new Button("✔ Xác nhận In");
        btnConfirm.setStyle("-fx-background-color: #0ea5e9; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20;");
        
        // ===============================================
        // ĐÃ SỬA: SỬ DỤNG ĐƯỜNG DẪN TƯƠNG ĐỐI (RELATIVE PATH)
        // ===============================================
        btnConfirm.setOnAction(e -> {
            try {
                // Định nghĩa đường dẫn tương đối (tự động lấy gốc là thư mục project của máy người chạy)
                File exportDir = new File("exports" + File.separator + "phieunhap");
                
                // Nếu máy thành viên khác chưa có thư mục này -> Tự động tạo
                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }

                // Khai báo file đích
                File finalFile = new File(exportDir, "PhieuNhapKho_" + phieuHienTai.getMaPhieuNhap() + ".pdf");
                String finalPath = finalFile.getAbsolutePath();

                // Gọi service In
                Print_PhieuNhap.inPhieu(phieuHienTai, tableChiTiet.getItems(), finalPath);
                
                AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã lưu phiếu thành công tại:\n" + finalPath);
                previewStage.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi Hệ Thống", "Không thể in phiếu. Chi tiết: " + ex.getMessage());
            }
        });

        HBox actionBox = new HBox(15, btnCancel, btnConfirm);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.setPadding(new Insets(15, 0, 0, 0));

        VBox root = new VBox(15, new ScrollPane(paper), actionBox);
        root.setStyle("-fx-background-color: #f8fafc; -fx-padding: 20;");
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 800, 700);
        previewStage.setScene(scene);
        previewStage.show();
    }

    @FXML void handleDong(ActionEvent event) {
        Stage stage = (Stage) lblMaPhieu.getScene().getWindow();
        stage.close();
    }

    public class ChiTietUI {
        private String tenThuoc, donVi, maLo;
        private int soLuong;
        private double giaNhap;
        private Date ngaySX, hanDung;

        public String getTenThuoc() { return tenThuoc; }
        public void setTenThuoc(String tenThuoc) { this.tenThuoc = tenThuoc; }
        public String getDonVi() { return donVi; }
        public void setDonVi(String donVi) { this.donVi = donVi; }
        public String getMaLo() { return maLo; }
        public void setMaLo(String maLo) { this.maLo = maLo; }
        public int getSoLuong() { return soLuong; }
        public void setSoLuong(int soLuong) { this.soLuong = soLuong; }
        public double getGiaNhap() { return giaNhap; }
        public void setGiaNhap(double giaNhap) { this.giaNhap = giaNhap; }
        public Date getNgaySX() { return ngaySX; }
        public void setNgaySX(Date ngaySX) { this.ngaySX = ngaySX; }
        public Date getHanDung() { return hanDung; }
        public void setHanDung(Date hanDung) { this.hanDung = hanDung; }

        public String getGiaNhapStr() { return df.format(giaNhap); }
        public String getThanhTienStr() { return df.format(giaNhap * soLuong); }
        public String getNgaySXStr() { return ngaySX != null ? sdf.format(ngaySX) : "---"; }
        public String getHanDungStr() { return hanDung != null ? sdf.format(hanDung) : "---"; }
    }
}