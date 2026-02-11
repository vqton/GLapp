-- Initialize PostgreSQL Database for AccountingERP
-- This script creates the database, user, and initial schema

-- ========== CREATE USER & DATABASE ==========

-- Tạo user
CREATE USER IF NOT EXISTS accounting WITH PASSWORD 'postgres';

-- Tạo database
CREATE DATABASE IF NOT EXISTS accounting_erp 
  OWNER accounting 
  ENCODING 'UTF8' 
  LC_COLLATE 'en_US.UTF-8' 
  LC_CTYPE 'en_US.UTF-8';

-- ========== GRANT PERMISSIONS ==========

-- Kết nối database
\c accounting_erp

-- Cấp quyền cho user accounting
GRANT CONNECT ON DATABASE accounting_erp TO accounting;
GRANT USAGE ON SCHEMA public TO accounting;
GRANT CREATE ON SCHEMA public TO accounting;
GRANT ALL PRIVILEGES ON ALL TABLES IN SCHEMA public TO accounting;
GRANT ALL PRIVILEGES ON ALL SEQUENCES IN SCHEMA public TO accounting;

-- ========== CREATE TABLES ==========

-- Bảng Chứng Từ (Vouchers)
CREATE TABLE IF NOT EXISTS chung_tu (
    id BIGSERIAL PRIMARY KEY,
    ma_chung_tu VARCHAR(50) UNIQUE NOT NULL,
    loai_chung_tu VARCHAR(50) NOT NULL,
    ngay_chung_tu DATE NOT NULL,
    nd_chung_tu TEXT,
    so_tien DECIMAL(18, 2) NOT NULL,
    don_vi_tinh VARCHAR(10) DEFAULT 'VND',
    n_phat_hanh_id BIGINT,
    n_thu_huong_id BIGINT,
    trang_thai VARCHAR(20) DEFAULT 'DRAFT',
    created_by BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by BIGINT,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    ghi_chu TEXT,
    CONSTRAINT chk_so_tien CHECK (so_tien >= 0),
    CONSTRAINT chk_trang_thai CHECK (trang_thai IN ('DRAFT', 'APPROVED', 'REJECTED'))
);

-- Indexes
CREATE INDEX idx_chung_tu_ma ON chung_tu(ma_chung_tu);
CREATE INDEX idx_chung_tu_loai ON chung_tu(loai_chung_tu);
CREATE INDEX idx_chung_tu_ngay ON chung_tu(ngay_chung_tu);
CREATE INDEX idx_chung_tu_trang_thai ON chung_tu(trang_thai);

-- ========== INITIAL DATA ==========

-- Sample Chứng Từ
INSERT INTO chung_tu (ma_chung_tu, loai_chung_tu, ngay_chung_tu, nd_chung_tu, so_tien, trang_thai)
VALUES 
    ('CT001', 'HDDON', CURRENT_DATE, 'Hóa đơn bán hàng mẫu', 1000000, 'DRAFT'),
    ('CT002', 'PHIEUCHU', CURRENT_DATE, 'Phiếu chi lương', 5000000, 'APPROVED'),
    ('CT003', 'PHIEOTHU', CURRENT_DATE, 'Phiếu thu doanh thu', 2500000, 'APPROVED');

-- ========== VERIFY ==========

-- Liệt kê bảng
\dt

-- Kiểm tra dữ liệu
SELECT COUNT(*) as so_chung_tu FROM chung_tu;

-- Kiểm tra user
\du

COMMIT;
