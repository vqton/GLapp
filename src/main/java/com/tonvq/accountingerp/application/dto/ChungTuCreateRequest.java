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
 * DTO for Creating ChungTu (Voucher/Document)
 * Maps to domain entity ChungTu
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChungTuCreateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    // Required fields
    private String maChungTu;           // Mã chứng từ
    private String loaiChungTu;         // Loại: HDDON, PHIEUCHU, PHIEOTHU, BIENBANKHO, etc.
    private LocalDate ngayChungTu;      // Ngày chứng từ
    private String ndChungTu;           // Nội dung/mô tả chứng từ
    private BigDecimal soTien;          // Số tiền
    
    // Optional fields
    private String donViTinh;           // Đơn vị tính (VND, USD, EUR) - default VND
    private Long nphatHanhId;           // Người phát hành
    private Long nThuHuongId;           // Người thụ hưởng
    private String ghiChu;              // Ghi chú
    
    // Related entities
    private List<ButToanCreateRequest> butToanList;  // Danh sách bút toán chi tiết
    
    // Metadata
    private Long createdBy;             // User tạo chứng từ
}
