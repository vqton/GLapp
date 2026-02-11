package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for Importing Stock (Nhập hàng)
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NhapHangRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long tonKhoId;
    private BigDecimal soLuongNhap;     // Số lượng nhập
    private BigDecimal giaVonNhap;      // Giá vốn nhập
    private Long importedBy;
}
