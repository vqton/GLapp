package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for Approving ChungTu
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChungTuApproveRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long chungTuId;
    private Long approvedBy;            // User approving
    private String approvalReason;      // Lý do phê duyệt (optional)
}
