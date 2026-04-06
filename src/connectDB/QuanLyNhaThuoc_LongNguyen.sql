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
-- 1. TẠO CÁC BẢNG ĐỘC LẬP
-- ========================================================
CREATE TABLE NhaCungCap (
    maNhaCungCap VARCHAR(20) PRIMARY KEY,
    tenNhaCungCap NVARCHAR(100) NOT NULL,
    sdt VARCHAR(15),
    diaChi NVARCHAR(255),
    congNo DECIMAL(18,2) DEFAULT 0
);

CREATE TABLE KhachHang (
    maKhachHang VARCHAR(20) PRIMARY KEY,
    hoTen NVARCHAR(100) NOT NULL,
    sdt VARCHAR(15),
    diaChi NVARCHAR(255),
    diemTichLuy INT DEFAULT 0
);

CREATE TABLE NhanVien (
    maNhanVien VARCHAR(20) PRIMARY KEY,
    tenDangNhap VARCHAR(50) UNIQUE NOT NULL,
    matKhau VARCHAR(255) NOT NULL,
    hoTen NVARCHAR(100) NOT NULL,
    chucVu NVARCHAR(50),
    caLamViec NVARCHAR(50),
    sdt VARCHAR(15)
);

CREATE TABLE DanhMucThuoc (
    maDanhMuc VARCHAR(20) PRIMARY KEY,
    tenDanhMuc NVARCHAR(100) NOT NULL,
    moTa NVARCHAR(255)
);

-- ========================================================
-- 2. TẠO BẢNG THUỐC, TỒN KHO & BẢNG GIÁ
-- ========================================================
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
    donViCoBan NVARCHAR(20) NOT NULL, 
    hinhAnh VARCHAR(255),
    canKeDon BIT DEFAULT 0, 
    trangThai VARCHAR(20) DEFAULT 'DANG_BAN', 
    
    FOREIGN KEY (maDanhMuc) REFERENCES DanhMucThuoc(maDanhMuc),
    CONSTRAINT CHK_TrangThai CHECK (trangThai IN ('DANG_BAN', 'NGUNG_BAN', 'HET_HANG'))
);

	CREATE TABLE DonViQuyDoi (
	    maQuyDoi VARCHAR(20) PRIMARY KEY,
	    maThuoc VARCHAR(20) NOT NULL,
	    tenDonVi NVARCHAR(50) NOT NULL,
	    tyLeQuyDoi INT NOT NULL,
	
	    FOREIGN KEY (maThuoc) REFERENCES Thuoc(maThuoc) ON DELETE CASCADE
	);

CREATE TABLE LoThuoc (
    maLoThuoc VARCHAR(20) PRIMARY KEY,
    maThuoc VARCHAR(20) NOT NULL,
    ngaySanXuat DATE,
    hanSuDung DATE NOT NULL,
    soLuongTon INT DEFAULT 0, 
    giaNhap DECIMAL(18,2),
    viTriKho VARCHAR(50) DEFAULT 'KHO_BAN_HANG',

    FOREIGN KEY (maThuoc) REFERENCES Thuoc(maThuoc) ON DELETE CASCADE,
    CONSTRAINT CHK_ViTriKho CHECK (viTriKho IN ('KHO_BAN_HANG', 'KHO_DU_TRU')),
    CONSTRAINT CHK_LoThuoc_SoLuong_Gia CHECK (soLuongTon >= 0 AND (giaNhap IS NULL OR giaNhap > 0))
);

CREATE TABLE BangGia (
    maBangGia VARCHAR(20) PRIMARY KEY,
    tenBangGia NVARCHAR(100) NOT NULL, 
    loaiBangGia VARCHAR(20) NOT NULL,  
    ngayBatDau DATE NOT NULL,
    ngayKetThuc DATE NULL,
    moTa NVARCHAR(255),
    trangThai BIT DEFAULT 1,
    CONSTRAINT CHK_LoaiBangGia CHECK (loaiBangGia IN ('DEFAULT', 'PROMO'))
);

CREATE TABLE ChiTietBangGia (
    maBangGia VARCHAR(20) NOT NULL,
    maQuyDoi VARCHAR(20) NOT NULL,
    donGiaBan DECIMAL(18,2) NOT NULL,
    PRIMARY KEY (maBangGia, maQuyDoi),
    FOREIGN KEY (maBangGia) REFERENCES BangGia(maBangGia) ON DELETE CASCADE,
    FOREIGN KEY (maQuyDoi) REFERENCES DonViQuyDoi(maQuyDoi),
    CONSTRAINT CHK_DonGiaBan CHECK (donGiaBan > 0)
);

