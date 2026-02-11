package com.tonvq.accountingerp.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * DTO for Posting ChungTu (Ghi sổ)
 * 
 * @author Ton VQ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChungTuPostRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long chungTuId;
    private Long postedBy;              // User posting
    private String postingReason;       // Lý do ghi sổ (optional)
}
