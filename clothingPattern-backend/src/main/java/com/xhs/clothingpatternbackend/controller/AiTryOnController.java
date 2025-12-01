package com.xhs.clothingpatternbackend.controller;

import com.xhs.clothingpatternbackend.config.CosClientConfig;
import com.xhs.clothingpatternbackend.model.entity.TryOnTask;
import com.xhs.clothingpatternbackend.model.entity.User;
import com.xhs.clothingpatternbackend.service.TryOnTaskService;
import com.xhs.clothingpatternbackend.service.UserService;
import com.xhs.clothingpatternbackend.service.impl.TryOnTaskServiceImpl;
import com.xhs.clothingpatternbackend.utils.CosImageUploadUtils;
import com.xhs.clothingpatternbackend.utils.CosUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-12-01
 * @Description:
 * @Version: 1.0
 */

@RestController
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

    // 上传图片到OSS，返回公网URL
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file,HttpServletRequest  request) throws Exception {
        User loginUser = userService.getLoginUser(request);
        return CosImageUploadUtils.uploadImageToCos(file,loginUser.getId(),cosUtils,cosClientConfig,"try_on_", "ai/tryOn/",true);
    }

    // 提交试衣任务
    @PostMapping("/submit")
    public String submit(@RequestParam String personImageUrl, @RequestParam String topGarmentUrl, HttpServletRequest  request) throws  IOException {
        User loginUser = userService.getLoginUser(request);
        Long userId = loginUser.getId();
        return tryOnTaskService.submitTask(userId, personImageUrl, topGarmentUrl);
    }

    // 查询任务状态
    @GetMapping("/status/{taskId}")
    public TryOnTask getStatus(@PathVariable String taskId) throws IOException {
        return tryOnTaskService.queryTaskStatus(taskId);
    }
}
