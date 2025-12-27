package com.xhs.clothingpatternbackend.model.vo;

import com.xhs.clothingpatternbackend.model.entity.Pattern;
import lombok.Data;

import java.io.Serializable;

@Data
public class PatternGenerateTaskVO implements Serializable {

    private String taskId;

    private String status;

    private Long patternId;

    private Pattern pattern;

    private String errorMessage;

    private Long createTime;

    private Long updateTime;

    private static final long serialVersionUID = 1L;
}