-- ========================================================
-- 3. TẠO BẢNG NGHIỆP VỤ: ĐẶT HÀNG (TÊN BẢNG CHUẨN MỚI)
-- ========================================================

-- Bảng Đơn Đặt Hàng (Header)
CREATE TABLE DonDatHang (
    maDonDatHang VARCHAR(20) PRIMARY KEY,
    maNhaCungCap VARCHAR(20) NOT NULL,
    maNhanVien VARCHAR(20) NOT NULL,
    ngayDat DATETIME DEFAULT GETDATE(),
    ngayGiaoDuKien DATETIME,
    tongTienDuTinh DECIMAL(18,2) DEFAULT 0,
    
    trangThai VARCHAR(50) DEFAULT 'CHO_GIAO', 
    ghiChu NVARCHAR(255),

    FOREIGN KEY (maNhaCungCap) REFERENCES NhaCungCap(maNhaCungCap),
    FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    
    -- CÁC TRẠNG THÁI DÙNG ĐỂ JAVA "DỊCH" RA 4 CỘT GIAO DIỆN
    CONSTRAINT CHK_TrangThai_DonDat CHECK (trangThai IN (
        'CHO_GIAO',       -- Chờ Giao | Đang Xử Lý | Nút ON
        'GIAO_DU',        -- Giao Đủ | Hoàn Thành | Nút OFF
        'GIAO_MOT_PHAN',  -- Giao Một Phần | Đang Xử Lý | Nút ON
        'DONG_DON_THIEU', -- Giao Một Phần | Hoàn Thành | Nút OFF
        'DA_HUY'          -- Hủy Đơn | Hoàn Thành | Nút OFF
    ))
);

-- Bảng Chi Tiết Đơn Đặt Hàng (Detail)
CREATE TABLE ChiTietDonDatHang (
    maDonDatHang VARCHAR(20) NOT NULL,
    maQuyDoi VARCHAR(20) NOT NULL,
    
    soLuongDat INT NOT NULL,        
    soLuongDaNhan INT DEFAULT 0,    
    donGiaDuKien DECIMAL(18,2) NOT NULL,

    -- Cột tạm để UI Java đẩy Lô/Date xuống DB
    maLo VARCHAR(20),
    ngaySanXuat DATE,
    hanSuDung DATE,

    -- Khóa chính kép và Khóa ngoại (Chỉ viết 1 lần)
    PRIMARY KEY (maDonDatHang, maQuyDoi),
    FOREIGN KEY (maDonDatHang) REFERENCES DonDatHang(maDonDatHang) ON DELETE CASCADE,
    FOREIGN KEY (maQuyDoi) REFERENCES DonViQuyDoi(maQuyDoi)
);

-- ========================================================
-- 4. TẠO BẢNG NGHIỆP VỤ: NHẬP HÀNG
-- ========================================================

CREATE TABLE PhieuNhap (
    maPhieuNhap VARCHAR(20) PRIMARY KEY,
    maDonDatHang VARCHAR(20), -- Khóa ngoại trỏ về DonDatHang
    maNhaCungCap VARCHAR(20) NOT NULL,
    maNhanVien VARCHAR(20) NOT NULL,
    ngayNhap DATETIME DEFAULT GETDATE(),
    
    FOREIGN KEY (maNhaCungCap) REFERENCES NhaCungCap(maNhaCungCap),
    FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    FOREIGN KEY (maDonDatHang) REFERENCES DonDatHang(maDonDatHang)
);

CREATE TABLE ChiTietPhieuNhap (
    maPhieuNhap VARCHAR(20) NOT NULL,
    maQuyDoi VARCHAR(20) NOT NULL,
    maLoThuoc VARCHAR(20) NOT NULL,
    soLuong INT NOT NULL,
    donGiaNhap DECIMAL(18,2) NOT NULL,

    PRIMARY KEY (maPhieuNhap, maQuyDoi, maLoThuoc),
    FOREIGN KEY (maPhieuNhap) REFERENCES PhieuNhap(maPhieuNhap) ON DELETE CASCADE,
    FOREIGN KEY (maQuyDoi) REFERENCES DonViQuyDoi(maQuyDoi),
    FOREIGN KEY (maLoThuoc) REFERENCES LoThuoc(maLoThuoc),
    CONSTRAINT CHK_ChiTietNhap_SL_Gia CHECK (soLuong > 0 AND donGiaNhap > 0)
);

