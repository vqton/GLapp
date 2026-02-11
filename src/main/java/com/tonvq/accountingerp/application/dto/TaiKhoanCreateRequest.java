package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for Creating TaiKhoan (Chart of Accounts)
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TaiKhoanCreateRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private String maTaiKhoan;          // Mã tài khoản (VD: 1111, 131, 331, etc.)
    private String tenTaiKhoan;         // Tên tài khoản
    private String loaiTaiKhoan;        // Loại: TSTC, TSLH, VOHH, HSTP, DTPB, TK_CHI, TK_CUA, etc.
    private Integer cap;                // Cấp (1, 2, 3)
    private String taiKhoanCha;         // Tài khoản cha
    private String ghiChu;
    private Long createdBy;
}
