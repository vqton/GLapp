package com.tonvq.accountingerp.domain.model.entity;

import com.tonvq.accountingerp.domain.model.valueobject.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity: Hóa Đơn (Invoice)
 * 
 * Đại diện cho hóa đơn bán hàng/cung cấp dịch vụ.
 * Tham chiếu từ Đơn Hàng hoặc được tạo độc lập.
 * 
 * Theo TT 99/2025/TT-BTC: Phải có đầy đủ thông tin hoá đơn, tên, địa chỉ, mã số thuế.
 * 
 * @author Ton VQ
 */
public class HoaDon implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;                              // UUID
    private String maHoaDon;                        // Mã hóa đơn
    private String soHoaDon;                        // Số hóa đơn từ hóa đơn gốc
    
    private String maKhachHang;                     // Mã khách hàng
    private String tenKhachHang;                    // Tên khách hàng
    private String diaChi;                          // Địa chỉ khách hàng
    private String maSoThue;                        // Mã số thuế khách hàng
    
    private LocalDateTime ngayHoaDon;               // Ngày hóa đơn
    private LocalDateTime ngayGhiSo;                // Ngày ghi sổ
    private String kyHoaDon;                        // Kỳ hóa đơn (VD: 01/2024)
    
    // ============ Pricing Info ============
    private Tien tongGiaHang;                       // Tổng giá hàng (chưa VAT)
    private Tien tongVAT;                           // Tổng VAT
    private Tien tongCong;                          // Tổng cộng
    private Tien tienDaThanhToan;                   // Tiền đã thanh toán
    private Tien tienConNo;                         // Tiền còn nợ
    
    // ============ Status ============
    private String trangThaiHoaDon;                 // Nháp, Đã phát hành, Hủy, Cấp lại
    private boolean isPhongDaHang = false;          // Có là phiếu chứng thực không?
    private String refMaDonHang;                    // Tham chiếu đơn hàng (nếu có)
    
    // ============ Payment ============
    private String hinhThucThanhToan;               // Hình thức thanh toán
    private int kyHanThanhToan;                     // Kỳ hạn thanh toán (ngày)
    
    // ============ Audit ============
    private String createdBy;                       // Người tạo
    private LocalDateTime createdAt;                // Thời gian tạo
    private String lastModifiedBy;                  // Người sửa cuối
    private LocalDateTime lastModifiedAt;           // Thời gian sửa cuối
    
    private String moTa;                            // Ghi chú

    // ============ Constructors ============
    public HoaDon() {
    }

    public HoaDon(String maHoaDon, String maKhachHang, String tenKhachHang, String diaChi) {
        validateHoaDon(maHoaDon, maKhachHang, tenKhachHang, diaChi);
        this.id = UUID.randomUUID().toString();
        this.maHoaDon = maHoaDon;
        this.maKhachHang = maKhachHang;
        this.tenKhachHang = tenKhachHang;
        this.diaChi = diaChi;
        this.ngayHoaDon = LocalDateTime.now();
        this.trangThaiHoaDon = "DRAFT";
        this.createdAt = LocalDateTime.now();
        this.tongGiaHang = Tien.zeroVND();
        this.tongVAT = Tien.zeroVND();
        this.tongCong = Tien.zeroVND();
        this.tienDaThanhToan = Tien.zeroVND();
        this.tienConNo = Tien.zeroVND();
    }

    private static void validateHoaDon(String maHoaDon, String maKhachHang, 
                                       String tenKhachHang, String diaChi) {
        if (maHoaDon == null || maHoaDon.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã hóa đơn không được rỗng");
        }
        if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khách hàng không được rỗng");
        }
        if (tenKhachHang == null || tenKhachHang.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên khách hàng không được rỗng");
        }
        if (diaChi == null || diaChi.trim().isEmpty()) {
            throw new IllegalArgumentException("Địa chỉ khách hàng không được rỗng");
        }
    }

    // ============ Business Methods ============

    /**
     * Phát hành hóa đơn
     */
    public void phatHanh(String phatHanhBy) {
        if (!this.trangThaiHoaDon.equals("DRAFT")) {
            throw new IllegalStateException(
                String.format("Chỉ có thể phát hành hóa đơn ở trạng thái DRAFT, hiện tại: %s", 
                    this.trangThaiHoaDon)
            );
        }
        this.trangThaiHoaDon = "ISSUED";
        this.ngayGhiSo = LocalDateTime.now();
        this.lastModifiedBy = phatHanhBy;
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Ghi nhận thanh toán
     */
    public void ghiNhanThanhToan(Tien soTienThanhToan, String ghiBatch) {
        if (soTienThanhToan == null || soTienThanhToan.isNegative()) {
            throw new IllegalArgumentException("Số tiền thanh toán phải là số dương");
        }
        this.tienDaThanhToan = this.tienDaThanhToan.add(soTienThanhToan);
        this.tienConNo = this.tongCong.subtract(this.tienDaThanhToan);
        this.lastModifiedBy = ghiBatch;
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Hủy hóa đơn
     */
    public void huy(String huyBy) {
        this.trangThaiHoaDon = "CANCELLED";
        this.lastModifiedBy = huyBy;
        this.lastModifiedAt = LocalDateTime.now();
    }

    // ============ Getters & Setters ============
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMaHoaDon() {
        return maHoaDon;
    }

    public void setMaHoaDon(String maHoaDon) {
        this.maHoaDon = maHoaDon;
    }

    public String getSoHoaDon() {
        return soHoaDon;
    }

    public void setSoHoaDon(String soHoaDon) {
        this.soHoaDon = soHoaDon;
    }

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public String getTenKhachHang() {
        return tenKhachHang;
    }

    public void setTenKhachHang(String tenKhachHang) {
        this.tenKhachHang = tenKhachHang;
    }

    public String getDiaChi() {
        return diaChi;
    }

    public void setDiaChi(String diaChi) {
        this.diaChi = diaChi;
    }

    public String getMaSoThue() {
        return maSoThue;
    }

    public void setMaSoThue(String maSoThue) {
        this.maSoThue = maSoThue;
    }

    public LocalDateTime getNgayHoaDon() {
        return ngayHoaDon;
    }

    public void setNgayHoaDon(LocalDateTime ngayHoaDon) {
        this.ngayHoaDon = ngayHoaDon;
    }

    public LocalDateTime getNgayGhiSo() {
        return ngayGhiSo;
    }

    public void setNgayGhiSo(LocalDateTime ngayGhiSo) {
        this.ngayGhiSo = ngayGhiSo;
    }

    public String getKyHoaDon() {
        return kyHoaDon;
    }

    public void setKyHoaDon(String kyHoaDon) {
        this.kyHoaDon = kyHoaDon;
    }

    public Tien getTongGiaHang() {
        return tongGiaHang;
    }

    public void setTongGiaHang(Tien tongGiaHang) {
        this.tongGiaHang = tongGiaHang;
    }

    public Tien getTongVAT() {
        return tongVAT;
    }

    public void setTongVAT(Tien tongVAT) {
        this.tongVAT = tongVAT;
    }

    public Tien getTongCong() {
        return tongCong;
    }

    public void setTongCong(Tien tongCong) {
        this.tongCong = tongCong;
    }

    public Tien getTienDaThanhToan() {
        return tienDaThanhToan;
    }

    public void setTienDaThanhToan(Tien tienDaThanhToan) {
        this.tienDaThanhToan = tienDaThanhToan;
    }

    public Tien getTienConNo() {
        return tienConNo;
    }

    public void setTienConNo(Tien tienConNo) {
        this.tienConNo = tienConNo;
    }

    public String getTrangThaiHoaDon() {
        return trangThaiHoaDon;
    }

    public void setTrangThaiHoaDon(String trangThaiHoaDon) {
        this.trangThaiHoaDon = trangThaiHoaDon;
    }

    public boolean isPhongDaHang() {
        return isPhongDaHang;
    }

    public void setPhongDaHang(boolean phongDaHang) {
        isPhongDaHang = phongDaHang;
    }

    public String getRefMaDonHang() {
        return refMaDonHang;
    }

    public void setRefMaDonHang(String refMaDonHang) {
        this.refMaDonHang = refMaDonHang;
    }

    public String getHinhThucThanhToan() {
        return hinhThucThanhToan;
    }

    public void setHinhThucThanhToan(String hinhThucThanhToan) {
        this.hinhThucThanhToan = hinhThucThanhToan;
    }

    public int getKyHanThanhToan() {
        return kyHanThanhToan;
    }

    public void setKyHanThanhToan(int kyHanThanhToan) {
        this.kyHanThanhToan = kyHanThanhToan;
    }

    public String getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getLastModifiedBy() {
        return lastModifiedBy;
    }

    public void setLastModifiedBy(String lastModifiedBy) {
        this.lastModifiedBy = lastModifiedBy;
    }

    public LocalDateTime getLastModifiedAt() {
        return lastModifiedAt;
    }

    public void setLastModifiedAt(LocalDateTime lastModifiedAt) {
        this.lastModifiedAt = lastModifiedAt;
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
        HoaDon hoaDon = (HoaDon) o;
        return Objects.equals(id, hoaDon.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("HoaDon{id='%s', maHoaDon='%s', tenKhachHang='%s', tongCong=%s}",
                id, maHoaDon, tenKhachHang, tongCong);
    }
}
