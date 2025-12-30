package com.xhs.clothingpatternbackend.controller;

import cn.hutool.core.util.StrUtil;
import com.xhs.clothingpatternbackend.common.BaseResponse;
import com.xhs.clothingpatternbackend.common.DeleteRequest;
import com.xhs.clothingpatternbackend.common.ResultUtils;
import com.xhs.clothingpatternbackend.config.CosClientConfig;
import com.xhs.clothingpatternbackend.exception.ErrorCode;
import com.xhs.clothingpatternbackend.exception.ThrowUtils;
import com.xhs.clothingpatternbackend.model.dto.tryon.TryOnGenerateMessage;
import com.xhs.clothingpatternbackend.model.entity.TryOnTask;
import com.xhs.clothingpatternbackend.model.entity.User;
import com.xhs.clothingpatternbackend.model.vo.QueryTaskHistoryResultVO;
import com.xhs.clothingpatternbackend.mq.TryOnGenerateProducer;
import com.xhs.clothingpatternbackend.service.TryOnTaskService;
import com.xhs.clothingpatternbackend.service.UserService;
import com.xhs.clothingpatternbackend.service.impl.TryOnTaskServiceImpl;
import com.xhs.clothingpatternbackend.utils.CosImageUploadUtils;
import com.xhs.clothingpatternbackend.utils.CosUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-12-01
 * @Description:
 * @Version: 1.0
 */

@RestController
@Validated
@RequestMapping("/try-on")

public class AiTryOnController {
    @Autowired
    private TryOnTaskServiceImpl tryOnTaskService;
    @Autowired
    private UserService userService;
    @Autowired
    private CosImageUploadUtils cosImageUploadUtils;
    @Autowired
    private CosUtils cosUtils;
    @Resource
    private CosClientConfig cosClientConfig;
    @Resource
    private TryOnGenerateProducer tryOnGenerateProducer;

    // 上传图片到OSS，返回公网URL
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file,HttpServletRequest  request) throws Exception {
        User loginUser = userService.getLoginUser(request);
        return CosImageUploadUtils.uploadImageToCos(file,loginUser.getId(),cosUtils,cosClientConfig,"try_on_", "ai/tryOn/",true);
    }

    // 提交试衣任务
    @PostMapping("/submit")
    public String submit(@RequestParam @NotBlank(message = "人物图片URL不能为空")String personImageUrl,
                         @RequestParam(required = false) String topGarmentUrl,
                         @RequestParam(required = false)String bottomGarmentUrl,
                         HttpServletRequest  request) throws  IOException {
        if (StringUtils.isEmpty(topGarmentUrl) && StringUtils.isEmpty(bottomGarmentUrl)) {
            throw new IllegalArgumentException("请上传上衣或裤子(上装或者下装)");
        }
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        String taskId = tryOnTaskService.submitTask(userId, personImageUrl, topGarmentUrl, bottomGarmentUrl);
        try {
            tryOnGenerateProducer.send(new TryOnGenerateMessage(taskId, userId));
        } catch (Exception e) {
            // 异步入队失败不影响任务提交
        }
        return taskId;
    }

    /**
     * 查询任务状态（返回完整信息）
     */
    @GetMapping("/status/{taskId}")
    public ResponseEntity<TryOnTask> getStatus(@PathVariable String taskId) {
        try {
            TryOnTask task = tryOnTaskService.queryTaskStatus(taskId);
            return ResponseEntity.ok(task);
        } catch (Exception e) {
            // 返回错误信息（前端可展示）
            return ResponseEntity.badRequest().body(null);
        }
    }
    /**
     * 获取用户试衣的历史记录
     */
    @GetMapping("/{userId}")
    public BaseResponse<List<QueryTaskHistoryResultVO>> getTryOnHistory(@PathVariable Long userId) {
        ThrowUtils.throwIf(userId <= 0, ErrorCode.PARAMS_ERROR);
        List<QueryTaskHistoryResultVO> taskHistory = tryOnTaskService.getTryOnHistory(userId);
        return ResultUtils.success(taskHistory);
    }
    /**
     * 删除试衣记录
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTryOnRecord(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        ThrowUtils.throwIf(deleteRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(deleteRequest.getId() <= 0, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        return ResultUtils.success(tryOnTaskService.removeByUserId(deleteRequest.getId(), userId));
    }

}
