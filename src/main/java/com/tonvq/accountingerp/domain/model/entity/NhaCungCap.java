package com.tonvq.accountingerp.domain.model.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity: Nhà Cung Cấp (Supplier)
 * 
 * Đại diện cho nhà cung cấp (nhà máy, nhân viên đại lý, v.v.).
 * 
 * @author Ton VQ
 */
public class NhaCungCap implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;                              // UUID
    private String maNhaCungCap;                    // Mã nhà cung cấp
    private String tenNhaCungCap;                   // Tên nhà cung cấp
    private String loaiNhaCungCap;                  // Loại (Cá nhân, Công ty, Tổng đại lý)
    
    private String diaChi;                          // Địa chỉ
    private String dienThoai;                       // Điện thoại
    private String email;                           // Email
    private String maSoThue;                        // Mã số thuế
    private String nguoiDaiDien;                    // Người đại diện
    
    private double tienPhaiTra = 0;                 // Tiền phải trả
    private double tienTamUng = 0;                  // Tiền tạm ứng
    
    private boolean isActive = true;                // Còn sử dụng?
    
    private String createdBy;                       // Người tạo
    private LocalDateTime createdAt;                // Thời gian tạo
    private String lastModifiedBy;                  // Người sửa cuối
    private LocalDateTime lastModifiedAt;           // Thời gian sửa cuối

    // ============ Constructors ============
    public NhaCungCap() {
    }

    public NhaCungCap(String maNhaCungCap, String tenNhaCungCap, String loaiNhaCungCap, String diaChi) {
        validateNhaCungCap(maNhaCungCap, tenNhaCungCap, loaiNhaCungCap, diaChi);
        this.id = UUID.randomUUID().toString();
        this.maNhaCungCap = maNhaCungCap;
        this.tenNhaCungCap = tenNhaCungCap;
        this.loaiNhaCungCap = loaiNhaCungCap;
        this.diaChi = diaChi;
        this.createdAt = LocalDateTime.now();
    }

    private static void validateNhaCungCap(String maNhaCungCap, String tenNhaCungCap, 
                                          String loaiNhaCungCap, String diaChi) {
        if (maNhaCungCap == null || maNhaCungCap.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã nhà cung cấp không được rỗng");
        }
        if (tenNhaCungCap == null || tenNhaCungCap.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên nhà cung cấp không được rỗng");
        }
        if (loaiNhaCungCap == null || loaiNhaCungCap.trim().isEmpty()) {
            throw new IllegalArgumentException("Loại nhà cung cấp không được rỗng");
        }
        if (diaChi == null || diaChi.trim().isEmpty()) {
            throw new IllegalArgumentException("Địa chỉ không được rỗng");
        }
    }

    // ============ Business Methods ============

    /**
     * Cập nhật số dư phải trả
     */
    public void updateTienPhaiTra(double tienPhaiTra) {
        this.tienPhaiTra = Math.max(0, tienPhaiTra);
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Thanh toán tiền phải trả
     */
    public void thanhToanNo(double soTienThanhToan) {
        if (soTienThanhToan < 0) {
            throw new IllegalArgumentException("Số tiền thanh toán không được âm");
        }
        this.tienPhaiTra = Math.max(0, this.tienPhaiTra - soTienThanhToan);
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Ghi nhận tạm ứng
     */
    public void ghiNhanTamUng(double soTienTamUng) {
        if (soTienTamUng < 0) {
            throw new IllegalArgumentException("Số tiền tạm ứng không được âm");
        }
        this.tienTamUng += soTienTamUng;
        this.lastModifiedAt = LocalDateTime.now();
    }

    // ============ Getters & Setters ============
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMaNhaCungCap() {
        return maNhaCungCap;
    }

    public void setMaNhaCungCap(String maNhaCungCap) {
        this.maNhaCungCap = maNhaCungCap;
    }

    public String getTenNhaCungCap() {
        return tenNhaCungCap;
    }

    public void setTenNhaCungCap(String tenNhaCungCap) {
        this.tenNhaCungCap = tenNhaCungCap;
    }

    public String getLoaiNhaCungCap() {
        return loaiNhaCungCap;
    }

    public void setLoaiNhaCungCap(String loaiNhaCungCap) {
        this.loaiNhaCungCap = loaiNhaCungCap;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getDienThoai() {
        return dienThoai;
    }

    public void setDienThoai(String dienThoai) {
        this.dienThoai = dienThoai;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMaSoThue() {
        return maSoThue;
    }

    public void setMaSoThue(String maSoThue) {
        this.maSoThue = maSoThue;
    }

    public String getNguoiDaiDien() {
        return nguoiDaiDien;
    }

    public void setNguoiDaiDien(String nguoiDaiDien) {
        this.nguoiDaiDien = nguoiDaiDien;
    }

    public double getTienPhaiTra() {
        return tienPhaiTra;
    }

    public void setTienPhaiTra(double tienPhaiTra) {
        this.tienPhaiTra = tienPhaiTra;
    }

    public double getTienTamUng() {
        return tienTamUng;
    }

    public void setTienTamUng(double tienTamUng) {
        this.tienTamUng = tienTamUng;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public LocalDateTime getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(LocalDateTime lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        NhaCungCap that = (NhaCungCap) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("NhaCungCap{id='%s', maNhaCungCap='%s', tenNhaCungCap='%s', tienPhaiTra=%s}",
                id, maNhaCungCap, tenNhaCungCap, tienPhaiTra);
    }
}
