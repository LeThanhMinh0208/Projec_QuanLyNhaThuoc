package utils;

import java.io.File;
import java.io.OutputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class ChiTietDonDatHangExcelExporter {

    public static class DrugRow {
        public final String tenThuoc;
        public final String tenDonVi;
        public final int soLuongDat;
        public final int soLuongNhan;
        public final String maLo;
        public final String ngaySanXuat;
        public final String hanDung;

        public DrugRow(String tenThuoc, String tenDonVi, int soLuongDat, int soLuongNhan,
                       String maLo, String ngaySanXuat, String hanDung) {
            this.tenThuoc   = tenThuoc    != null ? tenThuoc    : "";
            this.tenDonVi   = tenDonVi    != null ? tenDonVi    : "";
            this.soLuongDat = soLuongDat;
            this.soLuongNhan = soLuongNhan;
            this.maLo       = maLo        != null ? maLo        : "";
            this.ngaySanXuat = ngaySanXuat != null ? ngaySanXuat : "";
            this.hanDung    = hanDung     != null ? hanDung     : "";
        }
    }

    private static final class CV {
        final String v;
        final int s;
        CV(String v, int s) { this.v = v; this.s = s; }
    }

    public static void xuatFileNhapKho(String maDon, String nhaCungCap, String ghiChu,
                                        List<DrugRow> dsThuoc, File file) throws Exception {
        try (OutputStream os = Files.newOutputStream(file.toPath());
             ZipOutputStream zos = new ZipOutputStream(os)) {
            write(zos, "[Content_Types].xml", contentTypes());
            write(zos, "_rels/.rels",          rootRels());
            write(zos, "xl/workbook.xml",       workbook());
            write(zos, "xl/_rels/workbook.xml.rels", workbookRels());
            write(zos, "xl/styles.xml",         styles());
            write(zos, "xl/worksheets/sheet1.xml", sheet(maDon, nhaCungCap, ghiChu, dsThuoc));
        }
    }

    private static void write(ZipOutputStream zos, String name, String content) throws Exception {
        zos.putNextEntry(new ZipEntry(name));
        zos.write(content.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        zos.closeEntry();
    }

    private static String sheet(String maDon, String nhaCungCap, String ghiChu, List<DrugRow> dsThuoc) {
        StringBuilder sb = new StringBuilder();
        List<String> merges = new ArrayList<>();

        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>");
        sb.append("<worksheet xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\">\n");
        sb.append("<sheetFormatPr defaultRowHeight=\"22\"/>\n");
        sb.append("<cols>");
        sb.append(colDef(1, 1, 38)); // A: Tên Thuốc
        sb.append(colDef(2, 2, 12)); // B: Đơn Vị
        sb.append(colDef(3, 3, 10)); // C: SL Đặt
        sb.append(colDef(4, 4, 10)); // D: SL Nhận
        sb.append(colDef(5, 5, 18)); // E: Mã Lô
        sb.append(colDef(6, 6, 15)); // F: Ngày SX
        sb.append(colDef(7, 7, 15)); // G: Hạn Dùng
        sb.append("</cols>\n");
        sb.append("<sheetData>\n");

        int r = 1;

        // Row 1: Title
        merges.add("A1:G1");
        sb.append(row(r++, new CV[]{ new CV("PHIẾU NHẬP KHO - NHÀ THUỐC LONG NGUYÊN", 0) }));

        // Row 2: Instructions
        merges.add("A2:G2");
        sb.append(row(r++, new CV[]{ new CV(
            "Hướng dẫn: Điền Số Lượng Nhận, Mã Lô, Ngày SX và Hạn Dùng. Định dạng ngày dùng DẤU CHẤM: dd.MM.yyyy (VD: 01.03.2026) để tránh Excel tự đổi ngày. Không thay đổi cột Tên Thuốc, Đơn Vị, SL Đặt.", 2) }));

        // Row 3: Mã Đơn | Nhà Cung Cấp
        merges.add("B3:C3");
        merges.add("E3:G3");
        sb.append(row(r++, new CV[]{
            new CV("Mã Đơn ĐH:", 5),
            new CV(maDon != null ? maDon : "", 4),
            null,
            new CV("Nhà Cung Cấp:", 5),
            new CV(nhaCungCap != null ? nhaCungCap : "", 4)
        }));

        // Row 4: Ghi Chú or empty spacer
        String gc = (ghiChu != null) ? ghiChu.trim() : "";
        if (!gc.isEmpty() && !"---".equals(gc)) {
            merges.add("B4:G4");
            sb.append(row(r++, new CV[]{ new CV("Ghi Chú:", 5), new CV(gc, 4) }));
        } else {
            r++;
        }

        // Row 5: Table headers
        sb.append(row(r++, new CV[]{
            new CV("TÊN THUỐC",  3),
            new CV("ĐƠN VỊ",    3),
            new CV("SL ĐẶT",    3),
            new CV("SL NHẬN",   3),
            new CV("MÃ LÔ",     3),
            new CV("NGÀY SX",   3),
            new CV("HẠN DÙNG",  3)
        }));

        // Data rows
        if (dsThuoc != null) {
            for (DrugRow dr : dsThuoc) {
                String slNhan = dr.soLuongNhan > 0 ? String.valueOf(dr.soLuongNhan) : "";
                sb.append(row(r++, new CV[]{
                    new CV(dr.tenThuoc,             4), // read-only gray
                    new CV(dr.tenDonVi,             6),
                    new CV(String.valueOf(dr.soLuongDat), 8), // right-aligned
                    new CV(slNhan,                  9), // yellow input
                    new CV(dr.maLo,                 9),
                    new CV(dr.ngaySanXuat,          9),
                    new CV(dr.hanDung,              9)
                }));
            }
        }

        sb.append("</sheetData>\n");
        sb.append("<mergeCells count=\"").append(merges.size()).append("\">\n");
        for (String m : merges) sb.append("<mergeCell ref=\"").append(m).append("\"/>\n");
        sb.append("</mergeCells>\n");
        sb.append("<pageMargins left=\"0.5\" right=\"0.5\" top=\"0.5\" bottom=\"0.5\" header=\"0.3\" footer=\"0.3\"/>\n");
        sb.append("</worksheet>");
        return sb.toString();
    }

    private static String row(int rn, CV[] cells) {
        StringBuilder r = new StringBuilder();
        r.append("<row r=\"").append(rn).append("\">");
        for (int i = 0; i < cells.length; i++) {
            if (cells[i] == null) continue;
            String ref = colName(i + 1) + rn;
            String qp = (cells[i].s == 9) ? " quotePrefix=\"1\"" : "";
            r.append("<c r=\"").append(ref).append("\" s=\"").append(cells[i].s)
             .append("\"").append(qp)
             .append(" t=\"inlineStr\"><is><t xml:space=\"preserve\">")
             .append(esc(cells[i].v)).append("</t></is></c>");
        }
        r.append("</row>\n");
        return r.toString();
    }

    private static String colName(int idx) {
        StringBuilder sb = new StringBuilder();
        while (idx > 0) {
            int rem = (idx - 1) % 26;
            sb.insert(0, (char)('A' + rem));
            idx = (idx - 1) / 26;
        }
        return sb.toString();
    }

    private static String colDef(int min, int max, double w) {
        return "<col min=\"" + min + "\" max=\"" + max + "\" width=\"" + w + "\" customWidth=\"1\"/>";
    }

    private static String esc(String v) {
        return v.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
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
            "<workbook xmlns=\"http://schemas.openxmlformats.org/spreadsheetml/2006/main\"" +
            " xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\">" +
            "<sheets><sheet name=\"NhapKho\" sheetId=\"1\" r:id=\"rId1\"/></sheets>" +
            "</workbook>";
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
            "<fonts count=\"5\">" +
            "<font><sz val=\"11\"/><color rgb=\"FF000000\"/><name val=\"Calibri\"/><family val=\"2\"/></font>" +
            "<font><b/><sz val=\"16\"/><color rgb=\"FF0EA5E9\"/><name val=\"Calibri\"/><family val=\"2\"/></font>" +
            "<font><i/><sz val=\"10\"/><color rgb=\"FF6B7280\"/><name val=\"Calibri\"/><family val=\"2\"/></font>" +
            "<font><b/><sz val=\"10\"/><color rgb=\"FFFFFFFF\"/><name val=\"Calibri\"/><family val=\"2\"/></font>" +
            "<font><b/><sz val=\"10\"/><color rgb=\"FF1F2937\"/><name val=\"Calibri\"/><family val=\"2\"/></font>" +
            "</fonts>" +
            "<fills count=\"6\">" +
            "<fill><patternFill patternType=\"none\"/></fill>" +
            "<fill><patternFill patternType=\"gray125\"/></fill>" +
            "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFF8FAFC\"/><bgColor indexed=\"64\"/></patternFill></fill>" +
            "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFB91C1C\"/><bgColor indexed=\"64\"/></patternFill></fill>" +
            "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FF1D4ED8\"/><bgColor indexed=\"64\"/></patternFill></fill>" +
            "<fill><patternFill patternType=\"solid\"><fgColor rgb=\"FFFEF9C3\"/><bgColor indexed=\"64\"/></patternFill></fill>" +
            "</fills>" +
            "<borders count=\"2\">" +
            "<border><left/><right/><top/><bottom/><diagonal/></border>" +
            "<border>" +
            "<left style=\"thin\"><color rgb=\"FFD1D5DB\"/></left>" +
            "<right style=\"thin\"><color rgb=\"FFD1D5DB\"/></right>" +
            "<top style=\"thin\"><color rgb=\"FFD1D5DB\"/></top>" +
            "<bottom style=\"thin\"><color rgb=\"FFD1D5DB\"/></bottom>" +
            "<diagonal/>" +
            "</border>" +
            "</borders>" +
            "<cellStyleXfs count=\"1\"><xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"0\"/></cellStyleXfs>" +
            "<cellXfs count=\"10\">" +
            "<xf numFmtId=\"0\" fontId=\"1\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" +
            "<xf numFmtId=\"0\" fontId=\"2\" fillId=\"0\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" +
            "<xf numFmtId=\"0\" fontId=\"3\" fillId=\"4\" borderId=\"0\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"center\" vertical=\"center\" wrapText=\"1\"/></xf>" +
            "<xf numFmtId=\"0\" fontId=\"3\" fillId=\"3\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"center\" vertical=\"center\"/></xf>" +
            "<xf numFmtId=\"0\" fontId=\"4\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>" +
            "<xf numFmtId=\"0\" fontId=\"4\" fillId=\"2\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>" +
            "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>" +
            "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>" +
            "<xf numFmtId=\"0\" fontId=\"0\" fillId=\"0\" borderId=\"1\" xfId=\"0\" applyFont=\"1\"><alignment horizontal=\"right\" vertical=\"center\"/></xf>" +
            "<xf numFmtId=\"49\" fontId=\"4\" fillId=\"5\" borderId=\"1\" xfId=\"0\" applyFont=\"1\" applyNumberFormat=\"1\"><alignment horizontal=\"left\" vertical=\"center\"/></xf>" +
            "</cellXfs>" +
            "<cellStyles count=\"1\"><cellStyle name=\"Normal\" xfId=\"0\" builtinId=\"0\"/></cellStyles>" +
            "<dxfs count=\"0\"/>" +
            "<tableStyles count=\"0\" defaultTableStyle=\"TableStyleMedium2\" defaultPivotStyle=\"PivotStyleLight16\"/>" +
            "</styleSheet>";
    }
}
