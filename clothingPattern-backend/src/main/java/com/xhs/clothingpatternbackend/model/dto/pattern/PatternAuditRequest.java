package com.xhs.clothingpatternbackend.model.dto.pattern;

import lombok.Data;

import java.io.Serializable;

/**
 * 图案审核请求
 */
@Data
public class PatternAuditRequest implements Serializable {

    /**
     * 图案ID
     */
    private Long id;

    /**
     * 审核状态：APPROVED、REJECTED
     */
    private String auditStatus;

    /**
     * 拒绝原因（拒绝时必填）
     */
    private String rejectReason;

    private static final long serialVersionUID = 1L;
}
