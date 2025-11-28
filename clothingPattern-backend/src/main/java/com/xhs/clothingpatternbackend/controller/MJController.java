package com.xhs.clothingpatternbackend.controller;

import com.alibaba.fastjson2.JSON;
import com.xhs.clothingpatternbackend.common.BaseResponse;
import com.xhs.clothingpatternbackend.common.ResultUtils;
import com.xhs.clothingpatternbackend.exception.BusinessException;
import com.xhs.clothingpatternbackend.exception.ErrorCode;
import com.xhs.clothingpatternbackend.exception.ThrowUtils;
import com.xhs.clothingpatternbackend.model.dto.mj.MJActionRequest;
import com.xhs.clothingpatternbackend.model.dto.mj.MJImagineRequest;
import com.xhs.clothingpatternbackend.model.vo.MJImagineResponse;
import com.xhs.clothingpatternbackend.model.entity.Pattern;
import com.xhs.clothingpatternbackend.model.entity.User;
import com.xhs.clothingpatternbackend.model.enums.AuditStatusEnum;
import com.xhs.clothingpatternbackend.model.enums.GenerationTypeEnum;
import com.xhs.clothingpatternbackend.sdk.mj.MJGenImage;
import com.xhs.clothingpatternbackend.service.PatternService;
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
    
    /**
     * 生成图片（Imagine）- 仅返回MJ响应，不保存到数据库
     *
     * @param request 请求参数
     * @return 生成结果
     */
    @PostMapping("/imagine")
    @Operation(summary = "生成图片（不保存）")
    public BaseResponse<MJImagineResponse> imagine(@RequestBody MJImagineRequest request) {
        // 参数校验
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        String prompt = request.getPrompt();
        ThrowUtils.throwIf(StringUtils.isBlank(prompt), ErrorCode.PARAMS_ERROR, "提示词不能为空");
        
        try {
            // 调用Midjourney API
            MJImagineResponse response = mjGenImage.imagine(request);
            
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
    
    /**
     * 生成图片并保存到图案库
     *
     * @param request 请求参数
     * @param httpRequest HTTP请求
     * @return 保存的图案ID
     */
    @PostMapping("/generate")
    @Operation(summary = "生成图片并保存")
    public BaseResponse<Long> generateAndSave(@RequestBody MJImagineRequest request, 
                                               HttpServletRequest httpRequest) throws IOException {
        // 参数校验
        ThrowUtils.throwIf(request == null, ErrorCode.PARAMS_ERROR);
        String prompt = request.getPrompt();
        ThrowUtils.throwIf(StringUtils.isBlank(prompt), ErrorCode.PARAMS_ERROR, "提示词不能为空");
        
        // 获取登录用户
        User loginUser = userService.getLoginUser(httpRequest);
        
        try {
            // 调用Midjourney API生成图片
            MJImagineResponse mjResponse = mjGenImage.imagine(request);
            
            // 检查是否成功
            if (mjResponse == null || !Boolean.TRUE.equals(mjResponse.getSuccess())) {
                log.error("Midjourney图片生成失败，响应：{}", mjResponse);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片生成失败");
            }
            
            // 打印完整响应用于调试
            log.info("MJ响应详情 - taskId: {}, imageId: {}, rawImageUrl: {}, imageUrl: {}", 
                mjResponse.getTaskId(), mjResponse.getImageId(), 
                mjResponse.getRawImageUrl(), mjResponse.getImageUrl());
            
            // 检查必要的字段
            String rawImageUrl = mjResponse.getRawImageUrl();
            String imageUrl = mjResponse.getImageUrl();
            
            // 如果原始图片URL为空，使用缩略图URL
            if (StringUtils.isBlank(rawImageUrl)) {
                rawImageUrl = imageUrl;
            }
            
            // 如果两个URL都为空，抛出异常
            ThrowUtils.throwIf(StringUtils.isBlank(rawImageUrl), ErrorCode.SYSTEM_ERROR, 
                "图片URL为空，生成失败");
            
            // 保存到数据库
            Pattern pattern = new Pattern();
            pattern.setUserId(loginUser.getId());
            pattern.setPatternName("MJ-" + prompt.substring(0, Math.min(prompt.length(), 30))); // 截取前30个字符作为名称
            pattern.setDescription(prompt);
            pattern.setGenerationType(GenerationTypeEnum.MJ_GENERATED.getValue());
            pattern.setPatternUrl(rawImageUrl); // 使用原始图片URL
            pattern.setThumbUrl(StringUtils.isNotBlank(imageUrl) ? imageUrl : rawImageUrl); // 使用缩略图URL，如果为空则使用原始URL
            //如果是管理员则自动通过
            if (userService.isAdmin(loginUser)) {
                pattern.setAuditStatus(AuditStatusEnum.APPROVED.getValue());
            }else{
                pattern.setAuditStatus(AuditStatusEnum.PENDING.getValue());
            }
//            pattern.setAuditStatus(AuditStatusEnum.PENDING.getValue()); // 待审核
            
            // 将MJ响应信息保存到generationParams字段
            pattern.setGenerationParams(JSON.toJSONString(mjResponse));
            
            // 保存到数据库
            boolean saved = patternService.save(pattern);
            ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR, "保存图案失败");
            
            log.info("MJ图片生成并保存成功，图案ID：{}，用户ID：{}", pattern.getId(), loginUser.getId());
            
            return ResultUtils.success(pattern.getId());
        } catch (IOException e) {
            log.error("调用Midjourney API异常", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片生成失败：" + e.getMessage());
        }
    }
    
    /**
     * 执行动作（如upsample、variation等）
     *
     * @param request 请求参数
     * @return 执行结果
     */
    @PostMapping("/action")
    @Operation(summary = "执行动作")
    public BaseResponse<MJImagineResponse> executeAction(@RequestBody MJActionRequest request) {
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
            MJImagineResponse response = mjGenImage.executeAction(taskId, imageId, action);
            
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

