package utils;

import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ThongKeTonKhoExcelExporter {

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

    public static String xuatExcel(
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

        Path filePath = dir.resolve("ThongKeTonKho_" + LocalDateTime.now().format(FILE_TIME) + ".xlsx");
        try (OutputStream os = Files.newOutputStream(filePath);
             ZipOutputStream zos = new ZipOutputStream(os)) {
            writeEntry(zos, "[Content_Types].xml", contentTypes());
            writeEntry(zos, "_rels/.rels", rootRels());
            writeEntry(zos, "xl/workbook.xml", workbook());
            writeEntry(zos, "xl/_rels/workbook.xml.rels", workbookRels());
            writeEntry(zos, "xl/styles.xml", styles());
            writeEntry(zos, "xl/worksheets/sheet1.xml", sheet(tuNgay, denNgay, danhMuc, trangThaiTon,
                    tongQuan, tonKhoTheoDanhMuc, bienDongTonKho, topTonKho, loSapHetHan));
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
                "<sheets><sheet name=\"ThongKeTonKho\" sheetId=\"1\" r:id=\"rId1\"/></sheets></workbook>";
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

    private static String sheet(LocalDate tuNgay, LocalDate denNgay, String danhMuc, String trangThaiTon,
                                Map<String, Object> tongQuan,
                                List<Map<String, Object>> tonKhoTheoDanhMuc,
                                List<Map<String, Object>> bienDongTonKho,
                                List<Map<String, Object>> topTonKho,
                                List<Map<String, Object>> loSapHetHan) {
        StringBuilder sb = new StringBuilder();
        StringBuilder merges = new StringBuilder();
        int mergeCount = 0;

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"><sheetFormatPr defaultRowHeight=\"20\"/>");
        sb.append("<cols><col min=\"1\" max=\"1\" width=\"16\" customWidth=\"1\"/><col min=\"2\" max=\"2\" width=\"34\" customWidth=\"1\"/><col min=\"3\" max=\"8\" width=\"18\" customWidth=\"1\"/></cols><sheetData>");

        int row = 1;
        row = addMerged(sb, merges, row, 0, "BÁO CÁO THỐNG KÊ TỒN KHO"); mergeCount++;
        row = addMerged(sb, merges, row, 1, "Nhà Thuốc Long Nguyễn"); mergeCount++;
        row = addMerged(sb, merges, row, 1, "Biến động từ " + tuNgay.format(SHOW_DATE) + " đến " + denNgay.format(SHOW_DATE)); mergeCount++;
        row++;

        row = section(sb, merges, row, "THÔNG TIN LỌC"); mergeCount++;
        row = header(sb, row, "Chỉ tiêu", "Giá trị");
        row = row(sb, row, new CellValue("Danh mục", 4), new CellValue(emptyAll(danhMuc), 4));
        row = row(sb, row, new CellValue("Trạng thái tồn", 4), new CellValue(emptyAll(trangThaiTon), 4));
        row = row(sb, row, new CellValue("Ngày lập báo cáo", 4), new CellValue(LocalDate.now().format(SHOW_DATE), 4));
        row++;

        row = section(sb, merges, row, "TỔNG QUAN"); mergeCount++;
        row = header(sb, row, "Chỉ tiêu", "Giá trị");
        row = row(sb, row, new CellValue("Tổng mặt hàng", 4), new CellValue(formatInt(tongQuan.get("tongMatHang")), 5));
        row = row(sb, row, new CellValue("Tổng lô còn hàng", 4), new CellValue(formatInt(tongQuan.get("tongLoConHang")), 5));
        row = row(sb, row, new CellValue("Tổng số lượng tồn", 4), new CellValue(formatInt(tongQuan.get("tongSoLuongTon")), 5));
        row = row(sb, row, new CellValue("Tổng giá trị tồn", 4), new CellValue(formatMoney(tongQuan.get("tongGiaTriTon")) + " đ", 5));
        row = row(sb, row, new CellValue("Mặt hàng tồn thấp", 4), new CellValue(formatInt(tongQuan.get("soMatHangTonThap")), 5));
        row = row(sb, row, new CellValue("Lô sắp hết hạn", 4), new CellValue(formatInt(tongQuan.get("soLoSapHetHan")), 5));
        row++;

        row = section(sb, merges, row, "TỒN KHO THEO DANH MỤC"); mergeCount++;
        row = header(sb, row, "Danh mục", "Số lượng tồn", "Giá trị tồn");
        if (empty(tonKhoTheoDanhMuc)) {
            row = row(sb, row, new CellValue("Không có dữ liệu", 4));
        } else {
            for (Map<String, Object> item : tonKhoTheoDanhMuc) {
                row = row(sb, row, new CellValue(str(item.get("tenDanhMuc")), 4),
                        new CellValue(formatInt(item.get("soLuongTon")), 5),
                        new CellValue(formatMoney(item.get("giaTriTon")) + " đ", 5));
            }
        }
        row++;

        row = section(sb, merges, row, "BIẾN ĐỘNG NHẬP/XUẤT"); mergeCount++;
        row = header(sb, row, "Ngày", "Nhập", "Xuất");
        if (empty(bienDongTonKho)) {
            row = row(sb, row, new CellValue("Không có dữ liệu", 4));
        } else {
            for (Map<String, Object> item : bienDongTonKho) {
                row = row(sb, row, new CellValue(str(item.get("ngay")), 4),
                        new CellValue(formatInt(item.get("soLuongNhap")), 5),
                        new CellValue(formatInt(item.get("soLuongXuat")), 5));
            }
        }
        row++;

        row = section(sb, merges, row, "TOP TỒN KHO"); mergeCount++;
        row = header(sb, row, "STT", "Mã thuốc", "Tên thuốc", "Danh mục", "Tồn", "ĐVT", "Số lô", "Giá trị");
        if (empty(topTonKho)) {
            row = row(sb, row, new CellValue("", 4), new CellValue("Không có dữ liệu", 4));
        } else {
            for (Map<String, Object> item : topTonKho) {
                row = row(sb, row, new CellValue(str(item.get("stt")), 5), new CellValue(str(item.get("maThuoc")), 4),
                        new CellValue(str(item.get("tenThuoc")), 4), new CellValue(str(item.get("tenDanhMuc")), 4),
                        new CellValue(formatInt(item.get("soLuongTon")), 5), new CellValue(str(item.get("donViCoBan")), 4),
                        new CellValue(formatInt(item.get("soLo")), 5), new CellValue(formatMoney(item.get("giaTriTon")) + " đ", 5));
            }
        }
        row++;

        row = section(sb, merges, row, "LÔ SẮP HẾT HẠN"); mergeCount++;
        row = header(sb, row, "STT", "Mã lô", "Tên thuốc", "Danh mục", "Hạn dùng", "Còn lại", "Tồn", "Giá nhập");
        if (empty(loSapHetHan)) {
            row = row(sb, row, new CellValue("", 4), new CellValue("Không có dữ liệu", 4));
        } else {
            for (Map<String, Object> item : loSapHetHan) {
                row = row(sb, row, new CellValue(str(item.get("stt")), 5), new CellValue(str(item.get("maLoThuoc")), 4),
                        new CellValue(str(item.get("tenThuoc")), 4), new CellValue(str(item.get("tenDanhMuc")), 4),
                        new CellValue(str(item.get("hanSuDung")), 4), new CellValue(formatInt(item.get("soNgayConLai")) + " ngày", 5),
                        new CellValue(formatInt(item.get("soLuongTon")), 5), new CellValue(formatMoney(item.get("giaNhap")) + " đ", 5));
            }
        }

        sb.append("</sheetData>");
        if (mergeCount > 0) {
            sb.append("<mergeCells count=\"").append(mergeCount).append("\">").append(merges).append("</mergeCells>");
        }
        sb.append("<pageMargins left=\"0.5\" right=\"0.5\" top=\"0.5\" bottom=\"0.5\" header=\"0.3\" footer=\"0.3\"/></worksheet>");
        return sb.toString();
    }

    private static int section(StringBuilder sb, StringBuilder merges, int row, String title) {
        return addMerged(sb, merges, row, 6, title);
    }

    private static int addMerged(StringBuilder sb, StringBuilder merges, int row, int style, String text) {
        merges.append("<mergeCell ref=\"A").append(row).append(":H").append(row).append("\"/>");
        return row(sb, row, new CellValue(text, style));
    }

    private static int header(StringBuilder sb, int row, String... values) {
        CellValue[] cells = new CellValue[values.length];
        for (int i = 0; i < values.length; i++) {
            cells[i] = new CellValue(values[i], 2);
        }
        return row(sb, row, cells);
    }

    private static int row(StringBuilder sb, int rowNum, CellValue... cells) {
        sb.append("<row r=\"").append(rowNum).append("\">");
        for (int i = 0; i < cells.length; i++) {
            CellValue cell = cells[i];
            if (cell == null) continue;
            sb.append("<c r=\"").append(colName(i + 1)).append(rowNum).append("\" s=\"").append(cell.style).append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
                    .append(escape(cell.value)).append("</t></is></c>");
        }
        sb.append("</row>");
        return rowNum + 1;
    }

    private static String colName(int index) {
        StringBuilder sb = new StringBuilder();
        while (index > 0) {
            int rem = (index - 1) % 26;
            sb.insert(0, (char) ('A' + rem));
            index = (index - 1) / 26;
        }
        return sb.toString();
    }

    private static String escape(String value) {
        return str(value).replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }

    private static boolean empty(List<?> list) {
        return list == null || list.isEmpty();
    }

    private static String emptyAll(String value) {
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
