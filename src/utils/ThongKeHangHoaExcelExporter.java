package utils;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ThongKeHangHoaExcelExporter {

    private static final DateTimeFormatter SHOW_DATE = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter FILE_TIME = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");
    private static final java.text.DecimalFormat DF = new java.text.DecimalFormat("#,##0");

    private static final class CellValue {
        final String value;
        final int style;
        CellValue(String value, int style) {
            this.value = value;
            this.style = style;
        }
    }

    // ── Signature giữ nguyên 6 tham số như Controller cũ ──────────────
    public static String xuatExcel(
            LocalDate tuNgay,
            LocalDate denNgay,
            String danhMuc,
            Map<String, Object> tongQuan,
            List<Map<String, Object>> coCauDoanhThu,
            List<Map<String, Object>> topSanPham) throws Exception {

        Path dir = Paths.get("exports/thongke");
        Files.createDirectories(dir);

        Path filePath = dir.resolve("ThongKeHangHoa_" + LocalDateTime.now().format(FILE_TIME) + ".xlsx");
        try (OutputStream os = Files.newOutputStream(filePath);
             ZipOutputStream zos = new ZipOutputStream(os)) {
            writeEntry(zos, "[Content_Types].xml", contentTypes());
            writeEntry(zos, "_rels/.rels", rootRels());
            writeEntry(zos, "xl/workbook.xml", workbook());
            writeEntry(zos, "xl/_rels/workbook.xml.rels", workbookRels());
            writeEntry(zos, "xl/styles.xml", styles());
            writeEntry(zos, "xl/worksheets/sheet1.xml",
                    sheet(tuNgay, denNgay, danhMuc, tongQuan, coCauDoanhThu, topSanPham));
        }
        return filePath.toString();
    }

    private static void writeEntry(ZipOutputStream zos, String name, String content) throws Exception {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private static String contentTypes() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
                "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>" +
                "<Default Extension=\"xml\" ContentType=\"application/xml\"/>" +
                "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>" +
                "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>" +
                "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>" +
                "</Types>";
    }

    private static String rootRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
                "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>" +
                "</Relationships>";
    }

    private static String workbook() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">" +
                "<sheets><sheet name=\"ThongKeHangHoa\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>";
    }

    private static String workbookRels() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
                "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>" +
                "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>" +
                "</Relationships>";
    }

    private static String styles() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">" +
                "<fonts count=\"5\"><font><sz val=\"11\"/><name val=\"Calibri\"/></font>" +
                "<font><b/><sz val=\"16\"/><color rgb=\"FF0EA5E9\"/><name val=\"Calibri\"/></font>" +
                "<font><i/><sz val=\"10\"/><color rgb=\"FF64748B\"/><name val=\"Calibri\"/></font>" +
                "<font><b/><sz val=\"10\"/><color rgb=\"FFFFFFFF\"/><name val=\"Calibri\"/></font>" +
                "<font><b/><sz val=\"10\"/><color rgb=\"FF1E293B\"/><name val=\"Calibri\"/></font></fonts>" +
                "<fills count=\"4\"><fill><patternFill patternType=\"none\"/></fill><fill><patternFill patternType=\"gray125\"/></fill>" +
                "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFF8FAFC\"/></patternFill></fill>" +
                "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF0EA5E9\"/></patternFill></fill></fills>" +
                "<borders count=\"2\"><border/><border><left style=\"thin\"><color rgb=\"FFE2E8F0\"/></left><right style=\"thin\"><color rgb=\"FFE2E8F0\"/></right><top style=\"thin\"><color rgb=\"FFE2E8F0\"/></top><bottom style=\"thin\"><color rgb=\"FFE2E8F0\"/></bottom></border></borders>" +
                "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>" +
                "<cellXfs count=\"7\"><xf fontId=\"1\" fillId=\"0\" borderId=\"0\"><alignment horizontal=\"center\"/></xf>" +
                "<xf fontId=\"2\" fillId=\"0\" borderId=\"0\"><alignment horizontal=\"center\"/></xf>" +
                "<xf fontId=\"3\" fillId=\"3\" borderId=\"1\"><alignment horizontal=\"center\"/></xf>" +
                "<xf fontId=\"4\" fillId=\"2\" borderId=\"1\"><alignment horizontal=\"left\"/></xf>" +
                "<xf fontId=\"0\" fillId=\"0\" borderId=\"1\"><alignment horizontal=\"left\"/></xf>" +
                "<xf fontId=\"0\" fillId=\"0\" borderId=\"1\"><alignment horizontal=\"right\"/></xf>" +
                "<xf fontId=\"4\" fillId=\"0\" borderId=\"0\"><alignment horizontal=\"left\"/></xf></cellXfs>" +
                "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles></styleSheet>";
    }

    private static String sheet(LocalDate tuNgay, LocalDate denNgay, String danhMuc,
                                 Map<String, Object> tongQuan,
                                 List<Map<String, Object>> coCauDoanhThu,
                                 List<Map<String, Object>> topSanPham) {
        StringBuilder sb = new StringBuilder();
        StringBuilder merges = new StringBuilder();
        int mergeCount = 0;

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">");
        sb.append("<sheetFormatPr defaultRowHeight=\"20\"/>");
        sb.append("<cols>");
        sb.append("<col min=\"1\" max=\"1\" width=\"8\"  customWidth=\"1\"/>");
        sb.append("<col min=\"2\" max=\"2\" width=\"14\" customWidth=\"1\"/>");
        sb.append("<col min=\"3\" max=\"3\" width=\"36\" customWidth=\"1\"/>");
        sb.append("<col min=\"4\" max=\"4\" width=\"22\" customWidth=\"1\"/>");
        sb.append("<col min=\"5\" max=\"5\" width=\"12\" customWidth=\"1\"/>");
        sb.append("<col min=\"6\" max=\"6\" width=\"14\" customWidth=\"1\"/>");
        sb.append("<col min=\"7\" max=\"7\" width=\"20\" customWidth=\"1\"/>");
        sb.append("</cols>");
        sb.append("<sheetData>");

        int row = 1;

        // ── Tiêu đề ───────────────────────────────────────────────────
        merges.append(mr(row)); mergeCount++;
        row = row(sb, row, cv("BÁO CÁO THỐNG KÊ BIẾN ĐỘNG HÀNG HÓA", 0));

        merges.append(mr(row)); mergeCount++;
        row = row(sb, row, cv("Nhà Thuốc Long Nguyễn", 1));

        merges.append(mr(row)); mergeCount++;
        row = row(sb, row, cv("Khoảng thời gian: từ " + tuNgay.format(SHOW_DATE)
                + " đến " + denNgay.format(SHOW_DATE), 1));
        row++;

        // ── Bộ lọc ────────────────────────────────────────────────────
        merges.append(mr(row)); mergeCount++;
        row = row(sb, row, cv("THÔNG TIN BỘ LỌC", 6));
        row = hdr(sb, row, "Chỉ tiêu bộ lọc", "Giá trị áp dụng");
        row = row(sb, row, cv("Danh mục thuốc", 4), cv(displayAll(danhMuc), 4));
        row = row(sb, row, cv("Ngày trích xuất báo cáo", 4), cv(LocalDate.now().format(SHOW_DATE), 4));
        row++;

        // ── I. KPI Tổng Quan ──────────────────────────────────────────
        merges.append(mr(row)); mergeCount++;
        row = row(sb, row, cv("I. SỐ LIỆU TỔNG QUAN BIẾN ĐỘNG HÀNG HÓA", 6));
        row = hdr(sb, row, "Chỉ tiêu (Thẻ KPI trên giao diện)", "Giá trị");
        row = row(sb, row, cv("Mặt Hàng Đã Bán", 3),
                cv(formatInt(tongQuan.get("tongMatHangDaBan")) + " sản phẩm", 5));
        row = row(sb, row, cv("Tổng Số Lượng Bán", 3),
                cv(formatInt(tongQuan.get("tongSoLuongBan")), 5));
        row = row(sb, row, cv("Doanh Thu Gốc", 3),
                cv(formatMoney(tongQuan.get("tongDoanhThuGoc")) + " đ", 5));
        row = row(sb, row, cv("Doanh Thu Sau Thuế", 3),
                cv(formatMoney(tongQuan.get("tongDoanhThuSauThue")) + " đ", 5));
        row++;

        // ── II. Tỷ Trọng Doanh Thu Theo Nhóm (PieChart) ──────────────
        merges.append(mr(row)); mergeCount++;
        row = row(sb, row, cv("II. TỶ TRỌNG DOANH THU THEO NHÓM DANH MỤC ", 6));
        row = hdr(sb, row, "Nhóm Danh Mục Sản Phẩm", "Doanh Thu Đóng Góp - Sau Thuế (đ)");
        if (empty(coCauDoanhThu)) {
            row = row(sb, row, cv("Chưa có dữ liệu phát sinh trong khoảng thời gian này", 4));
        } else {
            for (Map<String, Object> item : coCauDoanhThu) {
                row = row(sb, row,
                        cv(str(item.get("tenDanhMuc")), 4),
                        cv(formatMoney(item.get("doanhThu")) + " đ", 5));
            }
        }
        row++;

        // ── III. Top 10 Sản Phẩm Bán Chạy Nhất (BarChart + TableView 1) ──
        merges.append(mr(row)); mergeCount++;
        row = row(sb, row, cv("III. TOP 10 SẢN PHẨM BÁN CHẠY NHẤT - THEO SỐ LƯỢNG ", 6));
        row = hdr(sb, row, "STT", "Mã SP", "Tên Sản Phẩm", "Nhóm", "ĐVT", "SL Bán", "Doanh Thu (đ)");
        if (empty(topSanPham)) {
            row = row(sb, row, cv("", 4), cv("Chưa có dữ liệu phát sinh trong khoảng thời gian này", 4));
        } else {
            for (Map<String, Object> item : topSanPham) {
                row = row(sb, row,
                        cv(str(item.get("stt")), 5),
                        cv(str(item.get("maThuoc")), 4),
                        cv(str(item.get("tenThuoc")), 4),
                        cv(str(item.get("tenDanhMuc")), 4),
                        cv(str(item.get("donVi")), 4),
                        cv(formatInt(item.get("soLuongBan")), 5),
                        cv(formatMoney(item.get("doanhThu")) + " đ", 5));
            }
        }
        row++;

        // ── IV. Sản Phẩm Doanh Thu Cao Nhất (TableView 2) ────────────
        List<Map<String, Object>> topDoanhThu = new ArrayList<>();
        if (!empty(topSanPham)) {
            topDoanhThu.addAll(topSanPham);
            topDoanhThu.sort((a, b) -> Double.compare(toDouble(b.get("doanhThu")), toDouble(a.get("doanhThu"))));
        }

        merges.append(mr(row)); mergeCount++;
        row = row(sb, row, cv("IV. SẢN PHẨM DOANH THU CAO NHẤT ", 6));
        row = hdr(sb, row, "STT", "Mã SP", "Tên Sản Phẩm", "Nhóm", "ĐVT", "SL Bán", "Doanh Thu (đ)");
        if (topDoanhThu.isEmpty()) {
            row = row(sb, row, cv("", 4), cv("Chưa có dữ liệu phát sinh trong khoảng thời gian này", 4));
        } else {
            int stt = 1;
            for (Map<String, Object> item : topDoanhThu) {
                row = row(sb, row,
                        cv(String.valueOf(stt++), 5),
                        cv(str(item.get("maThuoc")), 4),
                        cv(str(item.get("tenThuoc")), 4),
                        cv(str(item.get("tenDanhMuc")), 4),
                        cv(str(item.get("donVi")), 4),
                        cv(formatInt(item.get("soLuongBan")), 5),
                        cv(formatMoney(item.get("doanhThu")) + " đ", 5));
            }
        }

        sb.append("</sheetData>");
        if (mergeCount > 0) {
            sb.append("<mergeCells count=\"").append(mergeCount).append("\">");
            sb.append(merges);
            sb.append("</mergeCells>");
        }
        sb.append("<pageMargins left=\"0.5\" right=\"0.5\" top=\"0.5\" bottom=\"0.5\" header=\"0.3\" footer=\"0.3\"/>");
        sb.append("</worksheet>");
        return sb.toString();
    }

    // ── Helpers ───────────────────────────────────────────────────────

    /** merge toàn bộ 7 cột cho dòng rowNum */
    private static String mr(int rowNum) {
        return "<mergeCell ref=\"A" + rowNum + ":G" + rowNum + "\"/>";
    }

    private static int hdr(StringBuilder sb, int rowNum, String... labels) {
        CellValue[] cells = new CellValue[labels.length];
        for (int i = 0; i < labels.length; i++) cells[i] = cv(labels[i], 2);
        return row(sb, rowNum, cells);
    }

    private static int row(StringBuilder sb, int rowNum, CellValue... cells) {
        sb.append("<row r=\"").append(rowNum).append("\">");
        for (int i = 0; i < cells.length; i++) {
            CellValue cell = cells[i];
            if (cell == null) continue;
            sb.append("<c r=\"").append(colName(i + 1)).append(rowNum)
              .append("\" s=\"").append(cell.style)
              .append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
              .append(esc(cell.value)).append("</t></is></c>");
        }
        sb.append("</row>");
        return rowNum + 1;
    }

    private static CellValue cv(String value, int style) { return new CellValue(value, style); }

    private static String colName(int index) {
        StringBuilder sb = new StringBuilder();
        while (index > 0) {
            int rem = (index - 1) % 26;
            sb.insert(0, (char) ('A' + rem));
            index = (index - 1) / 26;
        }
        return sb.toString();
    }

    private static String esc(String value) {
        return str(value).replace("&", "&amp;").replace("<", "&lt;")
                .replace(">", "&gt;").replace("\"", "&quot;").replace("'", "&apos;");
    }

    private static boolean empty(List<?> list) { return list == null || list.isEmpty(); }
    private static String displayAll(String v) { return (v == null || v.trim().isEmpty()) ? "Tất cả" : v; }
    private static String str(Object v) { return v == null ? "" : String.valueOf(v); }
    private static String formatInt(Object v) { return DF.format(toDouble(v)); }
    private static String formatMoney(Object v) { return DF.format(toDouble(v)); }
    private static double toDouble(Object v) {
        if (v instanceof Number) return ((Number) v).doubleValue();
        if (v == null) return 0;
        try { return Double.parseDouble(String.valueOf(v)); } catch (NumberFormatException e) { return 0; }
    }
}