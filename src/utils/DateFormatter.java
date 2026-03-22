package utils;

import java.sql.Date;
import java.text.SimpleDateFormat;

public class DateFormatter {
    // Định dạng chuẩn Việt Nam: Ngày/Tháng/Năm
    private static final SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");

    public static String format(Date date) {
        if (date == null) {
            return ""; // Nếu không có ngày thì để trống, tránh lỗi NullPointerException
        }
        return sdf.format(date);
    }
}