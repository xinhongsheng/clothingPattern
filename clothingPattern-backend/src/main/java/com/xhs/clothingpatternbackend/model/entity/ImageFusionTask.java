package com.xhs.clothingpatternbackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import lombok.Data;

/**
 * 多图融合任务表（含结果）
 * @TableName image_fusion_task
 */
@TableName(value ="image_fusion_task")
@Data
public class ImageFusionTask implements Serializable {
    /**
     * 主键
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;
    private Long userId;
    private String dashscopeTaskId;
    private String prompt;
    private String negativePrompt;
    private String imageUrls; // 输入图片URL（逗号分隔）
    private String parameters;
    private String taskStatus;
    private LocalDateTime submitTime;
    private LocalDateTime scheduledTime;
    private LocalDateTime endTime;
    private String errorCode;
    private String errorMessage;

    // 新增：合并后的结果字段
    private String origPrompts; // 结果提示词（逗号分隔）
    private String tempImageUrls; // 临时URL（逗号分隔）
    private String localImageUrls; // 永久URL（逗号分隔）
    private String sorts; // 排序（逗号分隔）

    private LocalDateTime createTime;
    private LocalDateTime updateTime;

    // -------------------------- 辅助方法：多值拆分/拼接 --------------------------
    /**
     * 拆分临时URL为列表
     */
    public List<String> getTempImageUrlList() {
        return splitValue(this.tempImageUrls);
    }

    /**
     * 拆分永久URL为列表
     */
    public List<String> getLocalImageUrlList() {
        return splitValue(this.localImageUrls);
    }

    /**
     * 拆分排序为列表
     */
    public List<Integer> getSortList() {
        return splitValue(this.sorts).stream()
                .map(Integer::valueOf)
                .collect(Collectors.toList());
    }

    /**
     * 拆分提示词为列表
     */
    public List<String> getOrigPromptList() {
        return splitValue(this.origPrompts);
    }

    /**
     * 拼接列表为逗号分隔字符串
     */
    public void setTempImageUrlList(List<String> urls) {
        this.tempImageUrls = joinValue(urls);
    }

    public void setLocalImageUrlList(List<String> urls) {
        this.localImageUrls = joinValue(urls);
    }

    public void setSortList(List<Integer> sorts) {
        this.sorts = joinValue(sorts.stream().map(String::valueOf).collect(Collectors.toList()));
    }

    public void setOrigPromptList(List<String> prompts) {
        this.origPrompts = joinValue(prompts);
    }

    // 私有工具：拆分字符串为列表（空字符串返回空列表）
    private List<String> splitValue(String value) {
        if (value == null || value.trim().isEmpty()) {
            return List.of();
        }
        return Arrays.stream(value.split(","))
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .collect(Collectors.toList());
    }

    // 私有工具：拼接列表为逗号分隔字符串
    private String joinValue(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "";
        }
        return String.join(",", list);
    }

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}