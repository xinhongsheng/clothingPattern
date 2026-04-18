package com.xhs.clothingpatternbackend.sdk.dashscope;

import cn.hutool.core.util.StrUtil;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversation;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationParam;
import com.alibaba.dashscope.aigc.multimodalconversation.MultiModalConversationResult;
import com.alibaba.dashscope.common.MultiModalMessage;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.NoApiKeyException;
import com.alibaba.dashscope.exception.UploadFileException;
import com.alibaba.dashscope.utils.Constants;
import com.alibaba.dashscope.utils.JsonUtils;
import com.alibaba.fastjson2.JSON;
import com.xhs.clothingpatternbackend.config.BailianImageConfig;
import com.xhs.clothingpatternbackend.config.TongYiConfig;
import com.xhs.clothingpatternbackend.model.dto.mj.MJBlendRequest;
import com.xhs.clothingpatternbackend.model.dto.mj.MJImagineRequest;
import com.xhs.clothingpatternbackend.model.vo.MJImagineVO;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Component
public class BailianImageClient {

    @Resource
    private BailianImageConfig bailianImageConfig;

    @Resource
    private TongYiConfig tongYiConfig;

    @PostConstruct
    public void init() {
        Constants.baseHttpApiUrl = bailianImageConfig.getBaseUrl();
    }

    public MJImagineVO imagine(MJImagineRequest request) throws IOException {
        return callQwenImage(request, null);
    }

    public MJImagineVO executeAction(MJImagineVO source, String action) throws IOException {
        if (source == null) {
            throw new IOException("Original generation task not found");
        }
        if (StrUtil.isBlank(action) || action.startsWith("upsample")) {
            MJImagineVO copied = JSON.parseObject(JSON.toJSONString(source), MJImagineVO.class);
            copied.setSuccess(true);
            copied.setProgress(100);
            return copied;
        }

        MJImagineRequest request = new MJImagineRequest();
        request.setPrompt(source.getPrompt());
        request.setStyle(source.getStyle());
        request.setSeason(source.getSeason());
        request.setTargetAudience(source.getTargetAudience());
        request.setAction(action);
        return callQwenImage(request, action);
    }

    public MJImagineVO blend(MJBlendRequest request) throws IOException {
        List<Map<String, Object>> content = new ArrayList<>();
        for (String imageUrl : request.getImageUrls()) {
            if (StrUtil.isNotBlank(imageUrl)) {
                content.add(Collections.singletonMap("image", imageUrl));
            }
        }
        content.add(Collections.singletonMap("text", "基于参考图片融合设计一张适合服装印花使用的原创图案，画面干净，细节清晰，商业展示质感。"));
        return callQwenImage(content, "图片融合图案", null, null, null, "blend");
    }

    private MJImagineVO callQwenImage(MJImagineRequest request, String action) throws IOException {
        String prompt = buildPrompt(request, action);
        List<Map<String, Object>> content = Collections.singletonList(Collections.singletonMap("text", prompt));
        return callQwenImage(content, prompt, request.getStyle(), request.getSeason(), request.getTargetAudience(), action);
    }

