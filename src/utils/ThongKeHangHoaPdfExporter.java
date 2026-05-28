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
import java.util.ArrayList;
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

public class ThongKeHangHoaPdfExporter {

    private static final DecimalFormat DF = new DecimalFormat("#,##0");
    private static final DateTimeFormatter SHOW_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FILE_TIME  = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    private static final BaseColor COLOR_HEADER  = new BaseColor(14, 165, 233);
    private static final BaseColor COLOR_ROW_ALT = new BaseColor(248, 250, 252);

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
        } catch (Exception ignored) {}
        Font f = new Font(Font.FontFamily.HELVETICA, size, style);
        if (color != null) f.setColor(color);
        return f;
    }

    static class PageNumberEvent extends PdfPageEventHelper {
        private final Font fPage = getFont(8, Font.ITALIC, BaseColor.GRAY);
        @Override
        public void onEndPage(PdfWriter writer, Document doc) {
            PdfContentByte cb = writer.getDirectContent();
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    new Phrase("Trang " + writer.getPageNumber(), fPage),
                    doc.right(), doc.bottom() - 10, 0);
        }
    }

    // ── Signature giữ nguyên 6 tham số như Controller cũ ──────────────
    public static String xuatPDF(
            LocalDate tuNgay,
            LocalDate denNgay,
            String danhMuc,
            Map<String, Object> tongQuan,
            List<Map<String, Object>> coCauDoanhThu,
            List<Map<String, Object>> topSanPham,
            List<Map<String, Object>> chamLC,
            List<Map<String, Object>> doiTra) throws Exception {

        Path dir = Paths.get("exports/thongke");
        Files.createDirectories(dir);
        String filePath = dir.resolve("ThongKeHangHoa_" + LocalDateTime.now().format(FILE_TIME) + ".pdf").toString();

        Document doc = new Document(PageSize.A4, 36, 36, 42, 36);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(filePath));
        writer.setPageEvent(new PageNumberEvent());
        doc.open();

        Font fShopName    = getFont(18, Font.BOLD,   null);
        Font fReportTitle = getFont(13, Font.BOLD,   COLOR_HEADER);
        Font fSection     = getFont(11, Font.BOLD,   COLOR_HEADER);
        Font fHead        = getFont(9,  Font.BOLD,   BaseColor.WHITE);
        Font fLabel       = getFont(9,  Font.BOLD,   null);
        Font fNormal      = getFont(9,  Font.NORMAL, null);
        Font fBold        = getFont(9,  Font.BOLD,   null);
        Font fSmall       = getFont(8,  Font.ITALIC, BaseColor.DARK_GRAY);

        // ── Header ──────────────────────────────────────────────────────
        Paragraph shopName = new Paragraph("NHÀ THUỐC LONG NGUYỄN", fShopName);
        shopName.setAlignment(Element.ALIGN_CENTER);
        doc.add(shopName);

        Paragraph reportTitle = new Paragraph("BÁO CÁO THỐNG KÊ BIẾN ĐỘNG HÀNG HÓA", fReportTitle);
        reportTitle.setAlignment(Element.ALIGN_CENTER);
        reportTitle.setSpacingBefore(6);
        reportTitle.setSpacingAfter(6);
        doc.add(reportTitle);

        doc.add(new Chunk(new DottedLineSeparator()));

        String danhMucDisplay = (danhMuc == null || danhMuc.trim().isEmpty()
                || "Tất cả".equalsIgnoreCase(danhMuc.trim())) ? "Tất cả" : danhMuc;
        Paragraph meta = new Paragraph(
                "Chu kỳ báo cáo: từ " + tuNgay.format(SHOW_DATE)
                + " đến " + denNgay.format(SHOW_DATE)
                + "   |   Danh mục: " + danhMucDisplay
                + "   |   Ngày xuất: " + LocalDate.now().format(SHOW_DATE), fSmall);
        meta.setAlignment(Element.ALIGN_CENTER);
        meta.setSpacingBefore(6);
        meta.setSpacingAfter(14);
        doc.add(meta);

        // ── I. KPI Tổng Quan (4 thẻ card) ───────────────────────────────
        addTitle(doc, "I. Chỉ tiêu tổng quan biến động hàng hóa", fSection);
        PdfPTable kpiTable = new PdfPTable(new float[]{3.5f, 2f, 3.5f, 2f});
        kpiTable.setWidthPercentage(100);
        kpiTable.setSpacingAfter(10);
        addKpiPair(kpiTable, "Mặt Hàng Đã Bán",
                formatInt(tongQuan.get("tongMatHangDaBan")) + " sản phẩm", fLabel, fBold);
        addKpiPair(kpiTable, "Tổng Số Lượng Bán",
                formatInt(tongQuan.get("tongSoLuongBan")), fLabel, fBold);
        addKpiPair(kpiTable, "Doanh Thu Gốc",
                formatMoney(tongQuan.get("tongDoanhThuGoc")) + " đ", fLabel, fBold);
        addKpiPair(kpiTable, "Doanh Thu Sau Thuế",
                formatMoney(tongQuan.get("tongDoanhThuSauThue")) + " đ", fLabel, fBold);
        doc.add(kpiTable);

        // ── II. Cơ Cấu Số Lượng Tiêu Thụ Theo Nhóm (PieChart mới) ─────────
        addSimpleTable(doc,
                "II. Cơ cấu số lượng tiêu thụ theo nhóm danh mục ",
                new String[]{"Nhóm Danh Mục Sản Phẩm", "Số Lượng Tiêu Thụ (sản phẩm)"},
                coCauDoanhThu, fSection, fHead, fNormal,
                row -> new String[]{
                        str(row.get("tenDanhMuc")),
                        formatInt(row.get("soLuong"))
                });

        // ── III. Top 10 Sản Phẩm Bán Chạy Nhất (BarChart + TableView 1) ─
        addSimpleTable(doc,
                "III. Top 10 sản phẩm bán chạy nhất - theo số lượng ",
                new String[]{"STT", "Mã SP", "Tên Sản Phẩm", "Nhóm", "ĐVT", "SL Bán", "Doanh Thu (đ)"},
                topSanPham, fSection, fHead, fNormal,
                row -> new String[]{
                        str(row.get("stt")),
                        str(row.get("maThuoc")),
                        str(row.get("tenThuoc")),
                        str(row.get("tenDanhMuc")),
                        str(row.get("donVi")),
                        formatInt(row.get("soLuongBan")),
                        formatMoney(row.get("doanhThu")) + " đ"
                });

        // ── IV. Sản Phẩm Doanh Thu Cao Nhất (TableView 2) ───────────────
        List<Map<String, Object>> topDoanhThu = new ArrayList<>();
        if (topSanPham != null) {
            topDoanhThu.addAll(topSanPham);
            topDoanhThu.sort((a, b) -> Double.compare(toDouble(b.get("doanhThu")), toDouble(a.get("doanhThu"))));
        }
        final int[] stt = {1};
        addSimpleTable(doc,
                "IV. Sản phẩm doanh thu cao nhất ",
                new String[]{"STT", "Mã SP", "Tên Sản Phẩm", "Nhóm", "ĐVT", "SL Bán", "Doanh Thu (đ)"},
                topDoanhThu, fSection, fHead, fNormal,
                row -> new String[]{
                        String.valueOf(stt[0]++),
                        str(row.get("maThuoc")),
                        str(row.get("tenThuoc")),
                        str(row.get("tenDanhMuc")),
                        str(row.get("donVi")),
                        formatInt(row.get("soLuongBan")),
                        formatMoney(row.get("doanhThu")) + " đ"
                });

        // ── V. Top 10 Sản Phẩm Bán Ít Nhất ──────────────────────────────
        addSimpleTable(doc,
                "V. Top 10 sản phẩm bán ít nhất ",
                new String[]{"STT", "Mã SP", "Tên Sản Phẩm", "Nhóm", "ĐVT", "SL Bán"},
                chamLC, fSection, fHead, fNormal,
                row -> new String[]{
                        str(row.get("stt")),
                        str(row.get("maThuoc")),
                        str(row.get("tenThuoc")),
                        str(row.get("tenDanhMuc")),
                        str(row.get("donVi")),
                        formatInt(row.get("soLuongBan"))
                });

        // ── VI. Top 10 Sản Phẩm Đổi/Trả Nhiều Nhất ──────────────────────
        addSimpleTable(doc,
                "VI. Top 10 sản phẩm đổi/trả nhiều nhất ",
                new String[]{"STT", "Mã SP", "Tên Sản Phẩm", "Nhóm", "ĐVT", "SL Đổi/Trả"},
                doiTra, fSection, fHead, fNormal,
                row -> new String[]{
                        str(row.get("stt")),
                        str(row.get("maThuoc")),
                        str(row.get("tenThuoc")),
                        str(row.get("tenDanhMuc")),
                        str(row.get("donVi")),
                        formatInt(row.get("soLuongDoiTra"))
                });

        doc.close();
        return filePath;
    }

    @FunctionalInterface
    private interface RowMapper { String[] map(Map<String, Object> row); }

    private static void addTitle(Document doc, String title, Font font) throws Exception {
        Paragraph p = new Paragraph(title, font);
        p.setSpacingBefore(12);
        p.setSpacingAfter(6);
        doc.add(p);
    }

    private static void addKpiPair(PdfPTable table, String label, String value,
                                    Font fLabel, Font fBold) {
        PdfPCell cLabel = cell(label, Element.ALIGN_LEFT, fLabel, COLOR_ROW_ALT);
        PdfPCell cValue = cell(value, Element.ALIGN_RIGHT, fBold, BaseColor.WHITE);
        table.addCell(cLabel);
        table.addCell(cValue);
    }

    private static void addSimpleTable(Document doc, String title, String[] headers,
                                        List<Map<String, Object>> data,
                                        Font fSection, Font fHead, Font fNormal,
                                        RowMapper mapper) throws Exception {
        addTitle(doc, title, fSection);
        PdfPTable table = new PdfPTable(headers.length);
        table.setWidthPercentage(100);
        table.setSpacingAfter(8);
        for (String h : headers)
            table.addCell(cell(h, Element.ALIGN_CENTER, fHead, COLOR_HEADER));

        if (data == null || data.isEmpty()) {
            PdfPCell empty = cell("Chưa có dữ liệu phát sinh trong khoảng thời gian này",
                    Element.ALIGN_CENTER, fNormal, BaseColor.WHITE);
            empty.setColspan(headers.length);
            table.addCell(empty);
        } else {
            boolean alt = false;
            for (Map<String, Object> row : data) {
                BaseColor bg = alt ? COLOR_ROW_ALT : BaseColor.WHITE;
                for (String val : mapper.map(row))
                    table.addCell(cell(val, Element.ALIGN_LEFT, fNormal, bg));
                alt = !alt;
            }
        }
        doc.add(table);
    }

    private static PdfPCell cell(String text, int align, Font font, BaseColor bg) {
        PdfPCell c = new PdfPCell(new Phrase(text == null ? "" : text, font));
        c.setHorizontalAlignment(align);
        c.setVerticalAlignment(Element.ALIGN_MIDDLE);
        c.setPadding(5);
        c.setBackgroundColor(bg);
        c.setBorderColor(new BaseColor(226, 232, 240));
        return c;
    }

    private static String str(Object v) { return v == null ? "" : String.valueOf(v); }
    private static String formatInt(Object v)   { return DF.format(toDouble(v)); }
    private static String formatMoney(Object v) { return DF.format(toDouble(v)); }
    private static double toDouble(Object v) {
        if (v instanceof Number) return ((Number) v).doubleValue();
        if (v == null) return 0;
        try { return Double.parseDouble(String.valueOf(v)); } catch (NumberFormatException e) { return 0; }
    }
}