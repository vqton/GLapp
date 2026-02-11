package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for Tỷ giá (Exchange Rate) Calculation
 * Điều 31 - TK 413/515/635
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TyGiaCalculateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String tienTeCo;            // Tiền tệ có (USD, EUR, etc.)
    private String tienTeNo;            // Tiền tệ nợ (VND, etc.)
    private LocalDate ngayTinh;         // Ngày tính
    private BigDecimal soTienCo;        // Số tiền có
    private Long calculatedBy;
}
