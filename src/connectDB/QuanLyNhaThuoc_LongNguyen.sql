-- ==========================================
-- CẤU TRÚC CƠ SỞ DỮ LIỆU (SCHEMA)
-- ==========================================
-- ========================================================
-- TẠO DATABASE: NHÀ THUỐC LONG NGUYÊN
-- ========================================================
CREATE DATABASE QuanLyNhaThuoc_LongNguyen;
GO

USE QuanLyNhaThuoc_LongNguyen;
GO

-- ========================================================
-- 1. TẠO CÁC BẢNG ĐỘC LẬP (KHÔNG CÓ KHÓA NGOẠI)
-- ========================================================

-- Bảng Nhà Cung Cấp
CREATE TABLE NhaCungCap (
    maNhaCungCap VARCHAR(20) PRIMARY KEY,
    tenNhaCungCap NVARCHAR(100) NOT NULL,
    sdt VARCHAR(15),
    diaChi NVARCHAR(255),
    congNo DECIMAL(18,2) DEFAULT 0
);

-- Bảng Khách Hàng
CREATE TABLE KhachHang (
    maKhachHang VARCHAR(20) PRIMARY KEY,
    hoTen NVARCHAR(100) NOT NULL,
    sdt VARCHAR(15),
    diaChi NVARCHAR(255),
    diemTichLuy INT DEFAULT 0
);

-- Bảng Nhân Viên
CREATE TABLE NhanVien (
    maNhanVien VARCHAR(20) PRIMARY KEY,
    tenDangNhap VARCHAR(50) UNIQUE NOT NULL,
    matKhau VARCHAR(255) NOT NULL,
    hoTen NVARCHAR(100) NOT NULL,
    chucVu NVARCHAR(50),
    caLamViec NVARCHAR(50),
    sdt VARCHAR(15)
);

-- Bảng Danh Mục Thuốc
CREATE TABLE DanhMucThuoc (
    maDanhMuc VARCHAR(20) PRIMARY KEY,
    tenDanhMuc NVARCHAR(100) NOT NULL,
    moTa NVARCHAR(255)
);

-- ========================================================
-- 2. TẠO BẢNG THUỐC & CHI TIẾT SẢN PHẨM (CÓ KHÓA NGOẠI)
-- ========================================================

-- Bảng Thuốc (Header)
CREATE TABLE Thuoc (
    maThuoc VARCHAR(20) PRIMARY KEY,
    maDanhMuc VARCHAR(20) NOT NULL,
    tenThuoc NVARCHAR(150) NOT NULL,
    hoatChat NVARCHAR(255),
    hamLuong NVARCHAR(50),
    hangSanXuat NVARCHAR(100),
    nuocSanXuat NVARCHAR(50),
    congDung NVARCHAR(255),
    trieuChung NVARCHAR(255),
    donViCoBan NVARCHAR(20) NOT NULL, -- Vd: Viên, Gói...
    hinhAnh VARCHAR(255),
    canKeDon BIT DEFAULT 0, -- 0: Không cần, 1: Cần kê đơn
    trangThai VARCHAR(20) DEFAULT 'DANG_BAN', 
    
    FOREIGN KEY (maDanhMuc) REFERENCES DanhMucThuoc(maDanhMuc),
    -- Ràng buộc check cho Enum TrangThaiThuoc
    CONSTRAINT CHK_TrangThai CHECK (trangThai IN ('DANG_BAN', 'NGUNG_BAN', 'HET_HANG'))
);

-- Bảng Đơn Vị Quy Đổi (Detail Giá & Đóng gói)
CREATE TABLE DonViQuyDoi (
    maQuyDoi VARCHAR(20) PRIMARY KEY,
    maThuoc VARCHAR(20) NOT NULL,
    tenDonVi NVARCHAR(50) NOT NULL, -- Vd: Hộp, Vỉ...
    tyLeQuyDoi INT NOT NULL, -- Vd: 100, 10, 1...
    giaBan DECIMAL(18,2) NOT NULL,

    FOREIGN KEY (maThuoc) REFERENCES Thuoc(maThuoc) ON DELETE CASCADE
);

-- Bảng Lô Thuốc (Quản lý tồn kho FEFO)
CREATE TABLE LoThuoc (
    maLoThuoc VARCHAR(20) PRIMARY KEY,
    maThuoc VARCHAR(20) NOT NULL,
    ngaySanXuat DATE,
    hanSuDung DATE NOT NULL,
    soLuongTon INT DEFAULT 0, -- Lưu theo Đơn vị cơ bản (Viên)
    giaNhap DECIMAL(18,2),

    
    viTriKho VARCHAR(50) DEFAULT 'KHO_BAN_HANG',

    FOREIGN KEY (maThuoc) REFERENCES Thuoc(maThuoc) ON DELETE CASCADE
,
    CONSTRAINT CHK_ViTriKho CHECK (viTriKho IN ('KHO_BAN_HANG', 'KHO_DU_TRU'))
);

-- ========================================================
-- 3. TẠO BẢNG NGHIỆP VỤ: NHẬP HÀNG
-- ========================================================

-- Bảng Phiếu Nhập (Header)
CREATE TABLE PhieuNhap (
    maPhieuNhap VARCHAR(20) PRIMARY KEY,
    maNhaCungCap VARCHAR(20) NOT NULL,
    maNhanVien VARCHAR(20) NOT NULL,
    ngayNhap DATETIME DEFAULT GETDATE(),
    
    FOREIGN KEY (maNhaCungCap) REFERENCES NhaCungCap(maNhaCungCap),
    FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien)
);

-- Bảng Chi Tiết Phiếu Nhập (Detail)
CREATE TABLE ChiTietPhieuNhap (
    maPhieuNhap VARCHAR(20) NOT NULL,
    maQuyDoi VARCHAR(20) NOT NULL,
    maLoThuoc VARCHAR(20) NOT NULL,
    soLuong INT NOT NULL,
    donGiaNhap DECIMAL(18,2) NOT NULL,

    -- Khóa chính kép để 1 phiếu có thể nhập nhiều thuốc khác nhau
    PRIMARY KEY (maPhieuNhap, maQuyDoi, maLoThuoc),
    FOREIGN KEY (maPhieuNhap) REFERENCES PhieuNhap(maPhieuNhap) ON DELETE CASCADE,
    FOREIGN KEY (maQuyDoi) REFERENCES DonViQuyDoi(maQuyDoi),
    FOREIGN KEY (maLoThuoc) REFERENCES LoThuoc(maLoThuoc)
);

-- ========================================================
-- 4. TẠO BẢNG NGHIỆP VỤ: BÁN HÀNG
-- ========================================================

-- Bảng Hóa Đơn (Header)
CREATE TABLE HoaDon (
    maHoaDon VARCHAR(20) PRIMARY KEY,
    maKhachHang VARCHAR(20), -- Cho phép NULL nếu khách vãng lai không tích điểm
    maNhanVien VARCHAR(20) NOT NULL,
    ngayLap DATETIME DEFAULT GETDATE(),
    thueVAT DECIMAL(5,2) DEFAULT 0, -- Vd: 8.00 cho 8%
    hinhThucThanhToan VARCHAR(20) DEFAULT 'TIEN_MAT',
    ghiChu NVARCHAR(255),

    FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),
    FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    -- Ràng buộc check cho Enum HinhThucThanhToan
    CONSTRAINT CHK_ThanhToan CHECK (hinhThucThanhToan IN ('TIEN_MAT', 'CHUYEN_KHOAN', 'THE'))
);

