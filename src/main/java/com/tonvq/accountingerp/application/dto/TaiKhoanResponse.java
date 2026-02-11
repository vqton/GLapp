package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for TaiKhoan Response
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaiKhoanResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String maTaiKhoan;
    private String tenTaiKhoan;
    private String loaiTaiKhoan;
    private Integer cap;
    private String taiKhoanCha;
    private BigDecimal congNo;          // Công nợ
    private BigDecimal congCo;          // Công có
    private BigDecimal truNo;           // Trụ nợ
    private BigDecimal truCo;           // Trụ có
    private BigDecimal soDuRong;        // Số dư rộng
    private String ghiChu;
}
