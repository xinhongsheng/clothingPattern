package com.xhs.clothingpatternbackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qcloud.cos.model.PutObjectResult;
import com.xhs.clothingpatternbackend.config.CosClientConfig;
import com.xhs.clothingpatternbackend.exception.BusinessException;
import com.xhs.clothingpatternbackend.exception.ErrorCode;
import com.xhs.clothingpatternbackend.exception.ThrowUtils;
import com.xhs.clothingpatternbackend.mapper.PatternMapper;
import com.xhs.clothingpatternbackend.model.dto.pattern.PatternEditRequest;
import com.xhs.clothingpatternbackend.model.dto.pattern.PatternGenerateRequest;
import com.xhs.clothingpatternbackend.model.dto.pattern.PatternQueryRequest;
import com.xhs.clothingpatternbackend.model.entity.Pattern;
import com.xhs.clothingpatternbackend.model.entity.User;
import com.xhs.clothingpatternbackend.model.enums.AuditStatusEnum;
import com.xhs.clothingpatternbackend.model.enums.GenerationTypeEnum;
import com.xhs.clothingpatternbackend.model.enums.UserRoleEnum;
import com.xhs.clothingpatternbackend.model.vo.PatternVO;
import com.xhs.clothingpatternbackend.model.vo.UserVO;
import com.xhs.clothingpatternbackend.service.PatternService;
import com.xhs.clothingpatternbackend.service.UserService;
import com.xhs.clothingpatternbackend.sdk.dashscope.QwenImage;
import com.xhs.clothingpatternbackend.utils.CosUtils;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;

