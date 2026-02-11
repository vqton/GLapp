package com.tonvq.accountingerp.domain.model.entity;

import com.tonvq.accountingerp.domain.model.valueobject.Tien;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.UUID;

/**
 * Entity: Hợp Đồng Dịch Vụ (Service Contract)
 * 
 * Đại diện cho hợp đồng cung cấp dịch vụ dài hạn.
 * Doanh thu được công nhận dần theo milestone hoặc phương pháp % hoàn thành.
 * 
 * Theo VAS 14/15 (Service Revenue Recognition).
 * 
 * @author Ton VQ
 */
public class HopDongDichVu implements Serializable {
    private static final long serialVersionUID = 1L;

    private String id;                              // UUID
    private String maHopDong;                       // Mã hợp đồng
    private String maKhachHang;                     // Mã khách hàng
    private String tenKhachHang;                    // Tên khách hàng
    
    private LocalDateTime ngayKy;                   // Ngày ký hợp đồng
    private LocalDateTime ngayBatDau;               // Ngày bắt đầu cung cấp dịch vụ
    private LocalDateTime ngayKetThuc;              // Ngày kết thúc hợp đồng
    
    // ============ Contract Value ============
    private Tien tongGiaTriHopDong;                 // Tổng giá trị hợp đồng
    private Tien tongChiPhiDuKien;                  // Tổng chi phí dự kiến
    private Tien tongChiPhiThucTe;                  // Tổng chi phí thực tế
    private Tien tongDoanhThuCongNhan;              // Tổng doanh thu công nhận
    
    // ============ Milestone & Progress ============
    private int soMilestone;                        // Số lượng milestone
    private int milestoneHoanThanh;                 // Số milestone đã hoàn thành
    private double percentComplete = 0;             // % hoàn thành (0-100)
    
    // ============ Status ============
    private String trangThaiHopDong;                // DRAFT, ACTIVE, COMPLETED, CANCELLED
    
    // ============ Audit ============
    private String createdBy;                       // Người tạo
    private LocalDateTime createdAt;                // Thời gian tạo
    private String lastModifiedBy;                  // Người sửa cuối
    private LocalDateTime lastModifiedAt;           // Thời gian sửa cuối
    
    private String moTa;                            // Mô tả hợp đồng
    private boolean isActive = true;                // Còn sử dụng?

    // ============ Constructors ============
    public HopDongDichVu() {
    }

    public HopDongDichVu(String maHopDong, String maKhachHang, String tenKhachHang,
                        LocalDateTime ngayBatDau, LocalDateTime ngayKetThuc,
                        Tien tongGiaTriHopDong) {
        validateHopDongDichVu(maHopDong, maKhachHang, tenKhachHang, 
                            ngayBatDau, ngayKetThuc, tongGiaTriHopDong);
        this.id = UUID.randomUUID().toString();
        this.maHopDong = maHopDong;
        this.maKhachHang = maKhachHang;
        this.tenKhachHang = tenKhachHang;
        this.ngayKy = LocalDateTime.now();
        this.ngayBatDau = ngayBatDau;
        this.ngayKetThuc = ngayKetThuc;
        this.tongGiaTriHopDong = tongGiaTriHopDong;
        this.tongChiPhiDuKien = Tien.zeroVND();
        this.tongChiPhiThucTe = Tien.zeroVND();
        this.tongDoanhThuCongNhan = Tien.zeroVND();
        this.trangThaiHopDong = "DRAFT";
        this.createdAt = LocalDateTime.now();
    }

    private static void validateHopDongDichVu(String maHopDong, String maKhachHang,
                                             String tenKhachHang, LocalDateTime ngayBatDau,
                                             LocalDateTime ngayKetThuc, Tien tongGiaTriHopDong) {
        if (maHopDong == null || maHopDong.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã hợp đồng không được rỗng");
        }
        if (maKhachHang == null || maKhachHang.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã khách hàng không được rỗng");
        }
        if (tenKhachHang == null || tenKhachHang.trim().isEmpty()) {
            throw new IllegalArgumentException("Tên khách hàng không được rỗng");
        }
        if (ngayBatDau == null || ngayKetThuc == null) {
            throw new IllegalArgumentException("Ngày bắt đầu/kết thúc không được null");
        }
        if (ngayBatDau.isAfter(ngayKetThuc)) {
            throw new IllegalArgumentException("Ngày bắt đầu phải trước ngày kết thúc");
        }
        if (tongGiaTriHopDong == null || tongGiaTriHopDong.isNegative()) {
            throw new IllegalArgumentException("Giá trị hợp đồng không được âm");
        }
    }

    // ============ Business Methods ============

