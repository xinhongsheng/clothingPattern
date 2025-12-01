package com.xhs.clothingpatternbackend.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

/**
 * 
 * @TableName try_on_task
 */
@TableName(value ="try_on_task")
@Data
public class TryOnTask implements Serializable {
    /**
     * 主键ID
     */
    @TableId(type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 关联用户ID
     */
    private Long userId;

    /**
     * 人物图片公网URL
     */
    private String personImageUrl;

    /**
     * 上装图片公网URL
     */
    private String topGarmentUrl;

    /**
     * 下装图片公网URL
     */
    private String bottomGarmentUrl;

    /**
     * 阿里云异步任务ID
     */
    private String dashscopeTaskId;

    /**
     * 任务状态：PENDING/SUCCEEDED/FAILED
     */
    private String taskStatus;

    /**
     * 试衣结果图片URL
     */
    private String resultImageUrl;

    /**
     * 
     */
    private Date createTime;

    /**
     * 
     */
    private Date updateTime;

    @TableField(exist = false)
    private static final long serialVersionUID = 1L;
}