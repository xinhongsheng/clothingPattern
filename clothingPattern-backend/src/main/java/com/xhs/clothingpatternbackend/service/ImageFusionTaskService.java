package com.xhs.clothingpatternbackend.service;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.xhs.clothingpatternbackend.model.dto.mj.WanQueryRequest;
import com.xhs.clothingpatternbackend.model.dto.pattern.PatternQueryRequest;
import com.xhs.clothingpatternbackend.model.entity.ImageFusionTask;
import com.baomidou.mybatisplus.extension.service.IService;
import com.xhs.clothingpatternbackend.model.entity.Pattern;
import com.xhs.clothingpatternbackend.model.vo.PatternVO;
import com.xhs.clothingpatternbackend.model.vo.WanQueryVO;
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

    QueryWrapper<ImageFusionTask> getQueryWrapper(WanQueryRequest wanQueryRequest);

    List<WanQueryVO> getImageFusionVOList(List<ImageFusionTask> imageFusionTaskList, Long loginUserId);

    // -------------------------- 保存选中的图片（将选中的单张图片URL替换原有的多个URL） --------------------------
    @Transactional(rollbackFor = Exception.class)
    boolean saveSelectedImage(String dashscopeTaskId, String selectedImageUrl);
}
