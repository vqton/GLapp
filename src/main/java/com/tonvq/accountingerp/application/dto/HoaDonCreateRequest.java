package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for Creating HoaDon (Invoice)
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonCreateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maHoaDon;            // Mã hóa đơn
    private Long donHangId;             // Đơn hàng liên quan
    private LocalDate ngayLap;          // Ngày lập hóa đơn
    private Long khachHangId;           // Khách hàng
    private BigDecimal tongTienHang;    // Tổng tiền hàng
    private BigDecimal tienVAT;         // Tiền VAT
    private BigDecimal tongTienThanhToan;  // Tổng tiền thanh toán
    private String ghiChu;
    private Long createdBy;
}
