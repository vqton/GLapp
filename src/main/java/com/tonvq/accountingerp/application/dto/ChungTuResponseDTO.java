package com.tonvq.accountingerp.application.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for ChungTu Response
 * 
 * @author Ton VQ
 */
public class ChungTuResponseDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private Long id;
    private String maChungTu;
    private String loaiChungTu;
    private LocalDate ngayChungTu;
    private String ndChungTu;
    private BigDecimal soTien;
    private String donViTinh;
    private String trangThai;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String ghiChu;

    // ============ Constructors ============
    public ChungTuResponseDTO() {
    }

    public ChungTuResponseDTO(Long id, String maChungTu, String loaiChungTu, 
                             LocalDate ngayChungTu, BigDecimal soTien, String trangThai) {
        this.id = id;
        this.maChungTu = maChungTu;
        this.loaiChungTu = loaiChungTu;
        this.ngayChungTu = ngayChungTu;
        this.soTien = soTien;
        this.trangThai = trangThai;
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

    public String getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(String trangThai) {
        this.trangThai = trangThai;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
}
