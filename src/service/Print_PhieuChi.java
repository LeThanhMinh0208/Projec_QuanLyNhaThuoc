package service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;
import com.itextpdf.text.pdf.draw.DottedLineSeparator;
import entity.PhieuChi;

import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;

public class Print_PhieuChi {

    public static boolean inPhieu(PhieuChi pc, String path) {
        // Dùng kích thước A5 (nửa tờ A4) cho phiếu chi sẽ đẹp hơn, hoặc dùng A4
        Document document = new Document(PageSize.A5, 30, 30, 40, 40); 
        try {
            PdfWriter.getInstance(document, new FileOutputStream(path));
            document.open();

            // =======================================================
            // 1. CÀI ĐẶT FONT TIẾNG VIỆT (Giống hệt Phiếu Nhập)
            // =======================================================
            BaseFont bf = BaseFont.createFont("C:\\Windows\\Fonts\\arial.ttf", BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            Font fontShopName = new Font(bf, 16, Font.BOLD, BaseColor.BLACK);
            Font fontTitle = new Font(bf, 14, Font.BOLD, BaseColor.BLACK);
            Font fontNormal = new Font(bf, 11, Font.NORMAL, BaseColor.BLACK);
            Font fontBold = new Font(bf, 11, Font.BOLD, BaseColor.BLACK);
            Font fontItalic = new Font(bf, 10, Font.ITALIC, BaseColor.DARK_GRAY);
            Font fontTotalRed = new Font(bf, 14, Font.BOLD, new BaseColor(220, 38, 38)); 

            // =======================================================
            // 2. HEADER THƯƠNG HIỆU
            // =======================================================
            Paragraph shopName = new Paragraph("NHÀ THUỐC LONG NGUYÊN", fontShopName);
            shopName.setAlignment(Element.ALIGN_CENTER);
            document.add(shopName);

            Paragraph address = new Paragraph("Đ/c: 12 Nguyễn Văn Bảo, P.Hạnh Thông, TP.HCM\nHotline: 0123.456.789", fontItalic);
            address.setAlignment(Element.ALIGN_CENTER);
            document.add(address);
            
            document.add(new Paragraph(" "));

            DottedLineSeparator separator = new DottedLineSeparator();
            separator.setPercentage(100);
            separator.setLineColor(BaseColor.GRAY);
            document.add(new Chunk(separator));

            // =======================================================
            // 3. TIÊU ĐỀ PHIẾU
            // =======================================================
            Paragraph title = new Paragraph("PHIẾU CHI TIỀN", fontTitle);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(15f);
            title.setSpacingAfter(20f);
            document.add(title);

            // =======================================================
            // 4. THÔNG TIN PHIẾU CHI
            // =======================================================
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            infoTable.setWidths(new float[]{2f, 3f});
            infoTable.setSpacingAfter(15f);

            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm - dd/MM/yyyy");
            DecimalFormat df = new DecimalFormat("#,### VNĐ");

            // Xử lý hiển thị hình thức
            String ht = pc.getHinhThucChi();
            String tenHinhThuc = "Tiền Mặt";
            if ("CHUYEN_KHOAN".equals(ht)) tenHinhThuc = "Chuyển Khoản";
            else if ("THE".equals(ht)) tenHinhThuc = "Thẻ";

            addInfoRow(infoTable, "Mã phiếu chi:", pc.getMaPhieuChi(), fontNormal);
            addInfoRow(infoTable, "Ngày chi:", pc.getNgayChi() != null ? sdf.format(pc.getNgayChi()) : "---", fontNormal);
            addInfoRow(infoTable, "Người nhận (NCC):", pc.getNhaCungCap() != null ? pc.getNhaCungCap().getTenNhaCungCap() : "---", fontBold);
            addInfoRow(infoTable, "Người lập (NV):", pc.getNhanVien() != null ? pc.getNhanVien().getHoTen() : "---", fontNormal);
            addInfoRow(infoTable, "Hình thức chi:", tenHinhThuc, fontNormal);
            addInfoRow(infoTable, "Lý do / Ghi chú:", pc.getGhiChu() != null ? pc.getGhiChu() : "", fontNormal);

            document.add(infoTable);

            // =======================================================
            // 5. TỔNG CỘNG
            // =======================================================
            PdfPTable totalTable = new PdfPTable(2);
            totalTable.setWidthPercentage(100);
            totalTable.setSpacingBefore(10f);
            totalTable.setWidths(new float[]{6f, 4f});

            PdfPCell cellLabel = new PdfPCell(new Phrase("SỐ TIỀN CHI:", fontTotalRed));
            cellLabel.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellLabel.setBorder(Rectangle.TOP);
            cellLabel.setBorderColor(BaseColor.LIGHT_GRAY);
            cellLabel.setPaddingTop(10f);

            PdfPCell cellValue = new PdfPCell(new Phrase(df.format(pc.getTongTienChi()), fontTotalRed));
            cellValue.setHorizontalAlignment(Element.ALIGN_RIGHT);
            cellValue.setBorder(Rectangle.TOP);
            cellValue.setBorderColor(BaseColor.LIGHT_GRAY);
            cellValue.setPaddingTop(10f);

            totalTable.addCell(cellLabel);
            totalTable.addCell(cellValue);
            document.add(totalTable);

            document.add(new Paragraph(" "));
            document.add(new Paragraph(" "));

            // =======================================================
            // 6. CHỮ KÝ
            // =======================================================
            PdfPTable tableChuKy = new PdfPTable(2);
            tableChuKy.setWidthPercentage(100);
            
            PdfPCell cellNguoiLap = new PdfPCell(new Phrase("Người lập phiếu", fontBold));
            cellNguoiLap.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellNguoiLap.setBorder(Rectangle.NO_BORDER);
            
            PdfPCell cellNguoiNhan = new PdfPCell(new Phrase("Người nhận tiền", fontBold));
            cellNguoiNhan.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellNguoiNhan.setBorder(Rectangle.NO_BORDER);
            
            tableChuKy.addCell(cellNguoiLap);
            tableChuKy.addCell(cellNguoiNhan);

            PdfPCell cellGhiChu1 = new PdfPCell(new Phrase("(Ký và ghi rõ họ tên)", fontItalic));
            cellGhiChu1.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellGhiChu1.setBorder(Rectangle.NO_BORDER);

            PdfPCell cellGhiChu2 = new PdfPCell(new Phrase("(Ký và ghi rõ họ tên)", fontItalic));
            cellGhiChu2.setHorizontalAlignment(Element.ALIGN_CENTER);
            cellGhiChu2.setBorder(Rectangle.NO_BORDER);

            tableChuKy.addCell(cellGhiChu1);
            tableChuKy.addCell(cellGhiChu2);

            document.add(tableChuKy);

            document.close();

            // =======================================================
            // 7. TỰ ĐỘNG MỞ FILE PDF SAU KHI XUẤT
            // =======================================================
            File pdfFile = new File(path);
            if (pdfFile.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdfFile);
            }

            return true;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private static void addInfoRow(PdfPTable table, String label, String value, Font font) {
        PdfPCell c1 = new PdfPCell(new Phrase(label, font));
        c1.setBorder(Rectangle.NO_BORDER);
        c1.setPaddingBottom(8f);
        
        PdfPCell c2 = new PdfPCell(new Phrase(value, font));
        c2.setBorder(Rectangle.NO_BORDER);
        c2.setPaddingBottom(8f);
        
        table.addCell(c1);
        table.addCell(c2);
    }
}