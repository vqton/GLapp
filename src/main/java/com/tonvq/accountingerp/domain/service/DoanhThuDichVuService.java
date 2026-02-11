package com.tonvq.accountingerp.domain.service;

import com.tonvq.accountingerp.domain.model.valueobject.*;
import java.math.BigDecimal;

/**
 * Domain Service: Doanh Thu Dịch Vụ
 * 
 * Tính toán doanh thu dịch vụ theo milestone hoặc phương pháp khác.
 * Áp dụng cho hợp đồng cung cấp dịch vụ dài hạn.
 * 
 * Theo VAS 14/15 (Service Revenue Recognition):
 * - Doanh thu công nhân khi dịch vụ được hoàn thành hoặc milestone đạt
 * - Có thể công nhân dần từng giai đoạn
 * 
 * @author Ton VQ
 */
public class DoanhThuDichVuService {

    /**
     * Tính doanh thu theo milestone
     * 
     * @param tongGiaTriHopDong: Tổng giá trị hợp đồng
     * @param percentComplete: Tỷ lệ hoàn thành (%)
     * @return Doanh thu công nhận trong kỳ
     */
    public static Tien calculateDoanhThuMilestone(Tien tongGiaTriHopDong, double percentComplete) {
        if (tongGiaTriHopDong == null || tongGiaTriHopDong.isNegative()) {
            throw new IllegalArgumentException("Giá trị hợp đồng không được âm");
        }
        if (percentComplete < 0 || percentComplete > 100) {
            throw new IllegalArgumentException("Tỷ lệ hoàn thành phải nằm trong 0-100%");
        }

        return tongGiaTriHopDong.multiply(percentComplete / 100.0);
    }

    /**
     * Tính doanh thu theo phương pháp công nhân dần (percentage-of-completion)
     * 
     * @param tongGiaTriHopDong: Tổng giá trị hợp đồng
     * @param chiPhiThucTe: Chi phí thực tế phát sinh
     * @param chiPhiDuKien: Chi phí dự kiến toàn hợp đồng
     * @return Doanh thu cần công nhận trong kỳ
     */
    public static Tien calculateDoanhThuCongNhanDan(Tien tongGiaTriHopDong,
                                                     Tien chiPhiThucTe,
                                                     Tien chiPhiDuKien) {
        if (tongGiaTriHopDong == null || tongGiaTriHopDong.isNegative()) {
            throw new IllegalArgumentException("Giá trị hợp đồng không được âm");
        }
        if (chiPhiThucTe == null || chiPhiThucTe.isNegative()) {
            throw new IllegalArgumentException("Chi phí thực tế không được âm");
        }
        if (chiPhiDuKien == null || chiPhiDuKien.isNegative()) {
            throw new IllegalArgumentException("Chi phí dự kiến không được âm");
        }

        if (chiPhiDuKien.isZero()) {
            throw new IllegalArgumentException("Chi phí dự kiến không được 0");
        }

        // Tỷ lệ hoàn thành = Chi phí thực tế / Chi phí dự kiến
        BigDecimal percentComplete = chiPhiThucTe.getAmount()
                .divide(chiPhiDuKien.getAmount(), 4, BigDecimal.ROUND_HALF_UP);

        // Doanh thu = Tổng giá trị hợp đồng × Tỷ lệ hoàn thành
        return tongGiaTriHopDong.multiply(percentComplete);
    }

    /**
     * Tính doanh thu theo phương pháp hoàn thành (completed contract method)
     * Chỉ công nhân doanh thu khi hợp đồng hoàn tất
     * 
     * @param tongGiaTriHopDong: Tổng giá trị hợp đồng
     * @param isCompleted: Hợp đồng đã hoàn tất chưa?
     * @return Doanh thu (0 nếu chưa hoàn tất, toàn bộ nếu hoàn tất)
     */
    public static Tien calculateDoanhThuHoanThanh(Tien tongGiaTriHopDong, boolean isCompleted) {
        if (tongGiaTriHopDong == null || tongGiaTriHopDong.isNegative()) {
            throw new IllegalArgumentException("Giá trị hợp đồng không được âm");
        }

        if (isCompleted) {
            return tongGiaTriHopDong;
        } else {
            return Tien.zeroVND();
        }
    }

    /**
     * Tính lợi nhuận ước tính từ hợp đồng
     * 
     * @param tongGiaTriHopDong: Tổng giá trị hợp đồng
     * @param tongChiPhiDuKien: Tổng chi phí dự kiến
     * @return Lợi nhuận ước tính
     */
    public static Tien calculateLaiNhuanUocTinh(Tien tongGiaTriHopDong, Tien tongChiPhiDuKien) {
        if (tongGiaTriHopDong == null || tongChiPhiDuKien == null) {
            throw new IllegalArgumentException("Các tham số không được null");
        }

        if (tongGiaTriHopDong.isNegative() || tongChiPhiDuKien.isNegative()) {
            throw new IllegalArgumentException("Giá trị không được âm");
        }

        return tongGiaTriHopDong.subtract(tongChiPhiDuKien);
    }

    /**
     * Kiểm tra hợp đồng có lỗ không (lỗ = chi phí > doanh thu)
     */
    public static boolean isLossContract(Tien tongGiaTriHopDong, Tien tongChiPhiDuKien) {
        if (tongGiaTriHopDong == null || tongChiPhiDuKien == null) {
            throw new IllegalArgumentException("Các tham số không được null");
        }

        return tongChiPhiDuKien.compareTo(tongGiaTriHopDong) > 0;
    }
}
