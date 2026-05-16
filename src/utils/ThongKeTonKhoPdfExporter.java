package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

import com.itextpdf.text.BaseColor;
import com.itextpdf.text.Chunk;
import com.itextpdf.text.Document;
import com.itextpdf.text.Element;
import com.itextpdf.text.Font;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.Paragraph;
import com.itextpdf.text.Phrase;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;

public class ThongKeTonKhoPdfExporter {

    private static final DecimalFormat DF = new DecimalFormat("#,##0");
    private static final DateTimeFormatter SHOW_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private static Font getFont(float size, int style) {
        return getFont(size, style, null);
    }

    private static Font getFont(float size, int style, BaseColor color) {
        try {
            String[] fontPaths = {
                    "C:/Windows/Fonts/arial.ttf",
                    "C:/Windows/Fonts/times.ttf",
                    "/usr/share/fonts/truetype/freefont/FreeSerif.ttf"
            };
            for (String path : fontPaths) {
                if (new File(path).exists()) {
                    BaseFont bf = BaseFont.createFont(path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                    Font f = new Font(bf, size, style);
                    if (color != null) f.setColor(color);
                    return f;
                }
            }
        } catch (Exception ignored) {
        }
        Font f = new Font(Font.FontFamily.HELVETICA, size, style);
        if (color != null) f.setColor(color);
        return f;
    }

    static class PageNumberEvent extends PdfPageEventHelper {
        Font fPage = getFont(8, Font.ITALIC, BaseColor.GRAY);

        @Override
        public void onEndPage(PdfWriter writer, Document doc) {
            PdfContentByte cb = writer.getDirectContent();
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase("Trang " + writer.getPageNumber(), fPage), doc.right(), doc.bottom() - 10, 0);
        }
    }

    public static String xuatPDF(
            LocalDate tuNgay,
            LocalDate denNgay,
            String danhMuc,
            String trangThaiTon,
            Map<String, Object> tongQuan,
            List<Map<String, Object>> tonKhoTheoDanhMuc,
            List<Map<String, Object>> bienDongTonKho,
            List<Map<String, Object>> topTonKho,
            List<Map<String, Object>> loSapHetHan) throws Exception {

        Path dir = Paths.get("exports/thongke");
        Files.createDirectories(dir);
        String filePath = dir.resolve("ThongKeTonKho_" + LocalDateTime.now().format(FILE_TIME) + ".pdf").toString();

        Document doc = new Document(PageSize.A4, 36, 36, 42, 36);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(filePath));
        writer.setPageEvent(new PageNumberEvent());
        doc.open();

        Font fTitle = getFont(18, Font.BOLD);
        Font fSection = getFont(12, Font.BOLD, new BaseColor(14, 165, 233));
        Font fHead = getFont(9, Font.BOLD, BaseColor.WHITE);
        Font fNormal = getFont(9, Font.NORMAL);
        Font fBold = getFont(9, Font.BOLD);
        Font fSmall = getFont(8, Font.ITALIC, BaseColor.DARK_GRAY);

        Paragraph shopName = new Paragraph("NHÀ THUỐC LONG NGUYỄN", fTitle);
        shopName.setAlignment(Element.ALIGN_CENTER);
        doc.add(shopName);

        Paragraph title = new Paragraph("BÁO CÁO THỐNG KÊ TỒN KHO", fSection);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(8);
        title.setSpacingAfter(8);
        doc.add(title);

        DottedLineSeparator separator = new DottedLineSeparator();
        separator.setGap(3);
        doc.add(new Chunk(separator));

        Paragraph meta = new Paragraph(
                "Biến động từ " + tuNgay.format(SHOW_DATE) + " đến " + denNgay.format(SHOW_DATE)
                        + " | Danh mục: " + display(danhMuc)
                        + " | Trạng thái tồn: " + display(trangThaiTon),
                fSmall);
        meta.setAlignment(Element.ALIGN_CENTER);
        meta.setSpacingBefore(8);
        meta.setSpacingAfter(12);
        doc.add(meta);

        addSection(doc, "Tổng quan", fSection);
        PdfPTable kpi = new PdfPTable(new float[]{3, 2, 3, 2});
        kpi.setWidthPercentage(100);
        addPair(kpi, "Tổng mặt hàng", formatInt(tongQuan.get("tongMatHang")), fNormal, fBold);
        addPair(kpi, "Tổng lô còn hàng", formatInt(tongQuan.get("tongLoConHang")), fNormal, fBold);
        addPair(kpi, "Tổng số lượng tồn", formatInt(tongQuan.get("tongSoLuongTon")), fNormal, fBold);
        addPair(kpi, "Tổng giá trị tồn", formatMoney(tongQuan.get("tongGiaTriTon")) + " đ", fNormal, fBold);
        addPair(kpi, "Mặt hàng tồn thấp", formatInt(tongQuan.get("soMatHangTonThap")), fNormal, fBold);
        addPair(kpi, "Lô sắp hết hạn", formatInt(tongQuan.get("soLoSapHetHan")), fNormal, fBold);
        doc.add(kpi);

