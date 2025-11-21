package com.xhs.clothingpatternbackend.utils;

import cn.hutool.core.io.FileUtil;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.model.ciModel.persistence.PicOperations;
import com.xhs.clothingpatternbackend.config.CosClientConfig;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-11-21
 * @Description:通用对象存储工具类
 * @Version: 1.0
 */
@Component
public class CosUtils {
    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private COSClient cosClient;

    /**
     * 上传对象
     *
     * @param key
     * @param file
     * @return
     */
    public PutObjectResult putObject(String key, File file) {
        PutObjectRequest request = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        return cosClient.putObject(request);
    }

    /**
     * 获取对象
     *
     * @param key
     * @return
     */
    public COSObject getObject(String key) {
        return cosClient.getObject(new GetObjectRequest(cosClientConfig.getBucket(), key));
    }

    /**
     * 上传图片对象
     *
     * @param key
     * @param file
     * @return
     */
    public PutObjectResult putPictureObject(String key, File file) {
        PutObjectRequest putObjectRequest = new PutObjectRequest(cosClientConfig.getBucket(), key, file);
        PicOperations picOperations = new PicOperations();
        picOperations.setIsPicInfo(1);
        
        // 提取文件路径和文件名
        String filePath = "";
        String fileName = key;
        int lastSlashIndex = key.lastIndexOf("/");
        if (lastSlashIndex != -1) {
            filePath = key.substring(0, lastSlashIndex + 1); // 包括最后的 /
            fileName = key.substring(lastSlashIndex + 1);
        }
        
        String fileNameWithoutExt = FileUtil.mainName(fileName);
        String fileExt = FileUtil.getSuffix(fileName);
        
        //图片压缩
        List<PicOperations.Rule> rules = new ArrayList<>();
        String webKey = filePath + fileNameWithoutExt + ".webp";
        PicOperations.Rule compressedRule = new PicOperations.Rule();
        compressedRule.setBucket(cosClientConfig.getBucket());
        compressedRule.setFileId(webKey);
        compressedRule.setRule("imageMogr2/format/webp");
        rules.add(compressedRule);
        
        //缩略图做处理，仅对>20kb的图片进行处理
        if(file.length() > 20 * 1024){
            //缩略图处理
            PicOperations.Rule thumbnailRule = new PicOperations.Rule();
            thumbnailRule.setBucket(cosClientConfig.getBucket());
            String thumbnailKey = filePath + fileNameWithoutExt + "_thumbnail." + fileExt;
            thumbnailRule.setFileId(thumbnailKey);
            //缩放规则
            thumbnailRule.setRule(String.format("imageMogr2/thumbnail/%sx%s>", 128, 128));
            rules.add(thumbnailRule);
        }

        //构造处理参数
        picOperations.setRules(rules);
        putObjectRequest.setPicOperations(picOperations);
        return cosClient.putObject(putObjectRequest);
    }

    public void deleteObject(String key) {
        cosClient.deleteObject(cosClientConfig.getBucket(), key);
    }
}
