package utils;

public class ValidationUtils {

    /**
     * Chuẩn hóa chuỗi: Trim khoảng trắng 2 đầu và thay thế nhiều khoảng trắng liên tiếp bằng 1 khoảng trắng
     */
    public static String normalizeString(String input) {
        if (input == null || input.trim().isEmpty()) return "";
        return input.trim().replaceAll("\\s+", " ");
    }

    /**
     * Tự động viết hoa chữ cái đầu mỗi từ
     */
    public static String capitalizeName(String input) {
        String normalized = normalizeString(input);
        if (normalized.isEmpty()) return "";
        
        String[] words = normalized.split(" ");
        StringBuilder sb = new StringBuilder();
        for (String w : words) {
            if (!w.isEmpty()) {
                sb.append(Character.toUpperCase(w.charAt(0)));
                if (w.length() > 1) {
                    sb.append(w.substring(1).toLowerCase());
                }
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }

    /**
     * Kiểm tra chuỗi phải chứa ít nhất 1 chữ cái
     */
    public static boolean containsLetter(String input) {
        if (input == null) return false;
        return input.matches(".*[a-zA-ZÀ-ỹ].*");
    }

    /**
     * Kiểm tra chuỗi phải chứa ít nhất 1 chữ cái HOẶC số
     */
    public static boolean containsLetterOrNumber(String input) {
        if (input == null) return false;
        return input.matches(".*[a-zA-Z0-9À-ỹ].*");
    }

    // --- CÁC HÀM VALIDATION MẪU SẴN (Cho phép Controller gọi nhanh) ---

    public static boolean isValidTenKhachHang(String ten) {
        return ten.matches("^[a-zA-ZÀ-ỹ\\s'-]{2,100}$") && containsLetter(ten);
    }

    public static boolean isValidDiaChi(String diaChi) {
        return diaChi.matches("^[a-zA-Z0-9À-ỹ\\s,./#()\\-]{2,255}$") && containsLetterOrNumber(diaChi);
    }

    public static boolean isValidSdt(String sdt) {
        return sdt.matches("^0\\d{9}$");
    }

    public static boolean isValidTenNhaCungCap(String ten) {
        return ten.matches("^[a-zA-Z0-9À-ỹ\\s,./&()\\-+]{2,150}$") && containsLetter(ten);
    }

    public static boolean isValidTenThuoc(String ten) {
        return ten.matches("^[a-zA-Z0-9À-ỹ\\s,.\\-()\\[\\]+%/*]+$") && containsLetter(ten) && ten.length() >= 2 && ten.length() <= 150;
    }

    public static boolean isValidHangSanXuat(String hang) {
        return hang.matches("^[a-zA-Z0-9À-ỹ\\s,.\\-&']{2,100}$") && containsLetter(hang);
    }

    public static boolean isSoftValidText(String text) {
        return text != null && text.length() <= 255 && text.length() >= 2 && containsLetterOrNumber(text);
    }
}