-- Bảng Đơn Thuốc (Nếu mua theo đơn Bác sĩ - Quan hệ 1-1 với Hóa Đơn)
CREATE TABLE DonThuoc (
    maDonThuoc VARCHAR(20) PRIMARY KEY,
    maHoaDon VARCHAR(20) UNIQUE NOT NULL, -- Đảm bảo 1 HĐ chỉ có max 1 Đơn thuốc
    tenBacSi NVARCHAR(100),
    chanDoan NVARCHAR(255),
    hinhAnhDon VARCHAR(255),
    thongTinBenhNhan NVARCHAR(255),

    FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon) ON DELETE CASCADE
);

-- Bảng Chi Tiết Hóa Đơn (Detail)
CREATE TABLE ChiTietHoaDon (
    maHoaDon VARCHAR(20) NOT NULL,
    maQuyDoi VARCHAR(20) NOT NULL,
    maLoThuoc VARCHAR(20) NOT NULL,
    soLuong INT NOT NULL,
    donGia DECIMAL(18,2) NOT NULL, -- Chốt giá tại thời điểm bán

    PRIMARY KEY (maHoaDon, maQuyDoi, maLoThuoc),
    FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon) ON DELETE CASCADE,
    FOREIGN KEY (maQuyDoi) REFERENCES DonViQuyDoi(maQuyDoi),
    FOREIGN KEY (maLoThuoc) REFERENCES LoThuoc(maLoThuoc)
);

-- ========================================================
-- 5. TẠO BẢNG NGHIỆP VỤ: ĐỔI TRẢ
-- ========================================================

-- Bảng Phiếu Đổi Trả (Header)
CREATE TABLE PhieuDoiTra (
    maPhieuDoiTra VARCHAR(20) PRIMARY KEY,
    maHoaDon VARCHAR(20) NOT NULL, -- Đổi trả dựa trên bill nào
    maNhanVien VARCHAR(20) NOT NULL,
    ngayDoiTra DATETIME DEFAULT GETDATE(),
    lyDo NVARCHAR(255),
    hinhThucXuLy VARCHAR(20) NOT NULL,
    phiPhat DECIMAL(18,2) DEFAULT 0,

    FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon),
    FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    -- Ràng buộc check cho Enum HinhThucDoiTra
    CONSTRAINT CHK_HinhThucXuLy CHECK (hinhThucXuLy IN ('HOAN_TIEN', 'DOI_SAN_PHAM'))
);

-- Bảng Chi Tiết Đổi Trả (Detail)
CREATE TABLE ChiTietDoiTra (
    maPhieuDoiTra VARCHAR(20) NOT NULL,
    maQuyDoi VARCHAR(20) NOT NULL,
    maLoThuoc VARCHAR(20) NOT NULL, -- Biết trả lại vào Lô nào
    soLuong INT NOT NULL,
    tinhTrang NVARCHAR(100), -- Vd: Còn nguyên seal, Rách hộp...

    PRIMARY KEY (maPhieuDoiTra, maQuyDoi, maLoThuoc),
    FOREIGN KEY (maPhieuDoiTra) REFERENCES PhieuDoiTra(maPhieuDoiTra) ON DELETE CASCADE,
    FOREIGN KEY (maQuyDoi) REFERENCES DonViQuyDoi(maQuyDoi),
    FOREIGN KEY (maLoThuoc) REFERENCES LoThuoc(maLoThuoc)
);
GO
PRINT N'Đã tạo xong Database QuanLyNhaThuoc_LongNguyen xịn xò con bò cho sếp!';
GO

-- ==========================================
-- DỮ LIỆU KHỞI TẠO (DATA)
-- ==========================================
USE QuanLyNhaThuoc_LongNguyen;
GO

-- 1. NHAN VIEN
INSERT INTO NhanVien (maNhanVien, tenDangNhap, matKhau, hoTen, chucVu, caLamViec, sdt) VALUES
('NV001', 'admin', '123456', N'Lê Trọng Nghĩa', N'Quản Lý', N'Hành Chính', '0987654321'),
('NV002', 'nhanvien', '123456', N'Trần Thị Thu Trang', N'Nhân Viên', N'Ca Sáng', '0912345678');
GO

INSERT INTO KhachHang (maKhachHang, hoTen, sdt, diaChi, diemTichLuy) VALUES
('KH001', N'Trần Xuân Bách', '0995204225', N'Số 248 Lê Lợi, Quận 1, Hà Nội', 457),
('KH002', N'Nguyễn Ngọc Lan', '0992729194', N'Số 767 Điện Biên Phủ, Quận 10, TP. Hồ Chí Minh', 166),
('KH003', N'Phạm Minh Tuấn', '0970722282', N'Số 695 Nguyễn Trãi, Quận 5, TP. Hồ Chí Minh', 274),
('KH004', N'Lê Thu Trà', '0987661781', N'Số 900 Lê Duẩn, Quận 3, Hà Nội', 262),
('KH005', N'Hoàng Văn Cường', '0927063750', N'Số 654 Lê Duẩn, Quận 3, Đà Nẵng', 113),
('KH006', N'Vũ Bích Ngọc', '0913097253', N'Số 735 Điện Biên Phủ, Quận 5, Đà Nẵng', 70),
('KH007', N'Đinh Đức Mạnh', '0975744045', N'Số 565 Lê Lợi, Quận 1, Đà Nẵng', 355),
('KH008', N'Trịnh Hương Giang', '0927517238', N'Số 399 Nguyễn Trãi, Quận 1, Đà Nẵng', 276);
GO

INSERT INTO DanhMucThuoc (maDanhMuc, tenDanhMuc, moTa) VALUES
('DM001', N'Kháng sinh', N'Các loại thuốc thuộc nhóm Kháng sinh'),
('DM002', N'Giảm đau hạ sốt', N'Các loại thuốc thuộc nhóm Giảm đau hạ sốt'),
('DM003', N'Vitamin - Khoáng chất', N'Các loại thuốc thuộc nhóm Vitamin - Khoáng chất'),
('DM004', N'Dạ dày', N'Các loại thuốc thuộc nhóm Dạ dày'),
('DM005', N'Thuốc bôi ngoài da', N'Các loại thuốc thuộc nhóm Thuốc bôi ngoài da'),
('DM006', N'Thực phẩm chức năng', N'Các loại thuốc thuộc nhóm Thực phẩm chức năng'),
('DM007', N'Hô hấp', N'Các loại thuốc thuộc nhóm Hô hấp'),
('DM008', N'Tim mạch', N'Các loại thuốc thuộc nhóm Tim mạch'),
('DM009', N'Khác', N'Các loại thuốc thuộc nhóm Khác');
GO

