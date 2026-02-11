package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for Shipping DonHang (Giao hàng)
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonHangShipRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long donHangId;
    private LocalDate shipmentDate;     // Ngày giao hàng
    private String shipmentNote;        // Ghi chú vận chuyển
    private Long shippedBy;             // User thực hiện vận chuyển
}
