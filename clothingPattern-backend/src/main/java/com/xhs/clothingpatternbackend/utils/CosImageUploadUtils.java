package com.xhs.clothingpatternbackend.utils;
import com.xhs.clothingpatternbackend.config.CosClientConfig;
import com.xhs.clothingpatternbackend.exception.BusinessException;
import com.xhs.clothingpatternbackend.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.slf4j.Logger;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.UUID;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-12-01
 * @Description: COS文件上传工具类（专注图片上传：JPG/PNG格式、2MB限制）
 * @Version: 1.0
 */
@Component
@Slf4j
public class CosImageUploadUtils {
    // 私有构造方法，禁止实例化工具类
    private CosImageUploadUtils() {}

    /**
    * 通用图片上传到COS的静态方法（@Slf4j内置日志，boolean控制是否打印）
    *
    * @param file            待上传的图片文件（MultipartFile）
    * @param userId       登录用户id（用于拼接存储路径）
    * @param cosUtils        COS上传工具类实例
     * @param cosClientConfig COS客户端配置（获取Host）
    * @param tempFilePrefix  临时文件前缀（如：user_avatar_、article_cover_）
    * @param cosKeyPrefix    COS存储路径前缀（如：user/avatar/、article/cover/）
    * @param needLog         是否需要打印日志（true：打印；false：不打印）
    * @return 上传成功后的COS文件URL
     */
    public static String uploadImageToCos(MultipartFile file,
                                          Long userId,
                                          CosUtils cosUtils,
                                          CosClientConfig cosClientConfig,
                                          String tempFilePrefix,
                                          String cosKeyPrefix,
                                          boolean needLog) {
        // 1. 验证文件类型（JPG/PNG）
        String contentType = file.getContentType();
        if (contentType == null || (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType))) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "只支持JPG和PNG格式的图片");
        }

        // 2. 验证文件大小（2MB）
        final long MAX_SIZE = 2 * 1024 * 1024; // 2MB
        if (file.getSize() > MAX_SIZE) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图片大小不能超过2MB");
        }

        File tempFile = null;
        try {
            // 3. 处理文件后缀（无后缀时默认.png）
            String originalFilename = file.getOriginalFilename();
            String suffix = (originalFilename != null && originalFilename.contains("."))
                    ? originalFilename.substring(originalFilename.lastIndexOf("."))
                    : ".png";

            // 4. 创建临时文件
            tempFile = File.createTempFile(tempFilePrefix, suffix);
            file.transferTo(tempFile);

            // 5. 构建COS存储Key（用户ID + 时间戳 + 后缀，避免文件名重复）
            String cosKey = String.format("%s%s/%d%s",
                    cosKeyPrefix,
                    userId,
                    System.currentTimeMillis(),
                    suffix);

            // 6. 上传到COS
            cosUtils.putPictureObject(cosKey, tempFile);

            // 7. 构建最终访问URL
            String cosUrl = cosClientConfig.getHost() + "/" + cosKey;

            // 8. 日志记录（根据needLog控制是否打印）
            if (needLog) {
                log.info("图片上传COS成功 | 业务类型: {} | 用户ID: {} | 存储Key: {} | 访问URL: {}",
                        tempFilePrefix.replace("_", ""), // 从临时前缀提取业务类型（如user_avatar_ -> useravatar）
                        userId,
                        cosKey,
                        cosUrl);
            }

            return cosUrl;

        } catch (IOException e) {
            // 9. 上传异常处理（根据needLog控制是否打印堆栈）
            if (needLog) {
                log.error("图片上传COS失败 | 业务类型: {} | 用户ID: {}",
                        tempFilePrefix.replace("_", ""),
                        userId,
                        e);
            } else {
                // 不需要日志时，仅打印简单错误信息（避免堆栈冗余）
                System.err.printf("图片上传失败 | 业务类型: %s | 用户ID: %s%n",
                        tempFilePrefix.replace("_", ""),
                        userId);
            }
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "文件上传失败");
        } finally {
            // 10. 强制删除临时文件（避免磁盘占用）
            if (tempFile != null && tempFile.exists()) {
                boolean deleted = tempFile.delete();
                if (!deleted) {
                    String errorMsg = String.format("临时文件删除失败 | 路径: %s", tempFile.getAbsolutePath());
                    if (needLog) {
                        log.warn(errorMsg);
                    } else {
                        System.out.println(errorMsg);
                    }
                }
            }
        }
    }

}
