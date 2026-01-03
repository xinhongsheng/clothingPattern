package com.xhs.clothingpatternbackend.model.dto.ai;

import lombok.Data;

import java.io.Serializable;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-11-24
 * @Description: AI 问答请求
 * @Version: 1.0
 */
@Data
public class AiQuestionRequest implements Serializable {

    /**
     * 用户问题
     */
    private String question;

    /**
     * 图片 URL（可选，用于图片分析）
     */
    private String imageUrl;

    /**
     * AI 角色类型（可选）
     * - designer: 服装设计师（默认）
     * - analyst: 市场分析师
     */
    private String role;

    private static final long serialVersionUID = 1L;
}
