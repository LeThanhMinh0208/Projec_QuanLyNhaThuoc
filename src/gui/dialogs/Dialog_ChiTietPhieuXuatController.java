package gui.dialogs;

import java.net.URL;
import java.text.DecimalFormat;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.ResourceBundle;

import dao.DAO_NhaCungCap;
import dao.DAO_PhieuXuat;
import entity.ChiTietPhieuXuat;
import entity.NhaCungCap;
import entity.PhieuXuat;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Label;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;

public class Dialog_ChiTietPhieuXuatController implements Initializable {

    @FXML private Label lblMaPhieu, lblNgayLap, lblNguoiLap, lblLoaiPhieu;
    @FXML private Label lblNoiXuat, lblNoiNhan, lblGhiChu, lblNguoiVanChuyen; // Thêm lblNguoiVanChuyen

    @FXML private Label lblTextTongTien, lblTongTien;

    @FXML private TableView<ChiTietPhieuXuat> tableChiTiet;
    @FXML private TableColumn<ChiTietPhieuXuat, Integer> colSTT, colSoLuong;
    @FXML private TableColumn<ChiTietPhieuXuat, String> colTenThuoc, colSoLo;
    @FXML private TableColumn<ChiTietPhieuXuat, Double> colDonGia, colThanhTien;

    private DAO_PhieuXuat daoPX = new DAO_PhieuXuat();
    private DecimalFormat df = new DecimalFormat("#,##0");
    private DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

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
        lblMaPhieu.setText(px.getMaPhieuXuat());
        lblNguoiLap.setText(px.getMaNhanVien());
        lblNgayLap.setText(px.getNgayXuat() != null ? dtf.format(px.getNgayXuat()) : "");

        // =======================================================
        // TÁCH CHUỖI ĐỂ TÌM NGƯỜI VẬN CHUYỂN TRONG CỘT GHI CHÚ
        // =======================================================
        String ghiChuGoc = px.getGhiChu();
        if (ghiChuGoc != null && ghiChuGoc.contains("| VC:")) {
            // FIX: Dùng Regex cắt chuẩn dù có khoảng trắng hay không
            String[] parts = ghiChuGoc.split("\\s*\\| VC:\\s*");
            lblGhiChu.setText(parts.length > 0 && !parts[0].isEmpty() ? parts[0] : "Không có");
            lblNguoiVanChuyen.setText(parts.length > 1 && !parts[1].isEmpty() ? parts[1] : "Không có");
        } else {
            lblGhiChu.setText(ghiChuGoc != null && !ghiChuGoc.isEmpty() ? ghiChuGoc : "Không có");
            lblNguoiVanChuyen.setText("Không có");
        }

        // =======================================================
        // HIỂN THỊ NƠI XUẤT, NƠI NHẬN & ẨN HIỆN CỘT
        // =======================================================
        if (px.getLoaiPhieu() == 1) {
            lblLoaiPhieu.setText("LỆNH CHUYỂN KHO NỘI BỘ");
            String kn = px.getKhoNhan();
            if ("KHO_BAN_HANG".equals(kn)) {
                lblNoiXuat.setText("Kho Dự Trữ"); lblNoiNhan.setText("Kho Bán Hàng");
            } else if ("KHO_DU_TRU".equals(kn)) {
                lblNoiXuat.setText("Kho Bán Hàng"); lblNoiNhan.setText("Kho Dự Trữ");
            } else {
                lblNoiXuat.setText("Kho Nội Bộ"); lblNoiNhan.setText(kn);
            }

            colDonGia.setVisible(false);
            colThanhTien.setVisible(false);
            lblTextTongTien.setVisible(false);
            lblTongTien.setVisible(false);

        } else if (px.getLoaiPhieu() == 2) {
            lblLoaiPhieu.setText("PHIẾU TRẢ NHÀ CUNG CẤP");
            lblNoiXuat.setText("Kho Nội Bộ");
            NhaCungCap ncc = new DAO_NhaCungCap().getNhaCungCapByMa(px.getMaNhaCungCap());
            lblNoiNhan.setText(ncc != null ? ncc.getTenNhaCungCap() : px.getMaNhaCungCap());
            lblTextTongTien.setText("Tổng tiền NCC hoàn lại:");

            colDonGia.setVisible(true);
            colThanhTien.setVisible(true);
            lblTextTongTien.setVisible(true);
            lblTongTien.setVisible(true);

        } else if (px.getLoaiPhieu() == 3) {
            lblLoaiPhieu.setText("LỆNH XUẤT HỦY THUỐC");
            lblNoiXuat.setText("Kho Nội Bộ");
            lblNoiNhan.setText("Khu vực Hủy rác y tế");

            colDonGia.setVisible(false);
            colThanhTien.setVisible(false);
            lblTextTongTien.setVisible(false);
            lblTongTien.setVisible(false);
        }

        List<ChiTietPhieuXuat> listCT = daoPX.getChiTietPhieuXuat(px.getMaPhieuXuat());
        ObservableList<ChiTietPhieuXuat> data = FXCollections.observableArrayList(listCT);
        tableChiTiet.setItems(data);

        if (px.getLoaiPhieu() == 2) {
            double tong = 0;
            for (ChiTietPhieuXuat ct : listCT) {
                tong += ct.getThanhTien();
            }
            lblTongTien.setText(df.format(tong) + " VNĐ");
        }
    }

    @FXML private void handleDong() {
        lblMaPhieu.getScene().getWindow().hide();
    }
}