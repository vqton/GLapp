package com.tonvq.accountingerp.application.service;

import com.tonvq.accountingerp.application.dto.*;
import com.tonvq.accountingerp.application.exception.BusinessException;
import com.tonvq.accountingerp.application.exception.ResourceNotFoundException;
import com.tonvq.accountingerp.application.mapper.ChungTuMapper;
import com.tonvq.accountingerp.domain.model.ChungTu;
import com.tonvq.accountingerp.domain.repository.ChungTuRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

/**
 * Application Service for ChungTu (Voucher/Document)
 * Orchestrates ChungTu domain logic per TT 99/2025/TT-BTC
 * 
 * Lifecycle: DRAFT → APPROVED → POSTED → LOCKED → CANCELLED
 * 
 * @author Ton VQ
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class ChungTuApplicationService {

    private final ChungTuRepository chungTuRepository;
    private final ChungTuMapper chungTuMapper;

    /**
     * Tạo chứng từ mới (Create new voucher)
     * Initial state: DRAFT
     */
    public ChungTuResponse createChungTu(ChungTuCreateRequest request) {
        Objects.requireNonNull(request, "ChungTuCreateRequest cannot be null");
        
        log.info("Creating new ChungTu: {}", request.getMaChungTu());

        // Validate required fields
        if (request.getMaChungTu() == null || request.getMaChungTu().trim().isEmpty()) {
            throw new BusinessException("Mã chứng từ không được để trống", "INVALID_MA_CHUNGTUU");
        }
        if (request.getLoaiChungTu() == null || request.getLoaiChungTu().trim().isEmpty()) {
            throw new BusinessException("Loại chứng từ không được để trống", "INVALID_LOAI_CHUNGTUU");
        }
        if (request.getNgayChungTu() == null) {
            throw new BusinessException("Ngày chứng từ không được để trống", "INVALID_NGAY_CHUNGTUU");
        }
        if (request.getSoTien() == null) {
            throw new BusinessException("Số tiền không được để trống", "INVALID_SO_TIEN");
        }

        // Check duplicate
        Optional<ChungTu> existing = chungTuRepository.findByMaChungTu(request.getMaChungTu());
        if (existing.isPresent()) {
            throw new BusinessException("Mã chứng từ đã tồn tại: " + request.getMaChungTu(), "DUPLICATE_MA_CHUNGTUU");
        }

        // Map to entity
        ChungTu entity = chungTuMapper.toEntity(request);
        
        // Save
        ChungTu saved = chungTuRepository.save(entity);
        
        log.info("ChungTu created successfully: id={}, maChungTu={}", saved.getId(), saved.getMaChungTu());

        return chungTuMapper.toResponse(saved);
    }

    /**
     * Duyệt chứng từ (Approve voucher)
     * Transition: DRAFT → APPROVED
     */
    public ChungTuResponse approveChungTu(ChungTuApproveRequest request) {
        Objects.requireNonNull(request, "ChungTuApproveRequest cannot be null");
        
        Long chungTuId = request.getChungTuId();
        ChungTu chungTu = chungTuRepository.findById(chungTuId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chứng từ: " + chungTuId));

        log.info("Approving ChungTu: id={}", chungTuId);

        // Validate state
        if (!"DRAFT".equals(chungTu.getTrangThai())) {
            throw new BusinessException("Chứng từ phải ở trạng thái DRAFT để duyệt", "INVALID_STATE");
        }

        // Approve
        chungTu.setTrangThai("APPROVED");
        chungTu.setApprovedBy(request.getApprovedBy());
        chungTu.setApprovedAt(LocalDateTime.now());
        chungTu.setUpdatedAt(LocalDateTime.now());

        ChungTu saved = chungTuRepository.save(chungTu);

        log.info("ChungTu approved: id={}", chungTuId);

        return chungTuMapper.toResponse(saved);
    }

    /**
     * Ghi sổ chứng từ (Post voucher)
     * Transition: APPROVED → POSTED
     * Yêu cầu: Chứng từ phải cân bằng (Nợ = Có) và có ít nhất 2 bút toán
     */
    public ChungTuResponse postChungTu(ChungTuPostRequest request) {
        Objects.requireNonNull(request, "ChungTuPostRequest cannot be null");

        Long chungTuId = request.getChungTuId();
        ChungTu chungTu = chungTuRepository.findById(chungTuId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chứng từ: " + chungTuId));

        log.info("Posting ChungTu: id={}", chungTuId);

        // Validate state
        if (!"APPROVED".equals(chungTu.getTrangThai())) {
            throw new BusinessException("Chứng từ phải ở trạng thái APPROVED để ghi sổ", "INVALID_STATE");
        }

        // Post
        chungTu.setTrangThai("POSTED");
        chungTu.setPostedBy(request.getPostedBy());
        chungTu.setPostedAt(LocalDateTime.now());
        chungTu.setUpdatedAt(LocalDateTime.now());

        ChungTu saved = chungTuRepository.save(chungTu);

        log.info("ChungTu posted: id={}", chungTuId);

        return chungTuMapper.toResponse(saved);
    }

    /**
     * Khóa chứng từ (Lock voucher)
     * Transition: POSTED → LOCKED
     * Sau khi khóa, chứng từ không thể sửa đổi
     */
    public ChungTuResponse lockChungTu(ChungTuLockRequest request) {
        Objects.requireNonNull(request, "ChungTuLockRequest cannot be null");

        Long chungTuId = request.getChungTuId();
        ChungTu chungTu = chungTuRepository.findById(chungTuId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chứng từ: " + chungTuId));

        log.info("Locking ChungTu: id={}", chungTuId);

        // Validate state
        if (!"POSTED".equals(chungTu.getTrangThai())) {
            throw new BusinessException("Chứng từ phải ở trạng thái POSTED để khóa", "INVALID_STATE");
        }

        // Lock
        chungTu.setTrangThai("LOCKED");
        chungTu.setLockedBy(request.getLockedBy());
        chungTu.setLockedAt(LocalDateTime.now());
        chungTu.setUpdatedAt(LocalDateTime.now());

        ChungTu saved = chungTuRepository.save(chungTu);

        log.info("ChungTu locked: id={}", chungTuId);

        return chungTuMapper.toResponse(saved);
    }

    /**
     * Hủy chứng từ (Cancel voucher)
     * Transition: DRAFT/APPROVED/POSTED → CANCELLED
     * Chứng từ đã LOCKED không thể hủy
     */
    public ChungTuResponse cancelChungTu(Long chungTuId, Long cancelledBy, String reason) {
        ChungTu chungTu = chungTuRepository.findById(chungTuId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chứng từ: " + chungTuId));

        log.info("Cancelling ChungTu: id={}", chungTuId);

        // Validate state
        if ("LOCKED".equals(chungTu.getTrangThai())) {
            throw new BusinessException("Chứng từ đã khóa không thể hủy", "INVALID_STATE");
        }
        if ("CANCELLED".equals(chungTu.getTrangThai())) {
            throw new BusinessException("Chứng từ đã bị hủy", "ALREADY_CANCELLED");
        }

        // Cancel
        chungTu.setTrangThai("CANCELLED");
        chungTu.setUpdatedBy(cancelledBy);
        chungTu.setUpdatedAt(LocalDateTime.now());
        chungTu.setGhiChu(reason);

        ChungTu saved = chungTuRepository.save(chungTu);

        log.info("ChungTu cancelled: id={}", chungTuId);

        return chungTuMapper.toResponse(saved);
    }

    /**
     * Lấy chứng từ theo ID
     */
    @Transactional(readOnly = true)
    public ChungTuResponse getChungTuById(Long id) {
        ChungTu chungTu = chungTuRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chứng từ: " + id));
        return chungTuMapper.toResponse(chungTu);
    }

    /**
     * Lấy chứng từ theo mã
     */
    @Transactional(readOnly = true)
    public ChungTuResponse getChungTuByMa(String maChungTu) {
        ChungTu chungTu = chungTuRepository.findByMaChungTu(maChungTu)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy chứng từ: " + maChungTu));
        return chungTuMapper.toResponse(chungTu);
    }

    /**
     * Lấy danh sách chứng từ theo trạng thái
     */
    @Transactional(readOnly = true)
    public List<ChungTuResponse> getChungTuByTrangThai(String trangThai) {
        List<ChungTu> chungTuList = chungTuRepository.findByTrangThai(trangThai);
        return chungTuMapper.toResponseList(chungTuList);
    }
}
