package com.xhs.clothingpatternbackend.model.dto.pattern;

import lombok.Data;

import java.io.Serializable;

@Data
public class PatternGenerateTaskInfo implements Serializable {

    private String taskId;

    private Long userId;

    private String status;

    private Long patternId;

    private String errorMessage;

    private Long createTime;

    private Long updateTime;

    private static final long serialVersionUID = 1L;
}
