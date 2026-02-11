package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for Locking ChungTu (Khóa chứng từ)
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChungTuLockRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long chungTuId;
    private Long lockedBy;              // User locking
    private String lockingReason;       // Lý do khóa (optional)
}
