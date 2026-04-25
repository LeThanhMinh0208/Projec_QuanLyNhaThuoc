package gui.dialogs;

import dao.*;
import entity.*;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.*;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.*;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.*;
import javafx.stage.Stage;
import javafx.util.StringConverter;
import utils.*;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javafx.event.ActionEvent; 
import javafx.scene.control.Alert;

public class Dialog_XuatChuyenKhoController implements Initializable {
    @FXML private ComboBox<String> cbKhoXuat;
    @FXML private TextField txtKhoNhan, txtNguoiLap, txtNguoiVanChuyen, txtTimNhanhThuoc, txtSoLuongChuyen, txtGhiChu;
    @FXML private ComboBox<Thuoc> cbChonThuoc;
    @FXML private ComboBox<LoThuoc> cbChonLo;
    @FXML private TableView<ChiTietPhieuXuat> tableThuocChuyen;
    @FXML private TableColumn<ChiTietPhieuXuat, Integer> colSTT, colSoLuong;
    @FXML private TableColumn<ChiTietPhieuXuat, String> colTenThuoc, colSoLo, colXoa;

    private ObservableList<ChiTietPhieuXuat> dsXuatTam = FXCollections.observableArrayList();
    private DAO_LoThuoc daoLo = new DAO_LoThuoc();
    private DAO_PhieuXuat daoPX = new DAO_PhieuXuat();

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        setupTable();
        if (UserSession.getInstance().getUser() != null) {
            txtNguoiLap.setText(UserSession.getInstance().getUser().getHoTen());
        }

        // Khai báo danh sách thuốc động (sẽ thay đổi khi đổi kho)
        ObservableList<Thuoc> danhSachThuocKho = FXCollections.observableArrayList();
        FilteredList<Thuoc> filter = new FilteredList<>(danhSachThuocKho, p -> true);
        cbChonThuoc.setItems(filter);
        setupComboThuoc();

        txtTimNhanhThuoc.textProperty().addListener((o, oldV, newV) -> {
            filter.setPredicate(t -> newV == null || newV.isEmpty() || t.getTenThuoc().toLowerCase().contains(newV.toLowerCase()));
            if (!newV.isEmpty()) cbChonThuoc.show();
        });

        // ========================================================
        // LOGIC CHỌN KHO -> CẬP NHẬT DANH SÁCH THUỐC
        // ========================================================
        cbKhoXuat.getItems().addAll("Kho dự trữ", "Kho bán hàng");
        cbKhoXuat.valueProperty().addListener((o, oldV, newV) -> {
            if (newV != null) {
                // 1. Cập nhật tên kho nhận
                txtKhoNhan.setText(newV.equals("Kho dự trữ") ? "Kho bán hàng" : "Kho dự trữ");
                
                // 2. Reset các ô chọn thuốc & chọn lô cũ
                cbChonThuoc.getSelectionModel().clearSelection();
                cbChonLo.getItems().clear();
                txtSoLuongChuyen.setText("");
                
                // 3. Tải danh sách thuốc CHỈ CÓ TRONG KHO VỪA CHỌN
                String maKho = newV.equals("Kho dự trữ") ? "KHO_DU_TRU" : "KHO_BAN_HANG";
                danhSachThuocKho.setAll(new DAO_Thuoc().getThuocCoLoTrongKho(maKho));
            }
        });

        cbChonLo.setConverter(new StringConverter<LoThuoc>() {
            @Override public String toString(LoThuoc l) { return l==null?"":"Lô: "+l.getMaLoThuoc()+" (Tồn: "+l.getSoLuongTon()+")"; }
            @Override public LoThuoc fromString(String s) { return null; }
        });

        // KHI CHỌN LÔ -> TỰ ĐỘNG ĐIỀN MAX SỐ LƯỢNG VÀ KHÓA Ô LẠI
        cbChonLo.valueProperty().addListener((o, old, lo) -> {
            if (lo != null) {
                txtSoLuongChuyen.setText(String.valueOf(lo.getSoLuongTon()));
                txtSoLuongChuyen.setDisable(true); // Khóa ô nhập, ép chuyển full
            } else {
                txtSoLuongChuyen.setText("");
            }
        });

        // KHI CHỌN THUỐC -> TẢI LÔ CỦA KHO XUẤT ĐÓ
        cbChonThuoc.valueProperty().addListener((o, oldV, s) -> {
            if (s != null && cbKhoXuat.getValue() != null) {
                String k = cbKhoXuat.getValue().equals("Kho dự trữ") ? "KHO_DU_TRU" : "KHO_BAN_HANG";
                cbChonLo.setItems(FXCollections.observableArrayList(daoLo.getLoThuocTheoFEFO(s.getMaThuoc(), k)));
            } else {
                cbChonLo.getItems().clear();
            }
        });