        addSimpleTable(doc, "Tồn kho theo danh mục", new String[]{"Danh mục", "Số lượng", "Giá trị"},
                tonKhoTheoDanhMuc, fSection, fHead, fNormal,
                row -> new String[]{str(row.get("tenDanhMuc")), formatInt(row.get("soLuongTon")), formatMoney(row.get("giaTriTon")) + " đ"});

        addSimpleTable(doc, "Biến động nhập/xuất", new String[]{"Ngày", "Nhập", "Xuất"},
                bienDongTonKho, fSection, fHead, fNormal,
                row -> new String[]{str(row.get("ngay")), formatInt(row.get("soLuongNhap")), formatInt(row.get("soLuongXuat"))});

        addSimpleTable(doc, "Top tồn kho", new String[]{"STT", "Mã", "Tên thuốc", "Tồn", "ĐVT", "Giá trị"},
                topTonKho, fSection, fHead, fNormal,
                row -> new String[]{str(row.get("stt")), str(row.get("maThuoc")), str(row.get("tenThuoc")),
                        formatInt(row.get("soLuongTon")), str(row.get("donViCoBan")), formatMoney(row.get("giaTriTon")) + " đ"});

        addSimpleTable(doc, "Lô sắp hết hạn", new String[]{"STT", "Mã lô", "Tên thuốc", "Hạn dùng", "Còn lại", "Tồn"},
                loSapHetHan, fSection, fHead, fNormal,
                row -> new String[]{str(row.get("stt")), str(row.get("maLoThuoc")), str(row.get("tenThuoc")),
                        str(row.get("hanSuDung")), formatInt(row.get("soNgayConLai")) + " ngày", formatInt(row.get("soLuongTon"))});

        doc.close();
        return filePath;
    }

    private interface RowMapper {
        String[] map(Map<String, Object> row);
    }

    private static void addSection(Document doc, String title, Font font) throws Exception {
        Paragraph p = new Paragraph(title, font);
        p.setSpacingBefore(12);
        p.setSpacingAfter(6);
        doc.add(p);
    }

    private static void addPair(PdfPTable table, String label, String value, Font fNormal, Font fBold) {
        table.addCell(cell(label, Element.ALIGN_LEFT, fNormal, BaseColor.WHITE));
        table.addCell(cell(value, Element.ALIGN_RIGHT, fBold, BaseColor.WHITE));
    }

    private static void addSimpleTable(Document doc, String title, String[] headers, List<Map<String, Object>> data,
                                       Font fSection, Font fHead, Font fNormal, RowMapper mapper) throws Exception {
        addSection(doc, title, fSection);
        PdfPTable table = new PdfPTable(headers.length);
        table.setWidthPercentage(100);
        for (String header : headers) {
            table.addCell(cell(header, Element.ALIGN_CENTER, fHead, new BaseColor(14, 165, 233)));
        }
        if (data == null || data.isEmpty()) {
            PdfPCell empty = cell("Không có dữ liệu", Element.ALIGN_CENTER, fNormal, BaseColor.WHITE);
            empty.setColspan(headers.length);
            table.addCell(empty);
        } else {
            for (Map<String, Object> row : data) {
                for (String value : mapper.map(row)) {
                    table.addCell(cell(value, Element.ALIGN_LEFT, fNormal, BaseColor.WHITE));
                }
            }
        }
        doc.add(table);
    }

    private static PdfPCell cell(String text, int align, Font font, BaseColor bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text == null ? "" : text, font));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(5);
        cell.setBackgroundColor(bg);
        return cell;
    }

    private static String display(String value) {
        return value == null || value.trim().isEmpty() ? "Tất cả" : value;
    }

    private static String str(Object value) {
        return value == null ? "" : String.valueOf(value);
    }

    private static String formatInt(Object value) {
        return DF.format(toDouble(value));
    }

    private static String formatMoney(Object value) {
        return DF.format(toDouble(value));
    }

    private static double toDouble(Object value) {
        if (value instanceof Number) return ((Number) value).doubleValue();
        if (value == null) return 0;
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
