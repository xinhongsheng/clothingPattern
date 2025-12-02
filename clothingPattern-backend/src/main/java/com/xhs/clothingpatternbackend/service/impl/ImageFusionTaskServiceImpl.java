package com.xhs.clothingpatternbackend.service.impl;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qcloud.cos.utils.IOUtils;
import com.xhs.clothingpatternbackend.config.CosClientConfig;
import com.xhs.clothingpatternbackend.exception.BusinessException;
import com.xhs.clothingpatternbackend.exception.ErrorCode;
import com.xhs.clothingpatternbackend.mapper.ImageFusionTaskMapper;
import com.xhs.clothingpatternbackend.model.entity.ImageFusionTask;
import com.xhs.clothingpatternbackend.model.vo.WanQueryVO;
import com.xhs.clothingpatternbackend.sdk.dashscope.WanApiClient;
import com.xhs.clothingpatternbackend.service.ImageFusionTaskService;
import com.xhs.clothingpatternbackend.utils.CosImageUploadUtils;
import com.xhs.clothingpatternbackend.utils.CosUtils;
import io.micrometer.common.util.StringUtils;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
* @author 19099
* @description 针对表【image_fusion_task(多图融合任务表（含结果）)】的数据库操作Service实现
* @createDate 2025-12-02 14:59:51
*/
@Slf4j
@Service
public class ImageFusionTaskServiceImpl extends ServiceImpl<ImageFusionTaskMapper, ImageFusionTask>
    implements ImageFusionTaskService{

    @Resource
    private WanApiClient wanApiClient;
    @Autowired
    private CosUtils cosUtils;
    @Autowired
    private CosClientConfig cosClientConfig;

    // -------------------------- 提交任务（返回 DashScope 任务ID） --------------------------
    @Transactional(rollbackFor = Exception.class)
    @Override
    public String submitTask(Long userId, String prompt, String negativePrompt, List<String> imageUrls, String parameters) {
        try {
            // 1. 参数校验
            if (imageUrls == null || imageUrls.size() < 1 || imageUrls.size() > 3) {
                throw new IllegalArgumentException("图片数量必须为1-3张");
            }
            if (prompt == null || prompt.trim().isEmpty()) {
                throw new IllegalArgumentException("正向提示词不能为空");
            }

            // 2. 调用通义万相API
            JSONArray imageUrlJson = JSONArray.parseArray(JSONObject.toJSONString(imageUrls));
            JSONObject paramJson = (parameters != null && !parameters.isEmpty())
                    ? JSONObject.parseObject(parameters)
                    : new JSONObject();
            String dashscopeTaskId = wanApiClient.submitFusionTask(prompt, negativePrompt, imageUrlJson, paramJson);

            // 3. 保存主任务（结果字段默认空字符串）
            ImageFusionTask task = new ImageFusionTask();
            task.setUserId(userId);
            task.setDashscopeTaskId(dashscopeTaskId);
            task.setPrompt(prompt);
            task.setNegativePrompt(negativePrompt);
            task.setImageUrls(String.join(",", imageUrls));
            task.setParameters(parameters);
            task.setTaskStatus("PENDING");
            task.setCreateTime(LocalDateTime.now());
            task.setUpdateTime(LocalDateTime.now());
            this.save(task);

            log.info("任务提交成功：本地ID={}，通义ID={}", task.getId(), dashscopeTaskId);
            // 对外返回 DashScope 任务ID，前端后续以此为 taskId 查询
            return dashscopeTaskId;
        } catch (Exception e) {
            log.error("任务提交失败", e);
            throw new RuntimeException("任务提交失败：" + e.getMessage());
        }
    }

    // -------------------------- 查询任务状态（通过 DashScope 任务ID 查询并合并结果） --------------------------
    @Transactional(rollbackFor = Exception.class)
    @Override
    public ImageFusionTask queryTaskStatus(String dashscopeTaskId) {
        // 1. 通过 DashScope 任务ID 查询本地任务
        ImageFusionTask task = this.lambdaQuery()
                .eq(ImageFusionTask::getDashscopeTaskId, dashscopeTaskId)
                .one();
        if (task == null) {
            // 任务不存在时，返回一个带失败状态的占位对象，避免直接抛异常
            ImageFusionTask notFoundTask = new ImageFusionTask();
            notFoundTask.setDashscopeTaskId(dashscopeTaskId);
            notFoundTask.setTaskStatus("FAILED");
            notFoundTask.setErrorMessage("任务不存在或已删除：" + dashscopeTaskId);
            return notFoundTask;
        }

        // 2. 已完成任务直接返回
        if ("SUCCEEDED".equals(task.getTaskStatus()) || "FAILED".equals(task.getTaskStatus())) {
            return task;
        }

        try {
            // 3. 调用通义API查询结果
            WanQueryVO queryResult = wanApiClient.queryTask(task.getDashscopeTaskId());
            WanQueryVO.Output output = queryResult.getOutput();

            // 4. 更新任务基础信息
            task.setTaskStatus(output.getTaskStatus());
            task.setSubmitTime(output.getSubmitTime());
            task.setScheduledTime(output.getScheduledTime());
            task.setEndTime(output.getEndTime());
            task.setUpdateTime(LocalDateTime.now());

            // 5. 处理结果：成功则保存多图信息，失败则记录错误
            if ("SUCCEEDED".equals(output.getTaskStatus())) {
                // 解析通义返回的多图结果，保存到主表字段
                saveFusionResults(task, output.getResults());
            } else if ("FAILED".equals(output.getTaskStatus())) {
                task.setErrorCode(output.getCode());
                task.setErrorMessage(output.getMessage());
            }

            // 6. 更新数据库
            this.updateById(task);
            log.info("任务状态更新：DashScopeID={}，状态={}", dashscopeTaskId, task.getTaskStatus());
            return task;
        } catch (Exception e) {
            log.error("查询任务失败：DashScopeID={}", dashscopeTaskId, e);
            throw new RuntimeException("查询任务失败：" + e.getMessage());
        }
    }

    // -------------------------- 保存多图结果（核心修改：单表存储） --------------------------
    private void saveFusionResults(ImageFusionTask task, List<WanQueryVO.Result> results) {
        if (results == null || results.isEmpty()) {
            log.warn("任务无结果图片：ID={}", task.getId());
            return;
        }

        // 过滤有效结果（code为null或0表示成功）
        List<WanQueryVO.Result> validResults = results.stream()
                .filter(r -> r.getCode() == null || "0".equals(r.getCode()))
                .collect(Collectors.toList());

        // 构建多值列表
        List<String> origPrompts = new ArrayList<>();
        List<String> tempUrls = new ArrayList<>();
        List<String> localUrls = new ArrayList<>();
        List<Integer> sorts = new ArrayList<>();

        for (int i = 0; i < validResults.size(); i++) {
            WanQueryVO.Result result = validResults.get(i);
            origPrompts.add(result.getOrigPrompt());
            tempUrls.add(result.getUrl());
            sorts.add(i + 1); // 排序从1开始

            // 下载临时图片到COS，获取永久URL
            try {
                String localUrl = downloadAndSaveToCOS(result.getUrl());
                localUrls.add(localUrl);
            } catch (Exception e) {
                log.error("下载图片失败：临时URL={}", result.getUrl(), e);
                localUrls.add(""); // 下载失败存空字符串，前端可识别
            }
        }

        // 拼接多值为字符串，设置到任务实体
        task.setOrigPromptList(origPrompts);
        task.setTempImageUrlList(tempUrls);
        task.setLocalImageUrlList(localUrls);
        task.setSortList(sorts);

        log.info("保存结果成功：任务ID={}，图片数量={}", task.getId(), validResults.size());
    }

    /**
     * 下载网络图片到COS，返回永久有效的URL
     * 参考 TryOnTaskServiceImpl.downloadAndSaveToCOS，实现一致的 COS 上传流程
     */
    private String downloadAndSaveToCOS(String imageUrl) throws Exception {
        if (StringUtils.isEmpty(imageUrl)) {
            log.warn("下载图片失败：图片URL为空");
            return null;
        }

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(imageUrl).get().build();

        try (Response response = client.newCall(request).execute()) {
            // 1. 验证下载响应状态
            if (!response.isSuccessful()) {
                String errorMsg = String.format("下载图片失败：URL=%s，响应状态=%d，信息=%s",
                        imageUrl, response.code(), response.message());
                log.error(errorMsg);
                throw new IOException(errorMsg);
            }

            // 2. 获取响应流和文件类型（用于后缀匹配）
            try (InputStream inputStream = response.body().byteStream()) {
                String contentType = response.header("Content-Type");
                // 3. 验证文件类型（与uploadImageToCos保持一致：仅支持JPG/PNG）
                if (contentType == null || (!"image/jpeg".equals(contentType) && !"image/png".equals(contentType))) {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "只支持JPG和PNG格式的图片");
                }

                // 4. 确定文件后缀
                String suffix = "image/jpeg".equals(contentType) ? ".jpg" : ".png";

                // 5. 生成业务相关标识（任务ID+时间戳）
                String randomId = UUID.randomUUID().toString().replace("-", "");
                long timestamp = System.currentTimeMillis();

                // 6. 将InputStream转换为MultipartFile（适配uploadImageToCos的参数要求）
                MultipartFile multipartFile = new InputStreamMultipartFile(
                        "fusion-result-image",
                        randomId + "_" + timestamp + suffix,
                        contentType,
                        inputStream
                );

                // 7. 组装 uploadImageToCos 所需参数
                Long userId = 0L; // 多图融合结果暂不关联具体用户
                String tempFilePrefix = "fusion_result_"; // 临时文件前缀
                String cosKeyPrefix = "fusion-result/";   // COS 路径前缀
                boolean needLog = true;

                // 8. 上传到 COS，返回永久 URL
                return CosImageUploadUtils.uploadImageToCos(
                        multipartFile,
                        userId,
                        cosUtils,
                        cosClientConfig,
                        tempFilePrefix,
                        cosKeyPrefix,
                        needLog
                );
            }
        } catch (BusinessException e) {
            // 业务异常直接抛出（由上层统一处理）
            throw e;
        } catch (Exception e) {
            log.error("下载并上传图片到COS失败：URL={}", imageUrl, e);
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "下载并上传图片失败");
        }
    }

    /**
     * 自定义MultipartFile实现：将InputStream转换为MultipartFile（无本地文件残留）
     * 复用 TryOnTaskServiceImpl 中的思路，避免依赖 spring-test 的 MockMultipartFile
     */
    private static class InputStreamMultipartFile implements MultipartFile {
        private final String name;
        private final String originalFilename;
        private final String contentType;
        private final byte[] content;

        public InputStreamMultipartFile(String name, String originalFilename, String contentType, InputStream inputStream) throws IOException {
            this.name = name;
            this.originalFilename = originalFilename;
            this.contentType = contentType;
            this.content = IOUtils.toByteArray(inputStream);
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public String getOriginalFilename() {
            return originalFilename;
        }

        @Override
        public String getContentType() {
            return contentType;
        }

        @Override
        public boolean isEmpty() {
            return content.length == 0;
        }

        @Override
        public long getSize() {
            return content.length;
        }

        @Override
        public byte[] getBytes() {
            return content.clone();
        }

        @Override
        public InputStream getInputStream() {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            try (OutputStream outputStream = new FileOutputStream(dest)) {
                outputStream.write(content);
            }
        }
    }

    // -------------------------- 查询结果图片（通过 DashScope 任务ID 直接从主表获取） --------------------------
    @Override
    public List<String> getTaskResults(String dashscopeTaskId) {
        ImageFusionTask task = this.lambdaQuery()
                .eq(ImageFusionTask::getDashscopeTaskId, dashscopeTaskId)
                .one();
        if (task == null) {
            // 任务不存在时，直接返回空列表，前端可据此判断无结果
            log.warn("查询结果失败：任务ID不存在，DashScopeID={}", dashscopeTaskId);
            return List.of();
        }
        // 返回永久URL列表（前端直接展示）
        return task.getLocalImageUrlList();
    }

}




