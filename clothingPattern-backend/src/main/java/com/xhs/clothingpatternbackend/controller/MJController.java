package com.xhs.clothingpatternbackend.controller;

import com.alibaba.fastjson2.JSON;
import com.xhs.clothingpatternbackend.common.BaseResponse;
import com.xhs.clothingpatternbackend.common.ResultUtils;
import com.xhs.clothingpatternbackend.exception.BusinessException;
import com.xhs.clothingpatternbackend.exception.ErrorCode;
import com.xhs.clothingpatternbackend.exception.ThrowUtils;
import com.xhs.clothingpatternbackend.model.dto.mj.MJActionRequest;
import com.xhs.clothingpatternbackend.model.dto.mj.MJBlendRequest;
import com.xhs.clothingpatternbackend.model.dto.mj.MJImagineRequest;
import com.xhs.clothingpatternbackend.model.vo.MJImagineVO;
import com.xhs.clothingpatternbackend.model.entity.Pattern;
import com.xhs.clothingpatternbackend.model.entity.User;
import com.xhs.clothingpatternbackend.model.enums.AuditStatusEnum;
import com.xhs.clothingpatternbackend.model.enums.GenerationTypeEnum;
import com.xhs.clothingpatternbackend.sdk.mj.MJGenImage;
import com.xhs.clothingpatternbackend.service.PatternService;
import com.xhs.clothingpatternbackend.service.PromptTranslateService;
import com.xhs.clothingpatternbackend.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-11-28
 * @Description: Midjourney图片生成接口
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/mj")
@Tag(name = "Midjourney接口")
public class MJController {
    
    @Resource
    private MJGenImage mjGenImage;
    
    @Resource
    private PatternService patternService;
    
    @Resource
    private UserService userService;
    
    @Resource
    private PromptTranslateService promptTranslateService;
    
    /**
     * 生成图片（Imagine）- 仅返回MJ响应，不保存到数据库
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

            // 调用Midjourney API
            MJImagineVO response = mjGenImage.imagine(request);

            // 检查是否成功
            if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
                log.error("Midjourney图片生成失败，响应：{}", response);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片生成失败");
            }

            return ResultUtils.success(response);
        } catch (IOException e) {
            log.error("调用Midjourney API异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片生成失败：" + e.getMessage());
        }
    }
    
//    /**
//     * 生成图片并保存到图案库
//     *
//     * @param request 请求参数
//     * @param httpRequest HTTP请求
//     * @return 保存的图案ID
//     */
//    @PostMapping("/generate")
//    @Operation(summary = "生成图片并保存")
//    public BaseResponse<Long> generateAndSave(@RequestBody MJImagineRequest request,
//                                               HttpServletRequest httpRequest)  {
//        // 参数校验
//        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
//        String originalPrompt = request.getPrompt();
//        ThrowUtils.throwIf(StringUtils.isBlank(originalPrompt), ErrorCode.PARAMS_ERROR, "提示词不能为空");
//
//        // 获取登录用户
//        User loginUser = userService.getLoginUser(httpRequest);
//
//        // 获取额外字段
//        String style = request.getStyle();
//        String season = request.getSeason();
//        String targetAudience = request.getTargetAudience();
//
//        try {
//            // 翻译并优化 prompt（添加服装专业前缀，包含风格、季节、受众信息）
//            log.info("原始提示词：{}, 风格：{}, 季节：{}, 受众：{}",
//                    originalPrompt, style, season, targetAudience);
//            String optimizedPrompt = promptTranslateService.translateAndOptimize(
//                    originalPrompt,
//                    style,
//                    season,
//                    targetAudience
//            );
//            log.info("优化后提示词：{}", optimizedPrompt);
//
//            // 使用优化后的 prompt
//            request.setPrompt(optimizedPrompt);
//
//            // 调用Midjourney API生成图片
//            MJImagineVO mjResponse = mjGenImage.imagine(request);
//
//            // 检查是否成功
//            if (mjResponse == null || !Boolean.TRUE.equals(mjResponse.getSuccess())) {
//                log.error("Midjourney图片生成失败，响应：{}", mjResponse);
//                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片生成失败");
//            }
//
//            // 打印完整响应用于调试
//            log.info("MJ响应详情 - taskId: {}, imageId: {}, rawImageUrl: {}, imageUrl: {}",
//                mjResponse.getTaskId(), mjResponse.getImageId(),
//                mjResponse.getRawImageUrl(), mjResponse.getImageUrl());
//
//            // 检查必要的字段
//            String rawImageUrl = mjResponse.getRawImageUrl();
//            String imageUrl = mjResponse.getImageUrl();
//
//            // 如果原始图片URL为空，使用缩略图URL
//            if (StringUtils.isBlank(rawImageUrl)) {
//                rawImageUrl = imageUrl;
//            }
//
//            // 如果两个URL都为空，抛出异常
//            ThrowUtils.throwIf(StringUtils.isBlank(rawImageUrl), ErrorCode.SYSTEM_ERROR,
//                "图片URL为空，生成失败");
//
//            // 保存到数据库
//            Pattern pattern = new Pattern();
//            pattern.setUserId(loginUser.getId());
//            pattern.setPatternName("MJ-" + originalPrompt.substring(0, Math.min(originalPrompt.length(), 30))); // 使用原始提示词作为名称
//            pattern.setDescription(originalPrompt); // 保存原始提示词（用户输入的）
//            pattern.setGenerationType(GenerationTypeEnum.MJ_GENERATED.getValue());
//            pattern.setPatternUrl(rawImageUrl); // 使用原始图片URL
//            pattern.setThumbUrl(StringUtils.isNotBlank(imageUrl) ? imageUrl : rawImageUrl); // 使用缩略图URL，如果为空则使用原始URL
//            pattern.setStyle(style);
//            pattern.setSeason(season);
//            pattern.setTargetAudience(targetAudience);
//
//            //如果是管理员则自动通过
//            if (userService.isAdmin(loginUser)) {
//                pattern.setAuditStatus(AuditStatusEnum.APPROVED.getValue());
//            }else{
//                pattern.setAuditStatus(AuditStatusEnum.PENDING.getValue());
//            }
//
//            // 将MJ响应信息保存到generationParams字段
//            pattern.setGenerationParams(JSON.toJSONString(mjResponse));
//
//            // 保存到数据库
//            boolean saved = patternService.save(pattern);
//            ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR, "保存图案失败");
//
//            log.info("MJ图片生成并保存成功，图案ID：{}，用户ID：{}，风格：{}，季节：{}，受众：{}",
//                    pattern.getId(), loginUser.getId(), style, season, targetAudience);
//            log.info("1");
//            return ResultUtils.success(pattern.getId());
//        } catch (IOException e) {
//            log.error("调用Midjourney API异常", e);
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片生成失败：" + e.getMessage());
//        }
//    }
    
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
        
