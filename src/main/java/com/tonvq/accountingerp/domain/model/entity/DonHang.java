package com.tonvq.accountingerp.domain.model.entity;

import com.tonvq.accountingerp.domain.model.valueobject.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Entity: Đơn Hàng (Sales Order)
 * 
 * Đại diện cho đơn hàng bán (TMĐT + onsite).
 * Theo dõi toàn bộ vòng đời: từ tạo đơn → xác nhận → giao hàng → thanh toán.
 * 
 * @author Ton VQ
 */
public class DonHang implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;                              // UUID
    private String maDonHang;                       // Mã đơn hàng (VD: DH001, DH002)
    private String maKhachHang;                     // Tham chiếu khách hàng
    
    private LocalDateTime ngayTao;                  // Ngày tạo đơn
    private LocalDateTime ngayXacNhan;              // Ngày xác nhận
    private LocalDateTime ngayDuKienGiao;           // Ngày dự kiến giao
    private LocalDateTime ngayGiaoThuc;             // Ngày giao thực tế
    private LocalDateTime ngayThanhToan;            // Ngày thanh toán

    private TrangThaiDonHang trangThai;             // DRAFT, CONFIRMED, SHIPPING, DELIVERED, PAID, CANCELLED

    // ============ Order Items ============
    private List<DonHangChiTiet> chiTietList = new ArrayList<>();

    // ============ Pricing ============
    private Tien tongGiaHang;                       // Tổng giá hàng (chưa VAT)
    private double tienVAT = 0;                     // VAT (%)
    private Tien tongTienVAT;                       // Tổng tiền VAT
    private Tien tongCong;                          // Tổng cộng (gồm VAT)
    private Tien tienDaThanhToan;                   // Tiền đã thanh toán
    private Tien tienConNo;                         // Tiền còn nợ

    // ============ Shipping & Delivery ============
    private String diaChiGiao;                      // Địa chỉ giao
    private String tinhTrangGiao;                   // Tình trạng giao (Chưa giao, Đang giao, Đã giao)
    private String soNhieuuChuyenHang;              // Số nhiều chuyên hàng (nếu chia nhiều lần)

    // ============ Payment & Terms ============
    private String hinhThucThanhToan;               // Hình thức (Tiền mặt, Chuyển khoản, etc.)
    private int kyHanThanhToan;                     // Kỳ hạn thanh toán (ngày)
    private String ghuChiChoiTien;                  // Ghi chú chính sách thanh toán

    // ============ Audit ============
    private String createdBy;                       // Người tạo
    private LocalDateTime createdAt;                // Thời gian tạo
    private String lastModifiedBy;                  // Người sửa cuối
    private LocalDateTime lastModifiedAt;           // Thời gian sửa cuối

    private String moTa;                            // Mô tả đơn hàng
    private boolean isCompleted = false;            // Đã hoàn thành?

    // ============ Constructors ============
    public DonHang() {
    }

    public DonHang(String maDonHang, String maKhachHang, String diaChiGiao) {
        validateDonHang(maDonHang, maKhachHang, diaChiGiao);
        this.id = UUID.randomUUID().toString();
        this.maDonHang = maDonHang;
        this.maKhachHang = maKhachHang;
        this.diaChiGiao = diaChiGiao;
        this.ngayTao = LocalDateTime.now();
        this.trangThai = TrangThaiDonHang.DRAFT;
        this.tongGiaHang = Tien.zeroVND();
        this.tongTienVAT = Tien.zeroVND();
        this.tongCong = Tien.zeroVND();
        this.tienDaThanhToan = Tien.zeroVND();
        this.tienConNo = Tien.zeroVND();
        this.createdAt = LocalDateTime.now();
    }

    private static void validateDonHang(String maDonHang, String maKhachHang, String diaChiGiao) {
        if (maDonHang == null || maDonHang.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã đơn hàng không được rỗng");
        }
        if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khách hàng không được rỗng");
        }
        if (diaChiGiao == null || diaChiGiao.trim().isEmpty()) {
            throw new IllegalArgumentException("Địa chỉ giao không được rỗng");
        }
    }

    // ============ Business Methods ============

    /**
     * Thêm chi tiết đơn hàng
     */
    public void addChiTiet(DonHangChiTiet chiTiet) {
        if (chiTiet == null) {
            throw new IllegalArgumentException("Chi tiết đơn hàng không được null");
        }
        if (!trangThai.canEdit()) {
            throw new IllegalStateException(
                String.format("Không thể thêm chi tiết ở trạng thái %s", trangThai.getLabel())
            );
        }
        this.chiTietList.add(chiTiet);
        calculateTotals();
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Xóa chi tiết đơn hàng
     */
    public void removeChiTiet(DonHangChiTiet chiTiet) {
        if (!trangThai.canEdit()) {
            throw new IllegalStateException(
                String.format("Không thể xóa chi tiết ở trạng thái %s", trangThai.getLabel())
            );
        }
        this.chiTietList.remove(chiTiet);
        calculateTotals();
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Xác nhận đơn hàng
     */
    public void xacNhan(String xacNhanBy) {
        if (this.trangThai != TrangThaiDonHang.DRAFT) {
            throw new IllegalStateException(
                String.format("Chỉ có thể xác nhận đơn hàng ở trạng thái DRAFT, hiện tại: %s", 
                    this.trangThai.getLabel())
            );
        }
        this.trangThai = TrangThaiDonHang.CONFIRMED;
        this.ngayXacNhan = LocalDateTime.now();
        this.lastModifiedBy = xacNhanBy;
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Ghi chú giao hàng
     */
    public void batchGiao(String giaoBatch) {
        if (!this.trangThai.canShip()) {
            throw new IllegalStateException(
                String.format("Không thể giao đơn hàng ở trạng thái %s", this.trangThai.getLabel())
            );
        }
        if (this.trangThai == TrangThaiDonHang.CONFIRMED) {
            this.trangThai = TrangThaiDonHang.SHIPPING;
        }
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Đánh dấu đã giao
     */
    public void thucHienGiao(String giaoBatch) {
        if (this.trangThai != TrangThaiDonHang.SHIPPING) {
            throw new IllegalStateException(
                String.format("Chỉ có thể đánh dấu giao khi đang giao (SHIPPING), hiện tại: %s", 
                    this.trangThai.getLabel())
            );
        }
        this.trangThai = TrangThaiDonHang.DELIVERED;
        this.ngayGiaoThuc = LocalDateTime.now();
        this.lastModifiedBy = giaoBatch;
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Ghi nhận thanh toán
     */
    public void ghiNhanThanhToan(Tien soTienThanhToan, String ghiBatch) {
        if (soTienThanhToan == null || soTienThanhToan.isNegative()) {
            throw new IllegalArgumentException("Số tiền thanh toán phải là số dương");
        }
        if (this.trangThai == TrangThaiDonHang.DRAFT || this.trangThai == TrangThaiDonHang.CANCELLED) {
            throw new IllegalStateException(
                String.format("Không thể thanh toán đơn hàng ở trạng thái %s", this.trangThai.getLabel())
            );
        }
        
        this.tienDaThanhToan = this.tienDaThanhToan.add(soTienThanhToan);
        this.tienConNo = this.tienCong.subtract(this.tienDaThanhToan);
        
        if (this.tienConNo.isZero()) {
            this.trangThai = TrangThaiDonHang.PAID;
            this.ngayThanhToan = LocalDateTime.now();
            this.isCompleted = true;
        }
        
        this.lastModifiedBy = ghiBatch;
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Hủy đơn hàng
     */
    public void huy(String huyBy) {
        if (!this.trangThai.canCancel()) {
            throw new IllegalStateException(
                String.format("Không thể hủy đơn hàng ở trạng thái %s", this.trangThai.getLabel())
            );
        }
        this.trangThai = TrangThaiDonHang.CANCELLED;
        this.lastModifiedBy = huyBy;
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Tính lại tổng tiền
     */
    private void calculateTotals() {
        this.tongGiaHang = Tien.zeroVND();
        for (DonHangChiTiet chiTiet : chiTietList) {
            this.tongGiaHang = this.tongGiaHang.add(chiTiet.getTongTien());
        }
        
        this.tongTienVAT = this.tongGiaHang.multiply(this.tienVAT / 100.0);
        this.tongCong = this.tongGiaHang.add(this.tongTienVAT);
        
        // Tính lại tiền còn nợ
        this.tienConNo = this.tongCong.subtract(this.tienDaThanhToan);
    }

    /**
     * Cập nhật VAT
     */
    public void updateVAT(double tiemVAT) {
        if (tiemVAT < 0 || tiemVAT > 100) {
            throw new IllegalArgumentException("VAT phải nằm trong khoảng 0-100%");
        }
        this.tienVAT = tiemVAT;
        calculateTotals();
    }

    /**
     * Lấy danh sách chi tiết
     */
    public List<DonHangChiTiet> getChiTietList() {
        return Collections.unmodifiableList(chiTietList);
    }

    /**
     * Kiểm tra đơn hàng đã hoàn thành
     */
    public boolean isCompleted() {
        return this.trangThai == TrangThaiDonHang.PAID;
    }

    public boolean isCancelled() {
        return this.trangThai == TrangThaiDonHang.CANCELLED;
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

    public String getMaKhachHang() {
        return maKhachHang;
    }

    public void setMaKhachHang(String maKhachHang) {
        this.maKhachHang = maKhachHang;
    }

    public LocalDateTime getNgayTao() {
        return ngayTao;
    }

    public void setNgayTao(LocalDateTime ngayTao) {
        this.ngayTao = ngayTao;
    }

    public LocalDateTime getNgayXacNhan() {
        return ngayXacNhan;
    }

    public void setNgayXacNhan(LocalDateTime ngayXacNhan) {
        this.ngayXacNhan = ngayXacNhan;
    }

    public LocalDateTime getNgayDuKienGiao() {
        return ngayDuKienGiao;
    }

    public void setNgayDuKienGiao(LocalDateTime ngayDuKienGiao) {
        this.ngayDuKienGiao = ngayDuKienGiao;
    }

    public LocalDateTime getNgayGiaoThuc() {
        return ngayGiaoThuc;
    }

    public void setNgayGiaoThuc(LocalDateTime ngayGiaoThuc) {
        this.ngayGiaoThuc = ngayGiaoThuc;
    }

    public LocalDateTime getNgayThanhToan() {
        return ngayThanhToan;
    }

    public void setNgayThanhToan(LocalDateTime ngayThanhToan) {
        this.ngayThanhToan = ngayThanhToan;
    }

    public TrangThaiDonHang getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiDonHang trangThai) {
        this.trangThai = trangThai;
    }

    public Tien getTongGiaHang() {
        return tongGiaHang;
    }

    public void setTongGiaHang(Tien tongGiaHang) {
        this.tongGiaHang = tongGiaHang;
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

    public String getDiaChiGiao() {
        return diaChiGiao;
    }

    public void setDiaChiGiao(String diaChiGiao) {
        this.diaChiGiao = diaChiGiao;
    }

    public String getTinhTrangGiao() {
        return tinhTrangGiao;
    }

    public void setTinhTrangGiao(String tinhTrangGiao) {
        this.tinhTrangGiao = tinhTrangGiao;
    }

    public String getSoNhieuuChuyenHang() {
        return soNhieuuChuyenHang;
    }

    public void setSoNhieuuChuyenHang(String soNhieuuChuyenHang) {
        this.soNhieuuChuyenHang = soNhieuuChuyenHang;
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

    public String getGhuChiChoiTien() {
        return ghuChiChoiTien;
    }

    public void setGhuChiChoiTien(String ghuChiChoiTien) {
        this.ghuChiChoiTien = ghuChiChoiTien;
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
        DonHang donHang = (DonHang) o;
        return Objects.equals(id, donHang.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("DonHang{id='%s', maDonHang='%s', trangThai=%s, tongCong=%s}",
                id, maDonHang, trangThai, tongCong);
    }
}