INSERT INTO NhaCungCap (maNhaCungCap, tenNhaCungCap, sdt, diaChi, congNo) VALUES
('NCC0376', N'URSA Pharm', '0958317091', N'Số 951 Điện Biên Phủ, Quận 5, Đà Nẵng', 0),
('NCC0166', N'Allergan India Pvt Ltd', '0996065618', N'Số 467 Điện Biên Phủ, Quận 3, Hà Nội', 0),
('NCC0380', N'Lavue Pharmaceuticals', '0976827768', N'Số 293 Lê Duẩn, Quận 10, TP. Hồ Chí Minh', 0),
('NCC0241', N'Dermakare Pharmaceuticals Pvt Ltd', '0922558952', N'Số 601 Điện Biên Phủ, Bình Thạnh, Đà Nẵng', 0),
('NCC0070', N'Vestal Healthcare', '0981871256', N'Số 196 Nguyễn Trãi, Quận 3, Hà Nội', 0),
('NCC0391', N'Mac Laboratories Ltd', '0996189736', N'Số 324 Điện Biên Phủ, Tân Bình, TP. Hồ Chí Minh', 0),
('NCC0111', N'Bharat Serums & Vaccines Ltd', '0999899067', N'Số 53 Trần Hưng Đạo, Tân Bình, Hà Nội', 0),
('NCC0322', N'Torque Pharmaceuticals Pvt Ltd', '0997333282', N'Số 183 Lê Duẩn, Tân Bình, TP. Hồ Chí Minh', 0),
('NCC0055', N'Delcure Life Sciences', '0926214382', N'Số 904 Trần Hưng Đạo, Quận 10, Hà Nội', 0),
('NCC0294', N'Profic Organic Ltd', '0919661157', N'Số 540 Lê Duẩn, Bình Thạnh, Đà Nẵng', 0),
('NCC0362', N'Nexgen Rx Life Science Pvt Ltd', '0905818050', N'Số 385 Điện Biên Phủ, Tân Bình, TP. Hồ Chí Minh', 0),
('NCC0191', N'Alcon Laboratories', '0912055388', N'Số 403 Điện Biên Phủ, Quận 1, Đà Nẵng', 0),
('NCC0182', N'Skinska Pharmaceutica Pvt Ltd', '0927604207', N'Số 437 Hai Bà Trưng, Bình Thạnh, Hà Nội', 0),
('NCC0003', N'Mitoch Pharma Pvt Ltd', '0960613624', N'Số 114 Lê Lợi, Quận 10, Hà Nội', 0),
('NCC0226', N'MMC Healthcare Ltd', '0900527383', N'Số 158 Lê Lợi, Quận 5, Đà Nẵng', 0);
GO

