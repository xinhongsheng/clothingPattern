package com.xhs.clothingpatternbackend.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.xhs.clothingpatternbackend.annotation.AuthCheck;
import com.xhs.clothingpatternbackend.common.BaseResponse;
import com.xhs.clothingpatternbackend.common.DeleteRequest;
import com.xhs.clothingpatternbackend.common.ResultUtils;
import com.xhs.clothingpatternbackend.constant.UserConstant;
import com.xhs.clothingpatternbackend.exception.BusinessException;
import com.xhs.clothingpatternbackend.exception.ErrorCode;
import com.xhs.clothingpatternbackend.exception.ThrowUtils;
import com.xhs.clothingpatternbackend.model.dto.pattern.*;
import com.xhs.clothingpatternbackend.model.entity.Pattern;
import com.xhs.clothingpatternbackend.model.entity.User;
import com.xhs.clothingpatternbackend.model.vo.PatternVO;
import com.xhs.clothingpatternbackend.service.PatternService;
import com.xhs.clothingpatternbackend.service.UserService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 图案接口
 */
@RestController
@RequestMapping("/pattern")
public class PatternController {

    @Resource
    private PatternService patternService;

    @Resource
    private UserService userService;

    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    private final Cache<String, String> LOCAL_CACHE =
            Caffeine.newBuilder().initialCapacity(1024)
                    .maximumSize(10000L)
                    // 缓存 5 分钟移除
                    .expireAfterWrite(5L, TimeUnit.MINUTES)
                    .build();

    /**
     * 生成图案
     *
     * @param patternGenerateRequest
     * @param request
     * @return
     */
    @PostMapping("/generate")
    public BaseResponse<PatternVO> generatePattern(@RequestBody PatternGenerateRequest patternGenerateRequest,
                                                    HttpServletRequest request) {
        ThrowUtils.throwIf(patternGenerateRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long patternId = patternService.generatePattern(patternGenerateRequest, loginUser);
        
        // 获取生成的图案详情并返回
        Pattern pattern = patternService.getById(patternId);
        PatternVO patternVO = patternService.getPatternVO(pattern);
        
        return ResultUtils.success(patternVO);
    }

    /**
     * 根据 id 获取图案
     *
     * @param id
     * @return
     */
    @GetMapping("/get")
    public BaseResponse<Pattern> getPatternById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Pattern pattern = patternService.getById(id);
        ThrowUtils.throwIf(pattern == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(pattern);
    }

    /**
     * 根据 id 获取图案封装
     *
     * @param id
     * @return
     */
    @GetMapping("/get/vo")
    public BaseResponse<PatternVO> getPatternVOById(long id) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Pattern pattern = patternService.getById(id);
        ThrowUtils.throwIf(pattern == null, ErrorCode.NOT_FOUND_ERROR);
        return ResultUtils.success(patternService.getPatternVO(pattern));
    }

    /**
     * 分页获取图案列表（仅管理员）
     *
     * @param patternQueryRequest
     * @return
     */
    @PostMapping("/list/page")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Page<Pattern>> listPatternByPage(@RequestBody PatternQueryRequest patternQueryRequest) {
        long current = patternQueryRequest.getCurrent();
        long size = patternQueryRequest.getPageSize();
        Page<Pattern> patternPage = patternService.page(new Page<>(current, size),
                patternService.getQueryWrapper(patternQueryRequest));
        return ResultUtils.success(patternPage);
    }

