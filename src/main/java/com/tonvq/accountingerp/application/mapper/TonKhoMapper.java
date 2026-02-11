package com.tonvq.accountingerp.application.mapper;

import com.tonvq.accountingerp.application.dto.TonKhoCreateRequest;
import com.tonvq.accountingerp.application.dto.TonKhoResponse;
import com.tonvq.accountingerp.domain.model.TonKho;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for TonKho entity and DTOs
 * 
 * @author Ton VQ
 */
@Component
public class TonKhoMapper {

    /**
     * Convert TonKhoCreateRequest to TonKho domain entity
     */
    public TonKho toEntity(TonKhoCreateRequest dto) {
        if (dto == null) {
            return null;
        }

        TonKho entity = new TonKho();
        entity.setMaSanPham(dto.getMaSanPham());
        entity.setTenSanPham(dto.getTenSanPham());
        entity.setMaKho(dto.getMaKho());
        entity.setSoLuongDau(dto.getSoLuongDau());
        entity.setSoLuongNhap(java.math.BigDecimal.ZERO);
        entity.setSoLuongXuat(java.math.BigDecimal.ZERO);
        entity.setSoLuongCuoi(dto.getSoLuongDau());
        entity.setGiaVonDau(dto.getGiaBan());
        entity.setDonViTinh(dto.getDonViTinh());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedAt(LocalDateTime.now());

        return entity;
    }

    /**
     * Convert TonKho domain entity to TonKhoResponse DTO
     */
    public TonKhoResponse toResponse(TonKho entity) {
        if (entity == null) {
            return null;
        }

        return TonKhoResponse.builder()
                .id(entity.getId())
                .maSanPham(entity.getMaSanPham())
                .tenSanPham(entity.getTenSanPham())
                .maKho(entity.getMaKho())
                .soLuongDau(entity.getSoLuongDau())
                .soLuongNhap(entity.getSoLuongNhap())
                .soLuongXuat(entity.getSoLuongXuat())
                .soLuongCuoi(entity.getSoLuongCuoi())
                .giaVonDau(entity.getGiaVonDau())
                .giaVonNhap(entity.getGiaVonNhap())
                .giaVonXuat(entity.getGiaVonXuat())
                .giaVonCuoi(entity.getGiaVonCuoi())
                .donViTinh(entity.getDonViTinh())
                .phuongThucTinhGia(entity.getPhuongThucTinhGia())
                .build();
    }

    /**
     * Convert list of TonKho entities to TonKhoResponse DTOs
     */
    public List<TonKhoResponse> toResponseList(List<TonKho> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
