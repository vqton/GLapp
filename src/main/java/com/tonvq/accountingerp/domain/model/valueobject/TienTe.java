package com.tonvq.accountingerp.domain.model.valueobject;

import java.io.Serializable;
import java.util.Objects;

/**
 * Value Object: Tiền Tệ (Currency)
 * 
 * Đại diện cho một loại tiền tệ theo tiêu chuẩn ISO 4217.
 * Ví dụ: VND (Đồng Việt Nam), USD, EUR, v.v.
 * 
 * Theo TT 99/2025/TT-BTC Điều 31: Xác định tỷ giá ngoại tệ tại ngày hạch toán.
 * 
 * @author Ton VQ
 */
public class TienTe implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String code;              // ISO code: VND, USD, EUR, etc.
    private final String name;              // Tên tiền tệ
    private final String symbol;            // Ký hiệu: ₫, $, €, etc.
    
    // ============ Constructors ============
    public TienTe(String code, String name, String symbol) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã tiền tệ không được rỗng");
        }
        if (code.length() != 3) {
            throw new IllegalArgumentException("Mã tiền tệ phải là 3 ký tự ISO (ví dụ: VND, USD)");
        }
        this.code = code.toUpperCase();
        this.name = name != null ? name : "";
        this.symbol = symbol != null ? symbol : code;
    }

    // ============ Static Factories ============
    public static TienTe vnd() {
        return new TienTe("VND", "Đồng Việt Nam", "₫");
    }

    public static TienTe usd() {
        return new TienTe("USD", "Đô la Mỹ", "$");
    }

    public static TienTe eur() {
        return new TienTe("EUR", "Euro", "€");
    }

    // ============ Business Methods ============
    public boolean isVND() {
        return "VND".equals(this.code);
    }

    public boolean isForeignCurrency() {
        return !isVND();
    }

    // ============ Getters ============
    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }

    public String getSymbol() {
        return symbol;
    }

    // ============ equals & hashCode ============
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TienTe tienTe = (TienTe) o;
        return Objects.equals(code, tienTe.code);
    }

    @Override
    public int hashCode() {
        return Objects.hash(code);
    }

    @Override
    public String toString() {
        return code + " (" + name + ")";
    }
}
