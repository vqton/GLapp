package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * DTO for Creating DonHang (Purchase Order / Sales Order)
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonHangCreateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    // Required fields
    private String maDonHang;           // Mã đơn hàng
    private String loaiDonHang;         // MUAR (Mua) hoặc BAN (Bán)
    private LocalDate ngayDonHang;      // Ngày lập đơn hàng
    private Long maKhachHang;           // Mã khách hàng
    
    // Optional fields
    private String diaChi;              // Địa chỉ giao hàng
    private LocalDate ngayGiaoKyVong;   // Ngày giao kỳ vọng
    private BigDecimal tienChietKhau;   // Tiền chiết khấu (optional)
    private BigDecimal tienVAT;         // Tiền VAT
    private BigDecimal tongTien;        // Tổng tiền
    private String ghiChu;              // Ghi chú
    
    // Related items
    private List<DonHangChiTietCreateRequest> donHangChiTietList;  // Chi tiết đơn hàng
    
    // Metadata
    private Long createdBy;             // User tạo
}
