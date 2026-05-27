package utils;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public class NhapKhoExcelImporter {

    public static class KetQua {
        public String maDon = "";
        public List<DongDuLieu> dsDong = new ArrayList<>();
    }

    public static class DongDuLieu {
        public String tenThuoc;
        public int soLuongNhan = -1; // -1 = ô trống, không ghi đè
        public String maLo = "";
        public String ngaySanXuat = ""; // yyyy-MM-dd
        public String hanDung = "";     // yyyy-MM-dd
    }

    public static KetQua docFileExcel(String filePath) throws Exception {
        KetQua result = new KetQua();
        Map<Integer, String> sharedStrings = new HashMap<>();

        try (ZipFile zipFile = new ZipFile(filePath)) {
            ZipEntry ssEntry = zipFile.getEntry("xl/sharedStrings.xml");
            if (ssEntry != null) {
                try (InputStream is = zipFile.getInputStream(ssEntry)) {
                    sharedStrings = parseSharedStrings(is);
                }
            }
            ZipEntry sheetEntry = zipFile.getEntry("xl/worksheets/sheet1.xml");
            if (sheetEntry == null) throw new Exception("File Excel không đúng định dạng (thiếu sheet1.xml).");
            try (InputStream is = zipFile.getInputStream(sheetEntry)) {
                parseSheet(is, sharedStrings, result);
            }
        }
        return result;
    }

    private static Map<Integer, String> parseSharedStrings(InputStream is) throws Exception {
        Map<Integer, String> ss = new HashMap<>();
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
        NodeList siNodes = doc.getElementsByTagName("si");
        for (int i = 0; i < siNodes.getLength(); i++) {
            Element si = (Element) siNodes.item(i);
            NodeList tNodes = si.getElementsByTagName("t");
            StringBuilder sb = new StringBuilder();
            for (int j = 0; j < tNodes.getLength(); j++) sb.append(tNodes.item(j).getTextContent());
            ss.put(i, sb.toString());
        }
        return ss;
    }

    private static void parseSheet(InputStream is, Map<Integer, String> ss, KetQua result) throws Exception {
        Document doc = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(is);
        NodeList rowNodes = doc.getElementsByTagName("row");
        boolean headerFound = false;

        for (int i = 0; i < rowNodes.getLength(); i++) {
            Element rowEl = (Element) rowNodes.item(i);
            int rowNum;
            try { rowNum = Integer.parseInt(rowEl.getAttribute("r")); }
            catch (NumberFormatException e) { continue; }

            // Đọc tối đa 7 cột A-G (index 0-6)
            String[] vals = {"", "", "", "", "", "", ""};
            NodeList cNodes = rowEl.getElementsByTagName("c");
            for (int j = 0; j < cNodes.getLength(); j++) {
                Element c = (Element) cNodes.item(j);
                int colIdx = getColIdx(c.getAttribute("r"));
                if (colIdx >= 7) continue;
                vals[colIdx] = getCellValue(c, ss);
            }

            // Row 3: Mã Đơn ĐH ở cột B (index 1)
            if (rowNum == 3) {
                result.maDon = vals[1].trim();
                continue;
            }

            // Tìm dòng header: cột A chứa "TÊN THUỐC"
            if (!headerFound) {
                String a = vals[0].trim();
                if (a.equalsIgnoreCase("TÊN THUỐC") || a.equalsIgnoreCase("TEN THUOC") || a.equalsIgnoreCase("TENTHUOC")) {
                    headerFound = true;
                }
                continue;
            }

            // Dòng dữ liệu: A=TenThuoc, D=SLNhan, E=MaLo, F=NgaySX, G=HanDung
            String tenThuoc = vals[0].trim();
            if (tenThuoc.isEmpty()) continue;

            DongDuLieu dong = new DongDuLieu();
            dong.tenThuoc = tenThuoc;

            // D (index 3): SL Nhận
            String slStr = vals[3].replaceAll("[^\\d]", "").trim();
            if (!slStr.isEmpty()) {
                try { dong.soLuongNhan = Integer.parseInt(slStr); }
                catch (NumberFormatException ignored) { /* giữ -1 */ }
            }

            // E (index 4): Mã Lô
            dong.maLo = vals[4].trim();

            // F (index 5): Ngày SX → chuẩn hóa thành yyyy-MM-dd
            dong.ngaySanXuat = normalizeDate(vals[5].trim());

            // G (index 6): Hạn Dùng → chuẩn hóa thành yyyy-MM-dd
            dong.hanDung = normalizeDate(vals[6].trim());

            result.dsDong.add(dong);
        }
    }

    // Chuẩn hóa ngày về yyyy-MM-dd (thứ tự Việt Nam: ngày/tháng/năm)
    // Hỗ trợ: d/M/yyyy · d.M.yyyy · d-M-yyyy · yyyy-MM-dd · Excel serial number
    private static String normalizeDate(String raw) {
        if (raw == null || raw.isEmpty()) return "";

        // Đã đúng ISO yyyy-MM-dd
        if (raw.matches("\\d{4}-\\d{2}-\\d{2}")) return raw;

        // d/M/yyyy — xử lý cả 1/3/2026 lẫn 01/03/2026 (ngày/tháng/năm kiểu Việt Nam)
        try {
            return LocalDate.parse(raw, DateTimeFormatter.ofPattern("d/M/yyyy")).toString();
        } catch (DateTimeParseException ignored) { }

        // d.M.yyyy — dùng dấu chấm để tránh Excel tự chuyển thành số serial
        try {
            return LocalDate.parse(raw, DateTimeFormatter.ofPattern("d.M.yyyy")).toString();
        } catch (DateTimeParseException ignored) { }

        // d-M-yyyy
        try {
            return LocalDate.parse(raw, DateTimeFormatter.ofPattern("d-M-yyyy")).toString();
        } catch (DateTimeParseException ignored) { }

        // Excel/WPS serial number — khi ứng dụng tự chuyển văn bản ngày thành số
        // Epoch Excel: 30/12/1899 (có lỗi ngày 29/2/1900, serial > 59 trừ 1)
        try {
            long serial = Long.parseLong(raw.trim());
            if (serial >= 30000 && serial <= 60000) { // khoảng năm 1982–2064
                return LocalDate.of(1899, 12, 30).plusDays(serial).toString();
            }
        } catch (NumberFormatException ignored) { }

        return raw;
    }

    private static String getCellValue(Element c, Map<Integer, String> ss) {
        NodeList isNodes = c.getElementsByTagName("is");
        if (isNodes.getLength() > 0) {
            NodeList tNodes = ((Element) isNodes.item(0)).getElementsByTagName("t");
            if (tNodes.getLength() > 0) return tNodes.item(0).getTextContent();
        }
        NodeList vNodes = c.getElementsByTagName("v");
        if (vNodes.getLength() > 0) {
            String v = vNodes.item(0).getTextContent();
            if ("s".equals(c.getAttribute("t"))) {
                try { return ss.getOrDefault(Integer.parseInt(v), ""); }
                catch (NumberFormatException e) { /* fall through */ }
            }
            return v;
        }
        return "";
    }

    private static int getColIdx(String r) {
        if (r == null || r.isEmpty()) return 0;
        String letters = r.replaceAll("[0-9]", "");
        int idx = 0;
        for (int i = 0; i < letters.length(); i++) idx = idx * 26 + (letters.charAt(i) - 'A' + 1);
        return idx - 1;
    }
}
