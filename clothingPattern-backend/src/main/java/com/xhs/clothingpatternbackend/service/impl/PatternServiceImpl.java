package com.xhs.clothingpatternbackend.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qcloud.cos.model.PutObjectResult;
import com.xhs.clothingpatternbackend.config.CosClientConfig;
import com.xhs.clothingpatternbackend.exception.BusinessException;
import com.xhs.clothingpatternbackend.exception.ErrorCode;
import com.xhs.clothingpatternbackend.exception.ThrowUtils;
import com.xhs.clothingpatternbackend.mapper.PatternMapper;
import com.xhs.clothingpatternbackend.model.dto.pattern.PatternGenerateRequest;
import com.xhs.clothingpatternbackend.model.dto.pattern.PatternQueryRequest;
import com.xhs.clothingpatternbackend.model.entity.Pattern;
import com.xhs.clothingpatternbackend.model.entity.User;
import com.xhs.clothingpatternbackend.model.enums.AuditStatusEnum;
import com.xhs.clothingpatternbackend.model.enums.GenerationTypeEnum;
import com.xhs.clothingpatternbackend.model.vo.PatternVO;
import com.xhs.clothingpatternbackend.model.vo.UserVO;
import com.xhs.clothingpatternbackend.service.PatternService;
import com.xhs.clothingpatternbackend.service.UserService;
import com.xhs.clothingpatternbackend.DashScopeSDK.QwenImage;
import com.xhs.clothingpatternbackend.utils.CosUtils;
import jakarta.annotation.Resource;
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


    @Override
    public Long generatePattern(PatternGenerateRequest patternGenerateRequest, User loginUser) {
        // 校验参数
        ThrowUtils.throwIf(patternGenerateRequest == null, ErrorCode.PARAMS_ERROR);
        ThrowUtils.throwIf(loginUser == null, ErrorCode.NOT_LOGIN_ERROR);

        String patternName = patternGenerateRequest.getPatternName();
        String generationType = patternGenerateRequest.getGenerationType();
        String description = patternGenerateRequest.getDescription();
        String referenceImageUrl = patternGenerateRequest.getReferenceImageUrl();

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
        try {
            // 调用AI图片生成服务
            if (GenerationTypeEnum.TEXT_GENERATED.equals(typeEnum)) {
                // 文字生成模式
                generatedImageFile = qwenImage.generateImageByText(
                        description,
                        patternGenerateRequest.getSize(),
                        patternGenerateRequest.getNegativePrompt(),
                        patternGenerateRequest.getPromptExtend()
                );
            } else if (GenerationTypeEnum.IMAGE_REFERENCED.equals(typeEnum)) {
                // 图片参考生成模式
                generatedImageFile = qwenImage.generateImageByReference(
                        referenceImageUrl,
                        description,
                        patternGenerateRequest.getSize()
                );
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
                    System.out.println("获取COS处理结果失败: " + e.getMessage());
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
            pattern.setReferenceImageUrl(referenceImageUrl);
            pattern.setPatternUrl(generatedPatternUrl);
            pattern.setThumbUrl(thumbUrl);
            pattern.setStyle(patternGenerateRequest.getStyle());
            pattern.setSeason(patternGenerateRequest.getSeason());
            pattern.setTargetAudience(patternGenerateRequest.getTargetAudience());
            pattern.setAuditStatus(AuditStatusEnum.PENDING.getValue());
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

    /**
     * 删除临时文件
     */
    private void deleteTempFile(File file) {
        if (file != null && file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                System.out.println("临时文件删除失败: " + file.getAbsolutePath());
            }
        }
    }
}




