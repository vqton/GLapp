package com.tonvq.accountingerp.domain.event;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain Event: Base Class
 * 
 * Lớp cơ sở cho tất cả các domain event.
 * Domain event được phát hành khi có sự kiện quan trọng trong domain.
 * 
 * @author Ton VQ
 */
public abstract class DomainEvent {
    
    private String eventId;                         // ID sự kiện
    private LocalDateTime occurredAt;               // Thời gian sự kiện xảy ra
    private String aggregateId;                     // ID của aggregate gây ra sự kiện
    
    // ============ Constructor ============
    protected DomainEvent(String aggregateId) {
        this.eventId = UUID.randomUUID().toString();
        this.occurredAt = LocalDateTime.now();
        this.aggregateId = aggregateId;
    }
    
    // ============ Getters ============
    public String getEventId() {
        return eventId;
    }
    
    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
    
    public String getAggregateId() {
        return aggregateId;
    }
    
    /**
     * Lấy tên sự kiện
     */
    public String getEventName() {
        return this.getClass().getSimpleName();
    }
    
    @Override
    public String toString() {
        return String.format("%s{eventId='%s', occurredAt=%s, aggregateId='%s'}",
                this.getClass().getSimpleName(), eventId, occurredAt, aggregateId);
    }
}
