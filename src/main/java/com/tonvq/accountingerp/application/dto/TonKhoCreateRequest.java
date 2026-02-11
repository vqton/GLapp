package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for Creating TonKho (Inventory)
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TonKhoCreateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maSanPham;           // Mã sản phẩm
    private String tenSanPham;          // Tên sản phẩm
    private String maKho;               // Mã kho
    private BigDecimal soLuongDau;      // Số lượng đầu kỳ
    private BigDecimal giaBan;          // Giá bán
    private String donViTinh;           // Đơn vị tính
    private Long createdBy;
}
