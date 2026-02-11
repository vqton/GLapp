package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * DTO for Audit Trail
 * Log mọi thay đổi: user, timestamp, old/new values
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditTrailResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String entityType;          // ChungTu, DonHang, TonKho, etc.
    private Long entityId;              // ID của entity
    private String action;              // CREATE, UPDATE, DELETE, APPROVE, POST, LOCK
    private String userId;              // User thực hiện hành động
    private LocalDateTime changedAt;    // Thời gian thay đổi
    private String oldValue;            // Giá trị cũ (JSON)
    private String newValue;            // Giá trị mới (JSON)
    private String changeReason;        // Lý do thay đổi
    private String ipAddress;           // IP address
}