    /**
     * Kích hoạt hợp đồng
     */
    public void kichHoat(String kichHoatBy) {
        if (!this.trangThaiHopDong.equals("DRAFT")) {
            throw new IllegalStateException(
                String.format("Chỉ có thể kích hoạt hợp đồng ở trạng thái DRAFT, hiện tại: %s", 
                    this.trangThaiHopDong)
            );
        }
        this.trangThaiHopDong = "ACTIVE";
        this.lastModifiedBy = kichHoatBy;
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Cập nhật tiến độ/milestone
     */
    public void updateTienDo(int milestoneHoanThanh, double percentComplete, String updateBy) {
        if (milestoneHoanThanh < 0 || milestoneHoanThanh > this.soMilestone) {
            throw new IllegalArgumentException(
                String.format("Milestone hoàn thành phải từ 0 đến %d", this.soMilestone)
            );
        }
        if (percentComplete < 0 || percentComplete > 100) {
            throw new IllegalArgumentException("% hoàn thành phải từ 0 đến 100");
        }
        
        this.milestoneHoanThanh = milestoneHoanThanh;
        this.percentComplete = percentComplete;
        this.lastModifiedBy = updateBy;
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Cập nhật chi phí thực tế
     */
    public void updateChiPhiThucTe(Tien chiPhiThucTe, String updateBy) {
        if (chiPhiThucTe == null || chiPhiThucTe.isNegative()) {
            throw new IllegalArgumentException("Chi phí không được âm");
        }
        this.tongChiPhiThucTe = chiPhiThucTe;
        this.lastModifiedBy = updateBy;
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Ghi nhận doanh thu công nhân
     */
    public void ghiNhanDoanhThuCongNhan(Tien doanhThuCongNhan, String ghiNhanBy) {
        if (doanhThuCongNhan == null || doanhThuCongNhan.isNegative()) {
            throw new IllegalArgumentException("Doanh thu không được âm");
        }
        if (doanhThuCongNhan.compareTo(this.tongGiaTriHopDong) > 0) {
            throw new IllegalArgumentException(
                "Doanh thu công nhân không được vượt quá tổng giá trị hợp đồng"
            );
        }
        this.tongDoanhThuCongNhan = doanhThuCongNhan;
        this.lastModifiedBy = ghiNhanBy;
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Hoàn thành hợp đồng
     */
    public void hoanThanh(String hoanthanhBy) {
        if (!this.trangThaiHopDong.equals("ACTIVE")) {
            throw new IllegalStateException(
                String.format("Chỉ có thể hoàn thành hợp đồng ở trạng thái ACTIVE, hiện tại: %s", 
                    this.trangThaiHopDong)
            );
        }
        this.trangThaiHopDong = "COMPLETED";
        this.percentComplete = 100;
        this.lastModifiedBy = hoanthanhBy;
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Hủy hợp đồng
     */
    public void huy(String huyBy) {
        this.trangThaiHopDong = "CANCELLED";
        this.lastModifiedBy = huyBy;
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Tính lợi nhuận ước tính
     */
    public Tien calculateLaiNhuanUocTinh() {
        return this.tongGiaTriHopDong.subtract(this.tongChiPhiDuKien);
    }

    /**
     * Kiểm tra hợp đồng có lỗ không
     */
    public boolean isLossContract() {
        return this.tongChiPhiDuKien.compareTo(this.tongGiaTriHopDong) > 0;
    }

    // ============ Getters & Setters ============
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMaHopDong() {
        return maHopDong;
    }

    public void setMaHopDong(String maHopDong) {
        this.maHopDong = maHopDong;
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

    public LocalDateTime getNgayKy() {
        return ngayKy;
    }

    public void setNgayKy(LocalDateTime ngayKy) {
        this.ngayKy = ngayKy;
    }

    public LocalDateTime getNgayBatDau() {
        return ngayBatDau;
    }

    public void setNgayBatDau(LocalDateTime ngayBatDau) {
        this.ngayBatDau = ngayBatDau;
    }

    public LocalDateTime getNgayKetThuc() {
        return ngayKetThuc;
    }

    public void setNgayKetThuc(LocalDateTime ngayKetThuc) {
        this.ngayKetThuc = ngayKetThuc;
    }

    public Tien getTongGiaTriHopDong() {
        return tongGiaTriHopDong;
    }

    public void setTongGiaTriHopDong(Tien tongGiaTriHopDong) {
        this.tongGiaTriHopDong = tongGiaTriHopDong;
    }

    public Tien getTongChiPhiDuKien() {
        return tongChiPhiDuKien;
    }

    public void setTongChiPhiDuKien(Tien tongChiPhiDuKien) {
        this.tongChiPhiDuKien = tongChiPhiDuKien;
    }

    public Tien getTongChiPhiThucTe() {
        return tongChiPhiThucTe;
    }

    public void setTongChiPhiThucTe(Tien tongChiPhiThucTe) {
        this.tongChiPhiThucTe = tongChiPhiThucTe;
    }

    public Tien getTongDoanhThuCongNhan() {
        return tongDoanhThuCongNhan;
    }

    public void setTongDoanhThuCongNhan(Tien tongDoanhThuCongNhan) {
        this.tongDoanhThuCongNhan = tongDoanhThuCongNhan;
    }

    public int getSoMilestone() {
        return soMilestone;
    }

    public void setSoMilestone(int soMilestone) {
        this.soMilestone = soMilestone;
    }

    public int getMilestoneHoanThanh() {
        return milestoneHoanThanh;
    }

    public void setMilestoneHoanThanh(int milestoneHoanThanh) {
        this.milestoneHoanThanh = milestoneHoanThanh;
    }

    public double getPercentComplete() {
        return percentComplete;
    }

    public void setPercentComplete(double percentComplete) {
        this.percentComplete = percentComplete;
    }

    public String getTrangThaiHopDong() {
        return trangThaiHopDong;
    }

    public void setTrangThaiHopDong(String trangThaiHopDong) {
        this.trangThaiHopDong = trangThaiHopDong;
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

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        HopDongDichVu that = (HopDongDichVu) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("HopDongDichVu{id='%s', maHopDong='%s', tenKhachHang='%s', tongGiaTriHopDong=%s, trangThai=%s}",
                id, maHopDong, tenKhachHang, tongGiaTriHopDong, trangThaiHopDong);
    }
}
