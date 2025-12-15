package com.xhs.clothingpatternbackend.controller;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.xhs.clothingpatternbackend.common.BaseResponse;
import com.xhs.clothingpatternbackend.common.ResultUtils;
import com.xhs.clothingpatternbackend.config.CosClientConfig;
import com.xhs.clothingpatternbackend.exception.BusinessException;
import com.xhs.clothingpatternbackend.exception.ErrorCode;
import com.xhs.clothingpatternbackend.exception.ThrowUtils;
import com.xhs.clothingpatternbackend.model.dto.mj.WanQueryRequest;
import com.xhs.clothingpatternbackend.model.dto.pattern.PatternQueryRequest;
import com.xhs.clothingpatternbackend.model.entity.ImageFusionTask;
import com.xhs.clothingpatternbackend.model.entity.Pattern;
import com.xhs.clothingpatternbackend.model.entity.User;
import com.xhs.clothingpatternbackend.model.vo.PatternVO;
import com.xhs.clothingpatternbackend.model.vo.WanQueryVO;
import com.xhs.clothingpatternbackend.service.ImageFusionTaskService;
import com.xhs.clothingpatternbackend.service.UserService;
import com.xhs.clothingpatternbackend.utils.CosImageUploadUtils;
import com.xhs.clothingpatternbackend.utils.CosUtils;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Response;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-12-02
 * @Description:
 * @Version: 1.0
 */
@Slf4j
@RestController
@RequestMapping("/image-fusion")
public class ImageFusionController {
    @Resource
    private ImageFusionTaskService fusionTaskService;
    @Resource
    private CosUtils cosUtils;
    @Resource
    private CosClientConfig cosClientConfig;
    @Resource
    private UserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Resource
    private  ImageFusionTaskService imageFusionTaskService;
    // 改为公共静态变量，方便其他服务清空缓存
    public static final Cache<String, String> LOCAL_CACHE =
            Caffeine.newBuilder().initialCapacity(1024)
                    .maximumSize(10000L)
                    // 缓存 5 分钟移除
                    .expireAfterWrite(5L, TimeUnit.MINUTES)
                    .build();

