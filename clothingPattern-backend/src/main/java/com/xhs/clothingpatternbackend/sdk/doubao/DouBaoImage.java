package com.xhs.clothingpatternbackend.sdk.doubao;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.core.util.StrUtil;
import com.volcengine.ark.runtime.model.images.generation.GenerateImagesRequest;
import com.volcengine.ark.runtime.model.images.generation.ImagesResponse;
import com.volcengine.ark.runtime.model.images.generation.ResponseFormat;
import com.volcengine.ark.runtime.service.ArkService;
import com.xhs.clothingpatternbackend.config.CosClientConfig;
import com.xhs.clothingpatternbackend.config.DouBaoConfig;
import com.xhs.clothingpatternbackend.exception.BusinessException;
import com.xhs.clothingpatternbackend.exception.ErrorCode;
import com.xhs.clothingpatternbackend.utils.CosUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.ConnectionPool;
import okhttp3.Dispatcher;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
*@Author: 小辛同学
*@CreateTime: 2025-11-22
*@Description: DouBao图片生成服务
*@Version: 1.0
*/

/**
 *
 * 文本提示词（prompt）建议不超过300个汉字或600个英文单词。字数过多信息容易分散，模型可能因此忽略细节，只关注重点，造成图片缺失部分元素。详情可参见 Seedream 4.0 提示词指南。
 *
 * 图片传入限制
 * 图片格式：jpeg、png
 * 宽高比（宽/高）范围：[1/3, 3]
 * 宽高长度（px） > 14
 * 大小：不超过 10MB
 * 总像素：不超过 6000×6000 px
 * 最多支持传入 10 张参考图。
 */
@Service
@Slf4j
public class DouBaoImage {
    @Resource
    private DouBaoConfig douBaoConfig;

    @Resource
    private CosUtils cosUtils;

    @Resource
    private CosClientConfig cosClientConfig;

    private static final String BASE_URL = "https://ark.cn-beijing.volces.com/api/v3";
    private static final String DEFAULT_SIZE = "2K";
    private static final boolean DEFAULT_WATERMARK = true;

    /**
     * 创建ArkService实例
     */
    private ArkService createArkService() {
        ConnectionPool connectionPool = new ConnectionPool(5, 1, TimeUnit.SECONDS);
        Dispatcher dispatcher = new Dispatcher();
        return ArkService.builder()
                .baseUrl(BASE_URL)
                .dispatcher(dispatcher)
                .connectionPool(connectionPool)
                .apiKey(douBaoConfig.getApiKey())
                .build();
    }

    /**
     * 构建服装图案专用提示词
     * 将用户简单描述转换为专业的服装图案生成提示词
     */
    private String buildClothingPatternPrompt(String userDescription) {
        String promptTemplate = "设计一个可直接用于服装印刷的高质量图案。"+
                "核心要求："+
                "设计风格：现代时尚，符合当前流行趋势，具备商业应用价值"+
                "技术规格： 分辨率：300 DPI以上 格式：PNG透明背景 色彩模式：CMYK/RGB双模式适配"+
                "设计元素：%s"+
                "视觉特征： 清晰锐利的边缘线条,协调的色彩搭配（建议使用互补色或类比色方案）,适当的负空间处理 ,良好的视觉平衡"+
                "布局方案： 提供平铺重复版本（无缝衔接）,提供独立中心图案版本 ,考虑服装裁剪的适配性"+
                "专业要求： 避免过于复杂的细节,确保不同尺寸下的可识别性, 适配各种面料材质,印刷友好的色彩对比度"+
                "请确保设计兼具艺术美感与商业实用性，符合大规模印刷生产标准。只生产图案，不要印到服装上的效果图";
        
        return String.format(promptTemplate, userDescription);
    }

    /**
     * 构建批量生成专用提示词（明确指定生成数量）
     */
    private String buildBatchClothingPatternPrompt(String userDescription, int count) {
        return String.format(
            "生成一组共%d张服装图案设计，核心元素为：%s。" +
            "要求：1.每张图案风格统一但有细节变化 2.适合服装印刷 3.高清锐利 4.色彩协调美观",
            count, userDescription
        );
    }

