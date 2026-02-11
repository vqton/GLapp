package com.tonvq.accountingerp.domain.service;

import com.tonvq.accountingerp.domain.model.valueobject.Tien;
import com.tonvq.accountingerp.domain.model.valueobject.TienTe;
import java.math.BigDecimal;

/**
 * Domain Service: Dự Phòng Nợ
 * 
 * Tính toán dự phòng nợ phải thu theo các phương pháp:
 * - Phương pháp lịch sử: % cố định trên tổng nợ phải thu
 * - Phương pháp tuổi nợ: phân loại theo kỳ hạn
 * - Phương pháp cụ thể: dựa vào từng khách hàng
 * 
 * Theo TT 99/2025/TT-BTC Điều 32: Hạch toán dự phòng nợ phải thu vào TK 229.
 * 
 * @author Ton VQ
 */
public class DuPhongNoService {

    /**
     * Tính dự phòng nợ theo phương pháp lịch sử
     * 
     * Công thức: Dự phòng = Tổng nợ phải thu × %
     * 
     * @param tongNoPhaiThu: Tổng nợ phải thu
     * @param tyLePhongTru: Tỷ lệ phòng trừ (%)
     * @return Dự phòng nợ
     */
    public static Tien calculateDuPhongLichSu(Tien tongNoPhaiThu, double tyLePhongTru) {
        if (tongNoPhaiThu == null || tongNoPhaiThu.isNegative()) {
            throw new IllegalArgumentException("Tổng nợ không được âm");
        }
        if (tyLePhongTru < 0 || tyLePhongTru > 100) {
            throw new IllegalArgumentException("Tỷ lệ phòng trừ phải nằm trong 0-100%");
        }

        return tongNoPhaiThu.multiply(tyLePhongTru / 100.0);
    }

    /**
     * Tính dự phòng nợ theo phương pháp tuổi nợ
     * 
     * Phân loại nợ theo thời gian chưa thu:
     * - Dưới 3 tháng: 1%
     * - 3-6 tháng: 5%
     * - 6-12 tháng: 10%
     * - Trên 12 tháng: 50%
     * 
     * @param noTruoc3Thang: Nợ dưới 3 tháng
     * @param no3Den6Thang: Nợ 3-6 tháng
     * @param no6Den12Thang: Nợ 6-12 tháng
     * @param noTren12Thang: Nợ trên 12 tháng
     * @return Dự phòng nợ tổng cộng
     */
    public static Tien calculateDuPhongTuoiNo(Tien noTruoc3Thang, Tien no3Den6Thang,
                                              Tien no6Den12Thang, Tien noTren12Thang) {
        if (noTruoc3Thang == null || no3Den6Thang == null || 
            no6Den12Thang == null || noTren12Thang == null) {
            throw new IllegalArgumentException("Các tham số không được null");
        }

        Tien duPhongTruoc3Thang = noTruoc3Thang.multiply(0.01);        // 1%
        Tien duPhong3Den6Thang = no3Den6Thang.multiply(0.05);          // 5%
        Tien duPhong6Den12Thang = no6Den12Thang.multiply(0.10);        // 10%
        Tien duPhongTren12Thang = noTren12Thang.multiply(0.50);        // 50%

        return duPhongTruoc3Thang
                .add(duPhong3Den6Thang)
                .add(duPhong6Den12Thang)
                .add(duPhongTren12Thang);
    }

    /**
     * Tính dự phòng nợ theo phương pháp cụ thể
     * 
     * Dựa vào tình trạng tài chính cụ thể của từng khách hàng.
     * 
     * @param tongNoKhachHang: Tổng nợ của khách hàng
     * @param tyLeRuiRo: Tỷ lệ rủi ro cụ thể (%)
     * @return Dự phòng nợ
     */
    public static Tien calculateDuPhongCuThe(Tien tongNoKhachHang, double tyLeRuiRo) {
        if (tongNoKhachHang == null || tongNoKhachHang.isNegative()) {
            throw new IllegalArgumentException("Tổng nợ không được âm");
        }
        if (tyLeRuiRo < 0 || tyLeRuiRo > 100) {
            throw new IllegalArgumentException("Tỷ lệ rủi ro phải nằm trong 0-100%");
        }

        return tongNoKhachHang.multiply(tyLeRuiRo / 100.0);
    }

    /**
     * Hạn chế dự phòng (không vượt quá nợ)
     * 
     * @param duPhongTinh: Dự phòng tính được
     * @param tongNo: Tổng nợ
     * @return Dự phòng sau khi hạn chế
     */
    public static Tien limitDuPhong(Tien duPhongTinh, Tien tongNo) {
        if (duPhongTinh == null || tongNo == null) {
            throw new IllegalArgumentException("Các tham số không được null");
        }

        // Dự phòng không được vượt quá tổng nợ
        if (duPhongTinh.compareTo(tongNo) > 0) {
            return tongNo;
        }

        return duPhongTinh;
    }

    /**
     * Tính điều chỉnh dự phòng
     * 
     * @param duPhongCu: Dự phòng thời kỳ trước
     * @param duPhongMoi: Dự phòng thời kỳ hiện tại
     * @return Điều chỉnh (>0: tăng, <0: giảm)
     */
    public static Tien calculateDieuChinhDuPhong(Tien duPhongCu, Tien duPhongMoi) {
        if (duPhongCu == null || duPhongMoi == null) {
            throw new IllegalArgumentException("Các tham số không được null");
        }

        return duPhongMoi.subtract(duPhongCu);
    }

    /**
     * Kiểm tra đã sử dụng dự phòng để xử lý nợ không
     * 
     * @param tongDuPhongDaSuDung: Tổng dự phòng đã sử dụng
     * @param tongDuPhongCoSan: Tổng dự phòng có sẵn
     * @return true nếu đã sử dụng hết
     */
    public static boolean isDuPhongDaSuDungHet(Tien tongDuPhongDaSuDung, Tien tongDuPhongCoSan) {
        if (tongDuPhongDaSuDung == null || tongDuPhongCoSan == null) {
            throw new IllegalArgumentException("Các tham số không được null");
        }

        return tongDuPhongDaSuDung.compareTo(tongDuPhongCoSan) >= 0;
    }
}
