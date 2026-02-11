package com.tonvq.accountingerp.domain.model.entity;

import com.tonvq.accountingerp.domain.model.valueobject.*;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity: Chi Tiết Đơn Hàng (Order Line Item)
 * 
 * Đại diện cho một dòng chi tiết trong đơn hàng.
 * Mỗi dòng chứa thông tin: sản phẩm, số lượng, giá, VAT.
 * 
 * @author Ton VQ
 */
public class DonHangChiTiet implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;                              // UUID
    private String maDonHang;                       // Tham chiếu đơn hàng
    private String maSanPham;                       // Mã sản phẩm
    private String tenSanPham;                      // Tên sản phẩm
    
    private SoLuong soLuong;                        // Số lượng
    private Tien giaBan;                            // Giá bán (đơn vị)
    private Tien tongTien;                          // Tổng tiền (số lượng × giá bán)
    
    private double tienVAT = 0;                     // VAT (%) cho dòng này (tuỳ chọn)
    private Tien tongTienVAT;                       // Tổng VAT cho dòng
    
    private String moTa;                            // Mô tả sản phẩm

    // ============ Constructors ============
    public DonHangChiTiet() {
    }

    public DonHangChiTiet(String maSanPham, String tenSanPham, SoLuong soLuong, Tien giaBan) {
        validateDonHangChiTiet(maSanPham, soLuong, giaBan);
        this.id = UUID.randomUUID().toString();
        this.maSanPham = maSanPham;
        this.tenSanPham = tenSanPham;
        this.soLuong = soLuong;
        this.giaBan = giaBan;
        this.calculateTongTien();
    }

    private static void validateDonHangChiTiet(String maSanPham, SoLuong soLuong, Tien giaBan) {
        if (maSanPham == null || maSanPham.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã sản phẩm không được rỗng");
        }
        if (soLuong == null || !soLuong.isPositive()) {
            throw new IllegalArgumentException("Số lượng phải > 0");
        }
        if (giaBan == null || giaBan.isNegative()) {
            throw new IllegalArgumentException("Giá bán không được âm");
        }
    }

    // ============ Business Methods ============

    /**
     * Tính lại tổng tiền
     */
    private void calculateTongTien() {
        this.tongTien = this.giaBan.multiply(this.soLuong.getValue());
        if (this.tienVAT > 0) {
            this.tongTienVAT = this.tongTien.multiply(this.tienVAT / 100.0);
        } else {
            this.tongTienVAT = Tien.zeroVND();
        }
    }

    /**
     * Cập nhật số lượng
     */
    public void updateSoLuong(SoLuong soLuongMoi) {
        if (soLuongMoi == null || !soLuongMoi.isPositive()) {
            throw new IllegalArgumentException("Số lượng phải > 0");
        }
        this.soLuong = soLuongMoi;
        calculateTongTien();
    }

    /**
     * Cập nhật giá bán
     */
    public void updateGiaBan(Tien giaBanMoi) {
        if (giaBanMoi == null || giaBanMoi.isNegative()) {
            throw new IllegalArgumentException("Giá bán không được âm");
        }
        this.giaBan = giaBanMoi;
        calculateTongTien();
    }

    /**
     * Cập nhật VAT
     */
    public void updateVAT(double tiemVAT) {
        if (tiemVAT < 0 || tiemVAT > 100) {
            throw new IllegalArgumentException("VAT phải nằm trong khoảng 0-100%");
        }
        this.tienVAT = tiemVAT;
        calculateTongTien();
    }

    /**
     * Lấy tổng tiền cộng VAT
     */
    public Tien getTongTienCongVAT() {
        return this.tongTien.add(this.tongTienVAT);
    }

    // ============ Getters & Setters ============
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMaDonHang() {
        return maDonHang;
    }

    public void setMaDonHang(String maDonHang) {
        this.maDonHang = maDonHang;
    }

    public String getMaSanPham() {
        return maSanPham;
    }

    public void setMaSanPham(String maSanPham) {
        this.maSanPham = maSanPham;
    }

    public String getTenSanPham() {
        return tenSanPham;
    }

    public void setTenSanPham(String tenSanPham) {
        this.tenSanPham = tenSanPham;
    }

    public SoLuong getSoLuong() {
        return soLuong;
    }

    public void setSoLuong(SoLuong soLuong) {
        this.soLuong = soLuong;
    }

    public Tien getGiaBan() {
        return giaBan;
    }

    public void setGiaBan(Tien giaBan) {
        this.giaBan = giaBan;
    }

    public Tien getTongTien() {
        return tongTien;
    }

    public void setTongTien(Tien tongTien) {
        this.tongTien = tongTien;
    }

    public double getTienVAT() {
        return tienVAT;
    }

    public void setTienVAT(double tienVAT) {
        this.tienVAT = tienVAT;
    }

    public Tien getTongTienVAT() {
        return tongTienVAT;
    }

    public void setTongTienVAT(Tien tongTienVAT) {
        this.tongTienVAT = tongTienVAT;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DonHangChiTiet that = (DonHangChiTiet) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("DonHangChiTiet{id='%s', maSanPham='%s', soLuong=%s, giaBan=%s, tongTien=%s}",
                id, maSanPham, soLuong, giaBan, tongTien);
    }
}
