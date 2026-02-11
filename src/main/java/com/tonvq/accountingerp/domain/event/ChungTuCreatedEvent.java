package com.tonvq.accountingerp.domain.event;

import java.time.LocalDateTime;

/**
 * Domain Event: Chứng Từ Tạo Mới
 * 
 * Được phát hành khi tạo mới chứng từ.
 * 
 * @author Ton VQ
 */
public class ChungTuCreatedEvent extends DomainEvent {
    
    private String maChungTu;
    private String loaiChungTu;
    private LocalDateTime ngayChungTu;
    private String createdBy;
    
    // ============ Constructor ============
    public ChungTuCreatedEvent(String chungTuId, String maChungTu, String loaiChungTu,
                               LocalDateTime ngayChungTu, String createdBy) {
        super(chungTuId);
        this.maChungTu = maChungTu;
        this.loaiChungTu = loaiChungTu;
        this.ngayChungTu = ngayChungTu;
        this.createdBy = createdBy;
    }
    
    // ============ Getters ============
    public String getMaChungTu() {
        return maChungTu;
    }
    
    public String getLoaiChungTu() {
        return loaiChungTu;
    }
    
    public LocalDateTime getNgayChungTu() {
        return ngayChungTu;
    }
    
    public String getCreatedBy() {
        return createdBy;
    }
}
