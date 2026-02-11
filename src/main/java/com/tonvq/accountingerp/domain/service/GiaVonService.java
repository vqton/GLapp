package com.tonvq.accountingerp.domain.service;

import com.tonvq.accountingerp.domain.model.valueobject.*;
import java.math.BigDecimal;
import java.util.*;

/**
 * Domain Service: Giá Vốn Service
 * 
 * Tính toán giá vốn hàng bán theo các phương pháp:
 * - FIFO (First In, First Out)
 * - LIFO (Last In, First Out)
 * - Trung bình gia quyền
 * 
 * Theo TT 99/2025/TT-BTC & Chuẩn kế toán Việt Nam.
 * 
 * @author Ton VQ
 */
public class GiaVonService {

    /**
     * Tính giá vốn FIFO
     * 
     * Nguyên tắc: Sản phẩm nhập vào trước sẽ được xuất trước
     * Hàng nhập mới nhất sẽ còn lại trong tồn kho
     * 
     * @param tonKhoDau: Số lượng tồn kho đầu kỳ
     * @param giaVonDau: Giá vốn đầu kỳ
     * @param luotNhap: Danh sách {soLuong, giaBuy} nhập trong kỳ
     * @param soLuongXuat: Số lượng xuất trong kỳ
     * @return GiaVon của hàng xuất (giá vốn trung bình từ các lô nhập sớm)
     */
    public static GiaVon calculateFIFO(SoLuong tonKhoDau, GiaVon giaVonDau,
                                       List<Map.Entry<SoLuong, GiaVon>> luotNhap,
                                       SoLuong soLuongXuat) {
        if (tonKhoDau == null || giaVonDau == null || luotNhap == null || soLuongXuat == null) {
            throw new IllegalArgumentException("Các tham số không được null");
        }

        List<LoDuocNhap> dsCacLo = new ArrayList<>();
        
        // Thêm lô đầu kỳ
        dsCacLo.add(new LoDuocNhap(tonKhoDau, giaVonDau.getGia()));
        
        // Thêm các lô nhập trong kỳ
        for (Map.Entry<SoLuong, GiaVon> entry : luotNhap) {
            dsCacLo.add(new LoDuocNhap(entry.getKey(), entry.getValue().getGia()));
        }

        // Tính giá vốn trung bình của lô xuất (FIFO)
        SoLuong soLuongConLai = soLuongXuat;
        Tien tongGiaXuat = Tien.zeroVND();

        for (LoDuocNhap lo : dsCacLo) {
            if (soLuongConLai.isZero()) {
                break;
            }

            if (lo.soLuong.compareTo(soLuongConLai) >= 0) {
                // Lô này đủ để xuất
                tongGiaXuat = tongGiaXuat.add(lo.gia.multiply(soLuongConLai.getValue()));
                soLuongConLai = new SoLuong(BigDecimal.ZERO, soLuongConLai.getUnit());
            } else {
                // Lô này không đủ, xuất hết lô này
                tongGiaXuat = tongGiaXuat.add(lo.gia.multiply(lo.soLuong.getValue()));
                soLuongConLai = soLuongConLai.subtract(lo.soLuong);
            }
        }

        if (!soLuongConLai.isZero()) {
            throw new IllegalArgumentException("Không đủ hàng để xuất theo FIFO");
        }

        // Tính giá trung bình
        BigDecimal giaVonTrungBinh = tongGiaXuat.getAmount()
                .divide(soLuongXuat.getValue(), BigDecimal.ROUND_HALF_UP);
        Tien giaVonTB = new Tien(giaVonTrungBinh, TienTe.vnd());
        
        return new GiaVon(giaVonTB, GiaVon.FIFO);
    }

