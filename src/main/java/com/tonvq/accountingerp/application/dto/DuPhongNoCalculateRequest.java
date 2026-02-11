package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for Calculating DuPhongNo (Allowance for doubtful debts)
 * TT 48/2019 - Điều 32
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuPhongNoCalculateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long khachHangId;           // Mã khách hàng
    private String phuongPhapTinh;      // LICH_SU (By history %), TUOI_NO (By aging), CU_THE (Specific %)
    private BigDecimal tyLe;            // Tỷ lệ dự phòng (%)
    private LocalDate asOfDate;         // Ngày tính
    private Long calculatedBy;
}
