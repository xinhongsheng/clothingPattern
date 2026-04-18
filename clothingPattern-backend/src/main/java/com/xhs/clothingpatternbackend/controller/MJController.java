package com.xhs.clothingpatternbackend.controller;

import com.alibaba.fastjson2.JSON;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xhs.clothingpatternbackend.common.BaseResponse;
import com.xhs.clothingpatternbackend.common.ResultUtils;
import com.xhs.clothingpatternbackend.exception.BusinessException;
import com.xhs.clothingpatternbackend.exception.ErrorCode;
import com.xhs.clothingpatternbackend.exception.ThrowUtils;
import com.xhs.clothingpatternbackend.mapper.PatternMapper;
import com.xhs.clothingpatternbackend.model.dto.mj.MJActionRequest;
import com.xhs.clothingpatternbackend.model.dto.mj.MJBlendRequest;
import com.xhs.clothingpatternbackend.model.dto.mj.MJImagineRequest;
import com.xhs.clothingpatternbackend.model.dto.mj.MJGenerateMessage;
import com.xhs.clothingpatternbackend.model.dto.mj.MJGenerateTaskInfo;
import com.xhs.clothingpatternbackend.model.vo.MJImagineVO;
import com.xhs.clothingpatternbackend.model.vo.MJGenerateTaskVO;
import com.xhs.clothingpatternbackend.model.entity.Pattern;
import com.xhs.clothingpatternbackend.model.entity.User;
import com.xhs.clothingpatternbackend.model.enums.GenerationTypeEnum;
import com.xhs.clothingpatternbackend.model.enums.PatternGenerateStatusEnum;
import com.xhs.clothingpatternbackend.mq.MJGenerateProducer;
import com.xhs.clothingpatternbackend.sdk.dashscope.BailianImageClient;
import com.xhs.clothingpatternbackend.service.MJGenerateTaskService;
import com.xhs.clothingpatternbackend.service.PatternService;
import com.xhs.clothingpatternbackend.service.PromptTranslateService;
import com.xhs.clothingpatternbackend.service.UserService;
import com.xhs.clothingpatternbackend.utils.VectorUtils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.io.File;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-11-28
 * @Description: Bailian image generation API
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/mj")
@Tag(name = "阿里百炼图片生成接口")
public class MJController {
    
    @Resource
    private BailianImageClient bailianImageClient;
    
    @Resource
    private PatternService patternService;
    
    @Resource
    private UserService userService;
    
    @Resource
    private PromptTranslateService promptTranslateService;

    @Resource
    private MJGenerateTaskService mjGenerateTaskService;

    @Resource
    private MJGenerateProducer mjGenerateProducer;

    @Resource
    private ObjectMapper objectMapper; // Jackson 工具，用于 String 转 float[]
    

    
    @Resource
    private PatternMapper patternMapper;
    
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
    @Resource
    private com.xhs.clothingpatternbackend.utils.CosUtils cosUtils;
    
    @Resource
    private com.xhs.clothingpatternbackend.config.CosClientConfig cosClientConfig;
    
    /**
     * 生成图片（Imagine）
     *
     * @param request 请求参数
     * @return 生成结果
     */
    @PostMapping("/imagine")
    @Operation(summary = "生成图片（不保存）")
    public BaseResponse<MJImagineVO> imagine(@RequestBody MJImagineRequest request) {
        // 参数校验
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        String originalPrompt = request.getPrompt();
        ThrowUtils.throwIf(StringUtils.isBlank(originalPrompt), ErrorCode.PARAMS_ERROR, "提示词不能为空");

        try {
            // 翻译并优化 prompt（添加服装专业前缀，包含风格、季节、受众信息）
            log.info("原始提示词：{}", originalPrompt);
            String optimizedPrompt = promptTranslateService.translateAndOptimize(
                    originalPrompt,
                    request.getStyle(),
                    request.getSeason(),
                    request.getTargetAudience()
            );
            log.info("优化后提示词：{}", optimizedPrompt);

            // 使用优化后的 prompt
            request.setPrompt(optimizedPrompt);
            
            MJImagineVO response = bailianImageClient.imagine(request);

            // 检查是否成功
            if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
                log.error("Bailian image generation failed, response: {}", response);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片生成失败");
            }

            return ResultUtils.success(response);
        } catch (IOException e) {
            log.error("Bailian image generation API exception", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片生成失败：" + e.getMessage());
        }
    }

