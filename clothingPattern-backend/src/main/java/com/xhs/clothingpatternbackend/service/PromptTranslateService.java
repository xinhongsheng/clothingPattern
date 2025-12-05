package com.xhs.clothingpatternbackend.service;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.xhs.clothingpatternbackend.config.TongYiConfig;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.util.Arrays;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-11-28
 * @Description: Prompt 翻译服务 - 将中文提示词翻译成英文并优化为服装图案专业描述
 * @Version: 1.0
 */
@Service
@Slf4j
public class PromptTranslateService {
    
    @Resource
    private TongYiConfig tongYiConfig;
    
    /**
     * 服装图案专业前缀模板
     */
    private static final String CLOTHING_PATTERN_PREFIX = 
            "Professional clothing pattern design, fashion textile print, ";
    
    /**
     * 翻译系统提示词
     */
    private static final String TRANSLATE_SYSTEM_PROMPT = """
             设计一个可直接用于服装印刷的高质量单纯图案（仅呈现图案本身，不包含任何服装载体元素，如衣物款式、剪裁轮廓、穿着效果等）。
             核心要求：
             设计风格：现代时尚，符合当前流行趋势，具备商业应用价值
             技术规格：分辨率：300 DPI 以上 格式：PNG 透明背景 色彩模式：CMYK/RGB 双模式适配
             设计元素：视觉特征：清晰锐利的边缘线条，协调的色彩搭配（建议使用互补色或类比色方案），适当的负空间处理，良好的视觉平衡
             布局方案：提供平铺重复版本（无缝衔接），提供独立中心图案版本，图案结构适配服装剪裁场景（仅优化图案适配性，不呈现服装）
             专业要求：避免过于复杂的细节，确保不同尺寸下的可识别性，适配各种面料材质，印刷友好的色彩对比度
             请确保设计仅聚焦图案本身，兼具艺术美感与商业实用性，完全符合大规模服装印刷生产标准，全程不出现任何服装相关载体呈现。
            
            示例：
            输入："可爱的水豚图案"
            输出：cute capybara pattern
            
            输入："复古花卉图案，适合夏季连衣裙"
            输出：vintage floral pattern for summer dress
            
            输入："简约几何线条，黑白配色"
            输出：minimalist geometric lines, black and white color scheme
            """;
    
    /**
     * 将用户输入的 prompt 翻译成英文并添加服装专业前缀
     *
     * @param userPrompt 用户输入的提示词（中文或英文）
     * @return 优化后的英文提示词
     */
    
    /**
     * 将用户输入的 prompt 翻译成英文并添加服装专业前缀（包含风格、季节、受众信息）
     *
     * @param userPrompt 用户输入的提示词（中文或英文）
     * @param style 图案风格（如：简约、可爱、复古等）
     * @param season 适用季节（如：春季、夏季等）
     * @param targetAudience 目标受众（如：儿童、成人等）
     * @return 优化后的英文提示词
     */
    public String translateAndOptimize(String userPrompt, String style, String season, String targetAudience) {
        if (StringUtils.isBlank(userPrompt)) {
            return CLOTHING_PATTERN_PREFIX + "beautiful clothing pattern design";
        }
        
        try {
            // 1. 组合额外字段到 prompt
            StringBuilder combinedPromptBuilder = new StringBuilder(userPrompt);
            
            if (StringUtils.isNotBlank(style)) {
                combinedPromptBuilder.append(", ").append(style).append(" style");
            }
            if (StringUtils.isNotBlank(season)) {
                combinedPromptBuilder.append(", for ").append(season);
            }
            if (StringUtils.isNotBlank(targetAudience)) {
                combinedPromptBuilder.append(", target audience: ").append(targetAudience);
            }
            
            String combinedPrompt = combinedPromptBuilder.toString();
            log.info("组合额外字段后的提示词：{}", combinedPrompt);
            
            // 2. 判断是否需要翻译（简单判断：如果包含中文字符则需要翻译）
            boolean needsTranslation = containsChinese(combinedPrompt);
            if (needsTranslation) {
                log.info("检测到中文提示词，开始翻译：{}", combinedPrompt);
                String translatedPrompt = translateToEnglish(combinedPrompt);
                log.info("翻译结果：{}", translatedPrompt);
                // 添加服装专业前缀
                String finalPrompt = CLOTHING_PATTERN_PREFIX + translatedPrompt;
                log.info("最终提示词：{}", finalPrompt);
                
                return finalPrompt;
            } else {
                log.info("检测到英文提示词，直接添加专业前缀：{}", combinedPrompt);
                // 如果已经是英文，直接添加前缀
                String finalPrompt = CLOTHING_PATTERN_PREFIX + combinedPrompt;
                log.info("最终提示词：{}", finalPrompt);
                
                return finalPrompt;
            }
        } catch (Exception e) {
            log.error("翻译提示词失败，使用原始输入", e);
            // 如果翻译失败，至少添加前缀
            return CLOTHING_PATTERN_PREFIX + userPrompt;
        }
    }
    
    /**
     * 使用通义千问翻译中文到英文
     *
     * @param chineseText 中文文本
     * @return 英文翻译
     */
    private String translateToEnglish(String chineseText) throws NoApiKeyException, InputRequiredException {
        Generation gen = new Generation();
        
        // 构建消息
        Message systemMessage = Message.builder()
                .role(Role.SYSTEM.getValue())
                .content(TRANSLATE_SYSTEM_PROMPT)
                .build();
        
        Message userMessage = Message.builder()
                .role(Role.USER.getValue())
                .content(chineseText)
                .build();
        
        // 构建请求参数
        GenerationParam param = GenerationParam.builder()
                .apiKey(tongYiConfig.getDashscopeApiKey())
                .model("qwen-plus")  // 使用 qwen-plus 模型
                .messages(Arrays.asList(systemMessage, userMessage))
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .temperature(0.3f)  // 降低温度，使翻译更稳定
                .maxTokens(200)    // 限制输出长度
                .build();
        
        // 调用 API
        GenerationResult result = gen.call(param);
        
        // 提取翻译结果
        String translatedText = result.getOutput().getChoices().get(0).getMessage().getContent();
        
        // 清理结果（去除可能的引号、换行等）
        translatedText = translatedText.trim()
                .replaceAll("^[\"']|[\"']$", "")  // 去除首尾引号
                .replaceAll("\\n", " ")            // 替换换行为空格
                .replaceAll("\\s+", " ");          // 合并多个空格
        
        return translatedText;
    }
    
    /**
     * 判断字符串是否包含中文字符
     *
     * @param text 待检测的文本
     * @return true 如果包含中文
     */
    private boolean containsChinese(String text) {
        if (StringUtils.isBlank(text)) {
            return false;
        }
        
        // 使用正则表达式检测中文字符
        return text.matches(".*[\\u4e00-\\u9fa5]+.*");
    }
    
    /**
     * 仅添加服装专业前缀（不翻译）
     *
     * @param prompt 提示词
     * @return 添加前缀后的提示词
     */
    public String addClothingPrefix(String prompt) {
        if (StringUtils.isBlank(prompt)) {
            return CLOTHING_PATTERN_PREFIX + "beautiful clothing pattern design";
        }
        return CLOTHING_PATTERN_PREFIX + prompt;
    }
}