-- ========================================================
-- 5. TẠO BẢNG NGHIỆP VỤ: BÁN HÀNG & ĐỔI TRẢ
-- ========================================================
CREATE TABLE HoaDon (
    maHoaDon VARCHAR(20) PRIMARY KEY,
    maKhachHang VARCHAR(20), 
    maNhanVien VARCHAR(20) NOT NULL,
    ngayLap DATETIME DEFAULT GETDATE(),
    thueVAT DECIMAL(5,2) DEFAULT 0, 
    hinhThucThanhToan VARCHAR(20) DEFAULT 'TIEN_MAT',
    ghiChu NVARCHAR(255),

    FOREIGN KEY (maKhachHang) REFERENCES KhachHang(maKhachHang),
    FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    CONSTRAINT CHK_ThanhToan CHECK (hinhThucThanhToan IN ('TIEN_MAT', 'CHUYEN_KHOAN', 'THE'))
);

CREATE TABLE DonThuoc (
    maDonThuoc VARCHAR(20) PRIMARY KEY,
    maHoaDon VARCHAR(20) UNIQUE NOT NULL, 
    tenBacSi NVARCHAR(100),
    chanDoan NVARCHAR(255),
    hinhAnhDon VARCHAR(255),
    thongTinBenhNhan NVARCHAR(255),

    FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon) ON DELETE CASCADE
);

CREATE TABLE ChiTietHoaDon (
    maHoaDon VARCHAR(20) NOT NULL,
    maBangGia VARCHAR(20) NOT NULL, 
    maQuyDoi VARCHAR(20) NOT NULL,
    maLoThuoc VARCHAR(20) NOT NULL,
    soLuong INT NOT NULL,
    donGia DECIMAL(18,2) NOT NULL, 

    PRIMARY KEY (maHoaDon, maQuyDoi, maLoThuoc),
    FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon) ON DELETE CASCADE,
    FOREIGN KEY (maBangGia) REFERENCES BangGia(maBangGia),
    FOREIGN KEY (maQuyDoi) REFERENCES DonViQuyDoi(maQuyDoi),
    FOREIGN KEY (maLoThuoc) REFERENCES LoThuoc(maLoThuoc),
    CONSTRAINT CHK_ChiTietBan_SL_Gia CHECK (soLuong > 0 AND donGia > 0)
);

CREATE TABLE PhieuDoiTra (
    maPhieuDoiTra VARCHAR(20) PRIMARY KEY,
    maHoaDon VARCHAR(20) NOT NULL, 
    maNhanVien VARCHAR(20) NOT NULL,
    ngayDoiTra DATETIME DEFAULT GETDATE(),
    lyDo NVARCHAR(255),
    hinhThucXuLy VARCHAR(20) NOT NULL,
    phiPhat DECIMAL(18,2) DEFAULT 0,

    FOREIGN KEY (maHoaDon) REFERENCES HoaDon(maHoaDon),
    FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    CONSTRAINT CHK_HinhThucXuLy CHECK (hinhThucXuLy IN ('HOAN_TIEN', 'DOI_SAN_PHAM'))
);

CREATE TABLE ChiTietDoiTra (
    maPhieuDoiTra VARCHAR(20) NOT NULL,
    maQuyDoi VARCHAR(20) NOT NULL,
    maLoThuoc VARCHAR(20) NOT NULL, 
    soLuong INT NOT NULL,
    tinhTrang NVARCHAR(100), 

    PRIMARY KEY (maPhieuDoiTra, maQuyDoi, maLoThuoc),
    FOREIGN KEY (maPhieuDoiTra) REFERENCES PhieuDoiTra(maPhieuDoiTra) ON DELETE CASCADE,
    FOREIGN KEY (maQuyDoi) REFERENCES DonViQuyDoi(maQuyDoi),
    FOREIGN KEY (maLoThuoc) REFERENCES LoThuoc(maLoThuoc),
    CONSTRAINT CHK_ChiTietDoiTra_SL CHECK (soLuong > 0)
);
USE QuanLyNhaThuoc_LongNguyen;
GO

-- ========================================================
-- 6. TẠO BẢNG NGHIỆP VỤ: QUẢN LÝ CÔNG NỢ (TRẢ TIỀN NCC)
-- ========================================================

