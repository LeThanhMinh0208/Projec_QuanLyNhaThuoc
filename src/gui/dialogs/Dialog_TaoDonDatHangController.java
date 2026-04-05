package gui.dialogs;

import dao.DAO_DonDatHang;
import dao.DAO_NhaCungCap;
import dao.DAO_Thuoc;
import dao.DAO_DonViQuyDoi;
import entity.ChiTietDonDatHang;
import entity.DonDatHang;
import entity.DonViQuyDoi;
import entity.NhaCungCap;
import entity.NhanVien;
import entity.Thuoc;

import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import utils.AlertUtils;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.io.File;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Dialog_TaoDonDatHangController {

    // --- Khai báo UI ---
    @FXML private ComboBox<NhaCungCap> cbNhaCungCap;
    @FXML private DatePicker dpNgayGiao;
    @FXML private TextField txtGhiChu;
    
    // UI Nhập thuốc
    @FXML private TextField txtTimThuoc; // Thanh tìm kiếm mới
    @FXML private ComboBox<Thuoc> cbThuoc;
    @FXML private ComboBox<DonViQuyDoi> cbDonVi;
    @FXML private TextField txtSoLuong, txtGiaDuKien;
    
    @FXML private TableView<ChiTietDonDatHang> tableChiTiet;
    @FXML private TableColumn<ChiTietDonDatHang, String> colTenThuoc, colDonVi;
    @FXML private TableColumn<ChiTietDonDatHang, Integer> colSoLuong;
    @FXML private TableColumn<ChiTietDonDatHang, String> colGiaDuKien, colThanhTien;
    @FXML private TableColumn<ChiTietDonDatHang, Void> colThaoTac;
    @FXML private Label lblTongTien;

    // --- Khai báo biến toàn cục & DAO ---
    private ObservableList<ChiTietDonDatHang> listChiTiet = FXCollections.observableArrayList();
    private ObservableList<Thuoc> masterListThuoc = FXCollections.observableArrayList(); // List gốc chứa toàn bộ thuốc
    
    private DAO_DonDatHang daoDon = new DAO_DonDatHang();
    private DAO_NhaCungCap daoNcc = new DAO_NhaCungCap();
    private DAO_Thuoc daoThuoc = new DAO_Thuoc();
    private DAO_DonViQuyDoi daoDonVi = new DAO_DonViQuyDoi();
    private DecimalFormat df = new DecimalFormat("#,### VNĐ");

    @FXML public void initialize() {
        setupTable();
        loadDataToComboBox();
        setupSearchThuoc(); // Gọi hàm cài đặt tìm kiếm
        
        // Mặc định ngày giao dự kiến là ngày mai
        dpNgayGiao.setValue(LocalDate.now().plusDays(1));
    }

    private void setupTable() {
        colTenThuoc.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getThuoc().getTenThuoc()));
        colDonVi.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getDonViQuyDoi().getTenDonVi()));
        
        colSoLuong.setCellValueFactory(new PropertyValueFactory<>("soLuongDat"));
        colSoLuong.setStyle("-fx-alignment: CENTER; -fx-font-weight: bold;");
        
        colGiaDuKien.setCellValueFactory(c -> new SimpleStringProperty(df.format(c.getValue().getDonGiaDuKien())));
        colGiaDuKien.setStyle("-fx-alignment: CENTER-RIGHT;");
        
        colThanhTien.setCellValueFactory(c -> new SimpleStringProperty(df.format(c.getValue().getThanhTien())));
        colThanhTien.setStyle("-fx-alignment: CENTER-RIGHT; -fx-font-weight: bold; -fx-text-fill: #ef4444;");

        colThaoTac.setCellFactory(param -> new TableCell<ChiTietDonDatHang, Void>() {
            private final Button btnXoa = new Button("✕");
            {
                btnXoa.getStyleClass().add("btn-delete-row");
                btnXoa.setOnAction(e -> {
                    listChiTiet.remove(getIndex());
                    tinhTongTien();
                });
            }
            protected void updateItem(Void item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) {
                    setGraphic(null);
                } else {
                    setGraphic(btnXoa);
                    setAlignment(Pos.CENTER);
                }
            }
        });
        tableChiTiet.setItems(listChiTiet);
    }

    private void loadDataToComboBox() {
        // 1. Lấy danh sách Nhà cung cấp 
        List<NhaCungCap> dsNcc = daoNcc.getAllNhaCungCap(); 
        cbNhaCungCap.setItems(FXCollections.observableArrayList(dsNcc));

        // 2. Lấy danh sách Thuốc đưa vào masterList (kể cả NGUNG_BAN — vì đây là nhập hàng)
        ArrayList<Thuoc> dsThuoc = daoThuoc.getAllThuocTatCa(); 
        masterListThuoc.setAll(dsThuoc);
        cbThuoc.setItems(masterListThuoc);

        // ==============================================================
        // CODE HIỂN THỊ HÌNH ẢNH (DÙNG NATIVE TEXT & GRAPHIC ĐỂ KHÔNG MẤT CHỮ)
        // ==============================================================
        javafx.util.Callback<ListView<Thuoc>, ListCell<Thuoc>> cellFactory = param -> new ListCell<Thuoc>() {
            private ImageView imageView = new ImageView();

            {
                // Set kích thước ảnh
                imageView.setFitWidth(35);
                imageView.setFitHeight(35);
                imageView.setPreserveRatio(true);
            }

            @Override
            protected void updateItem(Thuoc item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    // DÙNG TRỰC TIẾP setText CỦA CELL (Tránh bị ẩn chữ khi chọn)
                    setText(item.getTenThuoc());
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;"); // Làm chữ đậm cho dễ nhìn
                    
                    String fileAnh = item.getHinhAnh();
                    if (fileAnh != null && !fileAnh.trim().isEmpty()) {
                        try {
                            java.io.InputStream is = getClass().getResourceAsStream("/resources/images/images_thuoc/" + fileAnh.trim());
                            if (is != null) {
                                imageView.setImage(new javafx.scene.image.Image(is));
                                setGraphic(imageView); // DÙNG TRỰC TIẾP setGraphic CỦA CELL
                            } else {
                                setGraphic(null);
                            }
                        } catch (Exception e) {
                            setGraphic(null);
                        }
                    } else {
                        setGraphic(null);
                    }
                }
            }
        };

        // Áp dụng cho danh sách thả xuống
        cbThuoc.setCellFactory(cellFactory);
        // Áp dụng cho ô đang hiển thị (ButtonCell)
        cbThuoc.setButtonCell(cellFactory.call(null)); 
        
     // 3. Sự kiện: Khi chọn Thuốc -> Load Đơn vị và TÍNH GIÁ LUÔN
        cbThuoc.getSelectionModel().selectedItemProperty().addListener((obs, oldV, thuocDuocChon) -> {
            if (thuocDuocChon != null) {
                ArrayList<DonViQuyDoi> dsDonVi = daoDonVi.getDonViByMaThuoc(thuocDuocChon.getMaThuoc());
                cbDonVi.setItems(FXCollections.observableArrayList(dsDonVi));
                
                if (!dsDonVi.isEmpty()) {
                    cbDonVi.getSelectionModel().selectFirst(); // Chọn đơn vị đầu tiên
                    
                    // ÉP TÍNH GIÁ NGAY LẬP TỨC:
                    DonViQuyDoi dvDauTien = dsDonVi.get(0);
                    double giaGoc = daoThuoc.getGiaNhapGanNhat(thuocDuocChon.getMaThuoc());
                    txtGiaDuKien.setText(String.valueOf(Math.round(giaGoc * dvDauTien.getTyLeQuyDoi())));
                } else {
                    txtGiaDuKien.setText("0"); 
                }
            } else {
                cbDonVi.setItems(FXCollections.observableArrayList());
                txtGiaDuKien.clear();
            }
        });

        // 4. Sự kiện: Khi người dùng đổi Đơn Vị thủ công (ví dụ từ Viên sang Hộp)
        cbDonVi.getSelectionModel().selectedItemProperty().addListener((obs, oldV, dvDuocChon) -> {
            Thuoc thuocHienTai = cbThuoc.getSelectionModel().getSelectedItem();
            if (dvDuocChon != null && thuocHienTai != null) {
                double giaGoc = daoThuoc.getGiaNhapGanNhat(thuocHienTai.getMaThuoc());
                txtGiaDuKien.setText(String.valueOf(Math.round(giaGoc * dvDuocChon.getTyLeQuyDoi())));
            }
        });
    }
    // ==============================================================
    // CHỨC NĂNG LỌC TÌM KIẾM THUỐC
    // ==============================================================
    private void setupSearchThuoc() {
        txtTimThuoc.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                cbThuoc.setItems(masterListThuoc);
            } else {
                ObservableList<Thuoc> filteredList = FXCollections.observableArrayList();
                String keyword = newValue.toLowerCase();
                
                for (Thuoc t : masterListThuoc) {
                    // Lọc theo tên thuốc (Có thể mở rộng lọc theo hoạt chất nếu muốn)
                    if (t.getTenThuoc() != null && t.getTenThuoc().toLowerCase().contains(keyword)) {
                        filteredList.add(t);
                    }
                }
                cbThuoc.setItems(filteredList);
                
                // Tự động bung ComboBox ra cho ngầu khi đang gõ chữ
                if (!filteredList.isEmpty()) {
                    cbThuoc.show();
                } else {
                    cbThuoc.hide();
                }
            }
        });
    }

    @FXML void handleThemThuoc(ActionEvent event) {
        Thuoc thuoc = cbThuoc.getSelectionModel().getSelectedItem();
        DonViQuyDoi dv = cbDonVi.getSelectionModel().getSelectedItem();
        String slStr = txtSoLuong.getText();
        String giaStr = txtGiaDuKien.getText();

        if (thuoc == null || dv == null) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn thuốc và đơn vị tính!"); return;
        }
        if (slStr.isEmpty() || giaStr.isEmpty()) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng nhập đầy đủ số lượng và giá dự kiến!"); return;
        }

        try {
            int sl = Integer.parseInt(slStr);
            double gia = Double.parseDouble(giaStr);
            
            if(sl <= 0 || gia <= 0) throw new NumberFormatException();

            for (ChiTietDonDatHang ct : listChiTiet) {
                if (ct.getThuoc().getMaThuoc().equals(thuoc.getMaThuoc()) && 
                    ct.getDonViQuyDoi().getMaQuyDoi().equals(dv.getMaQuyDoi())) {
                    
                    ct.setSoLuongDat(ct.getSoLuongDat() + sl);
                    ct.setDonGiaDuKien(gia); 
                    
                    tableChiTiet.refresh();
                    tinhTongTien();
                    resetFormThuoc();
                    return;
                }
            }

            ChiTietDonDatHang ct = new ChiTietDonDatHang();
            ct.setThuoc(thuoc);
            ct.setDonViQuyDoi(dv); 
            ct.setSoLuongDat(sl);
            ct.setSoLuongDaNhan(0); 
            ct.setDonGiaDuKien(gia);

            listChiTiet.add(ct);
            tinhTongTien();
            resetFormThuoc();

        } catch (NumberFormatException e) {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi nhập liệu", "Số lượng và Giá dự kiến phải là số hợp lệ!");
        }
    }

    private void tinhTongTien() {
        double tong = 0;
        for (ChiTietDonDatHang ct : listChiTiet) {
            tong += ct.getThanhTien();
        }
        lblTongTien.setText(df.format(tong));
    }

    private void resetFormThuoc() {
        txtTimThuoc.clear(); // Xóa khung tìm kiếm
        cbThuoc.getSelectionModel().clearSelection();
        cbDonVi.getSelectionModel().clearSelection();
        txtSoLuong.clear();
        txtGiaDuKien.clear();
    }

    @FXML void handleTaoDon(ActionEvent event) {
        NhaCungCap ncc = cbNhaCungCap.getSelectionModel().getSelectedItem();
        if (ncc == null) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn Nhà cung cấp!"); return;
        }
        if (listChiTiet.isEmpty()) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Đơn đặt hàng chưa có thuốc nào! Vui lòng thêm thuốc."); return;
        }

        DonDatHang donMoi = new DonDatHang();
        donMoi.setMaDonDatHang(daoDon.getMaDonMoi()); 
        donMoi.setNhaCungCap(ncc);
        
        NhanVien nv = new NhanVien(); 
        nv.setMaNhanVien("NV001"); 
        donMoi.setNhanVien(nv);
        
        double tongTien = 0;
        for (ChiTietDonDatHang ct : listChiTiet) tongTien += ct.getThanhTien();
        donMoi.setTongTienDuTinh(tongTien);
        
        donMoi.setTrangThai("CHO_GIAO"); 
        donMoi.setGhiChu(txtGhiChu.getText());
        if(dpNgayGiao.getValue() != null) {
            donMoi.setNgayGiaoDuKien(Date.valueOf(dpNgayGiao.getValue()));
        }

        boolean isSuccess = daoDon.luuDonDatHangMoi(donMoi, listChiTiet);

        if (isSuccess) {
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Đã tạo Đơn Đặt Hàng Mới thành công!");
            handleDong(null); 
        } else {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi Database", "Có lỗi xảy ra trong quá trình lưu. Vui lòng kiểm tra lại CSDL!");
        }
    }

    @FXML void handleDong(ActionEvent event) {
        Stage stage = (Stage) lblTongTien.getScene().getWindow();
        stage.close();
    }
}