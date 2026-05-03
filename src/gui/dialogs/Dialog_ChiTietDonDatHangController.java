package gui.dialogs;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;

import dao.DAO_DonDatHang;
import entity.ChiTietDonDatHang;
import entity.DonDatHang;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Separator;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.AlertUtils;

public class Dialog_ChiTietDonDatHangController {

    @FXML private Label lblMaDon, lblNgayLap, lblNhaCungCap, lblNhanVien, lblGhiChu, lblTongTienDuKien;
    @FXML private TableView<ChiTietDonDatHang> tableChiTiet;

    @FXML private TableColumn<ChiTietDonDatHang, String> colTenThuoc, colDonVi, colMaLo, colHanDung, colGiaNhap, colThanhTien, colTinhTrang, colTienDo;
    @FXML private TableColumn<ChiTietDonDatHang, Integer> colSoLuongDat, colSoLuongNhan;

    @FXML private Button btnHuyDon;

    private DAO_DonDatHang dao = new DAO_DonDatHang();
    private DecimalFormat df = new DecimalFormat("#,### VNĐ");
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    private DonDatHang donHienTai;
    private List<ChiTietDonDatHang> listChiTiet;
    private double tongTienDonHang = 0; // Thêm biến lưu tổng tiền cho phần In

    public void setDonDatHang(DonDatHang don) {
        this.donHienTai = don;

        lblMaDon.setText("Mã đơn: " + don.getMaDonDatHang());
        lblNgayLap.setText(sdf.format(don.getNgayDat()));
        lblNhaCungCap.setText(don.getNhaCungCap().getTenNhaCungCap());
        lblNhanVien.setText(don.getNhanVien().getHoTen());
        lblGhiChu.setText(don.getGhiChu() != null && !don.getGhiChu().isEmpty() ? don.getGhiChu() : "---");

        // KHÓA NÚT HỦY ĐƠN
        String trangThai = don.getTrangThaiHang();
        String trangThaiGocDB = don.getTrangThai();

        if (trangThai != null &&
           (trangThai.contains("Hoàn Thành") ||
            trangThai.contains("Hủy") ||
            "GIAO_MOT_PHAN".equals(trangThaiGocDB) ||
            "DONG_DON_THIEU".equals(trangThaiGocDB) ||
            "GIAO_DU".equals(trangThaiGocDB))) {

            btnHuyDon.setDisable(true);
            btnHuyDon.setStyle("-fx-background-color: #fca5a5; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");

        } else {
            btnHuyDon.setDisable(false);
            btnHuyDon.setStyle("-fx-background-color: linear-gradient(to bottom, #ef4444, #dc2626); -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8; -fx-cursor: hand;");
        }

        setupTable();

        listChiTiet = dao.getChiTietByMaDon(don.getMaDonDatHang());
        tableChiTiet.setItems(FXCollections.observableArrayList(listChiTiet));

        tinhTongTienDuKien();
    }

    private void tinhTongTienDuKien() {
        tongTienDonHang = 0;
        if (listChiTiet != null) {
            for (ChiTietDonDatHang ct : listChiTiet) {
                tongTienDonHang += ct.getThanhTien();
            }
        }
        lblTongTienDuKien.setText(df.format(tongTienDonHang));
    }

