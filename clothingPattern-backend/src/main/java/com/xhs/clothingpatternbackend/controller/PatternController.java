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
import com.xhs.clothingpatternbackend.service.PatternSimilarityService;
import com.xhs.clothingpatternbackend.service.PatternVectorService;
import com.xhs.clothingpatternbackend.service.UserBehaviorService;
import com.xhs.clothingpatternbackend.service.UserService;
import com.xhs.clothingpatternbackend.task.CollaborativeFilteringTask;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

    @Resource
    private StringRedisTemplate stringRedisTemplate;
    @Resource
    private PatternVectorService patternVectorService;

    @Resource
    private PatternSimilarityService patternSimilarityService;

    @Resource
    private UserBehaviorService userBehaviorService;

    @Resource
    private CollaborativeFilteringTask collaborativeFilteringTask;

    // 改为公共静态变量，方便其他服务清空缓存
    public static final Cache<String, String> LOCAL_CACHE =
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
    public BaseResponse<Pattern> generatePattern(@RequestBody PatternGenerateRequest patternGenerateRequest,
                                                    HttpServletRequest request) {
        ThrowUtils.throwIf(patternGenerateRequest == null, ErrorCode.PARAMS_ERROR);
        User loginUser = userService.getLoginUser(request);
        Long patternId = patternService.generatePattern(patternGenerateRequest, loginUser);
        
        // 获取生成的图案详情并返回
        Pattern pattern = patternService.getById(patternId);
//        PatternVO patternVO = patternService.getPatternVO(pattern, loginUser.getId());
        // 清空图案列表缓存
        clearPatternListCache();

        return ResultUtils.success(pattern);
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
    public BaseResponse<PatternVO> getPatternVOById(long id, HttpServletRequest request) {
        ThrowUtils.throwIf(id <= 0, ErrorCode.PARAMS_ERROR);
        Pattern pattern = patternService.getById(id);
        ThrowUtils.throwIf(pattern == null, ErrorCode.NOT_FOUND_ERROR);
        
        // 获取当前登录用户ID（未登录用户为null）
        Long loginUserId = null;
        try {
            User loginUser = userService.getLoginUser(request);
            if (loginUser != null) {
                loginUserId = loginUser.getId();
            }
        } catch (Exception e) {
            // 未登录，保持loginUserId为null
        }
        
        return ResultUtils.success(patternService.getPatternVO(pattern, loginUserId));
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
     * 登录用户在第一页且无筛选条件时，推荐图案优先显示
     *
     * @param patternQueryRequest
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<PatternVO>> listPatternVOByPage(@RequestBody PatternQueryRequest patternQueryRequest,
                                                              HttpServletRequest request) {
        long current = patternQueryRequest.getCurrent();
        long size = patternQueryRequest.getPageSize();
        // 限制爬虫
        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        
        // 获取当前登录用户ID（未登录用户为null）
        Long loginUserId = null;
        try {
            User loginUser = userService.getLoginUser(request);
            if (loginUser != null) {
                loginUserId = loginUser.getId();
            }
        } catch (Exception e) {
            // 未登录，保持loginUserId为null
        }

        // 判断是否需要插入推荐图案：第一页 + 已登录 + 无筛选条件
        boolean shouldInsertRecommend = current == 1 && loginUserId != null && !hasFilterConditions(patternQueryRequest);
        List<PatternVO> recommendList = new ArrayList<>();
        
        if (shouldInsertRecommend) {
            // 获取推荐图案（不使用缓存，因为推荐是个性化的）
            recommendList = patternSimilarityService.getRecommendations(loginUserId, 10);
        }
        
        // 构建缓存key（不包含用户ID，因为普通列表是公共的）
        String queryCondition = JSONUtil.toJsonStr(patternQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String cacheKey = String.format("xhs_pattern:listPictureVOByPage:%s", hashKey);
        
        Page<PatternVO> patternVOPage = null;
        
        // 先从本地缓存读取
        String cachedValue = LOCAL_CACHE.getIfPresent(cacheKey);
        if (cachedValue != null) {
            patternVOPage = parseCachedPage(cachedValue);
        } else {
            // 从Redis中读取
            ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
            cachedValue = valueOps.get(cacheKey);
            if (cachedValue != null) {
                LOCAL_CACHE.put(cacheKey, cachedValue);
                patternVOPage = parseCachedPage(cachedValue);
            } else {
                // 缓存未命中，查询数据库
                Page<Pattern> patternPage = patternService.page(new Page<>(current, size),
                        patternService.getQueryWrapper(patternQueryRequest));
                patternVOPage = new Page<>(current, size, patternPage.getTotal());
                List<PatternVO> patternVOList = patternService.getPatternVOList(patternPage.getRecords(), loginUserId);
                patternVOPage.setRecords(patternVOList);

                // 存入缓存
                String cacheValue = JSONUtil.toJsonStr(patternVOPage);
                int cacheExpireTime = 300 + RandomUtil.randomInt(0, 300);
                LOCAL_CACHE.put(cacheKey, cacheValue);
                valueOps.set(cacheKey, cacheValue, cacheExpireTime, TimeUnit.SECONDS);
            }
        }

        // 如果有推荐图案，将推荐图案插入到列表前面
        if (!recommendList.isEmpty() && patternVOPage != null) {
            List<PatternVO> originalList = patternVOPage.getRecords();
            if (originalList == null) {
                originalList = new ArrayList<>();
            }
            
            // 获取推荐图案ID集合，用于去重
            Set<Long> recommendIds = recommendList.stream()
                    .map(PatternVO::getId)
                    .collect(java.util.stream.Collectors.toSet());
            
            // 从普通列表中过滤掉已在推荐中的图案
            List<PatternVO> filteredList = originalList.stream()
                    .filter(item -> !recommendIds.contains(item.getId()))
                    .collect(java.util.stream.Collectors.toList());
            
            // 合并：推荐图案 + 普通图案
            List<PatternVO> mergedList = new java.util.ArrayList<>();
            mergedList.addAll(recommendList);
            mergedList.addAll(filteredList);
            
            patternVOPage.setRecords(mergedList);
        }

        return ResultUtils.success(patternVOPage);
    }

    /**
     * 判断是否有筛选条件
     */
    private boolean hasFilterConditions(PatternQueryRequest request) {
        if (request == null) {
            return false;
        }
        // 检查是否有搜索或筛选条件
        return (request.getPatternName() != null && !request.getPatternName().isEmpty()) ||
               (request.getStyle() != null && !request.getStyle().isEmpty()) ||
               (request.getSeason() != null && !request.getSeason().isEmpty()) ||
               (request.getTargetAudience() != null && !request.getTargetAudience().isEmpty()) ||
               (request.getGenerationType() != null && !request.getGenerationType().isEmpty()) ||
               request.getUserId() != null ||
               request.getId() != null;
    }

    /**
     * 解析缓存中的分页数据，正确处理泛型
     */
    private Page<PatternVO> parseCachedPage(String cachedValue) {
        cn.hutool.json.JSONObject jsonObject = JSONUtil.parseObj(cachedValue);
        Page<PatternVO> page = new Page<>();
        page.setCurrent(jsonObject.getLong("current", 1L));
        page.setSize(jsonObject.getLong("size", 10L));
        page.setTotal(jsonObject.getLong("total", 0L));
        page.setPages(jsonObject.getLong("pages", 0L));
        
        // 正确解析 records 为 PatternVO 列表
        cn.hutool.json.JSONArray recordsArray = jsonObject.getJSONArray("records");
        if (recordsArray != null) {
            List<PatternVO> records = JSONUtil.toList(recordsArray, PatternVO.class);
            page.setRecords(records);
        } else {
            page.setRecords(new ArrayList<>());
        }
        
        return page;
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
        //取消注释 为了统计所有图案
//        ThrowUtils.throwIf(size > 20, ErrorCode.PARAMS_ERROR);
        Page<Pattern> patternPage = patternService.page(new Page<>(current, size),
                patternService.getQueryWrapper(patternQueryRequest));
        Page<PatternVO> patternVOPage = new Page<>(current, size, patternPage.getTotal());
        List<PatternVO> patternVOList = patternService.getPatternVOList(patternPage.getRecords(), loginUser.getId());
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
        // 清空图案列表缓存
        clearPatternListCache();
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
        // 清空图案列表缓存
        clearPatternListCache();
        // 删除向量
        patternVectorService.removeById(id);
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
        // 清空图案列表缓存
        clearPatternListCache();
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
        // 清空图案列表缓存
        clearPatternListCache();
        return ResultUtils.success(true);
    }

    private void clearPatternListCache() {
        try {
            // 清空本地 Caffeine 缓存
            LOCAL_CACHE.invalidateAll();

            // 清空 Redis 中的图案列表缓存
            java.util.Set<String> keys = stringRedisTemplate.keys("xhs_pattern:listPictureVOByPage:*");
            if (keys != null && !keys.isEmpty()) {
                stringRedisTemplate.delete(keys);
            }
        } catch (Exception e) {
            // 缓存清理失败不影响主流程
        }
    }

    // ==================== 个性化推荐相关接口 ====================

    /**
     * 获取个性化推荐图案列表
     * 如果用户已登录且有行为数据，返回基于协同过滤的推荐结果
     * 否则返回热门图案（冷启动降级策略）
     *
     * @param request HTTP请求
     * @return 推荐的图案列表
     */
    @GetMapping("/recommend")
    public BaseResponse<List<PatternVO>> getRecommendations(HttpServletRequest request) {
        // 获取当前登录用户ID（未登录用户为null）
        Long loginUserId = null;
        try {
            User loginUser = userService.getLoginUser(request);
            if (loginUser != null) {
                loginUserId = loginUser.getId();
            }
        } catch (Exception e) {
            // 未登录，保持loginUserId为null
        }

        // 获取推荐列表（默认10个）
        List<PatternVO> recommendations = patternSimilarityService.getRecommendations(loginUserId, 10);
        return ResultUtils.success(recommendations);
    }

    /**
     * 记录用户浏览行为
     * 当用户进入图案详情页时调用
     *
     * @param patternId 图案ID
     * @param request HTTP请求
     * @return 是否记录成功
     */
    @PostMapping("/behavior/view")
    public BaseResponse<Boolean> recordViewBehavior(@RequestParam Long patternId, HttpServletRequest request) {
        ThrowUtils.throwIf(patternId == null || patternId <= 0, ErrorCode.PARAMS_ERROR);

        // 获取当前登录用户
        Long loginUserId = null;
        try {
            User loginUser = userService.getLoginUser(request);
            if (loginUser != null) {
                loginUserId = loginUser.getId();
            }
        } catch (Exception e) {
            // 未登录用户不记录行为
            return ResultUtils.success(false);
        }

        if (loginUserId == null) {
            return ResultUtils.success(false);
        }

        boolean result = userBehaviorService.recordBehavior(loginUserId, patternId, "VIEW");
        return ResultUtils.success(result);
    }

    /**
     * 手动触发推荐算法（仅管理员，用于测试）
     */
    @PostMapping("/recommend/refresh")
    @AuthCheck(mustRole = UserConstant.ADMIN_ROLE)
    public BaseResponse<Boolean> refreshRecommendations() {
        collaborativeFilteringTask.runManually();
        return ResultUtils.success(true);
    }
}
