package com.tonvq.accountingerp.domain.model.entity;

import com.tonvq.accountingerp.domain.model.valueobject.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Entity: Tồn Kho (Inventory)
 * 
 * Đại diện cho hàng tồn kho của công ty.
 * Quản lý số lượng, giá vốn, và các phương pháp tính giá (FIFO/LIFO).
 * 
 * Theo TT 99/2025/TT-BTC:
 * - Định giá hàng tồn kho theo giá gốc hoặc giá thị trường
 * - Hạch toán theo phương pháp FIFO, LIFO, hoặc trung bình gia quyền
 * - Kế toán phải lập bảng tồn kho cuối kỳ
 * 
 * @author Ton VQ
 */
public class TonKho implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;                              // UUID
    private String maSanPham;                       // Mã sản phẩm
    private String tenSanPham;                      // Tên sản phẩm
    private String moTa;                            // Mô tả

    // ============ Quantity & Price ============
    private SoLuong soLuongDau;                     // Số lượng đầu kỳ
    private GiaVon giaVonDau;                       // Giá vốn đầu kỳ
    
    private SoLuong soLuongNhap;                    // Số lượng nhập trong kỳ
    private GiaVon giaVonNhap;                      // Giá vốn nhập
    
    private SoLuong soLuongXuat;                    // Số lượng xuất (bán) trong kỳ
    private GiaVon giaVonXuat;                      // Giá vốn xuất
    
    private SoLuong soLuongCuoi;                    // Số lượng cuối kỳ
    private GiaVon giaVonCuoi;                      // Giá vốn cuối kỳ

    // ============ Pricing Method ============
    private PhuongThucTinhGia phuongPhapTinh;       // FIXED, PROFIT_MARGIN, COST_PLUS
    private double phanTramLaiSuatBan = 0;          // % lợi suất bán (nếu dùng)

    // ============ Kho Info ============
    private String maKho;                           // Mã kho
    private String tenKho;                          // Tên kho
    private String viTriKho;                        // Vị trí trong kho

    // ============ Audit ============
    private LocalDateTime createdAt;                // Thời gian tạo
    private String createdBy;                       // Người tạo
    private LocalDateTime lastModifiedAt;           // Thời gian sửa cuối
    private String lastModifiedBy;                  // Người sửa cuối

    private boolean isActive = true;                // Còn sử dụng?

    // ============ Constructors ============
    public TonKho() {
    }

    public TonKho(String maSanPham, String tenSanPham, SoLuong soLuongDau, GiaVon giaVonDau) {
        validateTonKho(maSanPham, tenSanPham, soLuongDau, giaVonDau);
        this.id = UUID.randomUUID().toString();
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.soLuongDau = soLuongDau;
        this.giaVonDau = giaVonDau;
        this.soLuongCuoi = soLuongDau;
        this.giaVonCuoi = giaVonDau;
        this.createdAt = LocalDateTime.now();
        this.phuongPhapTinh = PhuongThucTinhGia.FIXED_PRICE;
    }

    private static void validateTonKho(String maSanPham, String tenSanPham, SoLuong soLuongDau, GiaVon giaVonDau) {
        if (maSanPham == null || maSanPham.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã sản phẩm không được rỗng");
        }
        if (tenSanPham == null || tenSanPham.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên sản phẩm không được rỗng");
        }
        if (soLuongDau == null || !soLuongDau.isPositive()) {
            throw new IllegalArgumentException("Số lượng đầu kỳ phải > 0");
        }
        if (giaVonDau == null || giaVonDau.isZero()) {
            throw new IllegalArgumentException("Giá vốn đầu kỳ không được null hoặc 0");
        }
    }

    // ============ Business Methods ============

    /**
     * Nhập hàng
     */
    public void nhapHang(SoLuong soLuongNhap, GiaVon giaVonNhap, String nhapBy) {
        if (soLuongNhap == null || !soLuongNhap.isPositive()) {
            throw new IllegalArgumentException("Số lượng nhập phải > 0");
        }
        if (giaVonNhap == null) {
            throw new IllegalArgumentException("Giá vốn nhập không được null");
        }
        
        this.soLuongNhap = soLuongNhap;
        this.giaVonNhap = giaVonNhap;
        
        // Cập nhật tồn kho cuối
        this.soLuongCuoi = this.soLuongCuoi.add(soLuongNhap);
        this.updateGiaVonCuoi();
        
        this.lastModifiedBy = nhapBy;
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Xuất hàng (bán)
     */
    public void xuatHang(SoLuong soLuongXuat, GiaVon giaVonXuat, String xuatBy) {
        if (soLuongXuat == null || !soLuongXuat.isPositive()) {
            throw new IllegalArgumentException("Số lượng xuất phải > 0");
        }
        if (giaVonXuat == null) {
            throw new IllegalArgumentException("Giá vốn xuất không được null");
        }
        if (this.soLuongCuoi.compareTo(soLuongXuat) < 0) {
            throw new IllegalArgumentException(
                String.format("Không đủ hàng để xuất. Tồn kho: %s, Xuất: %s", 
                    this.soLuongCuoi, soLuongXuat)
            );
        }
        
        this.soLuongXuat = soLuongXuat;
        this.giaVonXuat = giaVonXuat;
        
        // Cập nhật tồn kho cuối
        this.soLuongCuoi = this.soLuongCuoi.subtract(soLuongXuat);
        this.updateGiaVonCuoi();
        
        this.lastModifiedBy = xuatBy;
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Cập nhật giá vốn cuối kỳ (theo phương pháp FIFO/LIFO/Trung bình)
     */
    private void updateGiaVonCuoi() {
        if (this.soLuongCuoi.isZero()) {
            this.giaVonCuoi = new GiaVon(Tien.zeroVND(), GiaVon.FIFO);
        } else {
            // Cách tính đơn giản: lấy giá vốn cuối
            if (this.giaVonXuat != null && !this.giaVonXuat.isZero()) {
                this.giaVonCuoi = this.giaVonXuat;
            } else if (this.giaVonNhap != null && !this.giaVonNhap.isZero()) {
                this.giaVonCuoi = this.giaVonNhap;
            }
        }
    }

    /**
     * Tính tổng giá trị tồn kho cuối kỳ
     */
    public Tien getTongGiaTriTonKho() {
        return this.giaVonCuoi.timesQuantity(this.soLuongCuoi);
    }

    /**
     * Tính lợi nhuận bán (nếu có)
     */
    public Tien calculateLaiSuat(Tien giaBan) {
        Tien giaVonTotal = getTongGiaTriTonKho();
        return giaBan.subtract(giaVonTotal);
    }

    /**
     * Hạ giá (điều chỉnh giá vốn)
     */
    public void haGia(GiaVon giaVonMoi, String haGiaBy) {
        if (giaVonMoi == null || giaVonMoi.isZero()) {
            throw new IllegalArgumentException("Giá vốn mới không được null hoặc 0");
        }
        this.giaVonCuoi = giaVonMoi;
        this.lastModifiedBy = haGiaBy;
        this.lastModifiedAt = LocalDateTime.now();
    }

    // ============ Getters & Setters ============
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMaSanPham() {
        return maSanPham;
    }

    public void setMaSanPham(String maSanPham) {
        this.maSanPham = maSanPham;
    }

    public String getTenSanPham() {
        return tenSanPham;
    }

    public void setTenSanPham(String tenSanPham) {
        this.tenSanPham = tenSanPham;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public SoLuong getSoLuongDau() {
        return soLuongDau;
    }

    public void setSoLuongDau(SoLuong soLuongDau) {
        this.soLuongDau = soLuongDau;
    }

    public GiaVon getGiaVonDau() {
        return giaVonDau;
    }

    public void setGiaVonDau(GiaVon giaVonDau) {
        this.giaVonDau = giaVonDau;
    }

    public SoLuong getSoLuongNhap() {
        return soLuongNhap;
    }

    public void setSoLuongNhap(SoLuong soLuongNhap) {
        this.soLuongNhap = soLuongNhap;
    }

    public GiaVon getGiaVonNhap() {
        return giaVonNhap;
    }

    public void setGiaVonNhap(GiaVon giaVonNhap) {
        this.giaVonNhap = giaVonNhap;
    }

    public SoLuong getSoLuongXuat() {
        return soLuongXuat;
    }

    public void setSoLuongXuat(SoLuong soLuongXuat) {
        this.soLuongXuat = soLuongXuat;
    }

    public GiaVon getGiaVonXuat() {
        return giaVonXuat;
    }

    public void setGiaVonXuat(GiaVon giaVonXuat) {
        this.giaVonXuat = giaVonXuat;
    }

    public SoLuong getSoLuongCuoi() {
        return soLuongCuoi;
    }

    public void setSoLuongCuoi(SoLuong soLuongCuoi) {
        this.soLuongCuoi = soLuongCuoi;
    }

    public GiaVon getGiaVonCuoi() {
        return giaVonCuoi;
    }

    public void setGiaVonCuoi(GiaVon giaVonCuoi) {
        this.giaVonCuoi = giaVonCuoi;
    }

    public PhuongThucTinhGia getPhuongPhapTinh() {
        return phuongPhapTinh;
    }

    public void setPhuongPhapTinh(PhuongThucTinhGia phuongPhapTinh) {
        this.phuongPhapTinh = phuongPhapTinh;
    }

    public double getPhanTramLaiSuatBan() {
        return phanTramLaiSuatBan;
    }

    public void setPhanTramLaiSuatBan(double phanTramLaiSuatBan) {
        this.phanTramLaiSuatBan = phanTramLaiSuatBan;
    }

    public String getMaKho() {
        return maKho;
    }

    public void setMaKho(String maKho) {
        this.maKho = maKho;
    }

    public String getTenKho() {
        return tenKho;
    }

    public void setTenKho(String tenKho) {
        this.tenKho = tenKho;
    }

    public String getViTriKho() {
        return viTriKho;
    }

    public void setViTriKho(String viTriKho) {
        this.viTriKho = viTriKho;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(LocalDateTime lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TonKho tonKho = (TonKho) o;
        return Objects.equals(id, tonKho.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("TonKho{id='%s', maSanPham='%s', tenSanPham='%s', soLuongCuoi=%s, giaVonCuoi=%s}",
                id, maSanPham, tenSanPham, soLuongCuoi, giaVonCuoi);
    }
}
