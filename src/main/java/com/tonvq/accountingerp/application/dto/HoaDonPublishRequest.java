package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for Publishing HoaDon (Phát hành)
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HoaDonPublishRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long hoaDonId;
    private Long publishedBy;
    private String publishReason;
}