    /**
     * 多图融合固定提示词：将服装图案自然、美观地融合到服装款式图上
     */
    private static final String FUSION_PROMPT = "【重要任务说明】\n" +
            "输入图片顺序：第1张是服装款式图（如上衣、裤子、连衣裙等），第2张是图案素材。\n" +
            "你的任务是：将第2张图的图案应用到第1张图的服装上，生成融合后的服装效果图。\n\n" +
            "【绝对禁止】\n" +
            "- 绝对禁止生成任何额外的服装单品！如果输入的是上衣，绝对不能生成裤子；如果输入的是裤子，绝对不能生成上衣。\n" +
            "- 只输出输入服装本身，不生成任何搭配单品（裤子、裙子、外套、鞋履、配饰等）。\n" +
            "- 不能添加原图中没有的元素。\n\n" +
            "【融合要求】\n" +
            "1. 将图案自然、完整地应用到输入服装的表面上。\n" +
            "2. 保持服装原有的版型、轮廓和结构细节（领口、袖子、腰线、褶皱、口袋等）不变。\n" +
            "3. 图案需要均匀铺展到服装可见区域，跟随服装结构自然形变。\n" +
            "4. 保证图案与服装底色协调，视觉效果干净、高级。\n" +
            "5. 背景保持简洁，不添加文字或额外装饰。";
    // -------------------------- 上传图片（无修改） --------------------------
    @PostMapping("/upload")
    public BaseResponse<String> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            if (file.getSize() > 5 * 1024 * 1024) {
                throw new BusinessException(400, "图片大小不能超过5MB");
            }
            String url = CosImageUploadUtils.uploadImageToCos(
                    file,
                    0L, // 无用户关联时用0L占位（可根据业务传入真实userId）
                    cosUtils,
                    cosClientConfig,
                    "try_on_input_", // 输入图片前缀（区分输入/输出）
                    "try-on-input/", // COS存储路径前缀
                    true // 开启日志
            );;
            return ResultUtils.success(url);
        } catch (Exception e) {
            log.error("图片上传失败", e);
            throw new BusinessException(500, "上传失败：" + e.getMessage());
        }
    }

    // -------------------------- 提交任务（返回 DashScope 任务ID） --------------------------
    @PostMapping("/submit")
    public BaseResponse<String> submitTask(
            @RequestParam Long userId,
            @RequestParam String imageUrls, // 逗号分隔的输入URL
            @RequestParam(required = false) String parameters) {
        try {
            List<String> imageUrlList = List.of(imageUrls.split(","));
            // 使用后端固定的融合提示词，前端无需再传 prompt
            String dashscopeTaskId = fusionTaskService.submitTask(userId, FUSION_PROMPT, null, imageUrlList, parameters);
            return ResultUtils.success(dashscopeTaskId);
        } catch (Exception e) {
            log.error("任务提交失败", e);

            throw new BusinessException(500, "任务提交失败：" + e.getMessage());
        }
    }

    // -------------------------- 查询任务状态（通过 DashScope 任务ID，返回完整实体，含结果） --------------------------
    @GetMapping("/status/{taskId}")
    public BaseResponse<ImageFusionTask> queryStatus(@PathVariable("taskId") String dashscopeTaskId) {
        try {
            ImageFusionTask task = fusionTaskService.queryTaskStatus(dashscopeTaskId);
            return ResultUtils.success(task);
        } catch (Exception e) {
            log.error("查询失败", e);
            throw new BusinessException(500, "查询失败：" + e.getMessage());
        }
    }

    // -------------------------- 单独查询结果图片（通过 DashScope 任务ID，可选） --------------------------
    @GetMapping("/results/{taskId}")
    public BaseResponse<List<String>> getResults(@PathVariable("taskId") String dashscopeTaskId) {
        try {
            List<String> localUrls = fusionTaskService.getTaskResults(dashscopeTaskId);
            return ResultUtils.success(localUrls);
        } catch (Exception e) {
            log.error("查询结果失败", e);
            throw new BusinessException(500, "查询结果失败：" + e.getMessage());
        }
    }

    // -------------------------- 保存选中的图片 --------------------------
    @PostMapping("/save-selected")
    public BaseResponse<Boolean> saveSelectedImage(
            @RequestParam String taskId,
            @RequestParam String imageUrl) {
        try {
            boolean result = fusionTaskService.saveSelectedImage(taskId, imageUrl);
            return ResultUtils.success(result);
        } catch (Exception e) {
            log.error("保存选中图片失败", e);
            throw new BusinessException(500, "保存失败：" + e.getMessage());
        }
    }

    /**
     * 分页获取融合图片封装列表
     *
     * @param wanQueryRequest
     * @return
     */
    @PostMapping("/list/page/vo")
    public BaseResponse<Page<WanQueryVO>> listImageFusionVOByPage(@RequestBody WanQueryRequest wanQueryRequest,
                                                                  HttpServletRequest request) {
        long current = wanQueryRequest.getCurrent();
        long size = wanQueryRequest.getPageSize();
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

        //构建缓存
        String queryCondition = JSONUtil.toJsonStr(wanQueryRequest);
        String hashKey = DigestUtils.md5DigestAsHex(queryCondition.getBytes());
        String cacheKey = String.format("xhs_pattern:listWanVOByPage:%s", hashKey);
        //先从本地缓存读取
        String cachedValue = LOCAL_CACHE.getIfPresent(cacheKey);
        if(cachedValue!=null){
            Page<WanQueryVO> cachePage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachePage);
        }

        //从redis中读取
        ValueOperations<String, String> valueOps = stringRedisTemplate.opsForValue();
        cachedValue = valueOps.get(cacheKey);
        if(cachedValue!=null){
            //存入本地缓存
            LOCAL_CACHE.put(cacheKey, cachedValue);
            Page<WanQueryVO> cachePage = JSONUtil.toBean(cachedValue, Page.class);
            return ResultUtils.success(cachePage);
        }

        Page<ImageFusionTask> imageFusionTaskPage = imageFusionTaskService.page(new Page<>(current, size),
                imageFusionTaskService.getQueryWrapper(wanQueryRequest));
        Page<WanQueryVO> wanQueryVOPage = new Page<>(current, size, imageFusionTaskPage.getTotal());
        List<WanQueryVO> wanQueryVOList = imageFusionTaskService.getImageFusionVOList(imageFusionTaskPage.getRecords(), loginUserId);
        wanQueryVOPage.setRecords(wanQueryVOList);

        //存入缓存中
        String cacheValue = JSONUtil.toJsonStr(wanQueryVOPage);
        int cacheExpireTime=300+ RandomUtil.randomInt(0, 300);
        LOCAL_CACHE.put(cacheKey, cacheValue);
        valueOps.set(cacheKey, cacheValue, cacheExpireTime, TimeUnit.SECONDS);

        return ResultUtils.success(wanQueryVOPage);
    }
}
