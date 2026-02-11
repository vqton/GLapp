package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for Confirming DonHang
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DonHangConfirmRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long donHangId;
    private Long confirmedBy;           // User confirming order
    private String confirmReason;       // Lý do xác nhận (optional)
}
