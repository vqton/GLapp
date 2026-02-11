package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

/**
 * DTO for Financial Reporting
 * Báo cáo tài chính (B01-B09 per TT 99)
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BaoCaoTaiChinhRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String loaiBaoCao;          // B01, B02, B03, B09 (etc.)
    private LocalDate startDate;        // Ngày bắt đầu
    private LocalDate endDate;          // Ngày kết thúc
    private LocalDate asOfDate;         // Ngày lập báo cáo
    private Long createdBy;
}