INSERT INTO Thuoc (maThuoc, maDanhMuc, tenThuoc, hoatChat, hamLuong, hangSanXuat, nuocSanXuat, congDung, trieuChung, donViCoBan, hinhAnh, canKeDon, trangThai) VALUES
('T01360', 'DM005', N'Doris Tablet', N'Ethinyl Estradiol (0.03mg) + Drospirenone (3mg)', N'0.03mg', N'Pfizer Ltd', N'Việt Nam', N'Tránh thai', N'Hỗ trợ kế hoạch hóa gia đình', N'Viên', 'T01360.jpg', 0, 'DANG_BAN'),
('T02541', 'DM001', N'Lupigest SR 300 Tablet', N'Progesterone (Natural Micronized) (300mg)', N'300mg', N'Lupin Ltd', N'Mỹ', N'Bổ sung dưỡng chất', N'Suy nhược, mệt mỏi', N'Viên', 'T02541.jpg', 1, 'DANG_BAN'),
('T01657', 'DM008', N'Plosat gm Cream', N'Clobetasol (0.05% w/w) + Miconazole (2% w/w) + Neomycin (0.5% w/w)', N'0.05%', N'Winsome Laboratories Pvt Ltd', N'Pháp', N'Bổ sung dưỡng chất', N'Suy nhược, mệt mỏi', N'Tuýp', 'T01657.jpg', 0, 'DANG_BAN'),
('T00556', 'DM001', N'Levokast Oral Suspension', N'Levocetirizine (2.5mg/5ml) + Montelukast (4mg/5ml)', N'2.5mg', N'TTK Healthcare Ltd', N'Ấn Độ', N'Sốt', N'Thân nhiệt cao, ớn lạnh, mệt mỏi', N'Chai', 'T00556.jpg', 0, 'DANG_BAN'),
('T00045', 'DM003', N'Pantodac-IT Capsule SR', N'Pantoprazole (40mg) + Itopride (150mg)', N'40mg', N'Zydus Cadila', N'Pháp', N'Trào ngược dạ dày thực quản', N'Ợ hơi, ợ chua, buồn nôn, tức ngực', N'Viên', 'T00045.jpg', 0, 'DANG_BAN'),
('T00655', 'DM008', N'Revlin M Capsule', N'Methylcobalamin (750mcg) + Pregabalin (75mg)', N'750mcg', N'Eris Lifesciences Ltd', N'Đức', N'Bổ sung dưỡng chất', N'Suy nhược, mệt mỏi', N'Viên', 'T00655.jpg', 1, 'DANG_BAN'),
('T00931', 'DM009', N'Synoflex Tablet', N'Diacerein (50mg) + Glucosamine Sulfate Potassium Chloride (750mg) + Methyl Sulfonyl Methane (250mg)', N'50mg', N'Gms Biomax Medicare Pvt Ltd', N'Đức', N'Ợ nóng', N'Nóng rát thực quản', N'Viên', 'T00931.jpg', 1, 'DANG_BAN'),
('T01877', 'DM003', N'Sebifin Tablet', N'Terbinafine (250mg)', N'250mg', N'Sun Pharmaceutical Industries Ltd', N'Nhật Bản', N'Nhiễm nấm', N'Ngứa ngáy, nổi mẩn đỏ có vảy', N'Viên', 'T01877.jpg', 0, 'DANG_BAN'),
('T02729', 'DM001', N'Enzictra - DS Tablet', N'Bromelain (180mg) + Trypsin (96mg) + Rutoside (200mg) + Papain (120mg)', N'180mg', N'Alembic Pharmaceuticals Ltd', N'Pháp', N'Bổ sung dưỡng chất', N'Suy nhược, mệt mỏi', N'Viên', 'T02729.jpg', 1, 'DANG_BAN'),
('T01011', 'DM001', N'Hhfexo M 10mg/120mg Tablet', N'Montelukast (10mg) + Fexofenadine (120mg)', N'10mg', N'Hegde and Hegde Pharmaceutical LLP', N'Nhật Bản', N'Dị ứng', N'Phát ban, mẩn ngứa, hắt hơi liên tục', N'Viên', 'T01011.jpg', 0, 'DANG_BAN'),
('T01264', 'DM008', N'Zedex Cough Syrup', N'Chlorpheniramine Maleate (2mg) + Dextromethorphan Hydrobromide (10mg)', N'2mg', N'Dr Reddy''s Laboratories Ltd', N'Nhật Bản', N'Bổ sung dưỡng chất', N'Suy nhược, mệt mỏi', N'Chai', 'T01264.jpg', 0, 'DANG_BAN'),
('T01367', 'DM001', N'Melabest Cream', N'Hydroquinone (2% w/w) + Mometasone (0.1% w/w) + Tretinoin (0.025% w/w)', N'2%', N'Mankind Pharma Ltd', N'Hàn Quốc', N'Bổ sung dưỡng chất', N'Suy nhược, mệt mỏi', N'Tuýp', 'T01367.jpg', 0, 'DANG_BAN'),
('T00639', 'DM003', N'Tidomet Plus Tablet', N'Levodopa (100mg) + Carbidopa (25mg)', N'100mg', N'Torrent Pharmaceuticals Ltd', N'Hàn Quốc', N'Điều trị bệnh Parkinson', N'Chóng mặt, buồn nôn, khô miệng', N'Viên', 'T00639.jpg', 1, 'DANG_BAN'),
('T02234', 'DM002', N'Fxla 180 Tablet', N'Fexofenadine (180mg)', N'180mg', N'Rockmed Pharma Pvt. Ltd.', N'Nhật Bản', N'Dị ứng', N'Phát ban, mẩn ngứa, hắt hơi liên tục', N'Viên', 'T02234.png', 0, 'DANG_BAN'),
('T02296', 'DM009', N'Asthalin Plus Expectorant', N'Ambroxol (15mg/5ml) + Levosalbutamol (0.5mg/5ml) + Guaifenesin (50mg/5ml)', N'15mg', N'Cipla Ltd', N'Việt Nam', N'Ho có đờm', N'Ho kéo dài, rát họng, khạc đờm', N'Hộp', 'T02296.jpg', 0, 'DANG_BAN'),
('T02319', 'DM001', N'M-Solvin Tablet', N'Ambroxol (30mg) + Guaifenesin (100mg) + Terbutaline (2.5mg)', N'30mg', N'Ipca Laboratories Ltd', N'Hàn Quốc', N'Ho có đờm', N'Ho kéo dài, rát họng, khạc đờm', N'Viên', 'T02319.jpg', 0, 'DANG_BAN'),
('T01435', 'DM003', N'Rabidoc LS 75mg/20mg Capsule', N'Levosulpiride (75mg) + Rabeprazole (20mg)', N'75mg', N'Morepen Laboratories Ltd', N'Ấn Độ', N'Trào ngược dạ dày thực quản', N'Ợ hơi, ợ chua, buồn nôn, tức ngực', N'Viên', 'T01435.png', 0, 'DANG_BAN'),
('T02394', 'DM002', N'Ara Eye Drop', N'Hydroxypropylmethylcellulose (0.3% w/v) + Glycerin (0.2% w/v) + Dextran 70 (0.1% w/v)', N'0.3%', N'Sunways India Pvt Ltd', N'Mỹ', N'Bổ sung dưỡng chất', N'Suy nhược, mệt mỏi', N'Lọ', 'T02394.jpg', 0, 'DANG_BAN'),
('T01119', 'DM004', N'Depakote XR 250 Tablet', N'Divalproex (250mg)', N'250mg', N'Sanofi India Ltd', N'Đức', N'Bổ sung dưỡng chất', N'Suy nhược, mệt mỏi', N'Viên', 'T01119.jpg', 0, 'DANG_BAN'),
('T00262', 'DM005', N'Hhcefi 200 Tablet', N'Cefixime (200mg)', N'200mg', N'Hegde and Hegde Pharmaceutical LLP', N'Đức', N'Nhiễm khuẩn', N'Sưng viêm, nóng đỏ, đau nhức', N'Viên', 'T00262.jpg', 0, 'DANG_BAN'),
('T02594', 'DM008', N'Metsmall 500 Tablet SR', N'Metformin (500mg)', N'500mg', N'Dr Reddy''s Laboratories Ltd', N'Hàn Quốc', N'Tiểu đường tuýp 2', N'Mệt mỏi, khát nước, sụt cân', N'Viên', 'T02594.jpg', 0, 'DANG_BAN'),
('T02381', 'DM003', N'Tyza Dusting Powder', N'Terbinafine (1% w/w)', N'1%', N'Abbott', N'Nhật Bản', N'Bổ sung dưỡng chất', N'Suy nhược, mệt mỏi', N'Gói', 'T02381.jpg', 0, 'DANG_BAN'),
('T01977', 'DM002', N'LTK 50 Tablet', N'Losartan (50mg)', N'50mg', N'Unison Pharmaceuticals Pvt Ltd', N'Hàn Quốc', N'Huyết áp cao', N'Chóng mặt, đau đầu, hồi hộp', N'Viên', 'T01977.jpg', 0, 'DANG_BAN'),
('T02247', 'DM006', N'Minolox 50 Tablet', N'Minocycline (50mg)', N'50mg', N'Micro Labs Ltd', N'Ấn Độ', N'Nhiễm khuẩn', N'Sưng viêm, nóng đỏ, đau nhức', N'Viên', 'T02247.jpg', 0, 'DANG_BAN'),
('T00504', 'DM009', N'Melapik Plus Cream', N'Hydroquinone (2% w/w) + Tretinoin (0.025% w/w) + Fluocinolone acetonide (0.01% w/w)', N'2%', N'KLM Laboratories Pvt Ltd', N'Hàn Quốc', N'Bổ sung dưỡng chất', N'Suy nhược, mệt mỏi', N'Tuýp', 'T00504.jpg', 1, 'DANG_BAN'),
('T02811', 'DM008', N'Racedot 100mg Capsule', N'Racecadotril (100mg)', N'100mg', N'Macleods Pharmaceuticals Pvt Ltd', N'Mỹ', N'Bổ sung dưỡng chất', N'Suy nhược, mệt mỏi', N'Viên', 'T02811.jpg', 1, 'DANG_BAN'),
('T01365', 'DM006', N'Zytee RB Gel', N'Choline Salicylate (9% w/v)', N'9%', N'Raptakos Brett & Co Ltd', N'Mỹ', N'Bổ sung dưỡng chất', N'Suy nhược, mệt mỏi', N'Tuýp', 'T01365.jpg', 0, 'DANG_BAN'),
('T02413', 'DM004', N'Moxclav 375 Tablet', N'Amoxycillin  (250mg) +  Clavulanic Acid (125mg)', N'250mg', N'Sun Pharmaceutical Industries Ltd', N'Ấn Độ', N'Nhiễm khuẩn', N'Sưng viêm, nóng đỏ, đau nhức', N'Viên', 'T02413.jpg', 0, 'DANG_BAN'),
('T01551', 'DM005', N'Nidagen 100 Capsule', N'Progesterone (Natural Micronized) (100mg)', N'100mg', N'Systopic Laboratories Pvt Ltd', N'Đức', N'Bổ sung dưỡng chất', N'Suy nhược, mệt mỏi', N'Viên', 'T01551.jpg', 0, 'DANG_BAN'),
('T02239', 'DM009', N'Rivatane 20 Tablet', N'Rivaroxaban (20mg)', N'20mg', N'Medley Pharmaceuticals', N'Mỹ', N'Bổ sung dưỡng chất', N'Suy nhược, mệt mỏi', N'Viên', 'T02239.jpg', 0, 'DANG_BAN'),
('T02363', 'DM005', N'Lacne 10 Capsule', N'Isotretinoin (10mg)', N'10mg', N'La Pristine Bioceuticals Pvt Ltd', N'Nhật Bản', N'Bổ sung dưỡng chất', N'Suy nhược, mệt mỏi', N'Viên', 'T02363.jpg', 1, 'DANG_BAN'),
('T00840', 'DM009', N'Monocef 2gm Injection', N'Ceftriaxone (2gm)', N'2g', N'Aristo Pharmaceuticals Pvt Ltd', N'Mỹ', N'Nhiễm khuẩn', N'Sưng viêm, nóng đỏ, đau nhức', N'Ống', 'T00840.jpg', 1, 'DANG_BAN'),
('T01538', 'DM009', N'Eptoin 300 ER Tablet', N'Phenytoin (300mg)', N'300mg', N'Abbott', N'Ấn Độ', N'Bổ sung dưỡng chất', N'Suy nhược, mệt mỏi', N'Viên', 'T01538.jpg', 1, 'DANG_BAN'),
('T01191', 'DM002', N'Depotex 4mg Tablet', N'Methylprednisolone (4mg)', N'4mg', N'Zydus Cadila', N'Mỹ', N'Bổ sung dưỡng chất', N'Suy nhược, mệt mỏi', N'Viên', 'T01191.jpg', 0, 'DANG_BAN'),
('T01179', 'DM001', N'Arip MT 15 Tablet', N'Aripiprazole (15mg)', N'15mg', N'Torrent Pharmaceuticals Ltd', N'Pháp', N'Bổ sung dưỡng chất', N'Suy nhược, mệt mỏi', N'Viên', 'T01179.jpg', 1, 'DANG_BAN'),
('T01774', 'DM004', N'Povidot 10% Ointment', N'Povidone Iodine (10% w/w)', N'10%', N'Leeford Healthcare Ltd', N'Mỹ', N'Bổ sung dưỡng chất', N'Suy nhược, mệt mỏi', N'Tuýp', 'T01774.jpg', 0, 'DANG_BAN'),
('T02883', 'DM006', N'Fevago 120mg Oral Suspension Strawberry', N'Paracetamol (120mg/5ml)', N'120mg', N'Cipla Ltd', N'Mỹ', N'Giảm đau', N'Đau đầu, đau cơ, nhức mỏi', N'Chai', 'T02883.jpg', 0, 'DANG_BAN'),
('T02671', 'DM003', N'Betacard 25 Tablet', N'Atenolol (25mg)', N'25mg', N'Torrent Pharmaceuticals Ltd', N'Pháp', N'Huyết áp cao', N'Chóng mặt, đau đầu, hồi hộp', N'Viên', 'T02671.jpg', 0, 'DANG_BAN'),
('T00653', 'DM003', N'J-Ring M Forte Tablet ER', N'Metformin (1000mg) + Teneligliptin (20mg)', N'1000mg', N'Indoco Remedies Ltd', N'Đức', N'Tiểu đường tuýp 2', N'Mệt mỏi, khát nước, sụt cân', N'Viên', 'T00653.jpg', 0, 'DANG_BAN'),
('T00731', 'DM006', N'Asthalin Syrup', N'Salbutamol (2mg/5ml)', N'2mg', N'Cipla Ltd', N'Việt Nam', N'Hen suyễn', N'Khó thở, thở khò khè, tức ngực', N'Chai', 'T00731.jpg', 0, 'DANG_BAN'),
('T00276', 'DM002', N'Zyrova 20 Tablet', N'Rosuvastatin (20mg)', N'20mg', N'Zydus Cadila', N'Mỹ', N'Cholesterol cao', N'Phòng ngừa nhồi máu cơ tim, đột quỵ', N'Viên', 'T00276.jpg', 0, 'DANG_BAN'),
('T01820', 'DM007', N'XREL Tablet', N'Aceclofenac (100mg) + Paracetamol (325mg)', N'100mg', N'Deys Medical', N'Mỹ', N'Giảm đau', N'Đau đầu, đau cơ, nhức mỏi', N'Viên', 'T01820.jpg', 0, 'DANG_BAN'),
('T00486', 'DM003', N'Voglibite 0.2 Tablet', N'Voglibose (0.2mg)', N'0.2mg', N'Corona Remedies Pvt Ltd', N'Mỹ', N'Tiểu đường tuýp 2', N'Mệt mỏi, khát nước, sụt cân', N'Viên', 'T00486.jpg', 0, 'DANG_BAN'),
('T00194', 'DM001', N'Telekast-Plus Tablet', N'Bambuterol (10mg) + Montelukast (10mg)', N'10mg', N'Lupin Ltd', N'Ấn Độ', N'Sốt', N'Thân nhiệt cao, ớn lạnh, mệt mỏi', N'Viên', 'T00194.jpg', 0, 'DANG_BAN'),
('T00742', 'DM002', N'Trigli 2 Tablet SR', N'Glimepiride (2mg) + Metformin (500mg) + Pioglitazone (15mg)', N'2mg', N'Ordain Health Care Global Pvt Ltd', N'Pháp', N'Tiểu đường tuýp 2', N'Mệt mỏi, khát nước, sụt cân', N'Viên', 'T00742.jpg', 0, 'DANG_BAN'),
('T00847', 'DM001', N'Etoro 90mg Tablet', N'Etoricoxib (90mg)', N'90mg', N'Hetero Drugs Ltd', N'Nhật Bản', N'Giảm đau', N'Đau đầu, đau cơ, nhức mỏi', N'Viên', 'T00847.jpg', 0, 'DANG_BAN'),
('T02945', 'DM002', N'HHLEVO Tablet', N'Levocetirizine (5mg)', N'5mg', N'Hegde and Hegde Pharmaceutical LLP', N'Pháp', N'Bổ sung dưỡng chất', N'Suy nhược, mệt mỏi', N'Viên', 'T02945.jpg', 1, 'DANG_BAN'),
('T00587', 'DM003', N'Saril Tablet', N'Ofloxacin (200mg) + Ornidazole (500mg)', N'200mg', N'Shreya Life Sciences Pvt Ltd', N'Việt Nam', N'Bổ sung dưỡng chất', N'Suy nhược, mệt mỏi', N'Viên', 'T00587.jpg', 0, 'DANG_BAN'),
('T01673', 'DM004', N'Pacimol Active Tablet', N'Caffeine (50mg) + Paracetamol (650mg)', N'50mg', N'Ipca Laboratories Ltd', N'Pháp', N'Bổ sung dưỡng chất', N'Suy nhược, mệt mỏi', N'Viên', 'T01673.jpg', 0, 'DANG_BAN'),
('T01802', 'DM006', N'Zoster 800 Tablet', N'Acyclovir (800mg)', N'800mg', N'Leeford Healthcare Ltd', N'Ấn Độ', N'Sốt', N'Thân nhiệt cao, ớn lạnh, mệt mỏi', N'Viên', 'T01802.jpg', 1, 'DANG_BAN');
GO

