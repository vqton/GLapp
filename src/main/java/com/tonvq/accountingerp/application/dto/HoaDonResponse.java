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
 * DTO for HoaDon Response
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String maHoaDon;
    private Long donHangId;
    private LocalDate ngayLap;
    private Long khachHangId;
    private BigDecimal tongTienHang;
    private BigDecimal tienVAT;
    private BigDecimal tongTienThanhToan;
    private BigDecimal tienDaThanhToan;
    private BigDecimal tienConNo;
    private String trangThai;           // DRAFT, ISSUED, CANCELLED
    private Long createdBy;
    private LocalDateTime createdAt;
    private LocalDateTime publishedAt;
    private String ghiChu;
}
