package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for Creating ButToan (Journal Entry Detail)
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ButToanCreateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long chungTuId;
    private String tkNo;                // Tài khoản nợ (Debit account)
    private String tkCo;                // Tài khoản có (Credit account)
    private BigDecimal soTien;          // Số tiền
    private String ghiChu;              // Ghi chú
}
