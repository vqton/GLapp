package com.tonvq.accountingerp.domain.event;

import com.tonvq.accountingerp.domain.model.valueobject.SoLuong;
import com.tonvq.accountingerp.domain.model.valueobject.GiaVon;

/**
 * Domain Event: Kho Cập Nhật
 * 
 * Được phát hành khi cập nhật tồn kho (nhập/xuất).
 * 
 * @author Ton VQ
 */
public class KhoUpdatedEvent extends DomainEvent {
    
    private String maSanPham;
    private String loaiThayDoi;                    // NHAP, XUAT
    private SoLuong soLuong;
    private GiaVon gia;
    private String updatedBy;
    
    // ============ Constructor ============
    public KhoUpdatedEvent(String khoId, String maSanPham, String loaiThayDoi,
                          SoLuong soLuong, GiaVon gia, String updatedBy) {
        super(khoId);
        this.maSanPham = maSanPham;
        this.loaiThayDoi = loaiThayDoi;
        this.soLuong = soLuong;
        this.gia = gia;
        this.updatedBy = updatedBy;
    }
    
    // ============ Getters ============
    public String getMaSanPham() {
        return maSanPham;
    }
    
    public String getLoaiThayDoi() {
        return loaiThayDoi;
    }
    
    public SoLuong getSoLuong() {
        return soLuong;
    }
    
    public GiaVon getGia() {
        return gia;
    }
    
    public String getUpdatedBy() {
        return updatedBy;
    }
}