    /**
     * Tính giá vốn LIFO
     * 
     * Nguyên tắc: Sản phẩm nhập vào sau sẽ được xuất trước
     * Hàng nhập sớm nhất sẽ còn lại trong tồn kho
     * 
     * @param tonKhoDau: Số lượng tồn kho đầu kỳ
     * @param giaVonDau: Giá vốn đầu kỳ
     * @param luotNhap: Danh sách {soLuong, giaBuy} nhập trong kỳ
     * @param soLuongXuat: Số lượng xuất trong kỳ
     * @return GiaVon của hàng xuất (giá vốn trung bình từ các lô nhập gần)
     */
    public static GiaVon calculateLIFO(SoLuong tonKhoDau, GiaVon giaVonDau,
                                       List<Map.Entry<SoLuong, GiaVon>> luotNhap,
                                       SoLuong soLuongXuat) {
        if (tonKhoDau == null || giaVonDau == null || luotNhap == null || soLuongXuat == null) {
            throw new IllegalArgumentException("Các tham số không được null");
        }

        List<LoDuocNhap> dsCacLo = new ArrayList<>();
        
        // Thêm lô đầu kỳ
        dsCacLo.add(new LoDuocNhap(tonKhoDau, giaVonDau.getGia()));
        
        // Thêm các lô nhập trong kỳ
        for (Map.Entry<SoLuong, GiaVon> entry : luotNhap) {
            dsCacLo.add(new LoDuocNhap(entry.getKey(), entry.getValue().getGia()));
        }

        // Đảo ngược danh sách (LIFO: xuất từ lô cuối cùng)
        Collections.reverse(dsCacLo);

        // Tính giá vốn trung bình của lô xuất (LIFO)
        SoLuong soLuongConLai = soLuongXuat;
        Tien tongGiaXuat = Tien.zeroVND();

        for (LoDuocNhap lo : dsCacLo) {
            if (soLuongConLai.isZero()) {
                break;
            }

            if (lo.soLuong.compareTo(soLuongConLai) >= 0) {
                // Lô này đủ để xuất
                tongGiaXuat = tongGiaXuat.add(lo.gia.multiply(soLuongConLai.getValue()));
                soLuongConLai = new SoLuong(BigDecimal.ZERO, soLuongConLai.getUnit());
            } else {
                // Lô này không đủ, xuất hết lô này
                tongGiaXuat = tongGiaXuat.add(lo.gia.multiply(lo.soLuong.getValue()));
                soLuongConLai = soLuongConLai.subtract(lo.soLuong);
            }
        }

        if (!soLuongConLai.isZero()) {
            throw new IllegalArgumentException("Không đủ hàng để xuất theo LIFO");
        }

        // Tính giá trung bình
        BigDecimal giaVonTrungBinh = tongGiaXuat.getAmount()
                .divide(soLuongXuat.getValue(), BigDecimal.ROUND_HALF_UP);
        Tien giaVonTB = new Tien(giaVonTrungBinh, TienTe.vnd());
        
        return new GiaVon(giaVonTB, GiaVon.LIFO);
    }

    /**
     * Tính giá vốn trung bình gia quyền
     * 
     * Công thức: Giá trung bình = Tổng giá vốn / Tổng số lượng
     * 
     * @param tonKhoDau
     * @param giaVonDau
     * @param luotNhap
     * @return Giá vốn trung bình
     */
    public static GiaVon calculateTrungBinh(SoLuong tonKhoDau, GiaVon giaVonDau,
                                           List<Map.Entry<SoLuong, GiaVon>> luotNhap) {
        if (tonKhoDau == null || giaVonDau == null || luotNhap == null) {
            throw new IllegalArgumentException("Các tham số không được null");
        }

        // Tính tổng giá trị
        Tien tongGiaVon = giaVonDau.timesQuantity(tonKhoDau);
        SoLuong tongSoLuong = tonKhoDau;

        for (Map.Entry<SoLuong, GiaVon> entry : luotNhap) {
            tongGiaVon = tongGiaVon.add(entry.getValue().timesQuantity(entry.getKey()));
            tongSoLuong = tongSoLuong.add(entry.getKey());
        }

        // Tính giá trung bình
        BigDecimal giaTrungBinh = tongGiaVon.getAmount()
                .divide(tongSoLuong.getValue(), BigDecimal.ROUND_HALF_UP);
        Tien giaTB = new Tien(giaTrungBinh, TienTe.vnd());

        return new GiaVon(giaTB, GiaVon.TRUNG_BINH);
    }

    // ============ Helper Class ============
    private static class LoDuocNhap {
        SoLuong soLuong;
        Tien gia;

        LoDuocNhap(SoLuong soLuong, Tien gia) {
            this.soLuong = soLuong;
            this.gia = gia;
        }
    }
}
