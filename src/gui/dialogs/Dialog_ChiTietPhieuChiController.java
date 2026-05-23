package gui.dialogs;

import java.io.File;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

import entity.PhieuChi;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import utils.AlertUtils;
import service.Print_PhieuChi;

public class Dialog_ChiTietPhieuChiController {

    @FXML private Label lblMaPhieu, lblNgayChi, lblNhanVien, lblNhaCungCap, lblTongTien, lblHinhThuc;
    @FXML private TextArea txtGhiChu;
    @FXML private Button btnDong;

    private PhieuChi phieuHienTai; 
    private DecimalFormat df = new DecimalFormat("#,### VNĐ");
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public void setPhieuChi(PhieuChi pc) {
        this.phieuHienTai = pc;

        lblMaPhieu.setText(pc.getMaPhieuChi());
        lblNgayChi.setText(pc.getNgayChi() != null ? sdf.format(pc.getNgayChi()) : "---");
        lblNhanVien.setText(pc.getNhanVien() != null ? pc.getNhanVien().getHoTen() : "---");
        lblNhaCungCap.setText(pc.getNhaCungCap() != null ? pc.getNhaCungCap().getTenNhaCungCap() : "---");
        lblTongTien.setText(df.format(pc.getTongTienChi()));

        // VIỆT HÓA HÌNH THỨC CHI
        String ht = pc.getHinhThucChi();
        if ("CHUYEN_KHOAN".equals(ht)) {
            lblHinhThuc.setText("Chuyển Khoản");
        } else if ("THE".equals(ht)) {
            lblHinhThuc.setText("Thẻ");
        } else {
            lblHinhThuc.setText("Tiền Mặt");
        }

        txtGhiChu.setText(pc.getGhiChu() != null ? pc.getGhiChu() : "Không có ghi chú");
    }

    @FXML
    void handleInPhieu() {
        if (phieuHienTai == null) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Thông báo", "Dữ liệu phiếu chi trống, không thể in!");
            return;
        }
        
        // =======================================================
        // GIAO DIỆN XEM TRƯỚC PHIẾU CHI (PREVIEW)
        // =======================================================
        Stage previewStage = new Stage();
        previewStage.setTitle("Xem trước Phiếu Chi - " + phieuHienTai.getMaPhieuChi());
        previewStage.initModality(Modality.APPLICATION_MODAL);

        VBox paper = new VBox(10);
        paper.setStyle("-fx-background-color: white; -fx-padding: 40; -fx-effect: dropshadow(three-pass-box, rgba(0,0,0,0.15), 10, 0, 0, 0);");
        paper.setMaxWidth(600); 

        Label lblHeader = new Label("NHÀ THUỐC LONG NGUYÊN");
        lblHeader.setStyle("-fx-font-size: 22px; -fx-font-weight: bold; -fx-text-fill: #1e293b;");
        Label lblAddress = new Label("Đ/c: 12 Nguyễn Văn Bảo, P.4, Gò Vấp, TP.HCM");
        lblAddress.setStyle("-fx-font-size: 14px; -fx-font-style: italic; -fx-text-fill: #64748b;");
        
        Label lblTitle = new Label("PHIẾU CHI TIỀN");
        lblTitle.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-padding: 15 0 10 0;");

        // Lưới thông tin phiếu chi
        GridPane infoGrid = new GridPane();
        infoGrid.setVgap(12); infoGrid.setHgap(30);
        infoGrid.add(new Label("Mã phiếu: " + phieuHienTai.getMaPhieuChi()), 0, 0);
        infoGrid.add(new Label("Ngày chi: " + (phieuHienTai.getNgayChi() != null ? sdf.format(phieuHienTai.getNgayChi()) : "---")), 0, 1);
        infoGrid.add(new Label("Người lập (NV): " + (phieuHienTai.getNhanVien() != null ? phieuHienTai.getNhanVien().getHoTen() : "---")), 1, 0);
        infoGrid.add(new Label("Người nhận (NCC): " + (phieuHienTai.getNhaCungCap() != null ? phieuHienTai.getNhaCungCap().getTenNhaCungCap() : "---")), 1, 1);
        infoGrid.add(new Label("Hình thức: " + lblHinhThuc.getText()), 0, 2);
        infoGrid.add(new Label("Ghi chú: " + (phieuHienTai.getGhiChu() != null ? phieuHienTai.getGhiChu() : "")), 1, 2);

        Label lblTotal = new Label("SỐ TIỀN CHI: " + df.format(phieuHienTai.getTongTienChi()));
        lblTotal.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #dc2626; -fx-padding: 20 0 0 0;");

        paper.getChildren().addAll(lblHeader, lblAddress, new Separator(), lblTitle, infoGrid, lblTotal);
        paper.setAlignment(Pos.TOP_CENTER);

        // Nút bấm
        Button btnCancel = new Button("Đóng");
        btnCancel.setStyle("-fx-background-color: #e2e8f0; -fx-text-fill: #475569; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20;");
        btnCancel.setOnAction(e -> previewStage.close());

        Button btnConfirm = new Button("✔ Xác nhận In");
        btnConfirm.setStyle("-fx-background-color: #0ea5e9; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand; -fx-padding: 8 20;");
        
        // LOGIC XUẤT FILE PDF
        btnConfirm.setOnAction(e -> {
            try {
                File exportDir = new File("exports" + File.separator + "phieuchi");
                if (!exportDir.exists()) {
                    exportDir.mkdirs();
                }

                File finalFile = new File(exportDir, "PhieuChi_" + phieuHienTai.getMaPhieuChi() + ".pdf");
                String finalPath = finalFile.getAbsolutePath();

                // Gọi service In (Đã tích hợp từ bản Incoming)
                Print_PhieuChi.inPhieu(phieuHienTai, finalPath);
                
                AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã lưu phiếu chi thành công tại:\n" + finalPath);
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

        Scene scene = new Scene(root, 700, 500); 
        previewStage.setScene(scene);
        previewStage.show();
    }

    @FXML
    void handleDong() {
        ((Stage) btnDong.getScene().getWindow()).close();
    }
}