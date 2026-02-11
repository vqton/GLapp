package com.tonvq.accountingerp.domain.model.valueobject;

import java.io.Serializable;

/**
 * Value Object: Trạng Thái Đơn Hàng (Order Status)
 * 
 * Đại diện cho trạng thái của đơn hàng bán (TMĐT + onsite).
 * 
 * @author Ton VQ
 */
public enum TrangThaiDonHang implements Serializable {
    /**
     * Dự thảo
     */
    DRAFT("Dự thảo"),

    /**
     * Đã xác nhận
     */
    CONFIRMED("Đã xác nhận"),

    /**
     * Đang giao
     */
    SHIPPING("Đang giao"),

    /**
     * Đã giao
     */
    DELIVERED("Đã giao"),

    /**
     * Đã thanh toán
     */
    PAID("Đã thanh toán"),

    /**
     * Bị hủy
     */
    CANCELLED("Bị hủy");

    private final String label;

    TrangThaiDonHang(String label) {
        this.label = label;
    }

    public String getLabel() {
        return label;
    }

    /**
     * Có thể giao được không?
     */
    public boolean canShip() {
        return this == CONFIRMED || this == SHIPPING;
    }

    /**
     * Có thể thanh toán không?
     */
    public boolean canPay() {
        return this == DELIVERED || this == SHIPPED;
    }

    public boolean isCompleted() {
        return this == PAID;
    }

    public boolean isCancelled() {
        return this == CANCELLED;
    }

    @Override
    public String toString() {
        return label;
    }

    private static final TrangThaiDonHang SHIPPED = DELIVERED;
}
