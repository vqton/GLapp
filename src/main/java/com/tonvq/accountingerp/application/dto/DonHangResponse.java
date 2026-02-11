package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for DonHang Response
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonHangResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    // Identity
    private Long id;
    private String maDonHang;
    private String loaiDonHang;         // MUAR or BAN
    private LocalDate ngayDonHang;
    
    // Customer/Supplier
    private Long maKhachHang;
    private String diaChi;
    private LocalDate ngayGiaoKyVong;
    
    // Financial
    private BigDecimal tienChietKhau;
    private BigDecimal tienVAT;
    private BigDecimal tongTien;
    private BigDecimal tienDaThanhToan;
    private BigDecimal tienConNo;
    
    // Status
    private String trangThai;           // DRAFT, CONFIRMED, SHIPPING, DELIVERED, PAID
    
    // Related items
    private List<DonHangChiTietResponse> donHangChiTietList;
    
    // Audit
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long confirmedBy;
    private LocalDateTime confirmedAt;
    private Long deliveredBy;
    private LocalDateTime deliveredAt;
    private String ghiChu;
}
