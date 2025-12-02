package com.xhs.clothingpatternbackend.service;

import com.xhs.clothingpatternbackend.model.entity.ImageFusionTask;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
* @author 19099
* @description 针对表【image_fusion_task(多图融合任务表（含结果）)】的数据库操作Service
* @createDate 2025-12-02 14:59:51
*/
public interface ImageFusionTaskService extends IService<ImageFusionTask> {

    // -------------------------- 提交任务（返回 DashScope 任务ID，供后续查询使用） --------------------------
    @Transactional(rollbackFor = Exception.class)
    String submitTask(Long userId, String prompt, String negativePrompt, List<String> imageUrls, String parameters);

    // -------------------------- 查询任务状态（通过 DashScope 任务ID 查询并合并结果） --------------------------
    @Transactional(rollbackFor = Exception.class)
    ImageFusionTask queryTaskStatus(String dashscopeTaskId);

    // -------------------------- 查询结果图片（通过 DashScope 任务ID 获取COS永久URL列表） --------------------------
    List<String> getTaskResults(String dashscopeTaskId);
}