-- Bảng Phiếu Chi (Lưu thông tin chung của lần đi trả tiền)
CREATE TABLE PhieuChi (
    maPhieuChi VARCHAR(20) PRIMARY KEY,
    maNhaCungCap VARCHAR(20) NOT NULL,
    maNhanVien VARCHAR(20) NOT NULL,
    ngayChi DATETIME DEFAULT GETDATE(),
    tongTienChi DECIMAL(18,2) NOT NULL,
    hinhThucChi VARCHAR(20) DEFAULT 'TIEN_MAT',
    ghiChu NVARCHAR(255),

    FOREIGN KEY (maNhaCungCap) REFERENCES NhaCungCap(maNhaCungCap),
    FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    CONSTRAINT CHK_TongTienChi CHECK (tongTienChi > 0),
    CONSTRAINT CHK_HinhThucChi CHECK (hinhThucChi IN ('TIEN_MAT', 'CHUYEN_KHOAN', 'THE'))
);

-- Bảng Chi Tiết Phiếu Chi (Lưu chi tiết lần này trả cho những phiếu nhập nào)
-- *Lưu ý: Nếu sếp chỉ muốn quản lý nợ theo CỤC (của NCC) thì không cần bảng này.
-- *Nhưng thiết kế chuẩn là trả tiền cho phiếu nhập nào thì trừ vào phiếu đó.
CREATE TABLE ChiTietPhieuChi (
    maPhieuChi VARCHAR(20) NOT NULL,
    maPhieuNhap VARCHAR(20) NOT NULL,
    soTienTra DECIMAL(18,2) NOT NULL,

    PRIMARY KEY (maPhieuChi, maPhieuNhap),
    FOREIGN KEY (maPhieuChi) REFERENCES PhieuChi(maPhieuChi) ON DELETE CASCADE,
    FOREIGN KEY (maPhieuNhap) REFERENCES PhieuNhap(maPhieuNhap),
    CONSTRAINT CHK_SoTienTra CHECK (soTienTra > 0)
);
-- BẢNG PHIẾU XUẤT (Header)
CREATE TABLE PhieuXuat (
    maPhieuXuat VARCHAR(20) PRIMARY KEY,
    ngayXuat DATETIME DEFAULT GETDATE(),
    maNhanVien VARCHAR(20) NOT NULL,
    loaiPhieu INT NOT NULL, -- 1: Chuyển kho nội bộ | 2: Trả NCC | 3: Xuất hủy
    
    maNhaCungCap VARCHAR(20) NULL, 
    khoNhan NVARCHAR(100) NULL,    
    
    tongTien DECIMAL(18,2) DEFAULT 0,
    ghiChu NVARCHAR(255),
    
    -- Móc khóa ngoại bảo vệ dữ liệu
    FOREIGN KEY (maNhanVien) REFERENCES NhanVien(maNhanVien),
    FOREIGN KEY (maNhaCungCap) REFERENCES NhaCungCap(maNhaCungCap)
);

-- BẢNG CHI TIẾT PHIẾU XUẤT (Detail)
CREATE TABLE ChiTietPhieuXuat (
    maPhieuXuat VARCHAR(20),
    maLoThuoc VARCHAR(20),  
    soLuong INT NOT NULL,
    donGia DECIMAL(18,2), 
    thanhTien DECIMAL(18,2),
    
    PRIMARY KEY (maPhieuXuat, maLoThuoc),
    
    -- Móc khóa ngoại bảo vệ dữ liệu
    FOREIGN KEY (maPhieuXuat) REFERENCES PhieuXuat(maPhieuXuat),
    FOREIGN KEY (maLoThuoc) REFERENCES LoThuoc(maLoThuoc) -- Đảm bảo xuất đúng cái lô đang có trong kho
);


GO
PRINT N'Đã tạo xong Database cấu trúc mới!';
GO
-- ==========================================
-- DỮ LIỆU KHỞI TẠO (DATA FULL GỐC 100%)
-- ==========================================
USE QuanLyNhaThuoc_LongNguyen;
GO

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

