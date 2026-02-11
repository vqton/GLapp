package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO for ChungTu Response
 * Returns complete ChungTu data with all related information
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChungTuResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    // Identity
    private Long id;

    // Business fields
    private String maChungTu;
    private String loaiChungTu;
    private LocalDate ngayChungTu;
    private String ndChungTu;
    private BigDecimal soTien;
    private String donViTinh;

    // Related entities
    private Long nphatHanhId;
    private Long nThuHuongId;

    // Status
    private String trangThai;           // DRAFT, APPROVED, POSTED, LOCKED, CANCELLED

    // Related data
    private List<ButToanResponse> butToanList;

    // Audit fields
    private Long createdBy;
    private LocalDateTime createdAt;
    private Long approvedBy;
    private LocalDateTime approvedAt;
    private Long postedBy;
    private LocalDateTime postedAt;
    private Long lockedBy;
    private LocalDateTime lockedAt;
    private String ghiChu;

    // Validation/Check fields
    private Boolean isBalanced;         // Nợ = Có?
    private Integer butToanCount;       // Số bút toán
}