        // Mặc định chọn "Kho dự trữ" khi vừa mở form lên
        cbKhoXuat.getSelectionModel().selectFirst();
    }

    private void setupComboThuoc() {
        cbChonThuoc.setCellFactory(lv -> new ListCell<Thuoc>() {
            private final ImageView iv = new ImageView();
            @Override protected void updateItem(Thuoc t, boolean e) {
                super.updateItem(t, e);
                if (e || t == null) { setGraphic(null); setText(null); }
                else {
                    setText(t.getTenThuoc());
                    try {
                        InputStream is = getClass().getResourceAsStream("/resources/images/images_thuoc/" + t.getHinhAnh().trim());
                        if (is != null) { iv.setImage(new Image(is)); iv.setFitWidth(40); iv.setFitHeight(30); setGraphic(iv); }
                    } catch (Exception ex) { setGraphic(null); }
                }
            }
        });
        cbChonThuoc.setButtonCell(cbChonThuoc.getCellFactory().call(null));
    }


    private void setupTable() {
        colSTT.setCellValueFactory(c -> new ReadOnlyObjectWrapper<>(dsXuatTam.indexOf(c.getValue()) + 1));
        
        // 1. Ánh xạ Tên Thuốc
        colTenThuoc.setCellValueFactory(c -> {
            String ma = c.getValue().getMaThuoc();
            for(Thuoc t : cbChonThuoc.getItems()) {
                if(t.getMaThuoc().equals(ma)) return new javafx.beans.property.SimpleStringProperty(t.getTenThuoc());
            }
            return new javafx.beans.property.SimpleStringProperty(ma);
        });

        // 2. Ánh xạ Số Lô và Số Lượng trực tiếp
        // 🚨 CHÚ Ý: Nếu chữ getSoLo() bị gạch đỏ, sếp hãy đổi nó thành getMaLo() hoặc getMaLoThuoc() cho khớp với file Entity ChiTietPhieuXuat nhé!
        colSoLo.setCellValueFactory(c -> new javafx.beans.property.SimpleStringProperty(c.getValue().getSoLo()));
        colSoLuong.setCellValueFactory(c -> new javafx.beans.property.SimpleObjectProperty<>(c.getValue().getSoLuong()));
        
        colXoa.setCellFactory(c -> new TableCell<>() {
            private final Button b = new Button("✕");
            { 
                b.setStyle("-fx-background-color: #ef4444; -fx-text-fill: white; -fx-cursor: hand; -fx-font-weight: bold;"); 
                b.setOnAction(e -> { 
                    dsXuatTam.remove(getTableView().getItems().get(getIndex())); 
                    tableThuocChuyen.refresh(); 
                }); 
            }
            @Override protected void updateItem(String i, boolean e) {
                super.updateItem(i, e); setGraphic(e ? null : b); setAlignment(Pos.CENTER);
            }
        });
        tableThuocChuyen.setItems(dsXuatTam);
    }

    @FXML private void handleThemThuoc() {
        Thuoc t = cbChonThuoc.getValue(); LoThuoc l = cbChonLo.getValue();
        if (t == null || l == null || txtSoLuongChuyen.getText().isEmpty()) return;
        
        // Kiểm tra xem lô này đã được đưa vào danh sách chuyển chưa
        for(ChiTietPhieuXuat ct : dsXuatTam) {
            if(ct.getSoLo().equals(l.getMaLoThuoc())) {
                AlertUtils.showAlert(Alert.AlertType.WARNING, "Cảnh báo", "Lô này đã có trong danh sách chờ chuyển!");
                return;
            }
        }
        
        int sl = Integer.parseInt(txtSoLuongChuyen.getText());
        dsXuatTam.add(new ChiTietPhieuXuat(null, t.getMaThuoc(), l.getMaLoThuoc(), sl, l.getGiaNhap(), sl*l.getGiaNhap()));
    }
    @FXML 
    private void handleXacNhanChuyen() {
        if (dsXuatTam.isEmpty()) return;
        String maKhoNhan = txtKhoNhan.getText().equals("Kho bán hàng") ? "KHO_BAN_HANG" : "KHO_DU_TRU";
        
        // 1. GỌI HÀM SINH MÃ TỰ ĐỘNG THAY VÌ DÙNG System.currentTimeMillis()
        String maPhieuMoi = daoPX.getMaPhieuXuatMoi("CK");
        
        // 2. TẠO PHIẾU VỚI MÃ VỪA SINH
        PhieuXuat px = new PhieuXuat(maPhieuMoi, null, UserSession.getInstance().getUser().getMaNhanVien(), 1, null, maKhoNhan, 0, txtGhiChu.getText()+" | VC: "+txtNguoiVanChuyen.getText());
        
        // 3. LƯU VÀO DATABASE
        if (daoPX.chuyenKhoNoiBo(px, new ArrayList<>(dsXuatTam), maKhoNhan)) {
            AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Thành công", "Chuyển kho hoàn tất! Mã phiếu: " + maPhieuMoi); 
            ((Stage) txtKhoNhan.getScene().getWindow()).close();
        }
    }
    @FXML
    private void handleImportCSV(ActionEvent event) {
        javafx.stage.FileChooser fileChooser = new javafx.stage.FileChooser();
        fileChooser.setTitle("Chọn file CSV Lệnh Chuyển Kho");
        fileChooser.getExtensionFilters().add(new javafx.stage.FileChooser.ExtensionFilter("CSV Files", "*.csv"));
        java.io.File file = fileChooser.showOpenDialog(txtKhoNhan.getScene().getWindow());

        if (file != null) {
            try (java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(new java.io.FileInputStream(file), "UTF-8"))) {
                String line;
                int lineNumber = 0;
                int successCount = 0;
                
                // Xóa danh sách cũ trước khi nạp lệnh mới
                dsXuatTam.clear();

                while ((line = br.readLine()) != null) {
                    lineNumber++;
                    if (lineNumber == 1 && line.startsWith("\uFEFF")) line = line.substring(1); // Gỡ BOM
                    if (line.trim().isEmpty()) continue;

                    String separator = line.contains(";") ? ";" : ",";
                    String[] cols = line.split(separator);

                    // --- BƯỚC 1: ĐỌC DÒNG 1 ĐỂ GÁN KHO LÊN GIAO DIỆN ---
                    if (lineNumber == 1) {
                        if (cols.length >= 2) {
                            String tenKhoXuatFile = cols[1].trim().replaceAll("^\"|\"$", "");
                            
                            // Tự động chọn Kho Xuất trên UI (Lệnh này sẽ kích hoạt listener tự đổi Kho Nhận luôn)
                            boolean foundWarehouse = false;
                            for (String item : cbKhoXuat.getItems()) {
                                if (item.equalsIgnoreCase(tenKhoXuatFile)) {
                                    cbKhoXuat.getSelectionModel().select(item);
                                    foundWarehouse = true;
                                    break;
                                }
                            }
                            
                            if (!foundWarehouse) {
                                AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi file", "Tên kho xuất '" + tenKhoXuatFile + "' không hợp lệ!");
                                return;
                            }
                        }
                        continue;
                    }

                    // Bỏ qua dòng tiêu đề cột (Dòng 2)
                    if (lineNumber == 2) continue;

                    // --- BƯỚC 2: NẠP DANH SÁCH THUỐC ---
                    if (cols.length < 2) continue;

                    String tenThuocFile = cols[0].trim().replaceAll("^\"|\"$", "");
                    String soLoFile = cols[1].trim().replaceAll("^\"|\"$", "");
                    String maKhoXuatDB = cbKhoXuat.getValue().equals("Kho dự trữ") ? "KHO_DU_TRU" : "KHO_BAN_HANG";

                    // Tìm thuốc trong hệ thống
                    Thuoc thuocFound = null;
                    for (Thuoc t : new DAO_Thuoc().getThuocCoLoTrongKho(maKhoXuatDB)) {
                        if (t.getTenThuoc().equalsIgnoreCase(tenThuocFile)) {
                            thuocFound = t;
                            break;
                        }
                    }

                    if (thuocFound != null) {
                        java.util.List<LoThuoc> dsLo = daoLo.getLoThuocTheoFEFO(thuocFound.getMaThuoc(), maKhoXuatDB);
                        LoThuoc loFound = null;
                        for (LoThuoc l : dsLo) {
                            if (l.getMaLoThuoc().equalsIgnoreCase(soLoFile)) {
                                loFound = l;
                                break;
                            }
                        }

                        if (loFound != null && loFound.getSoLuongTon() > 0) {
                            int slFull = loFound.getSoLuongTon();
                            dsXuatTam.add(new ChiTietPhieuXuat(null, thuocFound.getMaThuoc(), loFound.getMaLoThuoc(), slFull, loFound.getGiaNhap(), slFull * loFound.getGiaNhap()));
                            successCount++;
                        }
                    }
                }

                tableThuocChuyen.refresh();
                AlertUtils.showAlert(Alert.AlertType.INFORMATION, "Thành công", 
                    "Đã thiết lập lộ trình từ [" + cbKhoXuat.getValue() + "] sang [" + txtKhoNhan.getText() + "]\n" +
                    "Nạp thành công " + successCount + " lô thuốc.");

            } catch (Exception e) {
                e.printStackTrace();
                AlertUtils.showAlert(Alert.AlertType.ERROR, "Lỗi", "Không thể đọc file: " + e.getMessage());
            }
        }
    }
    
    @FXML private void handleHuyBo() { ((Stage) txtKhoNhan.getScene().getWindow()).close(); }
}