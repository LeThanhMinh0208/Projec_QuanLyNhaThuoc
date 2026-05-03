package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.DecimalFormat;
import java.time.LocalDate;
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
import com.itextpdf.text.Rectangle;
import com.itextpdf.text.pdf.BaseFont;
import com.itextpdf.text.pdf.ColumnText;
import com.itextpdf.text.pdf.PdfContentByte;
import com.itextpdf.text.pdf.PdfPCell;
import com.itextpdf.text.pdf.PdfPTable;
import com.itextpdf.text.pdf.PdfPageEventHelper;
import com.itextpdf.text.pdf.PdfWriter;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;

public class ThongKeDoanhThuPdfExporter {

    private static DecimalFormat dfCurrency = new DecimalFormat("#,##0");

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
            String text = "Trang " + writer.getPageNumber();
            Phrase phrase = new Phrase(text, fPage);
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT, phrase, doc.right(), doc.bottom() - 10, 0);
        }
    }

    /**
     * Xuất báo cáo thống kê doanh thu ra PDF
     */
    public static String xuatPDF(
            LocalDate tuNgay,
            LocalDate denNgay,
            String loaiBan,
            String hinhThuc,
            double tongDoanhThu,
            int tongDon,
            double giaTrungBinh,
            int soKhachHang,
            List<Map<String, Object>> doanhThuTheoNgay,
            List<Map<String, Object>> doanhThuTheoNhom,
            List<Map<String, Object>> topKhachHang,
            List<Map<String, Object>> productDead,
            List<Map<String, Object>> thongKeHinhThuc) throws Exception {

        Path dir = Paths.get("exports/thongke");
        Files.createDirectories(dir);

        DateTimeFormatter dtfFile = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
        DateTimeFormatter dtfShow = DateTimeFormatter.ofPattern("dd/MM/yyyy");

        String timeStr = java.time.LocalDateTime.now().format(dtfFile);
        String fileName = "ThongKeDoanhThu_" + timeStr + ".pdf";
        String filePath = dir.resolve(fileName).toString();

        Document doc = new Document(PageSize.A4, 40, 40, 50, 40);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(filePath));
        PageNumberEvent pageEvent = new PageNumberEvent();
        writer.setPageEvent(pageEvent);
        doc.open();

        Font fTitleXl = getFont(20, Font.BOLD);
        Font fTitle = getFont(14, Font.BOLD);
        Font fSubTitle = getFont(11, Font.BOLD);
        Font fSub = getFont(9, Font.ITALIC, BaseColor.DARK_GRAY);
        Font fHead = getFont(10, Font.BOLD, BaseColor.WHITE);
        Font fNormal = getFont(10, Font.NORMAL);
        Font fBold = getFont(10, Font.BOLD);

        // Header
        Paragraph shopName = new Paragraph("NHÀ THUỐC LONG NGUYÊN", fTitleXl);
        shopName.setAlignment(Element.ALIGN_CENTER);
        doc.add(shopName);

        Paragraph shopAddr = new Paragraph("Đ/c: 12 Nguyễn Văn Bảo, P.Hạnh Thông, TP.HCM | Hotline: 0123.456.789", fSub);
        shopAddr.setAlignment(Element.ALIGN_CENTER);
        shopAddr.setSpacingAfter(12);
        doc.add(shopAddr);

        DottedLineSeparator separator = new DottedLineSeparator();
        separator.setGap(3);
        doc.add(new Chunk(separator));

        // Title
        Paragraph title = new Paragraph("BÁO CÁO THỐNG KÊ DOANH THU", fTitle);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(15);
        title.setSpacingAfter(15);
        doc.add(title);

        // Ngày tháng
        String dateRange = String.format("Từ ngày %s đến %s", tuNgay.format(dtfShow), denNgay.format(dtfShow));
        Paragraph dateP = new Paragraph(dateRange, fBold);
        dateP.setAlignment(Element.ALIGN_CENTER);
        dateP.setSpacingAfter(12);
        doc.add(dateP);

        // Filter info
        PdfPTable filterTable = new PdfPTable(2);
        filterTable.setWidthPercentage(100);
        filterTable.setSpacingAfter(15);

        addInfoRow(filterTable, "Loại bán", mapLoaiBanDisplay(loaiBan), fNormal);
        addInfoRow(filterTable, "Hình thức thanh toán", mapHinhThucDisplay(hinhThuc), fNormal);
        doc.add(filterTable);

        // KPI Summary
        Paragraph kpiTitle = new Paragraph("TỔNG HỢP THỐNG KÊ CHÍNH", fSubTitle);
        kpiTitle.setSpacingBefore(10);
        kpiTitle.setSpacingAfter(10);
        doc.add(kpiTitle);

        PdfPTable kpiTable = new PdfPTable(2);
        kpiTable.setWidthPercentage(100);
        kpiTable.setSpacingAfter(20);

        addKPIRow(kpiTable, "Tổng doanh thu", dfCurrency.format(tongDoanhThu) + " ₫", fNormal, fBold);
        addKPIRow(kpiTable, "Tổng số đơn", tongDon + " đơn", fNormal, fBold);
        addKPIRow(kpiTable, "Giá trị trung bình/đơn", dfCurrency.format(giaTrungBinh) + " ₫", fNormal, fBold);
        addKPIRow(kpiTable, "Số khách hàng", soKhachHang + " khách", fNormal, fBold);
        doc.add(kpiTable);

        if (doanhThuTheoNhom != null && !doanhThuTheoNhom.isEmpty()) {
            Paragraph nhomTitle = new Paragraph("DOANH THU THEO NHÓM THUỐC", fSubTitle);
            nhomTitle.setSpacingBefore(10);
            nhomTitle.setSpacingAfter(10);
            doc.add(nhomTitle);

            PdfPTable nhomTable = new PdfPTable(3);
            nhomTable.setWidthPercentage(100);
            nhomTable.setWidths(new float[]{4f, 2.2f, 1.3f});
            nhomTable.setSpacingAfter(18);

            BaseColor headerBg = new BaseColor(41, 128, 185);
            String[] headers = {"Nhóm thuốc", "Doanh thu", "Tỷ lệ"};
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fHead));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setBackgroundColor(headerBg);
                cell.setPadding(6);
                nhomTable.addCell(cell);
            }

            for (Map<String, Object> row : doanhThuTheoNhom) {
                String nhomThuoc = (String) row.get("nhomThuoc");
                double doanhThu = toDouble(row.get("doanhThu"));
                nhomTable.addCell(makeCell(nhomThuoc, Element.ALIGN_LEFT, fNormal));
                nhomTable.addCell(makeCell(dfCurrency.format(doanhThu) + " ₫", Element.ALIGN_RIGHT, fNormal));
                nhomTable.addCell(makeCell(formatPercent(doanhThu, tongDoanhThu), Element.ALIGN_CENTER, fNormal));
            }
            doc.add(nhomTable);
        }

        // Thống kê theo hình thức thanh toán
        if (thongKeHinhThuc != null && !thongKeHinhThuc.isEmpty()) {
            Paragraph hinhThucTitle = new Paragraph("THỐNG KÊ THEO HÌNH THỨC THANH TOÁN", fSubTitle);
            hinhThucTitle.setSpacingBefore(15);
            hinhThucTitle.setSpacingAfter(10);
            doc.add(hinhThucTitle);

            PdfPTable hinhThucTable = new PdfPTable(2);
            hinhThucTable.setWidthPercentage(100);
            hinhThucTable.setWidths(new float[]{3f, 2f});
            hinhThucTable.setSpacingAfter(20);

            // Header
            BaseColor headerBg = new BaseColor(41, 128, 185);
            String[] hHeaders = {"Hình thức thanh toán", "Số đơn"};
            for (String h : hHeaders) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fHead));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setBackgroundColor(headerBg);
                cell.setPadding(6);
                hinhThucTable.addCell(cell);
            }

            for (Map<String, Object> row : thongKeHinhThuc) {
                String hinhThucTT = (String) row.get("hinhThuc");
                int soDon = (Integer) row.get("soDon");

                addTableCell(hinhThucTable, mapHinhThucDisplay(hinhThucTT), Element.ALIGN_LEFT, fNormal);
                addTableCell(hinhThucTable, String.valueOf(soDon), Element.ALIGN_CENTER, fNormal);
            }
            doc.add(hinhThucTable);
        }
        if (topKhachHang != null && !topKhachHang.isEmpty()) {
            Paragraph topKHTitle = new Paragraph("TOP 5 KHÁCH HÀNG MUA NHIỀU NHẤT", fSubTitle);
            topKHTitle.setSpacingBefore(15);
            topKHTitle.setSpacingAfter(10);
            doc.add(topKHTitle);

            PdfPTable topKHTable = new PdfPTable(4);
            topKHTable.setWidthPercentage(100);
            topKHTable.setWidths(new float[]{0.8f, 4f, 1.5f, 2.5f});
            topKHTable.setHeaderRows(1);
            topKHTable.setSpacingAfter(15);

            // Header
            String[] headers = {"STT", "Tên khách hàng", "Số đơn", "Tổng doanh thu"};
            BaseColor headerBg = new BaseColor(41, 128, 185);
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fHead));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setBackgroundColor(headerBg);
                cell.setPadding(6);
                topKHTable.addCell(cell);
            }

            int stt = 1;
            for (Map<String, Object> row : topKhachHang) {
                String tenKH = (String) row.get("tenKhachHang");
                int soDon = (Integer) row.get("soDon");
                double doanhThu = (Double) row.get("doanhThu");

                addTableCell(topKHTable, String.valueOf(stt++), Element.ALIGN_CENTER, fNormal);
                addTableCell(topKHTable, tenKH, Element.ALIGN_LEFT, fNormal);
                addTableCell(topKHTable, String.valueOf(soDon), Element.ALIGN_CENTER, fNormal);
                addTableCell(topKHTable, dfCurrency.format(doanhThu) + " ₫", Element.ALIGN_RIGHT, fNormal);
            }
            doc.add(topKHTable);
        }

        // Dead Products
        if (productDead != null && !productDead.isEmpty()) {
            Paragraph deadTitle = new Paragraph("SẢN PHẨM KHÔNG CHUYỂN ĐỘNG (CẢNH BÁO)", fSubTitle);
            deadTitle.setSpacingBefore(15);
            deadTitle.setSpacingAfter(10);
            doc.add(deadTitle);

            PdfPTable deadTable = new PdfPTable(5);
            deadTable.setWidthPercentage(100);
            deadTable.setWidths(new float[]{0.8f, 3f, 1.5f, 1.8f, 1.2f});
            deadTable.setHeaderRows(1);
            deadTable.setSpacingAfter(15);

            // Header
            String[] headers = {"STT", "Tên sản phẩm", "Nhóm", "Số ngày không bán", "Tồn kho"};
            BaseColor headerBg = new BaseColor(41, 128, 185);
            for (String h : headers) {
                PdfPCell cell = new PdfPCell(new Phrase(h, fHead));
                cell.setHorizontalAlignment(Element.ALIGN_CENTER);
                cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                cell.setBackgroundColor(headerBg);
                cell.setPadding(6);
                deadTable.addCell(cell);
            }

            int stt = 1;
            for (Map<String, Object> row : productDead) {
                String tenThuoc = (String) row.get("tenThuoc");
                String nhom = (String) row.get("nhomThuoc");
                int soNgayKhongBan = (Integer) row.get("soNgayKhongBan");
                int tonKho = (Integer) row.get("tonKho");

                addTableCell(deadTable, String.valueOf(stt++), Element.ALIGN_CENTER, fNormal);
                addTableCell(deadTable, tenThuoc, Element.ALIGN_LEFT, fNormal);
                addTableCell(deadTable, nhom, Element.ALIGN_CENTER, fNormal);
                addTableCell(deadTable, String.valueOf(soNgayKhongBan), Element.ALIGN_CENTER, fNormal);
                addTableCell(deadTable, String.valueOf(tonKho), Element.ALIGN_RIGHT, fNormal);
            }
            doc.add(deadTable);
        }

        // Footer
        doc.add(new Chunk(separator));
        Paragraph footer = new Paragraph(
                "Báo cáo này được tạo tự động. Cảm ơn quý khách!",
                fSub
        );
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(12);
        doc.add(footer);

        doc.close();
        return filePath;
    }

    private static void addInfoRow(PdfPTable t, String label, String value, Font f) {
        PdfPCell lbl = new PdfPCell(new Phrase(label + ":", f));
        lbl.setBackgroundColor(new BaseColor(245, 248, 250));
        lbl.setPaddingLeft(6);
        lbl.setBorder(Rectangle.NO_BORDER);
        lbl.setHorizontalAlignment(Element.ALIGN_LEFT);

        PdfPCell val = new PdfPCell(new Phrase(value != null ? value : "—", f));
        val.setBorder(Rectangle.NO_BORDER);
        val.setHorizontalAlignment(Element.ALIGN_LEFT);

        t.addCell(lbl);
        t.addCell(val);
    }

    private static void addKPIRow(PdfPTable t, String label, String value, Font fLabel, Font fValue) {
        PdfPCell lbl = new PdfPCell(new Phrase(label, fLabel));
        lbl.setBackgroundColor(new BaseColor(245, 248, 250));
        lbl.setBorder(Rectangle.NO_BORDER);
        lbl.setHorizontalAlignment(Element.ALIGN_LEFT);
        lbl.setPadding(8);

        PdfPCell val = new PdfPCell(new Phrase(value, fValue));
        val.setBorder(Rectangle.NO_BORDER);
        val.setHorizontalAlignment(Element.ALIGN_LEFT);
        val.setPadding(8);

        t.addCell(lbl);
        t.addCell(val);
    }

    private static void addTableCell(PdfPTable t, String text, int align, Font f) {
        PdfPCell cell = makeCell(text, align, f);
        cell.setBackgroundColor(new BaseColor(250, 250, 250));
        cell.setBorderColor(new BaseColor(220, 220, 220));
        cell.setBorderWidth(0.5f);
        t.addCell(cell);
    }

    private static PdfPCell makeCell(String text, int align, Font f) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", f));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(new BaseColor(250, 250, 250));
        cell.setBorderColor(new BaseColor(220, 220, 220));
        cell.setBorderWidth(0.5f);
        cell.setPadding(5);
        return cell;
    }

    private static double toDouble(Object value) {
        if (value instanceof Number) {
            return ((Number) value).doubleValue();
        }
        if (value == null) {
            return 0;
        }
        try {
            return Double.parseDouble(String.valueOf(value));
        } catch (NumberFormatException ex) {
            return 0;
        }
    }

    private static String formatPercent(double amount, double total) {
        if (total <= 0) {
            return "0.0%";
        }
        return String.format("%.1f%%", (amount / total) * 100.0);
    }

    private static String mapLoaiBanDisplay(String loaiBan) {
        if (loaiBan == null || loaiBan.isEmpty()) return "Tất cả";
        if ("BAN_THEO_DON".equalsIgnoreCase(loaiBan)) return "Bán theo đơn";
        if ("BAN_LE".equalsIgnoreCase(loaiBan)) return "Bán lẻ";
        return loaiBan;
    }

    private static String mapHinhThucDisplay(String hinhThuc) {
        if (hinhThuc == null || hinhThuc.isEmpty()) return "Tất cả";
        if ("TIEN_MAT".equalsIgnoreCase(hinhThuc)) return "Tiền mặt";
        if ("CHUYEN_KHOAN".equalsIgnoreCase(hinhThuc)) return "Chuyển khoản";
        if ("THE".equalsIgnoreCase(hinhThuc)) return "Thẻ";
        return hinhThuc;
    }
}
