package com.tonvq.accountingerp.application.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for creating/updating ChungTu
 * 
 * @author Ton VQ
 */
public class ChungTuCreateDTO implements Serializable {
    private static final long serialVersionUID = 1L;
    
    private String maChungTu;
    private String loaiChungTu;
    private LocalDate ngayChungTu;
    private String ndChungTu;
    private BigDecimal soTien;
    private String donViTinh;
    private Long nphatHanhId;
    private Long nThuHuongId;
    private String ghiChu;

    // ============ Constructors ============
    public ChungTuCreateDTO() {
    }

    public ChungTuCreateDTO(String maChungTu, String loaiChungTu, LocalDate ngayChungTu,
                           String ndChungTu, BigDecimal soTien) {
        this.maChungTu = maChungTu;
        this.loaiChungTu = loaiChungTu;
        this.ngayChungTu = ngayChungTu;
        this.ndChungTu = ndChungTu;
        this.soTien = soTien;
        this.donViTinh = "VND";
    }

    // ============ Getters & Setters ============
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

    public String getGhiChu() {
        return ghiChu;
    }

    public void setGhiChu(String ghiChu) {
        this.ghiChu = ghiChu;
    }
}