    /**
     * 根据文字描述生成图片（文生图）
     * 
     * @param description 用户描述
     * @param size 图片尺寸（可选：512*512, 1024*1024, 2K等）
     * @param watermark 是否添加水印
     * @return 生成的图片文件
     */
    public File generateImageByText(String description, String size, Boolean watermark) {
        ArkService service = null;
        try {
            service = createArkService();

            // 使用预设提示词模板构建专业的服装图案提示词
            String enhancedPrompt = buildClothingPatternPrompt(description);
            log.info("原始描述: {}", description);
            log.info("增强后的提示词: {}", enhancedPrompt);

            GenerateImagesRequest generateRequest = GenerateImagesRequest.builder()
                    .model(douBaoConfig.getModelId())
                    .prompt(enhancedPrompt)
                    .size(StrUtil.isNotBlank(size) ? size : DEFAULT_SIZE)
                    .sequentialImageGeneration("disabled")
                    .responseFormat(ResponseFormat.Url)
                    .stream(false)
                    .watermark(watermark != null ? watermark : DEFAULT_WATERMARK)
                    .build();

            ImagesResponse imagesResponse = service.generateImages(generateRequest);
            log.info("DouBao API response: {}", imagesResponse);

            String imageUrl = extractImageUrl(imagesResponse);
            if (StrUtil.isBlank(imageUrl)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI图片生成失败：未获取到图片URL");
            }

            return downloadImageToTempFile(imageUrl);
        } catch (Exception e) {
            log.error("DouBao API调用失败", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI图片生成失败：" + e.getMessage());
        } finally {
            if (service != null) {
                service.shutdownExecutor();
            }
        }
    }

    /**
     * 根据参考图片生成图片（单图生图）
     * 
     * @param referenceImageUrl 参考图片URL
     * @param description 描述文字
     * @param size 图片尺寸
     * @return 生成的图片文件
     */
    public File generateImageByReference(String referenceImageUrl, String description, String size) {
        ArkService service = null;
        File uploadedTempFile = null;
        try {
            service = createArkService();

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

            // 确保有描述文本
            String textPrompt = StrUtil.isNotBlank(description) 
                    ? description 
                    : "基于参考图片，生成一个适合服装印花的精美图案设计";

            log.info("参考图片URL: {}, 描述: {}", referenceImageUrl, textPrompt);

            GenerateImagesRequest generateRequest = GenerateImagesRequest.builder()
                    .model(douBaoConfig.getModelId())
                    .prompt(textPrompt)
                    .image(referenceImageUrl)
                    .size(StrUtil.isNotBlank(size) ? size : DEFAULT_SIZE)
                    .sequentialImageGeneration("disabled")
                    .responseFormat(ResponseFormat.Url)
                    .stream(false)
                    .watermark(DEFAULT_WATERMARK)
                    .build();

            ImagesResponse imagesResponse = service.generateImages(generateRequest);
            log.info("DouBao API response (reference mode): {}", imagesResponse);

            String imageUrl = extractImageUrl(imagesResponse);
            if (StrUtil.isBlank(imageUrl)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI图片生成失败：未获取到图片URL");
            }

            return downloadImageToTempFile(imageUrl);
        } catch (Exception e) {
            log.error("DouBao API调用失败（参考图片模式）", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI图片生成失败：" + e.getMessage());
        } finally {
            // 清理上传的临时文件
            if (uploadedTempFile != null && uploadedTempFile.exists()) {
                deleteTempFile(uploadedTempFile);
            }
            if (service != null) {
                service.shutdownExecutor();
            }
        }
    }

    /**
     * 根据多张图片生成单张图片（多图生图）
     * 
     * @param imageUrls 多张参考图片URL列表
     * @param description 描述文字
     * @param size 图片尺寸
     * @return 生成的图片文件
     */
    public File generateImageByMultipleReferences(List<String> imageUrls, String description, String size) {
        ArkService service = null;
        try {
            service = createArkService();

            if (imageUrls == null || imageUrls.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "参考图片不能为空");
            }

            String textPrompt = StrUtil.isNotBlank(description) 
                    ? description 
                    : "基于参考图片，生成一个融合设计的服装图案";

            log.info("参考图片数量: {}, 描述: {}", imageUrls.size(), textPrompt);

            GenerateImagesRequest generateRequest = GenerateImagesRequest.builder()
                    .model(douBaoConfig.getModelId())
                    .prompt(textPrompt)
                    .image(imageUrls)
                    .size(StrUtil.isNotBlank(size) ? size : DEFAULT_SIZE)
                    .sequentialImageGeneration("disabled")
                    .responseFormat(ResponseFormat.Url)
                    .stream(false)
                    .watermark(DEFAULT_WATERMARK)
                    .build();

            ImagesResponse imagesResponse = service.generateImages(generateRequest);
            log.info("DouBao API response (multiple images mode): {}", imagesResponse);

            String imageUrl = extractImageUrl(imagesResponse);
            if (StrUtil.isBlank(imageUrl)) {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI图片生成失败：未获取到图片URL");
            }

            return downloadImageToTempFile(imageUrl);
        } catch (Exception e) {
            log.error("DouBao API调用失败（多图模式）", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI图片生成失败：" + e.getMessage());
        } finally {
            if (service != null) {
                service.shutdownExecutor();
            }
        }
    }

    /**
     * 根据文字生成一组图片（文生组图）
     * 
     * @param description 描述文字
     * @param maxImages 最大图片数量
     * @param size 图片尺寸
     * @return 生成的图片文件列表
     */
    public List<File> generateImagesByText(String description, Integer maxImages, String size) {
        ArkService service = null;
        try {
            service = createArkService();

            // 使用批量生成专用提示词，明确说明生成数量
            int count = maxImages != null ? maxImages : 4;
            String enhancedPrompt = buildBatchClothingPatternPrompt(description, count);
            log.info("生成一组图片 - 原始描述: {}", description);
            log.info("生成一组图片 - 增强后的提示词: {}", enhancedPrompt);
            log.info("生成一组图片 - 请求数量: {}", maxImages);

            GenerateImagesRequest.SequentialImageGenerationOptions options = 
                    new GenerateImagesRequest.SequentialImageGenerationOptions();
            options.setMaxImages(count);
            
            log.info("生成一组图片 - 设置的maxImages: {}", options.getMaxImages());

            GenerateImagesRequest generateRequest = GenerateImagesRequest.builder()
                    .model(douBaoConfig.getModelId())
                    .prompt(enhancedPrompt)
                    .responseFormat(ResponseFormat.Url)
                    .size(StrUtil.isNotBlank(size) ? size : DEFAULT_SIZE)
                    .sequentialImageGeneration("auto")
                    .sequentialImageGenerationOptions(options)
                    .stream(false)
                    .watermark(DEFAULT_WATERMARK)
                    .build();

            ImagesResponse imagesResponse = service.generateImages(generateRequest);
            log.info("DouBao API response (batch mode): {}", imagesResponse);
            log.info("DouBao API 返回的图片数量: {}", 
                imagesResponse != null && imagesResponse.getData() != null ? imagesResponse.getData().size() : 0);

            return downloadImagesToTempFiles(imagesResponse);
        } catch (Exception e) {
            log.error("DouBao API调用失败（批量生成模式）", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI图片批量生成失败：" + e.getMessage());
        } finally {
            if (service != null) {
                service.shutdownExecutor();
            }
        }
    }

    /**
     * 根据单张参考图片生成一组图片（单图生组图）
     * 
     * @param referenceImageUrl 参考图片URL
     * @param description 描述文字
     * @param maxImages 最大图片数量
     * @param size 图片尺寸
     * @return 生成的图片文件列表
     */
    public List<File> generateImagesByReference(String referenceImageUrl, String description, 
                                                 Integer maxImages, String size) {
        ArkService service = null;
        try {
            service = createArkService();

//            String textPrompt = StrUtil.isNotBlank(description)
//                    ? description
//                    : "基于参考图片，生成一系列服装图案设计方案";

            String textPrompt=buildBatchClothingPatternPrompt(description, maxImages);

            log.info("参考图片生成一组 - URL: {}, 描述: {}", referenceImageUrl, textPrompt);

            GenerateImagesRequest.SequentialImageGenerationOptions options = 
                    new GenerateImagesRequest.SequentialImageGenerationOptions();
            options.setMaxImages(maxImages != null ? maxImages : 5);

            GenerateImagesRequest generateRequest = GenerateImagesRequest.builder()
                    .model(douBaoConfig.getModelId())
                    .prompt(textPrompt)
                    .image(referenceImageUrl)
                    .responseFormat(ResponseFormat.Url)
                    .size(StrUtil.isNotBlank(size) ? size : DEFAULT_SIZE)
                    .sequentialImageGeneration("auto")
                    .sequentialImageGenerationOptions(options)
                    .stream(false)
                    .watermark(DEFAULT_WATERMARK)
                    .build();

            ImagesResponse imagesResponse = service.generateImages(generateRequest);
            log.info("DouBao API response (single image to batch mode): {}", imagesResponse);

            return downloadImagesToTempFiles(imagesResponse);
        } catch (Exception e) {
            log.error("DouBao API调用失败（单图生成一组模式）", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI图片批量生成失败：" + e.getMessage());
        } finally {
            if (service != null) {
                service.shutdownExecutor();
            }
        }
    }

    /**
     * 根据多张参考图片生成一组图片（多图生组图）
     * 
     * @param imageUrls 多张参考图片URL列表
     * @param description 描述文字
     * @param maxImages 最大图片数量
     * @param size 图片尺寸
     * @return 生成的图片文件列表
     */
    public List<File> generateImagesByMultipleReferences(List<String> imageUrls, String description, 
                                                          Integer maxImages, String size) {
        ArkService service = null;
        try {
            service = createArkService();

            if (imageUrls == null || imageUrls.isEmpty()) {
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "参考图片不能为空");
            }

            String textPrompt = StrUtil.isNotBlank(description) 
                    ? description 
                    : "基于参考图片，生成一系列融合设计的服装图案方案";

            log.info("多图生成一组 - 参考图片数量: {}, 描述: {}", imageUrls.size(), textPrompt);

            GenerateImagesRequest.SequentialImageGenerationOptions options = 
                    new GenerateImagesRequest.SequentialImageGenerationOptions();
            options.setMaxImages(maxImages != null ? maxImages : 3);

            GenerateImagesRequest generateRequest = GenerateImagesRequest.builder()
                    .model(douBaoConfig.getModelId())
                    .prompt(textPrompt)
                    .image(imageUrls)
                    .responseFormat(ResponseFormat.Url)
                    .size(StrUtil.isNotBlank(size) ? size : DEFAULT_SIZE)
                    .sequentialImageGeneration("auto")
                    .sequentialImageGenerationOptions(options)
                    .stream(false)
                    .watermark(DEFAULT_WATERMARK)
                    .build();

            ImagesResponse imagesResponse = service.generateImages(generateRequest);
            log.info("DouBao API response (multiple images to batch mode): {}", imagesResponse);

            return downloadImagesToTempFiles(imagesResponse);
        } catch (Exception e) {
            log.error("DouBao API调用失败（多图生成一组模式）", e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI图片批量生成失败：" + e.getMessage());
        } finally {
            if (service != null) {
                service.shutdownExecutor();
            }
        }
    }

    /**
     * 从API响应中提取单张图片URL
     */
    private String extractImageUrl(ImagesResponse response) {
        if (response == null || response.getData() == null || response.getData().isEmpty()) {
            log.error("ImagesResponse is null or empty");
            return null;
        }

        String imageUrl = response.getData().get(0).getUrl();
        log.info("Extracted image URL: {}", imageUrl);
        return imageUrl;
    }

    /**
     * 下载单张图片到临时文件
     * 注意：调用者负责删除返回的临时文件
     */
    private File downloadImageToTempFile(String imageUrl) {
        String uuid = RandomUtil.randomString(10);
        String fileName = "doubao_image_" + uuid;
        
        File tempFile = null;
        try {
            tempFile = File.createTempFile(fileName, ".png");
            
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
            deleteTempFile(tempFile);
            log.error("图片下载失败: {}", imageUrl, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "图片下载失败：" + e.getMessage());
        }
    }

    /**
     * 下载多张图片到临时文件列表
     */
    private List<File> downloadImagesToTempFiles(ImagesResponse response) {
        if (response == null || response.getData() == null || response.getData().isEmpty()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "API响应为空，未生成图片");
        }

        java.util.List<File> files = new java.util.ArrayList<>();
        for (int i = 0; i < response.getData().size(); i++) {
            String url = response.getData().get(i).getUrl();
            String size = response.getData().get(i).getSize();
            log.info("Image {}: URL={}, Size={}", i + 1, url, size);
            
            try {
                File file = downloadImageToTempFile(url);
                files.add(file);
            } catch (Exception e) {
                log.error("下载第 {} 张图片失败", i + 1, e);
                // 清理已下载的文件
                files.forEach(this::deleteTempFile);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, 
                        "批量图片下载失败（第" + (i + 1) + "张）：" + e.getMessage());
            }
        }
        
        log.info("批量下载完成，共 {} 张图片", files.size());
        return files;
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