INSERT INTO DonViQuyDoi (maQuyDoi, maThuoc, tenDonVi, tyLeQuyDoi) VALUES
('QD00045', 'T00045', N'Viên', 1),
('QD00051', 'T00045', N'Vỉ', 10),
('QD00052', 'T00045', N'Hộp', 100),
('QD00194', 'T00194', N'Viên', 1),
('QD00053', 'T00194', N'Vỉ', 10),
('QD00054', 'T00194', N'Hộp', 100),
('QD00262', 'T00262', N'Viên', 1),
('QD00055', 'T00262', N'Vỉ', 10),
('QD00056', 'T00262', N'Hộp', 100),
('QD00276', 'T00276', N'Viên', 1),
('QD00057', 'T00276', N'Vỉ', 10),
('QD00058', 'T00276', N'Hộp', 100),
('QD00486', 'T00486', N'Viên', 1),
('QD00059', 'T00486', N'Vỉ', 10),
('QD00060', 'T00486', N'Hộp', 100),
('QD00504', 'T00504', N'Tuýp', 1),
('QD00061', 'T00504', N'Hộp', 5),
('QD00556', 'T00556', N'Chai', 1),
('QD00062', 'T00556', N'Hộp', 5),
('QD00587', 'T00587', N'Viên', 1),
('QD00063', 'T00587', N'Vỉ', 10),
('QD00064', 'T00587', N'Hộp', 100),
('QD00639', 'T00639', N'Viên', 1),
('QD00065', 'T00639', N'Vỉ', 10),
('QD00066', 'T00639', N'Hộp', 100),
('QD00653', 'T00653', N'Viên', 1),
('QD00067', 'T00653', N'Vỉ', 10),
('QD00068', 'T00653', N'Hộp', 100),
('QD00655', 'T00655', N'Viên', 1),
('QD00069', 'T00655', N'Vỉ', 10),
('QD00070', 'T00655', N'Hộp', 100),
('QD00731', 'T00731', N'Chai', 1),
('QD00071', 'T00731', N'Hộp', 5),
('QD00742', 'T00742', N'Viên', 1),
('QD00072', 'T00742', N'Vỉ', 10),
('QD00073', 'T00742', N'Hộp', 100),
('QD00840', 'T00840', N'Ống', 1),
('QD00074', 'T00840', N'Hộp', 5),
('QD00847', 'T00847', N'Viên', 1),
('QD00075', 'T00847', N'Vỉ', 10),
('QD00076', 'T00847', N'Hộp', 100),
('QD00931', 'T00931', N'Viên', 1),
('QD00077', 'T00931', N'Vỉ', 10),
('QD00078', 'T00931', N'Hộp', 100),
('QD01011', 'T01011', N'Viên', 1),
('QD00079', 'T01011', N'Vỉ', 10),
('QD00080', 'T01011', N'Hộp', 100),
('QD01119', 'T01119', N'Viên', 1),
('QD00081', 'T01119', N'Vỉ', 10),
('QD00082', 'T01119', N'Hộp', 100),
('QD01179', 'T01179', N'Viên', 1),
('QD00083', 'T01179', N'Vỉ', 10),
('QD00084', 'T01179', N'Hộp', 100),
('QD01191', 'T01191', N'Viên', 1),
('QD00085', 'T01191', N'Vỉ', 10),
('QD00086', 'T01191', N'Hộp', 100),
('QD01264', 'T01264', N'Chai', 1),
('QD00087', 'T01264', N'Hộp', 5),
('QD01360', 'T01360', N'Viên', 1),
('QD00088', 'T01360', N'Vỉ', 10),
('QD00089', 'T01360', N'Hộp', 100),
('QD01365', 'T01365', N'Tuýp', 1),
('QD00090', 'T01365', N'Hộp', 5),
('QD01367', 'T01367', N'Tuýp', 1),
('QD00091', 'T01367', N'Hộp', 5),
('QD01435', 'T01435', N'Viên', 1),
('QD00092', 'T01435', N'Vỉ', 10),
('QD00093', 'T01435', N'Hộp', 100),
('QD01538', 'T01538', N'Viên', 1),
('QD00094', 'T01538', N'Vỉ', 10),
('QD00095', 'T01538', N'Hộp', 100),
('QD01551', 'T01551', N'Viên', 1),
('QD00096', 'T01551', N'Vỉ', 10),
('QD00097', 'T01551', N'Hộp', 100),
('QD01657', 'T01657', N'Tuýp', 1),
('QD00098', 'T01657', N'Hộp', 5),
('QD01673', 'T01673', N'Viên', 1),
('QD00099', 'T01673', N'Vỉ', 10),
('QD00100', 'T01673', N'Hộp', 100),
('QD01774', 'T01774', N'Tuýp', 1),
('QD00101', 'T01774', N'Hộp', 5),
('QD01802', 'T01802', N'Viên', 1),
('QD00102', 'T01802', N'Vỉ', 10),
('QD00103', 'T01802', N'Hộp', 100),
('QD01820', 'T01820', N'Viên', 1),
('QD00104', 'T01820', N'Vỉ', 10),
('QD00105', 'T01820', N'Hộp', 100),
('QD01877', 'T01877', N'Viên', 1),
('QD00106', 'T01877', N'Vỉ', 10),
('QD00107', 'T01877', N'Hộp', 100),
('QD01977', 'T01977', N'Viên', 1),
('QD00108', 'T01977', N'Vỉ', 10),
('QD00109', 'T01977', N'Hộp', 100),
('QD02234', 'T02234', N'Viên', 1),
('QD00110', 'T02234', N'Vỉ', 10),
('QD00111', 'T02234', N'Hộp', 100),
('QD02239', 'T02239', N'Viên', 1),
('QD00112', 'T02239', N'Vỉ', 10),
('QD00113', 'T02239', N'Hộp', 100),
('QD02247', 'T02247', N'Viên', 1),
('QD00114', 'T02247', N'Vỉ', 10),
('QD00115', 'T02247', N'Hộp', 100),
('QD02296', 'T02296', N'Hộp', 1),
('QD02319', 'T02319', N'Viên', 1),
('QD00116', 'T02319', N'Vỉ', 10),
('QD00117', 'T02319', N'Hộp', 100),
('QD02363', 'T02363', N'Viên', 1),
('QD00118', 'T02363', N'Vỉ', 10),
('QD00119', 'T02363', N'Hộp', 100),
('QD02381', 'T02381', N'Gói', 1),
('QD02394', 'T02394', N'Lọ', 1),
('QD00120', 'T02394', N'Hộp', 5),
('QD02413', 'T02413', N'Viên', 1),
('QD00121', 'T02413', N'Vỉ', 10),
('QD00122', 'T02413', N'Hộp', 100),
('QD02541', 'T02541', N'Viên', 1),
('QD00123', 'T02541', N'Vỉ', 10),
('QD00124', 'T02541', N'Hộp', 100),
('QD02594', 'T02594', N'Viên', 1),
('QD00125', 'T02594', N'Vỉ', 10),
('QD00126', 'T02594', N'Hộp', 100),
('QD02671', 'T02671', N'Viên', 1),
('QD00127', 'T02671', N'Vỉ', 10),
('QD00128', 'T02671', N'Hộp', 100),
('QD02729', 'T02729', N'Viên', 1),
('QD00129', 'T02729', N'Vỉ', 10),
('QD00130', 'T02729', N'Hộp', 100),
('QD02811', 'T02811', N'Viên', 1),
('QD00131', 'T02811', N'Vỉ', 10),
('QD00132', 'T02811', N'Hộp', 100),
('QD02883', 'T02883', N'Chai', 1),
('QD00133', 'T02883', N'Hộp', 5),
('QD02945', 'T02945', N'Viên', 1),
('QD00134', 'T02945', N'Vỉ', 10),
('QD00135', 'T02945', N'Hộp', 100);
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

