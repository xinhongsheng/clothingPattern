package com.xhs.clothingpatternbackend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qcloud.cos.utils.IOUtils;
import com.xhs.clothingpatternbackend.config.CosClientConfig;
import com.xhs.clothingpatternbackend.exception.BusinessException;
import com.xhs.clothingpatternbackend.exception.ErrorCode;
import com.xhs.clothingpatternbackend.mapper.TryOnTaskMapper;
import com.xhs.clothingpatternbackend.model.entity.TryOnTask;
import com.xhs.clothingpatternbackend.model.vo.QueryTaskHistoryResultVO;
import com.xhs.clothingpatternbackend.model.vo.QueryTaskResultVO;
import com.xhs.clothingpatternbackend.sdk.dashscope.DashScopeApiAiTryOnClient;
import com.xhs.clothingpatternbackend.service.TryOnTaskService;
import com.xhs.clothingpatternbackend.utils.CosImageUploadUtils;
import com.xhs.clothingpatternbackend.utils.CosUtils;
import io.micrometer.common.util.StringUtils;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
* @author 19099
* @description 针对表【try_on_task】的数据库操作Service实现
* @createDate 2025-12-01 17:22:45
*/
@Slf4j
@Service
public class TryOnTaskServiceImpl extends ServiceImpl<TryOnTaskMapper, TryOnTask>
    implements TryOnTaskService {

    @Autowired
    private DashScopeApiAiTryOnClient dashScopeApiClient;
    @Autowired
    private CosUtils cosUtils;
    @Autowired
    private CosClientConfig cosClientConfig;

    // 提交试衣任务并保存到数据库
    @Override
    public String submitTask(Long userId, String personUrl, String topUrl, String bottomUrl) throws IOException {
        String taskId = dashScopeApiClient.submitTryOn(personUrl, topUrl, bottomUrl);
        TryOnTask task = new TryOnTask();
        task.setUserId(userId);
        task.setPersonImageUrl(personUrl);
        task.setTopGarmentUrl(topUrl);
        task.setBottomGarmentUrl(bottomUrl);
        task.setDashscopeTaskId(taskId);
        task.setTaskStatus("PENDING");
        task.setCreateTime(new Date());
        this.save(task);
        return taskId;
    }

    // 查询并更新任务状态，成功时自动保存图片到COS
    @Override
    public TryOnTask queryTaskStatus(String taskId) throws Exception {
        // 1. 调用阿里云API查询任务详情
        QueryTaskResultVO queryTaskResult = dashScopeApiClient.queryTask(taskId);

        // 2. 查询本地任务记录
        TryOnTask task = this.lambdaQuery()
                .eq(TryOnTask::getDashscopeTaskId, taskId)
                .one();
        if (task == null) {
            throw new IllegalArgumentException("任务ID不存在：" + taskId);
        }

        // 3. 更新任务状态和时间信息
        task.setTaskStatus(queryTaskResult.getTaskStatus());
        task.setSubmitTime(queryTaskResult.getSubmitTime());
        task.setScheduledTime(queryTaskResult.getScheduledTime());
        task.setEndTime(queryTaskResult.getEndTime());

        // 4. 处理任务结果：成功则保存图片到本地OSS，失败则记录错误信息
        if ("SUCCEEDED".equals(queryTaskResult.getTaskStatus())) {
            // 下载临时图片并保存到COS
            String localUrl = downloadAndSaveToCOS(queryTaskResult.getImageUrl());
            task.setResultImageUrl(queryTaskResult.getImageUrl()); // 临时URL
            task.setLocalResultUrl(localUrl); // 永久URL
        } else if ("FAILED".equals(queryTaskResult.getTaskStatus())) {
            // 记录错误信息
            task.setErrorCode(queryTaskResult.getErrorCode());
            task.setErrorMessage(queryTaskResult.getErrorMessage());
        }

        // 5. 保存更新
        this.updateById(task);
        return task;
    }
    /**
     * 下载网络图片到COS，返回永久有效的URL
     */
    public String downloadAndSaveToCOS(String imageUrl) throws Exception {
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

                // 4. 确定文件后缀（避免固定.jpg导致格式不匹配）
                String suffix = "image/jpeg".equals(contentType) ? ".jpg" : ".png";

                // 5. 生成业务相关标识（任务ID+时间戳，符合需求：try-on-result/任务ID_时间戳.后缀）
                String taskId = UUID.randomUUID().toString().replace("-", ""); // 任务ID（去横杠更简洁）
                long timestamp = System.currentTimeMillis();

                // 6. 将InputStream转换为MultipartFile（适配uploadImageToCos的参数要求）
                // 自定义MultipartFile实现，避免依赖Spring的MockMultipartFile（生产环境更通用）
                MultipartFile multipartFile = new InputStreamMultipartFile(
                        "downloaded-image", // 表单字段名（无实际意义，必填）
                        taskId + "_" + timestamp + suffix, // 原始文件名（符合需求格式）
                        contentType,
                        inputStream
                );

                // 7. 补充uploadImageToCos的完整参数
                Long userId =0L ; // 无实际用户关联时，用0L占位（可根据业务调整为有效ID）
                String tempFilePrefix = "try_on_result_"; // 临时文件前缀（用于业务类型标识）
                String cosKeyPrefix = "try-on-result/"; // COS存储路径前缀（符合需求）
                boolean needLog = true; // 开启日志（方便排查问题）

                // 8. 调用工具类上传到COS，返回永久URL
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
     * 删除指定用户下的所有任务记录
     */
    public Boolean removeByUserId(Long id, Long userId) {
        QueryWrapper<TryOnTask> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", userId);
        queryWrapper.eq("id", id);
        return this.remove(queryWrapper);
    }


    /**
     * 自定义MultipartFile实现：将InputStream转换为MultipartFile（无本地文件残留）
     * 避免依赖spring-test的MockMultipartFile，生产环境更稳定
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
            this.content = IOUtils.toByteArray(inputStream); // 读取流到字节数组（Apache Commons IO）
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
        public byte[] getBytes() throws IOException {
            return content.clone(); // 克隆避免字节数组被篡改
        }

        @Override
        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(content);
        }

        @Override
        public void transferTo(File dest) throws IOException, IllegalStateException {
            // 实现文件写入（uploadImageToCos会用到此方法）
            try (OutputStream outputStream = new FileOutputStream(dest)) {
                outputStream.write(content);
            }
        }
    }


    @Override
    public List<QueryTaskHistoryResultVO> getTryOnHistory(Long userId) {
        List<TryOnTask> tryOnTasks = this.list(new QueryWrapper<TryOnTask>().eq("userId", userId));

        List<QueryTaskHistoryResultVO> queryTaskHistoryResultVOList = tryOnTasks.stream().map(tryOnTask -> {
            QueryTaskHistoryResultVO queryTaskHistoryResultVO = new QueryTaskHistoryResultVO();
            queryTaskHistoryResultVO.setId(tryOnTask.getId());  // 设置任务ID
            queryTaskHistoryResultVO.setLocalImageUrl(tryOnTask.getLocalResultUrl());
            queryTaskHistoryResultVO.setSubmitTime(tryOnTask.getSubmitTime());
            queryTaskHistoryResultVO.setEndTime(tryOnTask.getEndTime());
            return  queryTaskHistoryResultVO;
        }).toList();
        return  queryTaskHistoryResultVOList;
    }

}




