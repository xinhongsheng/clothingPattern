package com.xhs.clothingpatternbackend.DashScopeSDK;
import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationOutput;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.alibaba.dashscope.utils.Constants;
import com.xhs.clothingpatternbackend.config.TongYiConfig;
import com.xhs.clothingpatternbackend.exception.BusinessException;
import com.xhs.clothingpatternbackend.exception.ErrorCode;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.*;

/**
 * Qwen图片生成服务
 */
@Service
@Slf4j
public class QwenImage {

    @Resource
    private TongYiConfig tongYiConfig;

    @PostConstruct
    public void init() {
        // 初始化API基础URL
        Constants.baseHttpApiUrl = "https://dashscope.aliyuncs.com/api/v1";
    }

    /**
     * 构建服装图案专用提示词
     * 将用户简单描述转换为专业的服装图案生成提示词
     */
    private String buildClothingPatternPrompt(String userDescription) {
        // 预设提示词模板：确保生成的是适合服装的图案设计
        String promptTemplate = "设计一个精美的服装图案。" +
                "要求：" +
                "1. 图案风格：现代、时尚、适合印刷在服装上；" +
                "2. 图案特点：清晰、对比度高、适合各种面料；" +
                "3. 设计元素：%s；" +
                "4. 色彩搭配：协调美观，适合服装设计；" +
                "5. 图案布局：可平铺重复或作为单一图案使用；" +
                "6. 输出要求：高清晰度，PNG格式，透明背景或纯色背景。";
        
        return String.format(promptTemplate, userDescription);
    }

    /**
     * 根据文字描述生成图片
     */
    public File generateImageByText(String description, String size, String negativePrompt, Boolean promptExtend) {
        try {
            MultiModalConversation conv = new MultiModalConversation();

            // 使用预设提示词模板构建专业的服装图案提示词
            String enhancedPrompt = buildClothingPatternPrompt(description);
            log.info("原始描述: {}", description);
            log.info("增强后的提示词: {}", enhancedPrompt);

            MultiModalMessage userMessage = MultiModalMessage.builder()
                    .role(Role.USER.getValue())
                    .content(Collections.singletonList(
                            Collections.singletonMap("text", enhancedPrompt)
                    ))
                    .build();

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("watermark", false);
            parameters.put("prompt_extend", promptExtend != null ? promptExtend : true);
            
            // 设置默认负面提示词，避免生成低质量或不适合服装的图案
            String defaultNegativePrompt = "模糊、低质量、变形、杂乱、水印、文字、人物脚部、人物手部、不完整、裁剪、过暴、过于复杂";
            String finalNegativePrompt = StrUtil.isNotBlank(negativePrompt) 
                    ? negativePrompt + ", " + defaultNegativePrompt 
                    : defaultNegativePrompt;
            parameters.put("negative_prompt", finalNegativePrompt);
            parameters.put("size", StrUtil.isNotBlank(size) ? size : "1024*1024");

            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(tongYiConfig.getDashscopeApiKey())
                    .model("qwen-image-plus")
                    .messages(Collections.singletonList(userMessage))
                    .parameters(parameters)
                    .build();

            MultiModalConversationResult result = conv.call(param);
            log.info("Qwen API response: {}", result);

            String imageUrl = extractImageUrl(result);
            if (StrUtil.isBlank(imageUrl)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI图片生成失败：未获取到图片URL");
            }

            return downloadImageToTempFile(imageUrl);
        } catch (ApiException | NoApiKeyException | UploadFileException e) {
            log.error("Qwen API调用失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI图片生成失败：" + e.getMessage());
        } catch (IOException e) {
            log.error("图片下载失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片下载失败：" + e.getMessage());
        }
    }