import java.io.File;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
* @author 小辛
* @description 针对表【pattern(服装图案表（智能图案生成模块核心表）)】的数据库操作Service实现
* @createDate 2025-11-21 15:44:19
*/
@Service
@Slf4j
public class PatternServiceImpl extends ServiceImpl<PatternMapper, Pattern>
    implements PatternService{

    @Resource
    private UserService userService;

    @Resource
    private CosUtils cosUtils;

    @Resource
    private CosClientConfig cosClientConfig;

    @Resource
    private QwenImage qwenImage;

    @Resource
    private com.xhs.clothingpatternbackend.sdk.doubao.DouBaoImage douBaoImage;


    @Override
    public Long generatePattern(PatternGenerateRequest patternGenerateRequest, User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(patternGenerateRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        String patternName = patternGenerateRequest.getPatternName();
        String generationType = patternGenerateRequest.getGenerationType();
        String description = patternGenerateRequest.getDescription();
        String referenceImageUrl = patternGenerateRequest.getReferenceImageUrl();
        String serviceType = patternGenerateRequest.getServiceType(); // 获取服务类型

        // 默认使用千问服务
        if (StrUtil.isBlank(serviceType)) {
            serviceType = "qwen";
        }

        // 校验生成类型
        GenerationTypeEnum typeEnum = GenerationTypeEnum.getEnumByValue(generationType);
        ThrowUtils.throwIf(typeEnum == null, ErrorCode.PARAMS_ERROR, "生成类型不合法");

        // 根据生成类型校验必要参数
        if (GenerationTypeEnum.TEXT_GENERATED.equals(typeEnum)) {
            ThrowUtils.throwIf(StrUtil.isBlank(description), ErrorCode.PARAMS_ERROR, "文字描述不能为空");
        } else if (GenerationTypeEnum.IMAGE_REFERENCED.equals(typeEnum)) {
            ThrowUtils.throwIf(StrUtil.isBlank(referenceImageUrl), ErrorCode.PARAMS_ERROR, "参考图片URL不能为空");
        }

        File generatedImageFile = null;
        String finalReferenceImageUrl = null; // 用于保存到数据库的参考图片URL
        try {
            // 根据serviceType选择不同AI服务
            if ("doubao".equals(serviceType)) {
                // 使用豆包服务，根据doubaoMode选择不同的生成方式
                String doubaoMode = patternGenerateRequest.getDoubaoMode();
                if (StrUtil.isBlank(doubaoMode)) {
                    doubaoMode = "single_text"; // 默认文生图
                }

                switch (doubaoMode) {
                    case "single_text": // 文生图（单张）
                        generatedImageFile = douBaoImage.generateImageByText(
                                description,
                                patternGenerateRequest.getSize(),
                                true
                        );
                        break;
                    case "single_image": // 图生图（单张）
                        generatedImageFile = douBaoImage.generateImageByReference(
                                referenceImageUrl,
                                description,
                                patternGenerateRequest.getSize()
                        );
                        // 处理参考图片URL
                        if (referenceImageUrl.startsWith("data:image")) {
                            finalReferenceImageUrl = uploadBase64ToCos(referenceImageUrl, loginUser.getId());
                        } else {
                            finalReferenceImageUrl = referenceImageUrl;
                        }
                        break;
                    case "multi_image": // 多图生图（单张）
                        java.util.List<String> imageUrls = patternGenerateRequest.getReferenceImageUrls();
                        if (imageUrls == null || imageUrls.size() < 2) {
                            throw new BusinessException(ErrorCode.PARAMS_ERROR, "多图生图至少需要2张图片");
                        }
                        // 处理base64图片
                        java.util.List<String> processedUrls = new java.util.ArrayList<>();
                        for (String url : imageUrls) {
                            if (url.startsWith("data:image")) {
                                processedUrls.add(uploadBase64ToCos(url, loginUser.getId()));
                            } else {
                                processedUrls.add(url);
                            }
                        }
                        generatedImageFile = douBaoImage.generateImageByMultipleReferences(
                                processedUrls,
                                description,
                                patternGenerateRequest.getSize()
                        );
                        break;
                    case "batch_text": // 文生组图（多张）
                        java.util.List<File> batchFiles = douBaoImage.generateImagesByText(
                                description,
                                patternGenerateRequest.getMaxImages(),
                                patternGenerateRequest.getSize()
                        );
                        if (batchFiles != null && !batchFiles.isEmpty()) {
                            // 保存所有生成的图片
                            return saveBatchPatterns(batchFiles, patternGenerateRequest, loginUser, finalReferenceImageUrl);
                        }
                        break;
                    case "batch_single_image": // 单图生组图（多张）
                        java.util.List<File> batchSingleFiles = douBaoImage.generateImagesByReference(
                                referenceImageUrl,
                                description,
                                patternGenerateRequest.getMaxImages(),
                                patternGenerateRequest.getSize()
                        );
                        if (referenceImageUrl.startsWith("data:image")) {
                            finalReferenceImageUrl = uploadBase64ToCos(referenceImageUrl, loginUser.getId());
                        } else {
                            finalReferenceImageUrl = referenceImageUrl;
                        }
                        if (batchSingleFiles != null && !batchSingleFiles.isEmpty()) {
                            return saveBatchPatterns(batchSingleFiles, patternGenerateRequest, loginUser, finalReferenceImageUrl);
                        }
                        break;
                    case "batch_multi_image": // 多图生组图（多张）
                        java.util.List<String> multiImageUrls = patternGenerateRequest.getReferenceImageUrls();
                        if (multiImageUrls == null || multiImageUrls.size() < 2) {
                            throw new BusinessException(ErrorCode.PARAMS_ERROR, "多图生组图至少需要2张图片");
                        }
                        java.util.List<String> processedMultiUrls = new java.util.ArrayList<>();
                        for (String url : multiImageUrls) {
                            if (url.startsWith("data:image")) {
                                processedMultiUrls.add(uploadBase64ToCos(url, loginUser.getId()));
                            } else {
                                processedMultiUrls.add(url);
                            }
                        }
                        java.util.List<File> batchMultiFiles = douBaoImage.generateImagesByMultipleReferences(
                                processedMultiUrls,
                                description,
                                patternGenerateRequest.getMaxImages(),
                                patternGenerateRequest.getSize()
                        );
                        if (batchMultiFiles != null && !batchMultiFiles.isEmpty()) {
                            return saveBatchPatterns(batchMultiFiles, patternGenerateRequest, loginUser, finalReferenceImageUrl);
                        }
                        break;
                    default:
                        throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的豆包生成模式");
                }
                finalReferenceImageUrl = null;
            } else {
                // 使用千问服务（支持文生图和图生图）
                if (GenerationTypeEnum.TEXT_GENERATED.equals(typeEnum)) {
                    // 文字生成模式，不需要参考图片URL
                    generatedImageFile = qwenImage.generateImageByText(
                            description,
                            patternGenerateRequest.getSize(),
                            patternGenerateRequest.getNegativePrompt(),
                            patternGenerateRequest.getPromptExtend()
                    );
                    // 文生图模式，参考图片URL为空
                    finalReferenceImageUrl = null;
                } else if (GenerationTypeEnum.IMAGE_REFERENCED.equals(typeEnum)) {
                    // 图片参考生成模式
                    // 如果是base64图片，会在generateImageByReference方法中上传到COS并返回COS URL
                    generatedImageFile = qwenImage.generateImageByReference(
                            referenceImageUrl,
                            description,
                            patternGenerateRequest.getSize()
                    );
                    
                    // 如果原始URL是base64，需要上传到COS并获取URL用于保存到数据库
                    if (referenceImageUrl.startsWith("data:image")) {
                        // base64图片已经在generateImageByReference中上传到COS
                        // 需要重新上传一份作为参考图片记录（或者从generateImageByReference返回COS URL）
                        try {
                            // 解码base64
                            String base64Data = referenceImageUrl;
                            if (base64Data.contains(",")) {
                                base64Data = base64Data.split(",")[1];
                            }
                            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
                            
                            // 保存为临时文件
                            File refTempFile = File.createTempFile("ref_save_", ".png");
                            java.nio.file.Files.write(refTempFile.toPath(), imageBytes);
                            
                            // 上传到COS作为参考图片记录
                            String refKey = "reference/" + loginUser.getId() + "/" + System.currentTimeMillis() + ".png";
                            cosUtils.putObject(refKey, refTempFile);
                            finalReferenceImageUrl = cosClientConfig.getHost() + "/" + refKey;
                            
                            // 删除临时文件
                            deleteTempFile(refTempFile);
                        } catch (Exception e) {
                            log.error("上传参考图片到COS失败", e);
                            // 如果上传失败，使用null，不影响主流程
                            finalReferenceImageUrl = null;
                        }
                    } else {
                        // 如果是URL，直接使用
                        finalReferenceImageUrl = referenceImageUrl;
                    }
                }
            }
            
            // 上传到COS并生成缩略图
            String key = "pattern/" + loginUser.getId() + "/" + System.currentTimeMillis() + ".png";
            String generatedPatternUrl;
            String thumbUrl;
            Integer fileSize;
            
            if (generatedImageFile != null && generatedImageFile.exists()) {
                // 使用CosUtils上传图片，自动生成缩略图
                PutObjectResult putResult = cosUtils.putPictureObject(key, generatedImageFile);
                
                // 设置文件大小
                fileSize = (int) generatedImageFile.length();
                
                // 从COS处理结果中获取实际的图片URL
                try {
                    // 获取COS图片处理结果
                    com.qcloud.cos.model.ciModel.persistence.CIUploadResult ciResult = putResult.getCiUploadResult();
                    if (ciResult != null && ciResult.getProcessResults() != null) {
                        com.qcloud.cos.model.ciModel.persistence.ProcessResults processResults = ciResult.getProcessResults();
                        java.util.List<com.qcloud.cos.model.ciModel.persistence.CIObject> objectList = processResults.getObjectList();
                        
                        if (objectList != null && !objectList.isEmpty()) {
                            // 第一个是压缩后的webp图片
                            com.qcloud.cos.model.ciModel.persistence.CIObject compressedObject = objectList.get(0);
                            generatedPatternUrl = cosClientConfig.getHost() + "/" + compressedObject.getKey();
                            
                            // 如果有第二个，则是缩略图（仅当文件>20KB时）
                            if (objectList.size() > 1) {
                                com.qcloud.cos.model.ciModel.persistence.CIObject thumbnailObject = objectList.get(1);
                                thumbUrl = cosClientConfig.getHost() + "/" + thumbnailObject.getKey();
                            } else {
                                // 文件<=20KB，使用压缩图作为缩略图
                                thumbUrl = generatedPatternUrl;
                            }
                        } else {
                            // 如果没有处理结果，使用原图
                            generatedPatternUrl = cosClientConfig.getHost() + "/" + key;
                            thumbUrl = generatedPatternUrl;
                        }
                    } else {
                        // 如果没有COS处理结果，使用原图
                        generatedPatternUrl = cosClientConfig.getHost() + "/" + key;
                        thumbUrl = generatedPatternUrl;
                    }
                } catch (Exception e) {
                    // 如果获取处理结果失败，使用原图
                    log.warn("获取COS处理结果失败: {}", e.getMessage());
                    generatedPatternUrl = cosClientConfig.getHost() + "/" + key;
                    thumbUrl = generatedPatternUrl;
                }
            } else {
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "AI图片生成失败,生成的图片为空！");
            }

            // 创建图案实体
            Pattern pattern = new Pattern();
            pattern.setUserId(loginUser.getId());
            pattern.setPatternName(patternName);
            pattern.setDescription(description);
            pattern.setGenerationType(generationType);
            pattern.setReferenceImageUrl(finalReferenceImageUrl); // 使用处理后的URL（文生图为null，图生图为COS URL）
            pattern.setPatternUrl(generatedPatternUrl);
            pattern.setThumbUrl(thumbUrl);
            pattern.setStyle(patternGenerateRequest.getStyle());
            pattern.setSeason(patternGenerateRequest.getSeason());
            pattern.setTargetAudience(patternGenerateRequest.getTargetAudience());
            //如果是管理员则直接通过
            if(loginUser.getUserRole().equals(UserRoleEnum.ADMIN.getValue())){
                pattern.setAuditStatus(AuditStatusEnum.APPROVED.getValue());
            }else{
                pattern.setAuditStatus(AuditStatusEnum.PENDING.getValue());
            }

            pattern.setFileType("image/png");
            pattern.setFileSize(fileSize);

            // 校验数据
            validPattern(pattern, true);

            // 保存到数据库
            boolean result = this.save(pattern);
            ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图案生成失败");

            return pattern.getId();
        } finally {
            // 删除临时文件，清理资源
            deleteTempFile(generatedImageFile);
        }
    }

    @Override
    public boolean auditPattern(Long id, String auditStatus, String rejectReason, Long auditorId) {
        // 校验参数
        ThrowUtils.throwIf(id == null || id <= 0, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(StrUtil.isBlank(auditStatus), ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(auditorId == null, ErrorCode.PARAMS_ERROR);

        // 校验审核状态
        AuditStatusEnum statusEnum = AuditStatusEnum.getEnumByValue(auditStatus);
        ThrowUtils.throwIf(statusEnum == null, ErrorCode.PARAMS_ERROR, "审核状态不合法");
        ThrowUtils.throwIf(AuditStatusEnum.PENDING.equals(statusEnum), ErrorCode.PARAMS_ERROR, "不能设置为待审核状态");

        // 如果是拒绝，必须填写拒绝原因
        if (AuditStatusEnum.REJECTED.equals(statusEnum)) {
            ThrowUtils.throwIf(StrUtil.isBlank(rejectReason), ErrorCode.PARAMS_ERROR, "拒绝时必须填写拒绝原因");
        }

        // 查询图案是否存在
        Pattern pattern = this.getById(id);
        ThrowUtils.throwIf(pattern == null, ErrorCode.NOT_FOUND_ERROR, "图案不存在");

        // 更新审核信息
        Pattern updatePattern = new Pattern();
        updatePattern.setId(id);
        updatePattern.setAuditStatus(auditStatus);
        updatePattern.setAuditTime(new Date());
        updatePattern.setAuditorId(auditorId);
        updatePattern.setRejectReason(rejectReason);

        return this.updateById(updatePattern);
    }

    @Override
    public void validPattern(Pattern pattern, boolean add) {
        ThrowUtils.throwIf(pattern == null, ErrorCode.PARAMS_ERROR);

        String patternName = pattern.getPatternName();
        String generationType = pattern.getGenerationType();
        String auditStatus = pattern.getAuditStatus();

        // 创建时校验
        if (add) {
            ThrowUtils.throwIf(StrUtil.isBlank(patternName), ErrorCode.PARAMS_ERROR, "图案名称不能为空");
            ThrowUtils.throwIf(StrUtil.isBlank(generationType), ErrorCode.PARAMS_ERROR, "生成类型不能为空");
        }

        // 校验图案名称长度
        if (StrUtil.isNotBlank(patternName) && patternName.length() > 255) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图案名称过长");
        }

        // 校验生成类型
        if (StrUtil.isNotBlank(generationType)) {
            GenerationTypeEnum typeEnum = GenerationTypeEnum.getEnumByValue(generationType);
            ThrowUtils.throwIf(typeEnum == null, ErrorCode.PARAMS_ERROR, "生成类型不合法");
        }

        // 校验审核状态
        if (StrUtil.isNotBlank(auditStatus)) {
            AuditStatusEnum statusEnum = AuditStatusEnum.getEnumByValue(auditStatus);
            ThrowUtils.throwIf(statusEnum == null, ErrorCode.PARAMS_ERROR, "审核状态不合法");
        }
    }

    @Override
    public QueryWrapper<Pattern> getQueryWrapper(PatternQueryRequest patternQueryRequest) {
        QueryWrapper<Pattern> queryWrapper = new QueryWrapper<>();
        if (patternQueryRequest == null) {
            return queryWrapper;
        }

        Long id = patternQueryRequest.getId();
        Long userId = patternQueryRequest.getUserId();
        String patternName = patternQueryRequest.getPatternName();
        String generationType = patternQueryRequest.getGenerationType();
        String style = patternQueryRequest.getStyle();
        String season = patternQueryRequest.getSeason();
        String targetAudience = patternQueryRequest.getTargetAudience();
        String auditStatus = patternQueryRequest.getAuditStatus();
        String sortField = patternQueryRequest.getSortField();
        String sortOrder = patternQueryRequest.getSortOrder();

        // 拼接查询条件
        queryWrapper.eq(id != null, "id", id);
        queryWrapper.eq(userId != null, "userId", userId);
        queryWrapper.like(StrUtil.isNotBlank(patternName), "patternName", patternName);
        queryWrapper.eq(StrUtil.isNotBlank(generationType), "generationType", generationType);
        queryWrapper.eq(StrUtil.isNotBlank(style), "style", style);
        queryWrapper.eq(StrUtil.isNotBlank(season), "season", season);
        queryWrapper.eq(StrUtil.isNotBlank(targetAudience), "targetAudience", targetAudience);
        queryWrapper.eq(StrUtil.isNotBlank(auditStatus), "auditStatus", auditStatus);

        // 排序
        queryWrapper.orderBy(StrUtil.isNotEmpty(sortField), sortOrder.equals("ascend"), sortField);
        return queryWrapper;
    }

    @Override
    public PatternVO getPatternVO(Pattern pattern) {
        if (pattern == null) {
            return null;
        }
        PatternVO patternVO = new PatternVO();
        BeanUtils.copyProperties(pattern, patternVO);

        // 关联查询用户信息
        Long userId = pattern.getUserId();
        if (userId != null && userId > 0) {
            User user = userService.getById(userId);
            UserVO userVO = userService.getUserVO(user);
            patternVO.setUser(userVO);
        }

        return patternVO;
    }

    @Override
    public List<PatternVO> getPatternVOList(List<Pattern> patternList) {
        if (CollUtil.isEmpty(patternList)) {
            return List.of();
        }

        // 批量查询用户信息
        Set<Long> userIdSet = patternList.stream()
                .map(Pattern::getUserId)
                .collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        // 填充信息
        return patternList.stream().map(pattern -> {
            PatternVO patternVO = new PatternVO();
            BeanUtils.copyProperties(pattern, patternVO);
            Long userId = pattern.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            patternVO.setUser(userService.getUserVO(user));
            return patternVO;
        }).collect(Collectors.toList());
    }

    @Override
    public void editPicture(PatternEditRequest patternEditRequest, User loginUser) {
        // 在此处将实体类和 DTO 进行转换
        Pattern pattern = new Pattern();
        BeanUtils.copyProperties(patternEditRequest, pattern);
        // 设置编辑时间
        pattern.setUpdateTime(new Date());
        // 数据校验
        this.validPattern(pattern);
        // 判断是否存在
        long id = patternEditRequest.getId();
        Pattern oldPattern = this.getById(id);
        ThrowUtils.throwIf(oldPattern == null, ErrorCode.NOT_FOUND_ERROR);
        // 校验权限
        checkPictureAuth(loginUser, oldPattern);
        // 补充审核参数
        this.fillReviewParams(pattern, loginUser);
        // 操作数据库
        boolean result = this.updateById(pattern);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
    }
    /**
     * 校验图案参数
     */
    private void validPattern(Pattern pattern) {
        if (pattern == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String patternName = pattern.getPatternName();
        if (StringUtils.isBlank(patternName)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图案名称不能为空");
        }
        if (patternName.length() > 255) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图案名称过长");
        }
        String description = pattern.getDescription();
        if (StringUtils.isNotBlank(description) && description.length() > 1024) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "图案描述过长");
        }
    }

    /**
     * 删除临时文件
     */
    private void deleteTempFile(File file) {
        if (file != null && file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                log.warn("临时文件删除失败: {}", file.getAbsolutePath());
            } else {
                log.debug("临时文件已删除: {}", file.getAbsolutePath());
            }
        }
    }

    /**
     * 上传base64图片到COS
     */
    private String uploadBase64ToCos(String base64Image, Long userId) {
        try {
            // 解码base64
            String base64Data = base64Image;
            if (base64Data.contains(",")) {
                base64Data = base64Data.split(",")[1];
            }
            byte[] imageBytes = java.util.Base64.getDecoder().decode(base64Data);
            
            // 保存为临时文件
            File refTempFile = File.createTempFile("ref_save_", ".png");
            java.nio.file.Files.write(refTempFile.toPath(), imageBytes);
            
            // 上传到COS作为参考图片记录
            String refKey = "reference/" + userId + "/" + System.currentTimeMillis() + ".png";
            cosUtils.putObject(refKey, refTempFile);
            String cosUrl = cosClientConfig.getHost() + "/" + refKey;
            
            // 删除临时文件
            deleteTempFile(refTempFile);
            
            return cosUrl;
        } catch (Exception e) {
            log.error("上传参考图片到COS失败", e);
            return null;
        }
    }

    /**
     * 批量保存生成的图片（组图模式）
     * 返回第一张图片的ID，其他图片也会保存到数据库
     */
    private Long saveBatchPatterns(java.util.List<File> files, PatternGenerateRequest request, 
                                    User loginUser, String referenceImageUrl) {
        if (files == null || files.isEmpty()) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "生成的图片为空");
        }

        Long firstPatternId = null;
        try {
            for (int i = 0; i < files.size(); i++) {
                File imageFile = files.get(i);
                
                // 上传到COS
                String key = "pattern/" + loginUser.getId() + "/" + System.currentTimeMillis() + "_" + i + ".png";
                PutObjectResult putResult = cosUtils.putPictureObject(key, imageFile);
                Integer fileSize = (int) imageFile.length();
                
                // 获取COS处理结果
                String patternUrl;
                String thumbUrl;
                try {
                    com.qcloud.cos.model.ciModel.persistence.CIUploadResult ciResult = putResult.getCiUploadResult();
                    if (ciResult != null && ciResult.getProcessResults() != null) {
                        com.qcloud.cos.model.ciModel.persistence.ProcessResults processResults = ciResult.getProcessResults();
                        java.util.List<com.qcloud.cos.model.ciModel.persistence.CIObject> objectList = processResults.getObjectList();
                        
                        if (objectList != null && !objectList.isEmpty()) {
                            com.qcloud.cos.model.ciModel.persistence.CIObject compressedObject = objectList.get(0);
                            patternUrl = cosClientConfig.getHost() + "/" + compressedObject.getKey();
                            
                            if (objectList.size() > 1) {
                                com.qcloud.cos.model.ciModel.persistence.CIObject thumbnailObject = objectList.get(1);
                                thumbUrl = cosClientConfig.getHost() + "/" + thumbnailObject.getKey();
                            } else {
                                thumbUrl = patternUrl;
                            }
                        } else {
                            patternUrl = cosClientConfig.getHost() + "/" + key;
                            thumbUrl = patternUrl;
                        }
                    } else {
                        patternUrl = cosClientConfig.getHost() + "/" + key;
                        thumbUrl = patternUrl;
                    }
                } catch (Exception e) {
                    log.warn("获取COS处理结果失败: {}", e.getMessage());
                    patternUrl = cosClientConfig.getHost() + "/" + key;
                    thumbUrl = patternUrl;
                }
                
                // 创建图案实体
                Pattern pattern = new Pattern();
                pattern.setUserId(loginUser.getId());
                pattern.setPatternName(request.getPatternName() + " #" + (i + 1));
                pattern.setDescription(request.getDescription());
                pattern.setGenerationType(request.getGenerationType());
                pattern.setReferenceImageUrl(referenceImageUrl);
                pattern.setPatternUrl(patternUrl);
                pattern.setThumbUrl(thumbUrl);
                pattern.setStyle(request.getStyle());
                pattern.setSeason(request.getSeason());
                pattern.setTargetAudience(request.getTargetAudience());
                pattern.setFileType("image/png");
                pattern.setFileSize(fileSize);
                
                // 设置审核状态
                if(loginUser.getUserRole().equals(UserRoleEnum.ADMIN.getValue())){
                    pattern.setAuditStatus(AuditStatusEnum.APPROVED.getValue());
                } else {
                    pattern.setAuditStatus(AuditStatusEnum.PENDING.getValue());
                }
                
                // 校验数据
                validPattern(pattern, true);
                
                // 保存到数据库
                boolean result = this.save(pattern);
                ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR, "图案生成失败");
                
                // 记录第一张图片的ID
                if (i == 0) {
                    firstPatternId = pattern.getId();
                }
                
                log.info("已保存第 {} 张图片，ID: {}", i + 1, pattern.getId());
            }
        } finally {
            // 清理所有临时文件
            files.forEach(this::deleteTempFile);
        }
        
        return firstPatternId;
    }

    @Override
    public void checkPictureAuth(User loginUser, Pattern pattern) {
            // 只能修改自己的图案
            if (!pattern.getUserId().equals(loginUser.getId())) {
                throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
            }
        }
    @Override
    public void fillReviewParams(Pattern pattern, User loginUser) {
        if (userService.isAdmin(loginUser)) {
            // 管理员自动过审
            pattern.setAuditStatus(AuditStatusEnum.APPROVED.getValue());
            pattern.setAuditorId(loginUser.getId());
            pattern.setRejectReason("管理员自动过审");
            pattern.setAuditTime(new Date());
        } else {
            // 非管理员，创建或编辑都要改为待审核
            pattern.setAuditStatus(AuditStatusEnum.PENDING.getValue());
        }


    }
    }





