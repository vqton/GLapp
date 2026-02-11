package com.tonvq.accountingerp.domain.model.valueobject;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object: Số Lượng (Quantity)
 * 
 * Đại diện cho số lượng sản phẩm/hàng hóa.
 * Hỗ trợ các đơn vị khác nhau (cái, kg, m, liter, v.v.)
 * 
 * @author Ton VQ
 */
public class SoLuong implements Serializable, Comparable<SoLuong> {
    private static final long serialVersionUID = 1L;

    private final BigDecimal value;                 // Giá trị số lượng
    private final String unit;                      // Đơn vị (cái, kg, m, liter, etc.)

    // ============ Constructors ============
    public SoLuong(BigDecimal value, String unit) {
        if (value == null) {
            throw new IllegalArgumentException("Số lượng không được null");
        }
        if (value.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Số lượng không được âm");
        }
        if (unit == null || unit.trim().isEmpty()) {
            throw new IllegalArgumentException("Đơn vị không được rỗng");
        }
        this.value = value.setScale(4, RoundingMode.HALF_UP);  // 4 chữ số thập phân
        this.unit = unit.trim();
    }

    public SoLuong(double value, String unit) {
        this(BigDecimal.valueOf(value), unit);
    }

    public SoLuong(long value, String unit) {
        this(BigDecimal.valueOf(value), unit);
    }

    // ============ Static Factories ============
    public static SoLuong ofCai(double value) {
        return new SoLuong(value, "cái");
    }

    public static SoLuong ofKg(double value) {
        return new SoLuong(value, "kg");
    }

    public static SoLuong ofMeter(double value) {
        return new SoLuong(value, "m");
    }

    public static SoLuong ofLiter(double value) {
        return new SoLuong(value, "liter");
    }

    // ============ Business Methods ============

    /**
     * Cộng số lượng (phải cùng đơn vị)
     */
    public SoLuong add(SoLuong other) {
        if (other == null) {
            return this;
        }
        if (!this.unit.equalsIgnoreCase(other.unit)) {
            throw new IllegalArgumentException(
                String.format("Không thể cộng số lượng khác đơn vị: %s + %s", 
                    this.unit, other.unit)
            );
        }
        return new SoLuong(this.value.add(other.value), this.unit);
    }

    /**
     * Trừ số lượng (phải cùng đơn vị)
     */
    public SoLuong subtract(SoLuong other) {
        if (other == null) {
            return this;
        }
        if (!this.unit.equalsIgnoreCase(other.unit)) {
            throw new IllegalArgumentException(
                String.format("Không thể trừ số lượng khác đơn vị: %s - %s", 
                    this.unit, other.unit)
            );
        }
        BigDecimal result = this.value.subtract(other.value);
        if (result.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("Số lượng không được âm");
        }
        return new SoLuong(result, this.unit);
    }

    /**
     * Nhân số lượng
     */
    public SoLuong multiply(BigDecimal multiplier) {
        if (multiplier == null) {
            throw new IllegalArgumentException("Hệ số nhân không được null");
        }
        return new SoLuong(this.value.multiply(multiplier), this.unit);
    }

    public SoLuong multiply(double multiplier) {
        return multiply(BigDecimal.valueOf(multiplier));
    }

    /**
     * Chia số lượng
     */
    public SoLuong divide(BigDecimal divisor) {
        if (divisor == null || BigDecimal.ZERO.equals(divisor)) {
            throw new IllegalArgumentException("Hệ số chia không được null hoặc 0");
        }
        return new SoLuong(this.value.divide(divisor, 4, RoundingMode.HALF_UP), this.unit);
    }

    /**
     * Kiểm tra bằng 0
     */
    public boolean isZero() {
        return this.value.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Kiểm tra dương
     */
    public boolean isPositive() {
        return this.value.compareTo(BigDecimal.ZERO) > 0;
    }

    // ============ Getters ============
    public BigDecimal getValue() {
        return value;
    }

    public String getUnit() {
        return unit;
    }

    // ============ Comparable ============
    @Override
    public int compareTo(SoLuong other) {
        if (!this.unit.equalsIgnoreCase(other.unit)) {
            throw new IllegalArgumentException("Không thể so sánh số lượng khác đơn vị");
        }
        return this.value.compareTo(other.value);
    }

    // ============ equals & hashCode ============
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SoLuong soLuong = (SoLuong) o;
        return Objects.equals(value, soLuong.value) && 
               Objects.equals(unit, soLuong.unit);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, unit);
    }

    @Override
    public String toString() {
        return String.format("%.4f %s", value, unit);
    }
}
