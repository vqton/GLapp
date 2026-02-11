package com.tonvq.accountingerp.domain.model.entity;

import com.tonvq.accountingerp.domain.model.valueobject.*;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Entity: Chứng Từ (Accounting Document)
 * 
 * Aggregate Root: ChungTu chứa các bút toán liên quan.
 * Đại diện cho chứng từ kế toán gốc.
 * 
 * Theo TT 99/2025/TT-BTC:
 * - Mỗi chứng từ phải có mã định danh duy nhất (TK nợ, TK có, số tiền)
 * - Phải ghi sổ Nhật ký, Sổ Cái
 * - Không được sửa sau khi khóa sổ kỳ
 * - Phải lưu trữ đầy đủ (gốc hoặc bản sao xác thực)
 * 
 * @author Ton VQ
 */
public class ChungTu implements Serializable {
    private static final long serialVersionUID = 1L;

    // ============ Identity ============
    private String id;                              // UUID
    private String maChungTu;                       // Mã chứng từ (VD: CT001, CT002)

    // ============ Document Info ============
    private String loaiChungTu;                     // Loại: HĐ, PKT, PTT, v.v.
    private LocalDateTime ngayChungTu;              // Ngày chứng từ
    private LocalDateTime ngayGhiSo;                // Ngày ghi sổ
    private String ndChungTu;                       // Nội dung (Mô tả nghiệp vụ)
    
    // ============ Accounting Info ============
    private String tkNo;                            // Tài khoản nợ (VD: 1010)
    private String tkCo;                            // Tài khoản có (VD: 4011)
    private Tien soTien;                            // Số tiền

    // ============ Status & Audit ============
    private TrangThaiChungTu trangThai;             // DRAFT, POSTED, LOCKED, CANCELLED
    private String createdBy;                       // Người tạo
    private LocalDateTime createdAt;                // Thời gian tạo
    private String lastModifiedBy;                  // Người sửa cuối
    private LocalDateTime lastModifiedAt;           // Thời gian sửa cuối
    private String approvedBy;                      // Người duyệt
    private LocalDateTime approvedAt;               // Thời gian duyệt
    private String lockedBy;                        // Người khóa
    private LocalDateTime lockedAt;                 // Thời gian khóa

    // ============ Additional Fields ============
    private String soHieuChungTu;                   // Số hiệu từ chứng từ gốc
    private String moTa;                            // Mô tả chi tiết
    private boolean kyDienTu;                       // Có ký số không?
    private String chungChiKyDienTu;                // Chứng chỉ ký số (nếu có)

    private List<ButToan> butToanList = new ArrayList<>();  // Danh sách bút toán

    // ============ Constructors ============
    public ChungTu() {
    }

    public ChungTu(String maChungTu, String loaiChungTu, LocalDateTime ngayChungTu, 
                   String tkNo, String tkCo, Tien soTien, String ndChungTu) {
        validateChungTu(maChungTu, loaiChungTu, ngayChungTu, tkNo, tkCo, soTien, ndChungTu);
        this.id = UUID.randomUUID().toString();
        this.maChungTu = maChungTu;
        this.loaiChungTu = loaiChungTu;
        this.ngayChungTu = ngayChungTu;
        this.tkNo = tkNo;
        this.tkCo = tkCo;
        this.soTien = soTien;
        this.ndChungTu = ndChungTu;
        this.trangThai = TrangThaiChungTu.DRAFT;
        this.createdAt = LocalDateTime.now();
    }

