package com.tonvq.accountingerp.application.mapper;

import com.tonvq.accountingerp.application.dto.DonHangCreateRequest;
import com.tonvq.accountingerp.application.dto.DonHangResponse;
import com.tonvq.accountingerp.domain.model.DonHang;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper for DonHang entity and DTOs
 * 
 * @author Ton VQ
 */
@Component
public class DonHangMapper {

    /**
     * Convert DonHangCreateRequest to DonHang domain entity
     */
    public DonHang toEntity(DonHangCreateRequest dto) {
        if (dto == null) {
            return null;
        }

        DonHang entity = new DonHang();
        entity.setMaDonHang(dto.getMaDonHang());
        entity.setLoaiDonHang(dto.getLoaiDonHang());
        entity.setNgayDonHang(dto.getNgayDonHang());
        entity.setMaKhachHang(dto.getMaKhachHang());
        entity.setDiaChi(dto.getDiaChi());
        entity.setNgayGiaoKyVong(dto.getNgayGiaoKyVong());
        entity.setTienChietKhau(dto.getTienChietKhau());
        entity.setTienVAT(dto.getTienVAT());
        entity.setTongTien(dto.getTongTien());
        entity.setTienDaThanhToan(java.math.BigDecimal.ZERO);
        entity.setTienConNo(dto.getTongTien());
        entity.setGhiChu(dto.getGhiChu());
        entity.setCreatedBy(dto.getCreatedBy());
        entity.setCreatedAt(LocalDateTime.now());
        entity.setTrangThai("DRAFT");

        return entity;
    }

    /**
     * Convert DonHang domain entity to DonHangResponse DTO
     */
    public DonHangResponse toResponse(DonHang entity) {
        if (entity == null) {
            return null;
        }

        return DonHangResponse.builder()
                .id(entity.getId())
                .maDonHang(entity.getMaDonHang())
                .loaiDonHang(entity.getLoaiDonHang())
                .ngayDonHang(entity.getNgayDonHang())
                .maKhachHang(entity.getMaKhachHang())
                .diaChi(entity.getDiaChi())
                .ngayGiaoKyVong(entity.getNgayGiaoKyVong())
                .tienChietKhau(entity.getTienChietKhau())
                .tienVAT(entity.getTienVAT())
                .tongTien(entity.getTongTien())
                .tienDaThanhToan(entity.getTienDaThanhToan())
                .tienConNo(entity.getTienConNo())
                .trangThai(entity.getTrangThai())
                .createdBy(entity.getCreatedBy())
                .createdAt(entity.getCreatedAt())
                .ghiChu(entity.getGhiChu())
                .build();
    }

    /**
     * Convert list of DonHang entities to DonHangResponse DTOs
     */
    public List<DonHangResponse> toResponseList(List<DonHang> entities) {
        if (entities == null) {
            return null;
        }
        return entities.stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }
}
