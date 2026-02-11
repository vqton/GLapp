package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for Updating Progress HopDongDichVu
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HopDongDichVuProgressRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long hopDongId;
    private BigDecimal percentComplete;  // Phần trăm hoàn thành (0-100)
    private Long updatedBy;
}