    // ============ Validation ============
    private static void validateChungTu(String maChungTu, String loaiChungTu,
                                       LocalDateTime ngayChungTu, String tkNo,
                                       String tkCo, Tien soTien, String ndChungTu) {
        if (maChungTu == null || maChungTu.trim().isEmpty()) {
            throw new IllegalArgumentException("Mã chứng từ không được rỗng");
        }
        if (loaiChungTu == null || loaiChungTu.trim().isEmpty()) {
            throw new IllegalArgumentException("Loại chứng từ không được rỗng");
        }
        if (ngayChungTu == null) {
            throw new IllegalArgumentException("Ngày chứng từ không được null");
        }
        if (tkNo == null || tkNo.trim().isEmpty()) {
            throw new IllegalArgumentException("Tài khoản nợ không được rỗng");
        }
        if (tkCo == null || tkCo.trim().isEmpty()) {
            throw new IllegalArgumentException("Tài khoản có không được rỗng");
        }
        if (tkNo.equals(tkCo)) {
            throw new IllegalArgumentException("Tài khoản nợ và tài khoản có phải khác nhau");
        }
        if (soTien == null || soTien.isNegative()) {
            throw new IllegalArgumentException("Số tiền phải là số dương");
        }
        if (ndChungTu == null || ndChungTu.trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung chứng từ không được rỗng");
        }
    }

    // ============ Business Methods ============

