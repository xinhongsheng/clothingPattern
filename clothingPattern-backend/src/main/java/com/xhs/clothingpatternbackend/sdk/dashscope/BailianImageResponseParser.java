package com.xhs.clothingpatternbackend.sdk.dashscope;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class BailianImageResponseParser {

    private BailianImageResponseParser() {
    }

    public static List<String> extractAllImageUrls(String resultJson) {
        List<String> urls = new ArrayList<>();
        if (StrUtil.isBlank(resultJson)) {
            return urls;
        }
        Object root = JSON.parse(resultJson);
        Object target = root;
        if (root instanceof JSONObject rootObject) {
            Object output = rootObject.get("output");
            if (output != null) {
                target = output;
            }
        }
        collectImageUrls(target, urls);
        return urls;
    }

    private static void collectImageUrls(Object node, List<String> urls) {
        if (node == null) {
            return;
        }
        if (node instanceof JSONObject object) {
            String direct = extractKnownImageField(object);
            if (StrUtil.isNotBlank(direct) && !urls.contains(direct)) {
                urls.add(direct);
                return;
            }
            for (Map.Entry<String, Object> entry : object.entrySet()) {
                collectImageUrls(entry.getValue(), urls);
            }
        } else if (node instanceof JSONArray array) {
            for (Object item : array) {
                collectImageUrls(item, urls);
            }
        }
    }

    public static String extractImageUrl(String resultJson) {
        if (StrUtil.isBlank(resultJson)) {
            return null;
        }
        Object root = JSON.parse(resultJson);
        if (root instanceof JSONObject rootObject) {
            String outputUrl = extractImageUrl(rootObject.get("output"));
            if (StrUtil.isNotBlank(outputUrl)) {
                return outputUrl;
            }
        }
        return extractImageUrl(root);
    }

    public static String extractTraceId(String resultJson) {
        if (StrUtil.isBlank(resultJson)) {
            return null;
        }
        JSONObject root = JSON.parseObject(resultJson);
        String requestId = root.getString("request_id");
        if (StrUtil.isBlank(requestId)) {
            requestId = root.getString("requestId");
        }
        return requestId;
    }

    private static String extractImageUrl(Object node) {
        if (node == null) {
            return null;
        }
        if (node instanceof JSONObject object) {
            String direct = extractKnownImageField(object);
            if (StrUtil.isNotBlank(direct)) {
                return direct;
            }
            for (Map.Entry<String, Object> entry : object.entrySet()) {
                String nested = extractImageUrl(entry.getValue());
                if (StrUtil.isNotBlank(nested)) {
                    return nested;
                }
            }
            return null;
        }
        if (node instanceof JSONArray array) {
            for (Object item : array) {
                String nested = extractImageUrl(item);
                if (StrUtil.isNotBlank(nested)) {
                    return nested;
                }
            }
        }
        return null;
    }

    private static String extractKnownImageField(JSONObject object) {
        Object imageUrl = object.get("image_url");
        if (imageUrl instanceof String value && isImageUrl(value)) {
            return value;
        }
        if (imageUrl instanceof JSONObject nested) {
            String value = nested.getString("url");
            if (isImageUrl(value)) {
                return value;
            }
        }

        Object image = object.get("image");
        if (image instanceof String value && isImageUrl(value)) {
            return value;
        }
        if (image instanceof JSONObject nested) {
            String value = nested.getString("url");
            if (isImageUrl(value)) {
                return value;
            }
        }
        return null;
    }

    private static boolean isImageUrl(String value) {
        return StrUtil.isNotBlank(value)
                && (value.startsWith("http://") || value.startsWith("https://"));
    }
}