INSERT INTO DonViQuyDoi (maQuyDoi, maThuoc, tenDonVi, tyLeQuyDoi, giaBan) VALUES
('QD00045', 'T00045', N'Viên', 1, 423000),
('QD00051', 'T00045', N'Vỉ', 10, 4230000),
('QD00052', 'T00045', N'Hộp', 100, 42300000),
('QD00194', 'T00194', N'Viên', 1, 359000),
('QD00053', 'T00194', N'Vỉ', 10, 3590000),
('QD00054', 'T00194', N'Hộp', 100, 35900000),
('QD00262', 'T00262', N'Viên', 1, 295000),
('QD00055', 'T00262', N'Vỉ', 10, 2950000),
('QD00056', 'T00262', N'Hộp', 100, 29500000),
('QD00276', 'T00276', N'Viên', 1, 399000),
('QD00057', 'T00276', N'Vỉ', 10, 3990000),
('QD00058', 'T00276', N'Hộp', 100, 39900000),
('QD00486', 'T00486', N'Viên', 1, 390000),
('QD00059', 'T00486', N'Vỉ', 10, 3900000),
('QD00060', 'T00486', N'Hộp', 100, 39000000),
('QD00504', 'T00504', N'Tuýp', 1, 172000),
('QD00061', 'T00504', N'Hộp', 5, 860000),
('QD00556', 'T00556', N'Chai', 1, 141000),
('QD00062', 'T00556', N'Hộp', 5, 705000),
('QD00587', 'T00587', N'Viên', 1, 438000),
('QD00063', 'T00587', N'Vỉ', 10, 4380000),
('QD00064', 'T00587', N'Hộp', 100, 43800000),
('QD00639', 'T00639', N'Viên', 1, 119000),
('QD00065', 'T00639', N'Vỉ', 10, 1190000),
('QD00066', 'T00639', N'Hộp', 100, 11900000),
('QD00653', 'T00653', N'Viên', 1, 49000),
('QD00067', 'T00653', N'Vỉ', 10, 490000),
('QD00068', 'T00653', N'Hộp', 100, 4900000),
('QD00655', 'T00655', N'Viên', 1, 343000),
('QD00069', 'T00655', N'Vỉ', 10, 3430000),
('QD00070', 'T00655', N'Hộp', 100, 34300000),
('QD00731', 'T00731', N'Chai', 1, 489000),
('QD00071', 'T00731', N'Hộp', 5, 2445000),
('QD00742', 'T00742', N'Viên', 1, 334000),
('QD00072', 'T00742', N'Vỉ', 10, 3340000),
('QD00073', 'T00742', N'Hộp', 100, 33400000),
('QD00840', 'T00840', N'Ống', 1, 272000),
('QD00074', 'T00840', N'Hộp', 5, 1360000),
('QD00847', 'T00847', N'Viên', 1, 429000),
('QD00075', 'T00847', N'Vỉ', 10, 4290000),
('QD00076', 'T00847', N'Hộp', 100, 42900000),
('QD00931', 'T00931', N'Viên', 1, 361000),
('QD00077', 'T00931', N'Vỉ', 10, 3610000),
('QD00078', 'T00931', N'Hộp', 100, 36100000),
('QD01011', 'T01011', N'Viên', 1, 454000),
('QD00079', 'T01011', N'Vỉ', 10, 4540000),
('QD00080', 'T01011', N'Hộp', 100, 45400000),
('QD01119', 'T01119', N'Viên', 1, 337000),
('QD00081', 'T01119', N'Vỉ', 10, 3370000),
('QD00082', 'T01119', N'Hộp', 100, 33700000),
('QD01179', 'T01179', N'Viên', 1, 28000),
('QD00083', 'T01179', N'Vỉ', 10, 280000),
('QD00084', 'T01179', N'Hộp', 100, 2800000),
('QD01191', 'T01191', N'Viên', 1, 220000),
('QD00085', 'T01191', N'Vỉ', 10, 2200000),
('QD00086', 'T01191', N'Hộp', 100, 22000000),
('QD01264', 'T01264', N'Chai', 1, 435000),
('QD00087', 'T01264', N'Hộp', 5, 2175000),
('QD01360', 'T01360', N'Viên', 1, 108000),
('QD00088', 'T01360', N'Vỉ', 10, 1080000),
('QD00089', 'T01360', N'Hộp', 100, 10800000),
('QD01365', 'T01365', N'Tuýp', 1, 378000),
('QD00090', 'T01365', N'Hộp', 5, 1890000),
('QD01367', 'T01367', N'Tuýp', 1, 125000),
('QD00091', 'T01367', N'Hộp', 5, 625000),
('QD01435', 'T01435', N'Viên', 1, 288000),
('QD00092', 'T01435', N'Vỉ', 10, 2880000),
('QD00093', 'T01435', N'Hộp', 100, 28800000),
('QD01538', 'T01538', N'Viên', 1, 452000),
('QD00094', 'T01538', N'Vỉ', 10, 4520000),
('QD00095', 'T01538', N'Hộp', 100, 45200000),
('QD01551', 'T01551', N'Viên', 1, 65000),
('QD00096', 'T01551', N'Vỉ', 10, 650000),
('QD00097', 'T01551', N'Hộp', 100, 6500000),
('QD01657', 'T01657', N'Tuýp', 1, 120000),
('QD00098', 'T01657', N'Hộp', 5, 600000),
('QD01673', 'T01673', N'Viên', 1, 36000),
('QD00099', 'T01673', N'Vỉ', 10, 360000),
('QD00100', 'T01673', N'Hộp', 100, 3600000),
('QD01774', 'T01774', N'Tuýp', 1, 43000),
('QD00101', 'T01774', N'Hộp', 5, 215000),
('QD01802', 'T01802', N'Viên', 1, 403000),
('QD00102', 'T01802', N'Vỉ', 10, 4030000),
('QD00103', 'T01802', N'Hộp', 100, 40300000),
('QD01820', 'T01820', N'Viên', 1, 367000),
('QD00104', 'T01820', N'Vỉ', 10, 3670000),
('QD00105', 'T01820', N'Hộp', 100, 36700000),
('QD01877', 'T01877', N'Viên', 1, 101000),
('QD00106', 'T01877', N'Vỉ', 10, 1010000),
('QD00107', 'T01877', N'Hộp', 100, 10100000),
('QD01977', 'T01977', N'Viên', 1, 269000),
('QD00108', 'T01977', N'Vỉ', 10, 2690000),
('QD00109', 'T01977', N'Hộp', 100, 26900000),
('QD02234', 'T02234', N'Viên', 1, 251000),
('QD00110', 'T02234', N'Vỉ', 10, 2510000),
('QD00111', 'T02234', N'Hộp', 100, 25100000),
('QD02239', 'T02239', N'Viên', 1, 210000),
('QD00112', 'T02239', N'Vỉ', 10, 2100000),
('QD00113', 'T02239', N'Hộp', 100, 21000000),
('QD02247', 'T02247', N'Viên', 1, 129000),
('QD00114', 'T02247', N'Vỉ', 10, 1290000),
('QD00115', 'T02247', N'Hộp', 100, 12900000),
('QD02296', 'T02296', N'Hộp', 1, 55000),
('QD02319', 'T02319', N'Viên', 1, 132000),
('QD00116', 'T02319', N'Vỉ', 10, 1320000),
('QD00117', 'T02319', N'Hộp', 100, 13200000),
('QD02363', 'T02363', N'Viên', 1, 242000),
('QD00118', 'T02363', N'Vỉ', 10, 2420000),
('QD00119', 'T02363', N'Hộp', 100, 24200000),
('QD02381', 'T02381', N'Gói', 1, 448000),
('QD02394', 'T02394', N'Lọ', 1, 378000),
('QD00120', 'T02394', N'Hộp', 5, 1890000),
('QD02413', 'T02413', N'Viên', 1, 343000),
('QD00121', 'T02413', N'Vỉ', 10, 3430000),
('QD00122', 'T02413', N'Hộp', 100, 34300000),
('QD02541', 'T02541', N'Viên', 1, 101000),
('QD00123', 'T02541', N'Vỉ', 10, 1010000),
('QD00124', 'T02541', N'Hộp', 100, 10100000),
('QD02594', 'T02594', N'Viên', 1, 77000),
('QD00125', 'T02594', N'Vỉ', 10, 770000),
('QD00126', 'T02594', N'Hộp', 100, 7700000),
('QD02671', 'T02671', N'Viên', 1, 74000),
('QD00127', 'T02671', N'Vỉ', 10, 740000),
('QD00128', 'T02671', N'Hộp', 100, 7400000),
('QD02729', 'T02729', N'Viên', 1, 273000),
('QD00129', 'T02729', N'Vỉ', 10, 2730000),
('QD00130', 'T02729', N'Hộp', 100, 27300000),
('QD02811', 'T02811', N'Viên', 1, 488000),
('QD00131', 'T02811', N'Vỉ', 10, 4880000),
('QD00132', 'T02811', N'Hộp', 100, 48800000),
('QD02883', 'T02883', N'Chai', 1, 277000),
('QD00133', 'T02883', N'Hộp', 5, 1385000),
('QD02945', 'T02945', N'Viên', 1, 348000),
('QD00134', 'T02945', N'Vỉ', 10, 3480000),
('QD00135', 'T02945', N'Hộp', 100, 34800000);
GO


