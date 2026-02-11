package com.tonvq.accountingerp.application.service;

import com.tonvq.accountingerp.application.dto.*;
import com.tonvq.accountingerp.application.exception.BusinessException;
import com.tonvq.accountingerp.application.exception.ResourceNotFoundException;
import com.tonvq.accountingerp.domain.repository.DuPhongNoRepository;
import com.tonvq.accountingerp.domain.repository.KhachHangRepository;
import com.tonvq.accountingerp.domain.service.DuPhongNoService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Application Service for DuPhongNo (Allowance for Doubtful Debts)
 * Per TT 48/2019 - Điều 32 (Article 32)
 * TK 229 - Dự phòng nợ phải thu (TK 131 or 141)
 * 
 * Methods: LICH_SU (By history %), TUOI_NO (By aging), CU_THE (Specific %)
 * 
 * @author Ton VQ
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class DuPhongNoApplicationService {

    private final DuPhongNoRepository duPhongNoRepository;
    private final KhachHangRepository khachHangRepository;
    private final DuPhongNoService duPhongNoService;

    /**
     * Tính dự phòng nợ theo lịch sử (Calculate by history %)
     * Phương pháp: Áp dụng tỷ lệ % lịch sử lỗ thế kỳ trước
     */
    public DuPhongNoResponse calculateDuPhongByHistory(DuPhongNoCalculateRequest request) {
        Objects.requireNonNull(request, "DuPhongNoCalculateRequest cannot be null");

        Long khachHangId = request.getKhachHangId();
        
        log.info("Calculating DuPhongNo by history: khachHangId={}, rate={}", khachHangId, request.getTyLe());

        // Validate
        if (khachHangId == null || khachHangId <= 0) {
            throw new BusinessException("Mã khách hàng không hợp lệ", "INVALID_MA_KHACHHANG");
        }
        if (request.getTyLe() == null || request.getTyLe().compareTo(BigDecimal.ZERO) < 0 
                || request.getTyLe().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException("Tỷ lệ dự phòng phải từ 0-100%", "INVALID_TYLE");
        }

        // Get customer outstanding amount
        BigDecimal tienTrongNo = khachHangRepository.findById(khachHangId)
                .map(kh -> kh.getTienNo())
                .orElse(BigDecimal.ZERO);

        // Calculate allowance
        BigDecimal duPhongAmount = duPhongNoService.calculateDuPhongLichSu(tienTrongNo, request.getTyLe());

        // Build response
        DuPhongNoResponse response = DuPhongNoResponse.builder()
                .khachHangId(khachHangId)
                .phuongPhapTinh("LICH_SU")
                .tienTrongNo(tienTrongNo)
                .tyLe(request.getTyLe())
                .soTienDuPhong(duPhongAmount)
                .asOfDate(request.getAsOfDate())
                .trangThai("CALCULATED")
                .build();

        log.info("DuPhongNo calculated by history: amount={}", duPhongAmount);

        return response;
    }

    /**
     * Tính dự phòng nợ theo tuổi nợ (Calculate by aging)
     * Phương pháp: Áp dụng tỷ lệ % theo nhóm tuổi nợ
     * 1-30 ngày: 1%, 31-60 ngày: 5%, 61-90 ngày: 10%, >90 ngày: 50%
     */
    public DuPhongNoResponse calculateDuPhongByAging(DuPhongNoCalculateRequest request) {
        Objects.requireNonNull(request, "DuPhongNoCalculateRequest cannot be null");

        Long khachHangId = request.getKhachHangId();

        log.info("Calculating DuPhongNo by aging: khachHangId={}", khachHangId);

        // Validate
        if (khachHangId == null || khachHangId <= 0) {
            throw new BusinessException("Mã khách hàng không hợp lệ", "INVALID_MA_KHACHHANG");
        }

        // Get customer outstanding amount
        BigDecimal tienTrongNo = khachHangRepository.findById(khachHangId)
                .map(kh -> kh.getTienNo())
                .orElse(BigDecimal.ZERO);

        // Calculate allowance by aging
        BigDecimal duPhongAmount = duPhongNoService.calculateDuPhongTuoiNo(tienTrongNo, request.getAsOfDate());

        // Build response
        DuPhongNoResponse response = DuPhongNoResponse.builder()
                .khachHangId(khachHangId)
                .phuongPhapTinh("TUOI_NO")
                .tienTrongNo(tienTrongNo)
                .soTienDuPhong(duPhongAmount)
                .asOfDate(request.getAsOfDate())
                .trangThai("CALCULATED")
                .build();

        log.info("DuPhongNo calculated by aging: amount={}", duPhongAmount);

        return response;
    }

    /**
     * Tính dự phòng nợ cụ thể (Calculate by specific %)
     * Phương pháp: Áp dụng tỷ lệ % cụ thể cho khách hàng có rủi ro cao
     */
    public DuPhongNoResponse calculateDuPhongBySpecific(DuPhongNoCalculateRequest request) {
        Objects.requireNonNull(request, "DuPhongNoCalculateRequest cannot be null");

        Long khachHangId = request.getKhachHangId();

        log.info("Calculating DuPhongNo by specific rate: khachHangId={}, rate={}", khachHangId, request.getTyLe());

        // Validate
        if (khachHangId == null || khachHangId <= 0) {
            throw new BusinessException("Mã khách hàng không hợp lệ", "INVALID_MA_KHACHHANG");
        }
        if (request.getTyLe() == null || request.getTyLe().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Tỷ lệ dự phòng không hợp lệ", "INVALID_TYLE");
        }

        // Get customer outstanding amount
        BigDecimal tienTrongNo = khachHangRepository.findById(khachHangId)
                .map(kh -> kh.getTienNo())
                .orElse(BigDecimal.ZERO);

        // Calculate allowance
        BigDecimal duPhongAmount = duPhongNoService.calculateDuPhongCuThe(tienTrongNo, request.getTyLe());

        // Build response
        DuPhongNoResponse response = DuPhongNoResponse.builder()
                .khachHangId(khachHangId)
                .phuongPhapTinh("CU_THE")
                .tienTrongNo(tienTrongNo)
                .tyLe(request.getTyLe())
                .soTienDuPhong(duPhongAmount)
                .asOfDate(request.getAsOfDate())
                .trangThai("CALCULATED")
                .build();

        log.info("DuPhongNo calculated by specific rate: amount={}", duPhongAmount);

        return response;
    }

    /**
     * Điều chỉnh dự phòng (Adjust allowance)
     * So sánh dự phòng hiện có với dự phòng cần thiết
     */
    public DuPhongNoResponse adjustAllowance(Long khachHangId, BigDecimal newAmount) {
        log.info("Adjusting DuPhongNo: khachHangId={}, newAmount={}", khachHangId, newAmount);

        // Get current allowance
        BigDecimal currentAllowance = BigDecimal.ZERO;

        // Calculate adjustment
        BigDecimal adjustment = newAmount.subtract(currentAllowance);

        // Build response
        DuPhongNoResponse response = DuPhongNoResponse.builder()
                .khachHangId(khachHangId)
                .soTienDuPhong(newAmount)
                .duPhongHienCo(currentAllowance)
                .duPhongCanDieuChinh(adjustment)
                .trangThai("ADJUSTED")
                .build();

        log.info("DuPhongNo adjusted: adjustment={}", adjustment);

        return response;
    }
}
