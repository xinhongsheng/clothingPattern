package com.xhs.clothingpatternbackend.sdk.dashscope;


import com.alibaba.fastjson2.JSONObject;
import okhttp3.*;
import com.xhs.clothingpatternbackend.config.AiTryOnConfig;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Component;

import java.io.IOException;

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

    public String submitTryOn(String personUrl, String topUrl) throws IOException {
        JSONObject body = new JSONObject();
        body.put("model", "aitryon-plus");
        body.put("input", new JSONObject()
                .fluentPut("person_image_url", personUrl)
                .fluentPut("top_garment_url", topUrl));
        body.put("parameters", new JSONObject()
                .fluentPut("resolution", -1)
                .fluentPut("restore_face", true));

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
            JSONObject resJson = JSONObject.parseObject(response.body().string());
            return resJson.getJSONObject("output").getString("task_id");
        }
    }

    public JSONObject queryTask(String taskId) throws IOException {
        String url = aiTryOnConfig.getTaskQueryUrl().replace("{taskId}", taskId);
        Request request = new Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer " + aiTryOnConfig.getApiKey())
                .get()
                .build();

        try (Response response = client.newCall(request).execute()) {
            return JSONObject.parseObject(response.body().string());
        }
    }
}
