package com.tonvq.accountingerp.domain.model;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Domain Model: Chứng Từ (Voucher/Document)
 * 
 * Chứng từ là tài liệu gốc chứng thực các giao dịch kinh tế.
 * Ví dụ: Hóa đơn, phiếu chi, phiếu thu, biên bản kiểm kho, v.v.
 * 
 * Theo TT 99/2025/TT-BTC: Chứng từ gốc phải được lưu giữ
 * 
 * @author Ton VQ
 */
public class ChungTu implements Serializable {
    private static final long serialVersionUID = 1L;

    // Identity
    private Long id;
    
    // Business fields
    private String maChungTu;           // Mã chứng từ (Voucher Code)
    private String loaiChungTu;         // Loại: HDDON, PHIEUCHU, PHIEOTHU, etc.
    private LocalDate ngayChungTu;      // Ngày chứng từ
    private String ndChungTu;           // Nội dung chứng từ
    private BigDecimal soTien;          // Số tiền
    private String donViTinh;           // Đơn vị tính (VND, USD, etc.)
    
    // Related entities
    private Long nphatHanhId;           // Người phát hành
    private Long nThuHuongId;           // Người thụ hưởng
    
    // Status
    private String trangThai;           // Trạng thái: DRAFT, APPROVED, REJECTED
    
    // Audit fields
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long updatedBy;
    private LocalDateTime updatedAt;
    private String ghiChu;              // Ghi chú

    // ============ Constructors ============
    public ChungTu() {
    }

    public ChungTu(String maChungTu, String loaiChungTu, LocalDate ngayChungTu, 
                   String ndChungTu, BigDecimal soTien) {
        this.maChungTu = maChungTu;
        this.loaiChungTu = loaiChungTu;
        this.ngayChungTu = ngayChungTu;
        this.ndChungTu = ndChungTu;
        this.soTien = soTien;
        this.trangThai = "DRAFT";
        this.donViTinh = "VND";
        this.createdAt = LocalDateTime.now();
    }

    // ============ Business Methods (Domain Logic) ============
    
    /**
     * Duyệt chứng từ (Approve voucher)
     * Chỉ có chứng từ ở trạng thái DRAFT mới có thể duyệt
     */
    public void duyetChungTu(Long userId) {
        if (!"DRAFT".equals(this.trangThai)) {
            throw new IllegalStateException("Chỉ có thể duyệt chứng từ ở trạng thái DRAFT");
        }
        this.trangThai = "APPROVED";
        this.updatedBy = userId;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Từ chối chứng từ (Reject voucher)
     */
    public void tuChoi(Long userId, String lyDo) {
        if (!"DRAFT".equals(this.trangThai)) {
            throw new IllegalStateException("Chỉ có thể từ chối chứng từ ở trạng thái DRAFT");
        }
        this.trangThai = "REJECTED";
        this.ghiChu = "Lý do: " + lyDo;
        this.updatedBy = userId;
        this.updatedAt = LocalDateTime.now();
    }

    /**
     * Kiểm tra chứng từ có hợp lệ không
     */
    public boolean isValid() {
        return this.maChungTu != null && !this.maChungTu.isEmpty() &&
               this.loaiChungTu != null && !this.loaiChungTu.isEmpty() &&
               this.ngayChungTu != null &&
               this.soTien != null && this.soTien.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Kiểm tra chứng từ đã được duyệt chưa
     */
    public boolean isDuyet() {
        return "APPROVED".equals(this.trangThai);
    }

    // ============ Getters & Setters ============
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getMaChungTu() {
        return maChungTu;
    }

    public void setMaChungTu(String maChungTu) {
        this.maChungTu = maChungTu;
    }

    public String getLoaiChungTu() {
        return loaiChungTu;
    }

    public void setLoaiChungTu(String loaiChungTu) {
        this.loaiChungTu = loaiChungTu;
    }

    public LocalDate getNgayChungTu() {
        return ngayChungTu;
    }

    public void setNgayChungTu(LocalDate ngayChungTu) {
        this.ngayChungTu = ngayChungTu;
    }

    public String getNdChungTu() {
        return ndChungTu;
    }

    public void setNdChungTu(String ndChungTu) {
        this.ndChungTu = ndChungTu;
    }

    public BigDecimal getSoTien() {
        return soTien;
    }

    public void setSoTien(BigDecimal soTien) {
        this.soTien = soTien;
    }

    public String getDonViTinh() {
        return donViTinh;
    }

    public void setDonViTinh(String donViTinh) {
        this.donViTinh = donViTinh;
    }

    public Long getNphatHanhId() {
        return nphatHanhId;
    }

    public void setNphatHanhId(Long nphatHanhId) {
        this.nphatHanhId = nphatHanhId;
    }

    public Long getNThuHuongId() {
        return nThuHuongId;
    }

    public void setNThuHuongId(Long nThuHuongId) {
        this.nThuHuongId = nThuHuongId;
    }

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public Long getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(Long createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Long getUpdatedBy() {
        return updatedBy;
    }

    public void setUpdatedBy(Long updatedBy) {
        this.updatedBy = updatedBy;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }

    // ============ equals() & hashCode() ============
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChungTu chungTu = (ChungTu) o;
        return Objects.equals(id, chungTu.id) && 
               Objects.equals(maChungTu, chungTu.maChungTu);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, maChungTu);
    }

    @Override
    public String toString() {
        return "ChungTu{" +
                "id=" + id +
                ", maChungTu='" + maChungTu + '\'' +
                ", loaiChungTu='" + loaiChungTu + '\'' +
                ", ngayChungTu=" + ngayChungTu +
                ", soTien=" + soTien +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
