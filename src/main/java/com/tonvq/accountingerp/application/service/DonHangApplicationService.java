package com.tonvq.accountingerp.application.service;

import com.tonvq.accountingerp.application.dto.*;
import com.tonvq.accountingerp.application.exception.BusinessException;
import com.tonvq.accountingerp.application.exception.ResourceNotFoundException;
import com.tonvq.accountingerp.application.mapper.DonHangMapper;
import com.tonvq.accountingerp.domain.model.DonHang;
import com.tonvq.accountingerp.domain.repository.DonHangRepository;
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
 * Application Service for DonHang (Purchase/Sales Order)
 * Orchestrates order lifecycle per commerce requirements
 * 
 * Lifecycle: DRAFT → CONFIRMED → SHIPPING → DELIVERED → PAID
 * 
 * @author Ton VQ
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class DonHangApplicationService {

    private final DonHangRepository donHangRepository;
    private final DonHangMapper donHangMapper;

    /**
     * Tạo đơn hàng mới (Create new order)
     */
    public DonHangResponse createDonHang(DonHangCreateRequest request) {
        Objects.requireNonNull(request, "DonHangCreateRequest cannot be null");

        log.info("Creating new DonHang: {}", request.getMaDonHang());

        // Validate required fields
        if (request.getMaDonHang() == null || request.getMaDonHang().trim().isEmpty()) {
            throw new BusinessException("Mã đơn hàng không được để trống", "INVALID_MA_DONHANG");
        }
        if (request.getLoaiDonHang() == null || request.getLoaiDonHang().trim().isEmpty()) {
            throw new BusinessException("Loại đơn hàng không được để trống", "INVALID_LOAI_DONHANG");
        }
        if (request.getNgayDonHang() == null) {
            throw new BusinessException("Ngày đơn hàng không được để trống", "INVALID_NGAY_DONHANG");
        }
        if (request.getMaKhachHang() == null || request.getMaKhachHang() <= 0) {
            throw new BusinessException("Mã khách hàng không hợp lệ", "INVALID_MA_KHACHHANG");
        }
        if (request.getTongTien() == null || request.getTongTien().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Tổng tiền phải lớn hơn 0", "INVALID_TONGTIEN");
        }

        // Check duplicate
        Optional<DonHang> existing = donHangRepository.findByMaDonHang(request.getMaDonHang());
        if (existing.isPresent()) {
            throw new BusinessException("Mã đơn hàng đã tồn tại: " + request.getMaDonHang(), "DUPLICATE_MA_DONHANG");
        }

        // Map to entity
        DonHang entity = donHangMapper.toEntity(request);

        // Save
        DonHang saved = donHangRepository.save(entity);

        log.info("DonHang created successfully: id={}, maDonHang={}", saved.getId(), saved.getMaDonHang());

        return donHangMapper.toResponse(saved);
    }

    /**
     * Xác nhận đơn hàng (Confirm order)
     * Transition: DRAFT → CONFIRMED
     */
    public DonHangResponse confirmDonHang(DonHangConfirmRequest request) {
        Objects.requireNonNull(request, "DonHangConfirmRequest cannot be null");

        Long donHangId = request.getDonHangId();
        DonHang donHang = donHangRepository.findById(donHangId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng: " + donHangId));

        log.info("Confirming DonHang: id={}", donHangId);

        // Validate state
        if (!"DRAFT".equals(donHang.getTrangThai())) {
            throw new BusinessException("Đơn hàng phải ở trạng thái DRAFT để xác nhận", "INVALID_STATE");
        }

        // Confirm
        donHang.setTrangThai("CONFIRMED");
        donHang.setConfirmedBy(request.getConfirmedBy());
        donHang.setConfirmedAt(LocalDateTime.now());

        DonHang saved = donHangRepository.save(donHang);

        log.info("DonHang confirmed: id={}", donHangId);

        return donHangMapper.toResponse(saved);
    }

    /**
     * Thực hiện giao hàng (Perform shipment)
     * Transition: CONFIRMED → SHIPPING → DELIVERED
     */
    public DonHangResponse shipDonHang(DonHangShipRequest request) {
        Objects.requireNonNull(request, "DonHangShipRequest cannot be null");

        Long donHangId = request.getDonHangId();
        DonHang donHang = donHangRepository.findById(donHangId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng: " + donHangId));

        log.info("Shipping DonHang: id={}", donHangId);

        // Validate state
        if (!"CONFIRMED".equals(donHang.getTrangThai()) && !"SHIPPING".equals(donHang.getTrangThai())) {
            throw new BusinessException("Đơn hàng phải ở trạng thái CONFIRMED hoặc SHIPPING để giao", "INVALID_STATE");
        }

        // Ship/Deliver
        donHang.setTrangThai("DELIVERED");
        donHang.setDeliveredBy(request.getShippedBy());
        donHang.setDeliveredAt(LocalDateTime.now());

        DonHang saved = donHangRepository.save(donHang);

        log.info("DonHang delivered: id={}", donHangId);

        return donHangMapper.toResponse(saved);
    }

    /**
     * Ghi nhận thanh toán (Record payment)
     * Reduces tienConNo (Outstanding amount)
     * Transition to PAID when fully paid
     */
    public DonHangResponse recordPayment(DonHangPaymentRequest request) {
        Objects.requireNonNull(request, "DonHangPaymentRequest cannot be null");

        Long donHangId = request.getDonHangId();
        DonHang donHang = donHangRepository.findById(donHangId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng: " + donHangId));

        log.info("Recording payment for DonHang: id={}, amount={}", donHangId, request.getPaymentAmount());

        // Validate state
        if ("DRAFT".equals(donHang.getTrangThai())) {
            throw new BusinessException("Đơn hàng ở trạng thái DRAFT không thể thanh toán", "INVALID_STATE");
        }
        if ("CANCELLED".equals(donHang.getTrangThai())) {
            throw new BusinessException("Đơn hàng đã bị hủy", "INVALID_STATE");
        }

        // Validate payment amount
        if (request.getPaymentAmount() == null || request.getPaymentAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Số tiền thanh toán phải lớn hơn 0", "INVALID_PAYMENT_AMOUNT");
        }
        if (request.getPaymentAmount().compareTo(donHang.getTienConNo()) > 0) {
            throw new BusinessException("Số tiền thanh toán vượt quá số tiền còn nợ", "OVERPAYMENT");
        }

        // Update payment info
        donHang.setTienDaThanhToan(donHang.getTienDaThanhToan().add(request.getPaymentAmount()));
        donHang.setTienConNo(donHang.getTienConNo().subtract(request.getPaymentAmount()));

        // Check if fully paid
        if (donHang.getTienConNo().compareTo(BigDecimal.ZERO) == 0) {
            donHang.setTrangThai("PAID");
            log.info("DonHang fully paid: id={}", donHangId);
        }

        DonHang saved = donHangRepository.save(donHang);

        log.info("Payment recorded for DonHang: id={}", donHangId);

        return donHangMapper.toResponse(saved);
    }

    /**
     * Tính toán VAT cho đơn hàng
     * Cập nhật tienVAT dựa trên tỷ lệ
     */
    public DonHangResponse calculateVAT(Long donHangId, BigDecimal vatRate) {
        DonHang donHang = donHangRepository.findById(donHangId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng: " + donHangId));

        log.info("Calculating VAT for DonHang: id={}, rate={}", donHangId, vatRate);

        // Validate state
        if (!"DRAFT".equals(donHang.getTrangThai())) {
            throw new BusinessException("Chỉ có thể tính VAT cho đơn hàng ở trạng thái DRAFT", "INVALID_STATE");
        }

        // Calculate VAT
        BigDecimal totalBeforeTax = donHang.getTongTien().subtract(donHang.getTienVAT());
        BigDecimal vatAmount = totalBeforeTax.multiply(vatRate).divide(BigDecimal.valueOf(100));

        donHang.setTienVAT(vatAmount);
        donHang.setTongTien(totalBeforeTax.add(vatAmount));

        DonHang saved = donHangRepository.save(donHang);

        log.info("VAT calculated for DonHang: id={}, vatAmount={}", donHangId, vatAmount);

        return donHangMapper.toResponse(saved);
    }

    /**
     * Lấy đơn hàng theo ID
     */
    @Transactional(readOnly = true)
    public DonHangResponse getDonHangById(Long id) {
        DonHang donHang = donHangRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng: " + id));
        return donHangMapper.toResponse(donHang);
    }

    /**
     * Lấy danh sách đơn hàng theo trạng thái
     */
    @Transactional(readOnly = true)
    public List<DonHangResponse> getDonHangByTrangThai(String trangThai) {
        List<DonHang> donHangList = donHangRepository.findByTrangThai(trangThai);
        return donHangMapper.toResponseList(donHangList);
    }

    /**
     * Lấy danh sách đơn hàng chưa thanh toán
     */
    @Transactional(readOnly = true)
    public List<DonHangResponse> getUnpaidDonHang() {
        List<DonHang> donHangList = donHangRepository.findUnpaidOrders();
        return donHangMapper.toResponseList(donHangList);
    }
}