    /**
     * 分页获取图案封装列表
     *
     * @param patternQueryRequest
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PatternVO>> listPatternVOByPage(@RequestBody PatternQueryRequest patternQueryRequest) {
        long current = patternQueryRequest.getCurrent();
        long size = patternQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        //构建缓存
        String queryCondition = JSONUtil.toJsonStr(patternQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String cacheKey = String.format("xhs_pattern:listPictureVOByPage:%s", hashKey);
        //先从本地缓存读取
        String cachedValue = LOCAL_CACHE.getIfPresent(cacheKey);
        if(cachedValue!=null){
            Page<PatternVO> cachePage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachePage);
        }

        //从redis中读取
        ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
        cachedValue = valueOps.get(cacheKey);
        if(cachedValue!=null){
            //存入本地缓存
            LOCAL_CACHE.put(cacheKey, cachedValue);
            Page<PatternVO> cachePage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachePage);
        }



        Page<Pattern> patternPage = patternService.page(new Page<>(current, size),
                patternService.getQueryWrapper(patternQueryRequest));
        Page<PatternVO> patternVOPage = new Page<>(current, size, patternPage.getTotal());
        List<PatternVO> patternVOList = patternService.getPatternVOList(patternPage.getRecords());
        patternVOPage.setRecords(patternVOList);

        //存入缓存中
        String cacheValue = JSONUtil.toJsonStr(patternVOPage);
        int cacheExpireTime=300+ RandomUtil.randomInt(0, 300);
        LOCAL_CACHE.put(cacheKey, cacheValue);
        valueOps.set(cacheKey, cacheValue, cacheExpireTime, TimeUnit.SECONDS);

        return ResultUtils.success(patternVOPage);
    }

    /**
     * 分页获取当前用户创建的图案列表
     *
     * @param patternQueryRequest
     * @param request
     * @return
     */
    @PostMapping("/my/list/page/vo")
    public BaseResponse<Page<PatternVO>> listMyPatternVOByPage(@RequestBody PatternQueryRequest patternQueryRequest,
                                                                HttpServletRequest request) {
        ThrowUtils.throwIf(patternQueryRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        patternQueryRequest.setUserId(loginUser.getId());
        long current = patternQueryRequest.getCurrent();
        long size = patternQueryRequest.getPageSize();
        // 限制爬虫
//        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Pattern> patternPage = patternService.page(new Page<>(current, size),
                patternService.getQueryWrapper(patternQueryRequest));
        Page<PatternVO> patternVOPage = new Page<>(current, size, patternPage.getTotal());
        List<PatternVO> patternVOList = patternService.getPatternVOList(patternPage.getRecords());
        patternVOPage.setRecords(patternVOList);
        return ResultUtils.success(patternVOPage);
    }

    /**
     * 更新图案（仅本人或管理员）
     *
     * @param patternUpdateRequest
     * @param request
     * @return
     */
    @PostMapping("/update")
    public BaseResponse<Boolean> updatePattern(@RequestBody PatternUpdateRequest patternUpdateRequest,
                                                HttpServletRequest request) {
        if (patternUpdateRequest == null || patternUpdateRequest.getId() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long id = patternUpdateRequest.getId();
        // 判断是否存在
        Pattern oldPattern = patternService.getById(id);
        ThrowUtils.throwIf(oldPattern == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可修改
        if (!oldPattern.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        Pattern pattern = new Pattern();
        BeanUtils.copyProperties(patternUpdateRequest, pattern);
        patternService.validPattern(pattern, false);
        boolean result = patternService.updateById(pattern);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 删除图案（仅本人或管理员）
     *
     * @param deleteRequest
     * @param request
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deletePattern(@RequestBody DeleteRequest deleteRequest,
                                                HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        long id = deleteRequest.getId();
        // 判断是否存在
        Pattern oldPattern = patternService.getById(id);
        ThrowUtils.throwIf(oldPattern == null, ErrorCode.NOT_FOUND_ERROR);
        // 仅本人或管理员可删除
        if (!oldPattern.getUserId().equals(loginUser.getId()) && !userService.isAdmin(loginUser)) {
            throw new BusinessException(ErrorCode.NO_AUTH_ERROR);
        }
        boolean result = patternService.removeById(id);
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }

    /**
     * 审核图案（仅管理员）
     *
     * @param patternAuditRequest
     * @param request
     * @return
     */
    @PostMapping("/audit")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> auditPattern(@RequestBody PatternAuditRequest patternAuditRequest,
                                               HttpServletRequest request) {
        ThrowUtils.throwIf(patternAuditRequest == null, ErrorCode.PARAMS_ERROR);
        Long id = patternAuditRequest.getId();
        String auditStatus = patternAuditRequest.getAuditStatus();
        String rejectReason = patternAuditRequest.getRejectReason();
        
        User loginUser = userService.getLoginUser(request);
        boolean result = patternService.auditPattern(id, auditStatus, rejectReason, loginUser.getId());
        ThrowUtils.throwIf(!result, ErrorCode.OPERATION_ERROR);
        return ResultUtils.success(true);
    }


    /**
     * 编辑图片（给用户使用）
     */
    @PostMapping("/edit")
    public BaseResponse<Boolean> editPattern(@RequestBody PatternEditRequest patternEditRequest, HttpServletRequest request) {
        if (patternEditRequest == null || patternEditRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        patternService.editPicture(patternEditRequest, userService.getLoginUser(request));
        return ResultUtils.success(true);
    }
}
