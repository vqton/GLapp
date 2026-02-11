package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for ButToan Response
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ButToanResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long chungTuId;
    private String tkNo;                // Tài khoản nợ
    private String tkCo;                // Tài khoản có
    private BigDecimal soTien;          // Số tiền
    private String ghiChu;
}