INSERT INTO LoThuoc (maLoThuoc, maThuoc, ngaySanXuat, hanSuDung, soLuongTon, giaNhap, viTriKho) VALUES
('LO00045', 'T00045', '2025-03-20', '2027-09-16', 477, 296100, 'KHO_BAN_HANG'),
('LO00194', 'T00194', '2025-09-08', '2028-04-24', 451, 251299, 'KHO_DU_TRU'),
('LO00262', 'T00262', '2025-05-12', '2027-05-01', 303, 206500, 'KHO_BAN_HANG'),
('LO00276', 'T00276', '2024-11-02', '2027-01-27', 146, 279300, 'KHO_DU_TRU'),
('LO00486', 'T00486', '2024-12-13', '2027-01-14', 383, 273000, 'KHO_DU_TRU'),
('LO00504', 'T00504', '2025-11-02', '2027-12-21', 181, 120399, 'KHO_BAN_HANG'),
('LO00556', 'T00556', '2025-07-24', '2028-02-08', 418, 98700, 'KHO_BAN_HANG'),
('LO00587', 'T00587', '2025-07-22', '2027-12-24', 443, 306600, 'KHO_DU_TRU'),
('LO00639', 'T00639', '2024-10-26', '2026-12-30', 234, 83300, 'KHO_DU_TRU'),
('LO00653', 'T00653', '2024-12-28', '2027-06-18', 434, 34300, 'KHO_DU_TRU'),
('LO00655', 'T00655', '2025-03-10', '2027-04-13', 337, 240099, 'KHO_BAN_HANG'),
('LO00731', 'T00731', '2025-07-26', '2028-04-01', 200, 342300, 'KHO_DU_TRU'),
('LO00742', 'T00742', '2024-12-05', '2027-01-18', 416, 233799, 'KHO_BAN_HANG'),
('LO00840', 'T00840', '2024-11-09', '2027-03-27', 367, 190400, 'KHO_BAN_HANG'),
('LO00847', 'T00847', '2024-11-03', '2026-11-27', 478, 300300, 'KHO_DU_TRU'),
('LO00931', 'T00931', '2024-11-19', '2027-02-19', 57, 252699, 'KHO_DU_TRU'),
('LO01011', 'T01011', '2024-11-23', '2027-06-26', 284, 317800, 'KHO_BAN_HANG'),
('LO01119', 'T01119', '2024-12-28', '2027-01-15', 101, 235899, 'KHO_BAN_HANG'),
('LO01179', 'T01179', '2025-05-27', '2027-10-18', 187, 19600, 'KHO_DU_TRU'),
('LO01191', 'T01191', '2025-06-25', '2027-06-23', 172, 154000, 'KHO_BAN_HANG'),
('LO01264', 'T01264', '2025-05-30', '2027-09-12', 490, 304500, 'KHO_BAN_HANG'),
('LO01360', 'T01360', '2025-01-23', '2027-06-11', 56, 75600, 'KHO_DU_TRU'),
('LO01365', 'T01365', '2025-07-24', '2028-03-10', 476, 264600, 'KHO_BAN_HANG'),
('LO01367', 'T01367', '2025-04-17', '2027-07-01', 359, 87500, 'KHO_DU_TRU'),
('LO01435', 'T01435', '2025-05-10', '2027-12-26', 149, 201600, 'KHO_DU_TRU'),
('LO01538', 'T01538', '2025-03-31', '2027-05-23', 476, 316400, 'KHO_DU_TRU'),
('LO01551', 'T01551', '2025-03-10', '2027-10-01', 444, 45500, 'KHO_BAN_HANG'),
('LO01657', 'T01657', '2025-10-19', '2028-06-27', 358, 84000, 'KHO_DU_TRU'),
('LO01673', 'T01673', '2024-11-12', '2027-03-01', 54, 25200, 'KHO_BAN_HANG'),
('LO01774', 'T01774', '2025-09-17', '2028-02-13', 136, 30099, 'KHO_BAN_HANG'),
('LO01802', 'T01802', '2025-05-29', '2027-05-16', 315, 282100, 'KHO_BAN_HANG'),
('LO01820', 'T01820', '2025-06-04', '2027-07-07', 385, 256899, 'KHO_BAN_HANG'),
('LO01877', 'T01877', '2025-06-18', '2027-07-15', 404, 70700, 'KHO_BAN_HANG'),
('LO01977', 'T01977', '2025-05-16', '2027-11-19', 334, 188300, 'KHO_BAN_HANG'),
('LO02234', 'T02234', '2025-11-07', '2028-02-21', 449, 175700, 'KHO_DU_TRU'),
('LO02239', 'T02239', '2025-05-15', '2027-08-11', 390, 147000, 'KHO_DU_TRU'),
('LO02247', 'T02247', '2024-12-21', '2027-08-29', 368, 90300, 'KHO_DU_TRU'),
('LO02296', 'T02296', '2025-06-09', '2027-12-27', 220, 38500, 'KHO_BAN_HANG'),
('LO02319', 'T02319', '2025-09-06', '2027-11-12', 222, 92400, 'KHO_DU_TRU'),
('LO02363', 'T02363', '2025-06-08', '2027-10-05', 343, 169400, 'KHO_DU_TRU'),
('LO02381', 'T02381', '2025-04-05', '2027-07-30', 260, 313600, 'KHO_DU_TRU'),
('LO02394', 'T02394', '2024-12-23', '2026-12-31', 89, 264600, 'KHO_DU_TRU'),
('LO02413', 'T02413', '2025-11-18', '2028-07-13', 83, 240099, 'KHO_BAN_HANG'),
('LO02541', 'T02541', '2025-02-10', '2027-11-05', 191, 70700, 'KHO_BAN_HANG'),
('LO02594', 'T02594', '2025-10-02', '2028-03-06', 383, 53900, 'KHO_BAN_HANG'),
('LO02671', 'T02671', '2025-07-27', '2028-03-29', 101, 51800, 'KHO_BAN_HANG'),
('LO02729', 'T02729', '2025-05-10', '2027-07-08', 381, 191100, 'KHO_BAN_HANG'),
('LO02811', 'T02811', '2025-07-29', '2027-07-31', 93, 341600, 'KHO_DU_TRU'),
('LO02883', 'T02883', '2025-09-04', '2028-02-27', 90, 193900, 'KHO_BAN_HANG'),
('LO02945', 'T02945', '2025-08-02', '2028-02-21', 500, 243599, 'KHO_BAN_HANG');
GO

