package com.xhs.clothingpatternbackend.sdk.dashscope;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.xhs.clothingpatternbackend.config.WanConfig;
import com.xhs.clothingpatternbackend.model.vo.WanQueryVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Objects;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-12-02
 * @Description: 封装通义万相调用
 * @Version: 1.0
 */
@Component
@Slf4j
public class WanApiClient {


    private final OkHttpClient client = new OkHttpClient();
    @Resource
    private WanConfig wanConfig;
    /**
     * 提交多图融合任务
     * @param prompt 正向提示词
     * @param negativePrompt 反向提示词
     * @param imageUrls 图片URL列表（1-3张）
     * @param parameters 任务参数（size、n等）
     * @return 通义万相返回的taskId
     */
    public String submitFusionTask(String prompt, String negativePrompt,
                                   JSONArray imageUrls, JSONObject parameters) throws IOException {



        // 1. 选择地域对应的提交URL
        String submitUrl = "BEIJING".equals(wanConfig.getDefaultRegion())
                ? wanConfig.getBeijingSubmitUrl()
                : wanConfig.getSingaporeSubmitUrl();

        // 2. 构造请求体
        JSONObject requestBody = new JSONObject();
        requestBody.put("model", "wan2.5-i2i-preview"); // 固定模型名
        // 输入参数
        JSONObject input = new JSONObject();
        input.put("prompt", prompt);
        input.put("images", imageUrls);
        if (parameters != null && !parameters.isEmpty()) {
            requestBody.put("parameters", parameters);
        }
        if (negativePrompt != null && !negativePrompt.isEmpty()) {
            input.put("negative_prompt", negativePrompt);
        }
        requestBody.put("input", input);

        // 3. 构造请求（必须启用异步）
        Request request = new Request.Builder()
                .url(submitUrl)
                .addHeader("X-DashScope-Async", "enable")
                .addHeader("Authorization", "Bearer " + wanConfig.getApiKey())
                .addHeader("Content-Type", "application/json")
                .post(RequestBody.create(MediaType.parse("application/json"), requestBody.toJSONString()))
                .build();

        // 4. 执行请求并解析taskId
        try (Response response = client.newCall(request).execute()) {
    String responseBody = response.body() != null ? response.body().string() : "";
    if (!response.isSuccessful()) {
        log.error("通义万相提交多图融合失败：HTTP {} {}，body={}",
                  response.code(), response.message(), responseBody);
        throw new IOException("提交任务失败：" + response.code() + "，" + response.message()
                + "，body=" + responseBody);
    }
    JSONObject resJson = JSONObject.parseObject(responseBody);
    return resJson.getJSONObject("output").getString("task_id");
}
    }

    /**
     * 查询任务结果
     */
    public WanQueryVO queryTask(String taskId) throws IOException {
        // 1. 选择地域对应的查询URL
        String queryUrlTemplate = "BEIJING".equals(wanConfig.getDefaultRegion())
                ? wanConfig.getBeijingQueryUrl()
                : wanConfig.getSingaporeQueryUrl();
        String queryUrl = queryUrlTemplate.replace("{taskId}", taskId);

        // 2. 构造查询请求
        Request request = new Request.Builder()
                .url(queryUrl)
                .addHeader("Authorization", "Bearer " + wanConfig.getApiKey())
                .get()
                .build();

        // 3. 执行请求并解析结果
        try (Response response = client.newCall(request).execute()) {
            if (!response.isSuccessful()) {
                throw new IOException("查询任务失败：" + response.code() + "，" + response.message());
            }
            String responseBody = Objects.requireNonNull(response.body()).string();
            return JSONObject.parseObject(responseBody, WanQueryVO.class);
        }
    }
}