INSERT INTO BangGia (maBangGia, tenBangGia, loaiBangGia, ngayBatDau, ngayKetThuc, moTa, trangThai) VALUES
('BG0001', N'Bảng giá bán lẻ mặc định', 'DEFAULT', '2026-01-01', NULL, N'Bảng giá bán lẻ mặc định', 1),
('BG0002', N'Khuyến Mãi Lễ 30/4 - 1/5', 'PROMO', '2026-04-25', '2026-05-05', N'Giảm giá các loại thuốc bổ, vitamin dịp lễ', 1);
GO

INSERT INTO ChiTietBangGia (maBangGia, maQuyDoi, donGiaBan) VALUES
('BG0001', 'QD00045', 5200), 
('BG0001', 'QD00051', 49400), 
('BG0001', 'QD00052', 478400),
('BG0001', 'QD00194', 4800), 
('BG0001', 'QD00053', 45600), 
('BG0001', 'QD00054', 441600),
('BG0001', 'QD00262', 12000), 
('BG0001', 'QD00055', 114000), 
('BG0001', 'QD00056', 1104000),
('BG0001', 'QD00276', 8500), 
('BG0001', 'QD00057', 80800), 
('BG0001', 'QD00058', 782000),
('BG0001', 'QD00486', 3600), 
('BG0001', 'QD00059', 34200), 
('BG0001', 'QD00060', 331200),
('BG0001', 'QD00504', 68000), 
('BG0001', 'QD00061', 326400),
('BG0001', 'QD00556', 42000), 
('BG0001', 'QD00062', 201600),
('BG0001', 'QD00587', 9500), 
('BG0001', 'QD00063', 90200), 
('BG0001', 'QD00064', 874000),
('BG0001', 'QD00639', 5400), 
('BG0001', 'QD00065', 51300), 
('BG0001', 'QD00066', 496800),
('BG0001', 'QD00653', 3800), 
('BG0001', 'QD00067', 36100), 
('BG0001', 'QD00068', 349600),
('BG0001', 'QD00655', 7800), 
('BG0001', 'QD00069', 74100), 
('BG0001', 'QD00070', 717600),
('BG0001', 'QD00731', 32000), 
('BG0001', 'QD00071', 153600),
('BG0001', 'QD00742', 4200), 
('BG0001', 'QD00072', 39900), 
('BG0001', 'QD00073', 386400),
('BG0001', 'QD00840', 78000), 
('BG0001', 'QD00074', 374400),
('BG0001', 'QD00847', 9800), 
('BG0001', 'QD00075', 93100), 
('BG0001', 'QD00076', 901600),
('BG0001', 'QD00931', 6500), 
('BG0001', 'QD00077', 61800), 
('BG0001', 'QD00078', 598000),
('BG0001', 'QD01011', 7600), 
('BG0001', 'QD00079', 72200), 
('BG0001', 'QD00080', 699200),
('BG0001', 'QD01119', 6200), 
('BG0001', 'QD00081', 58900), 
('BG0001', 'QD00082', 570400),
('BG0001', 'QD01179', 24000), 
('BG0001', 'QD00083', 228000), 
('BG0001', 'QD00084', 2208000),
('BG0001', 'QD01191', 4500), 
('BG0001', 'QD00085', 42800), 
('BG0001', 'QD00086', 414000),
('BG0001', 'QD01264', 36000), 
('BG0001', 'QD00087', 172800),
('BG0001', 'QD01360', 4800), 
('BG0001', 'QD00088', 45600), 
('BG0001', 'QD00089', 441600),
('BG0001', 'QD01365', 52000), 
('BG0001', 'QD00090', 249600),
('BG0001', 'QD01367', 72000), 
('BG0001', 'QD00091', 345600),
('BG0001', 'QD01435', 8300), 
('BG0001', 'QD00092', 78800), 
('BG0001', 'QD00093', 763600),
('BG0001', 'QD01538', 9100), 
('BG0001', 'QD00094', 86400), 
('BG0001', 'QD00095', 837200),
('BG0001', 'QD01551', 3200), 
('BG0001', 'QD00096', 30400), 
('BG0001', 'QD00097', 294400),
('BG0001', 'QD01657', 58000), 
('BG0001', 'QD00098', 278400),
('BG0001', 'QD01673', 2800), 
('BG0001', 'QD00099', 26600), 
('BG0001', 'QD00100', 257600),
('BG0001', 'QD01774', 26000), 
('BG0001', 'QD00101', 124800),
('BG0001', 'QD01802', 11200), 
('BG0001', 'QD00102', 106400), 
('BG0001', 'QD00103', 1030400),
('BG0001', 'QD01820', 6900), 
('BG0001', 'QD00104', 65600), 
('BG0001', 'QD00105', 634800),
('BG0001', 'QD01877', 5200), 
('BG0001', 'QD00106', 49400), 
('BG0001', 'QD00107', 478400),
('BG0001', 'QD01977', 4100), 
('BG0001', 'QD00108', 39000), 
('BG0001', 'QD00109', 377200),
('BG0001', 'QD02234', 5600), 
('BG0001', 'QD00110', 53200), 
('BG0001', 'QD00111', 515200),
('BG0001', 'QD02239', 26000), 
('BG0001', 'QD00112', 247000), 
('BG0001', 'QD00113', 2392000),
('BG0001', 'QD02247', 4700), 
('BG0001', 'QD00114', 44600), 
('BG0001', 'QD00115', 432400),
('BG0001', 'QD02296', 58000),
('BG0001', 'QD02319', 5900), 
('BG0001', 'QD00116', 56000), 
('BG0001', 'QD00117', 542800),
('BG0001', 'QD02363', 13500), 
('BG0001', 'QD00118', 128200), 
('BG0001', 'QD00119', 1242000),
('BG0001', 'QD02381', 42000),
('BG0001', 'QD02394', 68000), 
('BG0001', 'QD00120', 326400),
('BG0001', 'QD02413', 11000), 
('BG0001', 'QD00121', 104500), 
('BG0001', 'QD00122', 1012000),
('BG0001', 'QD02541', 9800), 
('BG0001', 'QD00123', 93100), 
('BG0001', 'QD00124', 901600),
('BG0001', 'QD02594', 2500), 
('BG0001', 'QD00125', 23800), 
('BG0001', 'QD00126', 230000),
('BG0001', 'QD02671', 2200), 
('BG0001', 'QD00127', 20900), 
('BG0001', 'QD00128', 202400),
('BG0001', 'QD02729', 7200), 
('BG0001', 'QD00129', 68400), 
('BG0001', 'QD00130', 662400),
('BG0001', 'QD02811', 7400), 
('BG0001', 'QD00131', 70300), 
('BG0001', 'QD00132', 680800),
('BG0001', 'QD02883', 30000), 
('BG0001', 'QD00133', 144000),
('BG0001', 'QD02945', 1800), 
('BG0001', 'QD00134', 17100), 
('BG0001', 'QD00135', 165600),
-- Data của Bảng giá BG0002 (Khuyến mãi 30/4)
('BG0002', 'QD00045', 4800),
('BG0002', 'QD00051', 45000),
('BG0002', 'QD00052', 430000),
('BG0002', 'QD01877', 4500),
('BG0002', 'QD00106', 42000),
('BG0002', 'QD00107', 410000),
('BG0002', 'QD02883', 25000),
('BG0002', 'QD00133', 120000),
('BG0002', 'QD01657', 50000),
('BG0002', 'QD00098', 240000);
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