INSERT INTO PhieuNhap (maPhieuNhap, maNhaCungCap, maNhanVien, ngayNhap) VALUES
('PN0001', 'NCC0391', 'NV001', '2026-01-08'),
('PN0002', 'NCC0055', 'NV002', '2025-12-23'),
('PN0003', 'NCC0362', 'NV002', '2026-01-06'),
('PN0004', 'NCC0241', 'NV001', '2026-01-02'),
('PN0005', 'NCC0391', 'NV001', '2026-01-22'),
('PN0006', 'NCC0294', 'NV002', '2026-02-15'),
('PN0007', 'NCC0391', 'NV001', '2026-01-29'),
('PN0008', 'NCC0362', 'NV002', '2026-01-26'),
('PN0009', 'NCC0380', 'NV001', '2025-12-11'),
('PN0010', 'NCC0070', 'NV002', '2026-02-25');
GO

INSERT INTO ChiTietPhieuNhap (maPhieuNhap, maQuyDoi, maLoThuoc, soLuong, donGiaNhap) VALUES
('PN0001', 'QD01435', 'LO01435', 145, 201600),
('PN0001', 'QD00931', 'LO00931', 88, 252699),
('PN0001', 'QD01179', 'LO01179', 147, 19600),
('PN0001', 'QD02413', 'LO02413', 144, 240099),
('PN0002', 'QD01179', 'LO01179', 61, 19600),
('PN0002', 'QD00262', 'LO00262', 140, 206500),
('PN0002', 'QD00587', 'LO00587', 69, 306600),
('PN0003', 'QD00731', 'LO00731', 142, 342300),
('PN0003', 'QD01802', 'LO01802', 75, 282100),
('PN0003', 'QD00504', 'LO00504', 84, 120399),
('PN0004', 'QD00262', 'LO00262', 74, 206500),
('PN0004', 'QD01264', 'LO01264', 133, 304500),
('PN0004', 'QD00847', 'LO00847', 121, 300300),
('PN0004', 'QD00045', 'LO00045', 73, 296100),
('PN0004', 'QD02729', 'LO02729', 144, 191100),
('PN0005', 'QD02247', 'LO02247', 106, 90300),
('PN0005', 'QD02945', 'LO02945', 95, 243599),
('PN0005', 'QD01657', 'LO01657', 91, 84000),
('PN0006', 'QD02671', 'LO02671', 133, 51800),
('PN0006', 'QD00045', 'LO00045', 146, 296100),
('PN0006', 'QD00276', 'LO00276', 149, 279300),
('PN0006', 'QD01877', 'LO01877', 50, 70700),
('PN0006', 'QD02239', 'LO02239', 75, 147000),
('PN0007', 'QD00276', 'LO00276', 119, 279300),
('PN0007', 'QD02541', 'LO02541', 90, 70700),
('PN0007', 'QD00639', 'LO00639', 113, 83300),
('PN0008', 'QD01657', 'LO01657', 91, 84000),
('PN0008', 'QD00931', 'LO00931', 139, 252699),
('PN0008', 'QD02234', 'LO02234', 122, 175700),
('PN0008', 'QD00194', 'LO00194', 148, 251299),
('PN0008', 'QD00731', 'LO00731', 65, 342300),
('PN0009', 'QD01877', 'LO01877', 110, 70700),
('PN0009', 'QD01365', 'LO01365', 134, 264600),
('PN0010', 'QD01551', 'LO01551', 129, 45500),
('PN0010', 'QD02247', 'LO02247', 132, 90300),
('PN0010', 'QD02394', 'LO02394', 66, 264600),
('PN0010', 'QD01179', 'LO01179', 121, 19600);
GO