    /**
     * Ghi sổ chứng từ (từ DRAFT sang POSTED)
     */
    public void ghiSo(String ghiSoBy) {
        if (!trangThai.canPost()) {
            throw new IllegalStateException(
                String.format("Không thể ghi sổ chứng từ ở trạng thái %s", trangThai.getLabel())
            );
        }
        this.trangThai = TrangThaiChungTu.POSTED;
        this.ngayGhiSo = LocalDateTime.now();
        this.lastModifiedBy = ghiSoBy;
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Khóa chứng từ (từ POSTED sang LOCKED)
     */
    public void khoa(String khoaBy) {
        if (!trangThai.canLock()) {
            throw new IllegalStateException(
                String.format("Không thể khóa chứng từ ở trạng thái %s", trangThai.getLabel())
            );
        }
        this.trangThai = TrangThaiChungTu.LOCKED;
        this.lockedBy = khoaBy;
        this.lockedAt = LocalDateTime.now();
        this.lastModifiedBy = khoaBy;
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Hủy chứng từ
     */
    public void huy(String huyBy) {
        if (!trangThai.canCancel()) {
            throw new IllegalStateException(
                String.format("Không thể hủy chứng từ ở trạng thái %s", trangThai.getLabel())
            );
        }
        this.trangThai = TrangThaiChungTu.CANCELLED;
        this.lastModifiedBy = huyBy;
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Sửa chứng từ (chỉ khi DRAFT)
     */
    public void sua(String tkNo, String tkCo, Tien soTien, String ndChungTu, String suaBy) {
        if (!trangThai.canEdit()) {
            throw new IllegalStateException(
                String.format("Không thể sửa chứng từ ở trạng thái %s", trangThai.getLabel())
            );
        }
        validateChungTu(maChungTu, loaiChungTu, ngayChungTu, tkNo, tkCo, soTien, ndChungTu);
        this.tkNo = tkNo;
        this.tkCo = tkCo;
        this.soTien = soTien;
        this.ndChungTu = ndChungTu;
        this.lastModifiedBy = suaBy;
        this.lastModifiedAt = LocalDateTime.now();
    }

    /**
     * Kiểm tra chứng từ có hợp lệ không
     */
    public boolean isValid() {
        return trangThai != TrangThaiChungTu.CANCELLED && 
               trangThai != TrangThaiChungTu.DRAFT;
    }

    /**
     * Kiểm tra chứng từ đã ghi sổ chưa
     */
    public boolean isDaGhiSo() {
        return trangThai == TrangThaiChungTu.POSTED ||
               trangThai == TrangThaiChungTu.LOCKED;
    }

    /**
     * Kiểm tra chứng từ đã khóa chưa
     */
    public boolean isDaKhoa() {
        return trangThai == TrangThaiChungTu.LOCKED;
    }

    /**
     * Thêm bút toán
     */
    public void addButToan(ButToan butToan) {
        if (butToan == null) {
            throw new IllegalArgumentException("Bút toán không được null");
        }
        if (!trangThai.canEdit()) {
            throw new IllegalStateException("Chỉ có thể thêm bút toán khi chứng từ ở trạng thái DRAFT");
        }
        this.butToanList.add(butToan);
    }

    /**
     * Xóa bút toán
     */
    public void removeButToan(ButToan butToan) {
        if (!trangThai.canEdit()) {
            throw new IllegalStateException("Chỉ có thể xóa bút toán khi chứng từ ở trạng thái DRAFT");
        }
        this.butToanList.remove(butToan);
    }

    /**
     * Lấy danh sách bút toán
     */
    public List<ButToan> getButToanList() {
        return Collections.unmodifiableList(butToanList);
    }

    /**
     * Kiểm tra bút toán có cân bằng không (Nợ = Có)
     */
    public boolean isBalanced() {
        Tien tongNo = Tien.zeroVND();
        Tien tongCo = Tien.zeroVND();
        for (ButToan bt : butToanList) {
            if (bt.isNo()) {
                tongNo = tongNo.add(bt.getSoTien());
            } else {
                tongCo = tongCo.add(bt.getSoTien());
            }
        }
        return tongNo.equals(tongCo);
    }

    // ============ Getters & Setters ============
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMaChungTu() {
        return maChungTu;
    }

    public void setMaChungTu(String maChungTu) {
        this.maChungTu = maChungTu;
    }

    public String getLoaiChungTu() {
        return loaiChungTu;
    }

    public void setLoaiChungTu(String loaiChungTu) {
        this.loaiChungTu = loaiChungTu;
    }

    public LocalDateTime getNgayChungTu() {
        return ngayChungTu;
    }

    public void setNgayChungTu(LocalDateTime ngayChungTu) {
        this.ngayChungTu = ngayChungTu;
    }

    public LocalDateTime getNgayGhiSo() {
        return ngayGhiSo;
    }

    public void setNgayGhiSo(LocalDateTime ngayGhiSo) {
        this.ngayGhiSo = ngayGhiSo;
    }

    public String getNdChungTu() {
        return ndChungTu;
    }

    public void setNdChungTu(String ndChungTu) {
        this.ndChungTu = ndChungTu;
    }

    public String getTkNo() {
        return tkNo;
    }

    public void setTkNo(String tkNo) {
        this.tkNo = tkNo;
    }

    public String getTkCo() {
        return tkCo;
    }

    public void setTkCo(String tkCo) {
        this.tkCo = tkCo;
    }

    public Tien getSoTien() {
        return soTien;
    }

    public void setSoTien(Tien soTien) {
        this.soTien = soTien;
    }

    public TrangThaiChungTu getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiChungTu trangThai) {
        this.trangThai = trangThai;
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

    public String getApprovedBy() {
        return approvedBy;
    }

    public void setApprovedBy(String approvedBy) {
        this.approvedBy = approvedBy;
    }

    public LocalDateTime getApprovedAt() {
        return approvedAt;
    }

    public void setApprovedAt(LocalDateTime approvedAt) {
        this.approvedAt = approvedAt;
    }

    public String getLockedBy() {
        return lockedBy;
    }

    public void setLockedBy(String lockedBy) {
        this.lockedBy = lockedBy;
    }

    public LocalDateTime getLockedAt() {
        return lockedAt;
    }

    public void setLockedAt(LocalDateTime lockedAt) {
        this.lockedAt = lockedAt;
    }

    public String getSoHieuChungTu() {
        return soHieuChungTu;
    }

    public void setSoHieuChungTu(String soHieuChungTu) {
        this.soHieuChungTu = soHieuChungTu;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public boolean isKyDienTu() {
        return kyDienTu;
    }

    public void setKyDienTu(boolean kyDienTu) {
        this.kyDienTu = kyDienTu;
    }

    public String getChungChiKyDienTu() {
        return chungChiKyDienTu;
    }

    public void setChungChiKyDienTu(String chungChiKyDienTu) {
        this.chungChiKyDienTu = chungChiKyDienTu;
    }

    // ============ equals & hashCode ============
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ChungTu chungTu = (ChungTu) o;
        return Objects.equals(id, chungTu.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("ChungTu{id='%s', maChungTu='%s', loaiChungTu='%s', soTien=%s, trangThai=%s}",
                id, maChungTu, loaiChungTu, soTien, trangThai);
    }
}
