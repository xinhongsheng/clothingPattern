package com.xhs.clothingpatternbackend.utils;

import cn.hutool.core.util.StrUtil;
import com.xhs.clothingpatternbackend.config.CosClientConfig;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import jakarta.annotation.Resource;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

/**
 * 分类图标处理工具类
 */
@Component
@Slf4j
public class CategoryIconUtils {
    
    @Resource
    private CosUtils cosUtils;
    
    @Resource
    private CosClientConfig cosClientConfig;
    
    /**
     * 上传分类图标到COS
     * 
     * @param base64Image base64格式的图片数据
     * @return COS上的图片URL
     */
    public String uploadCategoryIcon(String base64Image) {
        if (StrUtil.isBlank(base64Image)) {
            return null;
        }
        
        try {
            // 解码base64
            String base64Data = base64Image;
            if (base64Data.contains(",")) {
                base64Data = base64Data.split(",")[1];
            }
            byte[] imageBytes = Base64.getDecoder().decode(base64Data);
            
            // 保存为临时文件
            File tempFile = File.createTempFile("category_icon_", ".png");
            Files.write(tempFile.toPath(), imageBytes);
            
            // 上传到COS的CategoryIcon目录
            String key = "CategoryIcon/" + System.currentTimeMillis() + "_" + 
                         (int)(Math.random() * 10000) + ".png";
            cosUtils.putObject(key, tempFile);
            String cosUrl = cosClientConfig.getHost() + "/" + key;
            
            // 删除临时文件
            deleteTempFile(tempFile);
            
            return cosUrl;
        } catch (Exception e) {
            log.error("上传分类图标到COS失败", e);
            return null;
        }
    }
    
    /**
     * 删除临时文件
     */
    private void deleteTempFile(File file) {
        try {
            if (file != null && file.exists()) {
                file.delete();
            }
        } catch (Exception e) {
            log.warn("删除临时文件失败: {}", file.getAbsolutePath(), e);
        }
    }
}