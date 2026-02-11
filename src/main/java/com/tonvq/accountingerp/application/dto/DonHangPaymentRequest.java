package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for Recording Payment
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonHangPaymentRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long donHangId;
    private BigDecimal paymentAmount;   // Số tiền thanh toán
    private String paymentMethod;       // Phương thức thanh toán (CASH, BANK, CHEQUE, etc.)
    private Long recordedBy;            // User ghi nhận thanh toán
}
