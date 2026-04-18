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
import com.xhs.clothingpatternbackend.model.dto.pattern.DataExportRequest;
import com.xhs.clothingpatternbackend.model.dto.pattern.PatternEditRequest;
import com.xhs.clothingpatternbackend.model.dto.pattern.PatternGenerateRequest;
import com.xhs.clothingpatternbackend.model.dto.pattern.PatternQueryRequest;
import com.xhs.clothingpatternbackend.model.entity.Pattern;
import com.xhs.clothingpatternbackend.model.entity.User;
import com.xhs.clothingpatternbackend.model.enums.AuditStatusEnum;
import com.xhs.clothingpatternbackend.model.enums.GenerationTypeEnum;
import com.xhs.clothingpatternbackend.model.enums.UserRoleEnum;
import com.xhs.clothingpatternbackend.model.vo.CommentStatisticsVO;
import com.xhs.clothingpatternbackend.model.vo.HomeStatisticsVO;
import com.xhs.clothingpatternbackend.model.vo.PatternVO;
import com.xhs.clothingpatternbackend.model.vo.UserVO;
import com.xhs.clothingpatternbackend.service.CommentService;
import com.xhs.clothingpatternbackend.service.LikeService;
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
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author 小辛
 * @description 针对表【pattern(服装图案表（智能图案生成模块核心表）)】的数据库操作Service实现
 * @createDate 2025-11-21 15:44:19
 */
