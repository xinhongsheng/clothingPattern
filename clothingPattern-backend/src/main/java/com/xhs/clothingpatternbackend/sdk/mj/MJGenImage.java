package com.xhs.clothingpatternbackend.sdk.mj;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.xhs.clothingpatternbackend.config.MJConfig;
import com.xhs.clothingpatternbackend.model.dto.mj.MJBlendRequest;
import com.xhs.clothingpatternbackend.model.dto.mj.MJImagineRequest;
import com.xhs.clothingpatternbackend.model.vo.MJImagineVO;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import okhttp3.*;

import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.SocketTimeoutException;
import java.util.concurrent.TimeUnit;

/**
 * @Author: 小辛同学
 * @CreateTime: 2025-11-28
 * @Description: Midjourney图片生成服务
 * @Version: 1.0
 */
@Slf4j
@Component
public class MJGenImage {
    
//    @Value("${mj.api.token:e93b4e9976d344a897ea34e0d99f87c1}")
//    private String apiToken;
//
//    @Value("${mj.api.url:https://api.zhishuyun.com/midjourney/imagine}")
//    private String apiUrl;

    @Resource
    private MJConfig mjConfig;
    
    private final OkHttpClient client;
    
    public MJGenImage() {
        // 创建OkHttpClient，设置超时时间
        // Midjourney生成图片通常需要30-120秒，所以设置较长的超时时间
        this.client = new OkHttpClient.Builder()
                .connectTimeout(60, TimeUnit.SECONDS)      // 连接超时60秒
                .readTimeout(180, TimeUnit.SECONDS)        // 读取超时180秒（3分钟）
                .writeTimeout(60, TimeUnit.SECONDS)        // 写入超时60秒
                .retryOnConnectionFailure(true)            // 连接失败时重试
                .build();
    }
    
    /**
     * 调用Midjourney Imagine API生成图片
     * 
     * @param request 请求参数
     * @return 生成结果
     * @throws IOException 网络请求异常
     */
    public MJImagineVO imagine(MJImagineRequest request) throws IOException {
        // 构建请求JSON
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("action", request.getAction());
        jsonObject.put("prompt", request.getPrompt());
        
        // 构建请求体
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonObject.toString(), mediaType);
        
        // 构建请求URL（带token）
        String urlWithToken = mjConfig.getUrl() + "?token=" + mjConfig.getToken();
        
        // 构建HTTP请求
        Request httpRequest = new Request.Builder()
                .url(urlWithToken)
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("content-type", "application/json")
                .build();
        
        log.info("调用Midjourney API，请求参数：{}", jsonObject.toString());
        log.info("提示：Midjourney生成图片通常需要30-120秒，请耐心等待...");
        
        // 执行请求
        try (Response response = client.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                log.error("Midjourney API调用失败，HTTP状态码：{}", response.code());
                throw new IOException("Midjourney API返回错误状态码: " + response.code());
            }
            
            // 解析响应
            String responseBody = response.body().string();
            log.info("Midjourney API响应：{}", responseBody);
            
            // 将JSON响应转换为对象
            MJImagineVO mjResponse = JSON.parseObject(responseBody, MJImagineVO.class);
            
            return mjResponse;
        } catch (SocketTimeoutException e) {
            log.error("Midjourney API调用超时，可能是生成时间过长或网络问题", e);
            throw new IOException("Midjourney API调用超时，图片生成时间较长，请稍后重试或检查网络连接", e);
        }
    }
    
    /**
     * 执行Midjourney动作（如upsample、variation等）
     * 
     * @param taskId 任务ID
     * @param imageId 图片ID
     * @param action 动作类型（upsample1-4, variation1-4, reroll等）
     * @return 执行结果
     * @throws IOException 网络请求异常
     */
    public MJImagineVO executeAction(String taskId, String imageId, String action) throws IOException {
        // 构建请求JSON
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("task_id", taskId);
        jsonObject.put("image_id", imageId);
        jsonObject.put("action", action);
        
        // 构建请求体
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonObject.toString(), mediaType);
        
        // 构建请求URL（带token）
        String urlWithToken = mjConfig.getUrl() + "?token=" + mjConfig.getToken();
        
        // 构建HTTP请求
        Request httpRequest = new Request.Builder()
                .url(urlWithToken)
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("content-type", "application/json")
                .build();
        
        log.info("执行Midjourney动作，请求参数：{}", jsonObject.toString());
        log.info("提示：Midjourney执行动作通常需要30-120秒，请耐心等待...");
        
        // 执行请求
        try (Response response = client.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                log.error("Midjourney动作执行失败，HTTP状态码：{}", response.code());
                throw new IOException("Midjourney API返回错误状态码: " + response.code());
            }
            
            // 解析响应
            String responseBody = response.body().string();
            log.info("Midjourney动作执行响应：{}", responseBody);
            
            // 将JSON响应转换为对象
            MJImagineVO mjResponse = JSON.parseObject(responseBody, MJImagineVO.class);
            
            return mjResponse;
        } catch (SocketTimeoutException e) {
            log.error("Midjourney动作执行超时，可能是生成时间过长或网络问题", e);
            throw new IOException("Midjourney动作执行超时，处理时间较长，请稍后重试或检查网络连接", e);
        }
    }
    
    /**
     * 执行Midjourney Blend（垫图/混合）操作
     * 
     * @param request Blend请求参数
     * @return 执行结果
     * @throws IOException 网络请求异常
     */
    public MJImagineVO blend(MJBlendRequest request) throws IOException {
        // 构建请求JSON
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("action", "blend");
        jsonObject.put("image_urls", request.getImageUrls());
        
        // 构建请求体
        MediaType mediaType = MediaType.parse("application/json; charset=utf-8");
        RequestBody body = RequestBody.create(jsonObject.toString(), mediaType);
        
        // 构建请求URL（带token）
        String urlWithToken = mjConfig.getUrl() + "?token=" + mjConfig.getToken();
        
        // 构建HTTP请求
        Request httpRequest = new Request.Builder()
                .url(urlWithToken)
                .post(body)
                .addHeader("accept", "application/json")
                .addHeader("content-type", "application/json")
                .build();
        
        log.info("执行Midjourney Blend，请求参数：{}", jsonObject.toString());
        log.info("提示：Midjourney Blend通常需要30-120秒，请耐心等待...");
        
        // 执行请求
        try (Response response = client.newCall(httpRequest).execute()) {
            if (!response.isSuccessful()) {
                log.error("Midjourney Blend执行失败，HTTP状态码：{}", response.code());
                throw new IOException("Midjourney API返回错误状态码: " + response.code());
            }
            
            // 解析响应
            String responseBody = response.body().string();
            log.info("Midjourney Blend响应：{}", responseBody);
            
            // 将JSON响应转换为对象
            MJImagineVO mjResponse = JSON.parseObject(responseBody, MJImagineVO.class);
            
            return mjResponse;
        } catch (SocketTimeoutException e) {
            log.error("Midjourney Blend超时，可能是生成时间过长或网络问题", e);
            throw new IOException("Midjourney Blend超时，处理时间较长，请稍后重试或检查网络连接", e);
        }
    }
}
