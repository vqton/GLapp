package com.tonvq.accountingerp.domain.model.aggregate;

import com.tonvq.accountingerp.domain.model.entity.DonHang;
import com.tonvq.accountingerp.domain.model.entity.DonHangChiTiet;
import com.tonvq.accountingerp.domain.model.valueobject.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Aggregate Root: Đơn Hàng Aggregate
 * 
 * Quản lý toàn bộ đơn hàng + chi tiết.
 * Bảo vệ các invariant:
 * - Đơn hàng phải có ít nhất 1 chi tiết
 * - Không được sửa sau khi xác nhận
 * - Tổng tiền = Σ(số lượng × giá)
 * 
 * @author Ton VQ
 */
public class DonHangAggregate implements Serializable {
    private static final long serialVersionUID = 1L;

    private DonHang donHang;                        // Entity root
    private List<DonHangChiTiet> chiTietList = new ArrayList<>();

    // ============ Constructor ============
    public DonHangAggregate(DonHang donHang) {
        if (donHang == null) {
            throw new IllegalArgumentException("Đơn hàng không được null");
        }
        this.donHang = donHang;
    }

    // ============ Invariant Methods ============

    /**
     * Thêm chi tiết đơn hàng
     */
    public void addChiTiet(DonHangChiTiet chiTiet) {
        if (chiTiet == null) {
            throw new IllegalArgumentException("Chi tiết đơn hàng không được null");
        }
        if (!this.donHang.getTrangThai().canEdit()) {
            throw new IllegalStateException(
                String.format("Không thể thêm chi tiết ở trạng thái %s", this.donHang.getTrangThai().getLabel())
            );
        }
        this.chiTietList.add(chiTiet);
        this.donHang.addChiTiet(chiTiet);
    }

    /**
     * Xóa chi tiết
     */
    public void removeChiTiet(String chiTietId) {
        DonHangChiTiet toRemove = this.chiTietList.stream()
            .filter(ct -> ct.getId().equals(chiTietId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Chi tiết không tìm thấy"));
        
        this.chiTietList.remove(toRemove);
        this.donHang.removeChiTiet(toRemove);
    }

    /**
     * Xác nhận đơn hàng
     */
    public void xacNhan(String xacNhanBy) {
        if (this.chiTietList.isEmpty()) {
            throw new IllegalStateException("Đơn hàng phải có ít nhất 1 chi tiết");
        }
        this.donHang.xacNhan(xacNhanBy);
    }

    /**
     * Giao đơn hàng
     */
    public void giao(String giaoBatch) {
        if (!this.donHang.getTrangThai().canShip()) {
            throw new IllegalStateException(
                String.format("Không thể giao ở trạng thái %s", this.donHang.getTrangThai().getLabel())
            );
        }
        this.donHang.batchGiao(giaoBatch);
    }

    /**
     * Đánh dấu đã giao
     */
    public void thucHienGiao(String giaoBatch) {
        if (this.donHang.getTrangThai() != TrangThaiDonHang.SHIPPING) {
            throw new IllegalStateException("Đơn hàng phải ở trạng thái SHIPPING");
        }
        this.donHang.thucHienGiao(giaoBatch);
    }

    /**
     * Ghi nhận thanh toán
     */
    public void ghiNhanThanhToan(Tien soTienThanhToan, String ghiBatch) {
        this.donHang.ghiNhanThanhToan(soTienThanhToan, ghiBatch);
    }

    /**
     * Hủy đơn hàng
     */
    public void huy(String huyBy) {
        this.donHang.huy(huyBy);
    }

    /**
     * Lấy danh sách chi tiết
     */
    public List<DonHangChiTiet> getChiTietList() {
        return Collections.unmodifiableList(this.chiTietList);
    }

    /**
     * Kiểm tra có thể chỉnh sửa
     */
    public boolean canEdit() {
        return this.donHang.getTrangThai() == TrangThaiDonHang.DRAFT;
    }

    /**
     * Kiểm tra có thể xác nhận
     */
    public boolean canConfirm() {
        return this.donHang.getTrangThai() == TrangThaiDonHang.DRAFT && !this.chiTietList.isEmpty();
    }

    /**
     * Kiểm tra có thể giao
     */
    public boolean canShip() {
        return this.donHang.getTrangThai().canShip();
    }

    /**
     * Lấy tổng tiền
     */
    public Tien getTongCong() {
        return this.donHang.getTongCong();
    }

    /**
     * Lấy tiền còn nợ
     */
    public Tien getTienConNo() {
        return this.donHang.getTienConNo();
    }

    // ============ Getters ============
    public DonHang getDonHang() {
        return donHang;
    }

    public String getId() {
        return donHang.getId();
    }

    public String getMaDonHang() {
        return donHang.getMaDonHang();
    }

    public String getMaKhachHang() {
        return donHang.getMaKhachHang();
    }

    public TrangThaiDonHang getTrangThai() {
        return donHang.getTrangThai();
    }

    public int getChiTietCount() {
        return this.chiTietList.size();
    }

    @Override
    public String toString() {
        return String.format("DonHangAggregate{maDonHang='%s', trangThai=%s, chiTietCount=%d, tongCong=%s}",
                donHang.getMaDonHang(), donHang.getTrangThai(), this.chiTietList.size(), donHang.getTongCong());
    }
}
