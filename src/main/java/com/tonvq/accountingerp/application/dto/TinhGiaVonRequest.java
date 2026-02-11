package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;

/**
 * DTO for Calculating Cost (Tính giá vốn)
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TinhGiaVonRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long tonKhoId;
    private String phuongThucTinhGia;   // FIFO, LIFO, TRUNG_BINH
    private Long calculatedBy;
}
