package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for TyGia (Exchange Rate) Response
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TyGiaResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String tienTeCo;
    private String tienTeNo;
    private LocalDate ngayTinh;
    private BigDecimal tyGiaTinh;       // Tỷ giá tính
    private BigDecimal soTienCo;        // Số tiền có
    private BigDecimal soTienNo;        // Số tiền nợ quy đổi
    private BigDecimal chenhLechTyGia;  // Chênh lệch tỷ giá
    private String taiKhoanChenhLech;   // Tài khoản ghi chênh lệch
}
