package com.tonvq.accountingerp.application.service;

import com.tonvq.accountingerp.application.dto.*;
import com.tonvq.accountingerp.application.exception.BusinessException;
import com.tonvq.accountingerp.domain.repository.TyGiaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Application Service for TyGia (Exchange Rate)
 * Per TT 99 - Điều 31: Tính chênh lệch tỷ giá
 * Accounts: TK 413/515/635
 * 
 * @author Ton VQ
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class TyGiaApplicationService {

    private final TyGiaRepository tyGiaRepository;

    /**
     * Tính chênh lệch tỷ giá (Calculate exchange rate difference)
     * Phương pháp: Ghi chênh lệch tỷ giá vào tài khoản 413, 515, hoặc 635
     */
    public TyGiaResponse calculateExchangeRateDifference(TyGiaCalculateRequest request) {
        Objects.requireNonNull(request, "TyGiaCalculateRequest cannot be null");

        log.info("Calculating exchange rate difference: {} to {}", 
                request.getTienTeCo(), request.getTienTeNo());

        // Validate
        if (request.getTienTeCo() == null || request.getTienTeCo().trim().isEmpty()) {
            throw new BusinessException("Tiền tệ có không được để trống", "INVALID_TIENTECO");
        }
        if (request.getTienTeNo() == null || request.getTienTeNo().trim().isEmpty()) {
            throw new BusinessException("Tiền tệ nợ không được để trống", "INVALID_TIENTENO");
        }
        if (request.getTienTeCo().equals(request.getTienTeNo())) {
            throw new BusinessException("Tiền tệ có và nợ phải khác nhau", "SAME_CURRENCIES");
        }
        if (request.getSoTienCo() == null || request.getSoTienCo().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Số tiền có phải lớn hơn 0", "INVALID_SOTIEN");
        }
        if (request.getNgayTinh() == null) {
            throw new BusinessException("Ngày tính không được để trống", "INVALID_NGAY");
        }

        // Get exchange rate (from external service or database)
        BigDecimal tyGia = getExchangeRate(request.getTienTeCo(), request.getTienTeNo(), request.getNgayTinh());

        // Calculate converted amount
        BigDecimal soTienNo = request.getSoTienCo().multiply(tyGia)
                .setScale(2, RoundingMode.HALF_UP);

        // Calculate difference (assume previous rate for comparison)
        BigDecimal previousRate = BigDecimal.valueOf(24000); // Example rate
        BigDecimal previousAmount = request.getSoTienCo().multiply(previousRate)
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal difference = soTienNo.subtract(previousAmount);

        // Determine account (413 for sales, 515 for purchases, 635 for other)
        String taiKhoanChenhLech = determineDifferenceAccount(request.getTienTeCo());

        TyGiaResponse response = TyGiaResponse.builder()
                .tienTeCo(request.getTienTeCo())
                .tienTeNo(request.getTienTeNo())
                .ngayTinh(request.getNgayTinh())
                .tyGiaTinh(tyGia)
                .soTienCo(request.getSoTienCo())
                .soTienNo(soTienNo)
                .chenhLechTyGia(difference)
                .taiKhoanChenhLech(taiKhoanChenhLech)
                .build();

        log.info("Exchange rate difference calculated: rate={}, difference={}", tyGia, difference);

        return response;
    }

    /**
     * Get exchange rate from external source or database
     * This would typically call an external API or database
     */
    private BigDecimal getExchangeRate(String fromCurrency, String toCurrency, java.time.LocalDate date) {
        // TODO: Implement external API call to get real exchange rate
        // For now, return a mock rate
        if ("USD".equals(fromCurrency) && "VND".equals(toCurrency)) {
            return BigDecimal.valueOf(24000);
        } else if ("EUR".equals(fromCurrency) && "VND".equals(toCurrency)) {
            return BigDecimal.valueOf(26000);
        } else {
            return BigDecimal.ONE;
        }
    }

    /**
     * Determine which account to record exchange rate difference
     * TK 413: Sales (Doanh thu bán hàng)
     * TK 515: Purchases (Chi phí mua hàng)
     * TK 635: Other income/expense
     */
    private String determineDifferenceAccount(String currency) {
        // Logic to determine account based on transaction type
        // For simplicity, default to 413 (sales)
        return "413";
    }
}
