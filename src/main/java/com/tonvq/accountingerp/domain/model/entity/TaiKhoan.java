package com.tonvq.accountingerp.domain.model.entity;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity: Tài Khoản (Chart of Accounts)
 * 
 * Đại diện cho một tài khoản kế toán trong bảng tài khoản.
 * Theo TT 99/2025/TT-BTC: Có 71 tài khoản cấp 1.
 * 
 * Ví dụ:
 * - 1010: Tiền mặt
 * - 1020: Tiền gửi ngân hàng
 * - 1040: Tài khoản hoạt động
 * - 1100: Phải thu của khách hàng
 * - 2100: Phải trả nhà cung cấp
 * - 4011: Doanh thu bán hàng
 * - 6011: Giá vốn hàng bán
 * 
 * @author Ton VQ
 */
public class TaiKhoan implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;                              // UUID
    private String maTaiKhoan;                      // Mã tài khoản (VD: 1010, 2100)
    private String tenTaiKhoan;                     // Tên tài khoản
    private String tenTienAnh;                      // Tên tiếng Anh (Account Name)
    
    private String loaiTaiKhoan;                    // Loại: T.Sản, T.Nợ, T.Có, v.v.
    private String duKienTK;                        // Dự kiến TK (nợ hay có?)
    private String taiKhoanCha;                     // Tài khoản cha (tài khoản cấp cao hơn)
    
    private double soTienNo = 0;                    // Số dư nợ
    private double soTienCo = 0;                    // Số dư có
    
    private int capTaiKhoan = 1;                    // Cấp tài khoản (1, 2, 3, v.v.)
    private String moTa;                            // Mô tả
    
    private boolean isActive = true;                // Còn sử dụng?
    private boolean isDeletable = false;            // Có thể xóa không?
    
    private String createdBy;                       // Người tạo
    private LocalDateTime createdAt;                // Thời gian tạo
    private String lastModifiedBy;                  // Người sửa cuối
    private LocalDateTime lastModifiedAt;           // Thời gian sửa cuối

    // ============ Constructors ============
    public TaiKhoan() {
    }

    public TaiKhoan(String maTaiKhoan, String tenTaiKhoan, String loaiTaiKhoan, String duKienTK) {
        validateTaiKhoan(maTaiKhoan, tenTaiKhoan, loaiTaiKhoan, duKienTK);
        this.id = UUID.randomUUID().toString();
        this.maTaiKhoan = maTaiKhoan;
        this.tenTaiKhoan = tenTaiKhoan;
        this.loaiTaiKhoan = loaiTaiKhoan;
        this.duKienTK = duKienTK;
        this.createdAt = LocalDateTime.now();
    }

    private static void validateTaiKhoan(String maTaiKhoan, String tenTaiKhoan, 
                                        String loaiTaiKhoan, String duKienTK) {
        if (maTaiKhoan == null || maTaiKhoan.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã tài khoản không được rỗng");
        }
        if (tenTaiKhoan == null || tenTaiKhoan.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên tài khoản không được rỗng");
        }
        if (loaiTaiKhoan == null || loaiTaiKhoan.trim().isEmpty()) {
            throw new IllegalArgumentException("Loại tài khoản không được rỗng");
        }
        if (duKienTK == null || duKienTK.trim().isEmpty()) {
            throw new IllegalArgumentException("Dự kiến tài khoản không được rỗng");
        }
    }

    // ============ Business Methods ============

    /**
     * Cộng số dư nợ
     */
    public void congNo(double soTien) {
        if (soTien < 0) {
            throw new IllegalArgumentException("Số tiền không được âm");
        }
        this.soTienNo += soTien;
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Cộng số dư có
     */
    public void congCo(double soTien) {
        if (soTien < 0) {
            throw new IllegalArgumentException("Số tiền không được âm");
        }
        this.soTienCo += soTien;
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Trừ số dư nợ
     */
    public void truNo(double soTien) {
        if (soTien < 0) {
            throw new IllegalArgumentException("Số tiền không được âm");
        }
        this.soTienNo = Math.max(0, this.soTienNo - soTien);
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Trừ số dư có
     */
    public void truCo(double soTien) {
        if (soTien < 0) {
            throw new IllegalArgumentException("Số tiền không được âm");
        }
        this.soTienCo = Math.max(0, this.soTienCo - soTien);
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Lấy số dư ròng (khác nhau giữa nợ và có)
     */
    public double getSoDuRong() {
        return this.soTienNo - this.soTienCo;
    }

    /**
     * Kiểm tra tài khoản là tài khoản bộ trưởng (cấp 1)
     */
    public boolean isCapMotMuc() {
        return this.capTaiKhoan == 1;
    }

    // ============ Getters & Setters ============
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMaTaiKhoan() {
        return maTaiKhoan;
    }

    public void setMaTaiKhoan(String maTaiKhoan) {
        this.maTaiKhoan = maTaiKhoan;
    }

    public String getTenTaiKhoan() {
        return tenTaiKhoan;
    }

    public void setTenTaiKhoan(String tenTaiKhoan) {
        this.tenTaiKhoan = tenTaiKhoan;
    }

    public String getTenTienAnh() {
        return tenTienAnh;
    }

    public void setTenTienAnh(String tenTienAnh) {
        this.tenTienAnh = tenTienAnh;
    }

    public String getLoaiTaiKhoan() {
        return loaiTaiKhoan;
    }

    public void setLoaiTaiKhoan(String loaiTaiKhoan) {
        this.loaiTaiKhoan = loaiTaiKhoan;
    }

    public String getDuKienTK() {
        return duKienTK;
    }

    public void setDuKienTK(String duKienTK) {
        this.duKienTK = duKienTK;
    }

    public String getTaiKhoanCha() {
        return taiKhoanCha;
    }

    public void setTaiKhoanCha(String taiKhoanCha) {
        this.taiKhoanCha = taiKhoanCha;
    }

    public double getSoTienNo() {
        return soTienNo;
    }

    public void setSoTienNo(double soTienNo) {
        this.soTienNo = soTienNo;
    }

    public double getSoTienCo() {
        return soTienCo;
    }

    public void setSoTienCo(double soTienCo) {
        this.soTienCo = soTienCo;
    }

    public int getCapTaiKhoan() {
        return capTaiKhoan;
    }

    public void setCapTaiKhoan(int capTaiKhoan) {
        this.capTaiKhoan = capTaiKhoan;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public boolean isDeletable() {
        return isDeletable;
    }

    public void setDeletable(boolean deletable) {
        isDeletable = deletable;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TaiKhoan taiKhoan = (TaiKhoan) o;
        return Objects.equals(id, taiKhoan.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("TaiKhoan{id='%s', maTaiKhoan='%s', tenTaiKhoan='%s', soTienNo=%s, soTienCo=%s}",
                id, maTaiKhoan, tenTaiKhoan, soTienNo, soTienCo);
    }
}