    private MJImagineVO callQwenImage(List<Map<String, Object>> content,
                                     String prompt,
                                     String style,
                                     String season,
                                     String targetAudience,
                                     String action) throws IOException {
        try {
            MultiModalConversation conv = new MultiModalConversation();
            MultiModalMessage userMessage = MultiModalMessage.builder()
                    .role(Role.USER.getValue())
                    .content(content)
                    .build();

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("watermark", Boolean.TRUE.equals(bailianImageConfig.getWatermark()));
            parameters.put("prompt_extend", !Boolean.FALSE.equals(bailianImageConfig.getPromptExtend()));
            parameters.put("negative_prompt", bailianImageConfig.getNegativePrompt());
            parameters.put("size", getConfiguredSize());
            parameters.put("n", 4);

            MultiModalConversationParam param = MultiModalConversationParam.builder()
                    .apiKey(resolveApiKey())
                    .model(bailianImageConfig.getModel())
                    .messages(Collections.singletonList(userMessage))
                    .parameters(parameters)
                    .build();

            MultiModalConversationResult result = conv.call(param);
            String resultJson = JsonUtils.toJson(result);
            log.info("Bailian Qwen Image response: {}", resultJson);

            List<String> allImageUrls = BailianImageResponseParser.extractAllImageUrls(resultJson);
            if (allImageUrls.isEmpty()) {
                throw new IOException("Bailian image response has no image url");
            }
            String imageUrl = allImageUrls.get(0);

            String traceId = BailianImageResponseParser.extractTraceId(resultJson);
            if (StrUtil.isBlank(traceId)) {
                traceId = UUID.randomUUID().toString().replace("-", "");
            }
            MJImagineVO vo = toImagineVO(imageUrl, traceId, prompt, style, season, targetAudience, action);
            if (allImageUrls.size() > 1) {
                vo.setSubImageUrls(allImageUrls);
            }
            return vo;
        } catch (ApiException | NoApiKeyException | UploadFileException e) {
            throw new IOException("Bailian Qwen Image API failed: " + e.getMessage(), e);
        }
    }

    private String buildPrompt(MJImagineRequest request, String action) {
        String prompt = request.getPrompt();
        if (StrUtil.isBlank(prompt)) {
            prompt = "适合服装印花的原创图案";
        }
        if (StrUtil.isBlank(action)) {
            return prompt;
        }
        if (action.startsWith("variation")) {
            return prompt + "\n请保留主题、风格和服装图案适用性，重新生成一张构图与细节不同的变体。";
        }
        if ("reroll".equals(action)) {
            return prompt + "\n请重新生成一张新的图案方案，保持高质量、干净边缘和清晰纹理。";
        }
        return prompt;
    }

    private MJImagineVO toImagineVO(String imageUrl,
                                    String traceId,
                                    String prompt,
                                    String style,
                                    String season,
                                    String targetAudience,
                                    String action) {
        MJImagineVO vo = new MJImagineVO();
        vo.setImageUrl(imageUrl);
        vo.setRawImageUrl(imageUrl);
        int[] size = parseSize(getConfiguredSize());
        vo.setImageWidth(size[0]);
        vo.setImageHeight(size[1]);
        vo.setRawImageWidth(size[0]);
        vo.setRawImageHeight(size[1]);
        vo.setActions(List.of("variation1", "variation2", "variation3", "variation4", "reroll", "upsample1"));
        vo.setProgress(100);
        vo.setTaskId(traceId);
        vo.setImageId(traceId);
        vo.setSuccess(true);
        vo.setTraceId(traceId);
        vo.setPrompt(prompt);
        vo.setStyle(style);
        vo.setSeason(season);
        vo.setTargetAudience(targetAudience);
        vo.setPatternName(buildPatternName(prompt, action));
        return vo;
    }

    private String buildPatternName(String prompt, String action) {
        String prefix = StrUtil.isNotBlank(action) ? "图案-" + action : "图案";
        String text = StrUtil.blankToDefault(prompt, "智能生成");
        text = text.replaceAll("\\s+", "");
        if (text.length() > 12) {
            text = text.substring(0, 12);
        }
        return prefix + "-" + text;
    }

    private String resolveApiKey() {
        if (StrUtil.isNotBlank(bailianImageConfig.getApiKey())) {
            return bailianImageConfig.getApiKey();
        }
        String envKey = System.getenv("DASHSCOPE_API_KEY");
        if (StrUtil.isNotBlank(envKey)) {
            return envKey;
        }
        return tongYiConfig.getDashscopeApiKey();
    }

    private String getConfiguredSize() {
        return StrUtil.blankToDefault(bailianImageConfig.getSize(), "2048*2048");
    }

    private int[] parseSize(String size) {
        if (StrUtil.isBlank(size) || !size.contains("*")) {
            return new int[]{2048, 2048};
        }
        String[] parts = size.split("\\*");
        try {
            return new int[]{Integer.parseInt(parts[0]), Integer.parseInt(parts[1])};
        } catch (RuntimeException e) {
            return new int[]{2048, 2048};
        }
    }
}