    private void setupTable() {
        colTenThuoc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getThuoc().getTenThuoc()));
        colDonVi.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDonViQuyDoi().getTenDonVi()));

        colSoLuongDat.setCellValueFactory(new PropertyValueFactory<>("soLuongDat"));
        colSoLuongDat.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");

        colSoLuongNhan.setCellValueFactory(new PropertyValueFactory<>("soLuongDaNhan"));
        colSoLuongNhan.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: #10b981;");

        colMaLo.setCellValueFactory(c -> {
            String lo = c.getValue().getMaLo();
            return new SimpleStringProperty((lo != null && !lo.isEmpty()) ? lo : "---");
        });
        colMaLo.setStyle("-fx-alignment: CENTER;");

        colHanDung.setCellValueFactory(c -> {
            String hd = c.getValue().getHanSuDung();
            return new SimpleStringProperty((hd != null && !hd.isEmpty()) ? hd : "---");
        });
        colHanDung.setStyle("-fx-alignment: CENTER;");

        colGiaNhap.setCellValueFactory(c -> new SimpleStringProperty(df.format(c.getValue().getDonGiaDuKien())));
        colGiaNhap.setStyle("-fx-alignment: CENTER-RIGHT;");

        colThanhTien.setCellValueFactory(c -> new SimpleStringProperty(df.format(c.getValue().getThanhTien())));
        colThanhTien.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: #ef4444;");

        colTinhTrang.setCellValueFactory(c -> {
            int dat = c.getValue().getSoLuongDat();
            int nhan = c.getValue().getSoLuongDaNhan();
            String maTrangThaiDB = donHienTai.getTrangThai(); 
            
            String status = (nhan == 0) ? "Chờ Nhập" : (nhan < dat) ? "Thiếu Hàng" : "Nhập Đủ";
            
            if ("DA_HUY".equals(maTrangThaiDB)) { status = "Đã Hủy"; } 
            else if ("GIAO_MOT_PHAN".equals(maTrangThaiDB) || "DONG_DON_THIEU".equals(maTrangThaiDB)) {
                 if (nhan > 0 && nhan < dat) { status = "Chốt Thiếu"; }
            }
            return new SimpleStringProperty(status);
        });
        colTinhTrang.setStyle("-fx-alignment: CENTER; -fx-text-fill: #0284c7; -fx-font-weight: bold;");

        // ==========================================
        // FIX: LOGIC CỘT TIẾN ĐỘ ĐÃ GỠ CONFLICT
        // ==========================================
        colTienDo.setCellValueFactory(c -> {
            int dat = c.getValue().getSoLuongDat();
            int nhan = c.getValue().getSoLuongDaNhan();
            String maTrangThaiDB = donHienTai.getTrangThai();

            String tienDoStr = "Đang Xử Lý";
            
            if ("DA_HUY".equals(maTrangThaiDB)) { 
                tienDoStr = "Đã Hủy"; 
            } 
            else if ("GIAO_DU".equals(maTrangThaiDB) || "GIAO_MOT_PHAN".equals(maTrangThaiDB) || "DONG_DON_THIEU".equals(maTrangThaiDB)) {
                tienDoStr = "Hoàn Thành";
            } 
            else if (nhan >= dat && dat > 0) { 
                tienDoStr = "Hoàn Thành"; 
            }
            
            return new SimpleStringProperty(tienDoStr);
        });
        
        // TÔ MÀU CHO CỘT TIẾN ĐỘ TỰ ĐỘNG
        colTienDo.setCellFactory(column -> new TableCell<ChiTietDonDatHang, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) { setText(null); setStyle(""); } 
                else {
                    setText(item);
                    if (item.equals("Hoàn Thành")) setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: #10b981;"); 
                    else if (item.equals("Đã Hủy")) setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: #ef4444;"); 
                    else setStyle("-fx-alignment: CENTER; -fx-font-weight: bold; -fx-text-fill: #f59e0b;"); 
                }
            }
        });
    }

    @FXML void handleHuyDon(ActionEvent event) {
        if (donHienTai.getTrangThaiHang().contains("Hoàn Thành") || "GIAO_MOT_PHAN".equals(donHienTai.getTrangThai())) {
            AlertUtils.showAlert(AlertType.WARNING, "Cảnh báo", "Đơn hàng này đã chốt/hoàn thành, không thể hủy!");
            return;
        }

        Alert alert = new Alert(AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận hủy đơn");
        alert.setHeaderText("Bạn có chắc chắn muốn HỦY đơn đặt hàng " + donHienTai.getMaDonDatHang() + " không?");
        alert.setContentText("Lưu ý: NCC sẽ không giao hàng cho đơn này nữa.\nThao tác này không thể hoàn tác!");

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == ButtonType.OK) {
            boolean isSuccess = dao.updateTrangThaiDonHang(donHienTai.getMaDonDatHang(), "DA_HUY");

            if (isSuccess) {
                donHienTai.setTrangThai("DA_HUY");
                tableChiTiet.refresh(); 
                btnHuyDon.setDisable(true); 
                btnHuyDon.setStyle("-fx-background-color: #fca5a5; -fx-text-fill: white; -fx-font-weight: bold; -fx-background-radius: 8;");
                AlertUtils.showAlert(AlertType.INFORMATION, "Thành công", "Đã hủy đơn hàng thành công!");
            } else {
                AlertUtils.showAlert(AlertType.ERROR, "Lỗi", "Có lỗi xảy ra khi cập nhật CSDL. Vui lòng thử lại!");
            }
        }
    }

    @FXML void handleInDonHang(ActionEvent event) {
        if (donHienTai == null || listChiTiet == null || listChiTiet.isEmpty()) {
            AlertUtils.showAlert(AlertType.WARNING, "Cảnh báo", "Đơn hàng này rỗng, không có dữ liệu để in!");
            return;
        }

        Stage previewStage = new Stage();
        previewStage.setTitle("Xem trước Phiếu Đặt Hàng - " + donHienTai.getMaDonDatHang());
        previewStage.initModality(Modality.APPLICATION_MODAL); 

        VBox paper = new VBox(10);
        paper.setStyle("-fx-background-color: white; -fx-padding: 40; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 10, 0, 0, 0);");
        paper.setMaxWidth(650);

        Label lblHeader = new Label("NHÀ THUỐC LONG NGUYÊN");
        lblHeader.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Label lblAddress = new Label("Đ/c: 12 Nguyễn Văn Bảo, P.4, Gò Vấp, TP.HCM");
        lblAddress.setStyle("-fx-font-size: 14px; -fx-font-style: italic; -fx-text-fill: #64748b;");
        
        Label lblTitle = new Label("PHIẾU ĐẶT HÀNG");
        lblTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 15 0 10 0;");

        GridPane infoGrid = new GridPane();
        infoGrid.setVgap(8); infoGrid.setHgap(30);
        infoGrid.add(new Label("Mã phiếu: " + donHienTai.getMaDonDatHang()), 0, 0);
        infoGrid.add(new Label("Ngày lập: " + sdf.format(donHienTai.getNgayDat())), 0, 1);
        infoGrid.add(new Label("Nhà cung cấp: " + donHienTai.getNhaCungCap().getTenNhaCungCap()), 1, 0);
        infoGrid.add(new Label("Người lập: " + donHienTai.getNhanVien().getHoTen()), 1, 1);

        TableView<ChiTietDonDatHang> tablePreview = new TableView<>();
        tablePreview.setSelectionModel(null); 
        tablePreview.setFocusTraversable(false);
        tablePreview.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0;");
        tablePreview.setPrefHeight(250);

        TableColumn<ChiTietDonDatHang, String> cTen = new TableColumn<>("Tên thuốc");
        cTen.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getThuoc().getTenThuoc()));
        cTen.setPrefWidth(220);

        TableColumn<ChiTietDonDatHang, String> cDVT = new TableColumn<>("ĐVT");
        cDVT.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDonViQuyDoi().getTenDonVi()));
        cDVT.setPrefWidth(60);

        TableColumn<ChiTietDonDatHang, String> cGia = new TableColumn<>("Đơn giá");
        cGia.setCellValueFactory(c -> new SimpleStringProperty(df.format(c.getValue().getDonGiaDuKien())));
        cGia.setPrefWidth(100);

        TableColumn<ChiTietDonDatHang, Integer> cSL = new TableColumn<>("SL");
        cSL.setCellValueFactory(new PropertyValueFactory<>("soLuongDat"));
        cSL.setPrefWidth(50);

        TableColumn<ChiTietDonDatHang, String> cTT = new TableColumn<>("Thành tiền");
        cTT.setCellValueFactory(c -> new SimpleStringProperty(df.format(c.getValue().getThanhTien())));
        cTT.setPrefWidth(120);

        tablePreview.getColumns().addAll(cTen, cDVT, cGia, cSL, cTT);
        tablePreview.setItems(FXCollections.observableArrayList(listChiTiet));

        Label lblTotal = new Label("TỔNG CỘNG: " + df.format(tongTienDonHang));
        lblTotal.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #dc2626; -fx-padding: 10 0 0 0;");

        paper.getChildren().addAll(lblHeader, lblAddress, new Separator(), lblTitle, infoGrid, tablePreview, lblTotal);
        paper.setAlignment(Pos.TOP_CENTER);

        Button btnCancel = new Button("Đóng");
        btnCancel.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20;");
        btnCancel.setOnAction(e -> previewStage.close());

        Button btnConfirm = new Button("✔ Xác nhận In");
        btnConfirm.setStyle("-fx-background-color: #0ea5e9; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20;");
        
        // =========================================================================
        // LƯU BẰNG ĐƯỜNG DẪN TƯƠNG ĐỐI VÀO THƯ MỤC EXPORTS/PHIEUDATHANG
        // =========================================================================
        btnConfirm.setOnAction(e -> {
            try {
                // Định nghĩa đường dẫn lưu file (nằm trong thư mục project)
                File exportDir = new File(System.getProperty("user.dir") + File.separator + "exports" + File.separator + "phieudathang");
                
                // Tự động tạo thư mục nếu chưa tồn tại
                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }

                // Đường dẫn file PDF hoàn chỉnh
                File finalFile = new File(exportDir, "PhieuDatHang_" + donHienTai.getMaDonDatHang() + ".pdf");
                String finalPath = finalFile.getAbsolutePath();

                if (service.Print_PhieuDatHang.inHoaDon(donHienTai, listChiTiet, finalPath)) {
                    AlertUtils.showAlert(AlertType.INFORMATION, "In thành công", "Đã xuất PDF thành công tại:\n" + finalPath);
                    previewStage.close(); // Tắt popup xem trước
                } else {
                    AlertUtils.showAlert(AlertType.ERROR, "Lỗi", "Không thể in phiếu. Vui lòng thử lại!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                AlertUtils.showAlert(AlertType.ERROR, "Lỗi Hệ Thống", "Đã xảy ra lỗi: " + ex.getMessage());
            }
        });

        HBox actionBox = new HBox(15, btnCancel, btnConfirm);
        actionBox.setAlignment(Pos.CENTER_RIGHT);
        actionBox.setPadding(new Insets(15, 0, 0, 0));

        VBox root = new VBox(15, new ScrollPane(paper), actionBox);
        root.setStyle("-fx-background-color: #f8fafc; -fx-padding: 20;");
        root.setAlignment(Pos.CENTER);

        Scene scene = new Scene(root, 750, 650);
        previewStage.setScene(scene);
        previewStage.show();
    }

    @FXML void handleDong(ActionEvent event) {
        Stage stage = (Stage) lblMaDon.getScene().getWindow();
        stage.close();
    }
}