    /**
     * 异步生成图片（Imagine）
     *
     * @param request 请求参数
     * @param httpRequest HTTP请求
     * @return 任务信息
     */
    @PostMapping("/imagine/async")
    @Operation(summary = "异步生成图片（不保存）")
    public BaseResponse<MJGenerateTaskVO> imagineAsync(@RequestBody MJImagineRequest request,
                                                       HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        String originalPrompt = request.getPrompt();
        ThrowUtils.throwIf(StringUtils.isBlank(originalPrompt), ErrorCode.PARAMS_ERROR, "提示词不能为空");

        User loginUser = userService.getLoginUser(httpRequest);
        MJGenerateTaskInfo taskInfo = mjGenerateTaskService.createTask(loginUser.getId());
        MJGenerateMessage message = new MJGenerateMessage(taskInfo.getTaskId(), loginUser.getId(), request);
        try {
            mjGenerateProducer.send(message);
        } catch (Exception e) {
            mjGenerateTaskService.markFailed(taskInfo.getTaskId(), "Queue send failed");
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成任务提交失败");
        }
        return ResultUtils.success(toTaskVO(taskInfo));
    }

    @GetMapping("/imagine/status/{taskId}")
    @Operation(summary = "查询异步生成状态")
    public BaseResponse<MJGenerateTaskVO> getImagineStatus(@PathVariable String taskId,
                                                           HttpServletRequest httpRequest) {
        ThrowUtils.throwIf(StringUtils.isBlank(taskId), ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(httpRequest);
        MJGenerateTaskInfo taskInfo = mjGenerateTaskService.getTask(taskId);
        ThrowUtils.throwIf(taskInfo == null, ErrorCode.NOT_FOUND_ERROR, "任务不存在");
        if (!userService.isAdmin(loginUser) && !loginUser.getId().equals(taskInfo.getUserId())) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        MJGenerateTaskVO taskVO = toTaskVO(taskInfo);
        if (PatternGenerateStatusEnum.SUCCEEDED.getValue().equals(taskInfo.getStatus())) {
            taskVO.setResult(taskInfo.getResult());
        }
        return ResultUtils.success(taskVO);
    }
    /**
     * 执行动作（如upsample、variation等）
     *
     * @param request 请求参数
     * @return 执行结果
     */
    @PostMapping("/action")
    @Operation(summary = "执行动作")
    public BaseResponse<MJImagineVO> executeAction(@RequestBody MJActionRequest request) {
        // 参数校验
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        String taskId = request.getTaskId();
        String imageId = request.getImageId();
        String action = request.getAction();
        MJImagineVO sourceResult = request.getSourceResult();

        ThrowUtils.throwIf(StringUtils.isBlank(imageId), ErrorCode.PARAMS_ERROR, "图片ID不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(action), ErrorCode.PARAMS_ERROR, "动作类型不能为空");

        try {
            MJImagineVO originalResult = null;

            // 方式1：优先使用直接传递的sourceResult（前端保存的完整结果）
            if (sourceResult != null) {
                log.info("使用前端传递的原始生成结果执行变体操作，action: {}", action);
                originalResult = sourceResult;
            }
            // 方式2：通过taskId从Redis查询
            else if (StringUtils.isNotBlank(taskId)) {
                log.info("通过taskId查询原始生成结果，taskId: {}", taskId);
                MJGenerateTaskInfo taskInfo = mjGenerateTaskService.getTask(taskId);

                // 检查任务是否存在
                if (taskInfo == null) {
                    log.error("任务不存在，taskId: {}，建议前端直接传递sourceResult", taskId);
                    throw new BusinessException(ErrorCode.NOT_FOUND_ERROR,
                            "原始生成任务不存在或已过期，请重新生成或在生成时保存完整结果");
                }

                // 检查任务结果是否存在
                if (taskInfo.getResult() == null) {
                    log.error("任务结果为空，taskId: {}", taskId);
                    throw new BusinessException(ErrorCode.PARAMS_ERROR,
                            "原始生成任务未完成或结果丢失");
                }

                originalResult = taskInfo.getResult();
            }
            // 两种方式都没有提供
            else {
                log.error("既没有提供taskId也没有提供sourceResult");
                throw new BusinessException(ErrorCode.PARAMS_ERROR,
                        "必须提供taskId或sourceResult参数");
            }

            // 执行动作
            MJImagineVO response = bailianImageClient.executeAction(
                    originalResult,
                    action
            );

            // 检查是否成功
            if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
                log.error("Bailian image action failed, response: {}", response);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "动作执行失败");
            }

            log.info("变体操作执行成功，action: {}, resultImageUrl: {}", action, response.getImageUrl());

            return ResultUtils.success(response);
        } catch (IOException e) {
            log.error("Bailian image action exception", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "动作执行失败：" + e.getMessage());
        }
    }
    
