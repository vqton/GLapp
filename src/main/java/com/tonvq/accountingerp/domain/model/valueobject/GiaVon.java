package com.tonvq.accountingerp.domain.model.valueobject;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Objects;

/**
 * Value Object: Giá Vốn (Cost Price)
 * 
 * Đại diện cho giá vốn của một sản phẩm/hàng hóa.
 * Hỗ trợ các phương pháp tính: FIFO, LIFO, Trung bình.
 * 
 * Theo TT 99/2025/TT-BTC & Chuẩn kế toán VN:
 * - Tính giá vốn hàng bán theo FIFO, LIFO, hoặc trung bình gia quyền.
 * - Không được âm dương (luôn >= 0).
 * 
 * @author Ton VQ
 */
public class GiaVon implements Serializable, Comparable<GiaVon> {
    private static final long serialVersionUID = 1L;

    private final Tien gia;                         // Giá vốn = Tien (VND hoặc ngoại tệ)
    private final String phuongPhap;                // FIFO, LIFO, TRUNG_BINH

    // ============ Constants ============
    public static final String FIFO = "FIFO";
    public static final String LIFO = "LIFO";
    public static final String TRUNG_BINH = "TRUNG_BINH";

    // ============ Constructors ============
    public GiaVon(Tien gia, String phuongPhap) {
        if (gia == null) {
            throw new IllegalArgumentException("Giá vốn không được null");
        }
        if (gia.isNegative()) {
            throw new IllegalArgumentException("Giá vốn không được âm");
        }
        if (phuongPhap == null || phuongPhap.trim().isEmpty()) {
            throw new IllegalArgumentException("Phương pháp tính giá vốn không được rỗng");
        }
        String method = phuongPhap.toUpperCase().trim();
        if (!isValidMethod(method)) {
            throw new IllegalArgumentException(
                String.format("Phương pháp không hợp lệ: %s. Chỉ chấp nhận: FIFO, LIFO, TRUNG_BINH", method)
            );
        }
        this.gia = gia;
        this.phuongPhap = method;
    }

    // ============ Static Factories ============
    public static GiaVon fifoVND(BigDecimal gia) {
        return new GiaVon(new Tien(gia, TienTe.vnd()), FIFO);
    }

    public static GiaVon lifoBinh(BigDecimal gia) {
        return new GiaVon(new Tien(gia, TienTe.vnd()), LIFO);
    }

    public static GiaVon trungBinh(Tien gia) {
        return new GiaVon(gia, TRUNG_BINH);
    }

    // ============ Business Methods ============

    /**
     * Kiểm tra phương pháp có hợp lệ không
     */
    private static boolean isValidMethod(String method) {
        return FIFO.equals(method) || LIFO.equals(method) || TRUNG_BINH.equals(method);
    }

    /**
     * Cập nhật giá vốn (dùng cho tính toán trung bình)
     */
    public GiaVon updateGia(GiaVon other) {
        if (other == null) {
            return this;
        }
        if (!this.gia.getCurrency().equals(other.gia.getCurrency())) {
            throw new IllegalArgumentException("Không thể cập nhật giá vốn khác tiền tệ");
        }
        // Lấy giá cao nhất khi cập nhật (phù hợp với trung bình gia quyền)
        if (other.gia.getAmount().compareTo(this.gia.getAmount()) > 0) {
            return new GiaVon(other.gia, this.phuongPhap);
        }
        return this;
    }

    /**
     * Nhân giá vốn với hệ số (để tính thành tiền)
     */
    public Tien timesQuantity(SoLuong soLuong) {
        if (soLuong == null) {
            throw new IllegalArgumentException("Số lượng không được null");
        }
        return this.gia.multiply(soLuong.getValue());
    }

    /**
     * Chia giá vốn cho số lượng (tính đơn giá)
     */
    public GiaVon divideByQuantity(SoLuong soLuong) {
        if (soLuong == null || soLuong.isZero()) {
            throw new IllegalArgumentException("Số lượng không được null hoặc 0");
        }
        Tien newGia = this.gia.divide(soLuong.getValue());
        return new GiaVon(newGia, this.phuongPhap);
    }

    /**
     * Kiểm tra giá vốn có bằng 0 không
     */
    public boolean isZero() {
        return this.gia.isZero();
    }

    /**
     * Kiểm tra phương pháp FIFO
     */
    public boolean isFIFO() {
        return FIFO.equals(this.phuongPhap);
    }

    /**
     * Kiểm tra phương pháp LIFO
     */
    public boolean isLIFO() {
        return LIFO.equals(this.phuongPhap);
    }

    /**
     * Kiểm tra phương pháp trung bình
     */
    public boolean isTrungBinh() {
        return TRUNG_BINH.equals(this.phuongPhap);
    }

    // ============ Getters ============
    public Tien getGia() {
        return gia;
    }

    public String getPhuongPhap() {
        return phuongPhap;
    }

    // ============ Comparable ============
    @Override
    public int compareTo(GiaVon other) {
        return this.gia.compareTo(other.gia);
    }

    // ============ equals & hashCode ============
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        GiaVon giaVon = (GiaVon) o;
        return Objects.equals(gia, giaVon.gia) && 
               Objects.equals(phuongPhap, giaVon.phuongPhap);
    }

    @Override
    public int hashCode() {
        return Objects.hash(gia, phuongPhap);
    }

    @Override
    public String toString() {
        return String.format("%s (%s)", gia, phuongPhap);
    }
}
