package gui.dialogs;

import dao.DAO_Thuoc;
import entity.Thuoc;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.scene.control.TableCell;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Stage;

public class Dialog_ChonThuocController {

    @FXML private TextField txtTim;
    @FXML private TableView<Thuoc> tblThuoc;
    @FXML private TableColumn<Thuoc, String> colMa;
    @FXML private TableColumn<Thuoc, String> colTen;
    @FXML private TableColumn<Thuoc, String> colTrieuChung;
    @FXML private TableColumn<Thuoc, Boolean> colKeDon;

    private final DAO_Thuoc daoThuoc = new DAO_Thuoc();
    private final ObservableList<Thuoc> dsThuoc = FXCollections.observableArrayList();
    private Thuoc thuocChon;

    @FXML
    public void initialize() {
        colMa.setCellValueFactory(new PropertyValueFactory<>("maThuoc"));
        colTen.setCellValueFactory(new PropertyValueFactory<>("tenThuoc"));
        colTrieuChung.setCellValueFactory(new PropertyValueFactory<>("trieuChung"));
        colKeDon.setCellValueFactory(new PropertyValueFactory<>("canKeDon"));
        colKeDon.setCellFactory(c -> new TableCell<>() {
            @Override
            protected void updateItem(Boolean item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setStyle("");
                } else {
                    setText(item ? "Có" : "Không");
                    setStyle(item ? "-fx-text-fill: #ef4444; -fx-font-weight: bold;" : "-fx-text-fill: #10b981; -fx-font-weight: bold;");
                }
            }
        });

        dsThuoc.setAll(daoThuoc.getAllThuocDangBan());
        FilteredList<Thuoc> filtered = new FilteredList<>(dsThuoc, p -> true);
        txtTim.textProperty().addListener((obs, ov, nv) -> {
            filtered.setPredicate(t -> {
                if (nv == null || nv.trim().isEmpty()) return true;
                String f = nv.toLowerCase();
                if (t.getMaThuoc() != null && t.getMaThuoc().toLowerCase().contains(f)) return true;
                if (t.getTenThuoc() != null && t.getTenThuoc().toLowerCase().contains(f)) return true;
                if (t.getTrieuChung() != null && t.getTrieuChung().toLowerCase().contains(f)) return true;
                if (t.getCongDung() != null && t.getCongDung().toLowerCase().contains(f)) return true;
                return false;
            });
        });
        SortedList<Thuoc> sorted = new SortedList<>(filtered);
        sorted.comparatorProperty().bind(tblThuoc.comparatorProperty());
        tblThuoc.setItems(sorted);

        tblThuoc.setOnMouseClicked(e -> {
            if (e.getClickCount() == 2 && e.isPrimaryButtonDown()) {
                Thuoc t = tblThuoc.getSelectionModel().getSelectedItem();
                if (t != null) {
                    thuocChon = t;
                    close();
                }
            }
        });
    }

    @FXML
    private void handleChon() {
        thuocChon = tblThuoc.getSelectionModel().getSelectedItem();
        close();
    }

    @FXML
    private void handleHuy() {
        thuocChon = null;
        close();
    }

    private void close() {
        Stage stage = (Stage) tblThuoc.getScene().getWindow();
        stage.close();
    }

    public Thuoc getThuocChon() {
        return thuocChon;
    }
}

