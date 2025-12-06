package com.xhs.clothingpatternbackend.sdk.dashscope;


import com.alibaba.fastjson2.JSONObject;
import com.xhs.clothingpatternbackend.model.vo.QueryTaskResultVO;
import io.micrometer.common.util.StringUtils;
import okhttp3.*;
import com.xhs.clothingpatternbackend.config.AiTryOnConfig;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-12-01
 * @Description: Ai试衣接口
 * @Version: 1.0
 */
@Component
public class DashScopeApiAiTryOnClient {
    @Resource
    private AiTryOnConfig aiTryOnConfig;
    private final OkHttpClient client = new OkHttpClient();
    /**
     * 提交任务
     */
    public String submitTryOn(String personUrl, String topUrl,String bottomUrl) throws IOException {
        JSONObject body = new JSONObject();
        body.put("model", "aitryon-plus");
        JSONObject input = new JSONObject();
        input.put("person_image_url", personUrl);
        if (StringUtils.isNotBlank(topUrl)) {
            input.put("top_garment_url", topUrl);
        }
        if (StringUtils.isNotBlank(bottomUrl)) {
            input.put("bottom_garment_url", bottomUrl);
        }
        body.put("input", input);

        // parameters参数不变
        body.put("parameters", new JSONObject().fluentPut("resolution", -1).fluentPut("restore_face", true));

        // 注意：RequestBody.create() 方法在 OkHttp3 中参数顺序不同
        Request request = new Request.Builder()
                .url(aiTryOnConfig.getSubmitUrl())
                .addHeader("X-DashScope-Async", "enable")
                .addHeader("Authorization", "Bearer " + aiTryOnConfig.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(
                        body.toJSONString(),
                        MediaType.parse("application/json")))
                .build();

        try (Response response = client.newCall(request).execute()) {
            String responseBody = response.body() != null ? response.body().string() : "";

            // HTTP 非 2xx 直接抛错，附带原始返回便于排查
            if (!response.isSuccessful()) {
                throw new IOException("提交试衣任务失败：HTTP " + response.code() + " - " + response.message()
                        + "; body=" + responseBody);
            }

            JSONObject resJson = JSONObject.parseObject(responseBody);
            JSONObject output = resJson.getJSONObject("output");

            // 正常情况下 DashScope 会返回 output.task_id；如果没有 output，说明是错误结构
            if (output == null) {
                String code = resJson.getString("code");
                String msg = resJson.getString("message");
                String error = "调用 DashScope 试衣接口失败：" +
                        (code != null ? ("code=" + code + ", ") : "") +
                        (msg != null ? msg : "未知错误") +
                        "; 原始返回=" + resJson.toJSONString();
                throw new RuntimeException(error);
            }

            String taskId = output.getString("task_id");
            if (StringUtils.isEmpty(taskId)) {
                throw new RuntimeException("调用 DashScope 试衣接口失败：未返回 task_id，output=" + output.toJSONString());
            }

            return taskId;
        }
    }

    /**
     * 查询任务详情，返回完整结果（包含状态、时间、错误信息等）
     */
    public QueryTaskResultVO queryTask(String taskId) throws IOException {
        String url = aiTryOnConfig.getTaskQueryUrl().replace("{taskId}", taskId);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + aiTryOnConfig.getApiKey())
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("查询任务失败：HTTP " + response.code() + "，" + response.message());
            }
            String responseBody = response.body().string();
            return parseQueryResult(responseBody); // 解析返回结果为实体类
        }
    }

    /**
     * 解析阿里云返回的JSON为Java实体
     */
    private QueryTaskResultVO parseQueryResult(String json) {
        JSONObject root = JSONObject.parseObject(json);
        JSONObject output = root.getJSONObject("output");
        JSONObject usage = root.getJSONObject("usage");

        QueryTaskResultVO result = new QueryTaskResultVO();
        // 任务基本信息
        result.setTaskId(output.getString("task_id"));
        result.setTaskStatus(output.getString("task_status"));
        result.setImageUrl(output.getString("image_url"));
        // 时间字段（转换为LocalDateTime）
        result.setSubmitTime(parseDateTime(output.getString("submit_time")));
        result.setScheduledTime(parseDateTime(output.getString("scheduled_time")));
        result.setEndTime(parseDateTime(output.getString("end_time")));
        // 错误信息（失败时存在）
        result.setErrorCode(output.getString("code"));
        result.setErrorMessage(output.getString("message"));
        // 用量信息
        result.setImageCount(usage != null ? usage.getInteger("image_count") : 0);
        result.setRequestId(root.getString("request_id"));

        return result;
    }

    /**
     * 转换阿里云时间字符串（如"2024-07-30 15:39:39.918"）为LocalDateTime
     */
    private LocalDateTime parseDateTime(String timeStr) {
        if (StringUtils.isEmpty(timeStr)) return null;
        // 处理带毫秒的格式
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS");
        return LocalDateTime.parse(timeStr, formatter);
    }

    // 内部静态类：封装查询结果
//    @Data
//    public static class QueryTaskResult {
//        private String taskId;
//        private String taskStatus; // 包含PENDING/PRE-PROCESSING/RUNNING等状态
//        private String imageUrl;   // 阿里云返回的临时图片URL
//        private LocalDateTime submitTime;
//        private LocalDateTime scheduledTime;
//        private LocalDateTime endTime;
//        private String errorCode;
//        private String errorMessage;
//        private Integer imageCount;
//        private String requestId;
//    }
}
