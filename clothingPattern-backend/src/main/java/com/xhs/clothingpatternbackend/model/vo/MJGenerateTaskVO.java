package com.xhs.clothingpatternbackend.model.vo;

import lombok.Data;

import java.io.Serializable;

@Data
public class MJGenerateTaskVO implements Serializable {

    private static final long serialVersionUID = 1L;

    private String taskId;

    private String status;

    private MJImagineVO result;

    private String errorMessage;

    private Long createTime;

    private Long updateTime;
}
