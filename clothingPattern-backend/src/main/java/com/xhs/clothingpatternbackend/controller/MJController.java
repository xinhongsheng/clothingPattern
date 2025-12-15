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
import com.xhs.clothingpatternbackend.model.entity.PatternVector;
import com.xhs.clothingpatternbackend.model.vo.MJImagineVO;
import com.xhs.clothingpatternbackend.model.entity.Pattern;
import com.xhs.clothingpatternbackend.model.entity.User;
import com.xhs.clothingpatternbackend.model.enums.AuditStatusEnum;
import com.xhs.clothingpatternbackend.model.enums.GenerationTypeEnum;
import com.xhs.clothingpatternbackend.sdk.djl.EmbeddingService;
import com.xhs.clothingpatternbackend.sdk.mj.MJGenImage;
import com.xhs.clothingpatternbackend.service.PatternService;
import com.xhs.clothingpatternbackend.service.PatternVectorService;
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
import java.util.*;
import java.util.stream.Collectors;

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

    @Resource
    private EmbeddingService embeddingService;

    @Resource
    private ObjectMapper objectMapper; // Jackson 工具，用于 String 转 float[]
    
    @Resource
    private PatternVectorService patternVectorService;
    
    @Resource
    private PatternMapper patternMapper;
    
    @Resource
    private StringRedisTemplate stringRedisTemplate;
    
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
            patternService.fillReviewParams(pattern, loginUser);
            
            // 将MJ响应信息保存到generationParams字段
            pattern.setGenerationParams(JSON.toJSONString(request));
            
            // 保存到数据库
            boolean saved = patternService.save(pattern);
            ThrowUtils.throwIf(!saved, ErrorCode.SYSTEM_ERROR, "保存图案失败");
            //保存向量数据
            float[] vector = embeddingService.vectorize(request.getPrompt());
            // 3. 保存向量到 pattern_vector 表
            PatternVector pv = new PatternVector();
            pv.setPatternId(pattern.getId());
            try {
                // 将 float[] 转为 JSON 字符串存储
                pv.setVectorData(objectMapper.writeValueAsString(vector));
            } catch (JsonProcessingException e) {
                e.printStackTrace();
            }
            log.info("MJ图片保存成功，图案ID：{}，用户ID：{}，风格：{}，季节：{}，受众：{}",
                    pattern.getId(), loginUser.getId(), style, season, targetAudience);

            patternVectorService.save(pv);

            // 清空图案列表缓存，确保前端能立即看到新图案
            clearPatternListCache();

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
     * 基于提示词的智能图案推荐
     * 使用向量相似度算法查找最相似的图案
     *
     * @param prompt 用户输入的提示词
     * @return 推荐的图案列表（Top 10）
     */
    @GetMapping("/recommend")
    @Operation(summary = "智能图案推荐")
    public BaseResponse<List<Pattern>> recommendPatterns(@RequestParam("prompt") String prompt) {
        // 参数校验
        ThrowUtils.throwIf(StringUtils.isBlank(prompt), ErrorCode.PARAMS_ERROR, "提示词不能为空");
        
        try {
            // 1. 利用 Java 本地 AI 计算当前提示词的向量
            float[] currentVector = embeddingService.vectorize(prompt);
            
            // 向量为空则无法推荐
            if (currentVector == null || currentVector.length == 0) {
                log.warn("向量化失败，无法进行推荐");
                return ResultUtils.success(new ArrayList<>());
            }
            
            // 2. 查出库里所有的向量数据 (只查 pattern_vector 表，速度快)
            List<PatternVector> allVectors = patternVectorService.list();
            
            // 如果库里没有向量数据，返回空列表
            if (allVectors == null || allVectors.isEmpty()) {
                log.info("向量库为空，无法推荐");
                return ResultUtils.success(new ArrayList<>());
            }
            
            // 3. 在内存中计算相似度，并找出 Top 10 的 ID
            List<Long> topPatternIds = allVectors.stream()
                    .map(pv -> {
                        try {
                            // 将 JSON 字符串 "[0.1, ...]" 转为 float[]
                            float[] dbVector = objectMapper.readValue(
                                    pv.getVectorData().toString(), float[].class);
                            double score = VectorUtils.cosineSimilarity(currentVector, dbVector);
                            return new AbstractMap.SimpleEntry<>(pv.getPatternId(), score);
                        } catch (JsonProcessingException e) {
                            log.warn("向量数据解析失败，patternId: {}", pv.getPatternId());
                            return null;
                        } catch (IllegalArgumentException e) {
                            log.warn("向量维度不匹配，patternId: {}", pv.getPatternId());
                            return null;
                        }
                    })
                    .filter(Objects::nonNull)
                    .filter(entry -> entry.getValue() > 0.3) // 过滤相似度过低的结果
                    .sorted((a, b) -> Double.compare(b.getValue(), a.getValue())) // 按相似度降序
                    .limit(10) // 取前10个
                    .map(Map.Entry::getKey) // 只取 ID
                    .collect(Collectors.toList());
            
            // 4. 如果没有推荐结果，直接返回空
            if (topPatternIds.isEmpty()) {
                log.info("未找到相似图案，提示词: {}", prompt);
                return ResultUtils.success(new ArrayList<>());
            }
            
            // 5. 根据 ID 去 pattern 主表查详细信息
            List<Pattern> patterns = patternMapper.selectBatchIds(topPatternIds);
            
            // 6. 按照原始的相似度排序顺序返回（因为 selectBatchIds 不保证顺序）
            Map<Long, Pattern> patternMap = patterns.stream()
                    .collect(Collectors.toMap(Pattern::getId, p -> p));
            List<Pattern> sortedPatterns = topPatternIds.stream()
                    .map(patternMap::get)
                    .filter(Objects::nonNull)
                    .collect(Collectors.toList());
            
            log.info("推荐成功，提示词: {}, 推荐数量: {}", prompt, sortedPatterns.size());
            return ResultUtils.success(sortedPatterns);
            
        } catch (Exception e) {
            log.error("图案推荐失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "推荐失败：" + e.getMessage());
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
}

