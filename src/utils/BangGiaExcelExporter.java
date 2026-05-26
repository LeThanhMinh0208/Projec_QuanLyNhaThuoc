package utils;

import entity.BangGia;
import entity.ChiTietBangGia;

import java.io.OutputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Xuất bảng giá ra file Excel (.xlsx) không cần thư viện ngoài.
 * Dùng kỹ thuật raw XML + ZipOutputStream (OOXML format).
 */
public class BangGiaExcelExporter {

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

    /**
     * Xuất bảng giá ra file Excel.
     *
     * @param bangGia      thông tin bảng giá
     * @param chiTietList  danh sách chi tiết (thuốc + đơn vị + giá)
     * @return đường dẫn tuyệt đối đến file đã tạo
     */
    public static String xuatExcel(BangGia bangGia, List<ChiTietBangGia> chiTietList) throws Exception {
        Path dir = Paths.get("exports/banggia");
        Files.createDirectories(dir);

        String timeStr = LocalDateTime.now().format(FILE_TIME);
        String fileName = "BangGia_" + bangGia.getMaBangGia() + "_" + timeStr + ".xlsx";
        Path filePath = dir.resolve(fileName);

        try (OutputStream os = Files.newOutputStream(filePath);
             ZipOutputStream zos = new ZipOutputStream(os)) {

            writeEntry(zos, "[Content_Types].xml", buildContentTypesXml());
            writeEntry(zos, "_rels/.rels", buildRootRelsXml());
            writeEntry(zos, "xl/workbook.xml", buildWorkbookXml());
            writeEntry(zos, "xl/_rels/workbook.xml.rels", buildWorkbookRelsXml());
            writeEntry(zos, "xl/styles.xml", buildStylesXml());
            writeEntry(zos, "xl/worksheets/sheet1.xml", buildSheetXml(bangGia, chiTietList));
        }

        return filePath.toAbsolutePath().toString();
    }

    /**
     * Xuất file mẫu Excel để người dùng điền giá và nhập lại.
     * Format: Tên Thuốc | Đơn Vị | Giá Bán (VNĐ)
     *
     * @param chiTietList danh sách thuốc + đơn vị (giá để trống)
     * @return đường dẫn tuyệt đối đến file mẫu
     */
    public static String xuatFileMau(List<ChiTietBangGia> chiTietList) throws Exception {
        Path dir = Paths.get("exports/banggia");
        Files.createDirectories(dir);

        String timeStr = LocalDateTime.now().format(FILE_TIME);
        String fileName = "MauNhapBangGia_" + timeStr + ".xlsx";
        Path filePath = dir.resolve(fileName);

        try (OutputStream os = Files.newOutputStream(filePath);
             ZipOutputStream zos = new ZipOutputStream(os)) {

            writeEntry(zos, "[Content_Types].xml", buildContentTypesXml());
            writeEntry(zos, "_rels/.rels", buildRootRelsXml());
            writeEntry(zos, "xl/workbook.xml", buildWorkbookXmlMau());
            writeEntry(zos, "xl/_rels/workbook.xml.rels", buildWorkbookRelsXml());
            writeEntry(zos, "xl/styles.xml", buildStylesXml());
            writeEntry(zos, "xl/worksheets/sheet1.xml", buildSheetXmlMau(chiTietList));
        }

        return filePath.toAbsolutePath().toString();
    }

