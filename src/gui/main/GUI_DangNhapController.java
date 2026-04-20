package gui.main;

import dao.DAO_NhanVien;
import entity.NhanVien;
import javafx.animation.AnimationTimer;
import javafx.animation.FadeTransition;
import javafx.animation.ScaleTransition;
import javafx.animation.TranslateTransition;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.effect.Glow;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;
import utils.AlertUtils;
import utils.UserSession;
import utils.WindowUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Random;

public class GUI_DangNhapController {

    @FXML private StackPane rootPane;
    @FXML private AnchorPane animeBackgroundPane;
    @FXML private VBox mainCard; 
    @FXML private ImageView imgLogo; 
    @FXML private Button btnDangNhap;
    @FXML private TextField txtTenDangNhap;
    @FXML private PasswordField txtMatKhau;

    private DAO_NhanVien nhanVienDao = new DAO_NhanVien();
    
    // Quản lý Animation (Bụi lấp lánh & Pháo bông)
    private List<AnimeEntity> backgroundDust = new ArrayList<>();
    private List<Firework> fireworks = new ArrayList<>();
    private List<FireworkParticle> fwParticles = new ArrayList<>();
    private Random random = new Random();

    @FXML
    public void initialize() {
        if (animeBackgroundPane != null) {
            createAnimeScene();
            startAnimeEngine();
        }

        // Animation xuất hiện thẻ đăng nhập
        mainCard.setOpacity(0);
        mainCard.setTranslateY(40);
        FadeTransition fadeIn = new FadeTransition(Duration.seconds(1.2), mainCard);
        fadeIn.setToValue(1);
        TranslateTransition slideUp = new TranslateTransition(Duration.seconds(1.2), mainCard);
        slideUp.setByY(-40);
        fadeIn.play();
        slideUp.play();

        // Logo bay lơ lửng mượt mà
        TranslateTransition floatLogo = new TranslateTransition(Duration.seconds(2), imgLogo);
        floatLogo.setByY(-10);
        floatLogo.setAutoReverse(true);
        floatLogo.setCycleCount(TranslateTransition.INDEFINITE);
        floatLogo.play();

        // Hiệu ứng nảy nút Đăng nhập
        btnDangNhap.setOnMouseEntered(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), btnDangNhap);
            st.setToX(1.05); st.setToY(1.05); st.play();
        });
        btnDangNhap.setOnMouseExited(e -> {
            ScaleTransition st = new ScaleTransition(Duration.millis(200), btnDangNhap);
            st.setToX(1.0); st.setToY(1.0); st.play();
        });
    }

    private void createAnimeScene() {
        // Nền hạt bụi vàng lấp lánh sang trọng
        Color[] pColors = {Color.WHITE, Color.web("#fbbf24"), Color.web("#e0f2fe")};
        for (int i = 0; i < 200; i++) {
            backgroundDust.add(new AnimeEntity(pColors[random.nextInt(pColors.length)]));
        }
        
        backgroundDust.forEach(e -> animeBackgroundPane.getChildren().add(e.node));
    }

    // =================================================================
    // 🎇 ENGINE PHÁO HOA XUNG QUANH KHUNG ĐĂNG NHẬP (BẢN SLOW MOTION)
    // =================================================================
    private void startAnimeEngine() {
        new AnimationTimer() {
            long start = System.nanoTime();
            @Override
            public void handle(long now) {
                double t = (now - start) / 1_000_000_000.0;
                
                backgroundDust.forEach(e -> e.update(t));

                if (random.nextDouble() < 0.04) {
                    int burstCount = random.nextDouble() < 0.2 ? random.nextInt(2) + 1 : 1;
                    for(int i = 0; i < burstCount; i++) {
                        Firework fw = new Firework();
                        fireworks.add(fw);
                        animeBackgroundPane.getChildren().add(fw.node);
                    }
                }

                // Cập nhật Pháo bắn lên
                Iterator<Firework> fwIt = fireworks.iterator();
                while (fwIt.hasNext()) {
                    Firework fw = fwIt.next();
                    if (fw.update()) {
                        // 🌟 PHÁO NỔ TỪ TỪ (100 - 150 tia lửa)
                        int particleCount = random.nextInt(50) + 100;
                        for (int i = 0; i < particleCount; i++) {
                            FireworkParticle fp = new FireworkParticle(fw.x, fw.y, fw.color);
                            fwParticles.add(fp);
                            animeBackgroundPane.getChildren().add(fp.node);
                        }
                        
                        // 💡 ĐÃ SỬA: HIỆU ỨNG FLASH SÁNG LÊN VÀ TẮT ĐI TỪ TỪ (500ms thay vì 200ms)
                        Circle flash = new Circle(fw.x, fw.y, 200, Color.WHITE);
                        flash.setEffect(new Glow(1.0));
                        animeBackgroundPane.getChildren().add(flash);
                        FadeTransition ft = new FadeTransition(Duration.millis(500), flash);
                        ft.setFromValue(0.8);
                        ft.setToValue(0);
                        ft.setOnFinished(e -> animeBackgroundPane.getChildren().remove(flash));
                        ft.play();

                        animeBackgroundPane.getChildren().remove(fw.node);
                        fwIt.remove();
                    }
                }

                // Cập nhật Tia lửa rơi xuống
                Iterator<FireworkParticle> fpIt = fwParticles.iterator();
                while (fpIt.hasNext()) {
                    FireworkParticle fp = fpIt.next();
                    if (fp.update()) {
                        animeBackgroundPane.getChildren().remove(fp.node);
                        fpIt.remove();
                    }
                }
            }
        }.start();
    }

    // --- QUẢN LÝ BỤI NỀN ---
    private class AnimeEntity {
        javafx.scene.Node node;
        double dx, dy;

        public AnimeEntity(Color c) { 
            node = new Circle(random.nextDouble() * 2 + 1, c);
            node.setOpacity(random.nextDouble() * 0.6 + 0.1);
            node.setLayoutX(random.nextDouble() * 950);
            node.setLayoutY(random.nextDouble() * 600);
            dx = (random.nextDouble() - 0.5) * 1.0;
            dy = (random.nextDouble() - 0.5) * 1.0;
        }

        public void update(double t) {
            node.setLayoutX(node.getLayoutX() + dx);
            node.setLayoutY(node.getLayoutY() + dy);
            if (node.getLayoutX() < -10) node.setLayoutX(960);
            if (node.getLayoutX() > 960) node.setLayoutX(-10);
            if (node.getLayoutY() < -10) node.setLayoutY(610);
            if (node.getLayoutY() > 610) node.setLayoutY(-10);
        }
    }

    // --- QUẢN LÝ PHÁO BÔNG BẮN LÊN ---
    private class Firework {
        Circle node;
        double x, y, dy;
        Color color;

        public Firework() {
            if (random.nextBoolean()) {
                x = random.nextDouble() * 250; 
            } else {
                x = 700 + random.nextDouble() * 250; 
            }
            
            y = 650; 
            // 💡 ĐÃ SỬA: Lực bắn lên mượt hơn một chút
            dy = -(random.nextDouble() * 5 + 9); 
            
            color = Color.hsb(random.nextDouble() * 360, 1.0, 1.0);
            
            node = new Circle(3.5, color); 
            node.setEffect(new Glow(1.0)); 
            node.setLayoutX(x);
            node.setLayoutY(y);
        }

        public boolean update() {
            y += dy;
            // 💡 ĐÃ SỬA: Lực kéo trái đất nhẹ hơn lúc bay lên -> Bay lả lướt hơn
            dy += 0.15; 
            node.setLayoutY(y);
            return dy >= -0.5; // Nổ khi gần chạm đỉnh
        }
    }

    // --- QUẢN LÝ TIA LỬA NỔ TUNG TÓE (VẬT LÝ LƠ LỬNG) ---
    private class FireworkParticle {
        Circle node;
        double x, y, dx, dy, life, maxLife;

        public FireworkParticle(double startX, double startY, Color baseColor) {
            x = startX; y = startY;
            double angle = random.nextDouble() * Math.PI * 2;
            
            // 💡 ĐÃ SỬA 1: TỐC ĐỘ BUNG RA TỪ TỪ (Giảm từ 20 xuống 12)
            double speed = random.nextDouble() * 12 + 2; 
            dx = Math.cos(angle) * speed;
            dy = Math.sin(angle) * speed;
            
            // 💡 ĐÃ SỬA 2: TĂNG THỜI GIAN SỐNG LÊN ĐỂ TIA LỬA LƠ LỬNG LÂU HƠN
            maxLife = random.nextInt(100) + 80; 
            life = maxLife;
            
            Color particleColor = random.nextDouble() > 0.8 ? Color.WHITE : baseColor;
            double size = random.nextDouble() * 3.5 + 1.0;
            node = new Circle(size, particleColor);
            
            node.setEffect(new Glow(1.0));
            node.setLayoutX(x);
            node.setLayoutY(y);
        }

        public boolean update() {
            x += dx;
            y += dy;
            
            // 💡 ĐÃ SỬA 3: VẬT LÝ SIÊU MƯỢT
            dx *= 0.96; // Giảm lực ma sát -> Trôi ra xa mượt mà hơn (Không phanh gấp)
            dy *= 0.96; 
            dy += 0.06; // Trọng lực siêu nhẹ -> Rơi lơ lửng như bụi tiên thay vì rớt vèo xuống
            
            life--;
            
            // Mờ đi từ từ một cách uyển chuyển
            node.setOpacity((life / maxLife) * 1.2); 
            
            // Kích thước thu nhỏ dần mượt mà
            double scale = (life / maxLife);
            node.setScaleX(scale);
            node.setScaleY(scale);
            
            node.setLayoutX(x);
            node.setLayoutY(y);
            
            return life <= 0; 
        }
    }

    // =================================================================
    // LOGIC ĐĂNG NHẬP 
    // =================================================================
    @FXML
    void handleDangNhap(ActionEvent event) {
        String taiKhoan = txtTenDangNhap.getText().trim();
        String matKhau  = txtMatKhau.getText();

        if (taiKhoan.isEmpty() || matKhau.isEmpty()) {
            AlertUtils.showAlert(Alert.AlertType.WARNING, "Thiếu thông tin", "Vui lòng nhập đầy đủ tài khoản và mật khẩu!");
            return;
        }

        NhanVien nv = nhanVienDao.dangNhap(taiKhoan, matKhau);
        if (nv != null) {
        	if (nv.getTrangThai() == 2) {
        	    AlertUtils.showAlert(
        	        Alert.AlertType.ERROR,
        	        "Thất bại",
        	        "Tài khoản của bạn đã bị khóa!\nVui lòng liên hệ Quản lý để được hỗ trợ."
        	    );
        	    return;
        	}
            UserSession.getInstance().setUser(nv);
            WindowUtils.closeWindow(event);

            try {
                javafx.fxml.FXMLLoader loader = new javafx.fxml.FXMLLoader(getClass().getResource("/gui/main/GUI_TrangChu.fxml"));
                javafx.scene.Parent root = loader.load();
                javafx.stage.Stage mainStage = new javafx.stage.Stage();
                mainStage.setTitle("Long Nguyên Pharma - " + nv.getHoTen());
                
                mainStage.setScene(new javafx.scene.Scene(root));
                mainStage.setMaximized(true); 
                mainStage.show();
            } catch (Exception e) { e.printStackTrace(); }
        } else {
            AlertUtils.showAlert(Alert.AlertType.ERROR, "Thất bại", "Tài khoản hoặc mật khẩu không đúng!");
        }
    }
}