        ThrowUtils.throwIf(StringUtils.isBlank(taskId), ErrorCode.PARAMS_ERROR, "任务ID不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(imageId), ErrorCode.PARAMS_ERROR, "图片ID不能为空");
        ThrowUtils.throwIf(StringUtils.isBlank(action), ErrorCode.PARAMS_ERROR, "动作类型不能为空");
        
        try {
            // 调用Midjourney API
            MJImagineVO response = mjGenImage.executeAction(taskId, imageId, action);
            
            // 检查是否成功
            if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
                log.error("Midjourney动作执行失败，响应：{}", response);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "动作执行失败");
            }
            
            return ResultUtils.success(response);
        } catch (IOException e) {
            log.error("调用Midjourney API异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "动作执行失败：" + e.getMessage());
        }
    }
    
    /**
     * 保存 MJ 生成的图片到数据库
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
                patternName = "MJ-图案-" + System.currentTimeMillis();
            }
            
            // 获取额外字段
            String style = request.getStyle();
            String season = request.getSeason();
            String targetAudience = request.getTargetAudience();
            
            // 保存到数据库
            Pattern pattern = new Pattern();
            pattern.setUserId(loginUser.getId());
            pattern.setPatternName(patternName);
            pattern.setDescription(request.getPrompt() != null ? request.getPrompt() : "Midjourney生成的图案");
            pattern.setGenerationType(GenerationTypeEnum.MJ_GENERATED.getValue());
            pattern.setPatternUrl(rawImageUrl);
            pattern.setThumbUrl(StringUtils.isNotBlank(imageUrl) ? imageUrl : rawImageUrl);
            pattern.setStyle(style);
            pattern.setSeason(season);
            pattern.setTargetAudience(targetAudience);
            
            // 如果是管理员则自动通过
            if (userService.isAdmin(loginUser)) {
                pattern.setAuditStatus(AuditStatusEnum.APPROVED.getValue());
            } else {
                pattern.setAuditStatus(AuditStatusEnum.PENDING.getValue());
            }
            
            // 将MJ响应信息保存到generationParams字段
            pattern.setGenerationParams(JSON.toJSONString(request));
            
            // 保存到数据库
            boolean saved = patternService.save(pattern);
            ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR, "保存图案失败");
            
            log.info("MJ图片保存成功，图案ID：{}，用户ID：{}，风格：{}，季节：{}，受众：{}", 
                    pattern.getId(), loginUser.getId(), style, season, targetAudience);
            
            return ResultUtils.success(pattern.getId());
        } catch (Exception e) {
            log.error("保存MJ图片失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "保存失败：" + e.getMessage());
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
            // 调用Midjourney Blend API
            MJImagineVO response = mjGenImage.blend(request);
            
            // 检查是否成功
            if (response == null || !Boolean.TRUE.equals(response.getSuccess())) {
                log.error("Midjourney Blend失败，响应：{}", response);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Blend失败");
            }
            
            return ResultUtils.success(response);
        } catch (IOException e) {
            log.error("调用Midjourney Blend API异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "Blend失败：" + e.getMessage());
        }
    }
    
    /**
     * 测试接口
     *
     * @return 测试结果
     */
    @GetMapping("/test")
    @Operation(summary = "测试接口")
    public BaseResponse<String> test() {
        return ResultUtils.success("Midjourney API服务正常");
    }
}