    private static void writeEntry(ZipOutputStream zos, String name, String content) throws Exception {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private static String buildSheetXml(BangGia bg, List<ChiTietBangGia> chiTiet) {
        StringBuilder sb = new StringBuilder();
        List<String> merges = new ArrayList<>();

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">\n");
        sb.append("<sheetFormatPr defaultRowHeight=\"20\"/>\n");
        sb.append("<cols>");
        sb.append(colXml(1, 1, 8));   // STT
        sb.append(colXml(2, 2, 40));  // Tên thuốc
        sb.append(colXml(3, 3, 20));  // Đơn vị
        sb.append(colXml(4, 4, 20));  // Giá bán
        sb.append("</cols>\n");
        sb.append("<sheetData>\n");

        int row = 1;
        // Header section
        row = addMergedRow(sb, merges, row, 1, "BẢNG GIÁ THUỐC");
        row = addMergedRow(sb, merges, row, 2, "Nhà Thuốc Long Nguyên");
        row++;

        // Info section
        row = addMergedRow(sb, merges, row, 3, "THÔNG TIN BẢNG GIÁ");
        row = addTwoColRow(sb, row, "Mã bảng giá", bg.getMaBangGia(), 5, 6);
        row = addTwoColRow(sb, row, "Tên bảng giá", bg.getTenBangGia(), 5, 6);
        row = addTwoColRow(sb, row, "Loại", "DEFAULT".equals(bg.getLoaiBangGia()) ? "Mặc Định" : "Khuyến Mãi", 5, 6);
        row = addTwoColRow(sb, row, "Ngày bắt đầu",
                bg.getNgayBatDau() != null ? bg.getNgayBatDau().format(SHOW_DATE) : "", 5, 6);
        row = addTwoColRow(sb, row, "Ngày kết thúc",
                bg.getNgayKetThuc() != null ? bg.getNgayKetThuc().format(SHOW_DATE) : "Không có", 5, 6);
        row = addTwoColRow(sb, row, "Mô tả",
                bg.getMoTa() != null ? bg.getMoTa() : "", 5, 6);
        row = addTwoColRow(sb, row, "Ngày xuất báo cáo", LocalDate.now().format(SHOW_DATE), 5, 6);
        row++;

        // Table header
        row = addMergedRow(sb, merges, row, 3, "DANH SÁCH THUỐC VÀ GIÁ BÁN");
        row = addFourColRow(sb, row, "STT", "Tên Thuốc", "Đơn Vị", "Giá Bán (VNĐ)", true);

        // Data rows
        if (chiTiet != null) {
            int stt = 1;
            for (ChiTietBangGia ct : chiTiet) {
                String gia = (ct.getDonGiaBan() != null && ct.getDonGiaBan().compareTo(BigDecimal.ZERO) > 0)
                        ? DF.format(ct.getDonGiaBan())
                        : "";
                row = addFourColRow(sb, row,
                        String.valueOf(stt++),
                        ct.getTenThuoc() != null ? ct.getTenThuoc() : "",
                        ct.getTenDonVi() != null ? ct.getTenDonVi() : "",
                        gia,
                        false);
            }
        } else {
            row = addFourColRow(sb, row, "", "Không có dữ liệu", "", "", false);
        }

        row++;
        row = addMergedRow(sb, merges, row, 2, "File được xuất tự động từ hệ thống Quản Lý Nhà Thuốc.");

        sb.append("</sheetData>\n");
        if (!merges.isEmpty()) {
            sb.append("<mergeCells count=\"").append(merges.size()).append("\">\n");
            for (String merge : merges) {
                sb.append("<mergeCell ref=\"").append(merge).append("\"/>\n");
            }
            sb.append("</mergeCells>\n");
        }
        sb.append("<pageMargins left=\"0.5\" right=\"0.5\" top=\"0.5\" bottom=\"0.5\" header=\"0.3\" footer=\"0.3\"/>\n");
        sb.append("</worksheet>");
        return sb.toString();
    }

    private static String buildSheetXmlMau(List<ChiTietBangGia> chiTiet) {
        StringBuilder sb = new StringBuilder();
        List<String> merges = new ArrayList<>();

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">\n");
        sb.append("<sheetFormatPr defaultRowHeight=\"20\"/>\n");
        sb.append("<cols>");
        sb.append(colXml(1, 1, 40));  // Tên thuốc
        sb.append(colXml(2, 2, 20));  // Đơn vị
        sb.append(colXml(3, 3, 20));  // Giá bán
        sb.append("</cols>\n");
        sb.append("<sheetData>\n");

