package utils;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import dao.DAO_DonThuoc;
import entity.DonThuoc;
import entity.HoaDonView;

import java.io.*;
import java.nio.file.*;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HoaDonPdfExporter {

    // Dùng font Arial hệ thống để hiển thị tiếng Việt
    private static Font getFont(float size, int style) {
        return getFont(size, style, null);
    }

    private static Font getFont(float size, int style, BaseColor color) {
        try {
            String[] fontPaths = {
                    "C:/Windows/Fonts/arial.ttf",
                    "C:/Windows/Fonts/times.ttf",
                    "/usr/share/fonts/truetype/freefont/FreeSerif.ttf" // Linux fallback
            };
            for (String path : fontPaths) {
                if (new File(path).exists()) {
                    BaseFont bf = BaseFont.createFont(
                            path, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
                    Font f = new Font(bf, size, style);
                    if (color != null)
                        f.setColor(color);
                    return f;
                }
            }
        } catch (Exception ignored) {
        }
        // Fallback: Helvetica (không dấu nhưng không crash)
        Font f = new Font(Font.FontFamily.HELVETICA, size, style);
        if (color != null)
            f.setColor(color);
        return f;
    }

    static class PageNumberEvent extends PdfPageEventHelper {
        Font fPage = getFont(8, Font.ITALIC, BaseColor.GRAY);

        @Override
        public void onEndPage(PdfWriter writer, Document doc) {
            PdfContentByte cb = writer.getDirectContent();
            String text = "Trang " + writer.getPageNumber();
            Phrase phrase = new Phrase(text, fPage);
            ColumnText.showTextAligned(
                cb,
                Element.ALIGN_RIGHT,
                phrase,
                doc.right(),
                doc.bottom() - 10,
                0
            );
        }
    }

    /**
     * Xuất PDF hóa đơn dựa trên HoaDonView và danh sách chi tiết Object[].
     */
    public static String xuatPDF(HoaDonView hd, List<Object[]> chiTietList) throws Exception {
        Path dir = Paths.get("exports/hoadon");
        Files.createDirectories(dir);

        DateTimeFormatter dtfFile = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
        DateTimeFormatter dtfShow = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");

        String timeStr = hd.getNgayLap() != null
                ? hd.getNgayLap().toLocalDateTime().format(dtfFile)
                : "unknown";
        String timeShow = hd.getNgayLap() != null
                ? hd.getNgayLap().toLocalDateTime().format(dtfShow)
                : "—";

        String fileName = "HoaDon_" + hd.getMaHoaDon() + "_" + timeStr + ".pdf";
        String filePath = dir.resolve(fileName).toString();

        // Tinh chỉnh Margin để khổ A5 nhìn thoáng hơn
        Document doc = new Document(PageSize.A5, 20, 20, 25, 20);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(filePath));
        PageNumberEvent pageEvent = new PageNumberEvent();
        writer.setPageEvent(pageEvent);
        doc.open();

        Font fTitleXl = getFont(16, Font.BOLD);
        Font fTitle = getFont(13, Font.BOLD);
        Font fSub = getFont(9, Font.ITALIC, BaseColor.DARK_GRAY);
        Font fHead = getFont(10, Font.BOLD, BaseColor.WHITE); // Chữ trắng cho nổi bật header
        Font fNormal = getFont(10, Font.NORMAL);
        Font fBold = getFont(10, Font.BOLD);
        Font fTotalLabel = getFont(12, Font.BOLD, new BaseColor(192, 0, 0));
        Font fTotal = getFont(12, Font.BOLD, BaseColor.RED); // Tổng tiền màu đỏ cho máu

        // ── TÊN NHÀ THUỐC ──
        Paragraph shopName = new Paragraph("NHÀ THUỐC LONG NGUYÊN", fTitleXl);
        shopName.setAlignment(Element.ALIGN_CENTER);
        doc.add(shopName);

        Paragraph shopAddr = new Paragraph(
                "Đ/c: 12 Nguyễn Văn Bảo, P.Hạnh Thông, TP.HCM\nHotline: 0123.456.789", fSub);
        shopAddr.setAlignment(Element.ALIGN_CENTER);
        shopAddr.setSpacingAfter(8);
        doc.add(shopAddr);

        // Đường kẻ gạch ngang nét đứt nhìn nghệ hơn
        DottedLineSeparator separator = new DottedLineSeparator();
        separator.setGap(3);
        doc.add(new Chunk(separator));

        // ── TIÊU ĐỀ ──
        Paragraph title = new Paragraph("HÓA ĐƠN BÁN HÀNG", fTitle);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(10);
        title.setSpacingAfter(15);
        doc.add(title);

        // ── THÔNG TIN HÓA ĐƠN (Đẩy ra 100% width, căn lề thẳng tắp) ──
        String tenKH = hd.getTenKhachHang() != null ? hd.getTenKhachHang() : "Khách lẻ";
        String tenNV = hd.getTenNhanVien() != null ? hd.getTenNhanVien() : "—";
        String hinhThuc = hd.getHinhThucLabel() != null ? hd.getHinhThucLabel() : "Tiền mặt";

        // VĐ4: Loại bán
        String loaiHienThi = "BAN_THEO_DON".equals(hd.getLoaiBan())
                ? "Bán theo đơn thuốc"
                : "Bán lẻ";

        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[] { 3f, 7f }); // Tỉ lệ 3:7 siêu chuẩn
        infoTable.setSpacingAfter(15);

        addInfoRow(infoTable, "Mã hóa đơn", hd.getMaHoaDon(), fNormal);
        addInfoRow(infoTable, "Ngày lập", timeShow, fNormal);
        addInfoRow(infoTable, "Khách hàng", tenKH, fNormal);
        addInfoRow(infoTable, "Thu ngân", tenNV, fNormal);
        addInfoRow(infoTable, "Hình thức", hinhThuc, fNormal);
        addInfoRow(infoTable, "Loại", loaiHienThi, fNormal);
        doc.add(infoTable);

        // ── VĐ4: THÔNG TIN ĐƠN THUỐC (nếu BAN_THEO_DON) ──
        if ("BAN_THEO_DON".equals(hd.getLoaiBan())) {
            try {
                DAO_DonThuoc daoDT = new DAO_DonThuoc();
                DonThuoc dt = daoDT.getByMaHoaDon(hd.getMaHoaDon());
                if (dt != null) {
                    Paragraph dtTitle = new Paragraph("THÔNG TIN ĐƠN THUỐC", fBold);
                    dtTitle.setSpacingBefore(5);
                    dtTitle.setSpacingAfter(5);
                    doc.add(dtTitle);

                    PdfPTable dtTable = new PdfPTable(2);
                    dtTable.setWidthPercentage(100);
                    dtTable.setWidths(new float[] { 3f, 7f });
                    dtTable.setSpacingAfter(15);

                    addInfoRow(dtTable, "Bác sĩ kê đơn", dt.getTenBacSi(), fNormal);
                    addInfoRow(dtTable, "Chẩn đoán", dt.getChanDoan(), fNormal);
                    addInfoRow(dtTable, "Bệnh nhân", dt.getThongTinBenhNhan(), fNormal);
                    doc.add(dtTable);
                }
            } catch (Exception ignored) {
                // Không có DonThuoc → bỏ qua
            }
        }

        // ── BẢNG CHI TIẾT (VĐ4: thêm cột Đơn giá) ──
        PdfPTable table = new PdfPTable(6);
        table.setWidthPercentage(100);
        // STT | Tên thuốc | ĐVT | Đơn giá | SL | Thành tiền
        table.setWidths(new float[] { 0.7f, 3.5f, 1.0f, 1.8f, 0.7f, 2.0f });
        table.setHeaderRows(1); // Dòng header xanh lặp lại đầu mỗi trang

        // Header bảng — Xanh dương đậm, chữ trắng nhìn cực pro
        BaseColor headerBg = new BaseColor(41, 128, 185);
        String[] headers = { "STT", "Tên thuốc", "ĐVT", "Đơn giá", "SL", "Thành tiền" };
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, fHead));
            cell.setNoWrap(true);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBackgroundColor(headerBg);
            cell.setPadding(6); // Tăng padding nhìn cho thoáng
            cell.setPaddingBottom(8);
            table.addCell(cell);
        }

        int stt = 1;
        if (chiTietList == null || chiTietList.isEmpty()) {
            PdfPCell empty = new PdfPCell(new Phrase("Không có sản phẩm", fNormal));
            empty.setColspan(6);
            empty.setHorizontalAlignment(Element.ALIGN_CENTER);
            empty.setPadding(10);
            table.addCell(empty);
        } else {
            for (Object[] ct : chiTietList) {
                String tenThuoc = ct[0] != null ? (String) ct[0] : "—";
                String tenDonVi = ct[1] != null ? (String) ct[1] : "—";
                int soLuong = ((Number) ct[4]).intValue();
                double donGia = ((Number) ct[5]).doubleValue();
                double thanhTien = ((Number) ct[6]).doubleValue();

                boolean evenRow = (stt % 2 == 0);
                BaseColor rowBg = evenRow ? new BaseColor(245, 245, 245) : BaseColor.WHITE;

                PdfPCell sttCell = new PdfPCell(new Phrase(String.valueOf(stt++), fNormal));
                sttCell.setHorizontalAlignment(Element.ALIGN_CENTER);
                sttCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                sttCell.setBackgroundColor(rowBg);
                sttCell.setBorderColor(new BaseColor(220, 220, 220));
                sttCell.setBorderWidth(0.5f);
                sttCell.setPadding(5);
                table.addCell(sttCell);

                addCell(table, tenThuoc, fNormal, Element.ALIGN_LEFT, rowBg);
                addCell(table, tenDonVi, fNormal, Element.ALIGN_CENTER, rowBg);
                addCell(table, String.format("%,.0f", donGia), fNormal, Element.ALIGN_RIGHT, rowBg);
                addCell(table, String.valueOf(soLuong), fNormal, Element.ALIGN_CENTER, rowBg);
                addCell(table, String.format("%,.0f", thanhTien), fNormal, Element.ALIGN_RIGHT, rowBg);
            }
        }
        doc.add(table);

        // ── TỔNG TIỀN (Ép sang phải góc 60% màn hình) ──
        doc.add(Chunk.NEWLINE);
        double tamTinh = hd.getTamTinh();
        double thueVAT = hd.getThueVAT();
        double tongSauVAT = hd.getTongSauVAT();
        double tienVAT = tongSauVAT - tamTinh;

        PdfPTable tongTable = new PdfPTable(2);
        tongTable.setWidthPercentage(65); // Chỉ chiếm 65% bên phải
        tongTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tongTable.setWidths(new float[] { 4f, 3.5f });
        tongTable.setSpacingBefore(10);
        tongTable.setSpacingAfter(10);

        addTongRow(tongTable, "Tạm tính:", String.format("%,.0f đ", tamTinh), fNormal, fNormal);
        addTongRow(tongTable, String.format("VAT (%.0f%%):", thueVAT), String.format("%,.0f đ", tienVAT), fNormal, fNormal);

        // Kẻ line trước khi tính tổng chốt
        PdfPCell divider = new PdfPCell(new Phrase(""));
        divider.setBorder(Rectangle.TOP);
        divider.setColspan(2);
        divider.setPaddingTop(5);
        divider.setBorderColor(BaseColor.GRAY);
        tongTable.addCell(divider);

        addTongRow(tongTable, "TỔNG CỘNG:", String.format("%,.0f đ", tongSauVAT), fTotalLabel, fTotal);
        doc.add(tongTable);

        // ── FOOTER ──
        doc.add(Chunk.NEWLINE);
        doc.add(new Chunk(separator)); // Tái sử dụng dòng kẻ đứt
        Paragraph footer = new Paragraph(
                "Cảm ơn quý khách! Chúc quý khách sức khỏe. Hẹn gặp lại!", fSub);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(8);
        doc.add(footer);

        doc.close();
        return filePath;
    }

    // Tối ưu lại addInfoRow: gỡ bỏ dấu 2 chấm cứng nhắc, nhìn table phẳng hơn
    private static void addInfoRow(PdfPTable t, String label, String value, Font f) {
        PdfPCell lbl = new PdfPCell(new Phrase(label + ":", f));
        lbl.setBackgroundColor(new BaseColor(245, 248, 250));
        lbl.setPaddingLeft(6);
        lbl.setPaddingRight(8);
        lbl.setBorder(Rectangle.NO_BORDER);
        lbl.setHorizontalAlignment(Element.ALIGN_LEFT);
        lbl.setPaddingBottom(6);

        PdfPCell val = new PdfPCell(new Phrase(value != null ? value : "—", f));
        val.setBorder(Rectangle.NO_BORDER);
        val.setHorizontalAlignment(Element.ALIGN_LEFT);
        val.setPaddingBottom(6);

        t.addCell(lbl);
        t.addCell(val);
    }

    private static void addCell(PdfPTable t, String text, Font f, int align, BaseColor bg) {
        PdfPCell cell = new PdfPCell(new Phrase(text != null ? text : "", f));
        cell.setHorizontalAlignment(align);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE); // Căn giữa theo chiều dọc
        cell.setBackgroundColor(bg);
        cell.setBorderColor(new BaseColor(220, 220, 220));
        cell.setBorderWidth(0.5f);
        cell.setLeading(0f, 1.3f); // line height 1.3x
        cell.setMinimumHeight(22f);
        cell.setPadding(5);
        cell.setPaddingBottom(7);
        t.addCell(cell);
    }

    private static void addTongRow(PdfPTable t, String label, String value, Font fLabel, Font fValue) {
        PdfPCell lbl = new PdfPCell(new Phrase(label, fLabel));
        lbl.setBorder(Rectangle.NO_BORDER);
        lbl.setHorizontalAlignment(Element.ALIGN_RIGHT);
        lbl.setPaddingBottom(6);

        PdfPCell val = new PdfPCell(new Phrase(value, fValue));
        val.setBorder(Rectangle.NO_BORDER);
        val.setHorizontalAlignment(Element.ALIGN_RIGHT);
        val.setPaddingBottom(6);

        t.addCell(lbl);
        t.addCell(val);
    }
}