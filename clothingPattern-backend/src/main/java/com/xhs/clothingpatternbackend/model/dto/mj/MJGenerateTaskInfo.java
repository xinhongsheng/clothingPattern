package com.xhs.clothingpatternbackend.model.dto.mj;

import com.xhs.clothingpatternbackend.model.vo.MJImagineVO;
import lombok.Data;

import java.io.Serializable;

@Data
public class MJGenerateTaskInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    private String taskId;

    private Long userId;

    private String status;

    private MJImagineVO result;

    private String errorMessage;

    private Long createTime;

    private Long updateTime;
}
