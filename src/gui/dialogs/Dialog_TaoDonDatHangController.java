package gui.dialogs;

import dao.DAO_DonDatHang;
import dao.DAO_NhaCungCap;
import dao.DAO_Thuoc;
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
import javafx.util.StringConverter; // Thư viện để format ngày
import utils.AlertUtils;

import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter; // Thư viện định dạng ngày
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

public class Dialog_TaoDonDatHangController {

    // --- Khai báo UI ---
    @FXML private ComboBox<NhaCungCap> cbNhaCungCap;
    @FXML private DatePicker dpNgayGiao;
    @FXML private TextField txtGhiChu;
    
    // UI Nhập thuốc
    @FXML private TextField txtTimThuoc; 
    @FXML private ComboBox<Thuoc> cbThuoc;
    @FXML private TextField txtDonVi;
    @FXML private TextField txtSoLuong, txtGiaDuKien;
    
    @FXML private TableView<ChiTietDonDatHang> tableChiTiet;
    @FXML private TableColumn<ChiTietDonDatHang, String> colTenThuoc, colDonVi;
    @FXML private TableColumn<ChiTietDonDatHang, Integer> colSoLuong;
    @FXML private TableColumn<ChiTietDonDatHang, String> colGiaDuKien, colThanhTien;
    @FXML private TableColumn<ChiTietDonDatHang, Void> colThaoTac;
    @FXML private Label lblTongTien;

    // --- Khai báo biến toàn cục & DAO ---
    private ObservableList<ChiTietDonDatHang> listChiTiet = FXCollections.observableArrayList();
    private ObservableList<Thuoc> masterListThuoc = FXCollections.observableArrayList(); 
    
    private DAO_DonDatHang daoDon = new DAO_DonDatHang();
    private DAO_NhaCungCap daoNcc = new DAO_NhaCungCap();
    private DAO_Thuoc daoThuoc = new DAO_Thuoc();
    private DonViQuyDoi donViDuocChon = null;
    
    private DecimalFormat df = new DecimalFormat("#,### VNĐ");
    private DecimalFormat dfInput = new DecimalFormat("#,###"); // Định dạng cho ô nhập liệu

