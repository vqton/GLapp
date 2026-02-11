package com.tonvq.accountingerp.domain.model.valueobject;

import java.io.Serializable;

/**
 * Value Object: Phương Thức Tính Giá (Pricing Method)
 * 
 * Đại diện cho các phương pháp tính giá bán:
 * - Giá cố định
 * - Theo tỷ lệ lợi nhuận
 * - Theo chi phí + mark-up
 * 
 * @author Ton VQ
 */
public enum PhuongThucTinhGia implements Serializable {
    /**
     * Giá cố định
     */
    FIXED_PRICE("Giá cố định", "Nhập giá bán cụ thể"),

    /**
     * Theo tỷ lệ lợi nhuận
     */
    PROFIT_MARGIN("Tỷ lệ lợi nhuận", "Giá = Giá vốn / (1 - Tỷ lệ lợi nhuận)"),

    /**
     * Theo chi phí + mark-up
     */
    COST_PLUS_MARKUP("Chi phí + Mark-up", "Giá = Giá vốn * (1 + Mark-up%)");

    private final String name;
    private final String description;

    PhuongThucTinhGia(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    @Override
    public String toString() {
        return name;
    }
}
