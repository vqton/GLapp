package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for DuPhongNo Response
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DuPhongNoResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long khachHangId;
    private String phuongPhapTinh;      // LICH_SU, TUOI_NO, CU_THE
    private BigDecimal tienTrongNo;     // Tiền trong nợ
    private BigDecimal tyLe;            // Tỷ lệ dự phòng
    private BigDecimal soTienDuPhong;   // Số tiền dự phòng
    private BigDecimal duPhongHienCo;   // Dự phòng hiện có
    private BigDecimal duPhongCanDieuChinh;  // Dự phòng cần điều chỉnh
    private LocalDate asOfDate;
    private String trangThai;
}
