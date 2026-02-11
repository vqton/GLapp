package com.tonvq.accountingerp.domain.event;

import com.tonvq.accountingerp.domain.model.valueobject.TrangThaiChungTu;

/**
 * Domain Event: Chứng Từ Khóa
 * 
 * Được phát hành khi khóa chứng từ.
 * 
 * @author Ton VQ
 */
public class ChungTuLockedEvent extends DomainEvent {
    
    private String maChungTu;
    private TrangThaiChungTu previousStatus;
    private String lockedBy;
    
    // ============ Constructor ============
    public ChungTuLockedEvent(String chungTuId, String maChungTu, 
                             TrangThaiChungTu previousStatus, String lockedBy) {
        super(chungTuId);
        this.maChungTu = maChungTu;
        this.previousStatus = previousStatus;
        this.lockedBy = lockedBy;
    }
    
    // ============ Getters ============
    public String getMaChungTu() {
        return maChungTu;
    }
    
    public TrangThaiChungTu getPreviousStatus() {
        return previousStatus;
    }
    
    public String getLockedBy() {
        return lockedBy;
    }
}