    /**
     * 保存 MJ 生成的图片到数据库
     * 下载图片并上传到 COS，同时获取文件大小
     *
     * @param request MJ 响应数据
     * @param httpRequest HTTP请求
     * @return 保存的图案ID
     */
    @PostMapping("/save")
    @Operation(summary = "保存图片到数据库")
    public BaseResponse<Long> savePattern(@RequestBody MJImagineVO request,
                                           HttpServletRequest httpRequest) {
        // 参数校验
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        
        // 获取登录用户
        User loginUser = userService.getLoginUser(httpRequest);
        
        File tempFile = null;
        try {
            // 检查必要的字段
            String rawImageUrl = request.getRawImageUrl();
            String imageUrl = request.getImageUrl();
            
            // 如果原始图片URL为空，使用缩略图URL
            if (StringUtils.isBlank(rawImageUrl)) {
                rawImageUrl = imageUrl;
            }
            
            // 如果两个URL都为空，抛出异常
            ThrowUtils.throwIf(StringUtils.isBlank(rawImageUrl), ErrorCode.PARAMS_ERROR, 
                "图片URL为空，无法保存");
            
            // 从请求参数中获取图案名称（前端传入）
            String patternName = request.getPatternName();
            if (StringUtils.isBlank(patternName)) {
                patternName = "图案-" + System.currentTimeMillis();
            }
            
            // 获取额外字段
            String style = request.getStyle();
            String season = request.getSeason();
            String targetAudience = request.getTargetAudience();
            
            // === 下载图片并上传到 COS ===
            String cosPatternUrl;
            String cosThumbUrl;
            Integer fileSize = null;
            String fileType = "image/png";  // 默认值
            
            // 下载原始图片到临时文件
            DownloadResult downloadResult = downloadImageToTempFile(rawImageUrl);
            if (downloadResult != null && downloadResult.file != null && downloadResult.file.exists()) {
                tempFile = downloadResult.file;
                fileType = downloadResult.contentType;
                
                // 获取文件大小
                fileSize = (int) tempFile.length();
                
                // 上传到 COS
                String key = "bailian-pattern/" + loginUser.getId() + "/" + System.currentTimeMillis() + ".png";
                com.qcloud.cos.model.PutObjectResult putResult = cosUtils.putPictureObject(key, tempFile);
                
                // 从COS处理结果中获取实际的图片URL
                try {
                    com.qcloud.cos.model.ciModel.persistence.CIUploadResult ciResult = putResult.getCiUploadResult();
                    if (ciResult != null && ciResult.getProcessResults() != null) {
                        com.qcloud.cos.model.ciModel.persistence.ProcessResults processResults = ciResult.getProcessResults();
                        java.util.List<com.qcloud.cos.model.ciModel.persistence.CIObject> objectList = processResults.getObjectList();
                        
                        if (objectList != null && !objectList.isEmpty()) {
                            // 第一个是压缩后的webp图片（缩略图）
                            com.qcloud.cos.model.ciModel.persistence.CIObject compressedObject = objectList.get(0);
                            cosThumbUrl = cosClientConfig.getHost() + "/" + compressedObject.getKey();
                            // 原图使用上传的key
                            cosPatternUrl = cosClientConfig.getHost() + "/" + key;
                        } else {
                            cosPatternUrl = cosClientConfig.getHost() + "/" + key;
                            cosThumbUrl = cosPatternUrl;
                        }
                    } else {
                        cosPatternUrl = cosClientConfig.getHost() + "/" + key;
                        cosThumbUrl = cosPatternUrl;
                    }
                } catch (Exception e) {
                    log.warn("获取COS处理结果失败，使用默认URL: {}", e.getMessage());
                    cosPatternUrl = cosClientConfig.getHost() + "/" + key;
                    cosThumbUrl = cosPatternUrl;
                }
                
                log.info("Bailian image uploaded to COS, URL: {}, fileSize: {} bytes", cosPatternUrl, fileSize);
            } else {
                // 下载失败，使用原始URL
                log.warn("Failed to download Bailian image, using original URL");
                cosPatternUrl = rawImageUrl;
                cosThumbUrl = StringUtils.isNotBlank(imageUrl) ? imageUrl : rawImageUrl;
            }
            
            // 保存到数据库
            Pattern pattern = new Pattern();
            pattern.setUserId(loginUser.getId());
            pattern.setPatternName(patternName);
            pattern.setDescription(request.getPrompt() != null ? request.getPrompt() : "AI生成的图案");
            String generationType = StringUtils.isNotBlank(request.getReferenceImageUrl())
                    ? GenerationTypeEnum.IMAGE_GENERATED.getValue()
                    : GenerationTypeEnum.TEXT_GENERATED.getValue();
            pattern.setGenerationType(generationType);
            pattern.setPatternUrl(cosPatternUrl);
            pattern.setThumbUrl(cosThumbUrl);
            pattern.setFileSize(fileSize);  // 保存文件大小
            pattern.setFileType(fileType);  // 自动获取的文件类型
            pattern.setStyle(style);
            pattern.setSeason(season);
            pattern.setTargetAudience(targetAudience);
            patternService.fillReviewParams(pattern, loginUser);
            
            // Save generation response metadata.
            pattern.setGenerationParams(JSON.toJSONString(request));
            
            // 保存到数据库
            boolean saved = patternService.save(pattern);
            ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR, "保存图案失败");

            log.info("Bailian image saved, patternId={}, userId={}, fileSize={} bytes, style={}, season={}, targetAudience={}",
                    pattern.getId(), loginUser.getId(), fileSize, style, season, targetAudience);

            // 清空图案列表缓存，确保前端能立即看到新图案
            clearPatternListCache();

            return ResultUtils.success(pattern.getId());
        } catch (Exception e) {
            log.error("Failed to save Bailian image", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存失败：" + e.getMessage());
        } finally {
            // 清理临时文件
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    log.warn("临时文件删除失败: {}", tempFile.getAbsolutePath());
                }
            }
        }
    }
    
    /**
     * 下载图片到临时文件
     *
     * @param imageUrl 图片URL
     * @return 下载结果（包含临时文件和contentType）
     */
    private DownloadResult downloadImageToTempFile(String imageUrl) {
        HttpURLConnection connection = null;
        InputStream inputStream = null;
        try {
            URL url = new URL(imageUrl);
            connection = (HttpURLConnection) url.openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(30000);  // 30秒连接超时
            connection.setReadTimeout(60000);     // 60秒读取超时
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            
            int responseCode = connection.getResponseCode();
            if (responseCode != HttpURLConnection.HTTP_OK) {
                log.error("下载图片失败，HTTP状态码: {}", responseCode);
                return null;
            }
            
            // 获取 Content-Type
            String contentType = connection.getContentType();
            if (StringUtils.isBlank(contentType)) {
                contentType = "image/png";  // 默认值
            }
            // 去除可能的字符集后缀，如 "image/png; charset=utf-8"
            if (contentType.contains(";")) {
                contentType = contentType.substring(0, contentType.indexOf(";")).trim();
            }
            
            // 根据 Content-Type 确定文件后缀
            String suffix = getFileExtensionFromContentType(contentType);
            
            inputStream = connection.getInputStream();
            File tempFile = File.createTempFile("bailian_image_", suffix);
            Files.copy(inputStream, tempFile.toPath(), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
            
            log.info("图片下载成功，临时文件: {}，大小: {} bytes，类型: {}", 
                    tempFile.getAbsolutePath(), tempFile.length(), contentType);
            return new DownloadResult(tempFile, contentType);
            
        } catch (Exception e) {
            log.error("下载图片异常: {}", e.getMessage());
            return null;
        } finally {
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException ignored) {}
            }
            if (connection != null) {
                connection.disconnect();
            }
        }
    }
    
    /**
     * 根据 Content-Type 获取文件后缀
     */
    private String getFileExtensionFromContentType(String contentType) {
        if (contentType == null) return ".png";
        switch (contentType.toLowerCase()) {
            case "image/jpeg":
            case "image/jpg":
                return ".jpg";
            case "image/gif":
                return ".gif";
            case "image/webp":
                return ".webp";
            case "image/bmp":
                return ".bmp";
            case "image/png":
            default:
                return ".png";
        }
    }
    
    /**
     * 下载结果封装类
     */
    private static class DownloadResult {
        final File file;
        final String contentType;
        
        DownloadResult(File file, String contentType) {
            this.file = file;
            this.contentType = contentType;
        }
    }
    
    /**
     * Blend（垫图/混合）接口
     *
     * @param request Blend请求参数
     * @return 生成结果
     */
    @PostMapping("/blend")
    @Operation(summary = "Blend垫图/混合")
    public BaseResponse<MJImagineVO> blend(@RequestBody MJBlendRequest request) {
        // 参数校验
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(request.getImageUrls() == null || request.getImageUrls().isEmpty(), 
                ErrorCode.PARAMS_ERROR, "图片URL列表不能为空");
        ThrowUtils.throwIf(request.getImageUrls().size() < 2 || request.getImageUrls().size() > 5, 
                ErrorCode.PARAMS_ERROR, "图片数量必须在2-5张之间");
        
        try {
            MJImagineVO response = bailianImageClient.blend(request);
            
            // 检查是否成功
            if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
                log.error("Bailian blend failed, response: {}", response);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Blend失败");
            }
            
            return ResultUtils.success(response);
        } catch (IOException e) {
            log.error("Bailian blend API exception", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Blend失败：" + e.getMessage());
        }
    }
    

    
    /**
     * AI 扩写提示词
     * 将用户输入的简短描述扩展为丰富、专业的服装图案描述
     *
     * @param prompt 用户输入的简短描述
     * @return 扩写后的详细描述
     */
    @GetMapping("/expand")
    @Operation(summary = "AI扩写提示词")
    public BaseResponse<String> expandPrompt(@RequestParam("prompt") String prompt) {
        // 参数校验
        ThrowUtils.throwIf(StringUtils.isBlank(prompt), ErrorCode.PARAMS_ERROR, "提示词不能为空");
        ThrowUtils.throwIf(prompt.length() < 2, ErrorCode.PARAMS_ERROR, "提示词至少需要2个字");
        
        try {
            // 调用 AI 扩写服务
            String expandedPrompt = promptTranslateService.expandPrompt(prompt);
            
            return ResultUtils.success(expandedPrompt);
        } catch (Exception e) {
            log.error("AI扩写失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "扩写失败：" + e.getMessage());
        }
    }
    
    /**
     * 清空图案列表缓存
     */
    private void clearPatternListCache() {
        try {
            // 清空本地 Caffeine 缓存（使用 PatternController 的静态缓存）
            PatternController.LOCAL_CACHE.invalidateAll();

            // 清空 Redis 中的图案列表缓存
            java.util.Set<String> keys = stringRedisTemplate.keys("xhs_pattern:listPictureVOByPage:*");
            if (keys != null && !keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
            }
            log.info("图案缓存已清除");
        } catch (Exception e) {
            log.warn("缓存清理失败，不影响主流程", e);
        }
    }

    private MJGenerateTaskVO toTaskVO(MJGenerateTaskInfo taskInfo) {
        MJGenerateTaskVO taskVO = new MJGenerateTaskVO();
        taskVO.setTaskId(taskInfo.getTaskId());
        taskVO.setStatus(taskInfo.getStatus());
        taskVO.setResult(taskInfo.getResult());
        taskVO.setErrorMessage(taskInfo.getErrorMessage());
        taskVO.setCreateTime(taskInfo.getCreateTime());
        taskVO.setUpdateTime(taskInfo.getUpdateTime());
        return taskVO;
    }
}

