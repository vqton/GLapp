package com.tonvq.accountingerp.domain.model.valueobject;

import java.io.Serializable;

/**
 * Value Object: Trạng Thái Chứng Từ (Document Status)
 * 
 * Đại diện cho trạng thái của chứng từ kế toán.
 * 
 * Theo TT 99/2025/TT-BTC Phụ lục I:
 * - Chứng từ gốc phải được ký số (nếu ký số)
 * - Không được sửa chữa sau khi khóa sổ kỳ
 * - Phải lưu trữ đầy đủ
 * 
 * @author Ton VQ
 */
public enum TrangThaiChungTu implements Serializable {
    /**
     * Dự thảo - vừa tạo mới, chưa ghi sổ
     */
    DRAFT("Dự thảo", "CHỈ TẠOVÀ CÓ THỂ CHỈNH SỬA"),

    /**
     * Đã ghi sổ - ghi vào sổ cái
     */
    POSTED("Đã ghi sổ", "ĐÃ GHI VÀO SỔ CÁI"),

    /**
     * Khóa - khóa sổ kỳ, không được sửa
     */
    LOCKED("Khóa", "KHÓA SỔ KỲ - KHÔNG ĐƯỢC SỬA"),

    /**
     * Hủy - chứng từ bị hủy
     */
    CANCELLED("Hủy", "CHỨNG TỪ BỊ HỦY - KHÔNG CÓ GHI SỔ");

    private final String label;                     // Nhãn hiển thị
    private final String description;               // Mô tả

    // ============ Constructor ============
    TrangThaiChungTu(String label, String description) {
        this.label = label;
        this.description = description;
    }

    // ============ Business Methods ============

    /**
     * Có thể sửa chứng từ không?
     * Chỉ sửa được khi trạng thái là DRAFT
     */
    public boolean canEdit() {
        return this == DRAFT;
    }

    /**
     * Có thể ghi sổ không?
     * Chỉ ghi được khi trạng thái là DRAFT
     */
    public boolean canPost() {
        return this == DRAFT;
    }

    /**
     * Có thể khóa không?
     * Chỉ khóa được khi trạng thái là POSTED
     */
    public boolean canLock() {
        return this == POSTED;
    }

    /**
     * Có thể hủy không?
     * Có thể hủy khi trạng thái là DRAFT hoặc POSTED
     */
    public boolean canCancel() {
        return this == DRAFT || this == POSTED;
    }

    /**
     * Chứng từ đã khóa chưa?
     */
    public boolean isLocked() {
        return this == LOCKED;
    }

    /**
     * Chứng từ đã hủy chưa?
     */
    public boolean isCancelled() {
        return this == CANCELLED;
    }

    /**
     * Chứng từ đã ghi sổ chưa?
     */
    public boolean isPosted() {
        return this == POSTED;
    }

    // ============ Getters ============
    public String getLabel() {
        return label;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return label;
    }
}