        int row = 1;
        row = addMergedRow3Col(sb, merges, row, 1, "FILE MẪU NHẬP BẢNG GIÁ - Nhà Thuốc Long Nguyên");
        row = addMergedRow3Col(sb, merges, row, 2,
                "Hướng dẫn: Điền giá vào cột 'Giá Bán (VNĐ)'. Không thay đổi cột Tên Thuốc và Đơn Vị.");
        row++;

        // Header row
        row = addThreeColRow(sb, row, "Tên Thuốc", "Đơn Vị", "Giá Bán (VNĐ)", true);

        // Data rows - tên thuốc + đơn vị có sẵn, giá trống
        if (chiTiet != null) {
            for (ChiTietBangGia ct : chiTiet) {
                row = addThreeColRow(sb, row,
                        ct.getTenThuoc() != null ? ct.getTenThuoc() : "",
                        ct.getTenDonVi() != null ? ct.getTenDonVi() : "",
                        "",
                        false);
            }
        }

        sb.append("</sheetData>\n");
        if (!merges.isEmpty()) {
            sb.append("<mergeCells count=\"").append(merges.size()).append("\">\n");
            for (String merge : merges) {
                sb.append("<mergeCell ref=\"").append(merge).append("\"/>\n");
            }
            sb.append("</mergeCells>\n");
        }
        sb.append("<pageMargins left=\"0.5\" right=\"0.5\" top=\"0.5\" bottom=\"0.5\" header=\"0.3\" footer=\"0.3\"/>\n");
        sb.append("</worksheet>");
        return sb.toString();
    }

    // ============================================================
    // XML builder helpers
    // ============================================================
    private static String colXml(int min, int max, double width) {
        return "<col min=\"" + min + "\" max=\"" + max + "\" width=\"" + width + "\" customWidth=\"1\"/>";
    }

    private static int addMergedRow(StringBuilder sb, List<String> merges, int rowNum, int style, String text) {
        String ref = "A" + rowNum + ":D" + rowNum;
        merges.add(ref);
        sb.append(rowXml(rowNum, new CellValue[]{new CellValue(text, style)}));
        return rowNum + 1;
    }

    private static int addMergedRow3Col(StringBuilder sb, List<String> merges, int rowNum, int style, String text) {
        String ref = "A" + rowNum + ":C" + rowNum;
        merges.add(ref);
        sb.append(rowXml(rowNum, new CellValue[]{new CellValue(text, style)}));
        return rowNum + 1;
    }

    private static int addTwoColRow(StringBuilder sb, int rowNum, String left, String right, int ls, int rs) {
        sb.append(rowXml(rowNum, new CellValue[]{new CellValue(left, ls), new CellValue(right, rs)}));
        return rowNum + 1;
    }

    private static int addFourColRow(StringBuilder sb, int rowNum,
                                     String c1, String c2, String c3, String c4, boolean isHeader) {
        int s = isHeader ? 4 : 7;
        int sr = isHeader ? 4 : 8;
        sb.append(rowXml(rowNum, new CellValue[]{
                new CellValue(c1, s),
                new CellValue(c2, s),
                new CellValue(c3, s),
                new CellValue(c4, sr)
        }));
        return rowNum + 1;
    }

    private static int addThreeColRow(StringBuilder sb, int rowNum,
                                      String c1, String c2, String c3, boolean isHeader) {
        int s = isHeader ? 4 : 7;
        int sr = isHeader ? 4 : 8;
        sb.append(rowXml(rowNum, new CellValue[]{
                new CellValue(c1, s),
                new CellValue(c2, s),
                new CellValue(c3, sr)
        }));
        return rowNum + 1;
    }

    private static String rowXml(int rowNum, CellValue[] cells) {
        StringBuilder row = new StringBuilder();
        row.append("<row r=\"").append(rowNum).append("\">");
        for (int i = 0; i < cells.length; i++) {
            CellValue cell = cells[i];
            if (cell == null || cell.value == null) continue;
            String ref = colName(i + 1) + rowNum;
            row.append("<c r=\"").append(ref).append("\" s=\"").append(cell.style)
                    .append("\" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
                    .append(escapeXml(cell.value))
                    .append("</t></is></c>");
        }
        row.append("</row>\n");
        return row.toString();
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

    private static String escapeXml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    // ============================================================
    // OOXML structure builders
    // ============================================================
    private static String buildContentTypesXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">" +
                "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>" +
                "<Default Extension=\"xml\" ContentType=\"application/xml\"/>" +
                "<Override PartName=\"/xl/workbook.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.sheet.main+xml\"/>" +
                "<Override PartName=\"/xl/worksheets/sheet1.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.worksheet+xml\"/>" +
                "<Override PartName=\"/xl/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.spreadsheetml.styles+xml\"/>" +
                "</Types>";
    }

    private static String buildRootRelsXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
                "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"xl/workbook.xml\"/>" +
                "</Relationships>";
    }

    private static String buildWorkbookXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">" +
                "<sheets><sheet name=\"BangGia\" sheetId=\"1\" r:id=\"rId1\"/></sheets>" +
                "</workbook>";
    }

    private static String buildWorkbookXmlMau() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">" +
                "<sheets><sheet name=\"NhapBangGia\" sheetId=\"1\" r:id=\"rId1\"/></sheets>" +
                "</workbook>";
    }

    private static String buildWorkbookRelsXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" +
                "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/worksheet\" Target=\"worksheets/sheet1.xml\"/>" +
                "<Relationship Id=\"rId2\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>" +
                "</Relationships>";
    }

    private static String buildStylesXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>" +
                "<styleSheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">" +
                "<fonts count=\"5\">" +
                "<font><sz val=\"11\"/><color rgb=\"FF000000\"/><name val=\"Calibri\"/><family val=\"2\"/></font>" +
                "<font><b/><sz val=\"16\"/><color rgb=\"FF0EA5E9\"/><name val=\"Calibri\"/><family val=\"2\"/></font>" +
                "<font><i/><sz val=\"10\"/><color rgb=\"FF6B7280\"/><name val=\"Calibri\"/><family val=\"2\"/></font>" +
                "<font><b/><sz val=\"10\"/><color rgb=\"FFFFFFFF\"/><name val=\"Calibri\"/><family val=\"2\"/></font>" +
                "<font><b/><sz val=\"10\"/><color rgb=\"FF1F2937\"/><name val=\"Calibri\"/><family val=\"2\"/></font>" +
                "</fonts>" +
                "<fills count=\"5\">" +
                "<fill><patternFill patternType=\"none\"/></fill>" +
                "<fill><patternFill patternType=\"gray125\"/></fill>" +
                "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFF8FAFC\"/><bgColor indexed=\"64\"/></patternFill></fill>" +
                "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFB91C1C\"/><bgColor indexed=\"64\"/></patternFill></fill>" +
                "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF1D4ED8\"/><bgColor indexed=\"64\"/></patternFill></fill>" +
                "</fills>" +
                "<borders count=\"2\">" +
                "<border><left/><right/><top/><bottom/><diagonal/></border>" +
                "<border><left style=\"thin\"><color rgb=\"FFD1D5DB\"/></left><right style=\"thin\"><color rgb=\"FFD1D5DB\"/></right><top style=\"thin\"><color rgb=\"FFD1D5DB\"/></top><bottom style=\"thin\"><color rgb=\"FFD1D5DB\"/></bottom><diagonal/></border>" +
                "</borders>" +
                "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>" +
                "<cellXfs count=\"9\">" +
                "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" +
                "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" +
                "<xf numFmtId=\"0\" fontId=\"3\" fillId=\"4\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" +
                "<xf numFmtId=\"0\" fontId=\"3\" fillId=\"3\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" +
                "<xf numFmtId=\"0\" fontId=\"4\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>" +
                "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>" +
                "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>" +
                "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>" +
                "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"right\" vertical=\"center\"/></xf>" +
                "</cellXfs>" +
                "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>" +
                "<dxfs count=\"0\"/>" +
                "<tableStyles count=\"0\" defaultTableStyle=\"TableStyleMedium2\" defaultPivotStyle=\"PivotStyleLight16\"/>" +
                "</styleSheet>";
    }
}
