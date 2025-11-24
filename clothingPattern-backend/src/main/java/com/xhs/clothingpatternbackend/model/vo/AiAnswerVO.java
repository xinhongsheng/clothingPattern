package com.xhs.clothingpatternbackend.model.vo;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-11-24
 * @Description: AI 问答响应
 * @Version: 1.0
 */
@Data
public class AiAnswerVO implements Serializable {

    /**
     * 问题
     */
    private String question;

    /**
     * 回答内容
     */
    private String answer;

    /**
     * 图片 URL（如果有）
     */
    private String imageUrl;

    private static final long serialVersionUID = 1L;
}
