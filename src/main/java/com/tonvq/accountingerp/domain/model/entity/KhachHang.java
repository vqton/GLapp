package com.tonvq.accountingerp.domain.model.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity: Khách Hàng (Customer)
 * 
 * Đại diện cho khách hàng (cá nhân hoặc tổ chức).
 * 
 * @author Ton VQ
 */
public class KhachHang implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;                              // UUID
    private String maKhachHang;                     // Mã khách hàng
    private String tenKhachHang;                    // Tên khách hàng
    private String loaiKhachHang;                   // Cá nhân hay Tổ chức
    
    private String diaChi;                          // Địa chỉ
    private String dienThoai;                       // Điện thoại
    private String email;                           // Email
    private String maSoThue;                        // Mã số thuế (nếu là tổ chức)
    private String nguoiDaiDien;                    // Người đại diện (nếu là tổ chức)
    
    private double tienNo = 0;                      // Tiền nợ
    private double tienCoNo = 0;                    // Tiền có nợ (ứng trước)
    
    private boolean isActive = true;                // Còn sử dụng?
    
    private String createdBy;                       // Người tạo
    private LocalDateTime createdAt;                // Thời gian tạo
    private String lastModifiedBy;                  // Người sửa cuối
    private LocalDateTime lastModifiedAt;           // Thời gian sửa cuối

    // ============ Constructors ============
    public KhachHang() {
    }

    public KhachHang(String maKhachHang, String tenKhachHang, String loaiKhachHang, String diaChi) {
        validateKhachHang(maKhachHang, tenKhachHang, loaiKhachHang, diaChi);
        this.id = UUID.randomUUID().toString();
        this.maKhachHang = maKhachHang;
        this.tenKhachHang = tenKhachHang;
        this.loaiKhachHang = loaiKhachHang;
        this.diaChi = diaChi;
        this.createdAt = LocalDateTime.now();
    }

    private static void validateKhachHang(String maKhachHang, String tenKhachHang, 
                                         String loaiKhachHang, String diaChi) {
        if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khách hàng không được rỗng");
        }
        if (tenKhachHang == null || tenKhachHang.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên khách hàng không được rỗng");
        }
        if (loaiKhachHang == null || loaiKhachHang.trim().isEmpty()) {
            throw new IllegalArgumentException("Loại khách hàng không được rỗng");
        }
        if (diaChi == null || diaChi.trim().isEmpty()) {
            throw new IllegalArgumentException("Địa chỉ không được rỗng");
        }
    }

    // ============ Business Methods ============

    /**
     * Cập nhật số dư nợ
     */
    public void updateTienNo(double tienNo) {
        this.tienNo = Math.max(0, tienNo);
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Thanh toán tiền nợ
     */
    public void thanhToanNo(double soTienThanhToan) {
        if (soTienThanhToan < 0) {
            throw new IllegalArgumentException("Số tiền thanh toán không được âm");
        }
        this.tienNo = Math.max(0, this.tienNo - soTienThanhToan);
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Ghi nhận ứng trước
     */
    public void ghiNhanUngTruoc(double soTienUngTruoc) {
        if (soTienUngTruoc < 0) {
            throw new IllegalArgumentException("Số tiền ứng trước không được âm");
        }
        this.tienCoNo += soTienUngTruoc;
        this.lastModifiedAt = LocalDateTime.now();
    }

    // ============ Getters & Setters ============
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getLoaiKhachHang() {
        return loaiKhachHang;
    }

    public void setLoaiKhachHang(String loaiKhachHang) {
        this.loaiKhachHang = loaiKhachHang;
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

    public double getTienNo() {
        return tienNo;
    }

    public void setTienNo(double tienNo) {
        this.tienNo = tienNo;
    }

    public double getTienCoNo() {
        return tienCoNo;
    }

    public void setTienCoNo(double tienCoNo) {
        this.tienCoNo = tienCoNo;
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
        KhachHang khachHang = (KhachHang) o;
        return Objects.equals(id, khachHang.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("KhachHang{id='%s', maKhachHang='%s', tenKhachHang='%s', tienNo=%s}",
                id, maKhachHang, tenKhachHang, tienNo);
    }
}
