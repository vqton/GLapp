package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * DTO for HopDongDichVu Response
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HopDongDichVuResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String maHopDong;
    private Long khachHangId;
    private LocalDate ngayKy;
    private LocalDate ngayBatDau;
    private LocalDate ngayKetThuc;
    private BigDecimal giaHopDong;
    private Integer soMilestone;
    private Integer milestoneHoanThanh;
    private BigDecimal percentComplete;
    private String phuongPhapCongNhan;
    private String trangThai;           // DRAFT, ACTIVE, IN_PROGRESS, COMPLETED, CANCELLED
    private BigDecimal doanhThuCongNhan;
    private Long createdBy;
    private LocalDateTime createdAt;
    private String ghiChu;
}