INSERT INTO ChiTietHoaDon (maHoaDon, maBangGia, maQuyDoi, maLoThuoc, soLuong, donGia) VALUES
('HD0001', 'BG0001', 'QD01538', 'LO01538', 2, 452000),
('HD0002', 'BG0001', 'QD02671', 'LO02671', 2, 74000), 
('HD0002', 'BG0001', 'QD02363', 'LO02363', 2, 242000),
('HD0002', 'BG0001', 'QD00840', 'LO00840', 2, 272000), 
('HD0002', 'BG0001', 'QD01977', 'LO01977', 1, 269000),
('HD0003', 'BG0001', 'QD00931', 'LO00931', 1, 361000), 
('HD0003', 'BG0001', 'QD01820', 'LO01820', 2, 367000),
('HD0003', 'BG0001', 'QD02594', 'LO02594', 2, 77000), 
('HD0003', 'BG0001', 'QD00639', 'LO00639', 2, 119000),
('HD0004', 'BG0001', 'QD01365', 'LO01365', 2, 378000), 
('HD0004', 'BG0001', 'QD02296', 'LO02296', 1, 55000),
('HD0004', 'BG0001', 'QD02729', 'LO02729', 1, 273000), 
('HD0005', 'BG0001', 'QD01551', 'LO01551', 2, 65000),
('HD0005', 'BG0001', 'QD02363', 'LO02363', 3, 242000), 
('HD0005', 'BG0001', 'QD00556', 'LO00556', 2, 141000),
('HD0005', 'BG0001', 'QD02413', 'LO02413', 1, 343000), 
('HD0006', 'BG0001', 'QD01435', 'LO01435', 3, 288000),
('HD0006', 'BG0001', 'QD00931', 'LO00931', 3, 361000), 
('HD0006', 'BG0001', 'QD01119', 'LO01119', 1, 337000),
('HD0007', 'BG0001', 'QD01538', 'LO01538', 1, 452000), 
('HD0007', 'BG0001', 'QD01657', 'LO01657', 1, 120000),
('HD0007', 'BG0001', 'QD01191', 'LO01191', 2, 220000), 
('HD0008', 'BG0001', 'QD01435', 'LO01435', 1, 288000),
('HD0008', 'BG0001', 'QD01011', 'LO01011', 3, 454000), 
('HD0008', 'BG0001', 'QD01179', 'LO01179', 2, 28000),
('HD0009', 'BG0001', 'QD02239', 'LO02239', 1, 210000), 
('HD0010', 'BG0001', 'QD02594', 'LO02594', 1, 77000),
('HD0010', 'BG0001', 'QD01673', 'LO01673', 2, 36000), 
('HD0011', 'BG0001', 'QD00731', 'LO00731', 1, 489000),
('HD0012', 'BG0001', 'QD00731', 'LO00731', 3, 489000), 
('HD0013', 'BG0001', 'QD00639', 'LO00639', 1, 119000),
('HD0013', 'BG0001', 'QD00931', 'LO00931', 1, 361000), 
('HD0013', 'BG0001', 'QD02319', 'LO02319', 3, 132000),
('HD0014', 'BG0001', 'QD00276', 'LO00276', 1, 399000), 
('HD0014', 'BG0001', 'QD01360', 'LO01360', 1, 108000),
('HD0015', 'BG0001', 'QD01435', 'LO01435', 1, 288000), 
('HD0015', 'BG0001', 'QD00840', 'LO00840', 1, 272000),
('HD0015', 'BG0001', 'QD02945', 'LO02945', 3, 348000), 
('HD0015', 'BG0001', 'QD02381', 'LO02381', 2, 448000);
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
('PDT0001', 'QD01538', 'LO01538', 1, N'Thuốc còn nguyên seal'), 
('PDT0002', 'QD01435', 'LO01435', 1, N'Thuốc còn nguyên seal');
GO