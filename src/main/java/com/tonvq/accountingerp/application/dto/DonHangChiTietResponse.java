package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for DonHang Chi Tiết Response
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonHangChiTietResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long donHangId;
    private String maSanPham;
    private String tenSanPham;
    private BigDecimal soLuong;
    private String donViTinh;
    private BigDecimal giaBan;
    private BigDecimal tongTien;
    private BigDecimal tienVAT;
    private String ghiChu;
}
