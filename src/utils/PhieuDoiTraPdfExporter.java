package utils;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.format.DateTimeFormatter;
import java.util.List;

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

import entity.PhieuDoiTraView;

public class PhieuDoiTraPdfExporter {

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
                    if (color != null) {
						f.setColor(color);
					}
                    return f;
                }
            }
        } catch (Exception ignored) {}
        Font f = new Font(Font.FontFamily.HELVETICA, size, style);
        if (color != null) {
			f.setColor(color);
		}
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
                cb, Element.ALIGN_RIGHT, phrase, doc.right(), doc.bottom() - 10, 0
            );
        }
    }

    public static String xuatPDF(PhieuDoiTraView pdt, List<Object[]> listChiTiet, List<Object[]> listThuocDoi) throws Exception {
        Path dir = Paths.get("exports/phieudoitra");
        Files.createDirectories(dir);

        DateTimeFormatter dtfFile = DateTimeFormatter.ofPattern("yyyyMMdd_HHmm");
        DateTimeFormatter dtfShow = DateTimeFormatter.ofPattern("HH:mm - dd/MM/yyyy");

        String timeStr = pdt.getNgayDoiTra() != null ? pdt.getNgayDoiTra().toLocalDateTime().format(dtfFile) : "unknown";
        String timeShow = pdt.getNgayDoiTra() != null ? pdt.getNgayDoiTra().toLocalDateTime().format(dtfShow) : "—";

        String fileName = "PhieuDoiTra_" + pdt.getMaPhieuDoiTra() + "_" + timeStr + ".pdf";
        String filePath = dir.resolve(fileName).toString();

        Document doc = new Document(PageSize.A5, 20, 20, 25, 20);
        PdfWriter writer = PdfWriter.getInstance(doc, new FileOutputStream(filePath));
        writer.setPageEvent(new PageNumberEvent());
        doc.open();

        Font fTitleXl = getFont(16, Font.BOLD);
        Font fTitle = getFont(13, Font.BOLD);
        Font fSub = getFont(9, Font.ITALIC, BaseColor.DARK_GRAY);
        Font fHead = getFont(10, Font.BOLD, BaseColor.WHITE);
        Font fNormal = getFont(10, Font.NORMAL);
        Font fBold = getFont(10, Font.BOLD);
        Font fTotalLabel = getFont(12, Font.BOLD, new BaseColor(192, 0, 0));
        Font fTotal = getFont(12, Font.BOLD, BaseColor.RED);

        // ── Header Nhà Thuốc ──
        Paragraph shopName = new Paragraph("NHÀ THUỐC LONG NGUYÊN", fTitleXl);
        shopName.setAlignment(Element.ALIGN_CENTER);
        doc.add(shopName);

        Paragraph shopAddr = new Paragraph("Đ/c: 12 Nguyễn Văn Bảo, P.Hạnh Thông, TP.HCM\nHotline: 0123.456.789", fSub);
        shopAddr.setAlignment(Element.ALIGN_CENTER);
        shopAddr.setSpacingAfter(8);
        doc.add(shopAddr);

        DottedLineSeparator separator = new DottedLineSeparator();
        separator.setGap(3);
        doc.add(new Chunk(separator));

        // ── Tiêu đề Phiếu ──
        Paragraph title = new Paragraph("PHIẾU GIAO DỊCH ĐỔI TRẢ", fTitle);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(10);
        title.setSpacingAfter(15);
        doc.add(title);

        // ── Thông tin ──
        PdfPTable infoTable = new PdfPTable(2);
        infoTable.setWidthPercentage(100);
        infoTable.setWidths(new float[] { 3.5f, 6.5f });
        infoTable.setSpacingAfter(15);

        addInfoRow(infoTable, "Mã phiếu", pdt.getMaPhieuDoiTra(), fNormal);
        addInfoRow(infoTable, "Mã hóa đơn gốc", pdt.getMaHoaDon(), fNormal);
        addInfoRow(infoTable, "Ngày lập", timeShow, fNormal);
        addInfoRow(infoTable, "Khách hàng", pdt.getTenKhachHang(), fNormal);
        addInfoRow(infoTable, "Nhân viên", pdt.getTenNhanVien(), fNormal);
        addInfoRow(infoTable, "Hình thức", pdt.getHinhThucXuLyLabel(), fNormal);

        String lyDo = pdt.getLyDoHienThi();
        if (lyDo == null || lyDo.isEmpty()) {
			lyDo = "—";
		}
        addInfoRow(infoTable, "Lý do", lyDo, fNormal);
        doc.add(infoTable);

        // ── THUỐC KHÁCH TRẢ ──
        Paragraph pTra = new Paragraph("DANH SÁCH THUỐC KHÁCH TRẢ LẠI", fBold);
        pTra.setSpacingAfter(5);
        doc.add(pTra);
        doc.add(createTableChiTiet(listChiTiet, fHead, fNormal, true));

        // ── THUỐC KHÁCH NHẬN (nếu Đổi SP) ──
        if (pdt.isDoiSanPham() && listThuocDoi != null && !listThuocDoi.isEmpty()) {
            doc.add(Chunk.NEWLINE);
            Paragraph pDoi = new Paragraph("DANH SÁCH THUỐC KHÁCH NHẬN ĐỔI", fBold);
            pDoi.setSpacingAfter(5);
            doc.add(pDoi);
            doc.add(createTableChiTiet(listThuocDoi, fHead, fNormal, false));
        }

        // ── TỔNG KẾT BÙ/HOÀN TIỀN ──
        doc.add(Chunk.NEWLINE);
        PdfPTable tongTable = new PdfPTable(2);
        tongTable.setWidthPercentage(80);
        tongTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        tongTable.setWidths(new float[] { 5f, 5f });
        tongTable.setSpacingBefore(10);
        tongTable.setSpacingAfter(10);

        if (pdt.isDoiSanPham()) {
            addTongRow(tongTable, "Ghi chú:", pdt.getMoTaChenhLechDoiSanPham(), fTotalLabel, fTotal);
        } else {
            double tongTienTra = 0;
            if (listChiTiet != null) {
                tongTienTra = listChiTiet.stream().mapToDouble(r -> (double) r[6]).sum();
            }
            double phiPhat = pdt.getPhiPhat();
            double tongHoan = Math.max(0, tongTienTra - phiPhat);

            addTongRow(tongTable, "Giá trị thuốc trả:", String.format("%,.0f đ", tongTienTra), fNormal, fNormal);
            addTongRow(tongTable, "Phí thu hoàn/phạt:", String.format("- %,.0f đ", phiPhat), fNormal, fNormal);

            PdfPCell divider = new PdfPCell(new Phrase(""));
            divider.setBorder(Rectangle.TOP);
            divider.setColspan(2);
            divider.setPaddingTop(5);
            divider.setBorderColor(BaseColor.GRAY);
            tongTable.addCell(divider);

            addTongRow(tongTable, "TIỀN KHÁCH NHẬN LẠI:", String.format("%,.0f đ", tongHoan), fTotalLabel, fTotal);
        }
        doc.add(tongTable);

        // ── FOOTER ──
        doc.add(Chunk.NEWLINE);
        doc.add(new Chunk(separator));
        Paragraph footer = new Paragraph("Cảm ơn quý khách đã tin tưởng sử dụng dịch vụ!", fSub);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(8);
        doc.add(footer);

        doc.close();
        return filePath;
    }

    private static PdfPTable createTableChiTiet(List<Object[]> dataList, Font fHead, Font fNormal, boolean isThuocTra) throws Exception {
        PdfPTable table = new PdfPTable(5);
        table.setWidthPercentage(100);
        table.setWidths(new float[] { 0.8f, 3.5f, 1.2f, 1.0f, 2.0f });
        table.setHeaderRows(1);

        BaseColor headerBg = new BaseColor(41, 128, 185);
        String[] headers = { "STT", "Tên thuốc", "Đơn giá", "SL", "Thành tiền" };
        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, fHead));
            cell.setNoWrap(true);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            cell.setBackgroundColor(headerBg);
            cell.setPadding(6);
            cell.setPaddingBottom(8);
            table.addCell(cell);
        }

        int stt = 1;
        if (dataList == null || dataList.isEmpty()) {
            PdfPCell empty = new PdfPCell(new Phrase("Không có dữ liệu", fNormal));
            empty.setColspan(5);
            empty.setHorizontalAlignment(Element.ALIGN_CENTER);
            empty.setPadding(10);
            table.addCell(empty);
            return table;
        }

        for (Object item : dataList) {
            String tenThuoc = "—";
            double donGia = 0;
            int soDoi = 0;
            double thanhTien = 0;

            if (isThuocTra) { // Object[] từ Database (STT, TenThuoc, DonVi, Lo, HSD, SL, DonGia, ThanhTien)
                Object[] arr = (Object[]) item;
                tenThuoc = arr[0] != null ? (String) arr[0] : "—";
                soDoi = ((Number) arr[4]).intValue();
                donGia = ((Number) arr[5]).doubleValue();
                thanhTien = ((Number) arr[6]).doubleValue();
            } else { // Object[] từ parse JSON/String (maQuyDoi, tenThuoc, tenDonVi, soLuong, donGia)
                Object[] arr = (Object[]) item;
                tenThuoc = arr[1] != null ? (String) arr[1] : "—";
                soDoi = Integer.parseInt(arr[3].toString());
                donGia = Double.parseDouble(arr[4].toString());
                thanhTien = donGia * soDoi;
            }

            boolean evenRow = (stt % 2 == 0);
            BaseColor rowBg = evenRow ? new BaseColor(245, 245, 245) : BaseColor.WHITE;

            addCell(table, String.valueOf(stt++), fNormal, Element.ALIGN_CENTER, rowBg);
            addCell(table, tenThuoc, fNormal, Element.ALIGN_LEFT, rowBg);
            addCell(table, String.format("%,.0f", donGia), fNormal, Element.ALIGN_RIGHT, rowBg);
            addCell(table, String.valueOf(soDoi), fNormal, Element.ALIGN_CENTER, rowBg);
            addCell(table, String.format("%,.0f", thanhTien), fNormal, Element.ALIGN_RIGHT, rowBg);
        }
        return table;
    }

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
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setBackgroundColor(bg);
        cell.setBorderColor(new BaseColor(220, 220, 220));
        cell.setBorderWidth(0.5f);
        cell.setLeading(0f, 1.3f);
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
