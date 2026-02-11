package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for DonHang Chi Tiết (Line Item) Create Request
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonHangChiTietCreateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long donHangId;
    private String maSanPham;           // Mã sản phẩm
    private String tenSanPham;          // Tên sản phẩm
    private BigDecimal soLuong;         // Số lượng
    private String donViTinh;           // Đơn vị tính
    private BigDecimal giaBan;          // Giá bán
    private BigDecimal tongTien;        // Tổng tiền (soLuong x giaBan)
    private BigDecimal tienVAT;         // VAT cho line item
    private String ghiChu;              // Ghi chú
}
