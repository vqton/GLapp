package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for TonKho Response
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TonKhoResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String maSanPham;
    private String tenSanPham;
    private String maKho;
    private BigDecimal soLuongDau;
    private BigDecimal soLuongNhap;
    private BigDecimal soLuongXuat;
    private BigDecimal soLuongCuoi;
    private BigDecimal giaVonDau;
    private BigDecimal giaVonNhap;
    private BigDecimal giaVonXuat;
    private BigDecimal giaVonCuoi;
    private String donViTinh;
    private String phuongThucTinhGia;  // FIFO, LIFO, TRUNG_BINH
}
