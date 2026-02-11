package com.tonvq.accountingerp.application.mapper;

import com.tonvq.accountingerp.application.dto.ChungTuCreateRequest;
import com.tonvq.accountingerp.application.dto.ChungTuResponse;
import com.tonvq.accountingerp.domain.model.ChungTu;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for ChungTu entity and DTOs
 * Converts between domain model and DTOs
 * 
 * @author Ton VQ
 */
@Component
public class ChungTuMapper {

    /**
     * Convert ChungTuCreateRequest to ChungTu domain entity
     */
    public ChungTu toEntity(ChungTuCreateRequest dto) {
        if (dto == null) {
            return null;
        }

        ChungTu entity = new ChungTu();
        entity.setMaChungTu(dto.getMaChungTu());
        entity.setLoaiChungTu(dto.getLoaiChungTu());
        entity.setNgayChungTu(dto.getNgayChungTu());
        entity.setNdChungTu(dto.getNdChungTu());
        entity.setSoTien(dto.getSoTien());
        entity.setDonViTinh(dto.getDonViTinh() != null ? dto.getDonViTinh() : "VND");
        entity.setNphatHanhId(dto.getNphatHanhId());
        entity.setNThuHuongId(dto.getNThuHuongId());
        entity.setGhiChu(dto.getGhiChu());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setTrangThai("DRAFT");

        return entity;
    }

    /**
     * Convert ChungTu domain entity to ChungTuResponse DTO
     */
    public ChungTuResponse toResponse(ChungTu entity) {
        if (entity == null) {
            return null;
        }

        return ChungTuResponse.builder()
                .id(entity.getId())
                .maChungTu(entity.getMaChungTu())
                .loaiChungTu(entity.getLoaiChungTu())
                .ngayChungTu(entity.getNgayChungTu())
                .ndChungTu(entity.getNdChungTu())
                .soTien(entity.getSoTien())
                .donViTinh(entity.getDonViTinh())
                .nphatHanhId(entity.getNphatHanhId())
                .nThuHuongId(entity.getNThuHuongId())
                .trangThai(entity.getTrangThai())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .ghiChu(entity.getGhiChu())
                .build();
    }

    /**
     * Convert list of ChungTu entities to ChungTuResponse DTOs
     */
    public List<ChungTuResponse> toResponseList(List<ChungTu> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