@Service
@Slf4j
public class PatternServiceImpl extends ServiceImpl<PatternMapper, Pattern>
        implements PatternService {

    @Resource
    private UserService userService;

    @Resource
    private LikeService likeService;

    @Resource
    private CommentService commentService;

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
        Integer maxImages = patternGenerateRequest.getMaxImages();

        // 校验生成类型
        GenerationTypeEnum typeEnum = GenerationTypeEnum.getEnumByValue(generationType);
        ThrowUtils.throwIf(typeEnum == null, ErrorCode.PARAMS_ERROR, "生成类型不合法");

        // 根据生成类型校验必要参数
        if (GenerationTypeEnum.TEXT_GENERATED.equals(typeEnum)) {
            ThrowUtils.throwIf(StrUtil.isBlank(description), ErrorCode.PARAMS_ERROR, "文字描述不能为空");
        } else if (GenerationTypeEnum.IMAGE_REFERENCED.equals(typeEnum)) {
            ThrowUtils.throwIf(StrUtil.isBlank(referenceImageUrl), ErrorCode.PARAMS_ERROR, "参考图片URL不能为空");
        }

        List<File> generatedImageFiles = null;
        String finalReferenceImageUrl = null; // 用于保存到数据库的参考图片URL

        try {
            // 使用千问服务（支持文生图和图生图，支持多图生成）
            if (GenerationTypeEnum.TEXT_GENERATED.equals(typeEnum)) {
                // 文字生成模式，不需要参考图片URL
                generatedImageFiles = qwenImage.generateImageByText(
                        description,
                        patternGenerateRequest.getSize(),
                        patternGenerateRequest.getNegativePrompt(),
                        patternGenerateRequest.getPromptExtend(),
                        maxImages);
                // 文生图模式，参考图片URL为空
                finalReferenceImageUrl = null;
            } else if (GenerationTypeEnum.IMAGE_REFERENCED.equals(typeEnum)) {
                // 图片参考生成模式
                generatedImageFiles = qwenImage.generateImageByReference(
                        referenceImageUrl,
                        description,
                        patternGenerateRequest.getSize(),
                        maxImages);

                // 如果原始URL是base64，需要上传到COS并获取URL用于保存到数据库
                if (referenceImageUrl.startsWith("data:image")) {
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

            // 如果生成了多张图片，批量保存
            if (generatedImageFiles != null && generatedImageFiles.size() > 1) {
                log.info("生成了 {} 张图片，开始批量保存", generatedImageFiles.size());
                return saveBatchPatterns(generatedImageFiles, patternGenerateRequest, loginUser, finalReferenceImageUrl);
            }

            // 单张图片处理（兼容旧逻辑）
            File generatedImageFile = (generatedImageFiles != null && !generatedImageFiles.isEmpty())
                    ? generatedImageFiles.get(0) : null;

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
                        com.qcloud.cos.model.ciModel.persistence.ProcessResults processResults = ciResult
                                .getProcessResults();
                        java.util.List<com.qcloud.cos.model.ciModel.persistence.CIObject> objectList = processResults
                                .getObjectList();

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
            this.fillReviewParams(pattern, loginUser);
            // //如果是管理员则直接通过
            // if(loginUser.getUserRole().equals(UserRoleEnum.ADMIN.getValue())){
            // pattern.setAuditStatus(AuditStatusEnum.APPROVED.getValue());
            // }else{
            // pattern.setAuditStatus(AuditStatusEnum.PENDING.getValue());
            // }

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
            if (generatedImageFiles != null) {
                generatedImageFiles.forEach(this::deleteTempFile);
            }
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
    public PatternVO getPatternVO(Pattern pattern, Long loginUserId) {
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

        // 填充点赞信息
        Long patternId = pattern.getId();
        if (patternId != null) {
            // 获取点赞数
            long likeCount = likeService.getLikeCount(patternId);
            patternVO.setLikeCount(likeCount);

            // 获取当前用户是否点赞
            if (loginUserId != null && loginUserId > 0) {
                boolean isLiked = likeService.getLikeStatus(loginUserId, patternId);
                patternVO.setIsLiked(isLiked);
            } else {
                patternVO.setIsLiked(false);
            }
        }

        return patternVO;
    }

    @Override
    public List<PatternVO> getPatternVOList(List<Pattern> patternList, Long loginUserId) {
        if (CollUtil.isEmpty(patternList)) {
            return List.of();
        }

        // 批量查询用户信息
        Set<Long> userIdSet = patternList.stream()
                .map(Pattern::getUserId)
                .collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));

        // 批量查询点赞信息
        Set<Long> patternIdSet = patternList.stream()
                .map(Pattern::getId)
                .collect(Collectors.toSet());

        // 获取每个图案的点赞数
        Map<Long, Long> likeCountMap = new HashMap<>();
        for (Long patternId : patternIdSet) {
            long count = likeService.getLikeCount(patternId);
            likeCountMap.put(patternId, count);
        }

        // 获取当前用户的点赞状态
        Map<Long, Boolean> userLikeMap = new HashMap<>();
        if (loginUserId != null && loginUserId > 0) {
            for (Long patternId : patternIdSet) {
                boolean isLiked = likeService.getLikeStatus(loginUserId, patternId);
                userLikeMap.put(patternId, isLiked);
            }
        }

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

            // 填充点赞信息
            Long patternId = pattern.getId();
            patternVO.setLikeCount(likeCountMap.getOrDefault(patternId, 0L));
            patternVO.setIsLiked(userLikeMap.getOrDefault(patternId, false));

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
                        com.qcloud.cos.model.ciModel.persistence.ProcessResults processResults = ciResult
                                .getProcessResults();
                        java.util.List<com.qcloud.cos.model.ciModel.persistence.CIObject> objectList = processResults
                                .getObjectList();

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
                if (loginUser.getUserRole().equals(UserRoleEnum.ADMIN.getValue())) {
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

    /**
     * 获取首页统计数据
     * 
     * @return
     */
    @Override
    public HomeStatisticsVO getHomeStatistics() {
        HomeStatisticsVO statisticsVO = new HomeStatisticsVO();
        // 1. 获取所有已通过审核的图案
        QueryWrapper<Pattern> approvedWrapper = new QueryWrapper<>();
        approvedWrapper.eq("auditStatus", AuditStatusEnum.APPROVED.getValue());
        approvedWrapper.eq("isDelete", 0);
        List<Pattern> approvedPatterns = this.list(approvedWrapper);

        // 2. 统计总图案数
        statisticsVO.setTotalPatterns((long) approvedPatterns.size());

        // 3. 统计总用户数
        long totalUsers = userService.count();
        statisticsVO.setTotalUsers(totalUsers);

        // 4. 统计热门风格分布
        Map<String, Long> styleDistribution = approvedPatterns.stream()
                .filter(pattern -> StrUtil.isNotBlank(pattern.getStyle()))
                .collect(Collectors.groupingBy(Pattern::getStyle, Collectors.counting()));
        statisticsVO.setStyleDistribution(styleDistribution);

        // 5. 统计活跃用户排行（前5名）
        Map<Long, Long> userPatternCount = approvedPatterns.stream()
                .collect(Collectors.groupingBy(Pattern::getUserId, Collectors.counting()));

        List<com.xhs.clothingpatternbackend.model.vo.HomeStatisticsVO.ActiveUserVO> activeUsers = userPatternCount
                .entrySet().stream()
                .sorted(Map.Entry.<Long, Long>comparingByValue().reversed())
                .limit(5)
                .map(entry -> {
                    com.xhs.clothingpatternbackend.model.vo.HomeStatisticsVO.ActiveUserVO activeUserVO = new com.xhs.clothingpatternbackend.model.vo.HomeStatisticsVO.ActiveUserVO();
                    User user = userService.getById(entry.getKey());
                    activeUserVO.setUser(userService.getUserVO(user));
                    activeUserVO.setPatternCount(entry.getValue());
                    return activeUserVO;
                })
                .collect(Collectors.toList());
        statisticsVO.setActiveUsers(activeUsers);

        // 6. 统计创作趋势（最近7天）
        java.time.LocalDate today = java.time.LocalDate.now();
        java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("yyyy-MM-dd");

        List<com.xhs.clothingpatternbackend.model.vo.HomeStatisticsVO.TrendDataVO> trendData = new java.util.ArrayList<>();
        for (int i = 6; i >= 0; i--) {
            java.time.LocalDate date = today.minusDays(i);
            String dateStr = date.format(formatter);

            // 统计当天创建的图案数量
            long count = approvedPatterns.stream()
                    .filter(pattern -> {
                        if (pattern.getCreateTime() == null)
                            return false;
                        java.time.LocalDate createDate = pattern.getCreateTime().toInstant()
                                .atZone(java.time.ZoneId.systemDefault())
                                .toLocalDate();
                        return createDate.equals(date);
                    })
                    .count();

            com.xhs.clothingpatternbackend.model.vo.HomeStatisticsVO.TrendDataVO trendDataVO = new com.xhs.clothingpatternbackend.model.vo.HomeStatisticsVO.TrendDataVO();
            trendDataVO.setDate(dateStr);
            trendDataVO.setCount(count);
            trendData.add(trendDataVO);
        }
        statisticsVO.setTrendData(trendData);

        return statisticsVO;
    }

    @Override
    public void exportDataReport(DataExportRequest exportRequest, OutputStream outputStream) throws IOException {
        // 获取统计数据
        HomeStatisticsVO statisticsVO = getHomeStatistics();

        // 根据导出格式调用不同的导出方法
        String format = exportRequest.getFormat();
        if ("csv".equalsIgnoreCase(format)) {
            exportToCsv(statisticsVO, outputStream);
        } else if ("excel".equalsIgnoreCase(format)) {
            // Excel格式也使用CSV（Excel可以直接打开CSV文件）
            exportToCsv(statisticsVO, outputStream);
        } else if ("pdf".equalsIgnoreCase(format)) {
            // PDF格式使用文本格式（简化实现）
            exportToPdf(statisticsVO, outputStream);
        } else {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "不支持的导出格式：" + format);
        }
    }

    /**
     * 导出CSV格式报告
     */
    private void exportToCsv(HomeStatisticsVO statisticsVO, OutputStream outputStream) throws IOException {
        java.io.PrintWriter writer = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(outputStream, java.nio.charset.StandardCharsets.UTF_8));

        // 写入UTF-8 BOM，让Excel正确识别中文
        outputStream.write(new byte[] { (byte) 0xEF, (byte) 0xBB, (byte) 0xBF });

        // 报告标题
        writer.println("服装图案平台数据分析报告");
        writer.println("生成时间," + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        writer.println();

        // 一、总体概况
        writer.println("===== 一、总体概况 =====");
        writer.println("指标,数值");
        writer.println("总图案数," + (statisticsVO.getTotalPatterns() != null ? statisticsVO.getTotalPatterns() : 0));
        writer.println("总用户数," + (statisticsVO.getTotalUsers() != null ? statisticsVO.getTotalUsers() : 0));
        writer.println();

        // 二、风格分布统计
        writer.println("===== 二、风格分布统计 =====");
        writer.println("风格,图案数量,占比");
        if (statisticsVO.getStyleDistribution() != null && !statisticsVO.getStyleDistribution().isEmpty()) {
            long totalPatterns = statisticsVO.getTotalPatterns() != null ? statisticsVO.getTotalPatterns() : 0;
            statisticsVO.getStyleDistribution().forEach((style, count) -> {
                double percentage = totalPatterns > 0 ? (count.doubleValue() / totalPatterns * 100) : 0;
                writer.println(String.format("%s,%d,%.2f%%", style, count, percentage));
            });
        } else {
            writer.println("暂无数据,-,-");
        }
        writer.println();

        // 三、活跃用户TOP5
        writer.println("===== 三、活跃用户TOP5 =====");
        writer.println("排名,用户名,账号,图案数量");
        if (statisticsVO.getActiveUsers() != null && !statisticsVO.getActiveUsers().isEmpty()) {
            for (int i = 0; i < statisticsVO.getActiveUsers().size(); i++) {
                HomeStatisticsVO.ActiveUserVO activeUser = statisticsVO.getActiveUsers().get(i);
                String userName = activeUser.getUser() != null ? activeUser.getUser().getUserName() : "未知";
                String userAccount = activeUser.getUser() != null ? activeUser.getUser().getUserAccount() : "-";
                Long patternCount = activeUser.getPatternCount() != null ? activeUser.getPatternCount() : 0;
                writer.println(String.format("%d,%s,%s,%d", i + 1, userName, userAccount, patternCount));
            }
        } else {
            writer.println("暂无数据,-,-,-");
        }
        writer.println();

        // 四、创作趋势（最近7天）
        writer.println("===== 四、创作趋势（最近7天） =====");
        writer.println("日期,图案数量");
        if (statisticsVO.getTrendData() != null && !statisticsVO.getTrendData().isEmpty()) {
            statisticsVO.getTrendData().forEach(trend -> {
                String date = trend.getDate() != null ? trend.getDate() : "-";
                Long count = trend.getCount() != null ? trend.getCount() : 0;
                writer.println(String.format("%s,%d", date, count));
            });
        } else {
            writer.println("暂无数据,-");
        }
        writer.println();

        // 五、数据统计汇总
        writer.println("===== 五、数据统计汇总 =====");
        if (statisticsVO.getTrendData() != null && !statisticsVO.getTrendData().isEmpty()) {
            long totalTrendCount = statisticsVO.getTrendData().stream()
                    .mapToLong(t -> t.getCount() != null ? t.getCount() : 0)
                    .sum();
            double avgDailyCount = totalTrendCount / 7.0;
            writer.println("指标,数值");
            writer.println("近7天总图案数," + totalTrendCount);
            writer.println(String.format("日均图案数,%.2f", avgDailyCount));
        }

        writer.flush();
    }

    /**
     * 导出PDF格式报告（文本格式）
     */
    private void exportToPdf(HomeStatisticsVO statisticsVO, OutputStream outputStream) throws IOException {
        java.io.PrintWriter writer = new java.io.PrintWriter(
                new java.io.OutputStreamWriter(outputStream, java.nio.charset.StandardCharsets.UTF_8));

        // 报告头部
        writer.println("===============================================");
        writer.println("       服装图案平台数据分析报告");
        writer.println("===============================================");
        writer.println("生成时间：" + new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(new Date()));
        writer.println("报告类型：综合数据统计报告");
        writer.println();

        // 一、总体概况
        writer.println("【一、总体概况】");
        writer.println(
                "  • 总图案数：" + (statisticsVO.getTotalPatterns() != null ? statisticsVO.getTotalPatterns() : 0) + " 个");
        writer.println("  • 总用户数：" + (statisticsVO.getTotalUsers() != null ? statisticsVO.getTotalUsers() : 0) + " 人");
        writer.println();

        // 二、风格分布分析
        writer.println("【二、风格分布分析】");
        if (statisticsVO.getStyleDistribution() != null && !statisticsVO.getStyleDistribution().isEmpty()) {
            long totalPatterns = statisticsVO.getTotalPatterns() != null ? statisticsVO.getTotalPatterns() : 0;
            statisticsVO.getStyleDistribution().forEach((style, count) -> {
                double percentage = totalPatterns > 0 ? (count.doubleValue() / totalPatterns * 100) : 0;
                writer.println(String.format("  • %-8s : %4d 个 (%.2f%%)", style, count, percentage));
            });
        } else {
            writer.println("  暂无风格分布数据");
        }
        writer.println();

        // 三、活跃用户榜单
        writer.println("【三、活跃用户榜单】");
        if (statisticsVO.getActiveUsers() != null && !statisticsVO.getActiveUsers().isEmpty()) {
            for (int i = 0; i < statisticsVO.getActiveUsers().size(); i++) {
                HomeStatisticsVO.ActiveUserVO activeUser = statisticsVO.getActiveUsers().get(i);
                String userName = activeUser.getUser() != null ? activeUser.getUser().getUserName() : "未知用户";
                Long patternCount = activeUser.getPatternCount() != null ? activeUser.getPatternCount() : 0;
                String medal = i == 0 ? "🥇" : i == 1 ? "🥈" : i == 2 ? "🥉" : "  ";
                writer.println(String.format("  %s TOP%d. %-12s - %d 个图案", medal, i + 1, userName, patternCount));
            }
        } else {
            writer.println("  暂无活跃用户数据");
        }
        writer.println();

        // 四、创作趋势图表
        writer.println("【四、创作趋势图表（最近7天）】");
        if (statisticsVO.getTrendData() != null && !statisticsVO.getTrendData().isEmpty()) {
            // 找出最大值用于绘制简易图表
            long maxCount = statisticsVO.getTrendData().stream()
                    .mapToLong(t -> t.getCount() != null ? t.getCount() : 0)
                    .max()
                    .orElse(1);

            for (HomeStatisticsVO.TrendDataVO trend : statisticsVO.getTrendData()) {
                String date = trend.getDate() != null ? trend.getDate() : "-";
                Long count = trend.getCount() != null ? trend.getCount() : 0;
                // 简易柱状图（最多20个字符）
                int barLength = maxCount > 0 ? (int) (count * 20.0 / maxCount) : 0;
                String bar = "█".repeat(Math.max(0, barLength));
                writer.println(String.format("  %s | %-20s %d", date, bar, count));
            }

            // 统计汇总
            long totalTrendCount = statisticsVO.getTrendData().stream()
                    .mapToLong(t -> t.getCount() != null ? t.getCount() : 0)
                    .sum();
            double avgDailyCount = totalTrendCount / 7.0;
            writer.println();
            writer.println("  趋势统计：");
            writer.println("    - 近7天总计：" + totalTrendCount + " 个图案");
            writer.println(String.format("    - 日均创作：%.2f 个图案", avgDailyCount));
        } else {
            writer.println("  暂无趋势数据");
        }
        writer.println();

        // 报告尾部
        writer.println("===============================================");
        writer.println("              报告生成完成");
        writer.println("===============================================");

        writer.flush();
    }

    @Override
    public List<Map<String, Object>> getTargetAudienceTopFive() {
        // 查询条件：targetAudience不为空
        QueryWrapper<Pattern> queryWrapper = new QueryWrapper<>();
        queryWrapper.isNotNull("targetAudience");
        queryWrapper.ne("targetAudience", "");

        // 按targetAudience分组，统计数量，按数量降序排序，取前5个
        queryWrapper.select("targetAudience", "count(*) as count");
        queryWrapper.groupBy("targetAudience");
        queryWrapper.orderByDesc("count");
        queryWrapper.last("limit 5");

        // 执行查询
        List<Map<String, Object>> result = this.baseMapper.selectMaps(queryWrapper);

        return result;
    }

    /**
     * 获取最热门的服装风格
     * 
     * @return
     */
    @Override
    public List<Map<String, Object>> getHotStyleTopFive() {
        QueryWrapper<Pattern> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("style", "count(*) as count");
        queryWrapper.groupBy("style");
        queryWrapper.orderByDesc("count");
        queryWrapper.last("limit 5");
        List<Map<String, Object>> result = this.baseMapper.selectMaps(queryWrapper);
        return result;
    }

    /**
     * 获取互动数据
     * 
     * @return
     */
    @Override
    public List<Map<String, Object>> getInteraction() {
        // 查询所有审核通过的作品
        QueryWrapper<Pattern> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("auditStatus", AuditStatusEnum.APPROVED.getValue());
        List<Pattern> patternList = this.list(queryWrapper);

        // 如果没有作品，返回空列表
        if (CollUtil.isEmpty(patternList)) {
            return new ArrayList<>();
        }

        // 计算每个作品的总分
        List<Map<String, Object>> scoreList = new ArrayList<>();
        for (Pattern pattern : patternList) {
            // 获取评论统计信息
            CommentStatisticsVO commentStats = commentService.getCommentStatistics(pattern.getId());
            int commentCount = commentStats.getTotalComments() != null ? commentStats.getTotalComments() : 0;

            // 获取点赞数
            long likeCount = likeService.getLikeCount(pattern.getId());

            // 计算总分（评论占0.4，点赞占0.6）
            double score = commentCount * 0.4 + likeCount * 0.6;

            // 构建结果Map
            Map<String, Object> scoreMap = new HashMap<>();
            scoreMap.put("patternName", pattern.getPatternName());
            scoreMap.put("score", Math.round(score * 100.0) / 100.0); // 保留两位小数

            scoreList.add(scoreMap);
        }

        // 按分数降序排序，取前5名
        return scoreList.stream()
                .sorted((a, b) -> Double.compare((Double) b.get("score"), (Double) a.get("score")))
                .limit(5)
                .collect(Collectors.toList());
    }

    /**
     * 获取服装风格偏好
     * 
     * @return
     */

    // 日期格式化器（统一返回yyyy-MM-dd格式）
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    // 系统默认时区
    private static final ZoneId DEFAULT_ZONE = ZoneId.systemDefault();
    @Override
    public List<Map<String, Object>> getStylePreference() {
        // 最终返回结果：每个元素对应一天的统计（日期+当日风格Top5）
        List<Map<String, Object>> dailyResultList = new ArrayList<>();

        // 1. 生成过去7天的日期（包含今天，共7天）
        LocalDate today = LocalDate.now();
        for (int i = 6; i >= 0; i--) { // 从7天前到今天，倒序遍历（也可正序，根据需求调整）
            LocalDate currentDate = today.minusDays(i);
            String dateStr = currentDate.format(DATE_FORMATTER);

            // 2. 构建当天的时间范围：00:00:00 到 23:59:59
            LocalDateTime dayStart = currentDate.atStartOfDay(); // 当天0点
            LocalDateTime dayEnd = currentDate.atTime(23, 59, 59); // 当天23:59:59
            Date startTime = Date.from(dayStart.atZone(DEFAULT_ZONE).toInstant());
            Date endTime = Date.from(dayEnd.atZone(DEFAULT_ZONE).toInstant());

            // 3. 查询当天符合条件的图案（审核通过、未删除、有风格）
            QueryWrapper<Pattern> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("isDelete", 0)
                    .eq("auditStatus", "APPROVED")
                    .ge("createTime", startTime)
                    .le("createTime", endTime)
                    .isNotNull("style")
                    .ne("style", "");

            List<Pattern> patternList = this.list(queryWrapper);

            // 4. 统计当天各风格的数量
            Map<String, Long> styleCountMap = new HashMap<>();
            for (Pattern pattern : patternList) {
                String style = pattern.getStyle().trim(); // 防空格干扰
                if (!style.isEmpty()) {
                    styleCountMap.put(style, styleCountMap.getOrDefault(style, 0L) + 1);
                }
            }

            // 5. 对当天风格数量降序排序，取前5名
            List<Map<String, Object>> dayTop5 = styleCountMap.entrySet().stream()
                    .sorted((e1, e2) -> Long.compare(e2.getValue(), e1.getValue())) // 降序
                    .limit(5)
                    .map(entry -> {
                        Map<String, Object> styleMap = new HashMap<>();
                        styleMap.put("style", entry.getKey());
                        styleMap.put("count", entry.getValue());
                        return styleMap;
                    })
                    .collect(Collectors.toList());

            // 6. 封装当天的统计结果
            Map<String, Object> dailyMap = new HashMap<>();
            dailyMap.put("date", dateStr); // 日期（yyyy-MM-dd）
            dailyMap.put("topStyles", dayTop5); // 当天风格Top5（空则返回空列表）
            dailyResultList.add(dailyMap);
        }

        return dailyResultList;
    }
}
