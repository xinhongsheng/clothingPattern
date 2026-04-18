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
public class  QwenImage {

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
        String promptTemplate = "设计一个可直接用于服装印刷的高质量单纯图案（仅呈现图案本身，不包含任何服装载体元素，如衣物款式、剪裁轮廓、穿着效果等）。"+
                "核心要求："+
                "设计风格：现代时尚，符合当前流行趋势，具备商业应用价值"+
                "技术规格：分辨率：300 DPI 以上 格式：PNG 透明背景 色彩模式：CMYK/RGB 双模式适配"+"设计元素：%s"
                +"视觉特征：清晰锐利的边缘线条，协调的色彩搭配（建议使用互补色或类比色方案），适当的负空间处理，良好的视觉平衡"+
                "布局方案：提供平铺重复版本（无缝衔接），提供独立中心图案版本，图案结构适配服装剪裁场景（仅优化图案适配性，不呈现服装）"+
                "专业要求：避免过于复杂的细节，确保不同尺寸下的可识别性，适配各种面料材质，印刷友好的色彩对比度"+
                "请确保设计仅聚焦图案本身，兼具艺术美感与商业实用性，完全符合大规模服装印刷生产标准，全程不出现任何服装相关载体呈现。";
        
        return String.format(promptTemplate, userDescription);
    }

    /**
     * 根据文字描述生成图片
     * @param description 描述文字

     * @param size 图片尺寸，格式：宽*高，如 1024*1024，范围：512*512 到 2048*2048
     * @param negativePrompt 负面提示词



                                 * @param promptExtend 是否扩展提示词
     * @param maxImages 生成图片数量，范围 1-6 张，默认1张
     * @return 生成的图片文件列表
     */
    public List<File> generateImageByText(String description, String size, String negativePrompt, Boolean promptExtend, Integer maxImages) {
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

            // 校验并设置图片尺寸（512*512 到 2048*2048）
            String validatedSize = validateAndGetSize(size);
            parameters.put("size", validatedSize);

            // 校验并设置生成张数（1-6张）
            int validatedMaxImages = validateAndGetMaxImages(maxImages);
            parameters.put("n", validatedMaxImages);

            log.info("生成参数 - 尺寸: {}, 张数: {}", validatedSize, validatedMaxImages);

            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(tongYiConfig.getDashscopeApiKey())
                    .model("qwen-image-2.0-pro")  // 使用 qwen-image-2.0-pro 支持多图生成 (n=1-6)
                    .messages(Collections.singletonList(userMessage))
                    .parameters(parameters)
                    .build();

            MultiModalConversationResult result = conv.call(param);
            log.info("Qwen API response: {}", result);

            // 详细打印API响应结构（用于调试多图生成）
            if (result != null && result.getOutput() != null) {
                MultiModalConversationOutput output = result.getOutput();
                List<MultiModalConversationOutput.Choice> choices = output.getChoices();
                log.info("API响应详情 - choices数量: {}", choices != null ? choices.size() : 0);

                if (choices != null) {
                    for (int i = 0; i < choices.size(); i++) {
                        MultiModalConversationOutput.Choice choice = choices.get(i);
                        MultiModalMessage message = choice != null ? choice.getMessage() : null;
                        List<Map<String, Object>> messageContent = message != null ? message.getContent() : null;

                        log.info("Choice[{}] - message存在: {}, content数量: {}",
                                i,
                                message != null,
                                messageContent != null ? messageContent.size() : 0);

                        if (messageContent != null) {
                            for (int j = 0; j < messageContent.size(); j++) {
                                Map<String, Object> item = messageContent.get(j);
                                log.info("Choice[{}].content[{}] - keys: {}", i, j, item != null ? item.keySet() : "null");
                            }
                        }
                    }
                }
            }

            // 提取所有图片URL
            List<String> imageUrls = extractAllImageUrls(result);
            if (imageUrls.isEmpty()) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI图片生成失败：未获取到图片URL");
            }

            log.info("成功获取 {} 张图片URL", imageUrls.size());

            // 下载所有图片
            List<File> imageFiles = new ArrayList<>();
            for (int i = 0; i < imageUrls.size(); i++) {
                try {
                    File imageFile = downloadImageToTempFile(imageUrls.get(i));
                    imageFiles.add(imageFile);
                    log.info("已下载第 {}/{} 张图片", i + 1, imageUrls.size());
                } catch (IOException e) {
                    log.error("下载第 {} 张图片失败", i + 1, e);
                    // 清理已下载的文件
                    imageFiles.forEach(this::deleteTempFile);
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片下载失败：" + e.getMessage());
                }
            }

            return imageFiles;
        } catch (ApiException | NoApiKeyException | UploadFileException e) {
            log.error("Qwen API调用失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI图片生成失败：" + e.getMessage());
        }
    }

    /**
     * 兼容旧接口：单张图片生成
     */
    public File generateImageByText(String description, String size, String negativePrompt, Boolean promptExtend) {
        List<File> files = generateImageByText(description, size, negativePrompt, promptExtend, 1);
        return files.isEmpty() ? null : files.get(0);
    }

    /**
     * 根据参考图片生成图片
     * @param referenceImageUrl 参考图片URL
     * @param description 描述文字
     * @param size 图片尺寸，格式：宽*高，如 1024*1024，范围：512*512 到 2048*2048
     * @param maxImages 生成图片数量，范围 1-6 张，默认1张
     * @return 生成的图片文件列表
     */
    public List<File> generateImageByReference(String referenceImageUrl, String description, String size, Integer maxImages) {
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

            // 校验并设置图片尺寸（512*512 到 2048*2048）
            String validatedSize = validateAndGetSize(size);
            parameters.put("size", validatedSize);

            // 校验并设置生成张数（1-6张）
            int validatedMaxImages = validateAndGetMaxImages(maxImages);
            parameters.put("n", validatedMaxImages);

            log.info("生成参数 - 尺寸: {}, 张数: {}", validatedSize, validatedMaxImages);

            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(tongYiConfig.getDashscopeApiKey())
                    .model("qwen-image-2.0-pro")  // 使用 qwen-image-2.0-pro 支持多图生成 (n=1-6)
                    .messages(Collections.singletonList(userMessage))
                    .parameters(parameters)
                    .build();

            MultiModalConversationResult result = conv.call(param);
            log.info("Qwen API response (reference mode): {}", result);
            log.info("Response output: {}", result.getOutput());
            log.info("Response usage: {}", result.getUsage());

            // 详细打印API响应结构（用于调试多图生成）
            if (result != null && result.getOutput() != null) {
                MultiModalConversationOutput output = result.getOutput();
                List<MultiModalConversationOutput.Choice> choices = output.getChoices();
                log.info("API响应详情 - choices数量: {}", choices != null ? choices.size() : 0);

                if (choices != null) {
                    for (int i = 0; i < choices.size(); i++) {
                        MultiModalConversationOutput.Choice choice = choices.get(i);
                        MultiModalMessage message = choice != null ? choice.getMessage() : null;
                        List<Map<String, Object>> messageContent = message != null ? message.getContent() : null;

                        log.info("Choice[{}] - message存在: {}, content数量: {}",
                                i,
                                message != null,
                                messageContent != null ? messageContent.size() : 0);

                        if (messageContent != null) {
                            for (int j = 0; j < messageContent.size(); j++) {
                                Map<String, Object> item = messageContent.get(j);
                                log.info("Choice[{}].content[{}] - keys: {}", i, j, item != null ? item.keySet() : "null");
                            }
                        }
                    }
                }
            }

            // 提取所有图片URL
            List<String> imageUrls = extractAllImageUrls(result);
            if (imageUrls.isEmpty()) {
                log.error("Failed to extract image URLs from response: {}", result);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI图片生成失败：未获取到图片URL");
            }

            log.info("成功获取 {} 张图片URL", imageUrls.size());

            // 下载所有图片
            List<File> imageFiles = new ArrayList<>();
            for (int i = 0; i < imageUrls.size(); i++) {
                try {
                    File imageFile = downloadImageToTempFile(imageUrls.get(i));
                    imageFiles.add(imageFile);
                    log.info("已下载第 {}/{} 张图片", i + 1, imageUrls.size());
                } catch (IOException e) {
                    log.error("下载第 {} 张图片失败", i + 1, e);
                    // 清理已下载的文件
                    imageFiles.forEach(this::deleteTempFile);
                    throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片下载失败：" + e.getMessage());
                }
            }

            return imageFiles;
        } catch (ApiException | NoApiKeyException | UploadFileException e) {
            log.error("Qwen API调用失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI图片生成失败：" + e.getMessage());
        } finally {
            // 清理上传的临时文件
            if (uploadedTempFile != null && uploadedTempFile.exists()) {
                deleteTempFile(uploadedTempFile);
            }
        }
    }

    /**
     * 兼容旧接口：单张图片生成（图片参考模式）
     */
    public File generateImageByReference(String referenceImageUrl, String description, String size) {
        List<File> files = generateImageByReference(referenceImageUrl, description, size, 1);
        return files.isEmpty() ? null : files.get(0);
    }

    /**
     * 校验并获取图片尺寸
     * 支持格式：宽*高，范围：512*512 到 2048*2048
     */
    private String validateAndGetSize(String size) {
        // 默认尺寸
        if (StrUtil.isBlank(size)) {
            return "1024*1024";
        }

        // 解析尺寸
        String[] parts = size.split("\\*");
        if (parts.length != 2) {
            log.warn("无效的尺寸格式: {}, 使用默认值 1024*1024", size);
            return "1024*1024";
        }

        try {
            int width = Integer.parseInt(parts[0].trim());
            int height = Integer.parseInt(parts[1].trim());

            // 校验总像素：512*512 (262,144) 到 2048*2048 (4,194,304)
            int totalPixels = width * height;
            int minPixels = 512 * 512;
            int maxPixels = 2048 * 2048;

            if (totalPixels < minPixels || totalPixels > maxPixels) {
                log.warn("图片尺寸超出范围 (总像素: {}), 必须在 {}({}) 到 {}({}) 之间，使用默认值 1024*1024",
                        totalPixels, "512*512", minPixels, "2048*2048", maxPixels);
                return "1024*1024";
            }

            // 校验单边尺寸：最小512，最大2048
            if (width < 512 || width > 2048 || height < 512 || height > 2048) {
                log.warn("图片宽高超出范围 ({}*{}), 宽高必须在 512-2048 之间，使用默认值 1024*1024", width, height);
                return "1024*1024";
            }

            return width + "*" + height;
        } catch (NumberFormatException e) {
            log.warn("尺寸解析失败: {}, 使用默认值 1024*1024", size);
            return "1024*1024";
        }
    }

    /**
     * 校验并获取生成图片数量
     * 范围：1-6 张
     */
    private int validateAndGetMaxImages(Integer maxImages) {
        if (maxImages == null || maxImages < 1) {
            return 1; // 默认1张
        }
        if (maxImages > 6) {
            log.warn("图片数量超出限制: {}, 最多支持6张，已调整为6", maxImages);
            return 6;
        }
        return maxImages;
    }

    /**
     * 提取所有图片URL（支持多张图片）
     *
     * 千问API返回格式说明：
     * - 当 n=1 时：返回1个choice，该choice的content中有1个图片URL
     * - 当 n>1 时：返回n个choices，每个choice的content中有1个图片URL
     */
    private List<String> extractAllImageUrls(MultiModalConversationResult result) {
        List<String> imageUrls = new ArrayList<>();

        if (result == null) {
            log.error("MultiModalConversationResult is null");
            return imageUrls;
        }

        MultiModalConversationOutput output = result.getOutput();
        if (output == null) {
            log.error("MultiModalConversationResult.getOutput() is null");
            return imageUrls;
        }

        List<MultiModalConversationOutput.Choice> choices = output.getChoices();
        if (choices == null || choices.isEmpty()) {
            log.error("Output.choices is null or empty");
            return imageUrls;
        }

        log.info("API返回了 {} 个choices", choices.size());

        // 遍历所有Choices（每个choice对应一张图片）
        for (int i = 0; i < choices.size(); i++) {
            MultiModalConversationOutput.Choice choice = choices.get(i);
            log.info("处理第 {} 个choice", i + 1);

            MultiModalMessage message = choice.getMessage();
            if (message == null) {
                log.warn("Choice[{}].message is null, skip", i);
                continue;
            }

            List<Map<String, Object>> content = message.getContent();
            if (content == null || content.isEmpty()) {
                log.warn("Choice[{}].message.content is null or empty, skip", i);
                continue;
            }

            log.info("Choice[{}] 包含 {} 个content items", i, content.size());

            // 提取当前choice中的所有图片URL（通常每个choice只有1个图片）
            for (int j = 0; j < content.size(); j++) {
                Map<String, Object> contentItem = content.get(j);
                String imageUrl = extractSingleImageUrl(contentItem);
                if (StrUtil.isNotBlank(imageUrl)) {
                    imageUrls.add(imageUrl);
                    log.info("成功提取图片URL[{}]: {}", imageUrls.size(), imageUrl);
                } else {
                    log.warn("Choice[{}].content[{}] 未找到图片URL，内容: {}", i, j, contentItem);
                }
            }
        }

        log.info("共提取到 {} 张图片URL", imageUrls.size());

        // 打印所有URL用于调试
        for (int i = 0; i < imageUrls.size(); i++) {
            log.info("图片URL[{}]: {}", i + 1, imageUrls.get(i));
        }

        return imageUrls;
    }

    /**
     * 从单个content item中提取图片URL
     *
     * 支持的格式：
     * 1. {"image_url": "https://xxx.png"}
     * 2. {"image_url": {"url": "https://xxx.png"}}
     * 3. {"image": "https://xxx.png"}
     */
    private String extractSingleImageUrl(Map<String, Object> contentItem) {
        if (contentItem == null || contentItem.isEmpty()) {
            return null;
        }

        // 格式1：contentItem = {"image_url": "https://xxx.png"}
        if (contentItem.containsKey("image_url")) {
            Object imageUrlObj = contentItem.get("image_url");

            // 直接是字符串
            if (imageUrlObj instanceof String) {
                String url = (String) imageUrlObj;
                log.debug("提取到图片URL (格式1 - 直接字符串): {}", url);
                return url;
            }

            // 格式2：contentItem = {"image_url": {"url": "https://xxx.png"}}
            if (imageUrlObj instanceof Map) {
                @SuppressWarnings("unchecked")
                Map<String, Object> imageUrlMap = (Map<String, Object>) imageUrlObj;
                Object urlObj = imageUrlMap.get("url");
                if (urlObj instanceof String) {
                    String url = (String) urlObj;
                    log.debug("提取到图片URL (格式2 - 嵌套Map): {}", url);
                    return url;
                }
            }
        }

        // 格式3（兼容）：{"image": "https://xxx.png"}
        if (contentItem.containsKey("image")) {
            Object imageObj = contentItem.get("image");
            if (imageObj instanceof String) {
                String url = (String) imageObj;
                log.debug("提取到图片URL (格式3 - 兼容格式): {}", url);
                return url;
            }
        }

        // 未找到图片URL，记录详细信息用于调试
        log.warn("未能从content item中提取图片URL，keys: {}, values types: {}",
                contentItem.keySet(),
                contentItem.values().stream()
                        .map(v -> v == null ? "null" : v.getClass().getSimpleName())
                        .toArray());

        return null;
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