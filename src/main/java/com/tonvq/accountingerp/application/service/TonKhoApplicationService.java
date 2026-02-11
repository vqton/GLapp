package com.tonvq.accountingerp.application.service;

import com.tonvq.accountingerp.application.dto.*;
import com.tonvq.accountingerp.application.exception.BusinessException;
import com.tonvq.accountingerp.application.exception.ResourceNotFoundException;
import com.tonvq.accountingerp.application.mapper.TonKhoMapper;
import com.tonvq.accountingerp.domain.model.TonKho;
import com.tonvq.accountingerp.domain.repository.TonKhoRepository;
import com.tonvq.accountingerp.domain.service.GiaVonService;
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
 * Application Service for TonKho (Inventory)
 * Manages inventory import/export and cost calculation
 * 
 * Per TT 99: Support FIFO, LIFO, and average cost methods
 * 
 * @author Ton VQ
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class TonKhoApplicationService {

    private final TonKhoRepository tonKhoRepository;
    private final TonKhoMapper tonKhoMapper;
    private final GiaVonService giaVonService;

    /**
     * Tạo tồn kho mới
     */
    public TonKhoResponse createTonKho(TonKhoCreateRequest request) {
        Objects.requireNonNull(request, "TonKhoCreateRequest cannot be null");

        log.info("Creating new TonKho: {}", request.getMaSanPham());

        // Validate
        if (request.getMaSanPham() == null || request.getMaSanPham().trim().isEmpty()) {
            throw new BusinessException("Mã sản phẩm không được để trống", "INVALID_MA_SANPHAM");
        }
        if (request.getTenSanPham() == null || request.getTenSanPham().trim().isEmpty()) {
            throw new BusinessException("Tên sản phẩm không được để trống", "INVALID_TEN_SANPHAM");
        }
        if (request.getSoLuongDau() == null || request.getSoLuongDau().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Số lượng không hợp lệ", "INVALID_SOLUONG");
        }

        // Check duplicate
        Optional<TonKho> existing = tonKhoRepository.findByMaSanPham(request.getMaSanPham());
        if (existing.isPresent()) {
            throw new BusinessException("Sản phẩm đã tồn tại: " + request.getMaSanPham(), "DUPLICATE_SANPHAM");
        }

        TonKho entity = tonKhoMapper.toEntity(request);
        TonKho saved = tonKhoRepository.save(entity);

        log.info("TonKho created: id={}, maSanPham={}", saved.getId(), saved.getMaSanPham());

        return tonKhoMapper.toResponse(saved);
    }

    /**
     * Nhập hàng (Import stock)
     * Tăng số lượng nhập và cập nhật giá vốn
     */
    public TonKhoResponse importStock(NhapHangRequest request) {
        Objects.requireNonNull(request, "NhapHangRequest cannot be null");

        Long tonKhoId = request.getTonKhoId();
        TonKho tonKho = tonKhoRepository.findById(tonKhoId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm: " + tonKhoId));

        log.info("Importing stock: tonKhoId={}, quantity={}", tonKhoId, request.getSoLuongNhap());

        // Validate
        if (request.getSoLuongNhap() == null || request.getSoLuongNhap().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Số lượng nhập phải lớn hơn 0", "INVALID_SOLUONG_NHAP");
        }
        if (request.getGiaVonNhap() == null || request.getGiaVonNhap().compareTo(BigDecimal.ZERO) < 0) {
            throw new BusinessException("Giá vốn nhập không hợp lệ", "INVALID_GIAVON_NHAP");
        }

        // Update inventory
        tonKho.setSoLuongNhap(tonKho.getSoLuongNhap().add(request.getSoLuongNhap()));
        tonKho.setSoLuongCuoi(tonKho.getSoLuongDau()
                .add(tonKho.getSoLuongNhap())
                .subtract(tonKho.getSoLuongXuat()));
        tonKho.setGiaVonNhap(request.getGiaVonNhap());
        tonKho.setUpdatedBy(request.getImportedBy());
        tonKho.setUpdatedAt(LocalDateTime.now());

        TonKho saved = tonKhoRepository.save(tonKho);

        log.info("Stock imported: tonKhoId={}, totalQuantity={}", tonKhoId, tonKho.getSoLuongCuoi());

        return tonKhoMapper.toResponse(saved);
    }

    /**
     * Xuất hàng (Export stock)
     * Giảm số lượng xuất dựa trên phương thức tính giá vốn
     */
    public TonKhoResponse exportStock(XuatHangRequest request) {
        Objects.requireNonNull(request, "XuatHangRequest cannot be null");

        Long tonKhoId = request.getTonKhoId();
        TonKho tonKho = tonKhoRepository.findById(tonKhoId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm: " + tonKhoId));

        log.info("Exporting stock: tonKhoId={}, quantity={}, method={}", 
                tonKhoId, request.getSoLuongXuat(), request.getPhuongThucTinhGia());

        // Validate
        if (request.getSoLuongXuat() == null || request.getSoLuongXuat().compareTo(BigDecimal.ZERO) <= 0) {
            throw new BusinessException("Số lượng xuất phải lớn hơn 0", "INVALID_SOLUONG_XUAT");
        }
        if (request.getSoLuongXuat().compareTo(tonKho.getSoLuongCuoi()) > 0) {
            throw new BusinessException("Số lượng xuất vượt quá tồn kho hiện tại", "INSUFFICIENT_STOCK");
        }

        // Update inventory
        tonKho.setSoLuongXuat(tonKho.getSoLuongXuat().add(request.getSoLuongXuat()));
        tonKho.setSoLuongCuoi(tonKho.getSoLuongDau()
                .add(tonKho.getSoLuongNhap())
                .subtract(tonKho.getSoLuongXuat()));
        tonKho.setPhuongThucTinhGia(request.getPhuongThucTinhGia());
        tonKho.setUpdatedBy(request.getExportedBy());
        tonKho.setUpdatedAt(LocalDateTime.now());

        TonKho saved = tonKhoRepository.save(tonKho);

        log.info("Stock exported: tonKhoId={}, remainingQuantity={}", tonKhoId, tonKho.getSoLuongCuoi());

        return tonKhoMapper.toResponse(saved);
    }

    /**
     * Tính giá vốn (Calculate cost)
     * Hỗ trợ phương thức FIFO, LIFO, TRUNG_BINH
     */
    public TonKhoResponse calculateCost(TinhGiaVonRequest request) {
        Objects.requireNonNull(request, "TinhGiaVonRequest cannot be null");

        Long tonKhoId = request.getTonKhoId();
        TonKho tonKho = tonKhoRepository.findById(tonKhoId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm: " + tonKhoId));

        log.info("Calculating cost: tonKhoId={}, method={}", tonKhoId, request.getPhuongThucTinhGia());

        String method = request.getPhuongThucTinhGia();

        // Validate method
        if (!("FIFO".equals(method) || "LIFO".equals(method) || "TRUNG_BINH".equals(method))) {
            throw new BusinessException("Phương thức tính giá không hợp lệ", "INVALID_PRICING_METHOD");
        }

        // Calculate cost based on method
        BigDecimal giaVonXuat = switch (method) {
            case "FIFO" -> giaVonService.calculateFIFO(tonKho.getGiaVonDau(), tonKho.getGiaVonNhap());
            case "LIFO" -> giaVonService.calculateLIFO(tonKho.getGiaVonDau(), tonKho.getGiaVonNhap());
            case "TRUNG_BINH" -> giaVonService.calculateTrungBinh(tonKho.getGiaVonDau(), tonKho.getGiaVonNhap(),
                    tonKho.getSoLuongDau(), tonKho.getSoLuongNhap());
            default -> throw new BusinessException("Phương thức không được hỗ trợ", "UNSUPPORTED_METHOD");
        };

        // Update cost
        tonKho.setGiaVonXuat(giaVonXuat);
        tonKho.setGiaVonCuoi(giaVonXuat);
        tonKho.setPhuongThucTinhGia(method);
        tonKho.setUpdatedBy(request.getCalculatedBy());
        tonKho.setUpdatedAt(LocalDateTime.now());

        TonKho saved = tonKhoRepository.save(tonKho);

        log.info("Cost calculated: tonKhoId={}, costPrice={}", tonKhoId, giaVonXuat);

        return tonKhoMapper.toResponse(saved);
    }

    /**
     * Lấy tồn kho theo mã sản phẩm
     */
    @Transactional(readOnly = true)
    public TonKhoResponse getTonKhoByMaSanPham(String maSanPham) {
        TonKho tonKho = tonKhoRepository.findByMaSanPham(maSanPham)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm: " + maSanPham));
        return tonKhoMapper.toResponse(tonKho);
    }

    /**
     * Lấy danh sách sản phẩm hết hàng
     */
    @Transactional(readOnly = true)
    public List<TonKhoResponse> getOutOfStockProducts() {
        List<TonKho> products = tonKhoRepository.findOutOfStockProducts();
        return tonKhoMapper.toResponseList(products);
    }
}
