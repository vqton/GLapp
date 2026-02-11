package com.tonvq.accountingerp.domain.model.valueobject;

import java.io.Serializable;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * Value Object: Tiền (Money)
 * 
 * Đại diện cho một số tiền với đơn vị tiền tệ.
 * Cung cấp các phép toán tiền tệ an toàn.
 * 
 * Theo TT 99/2025/TT-BTC: Tính toán BCTC phải chính xác với 2 chữ số thập phân.
 * 
 * @author Ton VQ
 */
public class Tien implements Serializable, Comparable<Tien> {
    private static final long serialVersionUID = 1L;
    
    private static final int SCALE = 2;                          // 2 chữ số thập phân
    private static final RoundingMode ROUNDING_MODE = RoundingMode.HALF_UP;
    
    private final BigDecimal amount;                             // Số tiền
    private final TienTe currency;                               // Tiền tệ

    // ============ Constructors ============
    public Tien(BigDecimal amount, TienTe currency) {
        if (amount == null) {
            throw new IllegalArgumentException("Số tiền không được null");
        }
        if (currency == null) {
            throw new IllegalArgumentException("Tiền tệ không được null");
        }
        // Làm tròn theo VN: 2 chữ số thập phân, làm tròn lên từ 0.5
        this.amount = amount.setScale(SCALE, ROUNDING_MODE);
        this.currency = currency;
    }

    public Tien(long amountInCents, TienTe currency) {
        this(BigDecimal.valueOf(amountInCents).divide(BigDecimal.valueOf(100)), currency);
    }

    public Tien(double amount, TienTe currency) {
        this(BigDecimal.valueOf(amount), currency);
    }

    // ============ Static Factories ============
    public static Tien zeroVND() {
        return new Tien(BigDecimal.ZERO, TienTe.vnd());
    }

    public static Tien ofVND(long amount) {
        return new Tien(amount, TienTe.vnd());
    }

    public static Tien ofVND(BigDecimal amount) {
        return new Tien(amount, TienTe.vnd());
    }

    public static Tien ofVND(double amount) {
        return new Tien(amount, TienTe.vnd());
    }

    // ============ Business Methods ============

    /**
     * Cộng tiền (phải cùng tiền tệ)
     */
    public Tien add(Tien other) {
        if (other == null) {
            return this;
        }
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                String.format("Không thể cộng tiền khác tệ: %s + %s", 
                    this.currency.getCode(), other.currency.getCode())
            );
        }
        return new Tien(this.amount.add(other.amount), this.currency);
    }

    /**
     * Trừ tiền (phải cùng tiền tệ)
     */
    public Tien subtract(Tien other) {
        if (other == null) {
            return this;
        }
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException(
                String.format("Không thể trừ tiền khác tệ: %s - %s", 
                    this.currency.getCode(), other.currency.getCode())
            );
        }
        return new Tien(this.amount.subtract(other.amount), this.currency);
    }

    /**
     * Nhân tiền với hệ số
     */
    public Tien multiply(BigDecimal multiplier) {
        if (multiplier == null) {
            throw new IllegalArgumentException("Hệ số nhân không được null");
        }
        return new Tien(this.amount.multiply(multiplier).setScale(SCALE, ROUNDING_MODE), this.currency);
    }

    public Tien multiply(double multiplier) {
        return multiply(BigDecimal.valueOf(multiplier));
    }

    /**
     * Chia tiền cho hệ số
     */
    public Tien divide(BigDecimal divisor) {
        if (divisor == null || BigDecimal.ZERO.equals(divisor)) {
            throw new IllegalArgumentException("Hệ số chia không được null hoặc 0");
        }
        return new Tien(this.amount.divide(divisor, SCALE, ROUNDING_MODE), this.currency);
    }

    /**
     * Kiểm tra có âm không
     */
    public boolean isNegative() {
        return this.amount.compareTo(BigDecimal.ZERO) < 0;
    }

    /**
     * Kiểm tra bằng 0
     */
    public boolean isZero() {
        return this.amount.compareTo(BigDecimal.ZERO) == 0;
    }

    /**
     * Kiểm tra dương
     */
    public boolean isPositive() {
        return this.amount.compareTo(BigDecimal.ZERO) > 0;
    }

    /**
     * Lấy giá trị tuyệt đối
     */
    public Tien abs() {
        return new Tien(this.amount.abs(), this.currency);
    }

    // ============ Getters ============
    public BigDecimal getAmount() {
        return amount;
    }

    public TienTe getCurrency() {
        return currency;
    }

    public long getAmountInCents() {
        return this.amount.multiply(BigDecimal.valueOf(100)).longValue();
    }

    // ============ Comparable ============
    @Override
    public int compareTo(Tien other) {
        if (!this.currency.equals(other.currency)) {
            throw new IllegalArgumentException("Không thể so sánh tiền khác tệ");
        }
        return this.amount.compareTo(other.amount);
    }

    // ============ equals & hashCode ============
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Tien tien = (Tien) o;
        return Objects.equals(amount, tien.amount) && 
               Objects.equals(currency, tien.currency);
    }

    @Override
    public int hashCode() {
        return Objects.hash(amount, currency);
    }

    @Override
    public String toString() {
        return String.format("%s %,.0f", currency.getSymbol(), amount);
    }
}
