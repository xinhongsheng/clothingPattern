package com.xhs.clothingpatternbackend.sdk.dashscope;
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
import com.xhs.clothingpatternbackend.config.CosClientConfig;
import com.xhs.clothingpatternbackend.config.TongYiConfig;
import com.xhs.clothingpatternbackend.exception.BusinessException;
import com.xhs.clothingpatternbackend.exception.ErrorCode;
import com.xhs.clothingpatternbackend.utils.CosUtils;
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

    @Resource
    private CosUtils cosUtils;

    @Resource
    private CosClientConfig cosClientConfig;

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
        String promptTemplate = "设计一个可直接用于服装印刷的高质量图案。"+
        "核心要求："+
        "设计风格：现代时尚，符合当前流行趋势，具备商业应用价值"+
        "技术规格： 分辨率：300 DPI以上 格式：PNG透明背景 色彩模式：CMYK/RGB双模式适配"+
        "设计元素：%s"+
        "视觉特征： 清晰锐利的边缘线条,协调的色彩搭配（建议使用互补色或类比色方案）,适当的负空间处理 ,良好的视觉平衡"+
        "布局方案： 提供平铺重复版本（无缝衔接）,提供独立中心图案版本 ,考虑服装剪裁的适配性"+
        "专业要求： 避免过于复杂的细节,确保不同尺寸下的可识别性, 适配各种面料材质,印刷友好的色彩对比度"+
        "请确保设计兼具艺术美感与商业实用性，符合大规模印刷生产标准。";
        
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
            parameters.put("size", StrUtil.isNotBlank(size) ? size : "1328*1328");

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
        File uploadedTempFile = null;
        try {
            MultiModalConversation conv = new MultiModalConversation();

            // 处理base64图片：上传到COS获取URL（避免API长度限制）
            if (referenceImageUrl.startsWith("data:image")) {
                log.info("检测到base64图片，长度: {} 字符，准备上传到COS", referenceImageUrl.length());
                
                try {
                    // 1. 解码base64并保存为临时文件
                    String base64Data = referenceImageUrl;
                    if (base64Data.contains(",")) {
                        base64Data = base64Data.split(",")[1];
                    }
                    byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
                    
                    uploadedTempFile = File.createTempFile("ref_image_", ".png");
                    java.nio.file.Files.write(uploadedTempFile.toPath(), imageBytes);
                    log.info("Base64图片已解码为临时文件: {}, 大小: {} bytes", 
                            uploadedTempFile.getAbsolutePath(), uploadedTempFile.length());
                    
                    // 2. 上传到COS
                    String key = "temp/reference/" + System.currentTimeMillis() + "_" + RandomUtil.randomString(8) + ".png";
                    cosUtils.putObject(key, uploadedTempFile);
                    
                    // 3. 使用COS URL替换base64
                    referenceImageUrl = cosClientConfig.getHost() + "/" + key;
                    log.info("图片已上传到COS，URL: {}", referenceImageUrl);
                    
                } catch (Exception e) {
                    log.error("Base64图片上传到COS失败", e);
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, 
                            "参考图片处理失败：" + e.getMessage());
                }
            }

            // 确保有描述文本，如果没有则使用默认提示词
            String textPrompt = StrUtil.isNotBlank(description) 
                    ? description 
                    : "基于参考图片，生成一个适合服装印花的精美图案设计";

            log.info("参考图片URL: {}, 描述: {}", referenceImageUrl, textPrompt);

            List<Map<String, Object>> content = new ArrayList<>();
            content.add(Collections.singletonMap("image", referenceImageUrl));
            content.add(Collections.singletonMap("text", textPrompt));

            MultiModalMessage userMessage = MultiModalMessage.builder()
                    .role(Role.USER.getValue())
                    .content(content)
                    .build();

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("watermark", false);
            parameters.put("size", StrUtil.isNotBlank(size) ? size : "1328*1328");

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
        } finally {
            // 清理上传的临时文件
            if (uploadedTempFile != null && uploadedTempFile.exists()) {
                deleteTempFile(uploadedTempFile);
            }
        }
    }
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
     * 注意：调用者负责删除返回的临时文件
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