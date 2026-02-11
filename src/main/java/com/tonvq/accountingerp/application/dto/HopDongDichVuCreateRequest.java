package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for Creating HopDongDichVu (Service Contract)
 * VAS 14/15 - Tiêu chuẩn công nhận doanh thu
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HopDongDichVuCreateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maHopDong;           // Mã hợp đồng
    private Long khachHangId;           // Khách hàng
    private LocalDate ngayKy;           // Ngày ký hợp đồng
    private LocalDate ngayBatDau;       // Ngày bắt đầu
    private LocalDate ngayKetThuc;      // Ngày kết thúc
    private BigDecimal giaHopDong;      // Giá hợp đồng
    private Integer soMilestone;        // Số milestone
    private String phuongPhapCongNhan;  // Phương pháp: MILESTONE, % HOAN_THANH
    private String ghiChu;
    private Long createdBy;
}