    /**
     * 根据参考图片生成图片
     */
    public File generateImageByReference(String referenceImageUrl, String description, String size) {
        try {
            MultiModalConversation conv = new MultiModalConversation();

            List<Map<String, Object>> content = new ArrayList<>();
            content.add(Collections.singletonMap("image", referenceImageUrl));
            if (StrUtil.isNotBlank(description)) {
                content.add(Collections.singletonMap("text", description));
            }

            MultiModalMessage userMessage = MultiModalMessage.builder()
                    .role(Role.USER.getValue())
                    .content(content)
                    .build();

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("watermark", false);
            parameters.put("size", StrUtil.isNotBlank(size) ? size : "1024*1024");

            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(tongYiConfig.getDashscopeApiKey())
                    .model("qwen-image-plus")
                    .messages(Collections.singletonList(userMessage))
                    .parameters(parameters)
                    .build();

            MultiModalConversationResult result = conv.call(param);
            log.info("Qwen API response (reference mode): {}", result);
            log.info("Response output: {}", result.getOutput());
            log.info("Response usage: {}", result.getUsage());

            String imageUrl = extractImageUrl(result);
            if (StrUtil.isBlank(imageUrl)) {
                log.error("Failed to extract image URL from response: {}", result);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI图片生成失败：未获取到图片URL");
            }

            return downloadImageToTempFile(imageUrl);
        } catch (ApiException | NoApiKeyException | UploadFileException e) {
            log.error("Qwen API调用失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI图片生成失败：" + e.getMessage());
        } catch (IOException e) {
            log.error("图片下载失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片下载失败：" + e.getMessage());
        }
    }
    /**
     * 从API响应中提取图片URL
     */
    private String extractImageUrl(MultiModalConversationResult result) {
        if (result == null) {
            log.error("MultiModalConversationResult is null");
            return null;
        }

        // 1. 获取Output对象（正确类型：MultiModalConversationOutput）
        MultiModalConversationOutput output = result.getOutput();
        if (output == null) {
            log.error("MultiModalConversationResult.getOutput() is null");
            return null;
        }

        // 2. 获取Choices列表（核心数据载体）
        List<MultiModalConversationOutput.Choice> choices = output.getChoices();
        if (choices == null || choices.isEmpty()) {
            log.error("Output.choices is null or empty");
            return null;
        }

        // 3. 遍历Choices，获取第一个有效Message（通常只有一个）
        for (MultiModalConversationOutput.Choice choice : choices) {
            MultiModalMessage message = choice.getMessage();
            if (message == null) {
                log.warn("Choice.message is null, skip");
                continue;
            }

            // 4. 获取Message的Content（List<Map<String, Object>>，存储图片/文字信息）
            List<Map<String, Object>> content = message.getContent();
            if (content == null || content.isEmpty()) {
                log.warn("Message.content is null or empty, skip");
                continue;
            }

            // 5. 遍历Content，提取图片URL（处理两种常见格式：image_url直接存URL / 嵌套image_url.url）
            for (Map<String, Object> contentItem : content) {
                // 格式1：contentItem = {"image_url": "https://xxx.png"}（部分场景直接返回URL）
                if (contentItem.containsKey("image_url")) {
                    Object imageUrlObj = contentItem.get("image_url");
                    if (imageUrlObj instanceof String) {
                        String imageUrl = (String) imageUrlObj;
                        log.info("Found image URL (format 1): {}", imageUrl);
                        return imageUrl;
                    }

                    // 格式2：contentItem = {"image_url": {"url": "https://xxx.png"}}（SDK标准嵌套格式）
                    if (imageUrlObj instanceof Map) {
                        Map<String, Object> imageUrlMap = (Map<String, Object>) imageUrlObj;
                        String imageUrl = (String) imageUrlMap.get("url");
                        if (StrUtil.isNotBlank(imageUrl)) {
                            log.info("Found image URL (format 2): {}", imageUrl);
                            return imageUrl;
                        }
                    }
                }

                // 兼容旧版SDK可能的"image"字段（若有需要可保留）
                if (contentItem.containsKey("image") && contentItem.get("image") instanceof String) {
                    String imageUrl = (String) contentItem.get("image");
                    log.info("Found image URL (compatible format): {}", imageUrl);
                    return imageUrl;
                }
            }
        }

        // 所有路径都未找到URL
        log.error("No image URL found in response. Result: {}", result);
        return null;
    }
    /**
     * 下载图片到临时文件
     */
    private File downloadImageToTempFile(String imageUrl) throws IOException {
        String uuid = RandomUtil.randomString(10);
        String fileName = "qwen_image_" + uuid;
        
        File tempFile = null;
        try {
            // 创建临时文件
            tempFile = File.createTempFile(fileName, ".png");
            
            // 下载图片到临时文件
            try (InputStream in = new URL(imageUrl).openStream();
                 FileOutputStream out = new FileOutputStream(tempFile)) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = in.read(buffer)) != -1) {
                    out.write(buffer, 0, bytesRead);
                }
            }
            
            log.info("图片下载成功: {}, 大小: {} bytes", tempFile.getAbsolutePath(), tempFile.length());
            return tempFile;
        } catch (IOException e) {
            // 如果下载失败，删除临时文件
            deleteTempFile(tempFile);
            throw e;
        }
    }
    
    /**
     * 删除临时文件
     */
    private void deleteTempFile(File file) {
        if (file != null && file.exists()) {
            boolean deleted = file.delete();
            if (deleted) {
                log.debug("临时文件已删除: {}", file.getAbsolutePath());
            } else {
                log.warn("临时文件删除失败: {}", file.getAbsolutePath());
            }
        }
    }
}