INSERT INTO HoaDon (maHoaDon, maKhachHang, maNhanVien, ngayLap, thueVAT, hinhThucThanhToan, ghiChu) VALUES
('HD0001', 'KH006', 'NV002', '2026-02-27', 8.0, 'CHUYEN_KHOAN', N'Khách mua thuốc lẻ'),
('HD0002', 'KH004', 'NV002', '2026-02-22', 8.0, 'CHUYEN_KHOAN', N'Khách mua thuốc lẻ'),
('HD0003', 'KH002', 'NV001', '2026-03-07', 8.0, 'THE', N'Khách mua thuốc lẻ'),
('HD0004', 'KH008', 'NV001', '2026-03-07', 8.0, 'TIEN_MAT', N'Khách mua thuốc lẻ'),
('HD0005', 'KH007', 'NV002', '2026-03-05', 8.0, 'THE', N'Khách mua thuốc lẻ'),
('HD0006', 'KH006', 'NV002', '2026-02-19', 8.0, 'CHUYEN_KHOAN', N'Khách mua thuốc lẻ'),
('HD0007', 'KH001', 'NV002', '2026-02-25', 8.0, 'TIEN_MAT', N'Khách mua thuốc lẻ'),
('HD0008', 'KH003', 'NV001', '2026-02-07', 8.0, 'CHUYEN_KHOAN', N'Khách mua thuốc lẻ'),
('HD0009', 'KH002', 'NV002', '2026-02-09', 8.0, 'TIEN_MAT', N'Khách mua thuốc lẻ'),
('HD0010', 'KH003', 'NV001', '2026-02-22', 8.0, 'CHUYEN_KHOAN', N'Khách mua thuốc lẻ'),
('HD0011', 'KH004', 'NV001', '2026-02-21', 8.0, 'CHUYEN_KHOAN', N'Khách mua thuốc lẻ'),
('HD0012', 'KH004', 'NV001', '2026-02-18', 8.0, 'CHUYEN_KHOAN', N'Khách mua thuốc lẻ'),
('HD0013', 'KH004', 'NV002', '2026-03-01', 8.0, 'THE', N'Khách mua thuốc lẻ'),
('HD0014', 'KH006', 'NV001', '2026-02-23', 8.0, 'CHUYEN_KHOAN', N'Khách mua thuốc lẻ'),
('HD0015', 'KH007', 'NV001', '2026-02-20', 8.0, 'CHUYEN_KHOAN', N'Khách mua thuốc lẻ');
GO

INSERT INTO ChiTietHoaDon (maHoaDon, maQuyDoi, maLoThuoc, soLuong, donGia) VALUES
('HD0001', 'QD01538', 'LO01538', 2, 452000),
('HD0002', 'QD02671', 'LO02671', 2, 74000),
('HD0002', 'QD02363', 'LO02363', 2, 242000),
('HD0002', 'QD00840', 'LO00840', 2, 272000),
('HD0002', 'QD01977', 'LO01977', 1, 269000),
('HD0003', 'QD00931', 'LO00931', 1, 361000),
('HD0003', 'QD01820', 'LO01820', 2, 367000),
('HD0003', 'QD02594', 'LO02594', 2, 77000),
('HD0003', 'QD00639', 'LO00639', 2, 119000),
('HD0004', 'QD01365', 'LO01365', 2, 378000),
('HD0004', 'QD02296', 'LO02296', 1, 55000),
('HD0004', 'QD02729', 'LO02729', 1, 273000),
('HD0005', 'QD01551', 'LO01551', 2, 65000),
('HD0005', 'QD02363', 'LO02363', 3, 242000),
('HD0005', 'QD00556', 'LO00556', 2, 141000),
('HD0005', 'QD02413', 'LO02413', 1, 343000),
('HD0006', 'QD01435', 'LO01435', 3, 288000),
('HD0006', 'QD00931', 'LO00931', 3, 361000),
('HD0006', 'QD01119', 'LO01119', 1, 337000),
('HD0007', 'QD01538', 'LO01538', 1, 452000),
('HD0007', 'QD01657', 'LO01657', 1, 120000),
('HD0007', 'QD01191', 'LO01191', 2, 220000),
('HD0008', 'QD01435', 'LO01435', 1, 288000),
('HD0008', 'QD01011', 'LO01011', 3, 454000),
('HD0008', 'QD01179', 'LO01179', 2, 28000),
('HD0009', 'QD02239', 'LO02239', 1, 210000),
('HD0010', 'QD02594', 'LO02594', 1, 77000),
('HD0010', 'QD01673', 'LO01673', 2, 36000),
('HD0011', 'QD00731', 'LO00731', 1, 489000),
('HD0012', 'QD00731', 'LO00731', 3, 489000),
('HD0013', 'QD00639', 'LO00639', 1, 119000),
('HD0013', 'QD00931', 'LO00931', 1, 361000),
('HD0013', 'QD02319', 'LO02319', 3, 132000),
('HD0014', 'QD00276', 'LO00276', 1, 399000),
('HD0014', 'QD01360', 'LO01360', 1, 108000),
('HD0015', 'QD01435', 'LO01435', 1, 288000),
('HD0015', 'QD00840', 'LO00840', 1, 272000),
('HD0015', 'QD02945', 'LO02945', 3, 348000),
('HD0015', 'QD02381', 'LO02381', 2, 448000);
GO

INSERT INTO DonThuoc (maDonThuoc, maHoaDon, tenBacSi, chanDoan, hinhAnhDon, thongTinBenhNhan) VALUES
('DT0005', 'HD0005', N'BS Lê Thu Thủy', N'Viêm họng cấp', 'url_hinh_anh', N'Bệnh nhân khám tại BV'),
('DT0008', 'HD0008', N'BS Phạm Hữu Trí', N'Viêm họng cấp', 'url_hinh_anh', N'Bệnh nhân khám tại BV'),
('DT0013', 'HD0013', N'BS Phạm Hữu Trí', N'Viêm họng cấp', 'url_hinh_anh', N'Bệnh nhân khám tại BV');
GO

INSERT INTO PhieuDoiTra (maPhieuDoiTra, maHoaDon, maNhanVien, ngayDoiTra, lyDo, hinhThucXuLy, phiPhat) VALUES
('PDT0001', 'HD0001', 'NV001', '2026-03-09', N'Khách bị dị ứng', 'HOAN_TIEN', 0),
('PDT0002', 'HD0006', 'NV001', '2026-03-09', N'Khách bị dị ứng', 'DOI_SAN_PHAM', 0);
GO

INSERT INTO ChiTietDoiTra (maPhieuDoiTra, maQuyDoi, maLoThuoc, soLuong, tinhTrang) VALUES
('PDT0001', 'QD01191', 'LO01191', 1, N'Thuốc còn nguyên seal'),
('PDT0002', 'QD02541', 'LO02541', 1, N'Thuốc còn nguyên seal');
GO