    @FXML public void initialize() {
        setupTable();
        loadDataToComboBox();
        setupSearchThuoc(); 
        setupInputFormatting(); // Kích hoạt format ô nhập tiền
        
        // 🚨 ĐỊNH DẠNG NGÀY GIAO THÀNH DD/MM/YYYY 🚨
        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        dpNgayGiao.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                if (date != null) {
                    return dateFormatter.format(date);
                } else {
                    return "";
                }
            }
            @Override
            public LocalDate fromString(String string) {
                if (string != null && !string.isEmpty()) {
                    return LocalDate.parse(string, dateFormatter);
                } else {
                    return null;
                }
            }
        });

        // Mặc định ngày giao dự kiến là ngày mai
        dpNgayGiao.setValue(LocalDate.now().plusDays(1));
        
        // 🚨 CHẶN NGƯỜI DÙNG CHỌN NGÀY TRONG QUÁ KHỨ 🚨
        dpNgayGiao.setDayCellFactory(picker -> new DateCell() {
            @Override
            public void updateItem(LocalDate date, boolean empty) {
                super.updateItem(date, empty);
                LocalDate today = LocalDate.now();
                // Disable (làm mờ và cấm click) những ngày trước hôm nay
                setDisable(empty || date.compareTo(today) < 0);
            }
        });
    }

    // 🚨 FORMAT Ô NHẬP GIÁ TIỀN REAL-TIME 🚨
    private void setupInputFormatting() {
        txtGiaDuKien.textProperty().addListener((obs, oldV, newV) -> {
            if (newV == null || newV.isEmpty()) return;
            // Xóa tất cả các ký tự không phải số để lấy giá trị gốc
            String cleanStr = newV.replaceAll("[^\\d]", "");
            try {
                if (!cleanStr.isEmpty()) {
                    long val = Long.parseLong(cleanStr);
                    // Ép dấu phân cách thành dấu chấm cho thân thiện với VN
                    String formatted = dfInput.format(val).replace(',', '.'); 
                    if (!newV.equals(formatted)) {
                        txtGiaDuKien.setText(formatted);
                    }
                }
            } catch (NumberFormatException e) {
                txtGiaDuKien.setText(oldV);
            }
        });
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
        List<NhaCungCap> dsNcc = daoNcc.getAllNhaCungCap(); 
        cbNhaCungCap.setItems(FXCollections.observableArrayList(dsNcc));

        ArrayList<Thuoc> dsThuoc = daoThuoc.getAllThuocTatCa(); 
        masterListThuoc.setAll(dsThuoc);
        cbThuoc.setItems(masterListThuoc);

        javafx.util.Callback<ListView<Thuoc>, ListCell<Thuoc>> cellFactory = param -> new ListCell<Thuoc>() {
            private ImageView imageView = new ImageView();
            {
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
                    setText(item.getTenThuoc());
                    setStyle("-fx-font-weight: bold; -fx-text-fill: #1e293b;"); 
                    
                    String fileAnh = item.getHinhAnh();
                    if (fileAnh != null && !fileAnh.trim().isEmpty()) {
                        try {
                            java.io.InputStream is = getClass().getResourceAsStream("/resources/images/images_thuoc/" + fileAnh.trim());
                            if (is != null) {
                                imageView.setImage(new javafx.scene.image.Image(is));
                                setGraphic(imageView); 
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

        cbThuoc.setCellFactory(cellFactory);
        cbThuoc.setButtonCell(cellFactory.call(null)); 
        
        cbThuoc.getSelectionModel().selectedItemProperty().addListener((obs, oldV, thuocDuocChon) -> {
            if (thuocDuocChon != null) {
                DonViQuyDoi dvLonNhat = new dao.DAO_DonViQuyDoi().getDonViLonNhatCuaThuoc(thuocDuocChon.getMaThuoc());
                
                if (dvLonNhat != null) {
                    donViDuocChon = dvLonNhat; 
                    txtDonVi.setText(dvLonNhat.getTenDonVi()); 
                    
                    double giaGoc = daoThuoc.getGiaNhapGanNhat(thuocDuocChon.getMaThuoc());
                    long giaMoi = Math.round(giaGoc * dvLonNhat.getTyLeQuyDoi());
                    // 🚨 Đẩy giá lên ô text có kèm format dấu chấm 🚨
                    txtGiaDuKien.setText(dfInput.format(giaMoi).replace(',', '.'));
                } else {
                    donViDuocChon = null;
                    txtDonVi.setText("Lỗi ĐV");
                    txtGiaDuKien.setText("0");
                    AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi Dữ Liệu", "Thuốc chưa có Đơn vị trong DB!");
                }
            } else {
                donViDuocChon = null;
                txtDonVi.clear();
                txtGiaDuKien.clear();
            }
        });
    }

    private void setupSearchThuoc() {
        txtTimThuoc.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.trim().isEmpty()) {
                cbThuoc.setItems(masterListThuoc);
            } else {
                ObservableList<Thuoc> filteredList = FXCollections.observableArrayList();
                String keyword = newValue.toLowerCase();
                
                for (Thuoc t : masterListThuoc) {
                    if (t.getTenThuoc() != null && t.getTenThuoc().toLowerCase().contains(keyword)) {
                        filteredList.add(t);
                    }
                }
                cbThuoc.setItems(filteredList);
                
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
        DonViQuyDoi dv = donViDuocChon; 
        
        String slStr = txtSoLuong.getText();
        // 🚨 XÓA BỎ DẤU CHẤM TRƯỚC KHI PARSE ĐỂ KHÔNG BỊ VĂNG LỖI CHUỖI 🚨
        String giaStr = txtGiaDuKien.getText().replaceAll("[^\\d]", "");

        if (thuoc == null || dv == null) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Vui lòng chọn thuốc (Thuốc phải có đơn vị hợp lệ)!"); return;
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
        txtTimThuoc.clear(); 
        cbThuoc.getSelectionModel().clearSelection();
        
        txtDonVi.clear();
        donViDuocChon = null;
        
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