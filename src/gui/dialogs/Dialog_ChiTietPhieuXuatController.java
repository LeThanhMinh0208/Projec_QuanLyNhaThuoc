package gui.dialogs;

import dao.DAO_NhaCungCap;
import dao.DAO_PhieuXuat;
import entity.ChiTietPhieuXuat;
import entity.NhaCungCap;
import entity.PhieuXuat;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
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

import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

public class Dialog_ChiTietPhieuXuatController implements Initializable {

    @FXML private Label lblMaPhieu, lblNgayLap, lblNguoiLap, lblLoaiPhieu;
    @FXML private Label lblNoiXuat, lblNoiNhan, lblGhiChu, lblNguoiVanChuyen; 
    
    @FXML private Label lblTextTongTien, lblTongTien; 

    @FXML private TableView<ChiTietPhieuXuat> tableChiTiet;
    @FXML private TableColumn<ChiTietPhieuXuat, Integer> colSTT, colSoLuong;
    @FXML private TableColumn<ChiTietPhieuXuat, String> colTenThuoc, colSoLo;
    @FXML private TableColumn<ChiTietPhieuXuat, Double> colDonGia, colThanhTien;

    private DAO_PhieuXuat daoPX = new DAO_PhieuXuat();
    private DecimalFormat df = new DecimalFormat("#,##0");
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");
    
