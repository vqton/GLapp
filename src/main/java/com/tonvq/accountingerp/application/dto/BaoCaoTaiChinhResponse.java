package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTO for Financial Report Response
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaoCaoTaiChinhResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String loaiBaoCao;
    private LocalDate startDate;
    private LocalDate endDate;
    private LocalDate asOfDate;
    
    // Report data as map (flexible structure)
    private Map<String, Object> reportData;
    
    // Summary totals
    private BigDecimal tongDoanhThu;
    private BigDecimal tongChiPhi;
    private BigDecimal loiNhuanXu;
    
    private Long createdBy;
    private LocalDate createdAt;
}
