package com.tonvq.accountingerp.domain.model.entity;

import com.tonvq.accountingerp.domain.model.valueobject.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity: Bút Toán (Journal Entry)
 * 
 * Đại diện cho một bút toán (dòng chi tiết của chứng từ).
 * Mỗi bút toán ghi nợ hoặc có vào một tài khoản.
 * 
 * Theo TT 99/2025/TT-BTC:
 * - Mỗi bút toán phải có TK nợ, TK có, số tiền
 * - Chứng từ phải cân bằng (Nợ = Có)
 * - Phải ghi vào sổ Nhật ký, sổ Cái
 * 
 * @author Ton VQ
 */
public class ButToan implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;                              // UUID
    private String tkNo;                            // Tài khoản nợ
    private String tkCo;                            // Tài khoản có
    private Tien soTien;                            // Số tiền
    private String moTa;                            // Mô tả bút toán
    
    private String chungTuId;                       // Tham chiếu tới ChungTu

    private LocalDateTime createdAt;                // Thời gian tạo
    private String createdBy;                       // Người tạo

    // ============ Constructors ============
    public ButToan() {
    }

    public ButToan(String tkNo, String tkCo, Tien soTien, String moTa) {
        validateButToan(tkNo, tkCo, soTien);
        this.id = UUID.randomUUID().toString();
        this.tkNo = tkNo;
        this.tkCo = tkCo;
        this.soTien = soTien;
        this.moTa = moTa != null ? moTa : "";
        this.createdAt = LocalDateTime.now();
    }

    // ============ Validation ============
    private static void validateButToan(String tkNo, String tkCo, Tien soTien) {
        if (tkNo == null || tkNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Tài khoản nợ không được rỗng");
        }
        if (tkCo == null || tkCo.trim().isEmpty()) {
            throw new IllegalArgumentException("Tài khoản có không được rỗng");
        }
        if (tkNo.equals(tkCo)) {
            throw new IllegalArgumentException("Tài khoản nợ và có phải khác nhau");
        }
        if (soTien == null || soTien.isNegative()) {
            throw new IllegalArgumentException("Số tiền phải là số dương");
        }
    }

    // ============ Business Methods ============

    /**
     * Kiểm tra bút toán nợ
     */
    public boolean isNo() {
        // Để đơn giản, coi tất cả là nợ (trong thực tế có logic phức tạp hơn)
        return true;
    }

    /**
     * Kiểm tra bút toán có
     */
    public boolean isCo() {
        return !isNo();
    }

    // ============ Getters & Setters ============
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getTkNo() {
        return tkNo;
    }

    public void setTkNo(String tkNo) {
        this.tkNo = tkNo;
    }

    public String getTkCo() {
        return tkCo;
    }

    public void setTkCo(String tkCo) {
        this.tkCo = tkCo;
    }

    public Tien getSoTien() {
        return soTien;
    }

    public void setSoTien(Tien soTien) {
        this.soTien = soTien;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public String getChungTuId() {
        return chungTuId;
    }

    public void setChungTuId(String chungTuId) {
        this.chungTuId = chungTuId;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ButToan butToan = (ButToan) o;
        return Objects.equals(id, butToan.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("ButToan{id='%s', tkNo='%s', tkCo='%s', soTien=%s}",
                id, tkNo, tkCo, soTien);
    }
}
