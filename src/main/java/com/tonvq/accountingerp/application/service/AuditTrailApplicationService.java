package com.tonvq.accountingerp.application.service;

import com.tonvq.accountingerp.application.dto.AuditTrailResponse;
import com.tonvq.accountingerp.domain.repository.AuditTrailRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Application Service for Audit Trail
 * Logs all changes to entities: user, timestamp, old/new values
 * Per TT 99/2025/TT-BTC - Audit and compliance requirements
 * 
 * @author Ton VQ
 */
@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
public class AuditTrailApplicationService {

    private final AuditTrailRepository auditTrailRepository;

    /**
     * Log entity creation
     */
    public void logCreation(String entityType, Long entityId, String userId, String newValue, String reason) {
        log.info("Logging creation: type={}, id={}, user={}", entityType, entityId, userId);

        // TODO: Implement audit trail logging
        // This would save to audit trail table with:
        // - Entity type and ID
        // - Action: CREATE
        // - User ID
        // - Timestamp
        // - New value (as JSON)
        // - Change reason
        // - IP address (from request context)
    }

    /**
     * Log entity update
     */
    public void logUpdate(String entityType, Long entityId, String userId, 
                         String oldValue, String newValue, String reason) {
        log.info("Logging update: type={}, id={}, user={}", entityType, entityId, userId);

        // TODO: Implement audit trail logging
        // Same as above but with old/new values for comparison
    }

    /**
     * Log entity deletion
     */
    public void logDeletion(String entityType, Long entityId, String userId, String oldValue, String reason) {
        log.info("Logging deletion: type={}, id={}, user={}", entityType, entityId, userId);

        // TODO: Implement audit trail logging
        // Records deletion with old value preserved
    }

    /**
     * Log workflow action (approve, post, lock, etc.)
     */
    public void logAction(String entityType, Long entityId, String action, String userId, String reason) {
        log.info("Logging action: type={}, id={}, action={}, user={}", entityType, entityId, action, userId);

        // TODO: Implement action logging
        // Records workflow transitions: APPROVE, POST, LOCK, CANCEL, etc.
    }

    /**
     * Get audit trail for specific entity
     */
    @Transactional(readOnly = true)
    public List<AuditTrailResponse> getAuditTrail(String entityType, Long entityId) {
        log.info("Retrieving audit trail: type={}, id={}", entityType, entityId);

        // TODO: Implement retrieval of audit trail records
        return List.of();
    }

    /**
     * Get audit trail by date range
     */
    @Transactional(readOnly = true)
    public List<AuditTrailResponse> getAuditTrailByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        log.info("Retrieving audit trail: startDate={}, endDate={}", startDate, endDate);

        // TODO: Implement retrieval of audit trail records for date range
        return List.of();
    }

    /**
     * Get audit trail by user
     */
    @Transactional(readOnly = true)
    public List<AuditTrailResponse> getAuditTrailByUser(String userId) {
        log.info("Retrieving audit trail by user: userId={}", userId);

        // TODO: Implement retrieval of all changes made by specific user
        return List.of();
    }
}