    // Biến toàn cục để giữ thông tin in
    private PhieuXuat phieuHienTai;
    private double tongTienPhieu = 0;

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
    }

    private void setupTable() {
        colSTT.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(tableChiTiet.getItems().indexOf(c.getValue()) + 1));
        colTenThuoc.setCellValueFactory(new PropertyValueFactory<>("maThuoc")); 
        colSoLo.setCellValueFactory(new PropertyValueFactory<>("soLo"));
        colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuong"));

        colDonGia.setCellValueFactory(new PropertyValueFactory<>("donGia"));
        colDonGia.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : df.format(item));
            }
        });

        colThanhTien.setCellValueFactory(new PropertyValueFactory<>("thanhTien"));
        colThanhTien.setCellFactory(c -> new TableCell<>() {
            @Override protected void updateItem(Double item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null ? null : df.format(item));
            }
        });
    }

    public void setPhieuXuat(PhieuXuat px) {
        this.phieuHienTai = px; 
        this.tongTienPhieu = 0;
        
        lblMaPhieu.setText(px.getMaPhieuXuat());
        lblNguoiLap.setText(px.getMaNhanVien());
        lblNgayLap.setText(px.getNgayXuat() != null ? dtf.format(px.getNgayXuat()) : "");
        
        String ghiChuGoc = px.getGhiChu();
        if (ghiChuGoc != null && ghiChuGoc.contains("| VC:")) {
            String[] parts = ghiChuGoc.split("\\s*\\| VC:\\s*"); 
            lblGhiChu.setText(parts.length > 0 && !parts[0].isEmpty() ? parts[0] : "Không có");
            lblNguoiVanChuyen.setText(parts.length > 1 && !parts[1].isEmpty() ? parts[1] : "Không có");
        } else {
            lblGhiChu.setText(ghiChuGoc != null && !ghiChuGoc.isEmpty() ? ghiChuGoc : "Không có");
            lblNguoiVanChuyen.setText("Không có");
        }

        if (px.getLoaiPhieu() == 1) { 
            lblLoaiPhieu.setText("LỆNH CHUYỂN KHO NỘI BỘ");
            String kn = px.getKhoNhan();
            if ("KHO_BAN_HANG".equals(kn)) { lblNoiXuat.setText("Kho Dự Trữ"); lblNoiNhan.setText("Kho Bán Hàng"); } 
            else if ("KHO_DU_TRU".equals(kn)) { lblNoiXuat.setText("Kho Bán Hàng"); lblNoiNhan.setText("Kho Dự Trữ"); } 
            else { lblNoiXuat.setText("Kho Nội Bộ"); lblNoiNhan.setText(kn); }
            
            colDonGia.setVisible(false); colThanhTien.setVisible(false);
            lblTextTongTien.setVisible(false); lblTongTien.setVisible(false);

        } else if (px.getLoaiPhieu() == 2) { 
            lblLoaiPhieu.setText("PHIẾU TRẢ NHÀ CUNG CẤP");
            lblNoiXuat.setText("Kho Nội Bộ");
            NhaCungCap ncc = new DAO_NhaCungCap().getNhaCungCapByMa(px.getMaNhaCungCap());
            lblNoiNhan.setText(ncc != null ? ncc.getTenNhaCungCap() : px.getMaNhaCungCap());
            lblTextTongTien.setText("Tổng tiền NCC hoàn lại:");
            
            colDonGia.setVisible(true); colThanhTien.setVisible(true);
            lblTextTongTien.setVisible(true); lblTongTien.setVisible(true);

        } else if (px.getLoaiPhieu() == 3) { 
            lblLoaiPhieu.setText("LỆNH XUẤT HỦY THUỐC");
            lblNoiXuat.setText("Kho Nội Bộ");
            lblNoiNhan.setText("Khu vực Hủy rác y tế");
            
            colDonGia.setVisible(false); colThanhTien.setVisible(false);
            lblTextTongTien.setVisible(false); lblTongTien.setVisible(false);
        }

        List<ChiTietPhieuXuat> listCT = daoPX.getChiTietPhieuXuat(px.getMaPhieuXuat());
        ObservableList<ChiTietPhieuXuat> data = FXCollections.observableArrayList(listCT);
        tableChiTiet.setItems(data);

        if (px.getLoaiPhieu() == 2) {
            for (ChiTietPhieuXuat ct : listCT) {
                tongTienPhieu += ct.getThanhTien();
            }
            lblTongTien.setText(df.format(tongTienPhieu) + " VNĐ");
        }
    }

    // =========================================================================
    // HÀM XỬ LÝ NÚT IN PHIẾU (HIỂN THỊ PREVIEW UI RỒI MỚI XUẤT FILE)
    // =========================================================================
    @FXML 
    void handleInPhieu(ActionEvent event) {
        if (phieuHienTai == null || tableChiTiet.getItems().isEmpty()) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Trống", "Không có dữ liệu để in!");
            return;
        }

        // 1. TẠO CỬA SỔ PREVIEW
        Stage previewStage = new Stage();
        previewStage.setTitle("Xem trước Phiếu Xuất - " + phieuHienTai.getMaPhieuXuat());
        previewStage.initModality(Modality.APPLICATION_MODAL);

        VBox paper = new VBox(10);
        paper.setStyle("-fx-background-color: white; -fx-padding: 40; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 10, 0, 0, 0);");
        paper.setMaxWidth(750);

        // 2. XÁC ĐỊNH LOẠI PHIẾU
        String tieuDe = "";
        String labelDoiTac = "Nơi nhận:";
        String prefixFileName = "";
        
        if (phieuHienTai.getLoaiPhieu() == 1) {
            tieuDe = "PHIẾU CHUYỂN KHO NỘI BỘ";
            prefixFileName = "ChuyenKho_";
            labelDoiTac = "Kho nhận:";
        } else if (phieuHienTai.getLoaiPhieu() == 2) {
            tieuDe = "PHIẾU XUẤT TRẢ NHÀ CUNG CẤP";
            prefixFileName = "TraNCC_";
            labelDoiTac = "Nhà cung cấp:";
        } else if (phieuHienTai.getLoaiPhieu() == 3) {
            tieuDe = "PHIẾU XUẤT HỦY THUỐC";
            prefixFileName = "XuatHuy_";
            labelDoiTac = "Nơi hủy:";
        }

        Label lblHeader = new Label("NHÀ THUỐC LONG NGUYÊN"); lblHeader.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Label lblAddress = new Label("Đ/c: 12 Nguyễn Văn Bảo, P.4, Gò Vấp, TP.HCM"); lblAddress.setStyle("-fx-font-size: 14px; -fx-font-style: italic; -fx-text-fill: #64748b;");
        Label lblTitle = new Label(tieuDe); lblTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 15 0 10 0;");

        GridPane infoGrid = new GridPane(); infoGrid.setVgap(8); infoGrid.setHgap(30);
        infoGrid.add(new Label("Mã phiếu: " + phieuHienTai.getMaPhieuXuat()), 0, 0);
        infoGrid.add(new Label("Ngày lập: " + lblNgayLap.getText()), 0, 1);
        infoGrid.add(new Label(labelDoiTac + " " + lblNoiNhan.getText()), 1, 0);
        infoGrid.add(new Label("Người lập: " + lblNguoiLap.getText()), 1, 1);

        TableView<ChiTietPhieuXuat> tablePreview = new TableView<>();
        tablePreview.setSelectionModel(null); tablePreview.setFocusTraversable(false);
        tablePreview.setStyle("-fx-background-color: white; -fx-border-color: #e2e8f0;"); tablePreview.setPrefHeight(250);

        TableColumn<ChiTietPhieuXuat, String> cTen = new TableColumn<>("Tên thuốc"); cTen.setCellValueFactory(new PropertyValueFactory<>("maThuoc")); cTen.setPrefWidth(220);
        TableColumn<ChiTietPhieuXuat, String> cLo = new TableColumn<>("Số lô"); cLo.setCellValueFactory(new PropertyValueFactory<>("soLo")); cLo.setPrefWidth(100);
        TableColumn<ChiTietPhieuXuat, Integer> cSL = new TableColumn<>("SL"); cSL.setCellValueFactory(new PropertyValueFactory<>("soLuong")); cSL.setPrefWidth(60);

        tablePreview.getColumns().addAll(cTen, cLo, cSL);

        // NẾU LÀ PHIẾU TRẢ MỚI HIỆN CỘT TIỀN
        if (phieuHienTai.getLoaiPhieu() == 2) {
            TableColumn<ChiTietPhieuXuat, String> cGia = new TableColumn<>("Đơn giá"); cGia.setCellValueFactory(c -> new SimpleStringProperty(df.format(c.getValue().getDonGia()))); cGia.setPrefWidth(100);
            TableColumn<ChiTietPhieuXuat, String> cTT = new TableColumn<>("Thành tiền"); cTT.setCellValueFactory(c -> new SimpleStringProperty(df.format(c.getValue().getThanhTien()))); cTT.setPrefWidth(120);
            tablePreview.getColumns().addAll(cGia, cTT);
        }

        tablePreview.setItems(tableChiTiet.getItems());

        VBox paperContent = new VBox(lblHeader, lblAddress, new Separator(), lblTitle, infoGrid, tablePreview);
        
        if (phieuHienTai.getLoaiPhieu() == 2) {
            Label lblTotal = new Label("TỔNG GIÁ TRỊ: " + df.format(tongTienPhieu) + " VNĐ");
            lblTotal.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #dc2626; -fx-padding: 10 0 0 0;");
            paperContent.getChildren().add(lblTotal);
        }

        paperContent.setSpacing(10);
        paperContent.setAlignment(Pos.TOP_CENTER);
        paper.getChildren().add(paperContent);
        paper.setAlignment(Pos.TOP_CENTER);

        Button btnCancel = new Button("Đóng Preview");
        btnCancel.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20;");
        btnCancel.setOnAction(e -> previewStage.close());

        Button btnConfirm = new Button("✔ Xác nhận");
        btnConfirm.setStyle("-fx-background-color: #0ea5e9; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20;");

        // Khai báo final để truyền vào event của Button
        final String finalTieuDe = tieuDe;
        final String finalLabelDoiTac = labelDoiTac;
        final String finalPrefixFileName = prefixFileName;

        btnConfirm.setOnAction(e -> {
            // Gom data sang mảng String để chuyển cho Service in PDF
            List<String[]> dataPdf = new ArrayList<>();
            int stt = 1;
            for (ChiTietPhieuXuat ct : tableChiTiet.getItems()) {
                String donGia = phieuHienTai.getLoaiPhieu() == 2 ? df.format(ct.getDonGia()) : "---";
                String thanhTien = phieuHienTai.getLoaiPhieu() == 2 ? df.format(ct.getThanhTien()) : "---";
                dataPdf.add(new String[]{ String.valueOf(stt++), ct.getMaThuoc(), ct.getSoLo(), String.valueOf(ct.getSoLuong()), donGia, thanhTien });
            }

            try {
                // Tạo đường dẫn lưu file
                File exportDir = new File(System.getProperty("user.dir") + File.separator + "exports" + File.separator + "phieuxuat");
                if (!exportDir.exists()) exportDir.mkdirs();
                String finalPath = new File(exportDir, finalPrefixFileName + phieuHienTai.getMaPhieuXuat() + ".pdf").getAbsolutePath();

                // Chuyển sang Service để render PDF
                boolean isThanhCong = service.Print_PhieuXuat.inPhieu(
                    finalTieuDe, phieuHienTai.getMaPhieuXuat(), lblNgayLap.getText(), lblNguoiLap.getText(),
                    finalLabelDoiTac, lblNoiNhan.getText(), lblGhiChu.getText(), tongTienPhieu, dataPdf, finalPath
                );

                if (isThanhCong) {
                    AlertUtils.showAlert(Alert.AlertType.INFORMATION, "In thành công", "Đã xuất PDF tại:\n" + finalPath);
                    previewStage.close();
                } else {
                    AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Quá trình tạo file PDF thất bại!");
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        HBox actionBox = new HBox(15, btnCancel, btnConfirm); actionBox.setAlignment(Pos.CENTER_RIGHT); actionBox.setPadding(new Insets(15, 0, 0, 0));
        VBox root = new VBox(15, new ScrollPane(paper), actionBox); root.setStyle("-fx-background-color: #f8fafc; -fx-padding: 20;"); root.setAlignment(Pos.CENTER);
        previewStage.setScene(new Scene(root, 800, 700)); previewStage.show();
    }

    @FXML private void handleDong() {
        lblMaPhieu.getScene().getWindow().hide();
    }
}