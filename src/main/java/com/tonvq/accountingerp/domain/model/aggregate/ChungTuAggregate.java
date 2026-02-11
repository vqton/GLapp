package com.tonvq.accountingerp.domain.model.aggregate;

import com.tonvq.accountingerp.domain.model.entity.ChungTu;
import com.tonvq.accountingerp.domain.model.entity.ButToan;
import com.tonvq.accountingerp.domain.model.valueobject.*;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Aggregate Root: Chứng Từ Aggregate
 * 
 * Đây là root aggregate quản lý toàn bộ chứng từ + bút toán.
 * Bảo vệ các invariant:
 * - Chứng từ phải có ít nhất 1 bút toán
 * - Nợ = Có (cân bằng)
 * - Không được sửa sau khi khóa
 * 
 * @author Ton VQ
 */
public class ChungTuAggregate implements Serializable {
    private static final long serialVersionUID = 1L;

    private ChungTu chungTu;                        // Entity root
    private List<ButToan> butToanList = new ArrayList<>();

    // ============ Constructor ============
    public ChungTuAggregate(ChungTu chungTu) {
        if (chungTu == null) {
            throw new IllegalArgumentException("Chứng từ không được null");
        }
        this.chungTu = chungTu;
    }

    // ============ Invariant Methods ============

    /**
     * Thêm bút toán vào chứng từ (bảo vệ invariant)
     */
    public void addButToan(ButToan butToan) {
        if (butToan == null) {
            throw new IllegalArgumentException("Bút toán không được null");
        }
        if (chungTu.isDaKhoa()) {
            throw new IllegalStateException("Không thể thêm bút toán vào chứng từ đã khóa");
        }
        if (!chungTu.getTrangThai().canEdit()) {
            throw new IllegalStateException(
                String.format("Không thể thêm bút toán ở trạng thái %s", chungTu.getTrangThai())
            );
        }
        this.butToanList.add(butToan);
        this.chungTu.addButToan(butToan);
    }

    /**
     * Xóa bút toán
     */
    public void removeButToan(String butToanId) {
        ButToan toRemove = this.butToanList.stream()
            .filter(bt -> bt.getId().equals(butToanId))
            .findFirst()
            .orElseThrow(() -> new IllegalArgumentException("Bút toán không tìm thấy"));
        
        this.butToanList.remove(toRemove);
        this.chungTu.removeButToan(toRemove);
    }

    /**
     * Kiểm tra chứng từ có cân bằng không (Nợ = Có)
     */
    public boolean isBalanced() {
        return this.chungTu.isBalanced();
    }

    /**
     * Lấy danh sách bút toán
     */
    public List<ButToan> getButToanList() {
        return Collections.unmodifiableList(this.butToanList);
    }

    /**
     * Ghi sổ chứng từ (DRAFT → POSTED)
     * Yêu cầu: chứng từ phải cân bằng + có ít nhất 1 bút toán
     */
    public void ghiSo(String ghiSoBy) {
        if (this.butToanList.isEmpty()) {
            throw new IllegalStateException("Chứng từ phải có ít nhất 1 bút toán");
        }
        if (!isBalanced()) {
            throw new IllegalStateException(
                "Chứng từ không cân bằng. Tổng nợ ≠ Tổng có"
            );
        }
        this.chungTu.ghiSo(ghiSoBy);
    }

    /**
     * Khóa chứng từ
     */
    public void khoa(String khoaBy) {
        if (!this.chungTu.isDaGhiSo()) {
            throw new IllegalStateException(
                "Chỉ có thể khóa chứng từ đã ghi sổ"
            );
        }
        this.chungTu.khoa(khoaBy);
    }

    /**
     * Hủy chứng từ
     */
    public void huy(String huyBy) {
        this.chungTu.huy(huyBy);
    }

    /**
     * Tính tổng VAT (nếu có)
     * Công thức: Tổng = (Tổng gốc × VAT%) / 100
     */
    public Tien calculateVAT(double vat) {
        if (vat < 0 || vat > 100) {
            throw new IllegalArgumentException("VAT phải nằm trong khoảng 0-100");
        }
        Tien tongGoc = chungTu.getSoTien();
        return tongGoc.multiply(vat / 100.0);
    }

    /**
     * Lấy tổng tiền chứng từ
     */
    public Tien getTotalAmount() {
        return this.chungTu.getSoTien();
    }

    /**
     * Kiểm tra có thể chỉnh sửa không
     */
    public boolean canEdit() {
        return this.chungTu.getTrangThai().canEdit();
    }

    /**
     * Kiểm tra có thể ghi sổ không
     */
    public boolean canPost() {
        return this.chungTu.getTrangThai().canPost() && !this.butToanList.isEmpty();
    }

    /**
     * Kiểm tra có thể khóa không
     */
    public boolean canLock() {
        return this.chungTu.getTrangThai().canLock();
    }

    // ============ Getters ============
    public ChungTu getChungTu() {
        return chungTu;
    }

    public String getId() {
        return chungTu.getId();
    }

    public String getMaChungTu() {
        return chungTu.getMaChungTu();
    }

    public String getLoaiChungTu() {
        return chungTu.getLoaiChungTu();
    }

    public LocalDateTime getNgayChungTu() {
        return chungTu.getNgayChungTu();
    }

    public TrangThaiChungTu getTrangThai() {
        return chungTu.getTrangThai();
    }

    public int getButToanCount() {
        return this.butToanList.size();
    }

    @Override
    public String toString() {
        return String.format("ChungTuAggregate{maChungTu='%s', trangThai=%s, butToanCount=%d, balanced=%s}",
                chungTu.getMaChungTu(), chungTu.getTrangThai(), this.butToanList.size(), isBalanced());
    }
}
