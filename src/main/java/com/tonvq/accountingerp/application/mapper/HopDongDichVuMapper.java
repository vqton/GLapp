package com.tonvq.accountingerp.application.mapper;

import com.tonvq.accountingerp.application.dto.HopDongDichVuCreateRequest;
import com.tonvq.accountingerp.application.dto.HopDongDichVuResponse;
import com.tonvq.accountingerp.domain.model.HopDongDichVu;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for HopDongDichVu entity and DTOs
 * 
 * @author Ton VQ
 */
@Component
public class HopDongDichVuMapper {

    /**
     * Convert HopDongDichVuCreateRequest to HopDongDichVu domain entity
     */
    public HopDongDichVu toEntity(HopDongDichVuCreateRequest dto) {
        if (dto == null) {
            return null;
        }

        HopDongDichVu entity = new HopDongDichVu();
        entity.setMaHopDong(dto.getMaHopDong());
        entity.setKhachHangId(dto.getKhachHangId());
        entity.setNgayKy(dto.getNgayKy());
        entity.setNgayBatDau(dto.getNgayBatDau());
        entity.setNgayKetThuc(dto.getNgayKetThuc());
        entity.setGiaHopDong(dto.getGiaHopDong());
        entity.setSoMilestone(dto.getSoMilestone());
        entity.setMilestoneHoanThanh(0);
        entity.setPercentComplete(BigDecimal.ZERO);
        entity.setPhuongPhapCongNhan(dto.getPhuongPhapCongNhan());
        entity.setGhiChu(dto.getGhiChu());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setTrangThai("DRAFT");

        return entity;
    }

    /**
     * Convert HopDongDichVu domain entity to HopDongDichVuResponse DTO
     */
    public HopDongDichVuResponse toResponse(HopDongDichVu entity) {
        if (entity == null) {
            return null;
        }

        return HopDongDichVuResponse.builder()
                .id(entity.getId())
                .maHopDong(entity.getMaHopDong())
                .khachHangId(entity.getKhachHangId())
                .ngayKy(entity.getNgayKy())
                .ngayBatDau(entity.getNgayBatDau())
                .ngayKetThuc(entity.getNgayKetThuc())
                .giaHopDong(entity.getGiaHopDong())
                .soMilestone(entity.getSoMilestone())
                .milestoneHoanThanh(entity.getMilestoneHoanThanh())
                .percentComplete(entity.getPercentComplete())
                .phuongPhapCongNhan(entity.getPhuongPhapCongNhan())
                .trangThai(entity.getTrangThai())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .ghiChu(entity.getGhiChu())
                .build();
    }

    /**
     * Convert list of HopDongDichVu entities to HopDongDichVuResponse DTOs
     */
    public List<HopDongDichVuResponse> toResponseList(List<HopDongDichVu> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
