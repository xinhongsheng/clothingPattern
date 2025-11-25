package com.xhs.clothingpatternbackend.model.dto.pattern;

import lombok.Data;

import java.io.Serializable;

/**
 * 数据报告导出请求
 */
@Data
public class DataExportRequest implements Serializable {

    /**
     * 导出格式：excel、pdf、csv
     */
    private String format;

    /**
     * 开始日期
     */
    private String startDate;

    /**
     * 结束日期
     */
    private String endDate;

    private static final long serialVersionUID = 1L;
}
