package com.tonvq.accountingerp.application.service;

import com.tonvq.accountingerp.application.dto.*;
import com.tonvq.accountingerp.application.exception.BusinessException;
import com.tonvq.accountingerp.application.exception.ResourceNotFoundException;
import com.tonvq.accountingerp.application.mapper.HopDongDichVuMapper;
import com.tonvq.accountingerp.domain.model.HopDongDichVu;
import com.tonvq.accountingerp.domain.repository.HopDongDichVuRepository;
import com.tonvq.accountingerp.domain.service.DoanhThuDichVuService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Application Service for HopDongDichVu (Service Contract)
 * Per VAS 14/15 - Revenue recognition for service contracts
 * 
 * Lifecycle: DRAFT → ACTIVE → IN_PROGRESS → COMPLETED
 * 
 * @author Ton VQ
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class HopDongDichVuApplicationService {

    private final HopDongDichVuRepository hopDongDichVuRepository;
    private final HopDongDichVuMapper hopDongDichVuMapper;
    private final DoanhThuDichVuService doanhThuDichVuService;

    /**
     * Tạo hợp đồng dịch vụ mới
     */
    public HopDongDichVuResponse createHopDong(HopDongDichVuCreateRequest request) {
        Objects.requireNonNull(request, "HopDongDichVuCreateRequest cannot be null");

        log.info("Creating new HopDongDichVu: {}", request.getMaHopDong());

        // Validate
        if (request.getMaHopDong() == null || request.getMaHopDong().trim().isEmpty()) {
            throw new BusinessException("Mã hợp đồng không được để trống", "INVALID_MA_HOPDONG");
        }
        if (request.getKhachHangId() == null || request.getKhachHangId() <= 0) {
            throw new BusinessException("Mã khách hàng không hợp lệ", "INVALID_MA_KHACHHANG");
        }
        if (request.getGiaHopDong() == null || request.getGiaHopDong().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Giá hợp đồng phải lớn hơn 0", "INVALID_GIA_HOPDONG");
        }
        if (request.getNgayBatDau() == null || request.getNgayKetThuc() == null) {
            throw new BusinessException("Ngày bắt đầu/kết thúc không được để trống", "INVALID_NGAY");
        }
        if (request.getNgayBatDau().isAfter(request.getNgayKetThuc())) {
            throw new BusinessException("Ngày bắt đầu phải trước ngày kết thúc", "INVALID_NGAY_RANGE");
        }

        // Check duplicate
        Optional<HopDongDichVu> existing = hopDongDichVuRepository.findByMaHopDong(request.getMaHopDong());
        if (existing.isPresent()) {
            throw new BusinessException("Mã hợp đồng đã tồn tại: " + request.getMaHopDong(), "DUPLICATE_HOPDONG");
        }

        HopDongDichVu entity = hopDongDichVuMapper.toEntity(request);
        HopDongDichVu saved = hopDongDichVuRepository.save(entity);

        log.info("HopDongDichVu created: id={}, maHopDong={}", saved.getId(), saved.getMaHopDong());

        return hopDongDichVuMapper.toResponse(saved);
    }

    /**
     * Kích hoạt hợp đồng (Activate)
     * Transition: DRAFT → ACTIVE
     */
    public HopDongDichVuResponse activateHopDong(Long hopDongId) {
        HopDongDichVu hopDong = hopDongDichVuRepository.findById(hopDongId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hợp đồng: " + hopDongId));

        log.info("Activating HopDongDichVu: id={}", hopDongId);

        // Validate state
        if (!"DRAFT".equals(hopDong.getTrangThai())) {
            throw new BusinessException("Hợp đồng phải ở trạng thái DRAFT để kích hoạt", "INVALID_STATE");
        }

        hopDong.setTrangThai("ACTIVE");
        HopDongDichVu saved = hopDongDichVuRepository.save(hopDong);

        log.info("HopDongDichVu activated: id={}", hopDongId);

        return hopDongDichVuMapper.toResponse(saved);
    }

    /**
     * Cập nhật tiến độ (Update progress)
     * Transition: ACTIVE → IN_PROGRESS
     */
    public HopDongDichVuResponse updateProgress(HopDongDichVuProgressRequest request) {
        Objects.requireNonNull(request, "HopDongDichVuProgressRequest cannot be null");

        Long hopDongId = request.getHopDongId();
        HopDongDichVu hopDong = hopDongDichVuRepository.findById(hopDongId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hợp đồng: " + hopDongId));

        log.info("Updating progress: hopDongId={}, percentComplete={}", hopDongId, request.getPercentComplete());

        // Validate state
        if (!"ACTIVE".equals(hopDong.getTrangThai()) && !"IN_PROGRESS".equals(hopDong.getTrangThai())) {
            throw new BusinessException("Hợp đồng phải ở trạng thái ACTIVE hoặc IN_PROGRESS", "INVALID_STATE");
        }

        // Validate progress
        if (request.getPercentComplete() == null || 
                request.getPercentComplete().compareTo(BigDecimal.ZERO) < 0 ||
                request.getPercentComplete().compareTo(BigDecimal.valueOf(100)) > 0) {
            throw new BusinessException("Phần trăm hoàn thành phải từ 0-100%", "INVALID_PERCENT");
        }

        // Update progress
        hopDong.setPercentComplete(request.getPercentComplete());
        if (request.getPercentComplete().compareTo(BigDecimal.ZERO) > 0) {
            hopDong.setTrangThai("IN_PROGRESS");
        }

        HopDongDichVu saved = hopDongDichVuRepository.save(hopDong);

        log.info("HopDongDichVu progress updated: id={}, percentComplete={}", hopDongId, request.getPercentComplete());

        return hopDongDichVuMapper.toResponse(saved);
    }

    /**
     * Công nhận doanh thu (Recognize revenue)
     * Per VAS 14/15 using Cost-to-Cost method or Milestone method
     */
    public HopDongDichVuResponse recognizeRevenue(Long hopDongId) {
        HopDongDichVu hopDong = hopDongDichVuRepository.findById(hopDongId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hợp đồng: " + hopDongId));

        log.info("Recognizing revenue: hopDongId={}", hopDongId);

        // Calculate revenue based on method
        BigDecimal doanhThuCongNhan;

        if ("MILESTONE".equals(hopDong.getPhuongPhapCongNhan())) {
            doanhThuCongNhan = doanhThuDichVuService.calculateDoanhThuMilestone(
                    hopDong.getGiaHopDong(), 
                    hopDong.getMilestoneHoanThanh(), 
                    hopDong.getSoMilestone());
        } else {
            // % Completion method
            doanhThuCongNhan = doanhThuDichVuService.calculateDoanhThuCongNhanDan(
                    hopDong.getGiaHopDong(), 
                    hopDong.getPercentComplete());
        }

        hopDong.setDoanhThuCongNhan(doanhThuCongNhan);

        HopDongDichVu saved = hopDongDichVuRepository.save(hopDong);

        log.info("Revenue recognized: hopDongId={}, amount={}", hopDongId, doanhThuCongNhan);

        return hopDongDichVuMapper.toResponse(saved);
    }

    /**
     * Hoàn thành hợp đồng (Complete)
     * Transition: IN_PROGRESS → COMPLETED
     */
    public HopDongDichVuResponse completeHopDong(Long hopDongId) {
        HopDongDichVu hopDong = hopDongDichVuRepository.findById(hopDongId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hợp đồng: " + hopDongId));

        log.info("Completing HopDongDichVu: id={}", hopDongId);

        // Validate state
        if (!"IN_PROGRESS".equals(hopDong.getTrangThai())) {
            throw new BusinessException("Hợp đồng phải ở trạng thái IN_PROGRESS để hoàn thành", "INVALID_STATE");
        }

        hopDong.setTrangThai("COMPLETED");
        hopDong.setPercentComplete(BigDecimal.valueOf(100));

        HopDongDichVu saved = hopDongDichVuRepository.save(hopDong);

        log.info("HopDongDichVu completed: id={}", hopDongId);

        return hopDongDichVuMapper.toResponse(saved);
    }

    /**
     * Lấy hợp đồng theo ID
     */
    @Transactional(readOnly = true)
    public HopDongDichVuResponse getHopDongById(Long id) {
        HopDongDichVu hopDong = hopDongDichVuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy hợp đồng: " + id));
        return hopDongDichVuMapper.toResponse(hopDong);
    }

    /**
     * Lấy danh sách hợp đồng theo trạng thái
     */
    @Transactional(readOnly = true)
    public List<HopDongDichVuResponse> getHopDongByTrangThai(String trangThai) {
        List<HopDongDichVu> hopDongList = hopDongDichVuRepository.findByTrangThai(trangThai);
        return hopDongDichVuMapper.toResponseList(hopDongList);
    }
}
